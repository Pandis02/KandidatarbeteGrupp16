//--    SIDE NAVIGATION   --
function toggleSidebar() {
  const sidebar = document.querySelector('.sidebar');
  sidebar.classList.toggle('collapsed');
  
  // Store state in localStorage
  const isCollapsed = sidebar.classList.contains('collapsed');
  localStorage.setItem('sidebarCollapsed', isCollapsed);
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
