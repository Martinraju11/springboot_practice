package com.example.demo.controller;

import com.example.demo.dto.LeaveRequestDTO;
import com.example.demo.dto.MessageResponse;
import com.example.demo.dto.TaskStatusDTO;
import com.example.demo.model.LeaveRequest;
import com.example.demo.model.LeaveStatus;
import com.example.demo.model.Task;
import com.example.demo.model.User;
import com.example.demo.repository.LeaveRequestRepository;
import com.example.demo.repository.TaskRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employee")
public class EmployeeController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    private User getAuthenticatedUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
    }

    // 1. Get assigned tasks
    @GetMapping("/tasks")
    public ResponseEntity<List<Task>> getMyTasks() {
        User employee = getAuthenticatedUser();
        List<Task> tasks = taskRepository.findByAssignedToId(employee.getId());
        return ResponseEntity.ok(tasks);
    }

    // 2. Update task status (e.g. IN_PROGRESS, COMPLETED)
    @RequestMapping(value = "/tasks/{id}/status", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<?> updateTaskStatus(@PathVariable Long id, @Valid @RequestBody TaskStatusDTO statusDTO) {
        User employee = getAuthenticatedUser();

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + id));

        if (!task.getAssignedTo().getId().equals(employee.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Error: You are not authorized to update this task"));
        }

        task.setStatus(statusDTO.getStatus());
        Task updatedTask = taskRepository.save(task);
        return ResponseEntity.ok(updatedTask);
    }

    // 3. Submit Leave Request
    @PostMapping("/leaves")
    public ResponseEntity<?> submitLeaveRequest(@Valid @RequestBody LeaveRequestDTO leaveDTO) {
        User employee = getAuthenticatedUser();

        if (leaveDTO.getEndDate().isBefore(leaveDTO.getStartDate())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: End date cannot be before start date"));
        }

        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setReason(leaveDTO.getReason());
        leaveRequest.setStartDate(leaveDTO.getStartDate());
        leaveRequest.setEndDate(leaveDTO.getEndDate());
        leaveRequest.setStatus(LeaveStatus.PENDING);
        leaveRequest.setEmployee(employee);
        leaveRequest.setReviewer(employee.getManager());

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // 4. Get leave history for logged-in employee
    @GetMapping("/leaves")
    public ResponseEntity<List<LeaveRequest>> getMyLeaves() {
        User employee = getAuthenticatedUser();
        List<LeaveRequest> leaves = leaveRequestRepository.findByEmployeeId(employee.getId());
        return ResponseEntity.ok(leaves);
    }
}
