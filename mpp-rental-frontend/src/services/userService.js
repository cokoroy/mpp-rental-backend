import api from './api';

/**
 * UserService - Handles all user-related API calls
 */
const userService = {
  /**
   * Get current user's profile
   * @returns {Promise} API response
   */
  getProfile: async () => {
    try {
      const response = await api.get('/users/profile');
      return response;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Update current user's profile
   * @param {Object} profileData - Updated profile data
   * @returns {Promise} API response
   */
  updateProfile: async (profileData) => {
    try {
      const response = await api.put('/users/profile', profileData);
      
      // Update user data in localStorage
      if (response.success && response.data) {
        const currentUser = JSON.parse(localStorage.getItem('user'));
        const updatedUser = { ...currentUser, ...response.data };
        localStorage.setItem('user', JSON.stringify(updatedUser));
      }
      
      return response;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Get user by ID
   * @param {number} userId - User ID
   * @returns {Promise} API response
   */
  getUserById: async (userId) => {
    try {
      const response = await api.get(`/users/${userId}`);
      return response;
    } catch (error) {
      throw error;
    }
    },
  
  
  /**
   * Change password
   * @param {Object} passwordData - Current and new password
   * @returns {Promise} API response
   */
  changePassword: async (passwordData) => {
    try {
      const response = await api.put('/users/change-password', passwordData);
      return response;
    } catch (error) {
      throw error;
    }
  },
};

export default userService;