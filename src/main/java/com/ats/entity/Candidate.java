package com.ats.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA Entity mapping the candidate record to the H2 Database.
 * Implemented with standard Java getters and setters for maximum compatibility.
 */
@Entity
@Table(name = "candidates")
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String jobRole;

    private String resumeFileName;
    private String resumePath;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String resumeText;

    private Integer score; // Stored as a matching percentage (0 to 100)
    private String status;  // E.g., Applied, Shortlisted, Highly Recommended, Selected, Rejected
    
    private LocalDateTime createdDate;

    // Constructors
    public Candidate() {
    }

    public Candidate(Long id, String name, String email, String phone, String jobRole, 
                     String resumeFileName, String resumePath, String resumeText, 
                     Integer score, String status, LocalDateTime createdDate) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.jobRole = jobRole;
        this.resumeFileName = resumeFileName;
        this.resumePath = resumePath;
        this.resumeText = resumeText;
        this.score = score;
        this.status = status;
        this.createdDate = createdDate;
    }

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
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

    public String getResumePath() {
        return resumePath;
    }

    public void setResumePath(String resumePath) {
        this.resumePath = resumePath;
    }

    public String getResumeText() {
        return resumeText;
    }

    public void setResumeText(String resumeText) {
        this.resumeText = resumeText;
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
