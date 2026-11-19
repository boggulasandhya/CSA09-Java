// modules/risk/reportGenerator.js
// Student 3: Risk Prioritization and Security Reporting Module - Report Generator

class SecurityReportGenerator {
  /**
   * Generates comprehensive structured security audit report
   */
  generateExecutiveReport(scanSession, prioritizedResult) {
    const findings = (prioritizedResult && prioritizedResult.rankedFindings) ? prioritizedResult.rankedFindings : [];
    const generatedAt = new Date().toUTCString();

    return {
      reportId: 'REP-' + Date.now().toString(36).toUpperCase(),
      title: 'AI-Driven Web Application Security Audit & Risk Prioritization Report',
      targetName: scanSession.targetName || 'Enterprise Web Target',
      targetUrl: scanSession.targetUrl || 'https://target.local',
      generatedAt,
      executiveSummary: {
        totalVulnerabilities: findings.length,
        criticalRisks: findings.filter(f => f.priorityTier.startsWith('P1')).length,
        highRisks: findings.filter(f => f.priorityTier.startsWith('P2')).length,
        mediumRisks: findings.filter(f => f.priorityTier.startsWith('P3')).length,
        lowRisks: findings.filter(f => f.priorityTier.startsWith('P4')).length,
        overallPosture: prioritizedResult.overallSystemPosture || 'ELEVATED RISK',
        assetCriticality: prioritizedResult.assetCriticalityLabel || 'Tier-1 Mission Critical Core'
      },
      complianceMapping: {
        owaspTop10_2021: {
          status: findings.length > 0 ? "NON-COMPLIANT" : "COMPLIANT",
          violatedCategories: [...new Set(findings.map(f => f.owasp || 'A03:2021-Injection'))]
        },
        pciDss_v4: {
          requirement_6_2: findings.some(f => f.cwe === 'CWE-89' || f.cwe === 'CWE-79') ? "FAILED: High-Severity Flaws Unpatched" : "PASSED",
          requirement_6_4: "FAILED: Critical Endpoints Lack Strict Output Encoding / Prepared Statements"
        },
        nistCsf_v2: {
          protectCategory: "DEGRADED",
          mitigationRecommendation: "Execute P1 remediation playbook within the mandated 24-hour SLA window."
        }
      },
      actionableRemediationRoadmap: findings.map((f, i) => ({
        rank: i + 1,
        findingId: f.findingId,
        vulnerability: f.name,
        cwe: f.cwe,
        priority: f.priorityTier,
        sla: f.slaWindow,
        cvssScore: f.cvssBaseScore,
        remediationSummary: f.remediationSummary,
        codeDiff: this._getCodeDiffForCwe(f.cwe)
      })),
      moduleAttributions: {
        module1_Scanning: "Student 1: Web Application Vulnerability Scanning Module",
        module2_AIClassification: "Student 2: AI-Based Vulnerability Analysis and Classification Module",
        module3_RiskReporting: "Student 3: Risk Prioritization and Security Reporting Module"
      }
    };
  }

  _getCodeDiffForCwe(cweId) {
    const diffs = {
      'CWE-89': {
        filename: 'statementRepository.js',
        vulnerable: `// VULNERABLE CODE (Line 42)\nconst query = "SELECT * FROM statements WHERE doc_id = '" + req.query.doc_name + "';";\nconst result = await db.rawQuery(query);`,
        secure: `// SECURE REMEDIATION (Line 42)\nconst query = "SELECT * FROM statements WHERE doc_id = $1;";\nconst result = await db.query(query, [req.query.doc_name]);`
      },
      'CWE-79': {
        filename: 'reviewRenderer.js',
        vulnerable: `// VULNERABLE CODE (Line 18)\nelement.innerHTML = "<div class='review'>" + userComment + "</div>";`,
        secure: `// SECURE REMEDIATION (Line 18)\nconst commentDiv = document.createElement('div');\ncommentDiv.className = 'review';\ncommentDiv.textContent = userComment; // Context-aware escaping\nelement.appendChild(commentDiv);`
      },
      'CWE-639': {
        filename: 'accountController.js',
        vulnerable: `// VULNERABLE CODE (Line 29)\nconst account = await Account.findById(req.params.id);\nreturn res.json(account);`,
        secure: `// SECURE REMEDIATION (Line 29)\nconst account = await Account.findOne({\n  where: { id: req.params.id, ownerId: req.session.userId }\n});\nif (!account) return res.status(403).json({ error: 'Access Denied' });\nreturn res.json(account);`
      },
      'CWE-918': {
        filename: 'webhookService.js',
        vulnerable: `// VULNERABLE CODE (Line 61)\nconst response = await axios.get(req.body.callback_url);`,
        secure: `// SECURE REMEDIATION (Line 61)\nconst parsedUrl = new URL(req.body.callback_url);\nconst ALLOWED_DOMAINS = ['hooks.slack.com', 'api.partner.io'];\nif (!ALLOWED_DOMAINS.includes(parsedUrl.hostname)) {\n  throw new SecurityException('Outbound host forbidden');\n}\nconst response = await axios.get(parsedUrl.href);`
      }
    };

    return diffs[cweId] || {
      filename: 'securityGuard.js',
      vulnerable: `// VULNERABLE CODE: Unrestricted input flow without boundary verification`,
      secure: `// SECURE REMEDIATION: Validate schemas with Joi/Zod, bind query parameters, and enforce RBAC`
    };
  }

  generateMarkdown(report) {
    let md = `# ${report.title}\n\n`;
    md += `**Target:** ${report.targetName} (${report.targetUrl})\n`;
    md += `**Report ID:** ${report.reportId} | **Generated:** ${report.generatedAt}\n`;
    md += `**System Posture:** ${report.executiveSummary.overallPosture}\n\n`;
    md += `## 1. Executive Summary\n\n`;
    md += `- Total Identified Flaws: **${report.executiveSummary.totalVulnerabilities}**\n`;
    md += `- Critical (P1) Priority: **${report.executiveSummary.criticalRisks}**\n`;
    md += `- High (P2) Priority: **${report.executiveSummary.highRisks}**\n`;
    md += `- Medium (P3) Priority: **${report.executiveSummary.mediumRisks}**\n`;
    md += `- Low (P4) Priority: **${report.executiveSummary.lowRisks}**\n\n`;
    md += `## 2. Actionable Remediation Roadmap\n\n`;

    report.actionableRemediationRoadmap.forEach(item => {
      md += `### ${item.rank}. [${item.priority}] ${item.vulnerability} (${item.cwe})\n`;
      md += `- **SLA Window:** ${item.sla}\n`;
      md += `- **CVSS v3.1 Base Score:** ${item.cvssScore}\n`;
      md += `- **Remediation Plan:** ${item.remediationSummary}\n\n`;
      if (item.codeDiff) {
        md += `\`\`\`javascript\n${item.codeDiff.vulnerable}\n\n${item.codeDiff.secure}\n\`\`\`\n\n`;
      }
    });

    md += `## 3. Project Team Contributions\n\n`;
    md += `- ${report.moduleAttributions.module1_Scanning}\n`;
    md += `- ${report.moduleAttributions.module2_AIClassification}\n`;
    md += `- ${report.moduleAttributions.module3_RiskReporting}\n`;

    return md;
  }
}

module.exports = new SecurityReportGenerator();
