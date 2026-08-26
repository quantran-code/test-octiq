package com.example.demo;

import com.example.demo.dto.StudentRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private StudentRequest validRequest(String firstName, String lastName, String email, Integer age) {
        StudentRequest request = new StudentRequest();
        request.setFirstName(firstName);
        request.setLastName(lastName);
        request.setEmail(email);
        request.setAge(age);
        return request;
    }

    @Test
    void createStudent_valid_returns201() throws Exception {
        StudentRequest request = validRequest("John", "Doe", "john.doe@example.com", 20);

        mockMvc.perform(post("/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    void createStudent_missingFirstName_returns400() throws Exception {
        StudentRequest request = validRequest(null, "Doe", "missing.first@example.com", 20);

        mockMvc.perform(post("/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createStudent_invalidEmail_returns400() throws Exception {
        StudentRequest request = validRequest("Jane", "Doe", "not-an-email", 20);

        mockMvc.perform(post("/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createStudent_duplicateEmail_returns400() throws Exception {
        StudentRequest first = validRequest("Alice", "Smith", "alice.smith@example.com", 22);
        mockMvc.perform(post("/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(first)))
                .andExpect(status().isCreated());

        StudentRequest duplicate = validRequest("Alicia", "Smithson", "alice.smith@example.com", 25);
        mockMvc.perform(post("/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getById_existingAndUnknown() throws Exception {
        StudentRequest request = validRequest("Bob", "Brown", "bob.brown@example.com", 30);
        String response = mockMvc.perform(post("/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/students/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("bob.brown@example.com"));

        mockMvc.perform(get("/students/{id}", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void update_existingAndUnknown() throws Exception {
        StudentRequest request = validRequest("Carl", "Green", "carl.green@example.com", 28);
        String response = mockMvc.perform(post("/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        StudentRequest updateRequest = validRequest("Carl", "Greenwood", "carl.greenwood@example.com", 29);

        mockMvc.perform(put("/students/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Greenwood"));

        mockMvc.perform(put("/students/{id}", 999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_existingAndUnknown() throws Exception {
        StudentRequest request = validRequest("Dana", "White", "dana.white@example.com", 24);
        String response = mockMvc.perform(post("/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/students/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/students/{id}", 999999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void search_matchesFirstLastOrEmail() throws Exception {
        mockMvc.perform(post("/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest("Eve", "Adams", "eve.adams@example.com", 31))))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest("Frank", "Miller", "frank.miller@example.com", 40))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/students").param("q", "adams"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].lastName").value("Adams"));
    }

    @Test
    void pagination_returnsPagedResponseShape() throws Exception {
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/students")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    validRequest("Student" + i, "Last" + i, "student" + i + "@example.com", 20 + i))))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(get("/students").param("page", "0").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.total", greaterThanOrEqualTo(5)));
    }
}
