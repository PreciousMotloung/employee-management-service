package com.employeemanagement.controller;

import com.employeemanagement.dto.RegisterRequest;
import com.employeemanagement.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DepartmentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

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

    // --- GET /api/departments ---

    @Test
    void getAllDepartments_employee_returns200() throws Exception {
        mockMvc.perform(get("/api/departments")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3))); // seeded departments
    }

    @Test
    void getAllDepartments_noAuth_returns403() throws Exception {
        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isForbidden());
    }

    // --- GET /api/departments/{id} ---

    @Test
    void getDepartmentById_employee_returns200() throws Exception {
        mockMvc.perform(get("/api/departments/1")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Engineering"));
    }

    @Test
    void getDepartmentById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/departments/999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    // --- GET /api/departments/search ---

    @Test
    void searchDepartments_hr_returns200() throws Exception {
        mockMvc.perform(get("/api/departments/search")
                        .param("keyword", "Eng")
                        .header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Engineering"));
    }

    @Test
    void searchDepartments_employee_returns403() throws Exception {
        mockMvc.perform(get("/api/departments/search")
                        .param("keyword", "Eng")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }

    // --- POST /api/departments ---

    @Test
    void createDepartment_admin_returns201() throws Exception {
        String json = """
                {"name": "Marketing", "description": "Marketing dept"}
                """;

        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Marketing"));
    }

    @Test
    void createDepartment_hr_returns403() throws Exception {
        String json = """
                {"name": "Marketing", "description": "Marketing dept"}
                """;

        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void createDepartment_employee_returns403() throws Exception {
        String json = """
                {"name": "Marketing", "description": "Marketing dept"}
                """;

        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void createDepartment_duplicate_returns409() throws Exception {
        String json = """
                {"name": "Engineering", "description": "Already exists"}
                """;

        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isConflict());
    }

    // --- PUT /api/departments/{id} ---

    @Test
    void updateDepartment_admin_returns200() throws Exception {
        String json = """
                {"name": "Engineering Updated", "description": "Updated"}
                """;

        mockMvc.perform(put("/api/departments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Engineering Updated"));
    }

    @Test
    void updateDepartment_hr_returns403() throws Exception {
        String json = """
                {"name": "Engineering Updated", "description": "Updated"}
                """;

        mockMvc.perform(put("/api/departments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isForbidden());
    }
}
