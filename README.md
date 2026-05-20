# 🤖 SmartHire AI — AI-Powered Resume Analyzer

> Upload your PDF/DOCX resume + paste a job description → get ATS score, skill gaps, strengths, and **Actions to be Taken** — powered by free Groq LLaMA 3.3 AI.

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2-brightgreen?style=flat-square)
![Groq](https://img.shields.io/badge/Groq-LLaMA_3.3_70B-blue?style=flat-square)
![Render](https://img.shields.io/badge/Deployed-Render.com-purple?style=flat-square)

---

## ✨ Features
- 📎 **Upload PDF or DOCX resume** — no copy-paste (Apache PDFBox + POI)
- 🎯 **ATS Score 0–100** — Poor / Average / Good / Excellent
- ✅ **Matching Skills** — found in both resume and job description
- ❌ **Missing Skills** — key skills from JD not in resume
- ⭐ **Strengths** — genuine strong points of the resume
- 💡 **Areas to Improve** — specific things to fix
- 🚀 **Actions to be Taken** — concrete next steps
- 💾 **Analysis History** — every result auto-saved to H2 database
- 🌐 **Live on Render.com** — free, no credit card needed

---

## 🆓 100% Free Stack — Total Cost: ₹0

| Service | Purpose | Free Tier | Card? |
|---------|---------|-----------|-------|
| **Groq AI** | LLaMA 3.3 70B analysis | 14,400 req/day | ❌ No |
| **Render.com** | Cloud hosting | 750 hrs/month | ❌ No |
| **GitHub** | Code + CI/CD | 2,000 CI min/month | ❌ No |
| **H2 Database** | Embedded storage | Unlimited (in-memory) | ❌ No |

---

## 🚀 Run Locally

```bash
# 1. Clone
git clone https://github.com/shayan-304/smarthire-ai.git
cd smarthire-ai

# 2. Get free key at: console.groq.com → API Keys
# 3. Set the key
export GROQ_API_KEY=gsk_your-key-here      # Linux/Mac
set GROQ_API_KEY=gsk_your-key-here         # Windows

# 4. Run
mvn clean spring-boot:run

# 5. Open
# http://localhost:8080              → main app
# http://localhost:8080/api/health   → health check
# http://localhost:8080/h2-console   → view saved analysis data
```

---

## ☁️ Deploy Free on Render.com

1. Push to GitHub
2. Go to [render.com](https://render.com) → New → Web Service
3. Connect repo `shayan-304/smarthire-ai`
4. Select **Docker** | Plan: **Free**
5. Add env var: `GROQ_API_KEY` = your key
6. Click Deploy → live HTTPS URL in ~5 minutes

---

## 📡 API Endpoints

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/analyze` | Analyze via pasted text |
| POST | `/api/upload-analyze` | Analyze via PDF/DOCX file |
| GET | `/api/health` | Health check (Render uses this) |
| GET | `/api/history` | Last 20 saved analyses |
| GET | `/api/stats` | Total count + version info |

---

## 🏗️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.2 |
| AI Engine | Groq LLaMA 3.3 70B (`llama-3.3-70b-versatile`) |
| PDF Parsing | Apache PDFBox 2.0.31 |
| DOCX Parsing | Apache POI 5.2.3 |
| Database | H2 In-Memory + Spring Data JPA |
| Container | Docker |
| Hosting | Render.com (free tier) |
| CI/CD | GitHub Actions |
| Frontend | HTML5 + CSS3 + Vanilla JavaScript |

---

## 📁 Project Structure

```
smarthire-ai/
├── src/main/java/com/smarthire/
│   ├── SmartHireApplication.java
│   ├── controller/ResumeController.java    ← 5 REST endpoints
│   ├── service/
│   │   ├── AiAnalysisService.java          ← Groq AI integration
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

## 🔧 5 Production Bugs Fixed

| # | Bug | Fix Applied |
|---|-----|------------|
| 1 | PDFBox 3.x removed `PDDocument.load(InputStream)` | Downgraded to **2.0.31** |
| 2 | POI 5.2.5 classpath conflict with Spring logging | Downgraded to **5.2.3** + exclusions |
| 3 | API key placeholder used as actual key | Fixed fallback + `@PostConstruct` |
| 4 | No startup key validation | Added `@PostConstruct init()` |
| 5 | JSON parsing crashed on extra Groq text | Added `indexOf({})` boundary extraction |

---

## 👤 Author

**Mafaaz Shayan** — Electronics & Communication Engineering, Class of 2026

Built as a portfolio project demonstrating: Java backend development, REST API design, AI/LLM integration, file handling (PDF/DOCX), Docker containerization, cloud deployment, and CI/CD pipelines.

---

*Designed by Mafaaz Shayan*
