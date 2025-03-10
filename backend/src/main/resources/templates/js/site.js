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
  const isCollapsed = localStorage.getItem('sidebarCollapsed') === 'true';
  if (isCollapsed) {
      document.querySelector('.sidebar').classList.add('collapsed');
  }
});


//--    CHANGE THEME    --
const html = document.documentElement;
const ThemeSelector = document.getElementById('themeSelector');
const themeButtons = document.querySelectorAll('.themeButton');

// Load theme from localStorage or default to 'light' if not set.
const savedTheme = localStorage.getItem('theme') || 'light';
ThemeSelector.value = savedTheme;
applyTheme(savedTheme);

function applyTheme(theme) {
  html.setAttribute('data-theme', theme);
  localStorage.setItem('theme', theme);

  themeButtons.forEach(button => {
    if (theme === 'dark') {
        button.classList.remove('btn-outline-dark');
        button.classList.add('btn-outline-light');
    } else {
        button.classList.remove('btn-outline-light');
        button.classList.add('btn-outline-dark');
    }
  });
}

ThemeSelector.addEventListener('change', function() {
  applyTheme(this.value);
});