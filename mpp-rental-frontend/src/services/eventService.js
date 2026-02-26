import api from './api';

const eventService = {
  // ==================== MPP EVENT MANAGEMENT OPERATIONS ====================

  /**
   * Create new event with facility assignments
   * @param {Object} eventData - Event data with facilities array
   * @returns {Promise} Created event with facilities
   */
  createEvent: async (eventData) => {
    try {
      const response = await api.post('/mpp/events', eventData);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Get all events with optional search and filters
   * @param {Object} filters - { searchQuery, eventStatus }
   * @returns {Promise} List of events
   */
  getAllEvents: async (filters = {}) => {
    try {
      const params = new URLSearchParams();
      
      // Add filters to query params (only if they have values)
      if (filters.searchQuery) {
        params.append('searchQuery', filters.searchQuery);
      }
      if (filters.eventStatus && filters.eventStatus !== 'all') {
        params.append('eventStatus', filters.eventStatus);
      }

      const queryString = params.toString();
      const url = queryString ? `/mpp/events?${queryString}` : '/mpp/events';
      
      const response = await api.get(url);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Get event by ID
   * @param {number} eventId - Event ID
   * @returns {Promise} Event details
   */
  getEventById: async (eventId) => {
    try {
      const response = await api.get(`/mpp/events/${eventId}`);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Get event with assigned facilities
   * @param {number} eventId - Event ID
   * @returns {Promise} Event with facilities list
   */
  getEventWithFacilities: async (eventId) => {
    try {
      const response = await api.get(`/mpp/events/${eventId}/with-facilities`);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Get facilities assigned to an event
   * @param {number} eventId - Event ID
   * @returns {Promise} List of assigned facilities
   */
  getEventFacilities: async (eventId) => {
    try {
      const response = await api.get(`/mpp/events/${eventId}/facilities`);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Update event
   * @param {number} eventId - Event ID
   * @param {Object} eventData - Updated event data with facilities
   * @returns {Promise} Updated event with facilities
   */
  updateEvent: async (eventId, eventData) => {
    try {
      const response = await api.put(`/mpp/events/${eventId}`, eventData);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Cancel event (soft delete)
   * @param {number} eventId - Event ID
   * @returns {Promise} Success message
   */
  cancelEvent: async (eventId) => {
    try {
      const response = await api.delete(`/mpp/events/${eventId}`);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Toggle event application status (OPEN <-> CLOSED)
   * @param {number} eventId - Event ID
   * @returns {Promise} Updated event
   */
  toggleApplicationStatus: async (eventId) => {
    try {
      const response = await api.patch(`/mpp/events/${eventId}/application-status`);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Format date for display
   * @param {string} dateStr - Date string (YYYY-MM-DD)
   * @returns {string} Formatted date (e.g., "Mar 15, 2024")
   */
  formatDate: (dateStr) => {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
    });
  },

  /**
   * Format time for display (HH:MM:SS -> HH:MM)
   * @param {string} timeStr - Time string
   * @returns {string} Formatted time
   */
  formatTime: (timeStr) => {
    if (!timeStr) return '';
    // If time includes seconds, remove them
    return timeStr.substring(0, 5);
  },

  /**
   * Get status color class
   * @param {string} status - Event status
   * @returns {string} Tailwind CSS classes
   */
  getStatusColor: (status) => {
    switch (status) {
      case 'upcoming':
        return 'bg-blue-100 text-blue-700';
      case 'active':
        return 'bg-green-100 text-green-700';
      case 'completed':
        return 'bg-gray-100 text-gray-700';
      case 'cancelled':
        return 'bg-red-100 text-red-700';
      default:
        return 'bg-gray-100 text-gray-700';
    }
  },

  /**
   * Event type options
   */
  eventTypes: [
    'Bazaar',
    'Festival',
    'Expo',
    'Market',
    'Other',
  ],
};

export default eventService;
