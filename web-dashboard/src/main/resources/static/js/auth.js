// JWT Authentication Helper - Funcții globale
function isLoggedIn() {
    return sessionStorage.getItem('token') !== null;
}

function getToken() {
    return sessionStorage.getItem('token');
}

function getUser() {
    return {
        username: sessionStorage.getItem('username'),
        email: sessionStorage.getItem('email'),
        role: sessionStorage.getItem('role'),
        mustChangePassword: sessionStorage.getItem('mustChangePassword') === 'true'
    };
}

function hasRole(role) {
    return sessionStorage.getItem('role') === role;
}

// Obiect Auth pentru compatibilitate
const Auth = {
    // Check if user is logged in
    isLoggedIn: isLoggedIn,
    
    // Get JWT token
    getToken: getToken,
    
    // Get current user info
    getUser: getUser,
    
    // Check if user has specific role
    hasRole: hasRole,
    
    // Logout
    logout: function() {
        sessionStorage.clear();
        window.location.href = '/login';
    },
    
    // Redirect to login if not authenticated
    requireAuth: function() {
        if (!this.isLoggedIn()) {
            window.location.href = '/login';
            return false;
        }
        
        // Check if password change is required
        if (this.getUser().mustChangePassword && window.location.pathname !== '/profile') {
            window.location.href = '/profile';
            return false;
        }
        
        return true;
    },
    
    // Make authenticated API call
    fetchWithAuth: async function(url, options = {}) {
        const token = this.getToken();
        
        if (!token) {
            this.logout();
            throw new Error('No token found');
        }
        
        // Add Authorization header
        options.headers = {
            ...options.headers,
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        };
        
        try {
            const response = await fetch(url, options);
            
            // If 401 Unauthorized, token expired
            if (response.status === 401) {
                if (typeof showToast === 'function') {
                    showToast('Session expired. Please login again.', 'warning', 2500);
                    setTimeout(() => this.logout(), 1800);
                } else {
                    this.logout();
                }
                throw new Error('Unauthorized');
            }
            
            return response;
        } catch (error) {
            if (error.message === 'Unauthorized') {
                throw error;
            }
            console.error('API call failed:', error);
            throw error;
        }
    }
};

// Check authentication on page load (except login page)
if (window.location.pathname !== '/login') {
    Auth.requireAuth();
}

// Update role-based navbar visibility and expose shared logout modal helpers
if (Auth.isLoggedIn() && window.location.pathname !== '/login') {
    document.addEventListener('DOMContentLoaded', function() {
        const user = Auth.getUser();

        // Admin-only items stay hidden unless the user is ADMIN.
        document.querySelectorAll('.admin-only').forEach(el => {
            el.style.display = user.role === 'ADMIN' ? '' : 'none';
        });
        // Manager-admin items visible to MANAGER and ADMIN.
        document.querySelectorAll('.manager-admin-only').forEach(el => {
            el.style.display = (user.role === 'ADMIN' || user.role === 'MANAGER') ? '' : 'none';
        });
    });
}

function showLogoutModal() {
    const modalEl = document.getElementById('logoutModal');
    if (modalEl && typeof bootstrap !== 'undefined') {
        new bootstrap.Modal(modalEl).show();
    } else {
        Auth.logout();
    }
}

function confirmLogout() {
    Auth.logout();
}
