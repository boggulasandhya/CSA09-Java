// modules/risk/riskPrioritization.js
// Student 3: Risk Prioritization and Security Reporting Module - Prioritization Matrix

const cvssCalculator = require('./cvssCalculator');

class RiskPrioritizationEngine {
  /**
   * Evaluates and prioritizes a list of findings with asset criticality
   * @param {Array} rawFindings
   * @param {Object} aiClassificationsMap
   * @param {string} assetEnvironment (PRODUCTION_TIER1, PRODUCTION_TIER2, STAGING, DEV)
   * @returns {Object} prioritizationResult
   */
  prioritizeFindings(rawFindings = [], aiClassificationsMap = {}, assetEnvironment = 'PRODUCTION_TIER1') {
    const ASSET_CRITICALITY_MULTIPLIERS = {
      'PRODUCTION_TIER1': { factor: 1.5, label: 'Tier-1 Mission Critical Core (Financial/Auth/EHR)' },
      'PRODUCTION_TIER2': { factor: 1.2, label: 'Tier-2 Production Customer Facing Portal' },
      'STAGING': { factor: 0.9, label: 'Pre-Production Staging Sandbox' },
      'DEV': { factor: 0.7, label: 'Isolated Internal Development Cluster' }
    };

    const assetConfig = ASSET_CRITICALITY_MULTIPLIERS[assetEnvironment] || ASSET_CRITICALITY_MULTIPLIERS['PRODUCTION_TIER1'];

    const prioritizedList = rawFindings.map(finding => {
      const cweId = finding.cwe || 'CWE-89';
      const vector = cvssCalculator.getDefaultVectorForCwe(cweId);
      const cvss = cvssCalculator.calculateCVSS(vector);

      // Threat intelligence exploitability factor
      let exploitMultiplier = 1.0;
      if (cweId === 'CWE-89' || cweId === 'CWE-79' || cweId === 'CWE-918') {
        exploitMultiplier = 1.25; // Known active exploits in the wild
      } else if (cweId === 'CWE-639' || cweId === 'CWE-22') {
        exploitMultiplier = 1.15;
      }

      // Calculate Composite Dynamic Risk Score (Scale: 0 to 100)
      const rawComposite = cvss.baseScore * 10 * assetConfig.factor * exploitMultiplier;
      const compositeRiskScore = +Math.min(100.0, Math.max(5.0, rawComposite)).toFixed(1);

      // Determine Actionable SLA Priority Tier
      let priorityTier = 'P4 - LOW';
      let slaWindow = '30 Days';
      let badgeColor = 'emerald';

      if (compositeRiskScore >= 85.0) {
        priorityTier = 'P1 - EMERGENCY IMMEDIATE FIX';
        slaWindow = 'Within 24 Hours';
        badgeColor = 'rose';
      } else if (compositeRiskScore >= 65.0) {
        priorityTier = 'P2 - HIGH PRIORITY SPRINT';
        slaWindow = 'Within 72 Hours';
        badgeColor = 'orange';
      } else if (compositeRiskScore >= 40.0) {
        priorityTier = 'P3 - MEDIUM MAINTENANCE';
        slaWindow = 'Within 14 Days';
        badgeColor = 'amber';
      }

      return {
        findingId: finding.id,
        name: finding.name,
        type: finding.type,
        cwe: cweId,
        endpoint: finding.endpoint,
        cvssBaseScore: cvss.baseScore,
        cvssSeverity: cvss.severityRating,
        cvssVector: cvss.vectorString,
        assetEnvironment,
        assetWeight: assetConfig.factor,
        exploitMultiplier,
        compositeRiskScore,
        priorityTier,
        slaWindow,
        badgeColor,
        aiEnrichment: aiClassificationsMap[finding.id] || null,
        remediationSummary: this._getRemediationSummary(cweId)
      };
    });

    // Sort strictly in descending order of Composite Risk Score
    prioritizedList.sort((a, b) => b.compositeRiskScore - a.compositeRiskScore);

    // Compute distribution counts
    const distribution = {
      P1_CRITICAL: prioritizedList.filter(f => f.priorityTier.startsWith('P1')).length,
      P2_HIGH: prioritizedList.filter(f => f.priorityTier.startsWith('P2')).length,
      P3_MEDIUM: prioritizedList.filter(f => f.priorityTier.startsWith('P3')).length,
      P4_LOW: prioritizedList.filter(f => f.priorityTier.startsWith('P4')).length
    };

    const overallSystemPosture = distribution.P1_CRITICAL > 0 ? 'CRITICAL RISK DEFICIT' :
                                 distribution.P2_HIGH > 0 ? 'ELEVATED EXPOSURE' : 'MODERATE POSTURE';

    return {
      evaluatedFindingsCount: prioritizedList.length,
      assetEnvironment,
      assetCriticalityLabel: assetConfig.label,
      overallSystemPosture,
      priorityDistribution: distribution,
      rankedFindings: prioritizedList,
      prioritizedBy: "Student 3: Risk Prioritization and Security Reporting Module"
    };
  }

  _getRemediationSummary(cweId) {
    const plans = {
      'CWE-89': 'Migrate dynamic queries to parameterized prepared statements. Apply database user least privilege.',
      'CWE-79': 'Implement contextual HTML entity output encoding and deploy strict Content-Security-Policy (CSP) headers.',
      'CWE-639': 'Enforce session-bound tenant authorization checks at controller layer. Discontinue sequential integer IDs.',
      'CWE-918': 'Restrict outbound HTTP client to strict internal IP/domain allow-lists. Block cloud metadata address 169.254.169.254.',
      'CWE-22': 'Sanitize file path parameters using basename validation and whitelist allowable directory roots.',
      'CWE-693': 'Inject HSTS, CSP, X-Frame-Options, and X-Content-Type-Options into reverse proxy (Nginx/Cloudflare) headers.',
      'CWE-200': 'Disable debug trace dumps in production. Move server-status endpoints behind internal VPN/auth barriers.'
    };
    return plans[cweId] || 'Review system inputs, sanitize data sources, and enforce defense-in-depth controls.';
  }
}

module.exports = new RiskPrioritizationEngine();
