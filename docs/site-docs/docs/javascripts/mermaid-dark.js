// Mermaid dark-mode theme configuration for MkDocs Material
// This ensures diagrams are readable in both light and dark modes
window.addEventListener('DOMContentLoaded', function () {
  const observer = new MutationObserver(function () {
    const scheme = document.body.getAttribute('data-md-color-scheme');
    if (typeof mermaid !== 'undefined') {
      mermaid.initialize({
        theme: scheme === 'slate' ? 'dark' : 'default',
        themeVariables: scheme === 'slate' ? {
          darkMode: true,
          background: '#1e1e1e',
          primaryColor: '#00897b',
          primaryTextColor: '#e0e0e0',
          lineColor: '#90a4ae',
          secondaryColor: '#263238',
          tertiaryColor: '#37474f'
        } : {
          primaryColor: '#00897b',
          primaryTextColor: '#263238',
          lineColor: '#546e7a'
        }
      });
    }
  });
  observer.observe(document.body, { attributes: true, attributeFilter: ['data-md-color-scheme'] });
});
