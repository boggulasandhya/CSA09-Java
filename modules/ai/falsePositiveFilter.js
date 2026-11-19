// modules/ai/falsePositiveFilter.js
// Student 2: AI-Based Vulnerability Analysis and Classification Module - False Positive Filter

class FalsePositiveFilter {
  /**
   * Evaluates context and evidence to determine false positive likelihood
   * @param {Object} rawFinding
   * @param {Object} aiClassification
   * @returns {Object} filterResult
   */
  evaluateFalsePositive(rawFinding, aiClassification) {
    let fpScore = 0.04; // Baseline 4%
    const reasoning = [];

    // Factor 1: Evidence strength
    const evidence = (rawFinding.evidence || "").toLowerCase();
    if (evidence.includes("syntax error") || evidence.includes("root:") || evidence.includes("db_password") || evidence.includes("unrestricted")) {
      fpScore -= 0.02;
      reasoning.push("Direct execution artifacts / leak signatures detected in HTTP response body (-2% FP).");
    } else if (evidence.includes("403 forbidden") || evidence.includes("blocked by waf")) {
      fpScore += 0.45;
      reasoning.push("Payload blocked at perimeter by Web Application Firewall (WAF) (+45% FP).");
    }

    // Factor 2: HTTP Status Code Correlation
    if (rawFinding.endpoint && rawFinding.endpoint.includes(".env")) {
      if (evidence.includes("db_password")) {
        fpScore = 0.01;
        reasoning.push("Confirmed plaintext credential leak from accessible environment file (99% True Positive).");
      }
    }

    // Factor 3: Sanitization & Context Reflection
    if (rawFinding.type === "Cross-Site Scripting") {
      if (evidence.includes("text/html") && !evidence.includes("&lt;script&gt;")) {
        fpScore = 0.03;
        reasoning.push("Raw script tags reflected in text/html context without HTML entity encoding (97% True Positive).");
      } else if (evidence.includes("&lt;")) {
        fpScore = 0.88;
        reasoning.push("Payload was correctly HTML entity-encoded by templating engine (88% False Positive).");
      }
    }

    // Clamp between 0.01 and 0.99
    fpScore = Math.max(0.01, Math.min(0.99, fpScore));
    const truePositiveProb = +(1.0 - fpScore).toFixed(3);
    const falsePositiveProb = +fpScore.toFixed(3);

    let status = "CONFIRMED_TRUE_POSITIVE";
    if (falsePositiveProb > 0.50) {
      status = "FILTERED_FALSE_POSITIVE";
    } else if (falsePositiveProb > 0.15) {
      status = "NEEDS_MANUAL_TRIAGE";
    }

    return {
      findingId: rawFinding.id,
      verdict: status,
      truePositiveProbability: +(truePositiveProb * 100).toFixed(1) + "%",
      falsePositiveProbability: +(falsePositiveProb * 100).toFixed(1) + "%",
      isFilteredOut: status === "FILTERED_FALSE_POSITIVE",
      reasoningBreadcrumbs: reasoning,
      reviewedByAI: true,
      evaluatedBy: "Student 2: AI-Based Vulnerability Analysis and Classification Module"
    };
  }
}

module.exports = new FalsePositiveFilter();
