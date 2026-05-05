package com.employeemanagement.controller;

import com.employeemanagement.dto.CreateDepartmentRequest;
import com.employeemanagement.dto.DepartmentDto;
import com.employeemanagement.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@Tag(name = "Departments", description = "Department management endpoints")
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    @Operation(
            summary = "Get all departments",
            description = "Returns all departments. Accessible by EMPLOYEE, HR, ADMIN.",
            responses = @ApiResponse(responseCode = "200", description = "List of departments")
    )
    public ResponseEntity<List<DepartmentDto>> getAllDepartments() {
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get department by ID",
            description = "Returns a single department. Accessible by EMPLOYEE, HR, ADMIN.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Department found"),
                    @ApiResponse(responseCode = "404", description = "Department not found")
            }
    )
    public ResponseEntity<DepartmentDto> getDepartmentById(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.getDepartmentById(id));
    }

    @GetMapping("/search")
    @Operation(
            summary = "Search departments by name",
            description = "Searches departments by keyword. Accessible by HR, ADMIN.",
            responses = @ApiResponse(responseCode = "200", description = "Search results")
    )
    public ResponseEntity<List<DepartmentDto>> searchDepartments(@RequestParam String keyword) {
        return ResponseEntity.ok(departmentService.searchDepartments(keyword));
    }

    @PostMapping
    @Operation(
            summary = "Create a new department",
            description = "Creates a new department. Accessible by ADMIN only.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(examples = @ExampleObject(value = """
                            {
                              "name": "Marketing",
                              "description": "Marketing and communications"
                            }
                            """))
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Department created"),
                    @ApiResponse(responseCode = "409", description = "Department name already exists")
            }
    )
    public ResponseEntity<DepartmentDto> createDepartment(@Valid @RequestBody CreateDepartmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(departmentService.createDepartment(request));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update a department",
            description = "Updates an existing department. Accessible by ADMIN only.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(examples = @ExampleObject(value = """
                            {
                              "name": "Marketing Updated",
                              "description": "Updated description"
                            }
                            """))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Department updated"),
                    @ApiResponse(responseCode = "404", description = "Department not found"),
                    @ApiResponse(responseCode = "409", description = "Department name already exists")
            }
    )
    public ResponseEntity<DepartmentDto> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody CreateDepartmentRequest request) {
        return ResponseEntity.ok(departmentService.updateDepartment(id, request));
    }
}
