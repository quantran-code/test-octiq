package com.example.demo.controller;

import com.example.demo.dto.StudentCreateDto;
import com.example.demo.dto.StudentResponseDto;
import com.example.demo.dto.StudentUpdateDto;
import com.example.demo.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @PostMapping
    public ResponseEntity<StudentResponseDto> create(@Valid @RequestBody StudentCreateDto createDto) {
        StudentResponseDto response = studentService.create(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentResponseDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentResponseDto> update(@PathVariable Long id, @Valid @RequestBody StudentUpdateDto updateDto) {
        return ResponseEntity.ok(studentService.update(id, updateDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        studentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<StudentResponseDto>> list(
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return ResponseEntity.ok(studentService.search(search, pageable));
    }
}
