package com.example.demo.controller;

import com.example.demo.dto.LeaveReviewDTO;
import com.example.demo.dto.MessageResponse;
import com.example.demo.dto.TaskDTO;
import com.example.demo.model.*;
import com.example.demo.repository.LeaveRequestRepository;
import com.example.demo.repository.TaskRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.services.UserDetailsImpl;
import com.example.demo.service.ManagerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/manager")
public class ManagerController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ManagerService managerService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    private User getAuthenticatedManager() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new IllegalArgumentException("Manager not found"));
    }

    // 1. Get all employees in manager's team (Cached via Redis/Memory)
    @GetMapping("/team")
    public ResponseEntity<List<User>> getMyTeam() {
        User manager = getAuthenticatedManager();
        List<User> team = managerService.getTeamByManagerId(manager.getId());
        return ResponseEntity.ok(team);
    }

    // 2. Assign a task to an employee
    @PostMapping("/tasks")
    public ResponseEntity<?> createTask(@Valid @RequestBody TaskDTO taskDTO) {
        User manager = getAuthenticatedManager();

        User employee = userRepository.findById(taskDTO.getAssignedToId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + taskDTO.getAssignedToId()));

        Task task = new Task();
        task.setTitle(taskDTO.getTitle());
        task.setDescription(taskDTO.getDescription());
        task.setPriority(taskDTO.getPriority() != null ? taskDTO.getPriority() : TaskPriority.MEDIUM);
        task.setStatus(TaskStatus.PENDING);
        task.setDueDate(taskDTO.getDueDate());
        task.setAssignedTo(employee);
        task.setCreatedBy(manager);

        Task savedTask = taskRepository.save(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTask);
    }

    // 3. Get all tasks created by manager
    @GetMapping("/tasks")
    public ResponseEntity<List<Task>> getCreatedTasks() {
        User manager = getAuthenticatedManager();
        List<Task> tasks = taskRepository.findByCreatedById(manager.getId());
        return ResponseEntity.ok(tasks);
    }

    // 4. Get leave requests for team
    @GetMapping("/leaves")
    public ResponseEntity<List<LeaveRequest>> getTeamLeaveRequests() {
        User manager = getAuthenticatedManager();
        List<LeaveRequest> leaves = leaveRequestRepository.findByReviewerId(manager.getId());
        return ResponseEntity.ok(leaves);
    }

    // 5. Review (Approve/Reject) Leave Request
    @PutMapping("/leaves/{id}/review")
    public ResponseEntity<?> reviewLeaveRequest(@PathVariable Long id, @Valid @RequestBody LeaveReviewDTO reviewDTO) {
        User manager = getAuthenticatedManager();

        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found with ID: " + id));

        if (leaveRequest.getReviewer() != null && !leaveRequest.getReviewer().getId().equals(manager.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Error: You are not authorized to review this leave request"));
        }

        leaveRequest.setStatus(reviewDTO.getStatus());
        leaveRequest.setManagerComment(reviewDTO.getManagerComment());
        leaveRequest.setReviewer(manager);

        LeaveRequest updated = leaveRequestRepository.save(leaveRequest);
        return ResponseEntity.ok(updated);
    }
}
