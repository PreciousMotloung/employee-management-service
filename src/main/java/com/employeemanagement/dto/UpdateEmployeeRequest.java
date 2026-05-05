package com.employeemanagement.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEmployeeRequest {

    private String firstName;
    private String lastName;

    @Email(message = "Email must be valid")
    private String email;

    private String phone;
    private String jobTitle;

    @DecimalMin(value = "0.0", inclusive = false, message = "Salary must be positive")
    private BigDecimal salary;

    private Long departmentId;
}
