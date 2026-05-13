# 🤖 SmartHire AI — Resume Analyzer

> AI-powered resume analysis tool that scores ATS compatibility, identifies skill gaps, and provides actionable improvement suggestions.

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2-brightgreen?style=flat-square)
![OpenAI](https://img.shields.io/badge/OpenAI-GPT--4o--mini-blue?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-purple?style=flat-square)

---

## ✨ Features

- **ATS Score** — Get a 0–100 compatibility score against any job description
- **Skill Gap Analysis** — See which skills match and which are missing
- **Strengths & Weaknesses** — Honest assessment of your resume's strong and weak points
- **Action Items** — Concrete next steps to improve your chances
- **Modern UI** — Clean, responsive dark-mode interface
- **REST API** — Full JSON API, easily extendable

---

## 🏗️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17, Spring Boot 3.2 |
| AI Integration | OpenAI GPT-4o-mini API |
| Frontend | Vanilla HTML/CSS/JS (served by Spring) |
| HTTP Client | Java 11+ `java.net.http.HttpClient` |
| Build | Maven 3.9 |
| Deployment | Docker, Railway / Render |

---

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- OpenAI API key → [Get one here](https://platform.openai.com/api-keys)

### 1. Clone the repository
```bash
git clone https://github.com/YOUR_USERNAME/smarthire-ai.git
cd smarthire-ai
```

### 2. Set your OpenAI API key
```bash
# Linux / macOS
export OPENAI_API_KEY="sk-your-api-key-here"

# Windows (Command Prompt)
set OPENAI_API_KEY=sk-your-api-key-here

# Windows (PowerShell)
$env:OPENAI_API_KEY="sk-your-api-key-here"
```

### 3. Build and run
```bash
mvn clean spring-boot:run
```

### 4. Open in browser
```
http://localhost:8080
```

---

## 🌐 API Endpoints

### `POST /api/analyze`
Analyze a resume against a job description.

**Request Body:**
```json
{
  "resumeText": "John Doe\nSoftware Engineer...",
  "jobDescription": "We are looking for a Java developer..."
}
```

**Response:**
```json
{
  "atsScore": 74,
  "scoreLabel": "Good",
  "experienceLevel": "Fresher",
  "overallFeedback": "Your resume shows strong technical foundations...",
  "matchingSkills": ["Java", "Spring Boot", "SQL", "REST APIs"],
  "missingSkills": ["Docker", "Kubernetes", "AWS"],
  "strengths": ["Relevant internship experience", "Strong academic background"],
  "improvements": ["Add quantified achievements", "Include cloud certifications"],
  "actionItems": ["Learn Docker basics", "Add GitHub project links", "Quantify your internship impact"],
  "success": true
}
```

### `GET /api/health`
Returns server health status.

### `GET /api/info`
Returns application metadata.

---

## 🐳 Docker

```bash
# Build image
docker build -t smarthire-ai .

# Run container
docker run -p 8080:8080 -e OPENAI_API_KEY=sk-xxx smarthire-ai
```

---

## ☁️ Deploy to Railway (Free)

1. Push this repo to GitHub
2. Go to [railway.app](https://railway.app) → New Project → Deploy from GitHub
3. Select your repo
4. Add environment variable: `OPENAI_API_KEY = sk-your-key`
5. Railway auto-detects the Dockerfile and deploys 🎉

**Your app will be live at:** `https://smarthire-ai-xxx.railway.app`

---

## 📁 Project Structure

```
smarthire-ai/
├── src/
│   └── main/
│       ├── java/com/smarthire/
│       │   ├── SmartHireApplication.java      # App entry point
│       │   ├── controller/
│       │   │   └── ResumeController.java       # REST endpoints
│       │   ├── service/
│       │   │   └── AiAnalysisService.java      # OpenAI integration
│       │   └── model/
│       │       ├── AnalysisRequest.java        # Input model
│       │       └── AnalysisResponse.java       # Output model
│       └── resources/
│           ├── application.properties          # Configuration
│           └── static/
│               └── index.html                  # Frontend UI
├── Dockerfile                                  # Container config
├── pom.xml                                     # Maven dependencies
└── README.md
```

---

## 💡 Future Enhancements

- [ ] PDF resume upload support
- [ ] Resume history with H2/PostgreSQL database
- [ ] Multiple job description comparison
- [ ] Email report delivery
- [ ] LinkedIn profile import

---

## 🔐 Security Notes

- **Never** commit your API key to Git
- Always use environment variables for secrets
- `.gitignore` is configured to exclude `.env` files

---

## 👤 Author

**Mafaaz** — Electronics & Communication Engineering  
Built as a portfolio project demonstrating Java, Spring Boot, REST APIs, and AI integration.

---

## 📄 License

MIT License — free to use, modify, and distribute.
