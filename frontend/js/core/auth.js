// Authentication utilities for all pages

// LINH
/**
 * Check if user is authenticated
 * If not, redirect to home page
 * @param {string} requiredRole - Optional: 'organizer' or 'user' to check specific role
 */
function checkAuth(requiredRole = null) {
    const currentUser = localStorage.getItem('currentUser');
    
    if (!currentUser) {
        // User not logged in - redirect to home page
        redirectToHome();
        return null;
    }
    
    try {
        const userData = JSON.parse(currentUser);
        
        // Check if specific role is required
        if (requiredRole && userData.role !== requiredRole) {
            // User doesn't have required role - redirect to their appropriate dashboard
            redirectToDashboard(userData.role);
            return null;
        }
        
        return userData;
    } catch (error) {
        console.error('Error parsing user data:', error);
        // Invalid user data - clear and redirect
        localStorage.removeItem('currentUser');
        redirectToHome();
        return null;
    }
}

/**
 * Redirect to home page (index.html at root)
 */
function redirectToHome() {
    // Get current path
    const currentPath = window.location.pathname;
    
    // Calculate path to root index.html
    if (currentPath.includes('/pages/admin/')) {
        window.location.href = '../../index.html';
    } else if (currentPath.includes('/pages/user/')) {
        window.location.href = '../../index.html';
    } else if (currentPath.includes('/pages/')) {
        window.location.href = '../index.html';
    } else if (currentPath.includes('/frontend/')) {
        // Already in frontend folder
        const pathParts = currentPath.split('/');
        const frontendIndex = pathParts.indexOf('frontend');
        const basePath = pathParts.slice(0, frontendIndex + 1).join('/');
        window.location.href = basePath + '/index.html';
    } else {
        // Fallback
        window.location.href = '/frontend/index.html';
    }
}

/**
 * Redirect to appropriate dashboard based on role
 * @param {string} role - 'organizer' or 'user'
 */
function redirectToDashboard(role) {
    if (role === 'organizer') {
        // Check current location to calculate path
        const currentPath = window.location.pathname;
        if (currentPath.includes('/pages/user/')) {
            window.location.href = '../admin/index.html';
        } else if (currentPath.includes('/frontend/')) {
            window.location.href = 'pages/admin/index.html';
        } else {
            window.location.href = 'admin/index.html';
        }
    } else {
        // User role
        const currentPath = window.location.pathname;
        if (currentPath.includes('/pages/admin/')) {
            window.location.href = '../user/index.html';
        } else if (currentPath.includes('/frontend/')) {
            window.location.href = 'pages/user/index.html';
        } else {
            window.location.href = 'user/index.html';
        }
    }
}

/**
 * Get current user data
 * @returns {Object|null} User data or null if not logged in
 */
function getCurrentUser() {
    const currentUser = localStorage.getItem('currentUser');
    if (!currentUser) return null;
    
    try {
        return JSON.parse(currentUser);
    } catch (error) {
        console.error('Error parsing user data:', error);
        return null;
    }
}

/**
 * Logout user
 */
function logout() {
    localStorage.removeItem('currentUser');
    redirectToHome();
}

/**
 * Backward-compatible logout handler used by inline HTML onclick.
 */
function handleLogout() {
    logout();
}

/**
 * Check if user has specific role
 * @param {string} role - 'organizer' or 'user'
 * @returns {boolean}
 */
function hasRole(role) {
    const userData = getCurrentUser();
    return userData && userData.role === role;
}

/**
 * Initialize auth check on page load
 * Call this at the beginning of each protected page
 * @param {string} requiredRole - Optional: 'organizer' or 'user'
 */
function initAuth(requiredRole = null) {
    // Check auth on page load
    const userData = checkAuth(requiredRole);
    
    if (!userData) {
        // User will be redirected, stop execution
        return false;
    }
    
    console.log('User authenticated:', userData);
    return userData;
}

// Export for use in other files
if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        checkAuth,
        redirectToHome,
        redirectToDashboard,
        getCurrentUser,
        logout,
        handleLogout,
        hasRole,
        initAuth
    };
}

if (typeof window !== 'undefined') {
    window.handleLogout = handleLogout;
}
// LINH
