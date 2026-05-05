package com.employeemanagement.service;

import com.employeemanagement.dto.CreateEmployeeRequest;
import com.employeemanagement.dto.EmployeeDto;
import com.employeemanagement.dto.UpdateEmployeeRequest;
import com.employeemanagement.entity.Department;
import com.employeemanagement.entity.Employee;
import com.employeemanagement.enums.EmploymentStatus;
import com.employeemanagement.exception.DepartmentNotFoundException;
import com.employeemanagement.exception.DuplicateEmailException;
import com.employeemanagement.exception.EmployeeNotFoundException;
import com.employeemanagement.repository.DepartmentRepository;
import com.employeemanagement.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    public List<EmployeeDto> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public EmployeeDto getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
        return toDto(employee);
    }

    public List<EmployeeDto> searchEmployees(String keyword) {
        return employeeRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(keyword, keyword)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<EmployeeDto> getEmployeesByDepartment(Long departmentId) {
        return employeeRepository.findByDepartmentId(departmentId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<EmployeeDto> getEmployeesByStatus(EmploymentStatus status) {
        return employeeRepository.findByEmploymentStatus(status).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public EmployeeDto createEmployee(CreateEmployeeRequest request) {
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new DepartmentNotFoundException(request.getDepartmentId()));

        Employee employee = Employee.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .jobTitle(request.getJobTitle())
                .salary(request.getSalary())
                .employmentStatus(EmploymentStatus.ACTIVE)
                .department(department)
                .build();

        Employee saved = employeeRepository.save(employee);
        return toDto(saved);
    }

    @Transactional
    public EmployeeDto updateEmployee(Long id, UpdateEmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));

        if (request.getEmail() != null && !request.getEmail().equals(employee.getEmail())
                && employeeRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }

        if (request.getFirstName() != null) {
            employee.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            employee.setLastName(request.getLastName());
        }
        if (request.getEmail() != null) {
            employee.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            employee.setPhone(request.getPhone());
        }
        if (request.getJobTitle() != null) {
            employee.setJobTitle(request.getJobTitle());
        }
        if (request.getSalary() != null) {
            employee.setSalary(request.getSalary());
        }
        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new DepartmentNotFoundException(request.getDepartmentId()));
            employee.setDepartment(department);
        }

        Employee saved = employeeRepository.save(employee);
        return toDto(saved);
    }

    @Transactional
    public EmployeeDto updateEmploymentStatus(Long id, EmploymentStatus status) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));

        employee.setEmploymentStatus(status);
        Employee saved = employeeRepository.save(employee);
        return toDto(saved);
    }

    @Transactional
    public void deleteEmployee(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new EmployeeNotFoundException(id);
        }
        employeeRepository.deleteById(id);
    }

    private EmployeeDto toDto(Employee employee) {
        return EmployeeDto.builder()
                .id(employee.getId())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .phone(employee.getPhone())
                .jobTitle(employee.getJobTitle())
                .salary(employee.getSalary())
                .employmentStatus(employee.getEmploymentStatus().name())
                .departmentName(employee.getDepartment() != null ? employee.getDepartment().getName() : null)
                .departmentId(employee.getDepartment() != null ? employee.getDepartment().getId() : null)
                .createdAt(employee.getCreatedAt())
                .build();
    }
}
