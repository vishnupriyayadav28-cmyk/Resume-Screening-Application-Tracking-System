package com.ats.service;

import com.ats.entity.Candidate;
import com.ats.repository.CandidateRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Service class containing the business logic for resume processing, screening, and database operations.
 */
@Service
public class CandidateService {

    private final CandidateRepository candidateRepository;

    // Define the upload directory relative to project root
    private static final String UPLOAD_DIR = "uploads";

    // Predefined required skills for screening (8 total)
    public static final List<String> REQUIRED_SKILLS = List.of(
            "Java", "Spring Boot", "HTML", "CSS", "JavaScript", "SQL", "MySQL", "Git"
    );

    @Autowired
    public CandidateService(CandidateRepository candidateRepository) {
        this.candidateRepository = candidateRepository;
        // Ensure the upload directory exists on startup
        createUploadDirectory();
    }

    /**
     * Creates the upload directory if it does not exist.
     */
    private void createUploadDirectory() {
        try {
            Path path = Paths.get(UPLOAD_DIR);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                System.out.println("Uploads directory created: " + path.toAbsolutePath());
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage directory", e);
        }
    }

    /**
     * Processes candidate application: saves file, extracts text, screens, scores, and saves to database.
     */
    public Candidate applyCandidate(String name, String email, String phone, String jobRole, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Resume file is empty or missing.");
        }

        // 1. Generate unique file name and save file
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        // Ensure file is a PDF
        if (!".pdf".equalsIgnoreCase(extension)) {
            throw new IllegalArgumentException("Only PDF resumes are accepted.");
        }

        String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;
        Path targetPath = Paths.get(UPLOAD_DIR).resolve(uniqueFilename);
        
        try (InputStream is = file.getInputStream()) {
            Files.copy(is, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }

        // 2. Extract text using Apache PDFBox
        String extractedText = "";
        try (PDDocument document = PDDocument.load(targetPath.toFile())) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            extractedText = pdfStripper.getText(document);
        } catch (IOException e) {
            // Cleanup file if extraction fails
            Files.deleteIfExists(targetPath);
            throw new IOException("Failed to extract text from PDF: " + e.getMessage(), e);
        }

        // 3. Screen resume and calculate score
        List<String> matchedSkills = new ArrayList<>();
        for (String skill : REQUIRED_SKILLS) {
            if (containsSkill(extractedText, skill)) {
                matchedSkills.add(skill);
            }
        }

        int score = (int) Math.round(((double) matchedSkills.size() / REQUIRED_SKILLS.size()) * 100.0);

        // 4. Auto assign status
        String status = autoAssignStatus(score);

        // 5. Create and save Candidate Entity
        Candidate candidate = new Candidate();
        candidate.setName(name);
        candidate.setEmail(email);
        candidate.setPhone(phone);
        candidate.setJobRole(jobRole);
        candidate.setResumeFileName(originalFilename);
        candidate.setResumePath(targetPath.toString());
        candidate.setResumeText(extractedText);
        candidate.setScore(score);
        candidate.setStatus(status);

        return candidateRepository.save(candidate);
    }

    /**
     * Checks if a skill is in the text case-insensitively, using word boundaries for single words.
     */
    private boolean containsSkill(String text, String skill) {
        if (text == null || skill == null) return false;
        String lowerText = text.toLowerCase();
        String lowerSkill = skill.toLowerCase();

        // If the skill is letters-only and single-word (like Java, SQL, Git), check for exact word match.
        // For compound words like "Spring Boot", check simple contains.
        if (skill.matches("^[a-zA-Z]+$")) {
            String regex = "\\b" + Pattern.quote(lowerSkill) + "\\b";
            return Pattern.compile(regex).matcher(lowerText).find();
        } else {
            return lowerText.contains(lowerSkill);
        }
    }

    /**
     * Status categories based on screening score rules:
     * 80 - 100: Highly Recommended
     * 60 - 79: Shortlisted
     * 40 - 59: Under Review
     * 0 - 39: Rejected
     */
    private String autoAssignStatus(int score) {
        if (score >= 80) {
            return "Highly Recommended";
        } else if (score >= 60) {
            return "Shortlisted";
        } else if (score >= 40) {
            return "Under Review";
        } else {
            return "Rejected";
        }
    }

    public List<Candidate> getAllCandidates() {
        return candidateRepository.findAll();
    }

    public Candidate getCandidateById(Long id) {
        return candidateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found with ID: " + id));
    }

    /**
     * Updates candidate status manually.
     */
    public Candidate updateStatus(Long id, String status) {
        Candidate candidate = getCandidateById(id);
        
        // Allowed statuses: Applied, Shortlisted, Interview Scheduled, Selected, Rejected
        List<String> allowedStatuses = List.of("Applied", "Shortlisted", "Interview Scheduled", "Selected", "Rejected");
        // Also allow Highly Recommended and Under Review since they can be auto-assigned
        if (!allowedStatuses.contains(status) && !"Highly Recommended".equalsIgnoreCase(status) && !"Under Review".equalsIgnoreCase(status)) {
            throw new IllegalArgumentException("Invalid status value.");
        }
        
        candidate.setStatus(status);
        return candidateRepository.save(candidate);
    }

    /**
     * Deletes candidate record and deletes the uploaded file from disk.
     */
    public void deleteCandidate(Long id) {
        Candidate candidate = getCandidateById(id);
        String filePath = candidate.getResumePath();
        
        // Delete candidate record
        candidateRepository.delete(candidate);
        
        // Delete resume file from disk
        if (filePath != null) {
            try {
                Path path = Paths.get(filePath);
                Files.deleteIfExists(path);
            } catch (IOException e) {
                System.err.println("Failed to delete file from disk: " + filePath + ", error: " + e.getMessage());
            }
        }
    }

    /**
     * Retrieves file path for resume download.
     */
    public File getResumeFile(Long id) {
        Candidate candidate = getCandidateById(id);
        if (candidate.getResumePath() == null) {
            throw new IllegalArgumentException("Resume file path is missing for candidate " + id);
        }
        File file = new File(candidate.getResumePath());
        if (!file.exists()) {
            throw new IllegalArgumentException("Resume file does not exist on disk.");
        }
        return file;
    }
}
