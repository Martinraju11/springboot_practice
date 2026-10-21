package com.example.demo.dto;

import com.example.demo.model.Gender;
import com.example.demo.model.Role;
import lombok.Data;

@Data
public class UserInfoResponse {
    private Long id;
    private String name;
    private String email;
    private Integer age;
    private Gender gender;
    private Role role;
    private String department;
    private Long managerId;
    private String managerName;

    public UserInfoResponse() {
    }

    public UserInfoResponse(Long id, String name, String email, Integer age, Gender gender, Role role, String department, Long managerId, String managerName) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.age = age;
        this.gender = gender;
        this.role = role;
        this.department = department;
        this.managerId = managerId;
        this.managerName = managerName;
    }
}

