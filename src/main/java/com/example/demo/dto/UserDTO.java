package com.example.demo.dto;

import com.example.demo.model.Gender;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDTO {

    @NotBlank(message = "Name is required and cannot be empty")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email address format (e.g. user@example.com)")
    private String email;

    @NotNull(message = "Age is required")
    @Min(value = 18, message = "Age must be at least 18 years old")
    @Max(value = 120, message = "Age cannot be greater than 120")
    private Integer age;

    private Gender gender;

    private Boolean isActive;
}
