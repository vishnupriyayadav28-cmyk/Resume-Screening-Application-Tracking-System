package com.ats.dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for candidate summary information.
 * Implemented with standard Java getters and setters.
 */
public class CandidateDto {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String jobRole;
    private String resumeFileName;
    private Integer score;
    private String status;
    private LocalDateTime createdDate;

    // Constructors
    public CandidateDto() {
    }

    public CandidateDto(Long id, String name, String email, String phone, String jobRole, 
                        String resumeFileName, Integer score, String status, LocalDateTime createdDate) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.jobRole = jobRole;
        this.resumeFileName = resumeFileName;
        this.score = score;
        this.status = status;
        this.createdDate = createdDate;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getJobRole() {
        return jobRole;
    }

    public void setJobRole(String jobRole) {
        this.jobRole = jobRole;
    }

    public String getResumeFileName() {
        return resumeFileName;
    }

    public void setResumeFileName(String resumeFileName) {
        this.resumeFileName = resumeFileName;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
}
