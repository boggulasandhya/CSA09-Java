// modules/risk/cvssCalculator.js
// Student 3: Risk Prioritization and Security Reporting Module - CVSS v3.1 Calculator

class CVSSCalculator {
  /**
   * Computes CVSS v3.1 base score from metric vector parameters
   * @param {Object} metrics
   * @returns {Object} { baseScore, severityRating, vectorString }
   */
  calculateCVSS(metrics = {}) {
    const AV_VALUES = { N: 0.85, A: 0.62, L: 0.55, P: 0.2 };
    const AC_VALUES = { L: 0.77, H: 0.44 };
    const PR_VALUES_UNCHANGED = { N: 0.85, L: 0.62, H: 0.27 };
    const PR_VALUES_CHANGED = { N: 0.85, L: 0.68, H: 0.5 };
    const UI_VALUES = { N: 0.85, R: 0.62 };
    const CIA_VALUES = { N: 0.0, L: 0.22, H: 0.56 };

    const av = metrics.AV || 'N';
    const ac = metrics.AC || 'L';
    const pr = metrics.PR || 'N';
    const ui = metrics.UI || 'N';
    const s = metrics.S || 'U';
    const c = metrics.C || 'H';
    const i = metrics.I || 'H';
    const a = metrics.A || 'H';

    const avWeight = AV_VALUES[av] || 0.85;
    const acWeight = AC_VALUES[ac] || 0.77;
    const prWeight = (s === 'U' ? PR_VALUES_UNCHANGED[pr] : PR_VALUES_CHANGED[pr]) || 0.85;
    const uiWeight = UI_VALUES[ui] || 0.85;

    const cWeight = CIA_VALUES[c] || 0.56;
    const iWeight = CIA_VALUES[i] || 0.56;
    const aWeight = CIA_VALUES[a] || 0.56;

    // Exploitability sub-score
    const exploitability = 8.22 * avWeight * acWeight * prWeight * uiWeight;

    // Impact sub-score (ISS)
    const iss = 1.0 - (1.0 - cWeight) * (1.0 - iWeight) * (1.0 - aWeight);
    let impact;
    if (s === 'U') {
      impact = 6.42 * iss;
    } else {
      impact = 7.52 * (iss - 0.029) - 3.25 * Math.pow(iss - 0.02, 15);
    }

    let baseScore = 0.0;
    if (impact > 0) {
      if (s === 'U') {
        baseScore = Math.min(10.0, Math.ceil((impact + exploitability) * 10) / 10);
      } else {
        baseScore = Math.min(10.0, Math.ceil((1.08 * (impact + exploitability)) * 10) / 10);
      }
    }

    // Determine severity rating
    let severityRating = 'NONE';
    if (baseScore >= 9.0) severityRating = 'CRITICAL';
    else if (baseScore >= 7.0) severityRating = 'HIGH';
    else if (baseScore >= 4.0) severityRating = 'MEDIUM';
    else if (baseScore >= 0.1) severityRating = 'LOW';

    const vectorString = `CVSS:3.1/AV:${av}/AC:${ac}/PR:${pr}/UI:${ui}/S:${s}/C:${c}/I:${i}/A:${a}`;

    return {
      baseScore: +baseScore.toFixed(1),
      severityRating,
      exploitabilityScore: +exploitability.toFixed(2),
      impactScore: +impact.toFixed(2),
      vectorString
    };
  }

  /**
   * Helper mapping standard CWEs to baseline CVSS vectors
   */
  getDefaultVectorForCwe(cweId) {
    switch (cweId) {
      case 'CWE-89': // SQLi
        return { AV: 'N', AC: 'L', PR: 'N', UI: 'N', S: 'U', C: 'H', I: 'H', A: 'H' }; // 9.8 Critical
      case 'CWE-79': // XSS
        return { AV: 'N', AC: 'L', PR: 'N', UI: 'R', S: 'C', C: 'L', I: 'L', A: 'N' }; // 6.1 Medium / High
      case 'CWE-639': // IDOR
        return { AV: 'N', AC: 'L', PR: 'L', UI: 'N', S: 'U', C: 'H', I: 'H', A: 'N' }; // 8.1 High
      case 'CWE-918': // SSRF
        return { AV: 'N', AC: 'L', PR: 'N', UI: 'N', S: 'C', C: 'H', I: 'L', A: 'N' }; // 9.3 Critical
      case 'CWE-22': // Path Traversal
        return { AV: 'N', AC: 'L', PR: 'N', UI: 'N', S: 'U', C: 'H', I: 'N', A: 'N' }; // 7.5 High
      case 'CWE-693': // Missing Security Headers
        return { AV: 'N', AC: 'L', PR: 'N', UI: 'N', S: 'U', C: 'N', I: 'L', A: 'N' }; // 5.3 Medium
      case 'CWE-200': // Info Disclosure
        return { AV: 'N', AC: 'L', PR: 'N', UI: 'N', S: 'U', C: 'H', I: 'N', A: 'N' }; // 7.5 High
      default:
        return { AV: 'N', AC: 'L', PR: 'N', UI: 'N', S: 'U', C: 'L', I: 'L', A: 'L' };
    }
  }
}

module.exports = new CVSSCalculator();
