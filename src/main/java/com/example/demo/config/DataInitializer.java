package com.example.demo.config;

import com.example.demo.model.*;
import com.example.demo.repository.LeaveRequestRepository;
import com.example.demo.repository.TaskRepository;
import com.example.demo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (!userRepository.existsByEmail("manager@company.com")) {
            logger.info("Seeding initial corporate demo data...");

            // 1. Create Manager
            User manager = new User();
            manager.setName("Sarah Jenkins");
            manager.setEmail("manager@company.com");
            manager.setPassword(passwordEncoder.encode("password123"));
            manager.setRole(Role.ROLE_MANAGER);
            manager.setDepartment("Engineering");
            manager.setGender(Gender.FEMALE);
            manager.setAge(34);
            manager.setIsActive(true);
            manager = userRepository.save(manager);

            // 2. Create Employee 1
            User emp1 = new User();
            emp1.setName("Alex Developer");
            emp1.setEmail("alex@company.com");
            emp1.setPassword(passwordEncoder.encode("password123"));
            emp1.setRole(Role.ROLE_EMPLOYEE);
            emp1.setDepartment("Engineering");
            emp1.setGender(Gender.MALE);
            emp1.setAge(27);
            emp1.setManager(manager);
            emp1.setIsActive(true);
            emp1 = userRepository.save(emp1);

            // 3. Create Employee 2
            User emp2 = new User();
            emp2.setName("Emma UI Designer");
            emp2.setEmail("emma@company.com");
            emp2.setPassword(passwordEncoder.encode("password123"));
            emp2.setRole(Role.ROLE_EMPLOYEE);
            emp2.setDepartment("Design");
            emp2.setGender(Gender.FEMALE);
            emp2.setAge(25);
            emp2.setManager(manager);
            emp2.setIsActive(true);
            emp2 = userRepository.save(emp2);

            // 4. Create Sample Tasks
            Task task1 = new Task();
            task1.setTitle("Implement OAuth2 Single Sign-On");
            task1.setDescription("Integrate Google and GitHub OAuth providers for enterprise clients.");
            task1.setPriority(TaskPriority.HIGH);
            task1.setStatus(TaskStatus.IN_PROGRESS);
            task1.setDueDate(LocalDate.now().plusDays(5));
            task1.setAssignedTo(emp1);
            task1.setCreatedBy(manager);
            taskRepository.save(task1);

            Task task2 = new Task();
            task2.setTitle("Design Corporate Dark Mode Palette");
            task2.setDescription("Update design tokens and Figma prototypes for v2 dashboard.");
            task2.setPriority(TaskPriority.MEDIUM);
            task2.setStatus(TaskStatus.PENDING);
            task2.setDueDate(LocalDate.now().plusDays(3));
            task2.setAssignedTo(emp2);
            task2.setCreatedBy(manager);
            taskRepository.save(task2);

            // 5. Create Sample Leave Request
            LeaveRequest leave1 = new LeaveRequest();
            leave1.setReason("Attending ReactConf Conference & Workshops");
            leave1.setStartDate(LocalDate.now().plusDays(7));
            leave1.setEndDate(LocalDate.now().plusDays(10));
            leave1.setStatus(LeaveStatus.PENDING);
            leave1.setEmployee(emp1);
            leave1.setReviewer(manager);
            leaveRequestRepository.save(leave1);

            logger.info("Demo data successfully seeded! Login credentials: manager@company.com / password123, alex@company.com / password123");
        }
    }
}
