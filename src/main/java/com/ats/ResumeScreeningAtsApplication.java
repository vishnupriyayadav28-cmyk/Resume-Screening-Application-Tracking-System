package com.ats;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class to run the Resume Screening and Application Tracking System.
 */
@SpringBootApplication
public class ResumeScreeningAtsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResumeScreeningAtsApplication.class, args);
        System.out.println("====================================================================");
        System.out.println("Resume Screening & Application Tracking System started successfully!");
        System.out.println("Access UI: http://localhost:8080");
        System.out.println("Access Database Console: http://localhost:8080/h2-console");
        System.out.println("====================================================================");
    }
}
