# Resume Screening & Application Tracking System

A self-contained, fully working college mini-project built using **Spring Boot**, **Java 17**, **H2 Database**, **Apache PDFBox**, **Bootstrap 5**, and **Vanilla JavaScript**.

---

## Project Overview

This system allows candidates to submit job applications by filling out their details and uploading a PDF resume. The system automatically:
1. Saves the PDF file to disk under the `uploads` folder with a unique UUID.
2. Parses the PDF resume to extract raw text content using **Apache PDFBox**.
3. Compares the extracted text against **8 predefined required skills** (`Java`, `Spring Boot`, `HTML`, `CSS`, `JavaScript`, `SQL`, `MySQL`, `Git`).
4. Calculates a matching percentage score.
5. Automatically assigns a recommendation status.
6. Persists the candidate profile, score, status, and raw resume text to a file-based **H2 Database**.
7. Reflects the details in a recruiter tracking dashboard where status can be updated, candidates searched/deleted, and resumes downloaded.

---

## Folder Structure

```
resume screening and application tracking system/
├── pom.xml                         # Maven dependencies & build settings
├── README.md                       # This execution & viva prep guide
├── PROJECT_REPORT.md               # Complete Mini Project Report
├── uploads/                        # Resumes saved on disk (UUID_filename.pdf)
├── data/                           # Persistent H2 Database files
└── src/
    └── main/
        ├── java/com/ats/
        │   ├── ResumeScreeningAtsApplication.java # Spring Boot main entry
        │   ├── config/
        │   │   └── WebConfig.java  # Custom static folder mapping
        │   ├── controller/
        │   │   ├── CandidateController.java # REST APIs
        │   │   └── PageController.java      # Page Routing
        │   ├── dto/
        │   │   ├── CandidateDto.java        # Dashboard view model
        │   │   └── StatusUpdateDto.java     # Status payload
        │   ├── entity/
        │   │   └── Candidate.java           # DB model
        │   ├── repository/
        │   │   └── CandidateRepository.java # JPA Repository
        │   └── service/
        │       └── CandidateService.java    # Parsing & screening logic
        └── resources/
            ├── application.properties       # App properties
            └── static/                      # Web UI folder
                ├── index.html               # Landing Page
                ├── apply.html               # Application Form
                ├── dashboard.html           # Recruiter Dashboard
                ├── candidate-details.html   # Profile inspector
                ├── css/
                │   └── style.css            # Custom Styling
                └── js/
                    └── app.js               # Client-side JavaScript
```

---

## Technology Stack

- **Backend:** Java 17, Spring Boot 3.1.5, Spring Data JPA
- **Frontend:** HTML5, CSS3 (Custom Glassmorphism), Vanilla JavaScript, Bootstrap 5, Bootstrap Icons
- **Database:** H2 Embedded (file-based database)
- **Library:** Apache PDFBox (v2.0.30)
- **Build Tool:** Maven

---

## Database Configuration

The application is configured to use a file-based H2 database. Unlike in-memory databases that lose data on restart, this app stores records in the `./data/ats_db` file, preserving all candidates across server restarts.

- **JDBC URL:** `jdbc:h2:file:./data/ats_db`
- **Username:** `sa`
- **Password:** *(None)*
- **H2 Console:** Available at `http://localhost:8080/h2-console`
- **Tables:** Created automatically via Hibernate schema generation (`spring.jpa.hibernate.ddl-auto=update`).

---

## Execution Instructions

No complex setups, Docker, or external database installations are required. Follow these steps to execute:

### Prerequisite
Ensure you have **Java 17 (or higher)** and **Maven** installed on your system.

### Step 1: Clone/Copy the Project
Navigate to the root directory where `pom.xml` is located:
```bash
cd "C:\Users\PALLAVI\Desktop\resume  screening and application tracking system"
```

### Step 2: Build the Application
Compile and build the project using Maven:
```bash
mvn clean package
```

### Step 3: Run the Application
Start the Spring Boot server:
```bash
mvn spring-boot:run
```

### Step 4: Access the System
Once started, open your web browser and navigate to:
- **Main Portal (Home Page):** [http://localhost:8080](http://localhost:8080)
- **Direct Candidate Form:** [http://localhost:8080/apply](http://localhost:8080/apply)
- **Direct Recruiter Dashboard:** [http://localhost:8080/dashboard](http://localhost:8080/dashboard)
- **H2 DB Console:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console) (Use JDBC URL `jdbc:h2:file:./data/ats_db` to connect)

---

## Step-by-Step Viva Demonstration Guide

If an examiner asks you to demonstrate the project live, follow these steps to explain the workflow:

### 1. How a Resume is Uploaded
1. Open the candidate form at `http://localhost:8080/apply`.
2. Fill in the name, email, phone, and select a job role.
3. Drag and drop a PDF file. Show how validation prevents uploading files larger than 5MB or formats other than `.pdf`.
4. Click **Submit Application**. The page sends a multipart request containing all text inputs and the file stream to the `POST /apply` backend endpoint in [CandidateController.java](file:///c:/Users/PALLAVI/Desktop/resume%20%20screening%20and%20application%20tracking%20system/src/main/java/com/ats/controller/CandidateController.java).
5. The backend generates a unique UUID (e.g. `uuid_resume.pdf`), saves it in the `uploads` folder, and parses it.

### 2. How PDF Text is Extracted
1. In the `CandidateService.java` class, the `applyCandidate` method loads the file from disk using Apache PDFBox:
   ```java
   try (PDDocument document = PDDocument.load(targetPath.toFile())) {
       PDFTextStripper pdfStripper = new PDFTextStripper();
       extractedText = pdfStripper.getText(document);
   }
   ```
2. The `PDFTextStripper` parses the structure of the PDF file and returns all string characters as a single Java `String` representing the candidate's resume content.

### 3. How the Matching Score is Calculated
1. The system loops through the **8 required skills**: `Java`, `Spring Boot`, `HTML`, `CSS`, `JavaScript`, `SQL`, `MySQL`, and `Git`.
2. To check if a skill is present, a regex search is performed to search for exact word matches (case-insensitive) for single-word skills (like `\bjava\b`, `\bsql\b`), and simple contains checks for multi-word skills (like `spring boot`).
3. The formula applied is:
   $$\text{Score} = \left(\frac{\text{Matched Skills}}{\text{Total Skills}}\right) \times 100$$
4. For example, if a resume contains the terms "Java", "SQL", and "Git", the system matches 3 out of 8 skills. The score will be calculated as $\frac{3}{8} \times 100 = 37.5\%$, which is rounded to **38%**.

### 4. How the Status is Assigned
1. After calculating the score, the system dynamically sets the initial recommendation status:
   - **80% to 100%:** Highly Recommended
   - **60% to 79%:** Shortlisted
   - **40% to 59%:** Under Review
   - **0% to 39%:** Rejected
2. This is saved as the candidate's initial status in the database.

### 5. How Data is Stored
1. A new `Candidate` entity is created, populated with the details, the extracted raw text, matching score, and recommendation status.
2. The entity is saved to the database via `CandidateRepository.save(candidate)`.
3. The JPA framework connects to the H2 database, creates/updates the `candidates` table, and inserts the record.

### 6. Recruiter Actions on the Dashboard
1. Go to the dashboard: `http://localhost:8080/dashboard`.
2. Show the dynamic cards. They dynamically sum up the stats (Total, Shortlisted, Selected, Rejected) directly from the database using JavaScript array reducers.
3. Show the **Search** functionality: type a name or email in the search bar. Explain that Vanilla JS filters the rows on `keyup` instantly.
4. Show **Status Management**: change a candidate's status using the dropdown. This triggers an AJAX PUT request to `/candidate/status/{id}` which updates the database. The statistics cards immediately update without refreshing the page!
5. Show **View Details**: click the "Eye" button. Explain how the candidate details page maps matched vs missing skills as green/red badges and displays the raw parsed PDF text, showing transparency in the screening decision.
6. Show **Download Resume**: click the download button, which fetches the PDF from the server via `/download/{id}`.
7. Show **Delete**: click the delete button. The backend deletes the candidate from H2 and deletes the PDF file from the `uploads/` directory on disk.
