//--    SIDE NAVIGATION   --
function toggleSidebar() {
  const sidebar = document.querySelector('.sidebar');
  sidebar.classList.toggle('collapsed');

  // Store state in localStorage
  const isCollapsed = sidebar.classList.contains('collapsed');
  localStorage.setItem('sidebarCollapsed', isCollapsed);
}
//Toogle edit issue button
function toggleEdit(eventId) {
  document.getElementById(`tag-container-${eventId}`).classList.add('d-none');
  document.getElementById(`tag-edit-${eventId}`).classList.remove('d-none');

  const input = document.getElementById(`input-tag-${eventId}`);
  const currentText = document.getElementById(`tag-display-${eventId}`).textContent;
  input.value = currentText === 'Inget angivet' ? '' : currentText;
}

function cancelEdit(eventId) {
  document.getElementById(`tag-container-${eventId}`).classList.remove('d-none');
  document.getElementById(`tag-edit-${eventId}`).classList.add('d-none');
}

function saveTag(eventId) {
  const input = document.getElementById(`input-tag-${eventId}`);
  const newTag = input.value.trim();

  if (newTag === '' || newTag === 'None given') {
    return cancelEdit(eventId);
  }

  fetch('/logs/update-tag', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ eventId, tag: newTag })
  }).then(() => {
    const tagDisplay = document.getElementById(`tag-display-${eventId}`);
    const tagContainer = document.getElementById(`tag-container-${eventId}`);
    const editContainer = document.getElementById(`tag-edit-${eventId}`);
    const removeBtn = document.getElementById(`remove-btn-${eventId}`);

    tagDisplay.textContent = newTag || 'None given';

    if (newTag) {
      if (removeBtn) {
        removeBtn.classList.remove('d-none');
      } else {
        const btn = document.createElement('button');
        btn.className = 'btn btn-sm btn-outline-danger';
        btn.textContent = 'Remove';
        btn.id = `remove-btn-${eventId}`;
        btn.onclick = () => removeTag(eventId);
        tagDisplay.parentNode.appendChild(btn);
      }
    } else {
      if (removeBtn) {
        removeBtn.classList.add('d-none');
      }
    }

    tagContainer.classList.remove('d-none');
    editContainer.classList.add('d-none');
  });
}

function removeTag(eventId) {
  fetch('/logs/update-tag', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ eventId, tag: null })
  }).then(() => {
    const tagDisplay = document.getElementById(`tag-display-${eventId}`);
    const removeBtn = document.getElementById(`remove-btn-${eventId}`);

    tagDisplay.textContent = 'None given';
    if (removeBtn) {
      removeBtn.classList.add('d-none');
    }
  });
}

function updateFalsePositiveStatus(selectElement) {
  const eventId = selectElement.getAttribute("data-event-id");
  const rawValue = selectElement.value;

  let valueToSend = rawValue === "null" ? null : rawValue === "true";

  fetch('/logs/update-confirmation', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ eventId: parseInt(eventId), confirmed: valueToSend })
  }).then(res => {
    if (!res.ok) {
      alert("Failed to update alarm classification.");
    }
  });
}
