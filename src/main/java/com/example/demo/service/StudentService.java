package com.example.demo.service;

import com.example.demo.dto.StudentRequest;
import com.example.demo.dto.StudentResponse;
import com.example.demo.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface StudentService {

    StudentResponse create(StudentRequest request);

    StudentResponse update(Long id, StudentRequest request);

    void delete(Long id);

    StudentResponse findById(Long id);

    Page<Student> findAll(Pageable pageable, Optional<String> q);
}
