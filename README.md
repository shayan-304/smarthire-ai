#  SmartHire AI — AI-Powered Resume Analyzer

SmartHire AI is a resume analysis tool built using Spring Boot and Google Gemini 2.5 Flash models.
The application compares a resume with a job description and provides an ATS-style evaluation with suggestions for improvement.

Users can upload resumes in PDF or DOCX format and receive:

 - ATS score

 - Matching skills

 - Missing skills

- Resume strengths

- Suggestions for improvement

The project also stores previous analysis results using an H2 database.

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2-brightgreen?style=flat-square)
![Gemini](https://img.shields.io/badge/Google-Gemini_2.5_Flash-blue?style=flat-square)
![Render](https://img.shields.io/badge/Deployed-Render.com-purple?style=flat-square)

---

## Features
-  **Upload PDF or DOCX resume** — no copy-paste (Apache PDFBox + POI)
-  **ATS Score 0–100** — Poor / Average / Good / Excellent
-  **Matching Skills** — found in both resume and job description
-  **Missing Skills** — key skills from JD not in resume
-  **Strengths** — genuine strong points of the resume
-  **Areas to Improve** — specific things to fix
-  **Improvement suggestions** — concrete next steps
-  **Analysis History** — every result auto-saved to H2 database
-  **Live on Render.com** — free, no credit card needed
  
---

## Run Locally

```bash
# 1. Clone
git clone https://github.com/shayan-304/smarthire-ai.git
cd smarthire-ai

# 2. Get key at: aistudio.google.com
# 3. Set the key
export GEMINI_API_KEY=your-key-here      # Linux/Mac
set GEMINI_API_KEY=your-key-here         # Windows

# 4. Run
mvn clean spring-boot:run

# 5. Open
# http://localhost:8080              → main app
# http://localhost:8080/api/health   → health check
# http://localhost:8080/h2-console   → view saved analysis data
```

---

## Deploy on Render.com

1. Push to GitHub
2. Go to [render.com](https://render.com) → New → Web Service
3. Connect repo `shayan-304/smarthire-ai`
4. Select **Docker** | 
5. Add env var: `GEMINI_API_KEY` = your key
6. Click Deploy → live HTTPS URL in ~5 minutes

---

## API Endpoints

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/analyze` | Analyze via pasted text |
| POST | `/api/upload-analyze` | Analyze via PDF/DOCX file |
| GET | `/api/health` | Health check (Render uses this) |
| GET | `/api/history` | Last 20 saved analyses |
| GET | `/api/stats` | Total count + version info |

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.2 |
| AI Engine | Google Gemini 2.5 Flash |
| PDF Parsing | Apache PDFBox 2.0.31 |
| DOCX Parsing | Apache POI 5.2.3 |
| Database | H2 In-Memory + Spring Data JPA |
| Container | Docker |
| Hosting | Render.com (free tier) |
| CI/CD | GitHub Actions |
| Frontend | HTML5 + CSS3 + Vanilla JavaScript |

---

## Notes

PDF parsing is handled using Apache PDFBox

DOCX parsing is handled using Apache POI

Analysis history is stored using H2 Database

Gemini API is used for generating resume analysis results

---
## 📁 Project Structure

```
smarthire-ai/
├── src/main/java/com/smarthire/
│   ├── SmartHireApplication.java
│   ├── controller/ResumeController.java    ← 5 REST endpoints
│   ├── service/
│   │   ├── AiAnalysisService.java          ← Gemini AI integration
│   │   ├── FileParserService.java          ← PDF/DOCX parsing
│   │   └── HistoryService.java             ← DB save wrapper
│   ├── model/
│   │   ├── AnalysisRequest.java
│   │   ├── AnalysisResponse.java
│   │   └── AnalysisHistory.java            ← DB entity
│   └── repository/AnalysisHistoryRepository.java
├── src/main/resources/
│   ├── application.properties
│   └── static/index.html                   ← Full frontend (600+ lines)
├── .github/workflows/deploy.yml            ← CI/CD pipeline
├── Dockerfile
├── render.yaml
└── pom.xml
```

---

##  5 Production Bugs Fixed

| # | Bug | Fix Applied |
|---|-----|------------|
| 1 | PDFBox 3.x removed `PDDocument.load(InputStream)` | Downgraded to **2.0.31** |
| 2 | POI 5.2.5 classpath conflict with Spring logging | Downgraded to **5.2.3** + exclusions |
| 3 | API key placeholder used as actual key | Fixed fallback + `@PostConstruct` |
| 4 | No startup key validation | Added `@PostConstruct init()` |
| 5 | JSON parsing crashed on extra text | Added `indexOf({})` boundary extraction |

---

## 👤 Author

**Mafaaz Shayan M** — Electronics & Communication Engineering Student, Class of 2026

Built for learning backend development, REST APIs, file handling, and LLM integration using Java and Spring Boot.

---

*Designed by Mafaaz Shayan*
