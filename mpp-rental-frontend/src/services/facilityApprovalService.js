import api from './api';

const facilityApprovalService = {

    // ==================== EVENT LIST ====================

    /**
     * Get all events with application summary counts
     * @param {string} statusFilter - 'all' | 'upcoming' | 'active' | 'completed'
     */
    getEventsWithSummary: async (statusFilter = 'all') => {
        try {
            const params = new URLSearchParams();
            if (statusFilter && statusFilter !== 'all') {
                params.append('statusFilter', statusFilter);
            }
            const queryString = params.toString();
            const url = queryString ? `/mpp/approvals/events?${queryString}` : '/mpp/approvals/events';
            const response = await api.get(url);
            return response.data;
        } catch (error) {
            throw error;
        }
    },

    // ==================== APPLICATIONS BY EVENT ====================

    /**
     * Get all applications for a specific event
     * @param {number} eventId
     * @param {Object} filters - { statusFilter, searchQuery, sortOrder }
     */
    getApplicationsByEvent: async (eventId, filters = {}) => {
        try {
            const params = new URLSearchParams();
            if (filters.statusFilter && filters.statusFilter !== 'all') {
                params.append('statusFilter', filters.statusFilter);
            }
            if (filters.searchQuery) {
                params.append('searchQuery', filters.searchQuery);
            }
            if (filters.sortOrder) {
                params.append('sortOrder', filters.sortOrder);
            }
            const queryString = params.toString();
            const url = queryString
                ? `/mpp/approvals/events/${eventId}/applications?${queryString}`
                : `/mpp/approvals/events/${eventId}/applications`;
            const response = await api.get(url);
            return response.data;
        } catch (error) {
            throw error;
        }
    },

    // ==================== SINGLE ACTIONS ====================

    /**
     * Approve a single application
     * @param {number} applicationId
     */
    approveApplication: async (applicationId) => {
        try {
            const response = await api.patch(`/mpp/approvals/${applicationId}/approve`);
            return response.data;
        } catch (error) {
            throw error;
        }
    },

    /**
     * Reject a single application
     * @param {number} applicationId
     * @param {string} rejectionReason - optional
     */
    rejectApplication: async (applicationId, rejectionReason = null) => {
        try {
            const body = rejectionReason ? { rejectionReason } : {};
            const response = await api.patch(`/mpp/approvals/${applicationId}/reject`, body);
            return response.data;
        } catch (error) {
            throw error;
        }
    },

    /**
     * Revert application back to PENDING
     * @param {number} applicationId
     */
    revertToPending: async (applicationId) => {
        try {
            const response = await api.patch(`/mpp/approvals/${applicationId}/revert`);
            return response.data;
        } catch (error) {
            throw error;
        }
    },

    /**
     * Check if application has been paid (for revert confirmation)
     * @param {number} applicationId
     * @returns {boolean} hasPaid
     */
    checkPaymentStatus: async (applicationId) => {
        try {
            const response = await api.get(`/mpp/approvals/${applicationId}/payment-status`);
            return response.data?.hasPaid ?? false;
        } catch (error) {
            throw error;
        }
    },

    // ==================== BULK ACTIONS ====================

    /**
     * Bulk approve applications
     * @param {number[]} applicationIds
     */
    bulkApprove: async (applicationIds) => {
        try {
            const response = await api.post('/mpp/approvals/bulk-approve', { applicationIds });
            return response.data;
        } catch (error) {
            throw error;
        }
    },

    /**
     * Bulk reject applications
     * @param {number[]} applicationIds
     * @param {string} rejectionReason - optional
     */
    bulkReject: async (applicationIds, rejectionReason = null) => {
        try {
            const response = await api.post('/mpp/approvals/bulk-reject', {
                applicationIds,
                rejectionReason,
            });
            return response.data;
        } catch (error) {
            throw error;
        }
    },

    /**
     * Bulk revert applications
     * @param {number[]} applicationIds
     */
    bulkRevert: async (applicationIds) => {
        try {
            const response = await api.post('/mpp/approvals/bulk-revert', { applicationIds });
            return response.data;
        } catch (error) {
            throw error;
        }
    },

    // ==================== HELPERS ====================

    formatDateTime: (dateTimeStr) => {
        if (!dateTimeStr) return '';
        const date = new Date(dateTimeStr);
        return date.toLocaleDateString('en-US', {
            month: 'short',
            day: 'numeric',
            year: 'numeric',
        }) + ', ' + date.toLocaleTimeString('en-US', {
            hour: 'numeric',
            minute: '2-digit',
            hour12: true,
        });
    },

    getApplicationStatusStyle: (status) => {
        switch (status) {
            case 'PENDING':
                return 'bg-yellow-100 text-yellow-700 border border-yellow-200';
            case 'APPROVED':
                return 'bg-green-100 text-green-700 border border-green-200';
            case 'REJECTED':
                return 'bg-red-100 text-red-700 border border-red-200';
            case 'CANCELLED':
                return 'bg-gray-100 text-gray-600 border border-gray-200';
            default:
                return 'bg-gray-100 text-gray-600 border border-gray-200';
        }
    },

    getEventStatusStyle: (status) => {
        switch (status) {
            case 'upcoming':
                return 'bg-purple-100 text-purple-700 border border-purple-200';
            case 'active':
                return 'bg-blue-100 text-blue-700 border border-blue-200';
            case 'completed':
                return 'bg-gray-100 text-gray-600 border border-gray-200';
            default:
                return 'bg-gray-100 text-gray-600 border border-gray-200';
        }
    },
};

export default facilityApprovalService;