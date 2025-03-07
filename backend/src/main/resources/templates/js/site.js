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