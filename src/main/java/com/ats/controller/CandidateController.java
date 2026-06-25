package com.ats.controller;

import com.ats.dto.CandidateDto;
import com.ats.dto.StatusUpdateDto;
import com.ats.entity.Candidate;
import com.ats.service.CandidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller mapping endpoints for applying, querying, and managing candidate applications.
 */
@RestController
@CrossOrigin(origins = "*") // Allows easy access during local testing if needed
public class CandidateController {

    private final CandidateService candidateService;

    @Autowired
    public CandidateController(CandidateService candidateService) {
        this.candidateService = candidateService;
    }

    /**
     * POST /apply - Submit a resume application with PDF.
     */
    @PostMapping(value = "/apply", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> apply(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            @RequestParam("jobRole") String jobRole,
            @RequestParam("file") MultipartFile file) {
        
        Map<String, String> response = new HashMap<>();

        // Validation checks
        if (name == null || name.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            phone == null || phone.trim().isEmpty() ||
            jobRole == null || jobRole.trim().isEmpty() ||
            file == null || file.isEmpty()) {
            response.put("error", "All fields are mandatory.");
            return ResponseEntity.badRequest().body(response);
        }

        // Check if PDF file
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            response.put("error", "Only PDF resumes are accepted.");
            return ResponseEntity.badRequest().body(response);
        }

        // Check file size (5MB = 5 * 1024 * 1024 bytes)
        if (file.getSize() > 5 * 1024 * 1024) {
            response.put("error", "Resume file must be smaller than 5 MB.");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            Candidate candidate = candidateService.applyCandidate(name, email, phone, jobRole, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(candidate);
        } catch (IllegalArgumentException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * GET /candidates - Retrieve all candidates (DTO format for optimization).
     */
    @GetMapping("/candidates")
    public ResponseEntity<List<CandidateDto>> getAllCandidates() {
        List<Candidate> candidates = candidateService.getAllCandidates();
        List<CandidateDto> dtos = candidates.stream().map(c -> {
            CandidateDto dto = new CandidateDto();
            dto.setId(c.getId());
            dto.setName(c.getName());
            dto.setEmail(c.getEmail());
            dto.setPhone(c.getPhone());
            dto.setJobRole(c.getJobRole());
            dto.setResumeFileName(c.getResumeFileName());
            dto.setScore(c.getScore());
            dto.setStatus(c.getStatus());
            dto.setCreatedDate(c.getCreatedDate());
            return dto;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /candidate/{id} - Retrieve detailed information of a specific candidate (including extracted text).
     */
    @GetMapping("/candidate/{id}")
    public ResponseEntity<?> getCandidateById(@PathVariable Long id) {
        try {
            Candidate candidate = candidateService.getCandidateById(id);
            return ResponseEntity.ok(candidate);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * PUT /candidate/status/{id} - Update candidate's application status manually.
     */
    @PutMapping("/candidate/status/{id}")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody StatusUpdateDto statusDto) {
        Map<String, String> response = new HashMap<>();
        try {
            if (statusDto == null || statusDto.getStatus() == null || statusDto.getStatus().trim().isEmpty()) {
                response.put("error", "Status field is required.");
                return ResponseEntity.badRequest().body(response);
            }
            Candidate updated = candidateService.updateStatus(id, statusDto.getStatus());
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * DELETE /candidate/{id} - Delete a candidate and clean up their resume file.
     */
    @DeleteMapping("/candidate/{id}")
    public ResponseEntity<?> deleteCandidate(@PathVariable Long id) {
        Map<String, String> response = new HashMap<>();
        try {
            candidateService.deleteCandidate(id);
            response.put("message", "Candidate application deleted successfully.");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * GET /download/{id} - Download candidate's resume file.
     */
    @GetMapping("/download/{id}")
    public ResponseEntity<?> downloadResume(@PathVariable Long id) {
        try {
            File file = candidateService.getResumeFile(id);
            Path path = file.toPath();
            Resource resource = new UrlResource(path.toUri());

            Candidate candidate = candidateService.getCandidateById(id);
            String originalFileName = candidate.getResumeFileName();
            String downloadName = originalFileName != null ? originalFileName : file.getName();

            // Set Content-Disposition header so that browser downloads the file
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadName + "\"")
                    .body(resource);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error downloading file: " + e.getMessage());
        }
    }
}
