package com.example.demo.paginator;

import com.example.demo.dto.PagedResponse;
import com.example.demo.dto.StudentResponse;
import com.example.demo.entity.Student;
import com.example.demo.mapper.StudentMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class Paginator {

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    public PagedResponse<StudentResponse> toPagedResponse(Page<Student> page, StudentMapper mapper) {
        List<StudentResponse> items = page.getContent().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
        return PagedResponse.<StudentResponse>builder()
                .items(items)
                .page(page.getNumber())
                .size(page.getSize())
                .total(page.getTotalElements())
                .build();
    }

    public Pageable buildPageable(Integer page, Integer size, String sort) {
        int resolvedPage = (page == null || page < 0) ? DEFAULT_PAGE : page;
        int resolvedSize = (size == null || size <= 0) ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);

        if (sort == null || sort.isBlank()) {
            return PageRequest.of(resolvedPage, resolvedSize);
        }

        String[] parts = sort.split(",");
        String property = parts[0].trim();
        Sort.Direction direction = Sort.Direction.ASC;
        if (parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim())) {
            direction = Sort.Direction.DESC;
        }
        return PageRequest.of(resolvedPage, resolvedSize, Sort.by(direction, property));
    }
}
