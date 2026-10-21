package com.example.demo.dto;

import com.example.demo.model.TaskPriority;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class TaskDTO {

    @NotBlank(message = "Task title is required")
    private String title;

    private String description;

    private TaskPriority priority = TaskPriority.MEDIUM;

    @NotNull(message = "Assigned employee ID is required")
    private Long assignedToId;

    @FutureOrPresent(message = "Due date must be today or in the future")
    private LocalDate dueDate;
}
