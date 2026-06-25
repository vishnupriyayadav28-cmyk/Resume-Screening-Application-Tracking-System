// Predefined Skills for client-side highlighting (must match backend)
const REQUIRED_SKILLS = ["Java", "Spring Boot", "HTML", "CSS", "JavaScript", "SQL", "MySQL", "Git"];

// Global cache for dashboard data
let allCandidates = [];

// ==========================================
// 1. CANDIDATE APPLICATION FORM (apply.html)
// ==========================================
const applyForm = document.getElementById('applyForm');
if (applyForm) {
    const dropzone = document.getElementById('dropzone');
    const fileInput = document.getElementById('fileInput');
    const dropzoneText = document.getElementById('dropzoneText');
    const fileDetails = document.getElementById('fileDetails');
    const fileError = document.getElementById('fileError');
    const submitBtn = document.getElementById('submitBtn');
    const spinner = document.getElementById('spinner');
    const btnText = document.getElementById('btnText');
    const alertBox = document.getElementById('alertBox');

    // Drag and Drop listeners
    dropzone.addEventListener('dragover', (e) => {
        e.preventDefault();
        dropzone.classList.add('border-primary');
    });

    dropzone.addEventListener('dragleave', () => {
        dropzone.classList.remove('border-primary');
    });

    dropzone.addEventListener('drop', (e) => {
        e.preventDefault();
        dropzone.classList.remove('border-primary');
        if (e.dataTransfer.files.length > 0) {
            handleFileSelect(e.dataTransfer.files[0]);
        }
    });

    fileInput.addEventListener('change', () => {
        if (fileInput.files.length > 0) {
            handleFileSelect(fileInput.files[0]);
        }
    });

    function handleFileSelect(file) {
        // Validate File is PDF
        if (!file.name.toLowerCase().endsWith('.pdf')) {
            showFileError("Only PDF resumes are accepted.");
            fileInput.value = '';
            return;
        }

        // Validate File Size (5MB)
        if (file.size > 5 * 1024 * 1024) {
            showFileError("Resume file size must be less than 5 MB.");
            fileInput.value = '';
            return;
        }

        // Display selected file details
        clearFileError();
        dropzoneText.textContent = file.name;
        const sizeInMb = (file.size / (1024 * 1024)).toFixed(2);
        fileDetails.textContent = `File Size: ${sizeInMb} MB | Click/Drag to replace`;
        
        // Sync drag and drop file into fileInput elements
        const dataTransfer = new DataTransfer();
        dataTransfer.items.add(file);
        fileInput.files = dataTransfer.files;
    }

    function showFileError(msg) {
        fileError.textContent = msg;
        dropzone.style.borderColor = '#EF4444';
        dropzoneText.style.color = '#EF4444';
    }

    function clearFileError() {
        fileError.textContent = '';
        dropzone.style.borderColor = '';
        dropzoneText.style.color = '';
    }

    // Form Submission
    applyForm.addEventListener('submit', (e) => {
        e.preventDefault();
        
        // Custom Bootstrap validation
        if (!applyForm.checkValidity()) {
            e.stopPropagation();
            applyForm.classList.add('was-validated');
            if (fileInput.files.length === 0) {
                showFileError("Please upload your PDF resume.");
            }
            return;
        }

        if (fileInput.files.length === 0) {
            showFileError("Please upload your PDF resume.");
            return;
        }

        // Prepare FormData
        const formData = new FormData();
        formData.append('name', document.getElementById('name').value);
        formData.append('email', document.getElementById('email').value);
        formData.append('phone', document.getElementById('phone').value);
        formData.append('jobRole', document.getElementById('jobRole').value);
        formData.append('file', fileInput.files[0]);

        // Show loading state
        submitBtn.disabled = true;
        spinner.classList.remove('d-none');
        btnText.textContent = "Processing Resume...";
        alertBox.classList.add('d-none');

        // Post to API
        fetch('/apply', {
            method: 'POST',
            body: formData
        })
        .then(response => {
            if (!response.ok) {
                return response.json().then(err => { throw new Error(err.error || "Submission failed"); });
            }
            return response.json();
        })
        .then(data => {
            // Reset form
            applyForm.reset();
            applyForm.classList.remove('was-validated');
            dropzoneText.textContent = "Drag & Drop PDF here or Click to Browse";
            fileDetails.textContent = "Accepted file type: PDF (Max size: 5MB)";
            
            // Show Success Modal with Results
            document.getElementById('resultScore').textContent = `${data.score}%`;
            
            const scoreVal = data.score;
            const scoreClass = scoreVal >= 80 ? 'score-high' : (scoreVal >= 40 ? 'score-medium' : 'score-low');
            document.getElementById('resultScore').className = `display-4 fw-extrabold ${scoreClass}`;

            const badge = document.getElementById('resultStatus');
            badge.textContent = data.status;
            badge.className = `badge-status ${getStatusClass(data.status)}`;

            const myModal = new bootstrap.Modal(document.getElementById('resultModal'));
            myModal.show();
        })
        .catch(err => {
            alertBox.textContent = err.message;
            alertBox.className = "alert alert-danger";
            alertBox.classList.remove('d-none');
        })
        .finally(() => {
            submitBtn.disabled = false;
            spinner.classList.add('d-none');
            btnText.textContent = "Submit Application";
        });
    });
}

// ==========================================
// 2. RECRUITER DASHBOARD (dashboard.html)
// ==========================================
function loadDashboard() {
    const candidateTableBody = document.getElementById('candidateTableBody');
    if (!candidateTableBody) return;

    fetch('/candidates')
    .then(res => res.json())
    .then(data => {
        allCandidates = data;
        renderCandidateTable(allCandidates);
        updateStatistics(allCandidates);
    })
    .catch(err => {
        showDashboardError("Failed to fetch candidate applications. " + err.message);
    });
}

function renderCandidateTable(candidates) {
    const body = document.getElementById('candidateTableBody');
    if (candidates.length === 0) {
        body.innerHTML = `
            <tr>
                <td colspan="7" class="text-center py-5 text-secondary">
                    <i class="bi bi-folder-symlink fs-1 text-muted d-block mb-3"></i>
                    No candidate applications found.
                </td>
            </tr>
        `;
        return;
    }

    body.innerHTML = candidates.map(c => {
        const scoreClass = c.score >= 80 ? 'score-high' : (c.score >= 40 ? 'score-medium' : 'score-low');
        const formattedDate = new Date(c.createdDate).toLocaleDateString('en-US', {
            year: 'numeric', month: 'short', day: 'numeric'
        });

        return `
            <tr id="row-${c.id}">
                <td>
                    <div class="fw-bold text-white">${escapeHtml(c.name)}</div>
                </td>
                <td>
                    <div class="text-secondary">${escapeHtml(c.email)}</div>
                    <div class="small text-muted">${c.phone}</div>
                </td>
                <td>
                    <span class="text-light">${escapeHtml(c.jobRole)}</span>
                </td>
                <td align="center">
                    <span class="score-badge ${scoreClass}">${c.score}%</span>
                </td>
                <td>
                    <select class="form-select form-select-sm w-auto" onchange="updateCandidateStatusDirect(${c.id}, this)">
                        <option value="Applied" ${c.status === 'Applied' ? 'selected' : ''}>Applied</option>
                        <option value="Shortlisted" ${c.status === 'Shortlisted' ? 'selected' : ''}>Shortlisted</option>
                        <option value="Highly Recommended" ${c.status === 'Highly Recommended' ? 'selected' : ''}>Highly Recommended</option>
                        <option value="Under Review" ${c.status === 'Under Review' ? 'selected' : ''}>Under Review</option>
                        <option value="Interview Scheduled" ${c.status === 'Interview Scheduled' ? 'selected' : ''}>Interview Scheduled</option>
                        <option value="Selected" ${c.status === 'Selected' ? 'selected' : ''}>Selected</option>
                        <option value="Rejected" ${c.status === 'Rejected' ? 'selected' : ''}>Rejected</option>
                    </select>
                </td>
                <td>
                    <span class="small text-secondary">${formattedDate}</span>
                </td>
                <td align="right" style="padding-right: 1.5rem !important;">
                    <div class="d-inline-flex gap-2">
                        <a href="/candidate-details?id=${c.id}" class="btn-action btn-view" title="View Details">
                            <i class="bi bi-eye-fill"></i>
                        </a>
                        <button onclick="downloadResumeDirect(${c.id})" class="btn-action btn-download" title="Download Resume">
                            <i class="bi bi-download"></i>
                        </button>
                        <button onclick="deleteCandidateDirect(${c.id})" class="btn-action btn-delete" title="Delete Candidate">
                            <i class="bi bi-trash-fill"></i>
                        </button>
                    </div>
                </td>
            </tr>
        `;
    }).join('');
}

function updateStatistics(candidates) {
    document.getElementById('statTotal').textContent = candidates.length;
    
    // Shortlisted includes both "Shortlisted" and "Highly Recommended"
    const shortlisted = candidates.filter(c => c.status === 'Shortlisted' || c.status === 'Highly Recommended').length;
    const selected = candidates.filter(c => c.status === 'Selected').length;
    const rejected = candidates.filter(c => c.status === 'Rejected').length;

    document.getElementById('statShortlisted').textContent = shortlisted;
    document.getElementById('statSelected').textContent = selected;
    document.getElementById('statRejected').textContent = rejected;
}

// Client side Search Filter
function filterCandidates() {
    const query = document.getElementById('searchInput').value.toLowerCase().trim();
    if (!query) {
        renderCandidateTable(allCandidates);
        return;
    }

    const filtered = allCandidates.filter(c => 
        c.name.toLowerCase().includes(query) || 
        c.email.toLowerCase().includes(query)
    );
    renderCandidateTable(filtered);
}

// Direct Status Update from Dashboard Table Dropdown
function updateCandidateStatusDirect(id, selectElement) {
    const newStatus = selectElement.value;
    
    fetch(`/candidate/status/${id}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ status: newStatus })
    })
    .then(res => {
        if (!res.ok) throw new Error("Status update failed.");
        return res.json();
    })
    .then(data => {
        // Update cached values
        const candidate = allCandidates.find(c => c.id === id);
        if (candidate) {
            candidate.status = data.status;
            updateStatistics(allCandidates);
        }
        showDashboardSuccess(`Status for ${data.name} updated to ${data.status} successfully.`);
    })
    .catch(err => {
        showDashboardError(err.message);
        loadDashboard(); // Reload to restore correct state
    });
}

// Direct Delete from Dashboard
function deleteCandidateDirect(id) {
    if (!confirm("Are you sure you want to delete this candidate and their resume?")) return;

    fetch(`/candidate/${id}`, { method: 'DELETE' })
    .then(res => {
        if (!res.ok) throw new Error("Deletion failed.");
        return res.json();
    })
    .then(data => {
        allCandidates = allCandidates.filter(c => c.id !== id);
        renderCandidateTable(allCandidates);
        updateStatistics(allCandidates);
        showDashboardSuccess("Candidate record deleted successfully.");
    })
    .catch(err => {
        showDashboardError(err.message);
    });
}

// Redirect trigger for downloads
function downloadResumeDirect(id) {
    window.location.href = `/download/${id}`;
}

function showDashboardError(msg) {
    const box = document.getElementById('alertBox');
    box.textContent = msg;
    box.className = "alert alert-danger";
    box.classList.remove('d-none');
    setTimeout(() => box.classList.add('d-none'), 5000);
}

function showDashboardSuccess(msg) {
    const box = document.getElementById('alertBox');
    box.textContent = msg;
    box.className = "alert alert-success";
    box.classList.remove('d-none');
    setTimeout(() => box.classList.add('d-none'), 5000);
}

// ==========================================
// 3. CANDIDATE DETAILS INSPECTOR (candidate-details.html)
// ==========================================
function loadCandidateDetails() {
    const urlParams = new URLSearchParams(window.location.search);
    const candidateId = urlParams.get('id');

    if (!candidateId) {
        showDetailsError("No Candidate ID provided in URL.");
        return;
    }

    fetch(`/candidate/${candidateId}`)
    .then(res => {
        if (!res.ok) throw new Error("Candidate details could not be found.");
        return res.json();
    })
    .then(candidate => {
        renderDetailsPage(candidate);
    })
    .catch(err => {
        showDetailsError(err.message);
    });
}

function renderDetailsPage(c) {
    document.getElementById('candName').textContent = c.name;
    document.getElementById('candJobRole').textContent = c.jobRole;
    document.getElementById('candEmail').textContent = c.email;
    document.getElementById('candPhone').textContent = c.phone;
    document.getElementById('candFileName').textContent = c.resumeFileName || "resume.pdf";
    document.getElementById('candText').textContent = c.resumeText || "No text extracted from resume.";
    
    // Format Date
    const dateFormatted = new Date(c.createdDate).toLocaleDateString('en-US', {
        year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit'
    });
    document.getElementById('candDate').textContent = dateFormatted;

    // Score Meter
    const scoreElement = document.getElementById('candScore');
    scoreElement.textContent = `${c.score}%`;
    const scoreClass = c.score >= 80 ? 'score-high' : (c.score >= 40 ? 'score-medium' : 'score-low');
    scoreElement.className = `score-badge ${scoreClass} fs-5 py-2 px-3`;

    // Dropdown selection
    const select = document.getElementById('statusSelect');
    select.value = c.status;

    // Dynamic Skill Highlighter
    highlightSkills(c.resumeText);

    // Set actions
    document.getElementById('downloadBtn').onclick = () => downloadResumeDirect(c.id);
    document.getElementById('deleteBtn').onclick = () => {
        if (confirm("Are you sure you want to delete this candidate?")) {
            fetch(`/candidate/${c.id}`, { method: 'DELETE' })
            .then(res => {
                if (!res.ok) throw new Error("Deletion failed.");
                window.location.href = '/dashboard';
            })
            .catch(err => showDetailsError(err.message));
        }
    };
}

function highlightSkills(resumeText) {
    const container = document.getElementById('skillsTagContainer');
    if (!container) return;

    if (!resumeText) {
        container.innerHTML = `<span class="text-muted">No resume text available for analysis.</span>`;
        return;
    }

    const textLower = resumeText.toLowerCase();

    container.innerHTML = REQUIRED_SKILLS.map(skill => {
        const isMatched = checkSkillPresence(textLower, skill.toLowerCase());
        const tagClass = isMatched ? 'matched' : 'missing';
        const icon = isMatched ? '<i class="bi bi-check-circle-fill me-1"></i>' : '<i class="bi bi-x-circle me-1"></i>';
        
        return `<span class="skill-tag ${tagClass}">${icon}${skill}</span>`;
    }).join('');
}

// Custom Regex Word Boundary matcher mirroring Backend logic
function checkSkillPresence(textLower, skillLower) {
    if (skillLower.match(/^[a-z]+$/)) {
        const regex = new RegExp('\\b' + skillLower + '\\b');
        return regex.test(textLower);
    } else {
        return textLower.includes(skillLower);
    }
}

// Details page manual status dropdown update
function updateCandidateStatus() {
    const urlParams = new URLSearchParams(window.location.search);
    const id = urlParams.get('id');
    const select = document.getElementById('statusSelect');
    const newStatus = select.value;
    const box = document.getElementById('alertBox');

    fetch(`/candidate/status/${id}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ status: newStatus })
    })
    .then(res => {
        if (!res.ok) throw new Error("Failed to update status.");
        return res.json();
    })
    .then(data => {
        box.textContent = "Status updated successfully.";
        box.className = "alert alert-success";
        box.classList.remove('d-none');
        setTimeout(() => box.classList.add('d-none'), 3000);
    })
    .catch(err => {
        showDetailsError(err.message);
    });
}

function showDetailsError(msg) {
    const box = document.getElementById('alertBox');
    box.textContent = msg;
    box.className = "alert alert-danger";
    box.classList.remove('d-none');
}


// ==========================================
// HELPERS
// ==========================================
function getStatusClass(status) {
    switch (status) {
        case 'Highly Recommended': return 'status-highly-recommended';
        case 'Shortlisted': return 'status-shortlisted';
        case 'Under Review': return 'status-under-review';
        case 'Rejected': return 'status-rejected';
        case 'Applied': return 'status-applied';
        case 'Selected': return 'status-selected';
        case 'Interview Scheduled': return 'status-scheduled';
        default: return 'status-applied';
    }
}

function escapeHtml(text) {
    if (!text) return "";
    return text
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}
