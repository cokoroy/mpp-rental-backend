import api from './api';

const facilityService = {
  // ==================== MPP FACILITY MANAGEMENT OPERATIONS ====================

  /**
   * Create new facility
   * @param {Object} facilityData - Facility data
   * @param {File} imageFile - Optional image file
   * @returns {Promise} Created facility
   */
  createFacility: async (facilityData, imageFile = null) => {
    try {
      const formData = new FormData();
      
      // Append all facility fields
      formData.append('facilityName', facilityData.facilityName);
      formData.append('facilitySize', facilityData.facilitySize);
      formData.append('facilityType', facilityData.facilityType);
      formData.append('facilityDesc', facilityData.facilityDesc);
      formData.append('usage', facilityData.usage);
      formData.append('facilityBaseStudentPrice', facilityData.facilityBaseStudentPrice);
      formData.append('facilityBaseNonstudentPrice', facilityData.facilityBaseNonstudentPrice);
      formData.append('facilityStatus', facilityData.facilityStatus);
      
      // Append optional fields
      if (facilityData.remark) {
        formData.append('remark', facilityData.remark);
      }
      
      // Append image if provided
      if (imageFile) {
        formData.append('image', imageFile);
      }

      const response = await api.post('/mpp/facilities', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });
      
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Get all facilities with optional filters
   * @param {Object} filters - { searchQuery, facilityType, facilitySize, facilityStatus }
   * @returns {Promise} List of facilities
   */
  getAllFacilities: async (filters = {}) => {
    try {
      const params = new URLSearchParams();
      
      // Add filters to query params (only if they have values)
      if (filters.searchQuery) params.append('searchQuery', filters.searchQuery);
      if (filters.facilityType && filters.facilityType !== 'all') {
        params.append('facilityType', filters.facilityType);
      }
      if (filters.facilitySize && filters.facilitySize !== 'all') {
        params.append('facilitySize', filters.facilitySize);
      }
      if (filters.facilityStatus && filters.facilityStatus !== 'all') {
        params.append('facilityStatus', filters.facilityStatus);
      }

      const queryString = params.toString();
      const url = queryString ? `/mpp/facilities?${queryString}` : '/mpp/facilities';
      
      const response = await api.get(url);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Get facility by ID
   * @param {number} facilityId - Facility ID
   * @returns {Promise} Facility details
   */
  getFacilityById: async (facilityId) => {
    try {
      const response = await api.get(`/mpp/facilities/${facilityId}`);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Update facility
   * @param {number} facilityId - Facility ID
   * @param {Object} facilityData - Updated facility data
   * @param {File} imageFile - Optional new image file
   * @param {boolean} removeImage - True if image should be removed
   * @returns {Promise} Updated facility
   */
  updateFacility: async (facilityId, facilityData, imageFile = null, removeImage = false) => {
    try {
      const formData = new FormData();
      
      // Append all facility fields
      formData.append('facilityName', facilityData.facilityName);
      formData.append('facilitySize', facilityData.facilitySize);
      formData.append('facilityType', facilityData.facilityType);
      formData.append('facilityDesc', facilityData.facilityDesc);
      formData.append('usage', facilityData.usage);
      formData.append('facilityBaseStudentPrice', facilityData.facilityBaseStudentPrice);
      formData.append('facilityBaseNonstudentPrice', facilityData.facilityBaseNonstudentPrice);
      formData.append('facilityStatus', facilityData.facilityStatus);
      
      // Append optional fields
      if (facilityData.remark) {
        formData.append('remark', facilityData.remark);
      }
      
      // Handle image removal or update
      if (removeImage) {
        // Send flag to remove image
        formData.append('removeImage', 'true');
      } else if (imageFile) {
        // Append new image if provided
        formData.append('image', imageFile);
      }

      const response = await api.put(`/mpp/facilities/${facilityId}`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });
      
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Delete facility (soft delete)
   * @param {number} facilityId - Facility ID
   * @returns {Promise} Success message
   */
  deleteFacility: async (facilityId) => {
    try {
      const response = await api.delete(`/mpp/facilities/${facilityId}`);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Toggle facility status (activate/deactivate)
   * @param {number} facilityId - Facility ID
   * @returns {Promise} Updated facility
   */
  toggleFacilityStatus: async (facilityId) => {
    const response = await api.patch(`/mpp/facilities/${facilityId}/status`);
    return response.data;
  },

  /**
   * Get only active facilities
   * @returns {Promise} List of active facilities
   */
  getActiveFacilities: async () => {
    try {
      const response = await api.get('/mpp/facilities/active');
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Get facility image URL
   * @param {string} imagePath - Image path from backend (e.g., "uploads/facilities/uuid.jpg")
   * @returns {string} Full image URL
   */
  getFacilityImageUrl: (imagePath) => {
    if (!imagePath) return null;
    // If path already includes http, return as is
    if (imagePath.startsWith('http')) return imagePath;
    // Otherwise, prepend base URL
    // IMPORTANT: Don't use axios for images - use direct URL
    return `http://localhost:8080/${imagePath}`;
  },

  /**
   * Test if image URL is accessible
   * @param {string} imageUrl - Full image URL
   * @returns {Promise<boolean>} True if accessible
   */
  testImageUrl: async (imageUrl) => {
    try {
      const response = await fetch(imageUrl, {
        method: 'HEAD',
        mode: 'cors',
      });
      return response.ok;
    } catch (error) {
      console.error('Image not accessible:', error);
      return false;
    }
  },
};

export default facilityService;