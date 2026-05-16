# 🤖 SmartHire AI — Resume Analyzer

> AI-powered resume analysis tool built with Java Spring Boot + Google Groq (Free LLaMA 3.3)

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2-brightgreen?style=flat-square)
![Groq](https://img.shields.io/badge/Groq-LLaMA_3.3_70B-blue?style=flat-square)
![Render](https://img.shields.io/badge/Deployed-Render.com-purple?style=flat-square)

## ✨ Features
- Upload PDF or DOCX resume — no copy-paste needed
- ATS compatibility score (0–100)
- Matching and missing skills analysis
- Strengths, improvements, action items
- 100% free AI (Groq LLaMA 3.3) — works in India

## 🆓 Free Services Used
| Service | Purpose | Free Tier |
|---------|---------|-----------|
| Groq API | AI analysis | 14,400 req/day, no card |
| Render.com | Hosting | 750 hrs/month, no card |
| GitHub | Code + CI/CD | Free forever |

## 🚀 Run Locally
```bash
git clone https://github.com/shayan-304/smarthire-ai.git
cd smarthire-ai

# Get free key at console.groq.com → API Keys
export GROQ_API_KEY=your-key-here   # Linux/Mac
set GROQ_API_KEY=your-key-here      # Windows

mvn clean spring-boot:run
# Open: http://localhost:8080
```

## ☁️ Deploy Free on Render.com
1. Push to GitHub
2. Go to render.com → New → Web Service
3. Connect GitHub repo `shayan-304/smarthire-ai`
4. Select **Docker** environment
5. Add env var: `GROQ_API_KEY = your-key`
6. Click Deploy — free HTTPS URL in 5 minutes

## 🔑 Get Free Groq API Key
1. Go to **console.groq.com**
2. Sign up with Google account
3. Click **API Keys** → **Create API Key**
4. Free: 14,400 requests/day, no credit card

## 🏗️ Tech Stack
- Java 17 + Spring Boot 3.2
- Groq API (LLaMA 3.3 70B) — free AI
- Apache PDFBox 2.0.31 — PDF parsing
- Apache POI 5.2.3 — DOCX parsing
- Docker + Render.com — free deployment
- GitHub Actions — CI/CD

## 📡 API Endpoints
- `POST /api/analyze` — text input analysis
- `POST /api/upload-analyze` — PDF/DOCX file analysis
- `GET /api/health` — health check
