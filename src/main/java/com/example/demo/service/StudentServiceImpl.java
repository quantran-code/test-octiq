package com.example.demo.service;

import com.example.demo.dto.StudentCreateDto;
import com.example.demo.dto.StudentResponseDto;
import com.example.demo.dto.StudentUpdateDto;
import com.example.demo.entity.Student;
import com.example.demo.exception.StudentNotFoundException;
import com.example.demo.mapper.StudentMapper;
import com.example.demo.repository.StudentRepository;
import com.example.demo.validator.StudentValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final StudentValidator studentValidator;

    @Override
    public StudentResponseDto create(StudentCreateDto createDto) {
        studentValidator.validateEmailAvailableForCreate(createDto.getEmail());
        Student student = StudentMapper.toEntity(createDto);
        Student saved = studentRepository.save(student);
        return StudentMapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponseDto findById(Long id) {
        Student student = getStudentOrThrow(id);
        return StudentMapper.toResponseDto(student);
    }

    @Override
    public StudentResponseDto update(Long id, StudentUpdateDto updateDto) {
        Student student = getStudentOrThrow(id);
        studentValidator.validateEmailAvailableForUpdate(id, updateDto.getEmail());
        StudentMapper.applyUpdate(student, updateDto);
        Student saved = studentRepository.save(student);
        return StudentMapper.toResponseDto(saved);
    }

    @Override
    public void delete(Long id) {
        Student student = getStudentOrThrow(id);
        studentRepository.delete(student);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentResponseDto> search(String term, Pageable pageable) {
        Page<Student> page;
        if (term == null || term.isBlank()) {
            page = studentRepository.findAll(pageable);
        } else {
            page = studentRepository.search(term, pageable);
        }
        return page.map(StudentMapper::toResponseDto);
    }

    private Student getStudentOrThrow(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException(id));
    }
}
