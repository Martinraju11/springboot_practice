package com.example.demo.dto;

import com.example.demo.model.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TaskStatusDTO {

    @NotNull(message = "Task status is required")
    private TaskStatus status;
}
