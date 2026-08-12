/**
 * DARK MODE TOGGLE SCRIPT
 * Handles theme switching and persistence
 */

(function() {
    'use strict';

    // Theme management
    const THEME_KEY = 'inventory-theme';
    const THEME_DARK = 'dark';
    const THEME_LIGHT = 'light';

    /**
     * Get current theme from localStorage or system preference
     */
    function getCurrentTheme() {
        const savedTheme = localStorage.getItem(THEME_KEY);
        if (savedTheme) {
            return savedTheme;
        }
        
        // Check system preference
        if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
            return THEME_DARK;
        }
        
        return THEME_LIGHT;
    }

    /**
     * Apply theme to document
     */
    function applyTheme(theme) {
        if (theme === THEME_DARK) {
            document.documentElement.setAttribute('data-theme', THEME_DARK);
        } else {
            document.documentElement.removeAttribute('data-theme');
        }
        
        // Update toggle button icon
        updateToggleIcon(theme);
        
        // Save to localStorage
        localStorage.setItem(THEME_KEY, theme);
        
        // Dispatch custom event for other scripts
        window.dispatchEvent(new CustomEvent('themeChanged', { detail: { theme } }));
    }

    /**
     * Update toggle button icon
     */
    function updateToggleIcon(theme) {
        const toggleBtn = document.getElementById('darkModeToggle');
        const toggleIcon = document.getElementById('darkModeIcon');
        
        if (!toggleBtn || !toggleIcon) return;
        
        if (theme === THEME_DARK) {
            // Show sun icon (switch to light mode)
            toggleIcon.className = 'fas fa-sun';
            toggleBtn.setAttribute('aria-label', 'Switch to light mode');
            toggleBtn.title = 'Switch to light mode';
        } else {
            // Show moon icon (switch to dark mode)
            toggleIcon.className = 'fas fa-moon';
            toggleBtn.setAttribute('aria-label', 'Switch to dark mode');
            toggleBtn.title = 'Switch to dark mode';
        }
    }

    /**
     * Toggle theme
     */
    function toggleTheme() {
        const currentTheme = getCurrentTheme();
        const newTheme = currentTheme === THEME_DARK ? THEME_LIGHT : THEME_DARK;
        applyTheme(newTheme);
        
        // Add animation class
        document.body.classList.add('theme-transition');
        setTimeout(() => {
            document.body.classList.remove('theme-transition');
        }, 300);
    }

    /**
     * Initialize dark mode
     */
    function initDarkMode() {
        // Apply saved/system theme immediately
        const currentTheme = getCurrentTheme();
        applyTheme(currentTheme);

        // Setup toggle button listener
        const toggleBtn = document.getElementById('darkModeToggle');
        if (toggleBtn) {
            toggleBtn.addEventListener('click', toggleTheme);
        }

        // Listen for system theme changes
        if (window.matchMedia) {
            window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
                // Only apply if user hasn't manually set a preference
                if (!localStorage.getItem(THEME_KEY)) {
                    applyTheme(e.matches ? THEME_DARK : THEME_LIGHT);
                }
            });
        }

        // Keyboard shortcut: Ctrl+Shift+D
        document.addEventListener('keydown', (e) => {
            if (e.ctrlKey && e.shiftKey && e.key === 'D') {
                e.preventDefault();
                toggleTheme();
            }
        });
    }

    // Initialize on DOM ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initDarkMode);
    } else {
        initDarkMode();
    }

    // Export to window for debugging
    window.darkMode = {
        toggle: toggleTheme,
        getCurrentTheme: getCurrentTheme,
        setTheme: applyTheme
    };

})();
