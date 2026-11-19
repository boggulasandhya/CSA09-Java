# AI-Driven Web Application Vulnerability Detection and Risk Prioritization Framework
## Capstone Project Architecture & Implementation Guide (3-Student Team)

---

### Project Title
**AI-Driven Web Application Vulnerability Detection and Risk Prioritization Framework**

### Project Overview & Problem Statement
Modern web applications face an expanding attack surface characterized by complex REST/GraphQL APIs, multi-tenant cloud microservices, and dynamic client-side rendering. Conventional vulnerability scanners suffer from high false-positive rates and treat all detected vulnerabilities uniformly, ignoring business context and asset criticality. 

This Capstone Project develops an **end-to-end automated framework** combining:
1. **Dynamic & Static Vulnerability Scanning (DAST + SAST)**
2. **Deep Learning Natural Language Processing (NLP) Vulnerability Classification & False Positive Filtering**
3. **Contextual Risk Prioritization & Enterprise Security Reporting**

---

## 3-Student Module Distribution & Ownership

| Student | Assigned Module | Key Deliverables & Technical Contributions |
| :--- | :--- | :--- |
| **Student 1** | **Web Application Vulnerability Scanning Module** | • Active DAST crawling engine & attack surface mapper<br>• HTTP response security header audit (CSP, HSTS, X-Frame, CORS)<br>• OWASP Top 10 injection heuristics (SQLi, XSS, SSRF, IDOR)<br>• Static Application Security Testing (SAST) for source code analysis |
| **Student 2** | **AI-Based Vulnerability Analysis and Classification Module** | • Fine-tuned CyberBERT Transformer NLP classification pipeline<br>• Automated CWE & OWASP Top 10 taxonomy mapping<br>• Contextual False Positive reduction engine with confidence scoring<br>• Interactive AI Security Copilot with NLP token attention heatmaps |
| **Student 3** | **Risk Prioritization and Security Reporting Module** | • Industry-standard multi-factor CVSS v3.1 / v4.0 calculation engine<br>• Asset criticality multipliers & dynamic composite risk scoring<br>• Actionable SLA priority triage queue (P1 Emergency to P4 Low)<br>• Side-by-side code remediation diffs & multi-format export (PDF, Markdown, JSON) |

---

## Pipeline Data Flow Architecture

```
[Target URL / Source Code]
            │
            ▼
┌────────────────────────────────────────────────────────┐
│ MODULE 1 (Student 1)                                   │
│ Web Application Vulnerability Scanning                 │
│ • URL Crawler & Endpoint Discovery                     │
│ • Security Headers Audit (CSP, HSTS, CORS)             │
│ • DAST Payload Injection (SQLi, XSS, SSRF, IDOR)       │
│ • Static Source Code (SAST) Insecure Sink Detection    │
└───────────────────────────┬────────────────────────────┘
                            │ Raw Findings & Evidence
                            ▼
┌────────────────────────────────────────────────────────┐
│ MODULE 2 (Student 2)                                   │
│ AI-Based Vulnerability Analysis & Classification       │
│ • Transformer NLP Pipeline (CyberBERT-VulnClassify)    │
│ • Semantic Token Attention Heatmap Calculation         │
│ • Automated CWE & OWASP Top 10 Taxonomy Mapping        │
│ • False Positive Filter (True Positive vs FP Prob)     │
│ • Interactive AI Security Copilot Guidance             │
└───────────────────────────┬────────────────────────────┘
                            │ Enriched & Verified Threat Data
                            ▼
┌────────────────────────────────────────────────────────┐
│ MODULE 3 (Student 3)                                   │
│ Risk Prioritization & Security Reporting               │
│ • Multi-Factor CVSS v3.1 Score Deconstruction          │
│ • Asset Criticality Weighting (Tier 1 Core vs Staging) │
│ • Composite Risk Calculation: CVSS * Asset * Threat    │
│ • Actionable SLA Triage Queue (P1 24h to P4 30d)       │
│ • Side-by-Side Code Diffs & Multi-Format Reports       │
└────────────────────────────────────────────────────────┘
```

---

## Running the Web Application on Localhost

The server is actively running on **`http://localhost:5000`**.

To launch or restart the application manually:
```bash
# Windows
start_web_app.bat
# or
node server.js

# Linux / macOS
./start_web_app.sh
```

---

## Dashboard Navigation Guide

1. **Executive Dashboard:**
   - Real-time KPIs: Identified Flaws, P1 Emergency SLA count, P2 High Priority count, and Overall Posture.
   - Interactive Visualizations: Severity Distribution Donut Chart, OWASP Top 10 Bar Chart, Attack Surface Radar Chart.
2. **Module 1: Vulnerability Scanner (Student 1):**
   - Benchmark Target Catalog: ApexSecure FinTech Banking, GlobalMart E-Commerce, MedPulse Hospital EHR, or custom URL.
   - Live Scan Progress: Real-time progress bar, live terminal log feed, discovered endpoints table.
   - SAST Source Code Scanner: Test live code snippets with one-click presets (SQLi, XSS, Command Injection, Hardcoded Secrets).
3. **Module 2: AI Classification & Copilot (Student 2):**
   - Deep learning classification cards displaying model confidence, CWE definition, and semantic token attention heatmaps.
   - False positive probability assessment with contextual reasoning breadcrumbs.
   - Interactive AI Security Copilot chat for explaining vulnerability mechanics and defense strategies.
4. **Module 3: Risk Prioritization & Reports (Student 3):**
   - Asset Criticality Multiplier selector (Tier 1 Core Financial 1.5x, Customer Facing 1.2x, Staging 0.9x, Dev 0.7x).
   - Priority Triage Queue: Ranked by Composite Dynamic Risk with assigned SLA remediation windows.
   - Side-by-Side Remediation Code Diffs: Insecure code vs Secure parameterized/contextually encoded code.
   - Multi-Format Export Center: One-click export to printable PDF, Markdown playbook, and JSON audit telemetry.
5. **Capstone Architecture & Team:**
   - Visual team contribution matrix and architecture workflow diagram.
