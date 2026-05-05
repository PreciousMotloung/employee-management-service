package com.employeemanagement.controller;

import com.employeemanagement.dto.*;
import com.employeemanagement.enums.EmploymentStatus;
import com.employeemanagement.service.EmployeeService;
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
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Tag(name = "Employees", description = "Employee management endpoints")
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    @Operation(
            summary = "Get all employees",
            description = "Returns all employees. Accessible by HR, ADMIN.",
            responses = @ApiResponse(responseCode = "200", description = "List of employees")
    )
    public ResponseEntity<List<EmployeeDto>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get employee by ID",
            description = "Returns a single employee. Accessible by HR, ADMIN.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Employee found"),
                    @ApiResponse(responseCode = "404", description = "Employee not found")
            }
    )
    public ResponseEntity<EmployeeDto> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @GetMapping("/search")
    @Operation(
            summary = "Search employees by name",
            description = "Searches employees by first or last name. Accessible by HR, ADMIN.",
            responses = @ApiResponse(responseCode = "200", description = "Search results")
    )
    public ResponseEntity<List<EmployeeDto>> searchEmployees(@RequestParam String keyword) {
        return ResponseEntity.ok(employeeService.searchEmployees(keyword));
    }

    @GetMapping("/department/{departmentId}")
    @Operation(
            summary = "Get employees by department",
            description = "Returns employees in a given department. Accessible by HR, ADMIN.",
            responses = @ApiResponse(responseCode = "200", description = "List of employees")
    )
    public ResponseEntity<List<EmployeeDto>> getEmployeesByDepartment(@PathVariable Long departmentId) {
        return ResponseEntity.ok(employeeService.getEmployeesByDepartment(departmentId));
    }

    @GetMapping("/status/{status}")
    @Operation(
            summary = "Get employees by status",
            description = "Returns employees with a given employment status. Accessible by HR, ADMIN.",
            responses = @ApiResponse(responseCode = "200", description = "List of employees")
    )
    public ResponseEntity<List<EmployeeDto>> getEmployeesByStatus(@PathVariable EmploymentStatus status) {
        return ResponseEntity.ok(employeeService.getEmployeesByStatus(status));
    }

    @PostMapping
    @Operation(
            summary = "Create a new employee",
            description = "Creates a new employee. Accessible by HR, ADMIN.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(examples = @ExampleObject(value = """
                            {
                              "firstName": "John",
                              "lastName": "Doe",
                              "email": "john.doe@example.com",
                              "phone": "1234567890",
                              "jobTitle": "Developer",
                              "salary": 75000.00,
                              "departmentId": 1
                            }
                            """))
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Employee created"),
                    @ApiResponse(responseCode = "409", description = "Email already exists"),
                    @ApiResponse(responseCode = "404", description = "Department not found")
            }
    )
    public ResponseEntity<EmployeeDto> createEmployee(@Valid @RequestBody CreateEmployeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.createEmployee(request));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update an employee",
            description = "Updates an existing employee. Accessible by HR, ADMIN.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(examples = @ExampleObject(value = """
                            {
                              "firstName": "John",
                              "lastName": "Doe Updated",
                              "email": "john.updated@example.com",
                              "jobTitle": "Senior Developer",
                              "salary": 90000.00
                            }
                            """))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Employee updated"),
                    @ApiResponse(responseCode = "404", description = "Employee not found"),
                    @ApiResponse(responseCode = "409", description = "Email already exists")
            }
    )
    public ResponseEntity<EmployeeDto> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEmployeeRequest request) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, request));
    }

    @PatchMapping("/{id}/status")
    @Operation(
            summary = "Update employee status",
            description = "Updates the employment status. Accessible by ADMIN only.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(examples = @ExampleObject(value = """
                            {
                              "employmentStatus": "SUSPENDED"
                            }
                            """))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Status updated"),
                    @ApiResponse(responseCode = "404", description = "Employee not found")
            }
    )
    public ResponseEntity<EmployeeDto> updateEmploymentStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request) {
        EmploymentStatus status = EmploymentStatus.valueOf(request.getEmploymentStatus().toUpperCase());
        return ResponseEntity.ok(employeeService.updateEmploymentStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete an employee",
            description = "Deletes an employee. Accessible by ADMIN only.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Employee deleted"),
                    @ApiResponse(responseCode = "404", description = "Employee not found")
            }
    )
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
