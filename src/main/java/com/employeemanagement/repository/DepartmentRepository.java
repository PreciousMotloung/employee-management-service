package com.employeemanagement.repository;

import com.employeemanagement.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    boolean existsByName(String name);

    List<Department> findByNameContainingIgnoreCase(String name);
}
