package com.ats.dto;

/**
 * Data Transfer Object for candidate status updates.
 * Implemented with standard Java getters and setters.
 */
public class StatusUpdateDto {
    private String status;

    // Constructors
    public StatusUpdateDto() {
    }

    public StatusUpdateDto(String status) {
        this.status = status;
    }

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
