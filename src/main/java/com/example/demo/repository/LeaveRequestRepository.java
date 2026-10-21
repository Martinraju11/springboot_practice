package com.example.demo.repository;

import com.example.demo.model.LeaveRequest;
import com.example.demo.model.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByEmployeeId(Long employeeId);
    List<LeaveRequest> findByReviewerId(Long reviewerId);
    List<LeaveRequest> findByReviewerIdAndStatus(Long reviewerId, LeaveStatus status);
}
