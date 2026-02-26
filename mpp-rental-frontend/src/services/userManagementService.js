import api from './api';

/**
 * User Management Service
 * Handles all API calls for MPP User Management module
 */
const userManagementService = {
    /**
     * Get all users with their businesses
     * @returns {Promise} List of all users
     */
    getAllUsers: async () => {
        try {
            console.log('Calling API: /users/mpp/all');
            const response = await api.get('/users/mpp/all');
            console.log('API Response:', response.data);
            return response.data;
        } catch (error) {
            console.error('Error fetching all users:', error);
            console.error('Error response:', error.response?.data);
            console.error('Error status:', error.response?.status);
            throw error;
        }
    },

    /**
     * Search and filter users
     * @param {Object} filters - Search and filter parameters
     * @param {string} filters.searchQuery - Search by name, email, or phone
     * @param {string} filters.category - Filter by category (MPP, STUDENT, NON_STUDENT)
     * @param {string} filters.status - Filter by status (ACTIVE, BLOCKED, PENDING)
     * @returns {Promise} Filtered list of users
     */
    searchUsers: async (filters = {}) => {
        try {
            const params = new URLSearchParams();

            if (filters.searchQuery) {
                params.append('searchQuery', filters.searchQuery);
            }
            if (filters.category && filters.category !== 'all') {
                params.append('category', filters.category);
            }
            if (filters.status && filters.status !== 'all') {
                params.append('status', filters.status);
            }

            const queryString = params.toString();
            const url = queryString ? `/users/mpp/search?${queryString}` : '/users/mpp/search';

            console.log('Calling API:', url);
            const response = await api.get(url);
            console.log('API Response:', response.data);
            return response.data;
        } catch (error) {
            console.error('Error searching users:', error);
            console.error('Error response:', error.response?.data);
            console.error('Error status:', error.response?.status);
            throw error;
        }
    },

    /**
     * Get complete user details including password
     * @param {number} userId - User ID
     * @returns {Promise} Complete user details
     */
    getUserDetails: async (userId) => {
        try {
            const response = await api.get(`/users/mpp/details/${userId}`);
            return response.data;
        } catch (error) {
            console.error('Error fetching user details:', error);
            throw error;
        }
    },

    /**
     * Update user by MPP
     * @param {number} userId - User ID
     * @param {Object} userData - Updated user data
     * @returns {Promise} Updated user details
     */
    updateUser: async (userId, userData) => {
        try {
            const response = await api.put(`/users/mpp/update/${userId}`, userData);
            return response.data;
        } catch (error) {
            console.error('Error updating user:', error);
            throw error;
        }
    },

    /**
     * Toggle user status (Block/Activate)
     * @param {number} userId - User ID
     * @returns {Promise} Updated user details
     */
    toggleUserStatus: async (userId) => {
        try {
            const response = await api.put(`/users/mpp/toggle-status/${userId}`);
            return response.data;
        } catch (error) {
            console.error('Error toggling user status:', error);
            throw error;
        }
    },
};

export default userManagementService;