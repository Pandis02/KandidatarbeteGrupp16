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











// Check saved state on page load
document.addEventListener('DOMContentLoaded', () => {
  // SIDEBAR
  const isCollapsed = localStorage.getItem('sidebarCollapsed') === 'true';
  if (isCollapsed) {
      document.querySelector('.sidebar').classList.add('collapsed');
  }

  // THEME
  // Load theme from localStorage or default to 'light' if not set.
  const savedTheme = localStorage.getItem('theme') || 'light';
  const ThemeSelector = document.getElementById('themeSelector');
  ThemeSelector.value = savedTheme;
  ThemeSelector.addEventListener('change', function() {
    applyTheme(this.value);
  });
  applyTheme(savedTheme);

  // SEARCH FUNCTIOn
  const searchInput = document.getElementById('search');
  searchInput.addEventListener('input', function(e) {
      const searchTerm = e.target.value.toLowerCase();
      
      // Select all searchable cards
      const cards = document.querySelectorAll('#alertEvent, #offlineEvent');
      const rows = document.querySelectorAll('#tableData');
      
      cards.forEach(card => {
          // Get all text content from the card
          const cardText = Array.from(card.querySelectorAll('span, .badge, .detail-item'))
              .map(element => element.textContent.toLowerCase())
              .join(' ');
          
          // Toggle visibility based on search match
          if (cardText.includes(searchTerm)) {
              card.closest('.row').style.display = ''; // Show matching cards
          } else {
              card.closest('.row').style.display = 'none'; // Hide non-matching
          }
      });

      rows.forEach(row => {
        const cells = Array.from(row.cells);
        const rowText = cells.map(cell => cell.textContent.toLowerCase()).join(' ');
        if(rowText.includes(searchTerm)){
          row.style.display = '';
        }
        else{
          row.style.display = 'none';
        }
    });
  });
});

// ADD EMAIL
document.getElementById('addEmailBtn').addEventListener('click', () => {
  const newEmailItem = document.createElement('div');
  newEmailItem.className = 'email-list-item';
  newEmailItem.innerHTML = `
      <div class="d-flex gap-2 mb-2">
        <input type="email" class="form-control" placeholder="Enter email">
        <button class="btn btn-danger btn-sm"><i class="bi bi-trash"></i></button>
      </div>
  `;
  document.getElementById('emailList').prepend(newEmailItem);
});

// REMOVE EMAIl
document.getElementById('emailList').addEventListener('click', (e) => {
    if(e.target.closest('button')) {
        e.target.closest('.email-list-item').remove();
    }
});

//--    CHANGE THEME    --
function applyTheme(theme) {
  const html = document.documentElement;
  const themeButtons = document.querySelectorAll('.themeButton');
  html.setAttribute('data-theme', theme);
  localStorage.setItem('theme', theme);

  themeButtons.forEach(button => {
    button.classList.toggle('btn-outline-light', theme === 'dark');
    button.classList.toggle('btn-outline-dark', theme !== 'dark');
  });
}

// SEARCH FUNCTION
