import api from './api';

const businessService = {
  // ==================== BUSINESS OWNER OPERATIONS ====================

  /**
   * Create a new business
   * @param {Object} businessData - { businessName, businessCategory, businessDesc, ssmNumber }
   * @returns {Promise} Business response
   */
  createBusiness: async (businessData) => {
    try {
      const response = await api.post('/business/create', businessData);
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  /**
   * Get all businesses owned by current user
   * @returns {Promise} List of businesses
   */
  getMyBusinesses: async () => {
    try {
      const response = await api.get('/business/my-businesses');
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  /**
   * Get single business by ID
   * @param {number} businessId - Business ID
   * @returns {Promise} Business details
   */
  getBusinessById: async (businessId) => {
    try {
      const response = await api.get(`/business/${businessId}`);
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  /**
   * Update business details
   * @param {number} businessId - Business ID
   * @param {Object} businessData - Updated business data
   * @returns {Promise} Updated business
   */
  updateBusiness: async (businessId, businessData) => {
    try {
      const response = await api.put(`/business/${businessId}`, businessData);
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  /**
   * Delete business
   * @param {number} businessId - Business ID
   * @returns {Promise} Success message
   */
  deleteBusiness: async (businessId) => {
    try {
      const response = await api.delete(`/business/${businessId}`);
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  /**
   * Upload SSM document
   * @param {number} businessId - Business ID
   * @param {File} file - File object
   * @returns {Promise} Updated business with document
   */
  uploadSSMDocument: async (businessId, file) => {
    try {
      const formData = new FormData();
      formData.append('file', file);

      const response = await api.post(`/business/${businessId}/upload-ssm`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  /**
   * Download SSM document
   * @param {number} businessId - Business ID
   * @returns {Promise} File blob
   */
  downloadSSMDocument: async (businessId) => {
    try {
      const response = await api.get(`/business/${businessId}/ssm-document`, {
        responseType: 'blob',
      });
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  // ==================== MPP BUSINESS MANAGEMENT OPERATIONS ====================

  /**
   * Get all businesses with search and filter (MPP only)
   * @param {Object} filters - { searchQuery, businessCategory, businessStatus, ownerCategory, startDate, endDate }
   * @returns {Promise} List of businesses with owner info
   */
  getAllBusinessesForMPP: async (filters = {}) => {
    try {
      const params = new URLSearchParams();
      
      // Add filters to query params (only if they have values)
      if (filters.searchQuery) params.append('searchQuery', filters.searchQuery);
      if (filters.businessCategory) params.append('businessCategory', filters.businessCategory);
      if (filters.businessStatus) params.append('businessStatus', filters.businessStatus);
      if (filters.ownerCategory) params.append('ownerCategory', filters.ownerCategory);
      if (filters.startDate) params.append('startDate', filters.startDate);
      if (filters.endDate) params.append('endDate', filters.endDate);

      const queryString = params.toString();
      const url = queryString ? `/mpp/businesses?${queryString}` : '/mpp/businesses';
      
      const response = await api.get(url);
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  /**
   * Get single business details with owner info (MPP view)
   * @param {number} businessId - Business ID
   * @returns {Promise} Business details with owner information
   */
  getBusinessDetailForMPP: async (businessId) => {
    try {
      const response = await api.get(`/mpp/businesses/${businessId}`);
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  /**
   * Block business (MPP only)
   * @param {number} businessId - Business ID
   * @param {string} reason - Optional reason for blocking
   * @returns {Promise} Updated business
   */
  blockBusiness: async (businessId, reason = '') => {
    try {
      const response = await api.put(`/mpp/businesses/${businessId}/block`, {
        businessStatus: 'BLOCKED',
        reason: reason,
      });
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  /**
   * Activate business (MPP only)
   * @param {number} businessId - Business ID
   * @returns {Promise} Updated business
   */
  activateBusiness: async (businessId) => {
    try {
      const response = await api.put(`/mpp/businesses/${businessId}/activate`);
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  /**
   * Get business statistics (MPP only)
   * @returns {Promise} { totalBusinesses, activeBusinesses, blockedBusinesses }
   */
  getBusinessStatistics: async () => {
    try {
      const response = await api.get('/mpp/businesses/statistics');
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  /**
   * Get businesses by category (MPP only)
   * @param {string} category - Business category
   * @returns {Promise} List of businesses
   */
  getBusinessesByCategory: async (category) => {
    try {
      const response = await api.get(`/mpp/businesses/category/${category}`);
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  /**
   * Get businesses by status (MPP only)
   * @param {string} status - Business status (ACTIVE, BLOCKED)
   * @returns {Promise} List of businesses
   */
  getBusinessesByStatus: async (status) => {
    try {
      const response = await api.get(`/mpp/businesses/status/${status}`);
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  /**
   * Get businesses by owner category (MPP only)
   * @param {string} ownerCategory - Owner category (STUDENT, NON_STUDENT)
   * @returns {Promise} List of businesses
   */
  getBusinessesByOwnerCategory: async (ownerCategory) => {
    try {
      const response = await api.get(`/mpp/businesses/owner-category/${ownerCategory}`);
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  // ==================== OLD MPP OPERATIONS (Keep for compatibility) ====================

  /**
   * Get all businesses (MPP only) - OLD METHOD
   * @returns {Promise} List of all businesses
   */
  getAllBusinesses: async () => {
    try {
      const response = await api.get('/business/admin/all');
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  /**
   * Get businesses by owner ID (MPP only) - OLD METHOD
   * @param {number} userId - User ID
   * @returns {Promise} List of businesses
   */
  getBusinessesByOwnerId: async (userId) => {
    try {
      const response = await api.get(`/business/admin/owner/${userId}`);
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  /**
   * Get business by ID (MPP admin view) - OLD METHOD
   * @param {number} businessId - Business ID
   * @returns {Promise} Business details
   */
  getBusinessByIdAdmin: async (businessId) => {
    try {
      const response = await api.get(`/business/admin/${businessId}`);
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  /**
   * Block or activate business owner (MPP only) - OLD METHOD
   * @param {number} userId - User ID
   * @param {string} status - 'BLOCKED' or 'ACTIVE'
   * @returns {Promise} Success message
   */
  updateBusinessOwnerStatus: async (userId, status) => {
    try {
      const response = await api.put(`/business/admin/owner/${userId}/status`, {
        businessStatus: status,
      });
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },
};

export default businessService;