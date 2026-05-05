package com.employeemanagement.service;

import com.employeemanagement.dto.CreateDepartmentRequest;
import com.employeemanagement.dto.DepartmentDto;
import com.employeemanagement.entity.Department;
import com.employeemanagement.exception.DepartmentNotFoundException;
import com.employeemanagement.exception.DuplicateDepartmentException;
import com.employeemanagement.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public List<DepartmentDto> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public DepartmentDto getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new DepartmentNotFoundException(id));
        return toDto(department);
    }

    public List<DepartmentDto> searchDepartments(String keyword) {
        return departmentRepository.findByNameContainingIgnoreCase(keyword).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public DepartmentDto createDepartment(CreateDepartmentRequest request) {
        if (departmentRepository.existsByName(request.getName())) {
            throw new DuplicateDepartmentException(request.getName());
        }

        Department department = Department.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        Department saved = departmentRepository.save(department);
        return toDto(saved);
    }

    @Transactional
    public DepartmentDto updateDepartment(Long id, CreateDepartmentRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new DepartmentNotFoundException(id));

        if (!department.getName().equals(request.getName())
                && departmentRepository.existsByName(request.getName())) {
            throw new DuplicateDepartmentException(request.getName());
        }

        department.setName(request.getName());
        department.setDescription(request.getDescription());

        Department saved = departmentRepository.save(department);
        return toDto(saved);
    }

    private DepartmentDto toDto(Department department) {
        return DepartmentDto.builder()
                .id(department.getId())
                .name(department.getName())
                .description(department.getDescription())
                .createdAt(department.getCreatedAt())
                .build();
    }
}
