package com.example.demo.validator;

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

    public void validateEmailAvailableForCreate(String email) {
        studentRepository.findByEmailIgnoreCase(email).ifPresent(existing -> {
            throw new DuplicateEmailException(email);
        });
    }

    public void validateEmailAvailableForUpdate(Long studentId, String email) {
        Optional<Student> existing = studentRepository.findByEmailIgnoreCase(email);
        if (existing.isPresent() && !existing.get().getId().equals(studentId)) {
            throw new DuplicateEmailException(email);
        }
    }
}
