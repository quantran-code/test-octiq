package com.example.demo.validator;

import com.example.demo.dto.StudentRequest;
import com.example.demo.entity.Student;
import com.example.demo.exception.DuplicateEmailException;
import com.example.demo.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class StudentValidator {

    private final StudentRepository studentRepository;

    public void validateForCreate(StudentRequest request) {
        Optional<Student> existing = studentRepository.findByEmailIgnoreCase(request.getEmail());
        if (existing.isPresent()) {
            throw new DuplicateEmailException(request.getEmail());
        }
    }

    public void validateForUpdate(Long id, StudentRequest request) {
        Optional<Student> existing = studentRepository.findByEmailIgnoreCase(request.getEmail());
        if (existing.isPresent() && !existing.get().getId().equals(id)) {
            throw new DuplicateEmailException(request.getEmail());
        }
    }
}
