package com.example.demo.service;

import com.example.demo.dto.StudentCreateDto;
import com.example.demo.dto.StudentResponseDto;
import com.example.demo.dto.StudentUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StudentService {

    StudentResponseDto create(StudentCreateDto createDto);

    StudentResponseDto findById(Long id);

    StudentResponseDto update(Long id, StudentUpdateDto updateDto);

    void delete(Long id);

    Page<StudentResponseDto> search(String term, Pageable pageable);
}
