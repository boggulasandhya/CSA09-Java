// public/js/app.js
// Main Application State & Controller

const App = {
  state: {
    activeTab: 'dashboard',
    targets: [],
    selectedTargetId: 'target-banking',
    activeScanId: null,
    scanPollInterval: null,
    scanSession: null,
    rawFindings: [],
    aiClassifiedFindings: {},
    prioritizedResults: null,
    assetEnvironment: 'PRODUCTION_TIER1'
  },

  async init() {
    this.bindNavigation();
    initDashboardCharts();
    await this.loadTargets();
    this.bindScannerEvents();
    this.bindSastEvents();
    this.bindAiEvents();
    this.bindRiskEvents();

    // Load initial benchmark preview
    this.selectTarget(this.state.selectedTargetId);
  },

  bindNavigation() {
    document.querySelectorAll('.nav-tab-btn').forEach(btn => {
      btn.addEventListener('click', (e) => {
        const targetTab = btn.getAttribute('data-tab');
        this.switchTab(targetTab);
      });
    });
  },

  switchTab(tabId) {
    this.state.activeTab = tabId;
    document.querySelectorAll('.tab-content').forEach(el => el.classList.add('hidden'));
    document.querySelectorAll('.nav-tab-btn').forEach(b => {
      b.classList.remove('text-cyan-400', 'border-b-2', 'border-cyan-400', 'bg-gray-800/40');
      b.classList.add('text-gray-400');
    });

    const activeEl = document.getElementById(`tab-${tabId}`);
    const activeBtn = document.querySelector(`.nav-tab-btn[data-tab="${tabId}"]`);
    if (activeEl) activeEl.classList.remove('hidden');
    if (activeBtn) {
      activeBtn.classList.add('text-cyan-400', 'border-b-2', 'border-cyan-400', 'bg-gray-800/40');
      activeBtn.classList.remove('text-gray-400');
    }

    if (tabId === 'dashboard') {
      setTimeout(() => initDashboardCharts(), 50);
    }
  },

  async loadTargets() {
    try {
      const res = await fetch('/api/targets');
      this.state.targets = await res.json();
      const selectEl = document.getElementById('targetSelect');
      if (selectEl) {
        selectEl.innerHTML = this.state.targets.map(t => 
          `<option value="${t.id}">${t.name} (${t.category})</option>`
        ).join('');
        selectEl.addEventListener('change', (e) => this.selectTarget(e.target.value));
      }
    } catch (err) {
      console.error('Failed to load targets:', err);
    }
  },

  selectTarget(targetId) {
    this.state.selectedTargetId = targetId;
    const target = this.state.targets.find(t => t.id === targetId);
    if (!target) return;

    const urlInput = document.getElementById('targetUrlInput');
    if (urlInput) urlInput.value = target.url;

    const descEl = document.getElementById('targetDescription');
    if (descEl) descEl.textContent = target.description;

    // Pre-populate findings preview
    this.state.rawFindings = target.simulatedFindings;
    this.processFindingsThroughModules(this.state.rawFindings);
  },

  bindScannerEvents() {
    const startScanBtn = document.getElementById('startScanBtn');
    if (startScanBtn) {
      startScanBtn.addEventListener('click', () => this.startVulnerabilityScan());
    }
  },

  async startVulnerabilityScan() {
    const targetUrl = document.getElementById('targetUrlInput').value.trim();
    const scanDepth = document.getElementById('scanDepthSelect').value;
    const scanMode = document.getElementById('scanModeSelect').value;

    const startBtn = document.getElementById('startScanBtn');
    startBtn.disabled = true;
    startBtn.innerHTML = `<i class="fa-solid fa-spinner fa-spin mr-2"></i> Scanning In Progress...`;

    // Clear logs
    const logBox = document.getElementById('terminalLogBox');
    if (logBox) logBox.innerHTML = '';

    try {
      const res = await fetch('/api/scan/start', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          targetId: this.state.selectedTargetId,
          targetUrl,
          scanDepth,
          scanMode
        })
      });

      const data = await res.json();
      if (!data.success) throw new Error(data.error);

      this.state.activeScanId = data.scanId;
      this.pollScanProgress(data.scanId);

    } catch (err) {
      alert('Failed to initiate scan: ' + err.message);
      startBtn.disabled = false;
      startBtn.innerHTML = `<i class="fa-solid fa-bolt mr-2"></i> Launch Comprehensive Scan`;
    }
  },

  pollScanProgress(scanId) {
    if (this.state.scanPollInterval) clearInterval(this.state.scanPollInterval);

    const progressBar = document.getElementById('scanProgressBar');
    const progressPercent = document.getElementById('scanProgressPercent');
    const logBox = document.getElementById('terminalLogBox');
    const statusPill = document.getElementById('scanStatusPill');

    this.state.scanPollInterval = setInterval(async () => {
      try {
        const res = await fetch(`/api/scan/status/${scanId}`);
        const session = await res.json();
        this.state.scanSession = session;

        if (progressBar) progressBar.style.width = `${session.progress}%`;
        if (progressPercent) progressPercent.textContent = `${session.progress}%`;

        if (statusPill) {
          statusPill.textContent = session.status;
          statusPill.className = session.status === 'COMPLETED' 
            ? 'px-3 py-1 text-xs font-semibold rounded-full bg-emerald-950 text-emerald-400 border border-emerald-800'
            : 'px-3 py-1 text-xs font-semibold rounded-full bg-cyan-950 text-cyan-400 border border-cyan-800 animate-pulse';
        }

        // Render terminal logs
        if (logBox && session.logs) {
          logBox.innerHTML = session.logs.map(log => 
            `<div class="py-0.5"><span class="text-gray-500">[${log.timestamp}]</span> <span class="text-cyan-400">&gt;&gt;</span> <span class="text-gray-200">${log.message}</span></div>`
          ).join('');
          logBox.scrollTop = logBox.scrollHeight;
        }

        // Render discovered endpoints
        this.renderDiscoveredEndpoints(session.discoveredEndpoints || []);

        // When scan completes
        if (session.status === 'COMPLETED') {
          clearInterval(this.state.scanPollInterval);
          const startBtn = document.getElementById('startScanBtn');
          if (startBtn) {
            startBtn.disabled = false;
            startBtn.innerHTML = `<i class="fa-solid fa-bolt mr-2"></i> Scan Completed (Re-run)`;
          }

          this.state.rawFindings = session.rawFindings || [];
          await this.processFindingsThroughModules(this.state.rawFindings);
        }

      } catch (err) {
        console.error('Scan polling error:', err);
      }
    }, 600);
  },

  renderDiscoveredEndpoints(endpoints) {
    const container = document.getElementById('discoveredEndpointsList');
    if (!container) return;

    if (endpoints.length === 0) {
      container.innerHTML = `<p class="text-sm text-gray-500 italic">Endpoints will appear here as crawling proceeds...</p>`;
      return;
    }

    container.innerHTML = endpoints.map(ep => `
      <div class="flex items-center justify-between p-2.5 rounded bg-gray-900/60 border border-gray-800 text-xs font-mono">
        <div class="flex items-center space-x-2">
          <span class="px-2 py-0.5 rounded font-bold ${ep.method === 'POST' ? 'bg-amber-950 text-amber-400' : 'bg-blue-950 text-blue-400'}">${ep.method}</span>
          <span class="text-gray-300 font-semibold">${ep.path}</span>
        </div>
        <span class="text-gray-500">Params: [${(ep.params || []).join(', ') || 'none'}]</span>
      </div>
    `).join('');
  },

  async processFindingsThroughModules(rawFindings) {
    // 1. Render Raw Findings in Module 1 table
    this.renderRawFindingsTable(rawFindings);

    // 2. Pass findings to Module 2 (AI Classification & False Positive Filter)
    const classificationsMap = {};
    for (const finding of rawFindings) {
      try {
        const res = await fetch('/api/ai/classify', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ finding })
        });
        const data = await res.json();
        if (data.success) {
          classificationsMap[finding.id] = {
            classification: data.classification,
            fpEvaluation: data.fpEvaluation
          };
        }
      } catch (e) {
        console.error('AI Classification failed for finding:', finding.id, e);
      }
    }
    this.state.aiClassifiedFindings = classificationsMap;
    this.renderAiClassificationModule(rawFindings, classificationsMap);

    // 3. Pass findings to Module 3 (Risk Prioritization & Reporting)
    await this.calculateRiskPrioritization();
  },

  renderRawFindingsTable(findings) {
    const tbody = document.getElementById('rawFindingsTableBody');
    const badgeCount = document.getElementById('findingsCountBadge');
    if (badgeCount) badgeCount.textContent = `${findings.length} Detected`;

    if (!tbody) return;
    if (findings.length === 0) {
      tbody.innerHTML = `<tr><td colspan="6" class="text-center py-6 text-gray-500 italic">No vulnerabilities identified yet.</td></tr>`;
      return;
    }

    tbody.innerHTML = findings.map(f => `
      <tr class="border-b border-gray-800/80 hover:bg-gray-800/30 transition text-sm">
        <td class="py-3 px-4 font-mono font-bold text-cyan-400">${f.id}</td>
        <td class="py-3 px-4 font-medium text-gray-200">${f.name}</td>
        <td class="py-3 px-4"><span class="px-2 py-0.5 rounded text-xs font-mono bg-purple-950/70 text-purple-300 border border-purple-800">${f.cwe}</span></td>
        <td class="py-3 px-4 font-mono text-xs text-gray-400 truncate max-w-xs">${f.endpoint || '/'}</td>
        <td class="py-3 px-4">
          <span class="px-2.5 py-1 rounded text-xs font-bold ${
            f.rawSeverity === 'CRITICAL' ? 'bg-rose-950 text-rose-400 border border-rose-800' :
            f.rawSeverity === 'HIGH' ? 'bg-orange-950 text-orange-400 border border-orange-800' :
            'bg-amber-950 text-amber-400 border border-amber-800'
          }">${f.rawSeverity}</span>
        </td>
        <td class="py-3 px-4 text-xs font-mono text-emerald-400">${((f.confidenceScore || 0.95) * 100).toFixed(0)}%</td>
      </tr>
    `).join('');
  },

  renderAiClassificationModule(rawFindings, classificationsMap) {
    const container = document.getElementById('aiClassifiedCardsContainer');
    if (!container) return;

    if (rawFindings.length === 0) {
      container.innerHTML = `<p class="text-gray-500 italic">Run a scan in Module 1 to feed detections into the AI model.</p>`;
      return;
    }

    container.innerHTML = rawFindings.map(f => {
      const enrichment = classificationsMap[f.id];
      if (!enrichment) return '';
      const { classification, fpEvaluation } = enrichment;

      return `
        <div class="glass-panel p-5 rounded-xl border border-gray-800 hover:border-indigo-500/50 transition">
          <div class="flex items-start justify-between">
            <div>
              <div class="flex items-center space-x-2">
                <span class="px-2.5 py-0.5 rounded text-xs font-bold font-mono bg-indigo-950 text-indigo-300 border border-indigo-800">${classification.cweId}</span>
                <span class="px-2.5 py-0.5 rounded text-xs font-mono bg-gray-800 text-gray-300">${classification.owaspCategory}</span>
                <span class="px-2.5 py-0.5 rounded text-xs font-bold ${
                  fpEvaluation.verdict === 'CONFIRMED_TRUE_POSITIVE' 
                    ? 'bg-emerald-950 text-emerald-400 border border-emerald-800' 
                    : 'bg-amber-950 text-amber-400 border border-amber-800'
                }"><i class="fa-solid fa-brain mr-1"></i> ${fpEvaluation.verdict}</span>
              </div>
              <h4 class="text-base font-bold text-gray-100 mt-2">${classification.title}</h4>
              <p class="text-xs text-gray-400 mt-1">${classification.cweTitle}</p>
            </div>
            <div class="text-right">
              <span class="text-xs text-gray-400">AI Model Confidence</span>
              <div class="text-xl font-bold font-mono text-cyan-400">${(classification.aiConfidenceScore * 100).toFixed(1)}%</div>
            </div>
          </div>

          <!-- Token Attention Heatmap -->
          <div class="mt-4 pt-3 border-t border-gray-800/80">
            <span class="text-xs font-semibold text-gray-400 block mb-2"><i class="fa-solid fa-fire text-amber-400 mr-1"></i> NLP Transformer Attention Weights (Key Substrings Triggering Decision):</span>
            <div class="flex flex-wrap gap-1.5">
              ${(classification.nlpTokenAttention || []).map(item => `
                <span class="token-chip px-2 py-1 rounded text-xs font-mono border ${
                  item.attentionWeight > 0.8 
                    ? 'bg-rose-950/80 text-rose-300 border-rose-800 font-bold' 
                    : item.attentionWeight > 0.4
                    ? 'bg-amber-950/60 text-amber-300 border-amber-800'
                    : 'bg-gray-800 text-gray-400 border-gray-700'
                }">${item.token} <span class="text-[10px] opacity-75">(${item.attentionWeight})</span></span>
              `).join('')}
            </div>
          </div>

          <!-- False Positive Context Verification -->
          <div class="mt-4 p-3 rounded-lg bg-gray-900/60 border border-gray-800 text-xs">
            <div class="flex items-center justify-between text-gray-300 font-semibold mb-1">
              <span>False Positive Probability: <strong class="text-rose-400">${fpEvaluation.falsePositiveProbability}</strong></span>
              <span>True Positive Confidence: <strong class="text-emerald-400">${fpEvaluation.truePositiveProbability}</strong></span>
            </div>
            <ul class="text-gray-400 list-disc list-inside space-y-0.5 mt-1.5">
              ${(fpEvaluation.reasoningBreadcrumbs || []).map(r => `<li>${r}</li>`).join('')}
            </ul>
          </div>
        </div>
      `;
    }).join('');
  },

  async calculateRiskPrioritization() {
    try {
      const res = await fetch('/api/risk/prioritize', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          rawFindings: this.state.rawFindings,
          aiClassificationsMap: this.state.aiClassifiedFindings,
          assetEnvironment: this.state.assetEnvironment
        })
      });

      const data = await res.json();
      if (!data.success) throw new Error(data.error);

      this.state.prioritizedResults = data.prioritized;
      this.renderPrioritizationQueue(data.prioritized);
      this.renderExecutiveReport(data.prioritized);

      // Update dashboard cards & charts
      this.updateDashboardMetrics(data.prioritized);
      updateChartsWithResults(data.prioritized.rankedFindings);

    } catch (err) {
      console.error('Failed to prioritize risks:', err);
    }
  },

  renderPrioritizationQueue(prioritized) {
    const container = document.getElementById('prioritizedQueueContainer');
    if (!container) return;

    const findings = prioritized.rankedFindings || [];
    if (findings.length === 0) {
      container.innerHTML = `<p class="text-gray-500 italic">No prioritized findings to display.</p>`;
      return;
    }

    container.innerHTML = findings.map((f, index) => `
      <div class="glass-panel p-5 rounded-xl border border-gray-800 hover:border-gray-700 transition">
        <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div class="flex items-start space-x-3">
            <div class="w-8 h-8 rounded-lg flex items-center justify-center font-bold text-sm bg-gray-800 text-cyan-400 border border-gray-700 shrink-0">
              #${index + 1}
            </div>
            <div>
              <div class="flex flex-wrap items-center gap-2">
                <span class="px-2.5 py-0.5 rounded text-xs font-bold ${
                  f.priorityTier.startsWith('P1') ? 'bg-rose-950 text-rose-400 border border-rose-800' :
                  f.priorityTier.startsWith('P2') ? 'bg-orange-950 text-orange-400 border border-orange-800' :
                  'bg-amber-950 text-amber-400 border border-amber-800'
                }"><i class="fa-solid fa-triangle-exclamation mr-1"></i> ${f.priorityTier}</span>

                <span class="text-xs font-mono px-2 py-0.5 rounded bg-gray-800 text-gray-300">SLA: ${f.slaWindow}</span>
                <span class="text-xs font-mono px-2 py-0.5 rounded bg-purple-950 text-purple-300">${f.cwe}</span>
              </div>
              <h3 class="text-lg font-bold text-gray-100 mt-2">${f.name}</h3>
              <p class="text-xs font-mono text-gray-400 mt-0.5 truncate">${f.endpoint}</p>
            </div>
          </div>

          <div class="flex items-center space-x-6 border-t md:border-t-0 md:border-l border-gray-800 pt-3 md:pt-0 md:pl-6 shrink-0">
            <div>
              <span class="text-xs text-gray-400 block">CVSS v3.1</span>
              <div class="text-xl font-bold font-mono text-rose-400">${f.cvssBaseScore}</div>
            </div>
            <div>
              <span class="text-xs text-gray-400 block">Asset Factor</span>
              <div class="text-xl font-bold font-mono text-indigo-400">${f.assetWeight}x</div>
            </div>
            <div>
              <span class="text-xs text-gray-400 block">Composite Risk</span>
              <div class="text-2xl font-black font-mono text-cyan-400">${f.compositeRiskScore}</div>
            </div>
          </div>
        </div>

        <!-- Remediation Guidance -->
        <div class="mt-4 pt-4 border-t border-gray-800/80 flex flex-col sm:flex-row sm:items-center justify-between text-xs text-gray-400 gap-2">
          <div><strong class="text-gray-300">Remediation Action:</strong> ${f.remediationSummary}</div>
          <button onclick="App.viewCodeDiff('${f.cwe}', '${f.name}')" class="px-3 py-1.5 rounded bg-indigo-950/80 hover:bg-indigo-900 text-indigo-300 border border-indigo-800 font-semibold transition shrink-0">
            <i class="fa-solid fa-code-compare mr-1"></i> Inspect Code Diff
          </button>
        </div>
      </div>
    `).join('');
  },

  async renderExecutiveReport(prioritized) {
    try {
      const res = await fetch('/api/reports/generate', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          scanSession: this.state.scanSession || { targetName: "ApexSecure FinTech Banking", targetUrl: "https://demo-bank.apexsecure.internal" },
          prioritizedResult: prioritized
        })
      });

      const data = await res.json();
      if (!data.success) return;
      this.state.executiveReport = data.report;

      const postureBadge = document.getElementById('reportPostureBadge');
      if (postureBadge) {
        postureBadge.textContent = data.report.executiveSummary.overallPosture;
      }
    } catch (e) {
      console.error('Report generation error:', e);
    }
  },

  updateDashboardMetrics(prioritized) {
    const findings = prioritized.rankedFindings || [];
    const p1Count = findings.filter(f => f.priorityTier.startsWith('P1')).length;
    const p2Count = findings.filter(f => f.priorityTier.startsWith('P2')).length;

    const elTotal = document.getElementById('kpiTotalVulns');
    const elP1 = document.getElementById('kpiCriticalVulns');
    const elP2 = document.getElementById('kpiHighVulns');
    const elPosture = document.getElementById('kpiPosture');

    if (elTotal) elTotal.textContent = findings.length;
    if (elP1) elP1.textContent = p1Count;
    if (elP2) elP2.textContent = p2Count;
    if (elPosture) elPosture.textContent = prioritized.overallSystemPosture;
  },

  bindSastEvents() {
    const scanCodeBtn = document.getElementById('scanCodeBtn');
    if (scanCodeBtn) {
      scanCodeBtn.addEventListener('click', async () => {
        const code = document.getElementById('sastCodeInput').value;
        const resultContainer = document.getElementById('sastResultsContainer');

        scanCodeBtn.disabled = true;
        scanCodeBtn.innerHTML = `<i class="fa-solid fa-spinner fa-spin mr-2"></i> Analyzing AST & Patterns...`;

        try {
          const res = await fetch('/api/scan/code', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ code, language: 'javascript' })
          });
          const data = await res.json();
          scanCodeBtn.disabled = false;
          scanCodeBtn.innerHTML = `<i class="fa-solid fa-magnifying-glass-code mr-2"></i> Scan Source Code`;

          if (resultContainer && data.result) {
            const { findings, totalLinesAnalyzed, findingsCount } = data.result;
            resultContainer.innerHTML = `
              <div class="mb-3 text-xs text-gray-400 flex items-center justify-between">
                <span>Analyzed ${totalLinesAnalyzed} lines of source code</span>
                <span class="font-bold text-rose-400">${findingsCount} SAST Vulnerability Sink(s) Found</span>
              </div>
              <div class="space-y-2">
                ${findings.map(f => `
                  <div class="p-3 rounded bg-gray-900 border border-gray-800 text-xs">
                    <div class="flex items-center justify-between font-bold">
                      <span class="text-rose-400">[Line ${f.lineNumber}] ${f.name}</span>
                      <span class="px-2 py-0.5 rounded bg-purple-950 text-purple-300 font-mono">${f.cwe}</span>
                    </div>
                    <p class="text-gray-400 mt-1">${f.evidence}</p>
                    <pre class="mt-2 p-2 rounded bg-black/60 text-emerald-400 font-mono text-[11px] overflow-x-auto">${f.codeSnippet}</pre>
                  </div>
                `).join('')}
              </div>
            `;
          }
        } catch (e) {
          scanCodeBtn.disabled = false;
          scanCodeBtn.innerHTML = `<i class="fa-solid fa-magnifying-glass-code mr-2"></i> Scan Source Code`;
          alert('SAST Analysis failed: ' + e.message);
        }
      });
    }

    // Snippet loader buttons
    document.querySelectorAll('.load-snippet-btn').forEach(btn => {
      btn.addEventListener('click', () => {
        const type = btn.getAttribute('data-type');
        const input = document.getElementById('sastCodeInput');
        if (!input) return;

        if (type === 'sqli') {
          input.value = `// Node.js Express Controller - Vulnerable SQL Execution\napp.post('/api/user/login', async (req, res) => {\n  const username = req.body.username;\n  const password = req.body.password;\n  // RAW STRING CONCATENATION VULNERABILITY:\n  const sql = "SELECT id, username, role FROM users WHERE user = '" + username + "' AND pass = '" + password + "';";\n  const result = await db.query(sql);\n  res.json(result);\n});`;
        } else if (type === 'xss') {
          input.value = `// Client Profile Renderer - Reflected XSS\nfunction renderUserProfile(profileData) {\n  const userBio = profileData.bio;\n  // UNSANITIZED DOM INJECTION SINK:\n  document.getElementById('bio-container').innerHTML = "<div class='bio'>" + userBio + "</div>";\n}`;
        } else if (type === 'cmdi') {
          input.value = `// System Diagnostics Service - Command Injection\nconst { exec } = require('child_process');\napp.get('/ping', (req, res) => {\n  const host = req.query.host;\n  // ARBITRARY COMMAND EXECUTION VIA UNSANITIZED VARIABLE:\n  exec("ping -c 4 " + host, (error, stdout) => {\n    res.send(stdout);\n  });\n});`;
        } else if (type === 'secret') {
          input.value = `// Cloud Storage Adapter - Hardcoded Credentials\nconst awsConfig = {\n  accessKeyId: "AKIAIOSFODNN7EXAMPLE",\n  secretAccessKey: "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY",\n  jwt_secret: "super_secret_production_signing_token_9981"\n};`;
        }
      });
    });
  },

  bindAiEvents() {
    const copilotBtn = document.getElementById('askCopilotBtn');
    const copilotInput = document.getElementById('copilotInput');
    const copilotChatBox = document.getElementById('copilotChatBox');

    if (copilotBtn && copilotInput) {
      copilotBtn.addEventListener('click', async () => {
        const query = copilotInput.value.trim();
        if (!query) return;

        // Append user question
        copilotChatBox.innerHTML += `
          <div class="flex items-start space-x-2.5 justify-end">
            <div class="p-3 rounded-xl bg-indigo-600 text-white text-xs max-w-md">
              ${query}
            </div>
            <div class="w-7 h-7 rounded-full bg-indigo-800 flex items-center justify-center text-xs font-bold text-white shrink-0">
              U
            </div>
          </div>
        `;
        copilotInput.value = '';
        copilotChatBox.scrollTop = copilotChatBox.scrollHeight;

        try {
          const res = await fetch('/api/ai/copilot', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ prompt: query })
          });
          const data = await res.json();
          if (data.success && data.answer) {
            const ans = data.answer;
            copilotChatBox.innerHTML += `
              <div class="flex items-start space-x-2.5">
                <div class="w-7 h-7 rounded-full bg-cyan-700 flex items-center justify-center text-xs font-bold text-white shrink-0">
                  <i class="fa-solid fa-robot"></i>
                </div>
                <div class="p-3.5 rounded-xl bg-gray-900 border border-gray-800 text-xs text-gray-200 max-w-lg space-y-2">
                  <div class="font-bold text-cyan-400 text-sm">${ans.topic}</div>
                  <p>${ans.summary}</p>
                  <p class="text-gray-400 italic">${ans.attackVector}</p>
                  ${ans.remediationCode ? `
                    <div class="space-y-1 mt-2">
                      <span class="text-[10px] uppercase font-bold text-rose-400 block">Vulnerable Anti-Pattern:</span>
                      <pre class="p-2 rounded bg-black/60 text-rose-300 font-mono text-[10px] overflow-x-auto">${ans.remediationCode.vulnerable}</pre>
                      <span class="text-[10px] uppercase font-bold text-emerald-400 block mt-1">Recommended Secure Fix:</span>
                      <pre class="p-2 rounded bg-black/60 text-emerald-300 font-mono text-[10px] overflow-x-auto">${ans.remediationCode.secure}</pre>
                    </div>
                  ` : ''}
                </div>
              </div>
            `;
            copilotChatBox.scrollTop = copilotChatBox.scrollHeight;
          }
        } catch (e) {
          console.error('Copilot request failed:', e);
        }
      });
    }
  },

  bindRiskEvents() {
    const envSelect = document.getElementById('assetEnvSelect');
    if (envSelect) {
      envSelect.addEventListener('change', (e) => {
        this.state.assetEnvironment = e.target.value;
        this.calculateRiskPrioritization();
      });
    }

    const exportMdBtn = document.getElementById('exportMarkdownBtn');
    if (exportMdBtn) {
      exportMdBtn.addEventListener('click', async () => {
        if (!this.state.executiveReport) return;
        try {
          const res = await fetch('/api/reports/markdown', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ report: this.state.executiveReport })
          });
          const blob = await res.blob();
          const url = window.URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = `Security_Audit_Report_${Date.now()}.md`;
          a.click();
        } catch (e) {
          alert('Markdown export failed: ' + e.message);
        }
      });
    }

    const exportJsonBtn = document.getElementById('exportJsonBtn');
    if (exportJsonBtn) {
      exportJsonBtn.addEventListener('click', () => {
        if (!this.state.prioritizedResults) return;
        const blob = new Blob([JSON.stringify(this.state.prioritizedResults, null, 2)], { type: 'application/json' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `Vulnerability_Prioritization_${Date.now()}.json`;
        a.click();
      });
    }

    const printBtn = document.getElementById('printReportBtn');
    if (printBtn) {
      printBtn.addEventListener('click', () => window.print());
    }
  },

  viewCodeDiff(cweId, title) {
    const modal = document.getElementById('codeDiffModal');
    const modalTitle = document.getElementById('diffModalTitle');
    const vulnPre = document.getElementById('diffVulnCode');
    const securePre = document.getElementById('diffSecureCode');

    if (!modal) return;
    if (modalTitle) modalTitle.textContent = `${title} (${cweId}) - Side-by-Side Remediation Diff`;

    // Fetch diff definition
    const diffs = {
      'CWE-89': {
        vulnerable: `// VULNERABLE CODE:\nconst query = "SELECT * FROM statements WHERE doc_id = '" + req.query.doc_name + "';";\nconst result = await db.rawQuery(query);`,
        secure: `// SECURE REMEDIATION:\nconst query = "SELECT * FROM statements WHERE doc_id = $1;";\nconst result = await db.query(query, [req.query.doc_name]);`
      },
      'CWE-79': {
        vulnerable: `// VULNERABLE CODE:\nelement.innerHTML = "<div class='review'>" + userComment + "</div>";`,
        secure: `// SECURE REMEDIATION:\nconst commentDiv = document.createElement('div');\ncommentDiv.className = 'review';\ncommentDiv.textContent = userComment; // Context-aware escaping\nelement.appendChild(commentDiv);`
      },
      'CWE-639': {
        vulnerable: `// VULNERABLE CODE:\nconst account = await Account.findById(req.params.id);\nreturn res.json(account);`,
        secure: `// SECURE REMEDIATION:\nconst account = await Account.findOne({\n  where: { id: req.params.id, ownerId: req.session.userId }\n});\nif (!account) return res.status(403).json({ error: 'Access Denied' });\nreturn res.json(account);`
      },
      'CWE-918': {
        vulnerable: `// VULNERABLE CODE:\nconst response = await axios.get(req.body.callback_url);`,
        secure: `// SECURE REMEDIATION:\nconst parsedUrl = new URL(req.body.callback_url);\nconst ALLOWED_DOMAINS = ['hooks.slack.com', 'api.partner.io'];\nif (!ALLOWED_DOMAINS.includes(parsedUrl.hostname)) {\n  throw new SecurityException('Outbound host forbidden');\n}\nconst response = await axios.get(parsedUrl.href);`
      }
    };

    const diff = diffs[cweId] || {
      vulnerable: `// VULNERABLE: Direct parameter flow into sensitive sink`,
      secure: `// SECURE: Enforce boundary validation and defense-in-depth sanitization`
    };

    if (vulnPre) vulnPre.textContent = diff.vulnerable;
    if (securePre) securePre.textContent = diff.secure;

    modal.classList.remove('hidden');
  },

  closeCodeDiff() {
    const modal = document.getElementById('codeDiffModal');
    if (modal) modal.classList.add('hidden');
  }
};

window.addEventListener('DOMContentLoaded', () => App.init());
