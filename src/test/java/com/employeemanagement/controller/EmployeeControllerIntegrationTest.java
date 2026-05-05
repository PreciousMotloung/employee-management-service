package com.employeemanagement.controller;

import com.employeemanagement.dto.CreateEmployeeRequest;
import com.employeemanagement.dto.RegisterRequest;
import com.employeemanagement.service.AuthService;
import com.employeemanagement.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EmployeeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private EmployeeService employeeService;

    private String employeeToken;
    private String hrToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        employeeToken = authService.register(RegisterRequest.builder()
                .email("emp@test.com").password("password123").role("EMPLOYEE").build()).getToken();
        hrToken = authService.register(RegisterRequest.builder()
                .email("hr@test.com").password("password123").role("HR").build()).getToken();
        adminToken = authService.register(RegisterRequest.builder()
                .email("admin@test.com").password("password123").role("ADMIN").build()).getToken();
    }

    private String createEmployeeJson(String firstName, String lastName, String email) {
        return """
                {
                  "firstName": "%s",
                  "lastName": "%s",
                  "email": "%s",
                  "phone": "1234567890",
                  "jobTitle": "Developer",
                  "salary": 75000.00,
                  "departmentId": 1
                }
                """.formatted(firstName, lastName, email);
    }

    // --- Role-based access: EMPLOYEE cannot create ---

    @Test
    void createEmployee_employee_returns403() throws Exception {
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createEmployeeJson("John", "Doe", "john@example.com"))
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }

    // --- Role-based access: EMPLOYEE cannot delete ---

    @Test
    void deleteEmployee_employee_returns403() throws Exception {
        mockMvc.perform(delete("/api/employees/1")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }

    // --- Role-based access: EMPLOYEE cannot list employees ---

    @Test
    void getAllEmployees_employee_returns403() throws Exception {
        mockMvc.perform(get("/api/employees")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }

    // --- HR can create but not delete ---

    @Test
    void createEmployee_hr_returns201() throws Exception {
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createEmployeeJson("John", "Doe", "john@example.com"))
                        .header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.employmentStatus").value("ACTIVE"));
    }

    @Test
    void deleteEmployee_hr_returns403() throws Exception {
        // Create employee first as admin
        var emp = employeeService.createEmployee(CreateEmployeeRequest.builder()
                .firstName("ToDelete").lastName("User").email("todelete@example.com")
                .departmentId(1L).salary(new BigDecimal("50000")).build());

        mockMvc.perform(delete("/api/employees/" + emp.getId())
                        .header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateEmployeeStatus_hr_returns403() throws Exception {
        var emp = employeeService.createEmployee(CreateEmployeeRequest.builder()
                .firstName("Status").lastName("Test").email("status@example.com")
                .departmentId(1L).salary(new BigDecimal("50000")).build());

        mockMvc.perform(patch("/api/employees/" + emp.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"employmentStatus": "SUSPENDED"}
                                """)
                        .header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isForbidden());
    }

    // --- ADMIN can do everything ---

    @Test
    void createEmployee_admin_returns201() throws Exception {
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createEmployeeJson("Jane", "Smith", "jane@example.com"))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Jane"));
    }

    @Test
    void deleteEmployee_admin_returns204() throws Exception {
        var emp = employeeService.createEmployee(CreateEmployeeRequest.builder()
                .firstName("ToDelete").lastName("Admin").email("deleteadmin@example.com")
                .departmentId(1L).salary(new BigDecimal("50000")).build());

        mockMvc.perform(delete("/api/employees/" + emp.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateEmployeeStatus_admin_returns200() throws Exception {
        var emp = employeeService.createEmployee(CreateEmployeeRequest.builder()
                .firstName("Status").lastName("Admin").email("statusadmin@example.com")
                .departmentId(1L).salary(new BigDecimal("50000")).build());

        mockMvc.perform(patch("/api/employees/" + emp.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"employmentStatus": "SUSPENDED"}
                                """)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employmentStatus").value("SUSPENDED"));
    }

    @Test
    void getAllEmployees_admin_returns200() throws Exception {
        mockMvc.perform(get("/api/employees")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void getEmployeeById_admin_returns200() throws Exception {
        var emp = employeeService.createEmployee(CreateEmployeeRequest.builder()
                .firstName("Get").lastName("ById").email("getbyid@example.com")
                .departmentId(1L).salary(new BigDecimal("50000")).build());

        mockMvc.perform(get("/api/employees/" + emp.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Get"));
    }

    // --- Search returns correct results ---

    @Test
    void searchEmployees_returnsCorrectResults() throws Exception {
        employeeService.createEmployee(CreateEmployeeRequest.builder()
                .firstName("Alice").lastName("Wonder").email("alice@example.com")
                .departmentId(1L).salary(new BigDecimal("60000")).build());
        employeeService.createEmployee(CreateEmployeeRequest.builder()
                .firstName("Bob").lastName("Builder").email("bob@example.com")
                .departmentId(1L).salary(new BigDecimal("55000")).build());

        mockMvc.perform(get("/api/employees/search")
                        .param("keyword", "Alice")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].firstName").value("Alice"));
    }

    @Test
    void getEmployeesByDepartment_returns200() throws Exception {
        employeeService.createEmployee(CreateEmployeeRequest.builder()
                .firstName("Dept").lastName("Test").email("depttest@example.com")
                .departmentId(1L).salary(new BigDecimal("60000")).build());

        mockMvc.perform(get("/api/employees/department/1")
                        .header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getEmployeesByStatus_returns200() throws Exception {
        employeeService.createEmployee(CreateEmployeeRequest.builder()
                .firstName("Active").lastName("Employee").email("active@example.com")
                .departmentId(1L).salary(new BigDecimal("60000")).build());

        mockMvc.perform(get("/api/employees/status/ACTIVE")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].employmentStatus").value("ACTIVE"));
    }

    // --- Duplicate email returns 409 ---

    @Test
    void createEmployee_duplicateEmail_returns409() throws Exception {
        String json = createEmployeeJson("John", "Doe", "dup@example.com");

        // First create
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isCreated());

        // Duplicate
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isConflict());
    }

    // --- Non-existent employee returns 404 ---

    @Test
    void getEmployeeById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/employees/999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateEmployee_notFound_returns404() throws Exception {
        mockMvc.perform(put("/api/employees/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName": "Nobody"}
                                """)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteEmployee_notFound_returns404() throws Exception {
        mockMvc.perform(delete("/api/employees/999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    // --- Update employee ---

    @Test
    void updateEmployee_hr_returns200() throws Exception {
        var emp = employeeService.createEmployee(CreateEmployeeRequest.builder()
                .firstName("Update").lastName("Test").email("update@example.com")
                .departmentId(1L).salary(new BigDecimal("60000")).build());

        mockMvc.perform(put("/api/employees/" + emp.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName": "Updated", "lastName": "Name"}
                                """)
                        .header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"));
    }

    // --- No auth ---

    @Test
    void getAllEmployees_noAuth_returns403() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isForbidden());
    }
}
