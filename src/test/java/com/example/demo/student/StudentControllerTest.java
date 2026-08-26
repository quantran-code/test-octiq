package com.example.demo.student;

import com.example.demo.dto.StudentCreateDto;
import com.example.demo.dto.StudentUpdateDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createStudent_valid_returns201() throws Exception {
        StudentCreateDto dto = new StudentCreateDto("Alice", "Walker", "alice.walker@example.com", 21);

        mockMvc.perform(post("/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.firstName").value("Alice"))
                .andExpect(jsonPath("$.email").value("alice.walker@example.com"));
    }

    @Test
    void createStudent_invalid_returns400() throws Exception {
        StudentCreateDto dto = new StudentCreateDto("", "Walker", "not-an-email", -5);

        mockMvc.perform(post("/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void getById_existing_returns200_unknown_returns404() throws Exception {
        StudentCreateDto dto = new StudentCreateDto("Bob", "Smith", "bob.smith@example.com", 22);
        MvcResult createResult = mockMvc.perform(post("/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/students/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("bob.smith@example.com"));

        mockMvc.perform(get("/students/{id}", 999999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateStudent_fullUpdate_returns200_duplicateEmail_returns409() throws Exception {
        StudentCreateDto first = new StudentCreateDto("Carl", "Jones", "carl.jones@example.com", 30);
        StudentCreateDto second = new StudentCreateDto("Dana", "White", "dana.white@example.com", 28);

        MvcResult firstResult = mockMvc.perform(post("/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(first)))
                .andExpect(status().isCreated())
                .andReturn();
        Long firstId = objectMapper.readTree(firstResult.getResponse().getContentAsString()).get("id").asLong();

        MvcResult secondResult = mockMvc.perform(post("/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(second)))
                .andExpect(status().isCreated())
                .andReturn();
        Long secondId = objectMapper.readTree(secondResult.getResponse().getContentAsString()).get("id").asLong();

        StudentUpdateDto update = new StudentUpdateDto("Carl", "Jones", "carl.jones.updated@example.com", 31);
        mockMvc.perform(put("/students/{id}", firstId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("carl.jones.updated@example.com"));

        StudentUpdateDto duplicateUpdate = new StudentUpdateDto("Dana", "White", "carl.jones.updated@example.com", 28);
        mockMvc.perform(put("/students/{id}", secondId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateUpdate)))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteStudent_existing_returns204_unknown_returns404() throws Exception {
        StudentCreateDto dto = new StudentCreateDto("Eve", "Brown", "eve.brown@example.com", 25);
        MvcResult createResult = mockMvc.perform(post("/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();
        Long id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/students/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/students/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void listStudents_paged_returns200WithMetadata() throws Exception {
        for (int i = 0; i < 3; i++) {
            StudentCreateDto dto = new StudentCreateDto("Page" + i, "Test", "page" + i + "@example.com", 20 + i);
            mockMvc.perform(post("/students")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(get("/students").param("page", "0").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").exists());
    }

    @Test
    void searchStudents_caseInsensitive_returnsOnlyMatching() throws Exception {
        StudentCreateDto match = new StudentCreateDto("Zach", "Unique", "zach.unique@example.com", 26);
        StudentCreateDto noMatch = new StudentCreateDto("Other", "Person", "other.person@example.com", 27);

        mockMvc.perform(post("/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(match)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(noMatch)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/students").param("search", "unique"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].email").value("zach.unique@example.com"));
    }
}
