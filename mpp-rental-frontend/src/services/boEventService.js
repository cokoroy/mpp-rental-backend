import api from './api';

const boEventService = {

    // ==================== BO EVENT BROWSING ====================

    /**
     * Get all events for Business Owner
     * @param {Object} filters - { searchQuery, eventStatus }
     */
    getEvents: async (filters = {}) => {
        try {
            const params = new URLSearchParams();
            if (filters.searchQuery) params.append('searchQuery', filters.searchQuery);
            if (filters.eventStatus && filters.eventStatus !== 'all') {
                params.append('eventStatus', filters.eventStatus);
            }
            const queryString = params.toString();
            const url = queryString ? `/bo/events?${queryString}` : '/bo/events';
            const response = await api.get(url);
            return response.data;
        } catch (error) {
            throw error;
        }
    },

    /**
     * Get event with facilities for BO (includes applicablePrice, remainingQuota)
     * @param {number} eventId
     */
    getEventWithFacilities: async (eventId) => {
        try {
            const response = await api.get(`/bo/events/${eventId}/with-facilities`);
            return response.data;
        } catch (error) {
            throw error;
        }
    },

    // ==================== APPLICATION ====================

    /**
     * Submit facility applications
     * @param {Object} payload - { businessId, facilities: [{ eventFacilityId, quantity }] }
     */
    submitApplications: async (payload) => {
        try {
            const response = await api.post('/bo/applications', payload);
            return response.data;
        } catch (error) {
            throw error;
        }
    },

    // ==================== HELPERS ====================

    /**
     * Format date for display (e.g. "Feb 1, 2024")
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
     * Format long date (e.g. "February 1, 2024")
     */
    formatDateLong: (dateStr) => {
        if (!dateStr) return '';
        return new Date(dateStr).toLocaleDateString('en-US', {
            month: 'long',
            day: 'numeric',
            year: 'numeric',
        });
    },

    /**
     * Get event status badge style
     */
    getStatusStyle: (status) => {
        switch (status) {
            case 'upcoming':
                return 'bg-blue-100 text-blue-700';
            case 'active':
                return 'bg-green-100 text-green-700';
            case 'completed':
                return 'bg-gray-100 text-gray-700';
            default:
                return 'bg-gray-100 text-gray-700';
        }
    },

    /**
     * Get application status badge style
     */
    getApplicationStatusStyle: (status) => {
        switch (status) {
            case 'PENDING':
                return 'bg-yellow-100 text-yellow-700';
            case 'APPROVED':
                return 'bg-green-100 text-green-700';
            case 'REJECTED':
                return 'bg-red-100 text-red-700';
            case 'CANCELLED':
                return 'bg-gray-100 text-gray-700';
            default:
                return 'bg-gray-100 text-gray-700';
        }
    },
};

export default boEventService;