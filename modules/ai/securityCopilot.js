// modules/ai/securityCopilot.js
// Student 2: AI-Based Vulnerability Analysis and Classification Module - AI Security Copilot

class SecurityCopilot {
  /**
   * Generates expert contextual security answers and remediation code
   * @param {string} prompt
   * @param {Object} contextFinding
   * @returns {Object} response
   */
  answerQuery(prompt, contextFinding = null) {
    const p = prompt.toLowerCase();

    if (p.includes("sql") || p.includes("cwe-89") || (contextFinding && contextFinding.type === "SQL Injection")) {
      return {
        topic: "SQL Injection Remediation & Exploit Mechanics",
        summary: "SQL Injection occurs when untrusted user input is directly concatenated into a dynamic SQL query string without parameter binding or prepared statements.",
        attackVector: "An attacker supplies meta-characters such as `' OR 1=1--` to bypass authentication or `UNION SELECT` to dump unauthorized tables.",
        remediationCode: {
          vulnerable: `// VULNERABLE: Direct string interpolation\nconst query = "SELECT * FROM users WHERE email = '" + req.body.email + "' AND pass = '" + req.body.pass + "';";\ndb.query(query);`,
          secure: `// SECURE: Parameterized Prepared Statement\nconst query = "SELECT id, email, role FROM users WHERE email = $1 AND pass_hash = crypt($2, pass_hash);";\ndb.query(query, [req.body.email, req.body.pass]);`
        },
        bestPractices: [
          "Always use parameterized queries or trusted Object-Relational Mappers (ORM) with prepared statement caching.",
          "Apply least privilege database roles (e.g. read-only accounts for search queries).",
          "Implement server-side input validation with strict regex allow-listing."
        ]
      };
    }

    if (p.includes("xss") || p.includes("cwe-79") || (contextFinding && contextFinding.type === "Cross-Site Scripting")) {
      return {
        topic: "Cross-Site Scripting (XSS) Prevention",
        summary: "XSS occurs when an application receives untrusted data and includes it in web pages without proper HTML/JavaScript contextual escaping.",
        attackVector: "Malicious JavaScript executes in the victim's browser context, gaining access to non-HttpOnly cookies, session tokens, or performing unauthorized DOM actions.",
        remediationCode: {
          vulnerable: `// VULNERABLE: Direct HTML innerHTML assignment\ndocument.getElementById('user-comment').innerHTML = userComment;`,
          secure: `// SECURE: Context-aware DOM textContent assignment\nconst el = document.getElementById('user-comment');\nel.textContent = userComment; // Automatically treats payload as raw text`
        },
        bestPractices: [
          "Deploy strict Content-Security-Policy (CSP) headers (e.g. default-src 'self'; script-src 'nonce-...').",
          "Set the HttpOnly flag on session cookies to prevent document.cookie theft.",
          "Contextually encode output according to HTML body, attribute, script, or URL contexts."
        ]
      };
    }

    if (p.includes("idor") || p.includes("cwe-639") || (contextFinding && contextFinding.type === "Broken Object Level Authorization")) {
      return {
        topic: "Insecure Direct Object Reference (IDOR) / BOLA",
        summary: "IDOR occurs when an application provides direct access to objects based on user-supplied input without verifying that the requesting user owns that object.",
        attackVector: "User A requests `/api/v1/accounts/10892/balance`, then modifies the integer ID to `10893` to view User B's private account balance.",
        remediationCode: {
          vulnerable: `// VULNERABLE: Missing ownership validation\napp.get('/api/accounts/:id', async (req, res) => {\n  const account = await db.findAccount(req.params.id);\n  res.json(account);\n});`,
          secure: `// SECURE: Tenant-scoped lookup enforcing session ownership\napp.get('/api/accounts/:id', authenticateToken, async (req, res) => {\n  const account = await db.findAccountWhere({ id: req.params.id, userId: req.user.id });\n  if (!account) return res.status(404).json({ error: 'Account not found or access denied' });\n  res.json(account);\n});`
        },
        bestPractices: [
          "Validate object authorization on every client request using the authenticated session context.",
          "Use non-sequential, cryptographically random UUID v4 values instead of auto-incrementing database integers.",
          "Implement centralized Attribute-Based Access Control (ABAC) or Role-Based Access Control (RBAC)."
        ]
      };
    }

    // Default response
    return {
      topic: "AI Security Engineering Guidance",
      summary: "The AI-Driven Vulnerability Framework continuously correlates detected threats against the MITRE ATT&CK framework and OWASP Top 10 standards.",
      attackVector: "Automated analysis reduces false positive alerts and computes contextual CVSS v3.1 / v4.0 dynamic risk vectors.",
      remediationCode: {
        vulnerable: `// General advice: Avoid dynamic string execution and unvalidated input sinks`,
        secure: `// Adopt defense-in-depth: Input Validation -> Access Control -> Encryption -> Auditing`
      },
      bestPractices: [
        "Incorporate Automated SAST/DAST into your CI/CD deployment pipelines.",
        "Maintain an up-to-date Software Bill of Materials (SBOM) to catch supply chain vulnerabilities.",
        "Implement automated regression testing for all resolved security flaws."
      ]
    };
  }
}

module.exports = new SecurityCopilot();
