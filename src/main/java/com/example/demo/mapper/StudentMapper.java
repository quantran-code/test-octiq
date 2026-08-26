package com.example.demo.mapper;

import com.example.demo.dto.StudentCreateDto;
import com.example.demo.dto.StudentResponseDto;
import com.example.demo.dto.StudentUpdateDto;
import com.example.demo.entity.Student;

public final class StudentMapper {

    private StudentMapper() {
    }

    public static Student toEntity(StudentCreateDto dto) {
        Student student = new Student();
        student.setFirstName(dto.getFirstName());
        student.setLastName(dto.getLastName());
        student.setEmail(dto.getEmail());
        student.setAge(dto.getAge());
        return student;
    }

    public static void applyUpdate(Student student, StudentUpdateDto dto) {
        student.setFirstName(dto.getFirstName());
        student.setLastName(dto.getLastName());
        student.setEmail(dto.getEmail());
        student.setAge(dto.getAge());
    }

    public static StudentResponseDto toResponseDto(Student student) {
        return StudentResponseDto.builder()
                .id(student.getId())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .email(student.getEmail())
                .age(student.getAge())
                .build();
    }
}
