package com.employeemanagement.service;

import com.employeemanagement.dto.CreateDepartmentRequest;
import com.employeemanagement.dto.DepartmentDto;
import com.employeemanagement.entity.Department;
import com.employeemanagement.exception.DepartmentNotFoundException;
import com.employeemanagement.exception.DuplicateDepartmentException;
import com.employeemanagement.repository.DepartmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DepartmentService departmentService;

    private Department department;

    @BeforeEach
    void setUp() {
        department = Department.builder()
                .id(1L)
                .name("Engineering")
                .description("Software engineering")
                .createdAt(LocalDateTime.now())
                .build();
    }

    // --- getAllDepartments ---

    @Test
    void getAllDepartments_returnsList() {
        when(departmentRepository.findAll()).thenReturn(List.of(department));

        List<DepartmentDto> result = departmentService.getAllDepartments();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Engineering");
        verify(departmentRepository).findAll();
    }

    @Test
    void getAllDepartments_emptyList() {
        when(departmentRepository.findAll()).thenReturn(Collections.emptyList());

        List<DepartmentDto> result = departmentService.getAllDepartments();

        assertThat(result).isEmpty();
    }

    // --- getDepartmentById ---

    @Test
    void getDepartmentById_found() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));

        DepartmentDto result = departmentService.getDepartmentById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Engineering");
    }

    @Test
    void getDepartmentById_notFound_throwsException() {
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.getDepartmentById(99L))
                .isInstanceOf(DepartmentNotFoundException.class)
                .hasMessageContaining("99");
    }

    // --- searchDepartments ---

    @Test
    void searchDepartments_returnsMatches() {
        when(departmentRepository.findByNameContainingIgnoreCase("eng"))
                .thenReturn(List.of(department));

        List<DepartmentDto> result = departmentService.searchDepartments("eng");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Engineering");
    }

    @Test
    void searchDepartments_noMatches() {
        when(departmentRepository.findByNameContainingIgnoreCase("xyz"))
                .thenReturn(Collections.emptyList());

        List<DepartmentDto> result = departmentService.searchDepartments("xyz");

        assertThat(result).isEmpty();
    }

    // --- createDepartment ---

    @Test
    void createDepartment_success() {
        CreateDepartmentRequest request = CreateDepartmentRequest.builder()
                .name("Finance")
                .description("Finance dept")
                .build();

        when(departmentRepository.existsByName("Finance")).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenReturn(
                Department.builder().id(2L).name("Finance").description("Finance dept")
                        .createdAt(LocalDateTime.now()).build());

        DepartmentDto result = departmentService.createDepartment(request);

        assertThat(result.getName()).isEqualTo("Finance");
        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    void createDepartment_duplicateName_throwsException() {
        CreateDepartmentRequest request = CreateDepartmentRequest.builder()
                .name("Engineering")
                .build();

        when(departmentRepository.existsByName("Engineering")).thenReturn(true);

        assertThatThrownBy(() -> departmentService.createDepartment(request))
                .isInstanceOf(DuplicateDepartmentException.class)
                .hasMessageContaining("Engineering");

        verify(departmentRepository, never()).save(any());
    }

    // --- updateDepartment ---

    @Test
    void updateDepartment_success() {
        CreateDepartmentRequest request = CreateDepartmentRequest.builder()
                .name("Engineering Updated")
                .description("Updated description")
                .build();

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(departmentRepository.existsByName("Engineering Updated")).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenReturn(
                Department.builder().id(1L).name("Engineering Updated")
                        .description("Updated description").createdAt(department.getCreatedAt()).build());

        DepartmentDto result = departmentService.updateDepartment(1L, request);

        assertThat(result.getName()).isEqualTo("Engineering Updated");
    }

    @Test
    void updateDepartment_sameName_success() {
        CreateDepartmentRequest request = CreateDepartmentRequest.builder()
                .name("Engineering")
                .description("Updated description")
                .build();

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(departmentRepository.save(any(Department.class))).thenReturn(department);

        DepartmentDto result = departmentService.updateDepartment(1L, request);

        assertThat(result.getName()).isEqualTo("Engineering");
        verify(departmentRepository, never()).existsByName(anyString());
    }

    @Test
    void updateDepartment_notFound_throwsException() {
        CreateDepartmentRequest request = CreateDepartmentRequest.builder()
                .name("Whatever")
                .build();

        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.updateDepartment(99L, request))
                .isInstanceOf(DepartmentNotFoundException.class);
    }

    @Test
    void updateDepartment_duplicateName_throwsException() {
        CreateDepartmentRequest request = CreateDepartmentRequest.builder()
                .name("Finance")
                .build();

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(departmentRepository.existsByName("Finance")).thenReturn(true);

        assertThatThrownBy(() -> departmentService.updateDepartment(1L, request))
                .isInstanceOf(DuplicateDepartmentException.class)
                .hasMessageContaining("Finance");

        verify(departmentRepository, never()).save(any());
    }
}
