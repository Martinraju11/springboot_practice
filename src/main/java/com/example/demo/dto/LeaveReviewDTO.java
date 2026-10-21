package com.example.demo.dto;

import com.example.demo.model.LeaveStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LeaveReviewDTO {

    @NotNull(message = "Leave status (APPROVED/REJECTED) is required")
    private LeaveStatus status;

    private String managerComment;
}
