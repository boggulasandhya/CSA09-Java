// server.js
// AI-Driven Web Application Vulnerability Detection and Risk Prioritization Framework
// Capstone Project Web Application Suite

const express = require('express');
const cors = require('cors');
const path = require('path');

// Import the 3 Capstone Modules
const scannerEngine = require('./modules/scanner/scannerEngine');
const sampleTargets = require('./modules/scanner/sampleTargets');
const aiClassifier = require('./modules/ai/aiClassifier');
const falsePositiveFilter = require('./modules/ai/falsePositiveFilter');
const securityCopilot = require('./modules/ai/securityCopilot');
const cvssCalculator = require('./modules/risk/cvssCalculator');
const riskPrioritization = require('./modules/risk/riskPrioritization');
const reportGenerator = require('./modules/risk/reportGenerator');

const app = express();
const PORT = process.env.PORT || 5000;

app.use(cors());
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true, limit: '10mb' }));

// Serve frontend static assets
app.use(express.static(path.join(__dirname, 'public')));

// ============================================================================
// 1. PROJECT METADATA & TEAM ATTRIBUTION API
// ============================================================================
app.get('/api/project/info', (req, res) => {
  res.json({
    title: "AI-Driven Web Application Vulnerability Detection and Risk Prioritization Framework",
    academicScope: "Final Year Capstone Project",
    teamMembers: [
      {
        moduleNumber: 1,
        moduleName: "Web Application Vulnerability Scanning Module",
        studentName: "Student 1",
        responsibilities: [
          "Dynamic Web Application Security Testing (DAST) crawling engine",
          "HTTP Security Headers inspection & misconfiguration auditing",
          "OWASP Top 10 injection heuristics (SQLi, XSS, SSRF, IDOR)",
          "Static Application Security Testing (SAST) for source code analysis"
        ]
      },
      {
        moduleNumber: 2,
        moduleName: "AI-Based Vulnerability Analysis and Classification Module",
        studentName: "Student 2",
        responsibilities: [
          "Deep Learning Transformer / CyberBERT vulnerability classifier",
          "Automated CWE & OWASP Top 10 taxonomy mapping",
          "Context-aware False Positive reduction & verification engine",
          "Interactive AI Security Copilot with attention token heatmaps"
        ]
      },
      {
        moduleNumber: 3,
        moduleName: "Risk Prioritization and Security Reporting Module",
        studentName: "Student 3",
        responsibilities: [
          "Industry standard CVSS v3.1 / v4.0 multi-factor calculation",
          "Dynamic composite risk scoring with asset criticality weighting",
          "Actionable SLA priority triage queue (P1 Emergency to P4 Low)",
          "Comprehensive executive reporting & side-by-side remediation code diffs"
        ]
      }
    ]
  });
});

// ============================================================================
// 2. MODULE 1 APIS: VULNERABILITY SCANNING (STUDENT 1)
// ============================================================================
app.get('/api/targets', (req, res) => {
  res.json(sampleTargets.targets);
});

app.post('/api/scan/start', (req, res) => {
  try {
    const session = scannerEngine.startScan(req.body);
    res.json({
      success: true,
      message: "Vulnerability scan initiated successfully.",
      scanId: session.scanId,
      session
    });
  } catch (error) {
    res.status(500).json({ success: false, error: error.message });
  }
});

app.get('/api/scan/status/:id', (req, res) => {
  const session = scannerEngine.getScanStatus(req.params.id);
  if (!session) {
    return res.status(404).json({ success: false, message: "Scan session not found." });
  }
  res.json(session);
});

app.post('/api/scan/code', (req, res) => {
  try {
    const { code, language } = req.body;
    if (!code) {
      return res.status(400).json({ success: false, error: "Source code snippet is required." });
    }
    const result = scannerEngine.scanSourceCode(code, language);
    res.json({ success: true, result });
  } catch (error) {
    res.status(500).json({ success: false, error: error.message });
  }
});

// ============================================================================
// 3. MODULE 2 APIS: AI ANALYSIS & CLASSIFICATION (STUDENT 2)
// ============================================================================
app.post('/api/ai/classify', (req, res) => {
  try {
    const { finding } = req.body;
    if (!finding) {
      return res.status(400).json({ success: false, error: "Finding object is required." });
    }
    const classification = aiClassifier.classifyFinding(finding);
    const fpEvaluation = falsePositiveFilter.evaluateFalsePositive(finding, classification);
    res.json({
      success: true,
      classification,
      fpEvaluation
    });
  } catch (error) {
    res.status(500).json({ success: false, error: error.message });
  }
});

app.get('/api/ai/stats', (req, res) => {
  res.json(aiClassifier.getModelStats());
});

app.post('/api/ai/copilot', (req, res) => {
  try {
    const { prompt, finding } = req.body;
    const answer = securityCopilot.answerQuery(prompt || "", finding || null);
    res.json({ success: true, answer });
  } catch (error) {
    res.status(500).json({ success: false, error: error.message });
  }
});

// ============================================================================
// 4. MODULE 3 APIS: RISK PRIORITIZATION & REPORTING (STUDENT 3)
// ============================================================================
app.post('/api/risk/prioritize', (req, res) => {
  try {
    const { rawFindings, aiClassificationsMap, assetEnvironment } = req.body;
    const prioritized = riskPrioritization.prioritizeFindings(
      rawFindings || [],
      aiClassificationsMap || {},
      assetEnvironment || 'PRODUCTION_TIER1'
    );
    res.json({ success: true, prioritized });
  } catch (error) {
    res.status(500).json({ success: false, error: error.message });
  }
});

app.post('/api/reports/generate', (req, res) => {
  try {
    const { scanSession, prioritizedResult } = req.body;
    const report = reportGenerator.generateExecutiveReport(scanSession || {}, prioritizedResult || {});
    res.json({ success: true, report });
  } catch (error) {
    res.status(500).json({ success: false, error: error.message });
  }
});

app.post('/api/reports/markdown', (req, res) => {
  try {
    const { report } = req.body;
    const markdown = reportGenerator.generateMarkdown(report || {});
    res.setHeader('Content-Type', 'text/markdown');
    res.setHeader('Content-Disposition', 'attachment; filename="Security_Audit_Report.md"');
    res.send(markdown);
  } catch (error) {
    res.status(500).send("Failed to generate markdown");
  }
});

// Fallback to index.html for SPA routing
app.get('*', (req, res) => {
  res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

// Start the server
app.listen(PORT, () => {
  console.log(`===========================================================================`);
  console.log(`  AI-Driven Vulnerability Detection & Risk Prioritization Framework`);
  console.log(`  Capstone Project Web Application Suite`);
  console.log(`===========================================================================`);
  console.log(`  Server is actively running on: http://localhost:${PORT}`);
  console.log(`  Module 1: Web Application Vulnerability Scanning  [READY]`);
  console.log(`  Module 2: AI-Based Vulnerability Classification    [READY]`);
  console.log(`  Module 3: Risk Prioritization & Security Reporting [READY]`);
  console.log(`===========================================================================`);
});
