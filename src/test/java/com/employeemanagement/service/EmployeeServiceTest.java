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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Department department;
    private Employee employee;

    @BeforeEach
    void setUp() {
        department = Department.builder()
                .id(1L)
                .name("Engineering")
                .description("Software engineering")
                .createdAt(LocalDateTime.now())
                .build();

        employee = Employee.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phone("1234567890")
                .jobTitle("Developer")
                .salary(new BigDecimal("75000.00"))
                .employmentStatus(EmploymentStatus.ACTIVE)
                .department(department)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // --- getAllEmployees ---

    @Test
    void getAllEmployees_returnsList() {
        when(employeeRepository.findAll()).thenReturn(List.of(employee));

        List<EmployeeDto> result = employeeService.getAllEmployees();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFirstName()).isEqualTo("John");
    }

    @Test
    void getAllEmployees_emptyList() {
        when(employeeRepository.findAll()).thenReturn(Collections.emptyList());

        List<EmployeeDto> result = employeeService.getAllEmployees();

        assertThat(result).isEmpty();
    }

    // --- getEmployeeById ---

    @Test
    void getEmployeeById_found() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        EmployeeDto result = employeeService.getEmployeeById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void getEmployeeById_notFound_throwsException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getEmployeeById(99L))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessageContaining("99");
    }

    // --- searchEmployees ---

    @Test
    void searchEmployees_returnsMatches() {
        when(employeeRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("john", "john"))
                .thenReturn(List.of(employee));

        List<EmployeeDto> result = employeeService.searchEmployees("john");

        assertThat(result).hasSize(1);
    }

    @Test
    void searchEmployees_noMatches() {
        when(employeeRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("xyz", "xyz"))
                .thenReturn(Collections.emptyList());

        List<EmployeeDto> result = employeeService.searchEmployees("xyz");

        assertThat(result).isEmpty();
    }

    // --- getEmployeesByDepartment ---

    @Test
    void getEmployeesByDepartment_returnsList() {
        when(employeeRepository.findByDepartmentId(1L)).thenReturn(List.of(employee));

        List<EmployeeDto> result = employeeService.getEmployeesByDepartment(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDepartmentName()).isEqualTo("Engineering");
    }

    @Test
    void getEmployeesByDepartment_emptyList() {
        when(employeeRepository.findByDepartmentId(99L)).thenReturn(Collections.emptyList());

        List<EmployeeDto> result = employeeService.getEmployeesByDepartment(99L);

        assertThat(result).isEmpty();
    }

    // --- getEmployeesByStatus ---

    @Test
    void getEmployeesByStatus_returnsList() {
        when(employeeRepository.findByEmploymentStatus(EmploymentStatus.ACTIVE))
                .thenReturn(List.of(employee));

        List<EmployeeDto> result = employeeService.getEmployeesByStatus(EmploymentStatus.ACTIVE);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmploymentStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void getEmployeesByStatus_emptyList() {
        when(employeeRepository.findByEmploymentStatus(EmploymentStatus.SUSPENDED))
                .thenReturn(Collections.emptyList());

        List<EmployeeDto> result = employeeService.getEmployeesByStatus(EmploymentStatus.SUSPENDED);

        assertThat(result).isEmpty();
    }

    // --- createEmployee ---

    @Test
    void createEmployee_success() {
        CreateEmployeeRequest request = CreateEmployeeRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane@example.com")
                .phone("9876543210")
                .jobTitle("Designer")
                .salary(new BigDecimal("65000.00"))
                .departmentId(1L)
                .build();

        Employee savedEmployee = Employee.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Smith")
                .email("jane@example.com")
                .phone("9876543210")
                .jobTitle("Designer")
                .salary(new BigDecimal("65000.00"))
                .employmentStatus(EmploymentStatus.ACTIVE)
                .department(department)
                .createdAt(LocalDateTime.now())
                .build();

        when(employeeRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(employeeRepository.save(any(Employee.class))).thenReturn(savedEmployee);

        EmployeeDto result = employeeService.createEmployee(request);

        assertThat(result.getFirstName()).isEqualTo("Jane");
        assertThat(result.getEmploymentStatus()).isEqualTo("ACTIVE");
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void createEmployee_duplicateEmail_throwsException() {
        CreateEmployeeRequest request = CreateEmployeeRequest.builder()
                .email("john@example.com")
                .departmentId(1L)
                .build();

        when(employeeRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> employeeService.createEmployee(request))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("john@example.com");

        verify(employeeRepository, never()).save(any());
    }

    @Test
    void createEmployee_departmentNotFound_throwsException() {
        CreateEmployeeRequest request = CreateEmployeeRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane@example.com")
                .departmentId(99L)
                .build();

        when(employeeRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.createEmployee(request))
                .isInstanceOf(DepartmentNotFoundException.class)
                .hasMessageContaining("99");
    }

    // --- updateEmployee ---

    @Test
    void updateEmployee_success_allFields() {
        UpdateEmployeeRequest request = UpdateEmployeeRequest.builder()
                .firstName("John Updated")
                .lastName("Doe Updated")
                .email("john.updated@example.com")
                .phone("1111111111")
                .jobTitle("Senior Developer")
                .salary(new BigDecimal("90000.00"))
                .departmentId(1L)
                .build();

        Employee updatedEmployee = Employee.builder()
                .id(1L)
                .firstName("John Updated")
                .lastName("Doe Updated")
                .email("john.updated@example.com")
                .phone("1111111111")
                .jobTitle("Senior Developer")
                .salary(new BigDecimal("90000.00"))
                .employmentStatus(EmploymentStatus.ACTIVE)
                .department(department)
                .createdAt(employee.getCreatedAt())
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.existsByEmail("john.updated@example.com")).thenReturn(false);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(employeeRepository.save(any(Employee.class))).thenReturn(updatedEmployee);

        EmployeeDto result = employeeService.updateEmployee(1L, request);

        assertThat(result.getFirstName()).isEqualTo("John Updated");
        assertThat(result.getJobTitle()).isEqualTo("Senior Developer");
    }

    @Test
    void updateEmployee_sameEmail_noConflict() {
        UpdateEmployeeRequest request = UpdateEmployeeRequest.builder()
                .email("john@example.com")
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        EmployeeDto result = employeeService.updateEmployee(1L, request);

        assertThat(result.getEmail()).isEqualTo("john@example.com");
        verify(employeeRepository, never()).existsByEmail(anyString());
    }

    @Test
    void updateEmployee_notFound_throwsException() {
        UpdateEmployeeRequest request = UpdateEmployeeRequest.builder().build();

        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.updateEmployee(99L, request))
                .isInstanceOf(EmployeeNotFoundException.class);
    }

    @Test
    void updateEmployee_duplicateEmail_throwsException() {
        UpdateEmployeeRequest request = UpdateEmployeeRequest.builder()
                .email("existing@example.com")
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> employeeService.updateEmployee(1L, request))
                .isInstanceOf(DuplicateEmailException.class);

        verify(employeeRepository, never()).save(any());
    }

    @Test
    void updateEmployee_departmentNotFound_throwsException() {
        UpdateEmployeeRequest request = UpdateEmployeeRequest.builder()
                .departmentId(99L)
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.updateEmployee(1L, request))
                .isInstanceOf(DepartmentNotFoundException.class);
    }

    // --- updateEmploymentStatus ---

    @Test
    void updateEmploymentStatus_success() {
        Employee suspendedEmployee = Employee.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .employmentStatus(EmploymentStatus.SUSPENDED)
                .department(department)
                .createdAt(employee.getCreatedAt())
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(suspendedEmployee);

        EmployeeDto result = employeeService.updateEmploymentStatus(1L, EmploymentStatus.SUSPENDED);

        assertThat(result.getEmploymentStatus()).isEqualTo("SUSPENDED");
    }

    @Test
    void updateEmploymentStatus_notFound_throwsException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.updateEmploymentStatus(99L, EmploymentStatus.INACTIVE))
                .isInstanceOf(EmployeeNotFoundException.class);
    }

    // --- deleteEmployee ---

    @Test
    void deleteEmployee_success() {
        when(employeeRepository.existsById(1L)).thenReturn(true);

        employeeService.deleteEmployee(1L);

        verify(employeeRepository).deleteById(1L);
    }

    @Test
    void deleteEmployee_notFound_throwsException() {
        when(employeeRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> employeeService.deleteEmployee(99L))
                .isInstanceOf(EmployeeNotFoundException.class);

        verify(employeeRepository, never()).deleteById(any());
    }
}
