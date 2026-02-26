import { useState, useEffect, useCallback } from 'react';
import {
    CheckCircle, XCircle, Eye, Calendar, MapPin, Building, User,
    Clock, Search, ArrowUpDown, RotateCcw, ArrowLeft, Users,
    CheckSquare, Square, AlertCircle, CreditCard,
} from 'lucide-react';
import facilityApprovalService from '../services/facilityApprovalService';
import { Toast } from './Toast';
import { RejectModal } from './RejectModal';
import { RevertModal } from './RevertModal';

// ============================================================
// APPLICATION DETAIL MODAL
// ============================================================
function ApplicationDetailModal({ app, onClose, onApprove, onReject, onRevert, actionLoading }) {
    const [showRejectModal, setShowRejectModal] = useState(false);
    const [showRevertModal, setShowRevertModal] = useState(false);
    const [hasPaid, setHasPaid] = useState(false);
    const [checkingPayment, setCheckingPayment] = useState(false);

    const handleRevertClick = async () => {
        if (app.applicationStatus === 'APPROVED') {
            setCheckingPayment(true);
            try {
                const paid = await facilityApprovalService.checkPaymentStatus(app.applicationId);
                setHasPaid(paid);
            } catch {
                setHasPaid(false);
            } finally {
                setCheckingPayment(false);
            }
        }
        setShowRevertModal(true);
    };

    const statusStyle = facilityApprovalService.getApplicationStatusStyle(app.applicationStatus);

    return (
        <>
            <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-40">
                <div className="bg-white rounded-2xl max-w-2xl w-full max-h-[90vh] overflow-y-auto shadow-xl">
                    {/* Header */}
                    <div className="sticky top-0 bg-white border-b border-gray-100 px-6 py-4 flex items-center justify-between rounded-t-2xl z-10">
                        <h2 className="text-lg font-semibold text-gray-900">Application Details</h2>
                        <button
                            onClick={onClose}
                            className="w-8 h-8 flex items-center justify-center rounded-lg hover:bg-gray-100 transition-colors"
                        >
                            <XCircle className="w-5 h-5 text-gray-400" />
                        </button>
                    </div>

                    <div className="p-6 space-y-6">
                        {/* Info Grid */}
                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-x-8 gap-y-4">
                            <InfoField label="Business Name" value={app.businessName} />
                            <InfoField label="Owner Name" value={app.ownerName} />
                            <InfoField label="Event Name" value={app.eventName} />
                            <InfoField label="Category" value={app.businessCategory} />
                            <InfoField label="Event Date" value={app.eventStartDate} />
                            <InfoField
                                label="Application Created At"
                                value={facilityApprovalService.formatDateTime(app.applicationCreatedAt)}
                                icon={<Clock className="w-4 h-4 text-gray-400" />}
                            />
                            <InfoField label="Location" value={app.eventVenue} />
                            <div>
                                <p className="text-xs font-medium text-gray-500 uppercase tracking-wide mb-1.5">Status</p>
                                <span className={`inline-block px-3 py-1 rounded-full text-sm font-medium capitalize ${statusStyle}`}>
                  {app.applicationStatus.charAt(0) + app.applicationStatus.slice(1).toLowerCase()}
                </span>
                            </div>
                            {app.applicationStatus === 'REJECTED' && app.rejectionReason && (
                                <InfoField label="Rejection Reason" value={app.rejectionReason} />
                            )}
                            {/* Payment info for approved */}
                            {app.applicationStatus === 'APPROVED' && app.paymentAmount != null && (
                                <>
                                    <InfoField
                                        label="Payment Amount"
                                        value={`RM ${parseFloat(app.paymentAmount).toFixed(2)}`}
                                    />
                                    <div>
                                        <p className="text-xs font-medium text-gray-500 uppercase tracking-wide mb-1.5">Payment Status</p>
                                        <span className={`inline-block px-3 py-1 rounded-full text-sm font-medium ${
                                            app.paymentStatus === 'PAID'
                                                ? 'bg-green-100 text-green-700 border border-green-200'
                                                : app.paymentStatus === 'UNPAID'
                                                    ? 'bg-yellow-100 text-yellow-700 border border-yellow-200'
                                                    : 'bg-gray-100 text-gray-600 border border-gray-200'
                                        }`}>
                      {app.paymentStatus}
                    </span>
                                    </div>
                                </>
                            )}
                        </div>

                        {/* Rejection reason banner */}
                        {app.applicationStatus === 'REJECTED' && app.rejectionReason && (
                            <div className="bg-red-50 border border-red-200 rounded-xl p-4 flex items-start gap-3">
                                <XCircle className="w-5 h-5 text-red-500 flex-shrink-0 mt-0.5" />
                                <div>
                                    <p className="text-sm font-semibold text-red-800">Rejection Reason</p>
                                    <p className="text-sm text-red-700 mt-0.5">{app.rejectionReason}</p>
                                </div>
                            </div>
                        )}

                        {/* Facilities Table */}
                        <div>
                            <p className="text-xs font-medium text-gray-500 uppercase tracking-wide mb-3">Facility Requested</p>
                            <div className="rounded-xl border border-gray-200 overflow-hidden">
                                <table className="w-full text-sm">
                                    <thead>
                                    <tr className="bg-gray-50 border-b border-gray-200">
                                        <th className="px-4 py-3 text-left font-semibold text-gray-700">Facility Name</th>
                                        <th className="px-4 py-3 text-left font-semibold text-gray-700">Quantity</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <tr className="border-b border-gray-100">
                                        <td className="px-4 py-3 text-gray-800">{app.facilityName}</td>
                                        <td className="px-4 py-3 text-gray-800">{app.applicationFacilityQuantity} units</td>
                                    </tr>
                                    </tbody>
                                    <tfoot>
                                    <tr className="bg-gray-50 border-t border-gray-200">
                                        <td className="px-4 py-3 font-semibold text-gray-800">Total Quantity</td>
                                        <td className="px-4 py-3 font-semibold text-gray-800">{app.applicationFacilityQuantity} units</td>
                                    </tr>
                                    </tfoot>
                                </table>
                            </div>
                        </div>

                        {/* Business Description */}
                        {app.businessDesc && (
                            <div>
                                <p className="text-xs font-medium text-gray-500 uppercase tracking-wide mb-2">Business Description</p>
                                <p className="text-sm text-gray-700 leading-relaxed">{app.businessDesc}</p>
                            </div>
                        )}

                        {/* Actions */}
                        <div className="pt-4 border-t border-gray-100">
                            {app.applicationStatus === 'PENDING' && (
                                <div className="flex gap-3">
                                    <button
                                        onClick={() => setShowRejectModal(true)}
                                        disabled={actionLoading}
                                        className="flex-1 px-5 py-2.5 bg-red-600 text-white rounded-xl hover:bg-red-700 transition-colors flex items-center justify-center gap-2 text-sm font-medium disabled:opacity-50"
                                    >
                                        <XCircle className="w-4 h-4" />
                                        Reject Application
                                    </button>
                                    <button
                                        onClick={() => { onApprove(app.applicationId); onClose(); }}
                                        disabled={actionLoading}
                                        className="flex-1 px-5 py-2.5 bg-green-600 text-white rounded-xl hover:bg-green-700 transition-colors flex items-center justify-center gap-2 text-sm font-medium disabled:opacity-50"
                                    >
                                        <CheckCircle className="w-4 h-4" />
                                        Approve Application
                                    </button>
                                </div>
                            )}
                            {(app.applicationStatus === 'APPROVED' || app.applicationStatus === 'REJECTED') && (
                                <button
                                    onClick={handleRevertClick}
                                    disabled={actionLoading || checkingPayment}
                                    className="w-full px-5 py-2.5 bg-amber-600 text-white rounded-xl hover:bg-amber-700 transition-colors flex items-center justify-center gap-2 text-sm font-medium disabled:opacity-50"
                                >
                                    {checkingPayment ? (
                                        <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                                    ) : (
                                        <RotateCcw className="w-4 h-4" />
                                    )}
                                    Revert to Pending
                                </button>
                            )}
                        </div>
                    </div>
                </div>
            </div>

            {/* Reject Modal (from detail modal) */}
            <RejectModal
                isOpen={showRejectModal}
                onClose={() => setShowRejectModal(false)}
                onConfirm={(reason) => {
                    onReject(app.applicationId, reason);
                    setShowRejectModal(false);
                    onClose();
                }}
                loading={actionLoading}
            />

            {/* Revert Modal (from detail modal) */}
            <RevertModal
                isOpen={showRevertModal}
                onClose={() => setShowRevertModal(false)}
                onConfirm={() => {
                    onRevert(app.applicationId);
                    setShowRevertModal(false);
                    onClose();
                }}
                hasPaid={hasPaid}
                loading={actionLoading}
            />
        </>
    );
}

function InfoField({ label, value, icon }) {
    return (
        <div>
            <p className="text-xs font-medium text-gray-500 uppercase tracking-wide mb-1.5">{label}</p>
            <p className="text-sm text-gray-900 flex items-center gap-1.5">
                {icon}
                {value || '—'}
            </p>
        </div>
    );
}

// ============================================================
// STATUS TAB BAR
// ============================================================
function StatusTabs({ tabs, selected, onChange }) {
    return (
        <div className="flex gap-1 bg-gray-100 rounded-xl p-1 flex-wrap">
            {tabs.map((tab) => (
                <button
                    key={tab.value}
                    onClick={() => onChange(tab.value)}
                    className={`px-4 py-2 rounded-lg text-sm font-medium transition-all ${
                        selected === tab.value
                            ? 'bg-white text-purple-700 shadow-sm'
                            : 'text-gray-600 hover:text-gray-800'
                    }`}
                >
                    {tab.label}
                </button>
            ))}
        </div>
    );
}

// ============================================================
// APPLICATION CARD
// ============================================================
function ApplicationCard({ app, isSelected, onToggleSelect, onView, onApprove, onReject, onRevert }) {
    const statusStyle = facilityApprovalService.getApplicationStatusStyle(app.applicationStatus);
    const statusLabel = app.applicationStatus.charAt(0) + app.applicationStatus.slice(1).toLowerCase();

    return (
        <div className={`bg-white rounded-xl border transition-all ${
            isSelected ? 'border-purple-400 ring-1 ring-purple-300 shadow-sm' : 'border-gray-200 hover:border-gray-300 hover:shadow-sm'
        }`}>
            <div className="p-5">
                <div className="flex items-start gap-3">
                    {/* Checkbox */}
                    <button onClick={() => onToggleSelect(app.applicationId)} className="mt-0.5 flex-shrink-0">
                        {isSelected
                            ? <CheckSquare className="w-5 h-5 text-purple-600" />
                            : <Square className="w-5 h-5 text-gray-300 hover:text-gray-500" />
                        }
                    </button>

                    {/* Icon */}
                    <div className="w-9 h-9 bg-purple-100 rounded-lg flex items-center justify-center flex-shrink-0">
                        <Building className="w-4 h-4 text-purple-600" />
                    </div>

                    {/* Info */}
                    <div className="flex-1 min-w-0">
                        <div className="flex items-center gap-2 flex-wrap">
                            <span className="text-sm font-semibold text-gray-900">{app.businessName}</span>
                            <span className={`px-2.5 py-0.5 rounded-full text-xs font-medium ${statusStyle}`}>
                {statusLabel}
              </span>
                        </div>
                        <div className="flex items-center gap-1 mt-0.5">
                            <User className="w-3.5 h-3.5 text-gray-400" />
                            <span className="text-xs text-gray-500">{app.ownerName}</span>
                        </div>
                    </div>

                    {/* Actions */}
                    <div className="flex flex-col gap-1.5 flex-shrink-0">
                        <button
                            onClick={() => onView(app)}
                            className="px-3 py-1.5 border border-gray-200 text-gray-600 rounded-lg hover:bg-gray-50 transition-colors flex items-center gap-1.5 text-xs font-medium"
                        >
                            <Eye className="w-3.5 h-3.5" />
                            View
                        </button>
                        {app.applicationStatus === 'PENDING' && (
                            <>
                                <button
                                    onClick={() => onApprove(app.applicationId)}
                                    className="px-3 py-1.5 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors flex items-center gap-1.5 text-xs font-medium"
                                >
                                    <CheckCircle className="w-3.5 h-3.5" />
                                    Approve
                                </button>
                                <button
                                    onClick={() => onReject(app)}
                                    className="px-3 py-1.5 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors flex items-center gap-1.5 text-xs font-medium"
                                >
                                    <XCircle className="w-3.5 h-3.5" />
                                    Reject
                                </button>
                            </>
                        )}
                        {(app.applicationStatus === 'APPROVED' || app.applicationStatus === 'REJECTED') && (
                            <button
                                onClick={() => onRevert(app)}
                                className="px-3 py-1.5 bg-amber-500 text-white rounded-lg hover:bg-amber-600 transition-colors flex items-center gap-1.5 text-xs font-medium"
                            >
                                <RotateCcw className="w-3.5 h-3.5" />
                                Revert
                            </button>
                        )}
                    </div>
                </div>

                {/* Details Row */}
                <div className="mt-3 ml-16 grid grid-cols-2 sm:grid-cols-3 gap-2 text-xs text-gray-500">
                    <span><span className="text-gray-400">Facility: </span>{app.facilityName}</span>
                    <span><span className="text-gray-400">Qty: </span>{app.applicationFacilityQuantity} units</span>
                    <span><span className="text-gray-400">Category: </span>{app.businessCategory}</span>
                    <span className="col-span-2 flex items-center gap-1">
            <Clock className="w-3 h-3 text-gray-400" />
            Applied: {facilityApprovalService.formatDateTime(app.applicationCreatedAt)}
          </span>
                </div>
            </div>
        </div>
    );
}

// ============================================================
// MAIN COMPONENT
// ============================================================
export function FacilityApproval() {
    // ---- View state ----
    const [selectedEvent, setSelectedEvent] = useState(null);

    // ---- Event list state ----
    const [events, setEvents] = useState([]);
    const [eventFilter, setEventFilter] = useState('all');
    const [eventsLoading, setEventsLoading] = useState(false);

    // ---- Applications state ----
    const [applications, setApplications] = useState([]);
    const [statusFilter, setStatusFilter] = useState('all');
    const [searchQuery, setSearchQuery] = useState('');
    const [sortOrder, setSortOrder] = useState('latest');
    const [appsLoading, setAppsLoading] = useState(false);

    // ---- Selection ----
    const [selectedIds, setSelectedIds] = useState([]);

    // ---- Modals ----
    const [viewingApp, setViewingApp] = useState(null);
    const [rejectTarget, setRejectTarget] = useState(null);   // single app object
    const [revertTarget, setRevertTarget] = useState(null);   // single app object
    const [hasPaidForRevert, setHasPaidForRevert] = useState(false);
    const [checkingPayment, setCheckingPayment] = useState(false);
    const [showBulkRejectModal, setShowBulkRejectModal] = useState(false);
    const [showBulkRevertModal, setShowBulkRevertModal] = useState(false);

    // ---- Loading/action ----
    const [actionLoading, setActionLoading] = useState(false);

    // ---- Toast ----
    const [toast, setToast] = useState({ isVisible: false, type: 'success', message: '' });

    const showToast = (type, message) => setToast({ isVisible: true, type, message });
    const hideToast = () => setToast((prev) => ({ ...prev, isVisible: false }));

    // ==================== LOAD EVENTS ====================
    const loadEvents = useCallback(async () => {
        setEventsLoading(true);
        try {
            const data = await facilityApprovalService.getEventsWithSummary(eventFilter);
            setEvents(data || []);
        } catch (err) {
            showToast('error', err || 'Failed to load events');
        } finally {
            setEventsLoading(false);
        }
    }, [eventFilter]);

    useEffect(() => {
        loadEvents();
    }, [loadEvents]);

    // ==================== LOAD APPLICATIONS ====================
    const loadApplications = useCallback(async () => {
        if (!selectedEvent) return;
        setAppsLoading(true);
        try {
            const data = await facilityApprovalService.getApplicationsByEvent(selectedEvent.eventId, {
                statusFilter,
                searchQuery,
                sortOrder,
            });
            setApplications(data || []);
        } catch (err) {
            showToast('error', err || 'Failed to load applications');
        } finally {
            setAppsLoading(false);
        }
    }, [selectedEvent, statusFilter, searchQuery, sortOrder]);

    useEffect(() => {
        loadApplications();
    }, [loadApplications]);

    // ==================== APPROVE ====================
    const handleApprove = async (applicationId) => {
        setActionLoading(true);
        try {
            await facilityApprovalService.approveApplication(applicationId);
            showToast('success', 'Application approved successfully');
            loadApplications();
            loadEvents();
            setSelectedIds((prev) => prev.filter((id) => id !== applicationId));
        } catch (err) {
            showToast('error', err || 'Failed to approve application');
        } finally {
            setActionLoading(false);
        }
    };

    // ==================== REJECT ====================
    const handleReject = async (applicationId, reason) => {
        setActionLoading(true);
        try {
            await facilityApprovalService.rejectApplication(applicationId, reason);
            showToast('success', 'Application rejected');
            loadApplications();
            loadEvents();
            setSelectedIds((prev) => prev.filter((id) => id !== applicationId));
        } catch (err) {
            showToast('error', err || 'Failed to reject application');
        } finally {
            setActionLoading(false);
        }
    };

    // ==================== REVERT ====================
    const handleRevert = async (applicationId) => {
        setActionLoading(true);
        try {
            await facilityApprovalService.revertToPending(applicationId);
            showToast('success', 'Application reverted to pending');
            loadApplications();
            loadEvents();
            setSelectedIds((prev) => prev.filter((id) => id !== applicationId));
        } catch (err) {
            showToast('error', err || 'Failed to revert application');
        } finally {
            setActionLoading(false);
        }
    };

    // Open single reject modal from card (not detail modal)
    const openRejectModal = (app) => {
        setRejectTarget(app);
    };

    // Open single revert modal from card
    const openRevertModal = async (app) => {
        setRevertTarget(app);
        if (app.applicationStatus === 'APPROVED') {
            setCheckingPayment(true);
            try {
                const paid = await facilityApprovalService.checkPaymentStatus(app.applicationId);
                setHasPaidForRevert(paid);
            } catch {
                setHasPaidForRevert(false);
            } finally {
                setCheckingPayment(false);
            }
        } else {
            setHasPaidForRevert(false);
        }
    };

    // ==================== BULK APPROVE ====================
    const handleBulkApprove = async () => {
        setActionLoading(true);
        try {
            await facilityApprovalService.bulkApprove(selectedIds);
            showToast('success', `${selectedIds.length} application(s) approved`);
            setSelectedIds([]);
            loadApplications();
            loadEvents();
        } catch (err) {
            showToast('error', err || 'Bulk approve failed');
        } finally {
            setActionLoading(false);
        }
    };

    // ==================== BULK REJECT ====================
    const handleBulkReject = async (reason) => {
        setActionLoading(true);
        try {
            await facilityApprovalService.bulkReject(selectedIds, reason);
            showToast('success', `${selectedIds.length} application(s) rejected`);
            setSelectedIds([]);
            setShowBulkRejectModal(false);
            loadApplications();
            loadEvents();
        } catch (err) {
            showToast('error', err || 'Bulk reject failed');
        } finally {
            setActionLoading(false);
        }
    };

    // ==================== BULK REVERT ====================
    const handleBulkRevert = async () => {
        setActionLoading(true);
        try {
            await facilityApprovalService.bulkRevert(selectedIds);
            showToast('success', `${selectedIds.length} application(s) reverted`);
            setSelectedIds([]);
            setShowBulkRevertModal(false);
            loadApplications();
            loadEvents();
        } catch (err) {
            showToast('error', err || 'Bulk revert failed');
        } finally {
            setActionLoading(false);
        }
    };

    // ==================== SELECTION ====================
    const toggleSelect = (id) => {
        setSelectedIds((prev) =>
            prev.includes(id) ? prev.filter((i) => i !== id) : [...prev, id]
        );
    };

    const selectableApps = applications.filter((a) =>
        statusFilter === 'all' ? true : a.applicationStatus === statusFilter.toUpperCase()
    );

    const allSelected = selectableApps.length > 0 && selectedIds.length === selectableApps.length;

    const toggleSelectAll = () => {
        if (allSelected) {
            setSelectedIds([]);
        } else {
            setSelectedIds(selectableApps.map((a) => a.applicationId));
        }
    };

    // ==================== COUNTS ====================
    const counts = {
        all: applications.length,
        pending: applications.filter((a) => a.applicationStatus === 'PENDING').length,
        approved: applications.filter((a) => a.applicationStatus === 'APPROVED').length,
        rejected: applications.filter((a) => a.applicationStatus === 'REJECTED').length,
    };

    const statusTabs = [
        { value: 'all', label: `All (${counts.all})` },
        { value: 'pending', label: `Pending (${counts.pending})` },
        { value: 'approved', label: `Approved (${counts.approved})` },
        { value: 'rejected', label: `Rejected (${counts.rejected})` },
    ];

    const eventTabs = [
        { value: 'all', label: 'All' },
        { value: 'upcoming', label: 'Upcoming' },
        { value: 'active', label: 'Active' },
        { value: 'completed', label: 'Completed' },
    ];

    const selectedStatus = statusFilter.toUpperCase();
    const anyPendingSelected = selectedIds.some(
        (id) => applications.find((a) => a.applicationId === id)?.applicationStatus === 'PENDING'
    );
    const anyNonPendingSelected = selectedIds.some(
        (id) => applications.find((a) => a.applicationId === id)?.applicationStatus !== 'PENDING'
    );

    // ==================== APPLICATIONS VIEW ====================
    if (selectedEvent) {
        return (
            <div className="space-y-5">
                <Toast {...toast} onClose={hideToast} />

                {/* Header */}
                <div className="flex items-center gap-4">
                    <button
                        onClick={() => {
                            setSelectedEvent(null);
                            setSelectedIds([]);
                            setSearchQuery('');
                            setStatusFilter('all');
                            setApplications([]);
                        }}
                        className="px-3 py-2 border border-gray-200 text-gray-600 rounded-xl hover:bg-gray-50 transition-colors flex items-center gap-2 text-sm font-medium"
                    >
                        <ArrowLeft className="w-4 h-4" />
                        Back to Events
                    </button>
                    <div>
                        <h1 className="text-xl font-bold text-gray-900">{selectedEvent.eventName}</h1>
                        <p className="text-sm text-gray-500 mt-0.5">Manage facility applications for this event</p>
                    </div>
                </div>

                {/* Status Tabs */}
                <StatusTabs tabs={statusTabs} selected={statusFilter} onChange={(v) => { setStatusFilter(v); setSelectedIds([]); }} />

                {/* Search + Sort */}
                <div className="flex gap-3">
                    <div className="relative flex-1">
                        <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
                        <input
                            type="text"
                            placeholder="Search by business name or owner..."
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                            className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-gray-200 text-sm focus:outline-none focus:ring-2 focus:ring-purple-400 focus:border-transparent"
                        />
                    </div>
                    <div className="relative">
                        <ArrowUpDown className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400 pointer-events-none" />
                        <select
                            value={sortOrder}
                            onChange={(e) => setSortOrder(e.target.value)}
                            className="pl-9 pr-4 py-2.5 rounded-xl border border-gray-200 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-purple-400 focus:border-transparent appearance-none cursor-pointer"
                        >
                            <option value="latest">Latest First</option>
                            <option value="oldest">Oldest First</option>
                        </select>
                    </div>
                </div>

                {/* Bulk Action Bar */}
                {selectableApps.length > 0 && (
                    <div className="bg-white rounded-xl border border-gray-200 px-4 py-3 flex items-center justify-between gap-4">
                        <button
                            onClick={toggleSelectAll}
                            className="flex items-center gap-2 text-sm text-gray-700 hover:text-gray-900"
                        >
                            {allSelected
                                ? <CheckSquare className="w-5 h-5 text-purple-600" />
                                : <Square className="w-5 h-5 text-gray-400" />
                            }
                            <span>
                {selectedIds.length > 0
                    ? `${selectedIds.length} selected`
                    : `Select all (${selectableApps.length})`
                }
              </span>
                        </button>

                        {selectedIds.length > 0 && (
                            <div className="flex gap-2">
                                {anyNonPendingSelected && (
                                    <button
                                        onClick={() => setShowBulkRevertModal(true)}
                                        disabled={actionLoading}
                                        className="px-3 py-1.5 bg-amber-500 text-white rounded-lg hover:bg-amber-600 transition-colors flex items-center gap-1.5 text-sm font-medium disabled:opacity-50"
                                    >
                                        <RotateCcw className="w-4 h-4" />
                                        Revert ({selectedIds.length})
                                    </button>
                                )}
                                {anyPendingSelected && (
                                    <>
                                        <button
                                            onClick={() => setShowBulkRejectModal(true)}
                                            disabled={actionLoading}
                                            className="px-3 py-1.5 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors flex items-center gap-1.5 text-sm font-medium disabled:opacity-50"
                                        >
                                            <XCircle className="w-4 h-4" />
                                            Reject ({selectedIds.length})
                                        </button>
                                        <button
                                            onClick={handleBulkApprove}
                                            disabled={actionLoading}
                                            className="px-3 py-1.5 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors flex items-center gap-1.5 text-sm font-medium disabled:opacity-50"
                                        >
                                            <CheckCircle className="w-4 h-4" />
                                            Approve ({selectedIds.length})
                                        </button>
                                    </>
                                )}
                            </div>
                        )}
                    </div>
                )}

                {/* Applications List */}
                {appsLoading ? (
                    <div className="text-center py-16">
                        <div className="w-8 h-8 border-2 border-purple-600 border-t-transparent rounded-full animate-spin mx-auto mb-3" />
                        <p className="text-sm text-gray-500">Loading applications...</p>
                    </div>
                ) : applications.length === 0 ? (
                    <div className="bg-white rounded-xl border border-gray-200 p-12 text-center">
                        <AlertCircle className="w-10 h-10 text-gray-300 mx-auto mb-3" />
                        <p className="text-gray-500 text-sm">No applications found</p>
                    </div>
                ) : (
                    <div className="space-y-3">
                        {applications.map((app) => (
                            <ApplicationCard
                                key={app.applicationId}
                                app={app}
                                isSelected={selectedIds.includes(app.applicationId)}
                                onToggleSelect={toggleSelect}
                                onView={setViewingApp}
                                onApprove={handleApprove}
                                onReject={openRejectModal}
                                onRevert={openRevertModal}
                            />
                        ))}
                    </div>
                )}

                {/* Application Detail Modal */}
                {viewingApp && (
                    <ApplicationDetailModal
                        app={viewingApp}
                        onClose={() => setViewingApp(null)}
                        onApprove={handleApprove}
                        onReject={handleReject}
                        onRevert={handleRevert}
                        actionLoading={actionLoading}
                    />
                )}

                {/* Single Reject Modal (from card) */}
                <RejectModal
                    isOpen={!!rejectTarget}
                    onClose={() => setRejectTarget(null)}
                    onConfirm={(reason) => {
                        handleReject(rejectTarget.applicationId, reason);
                        setRejectTarget(null);
                    }}
                    loading={actionLoading}
                />

                {/* Single Revert Modal (from card) */}
                <RevertModal
                    isOpen={!!revertTarget && !checkingPayment}
                    onClose={() => setRevertTarget(null)}
                    onConfirm={() => {
                        handleRevert(revertTarget.applicationId);
                        setRevertTarget(null);
                    }}
                    hasPaid={hasPaidForRevert}
                    loading={actionLoading}
                />

                {/* Bulk Reject Modal */}
                <RejectModal
                    isOpen={showBulkRejectModal}
                    onClose={() => setShowBulkRejectModal(false)}
                    onConfirm={handleBulkReject}
                    isBulk
                    count={selectedIds.length}
                    loading={actionLoading}
                />

                {/* Bulk Revert Modal */}
                <RevertModal
                    isOpen={showBulkRevertModal}
                    onClose={() => setShowBulkRevertModal(false)}
                    onConfirm={handleBulkRevert}
                    isBulk
                    count={selectedIds.length}
                    loading={actionLoading}
                />
            </div>
        );
    }

    // ==================== EVENT LIST VIEW ====================
    return (
        <div className="space-y-5">
            <Toast {...toast} onClose={hideToast} />

            <div>
                <h1 className="text-2xl font-bold text-gray-900">Facility Application Approvals</h1>
                <p className="text-sm text-gray-500 mt-1">Review and manage facility applications by event</p>
            </div>

            {/* Event Filter Tabs */}
            <StatusTabs tabs={eventTabs} selected={eventFilter} onChange={setEventFilter} />

            {/* Events List */}
            {eventsLoading ? (
                <div className="text-center py-16">
                    <div className="w-8 h-8 border-2 border-purple-600 border-t-transparent rounded-full animate-spin mx-auto mb-3" />
                    <p className="text-sm text-gray-500">Loading events...</p>
                </div>
            ) : events.length === 0 ? (
                <div className="bg-white rounded-xl border border-gray-200 p-12 text-center">
                    <Calendar className="w-10 h-10 text-gray-300 mx-auto mb-3" />
                    <p className="text-gray-500 text-sm">No events found</p>
                </div>
            ) : (
                <div className="space-y-3">
                    {events.map((event) => {
                        const eventStatusStyle = facilityApprovalService.getEventStatusStyle(event.eventStatus);
                        return (
                            <div
                                key={event.eventId}
                                onClick={() => setSelectedEvent(event)}
                                className="bg-white rounded-xl border border-gray-200 p-5 hover:border-purple-300 hover:shadow-sm transition-all cursor-pointer group"
                            >
                                <div className="flex items-start gap-3">
                                    <div className="w-10 h-10 bg-purple-100 rounded-xl flex items-center justify-center flex-shrink-0 group-hover:bg-purple-200 transition-colors">
                                        <Calendar className="w-5 h-5 text-purple-600" />
                                    </div>
                                    <div className="flex-1 min-w-0">
                                        <div className="flex items-center justify-between gap-3">
                                            <h3 className="text-sm font-semibold text-gray-900 truncate">{event.eventName}</h3>
                                            <span className={`px-2.5 py-1 rounded-full text-xs font-medium flex-shrink-0 capitalize ${eventStatusStyle}`}>
                        {event.eventStatus}
                      </span>
                                        </div>
                                        <div className="flex items-center gap-1 mt-1">
                                            <MapPin className="w-3.5 h-3.5 text-gray-400" />
                                            <span className="text-xs text-gray-500">{event.eventVenue}</span>
                                        </div>

                                        <div className="mt-3 flex flex-wrap gap-x-4 gap-y-1 text-xs text-gray-500">
                      <span className="flex items-center gap-1">
                        <Calendar className="w-3.5 h-3.5 text-gray-400" />
                        Start: <strong className="text-gray-700">{event.eventStartDate}</strong>
                      </span>
                                            <span className="flex items-center gap-1">
                        <Calendar className="w-3.5 h-3.5 text-gray-400" />
                        End: <strong className="text-gray-700">{event.eventEndDate}</strong>
                      </span>
                                            <span className="flex items-center gap-1">
                        <Users className="w-3.5 h-3.5 text-gray-400" />
                        Total: <strong className="text-gray-700">{event.totalApplications}</strong>
                      </span>
                                        </div>

                                        <div className="mt-3 flex flex-wrap gap-2">
                                            <StatBadge
                                                icon={<Clock className="w-3.5 h-3.5 text-yellow-600" />}
                                                label={`Pending: ${event.pendingCount}`}
                                                className="bg-yellow-50 border-yellow-200 text-yellow-700"
                                            />
                                            <StatBadge
                                                icon={<CheckCircle className="w-3.5 h-3.5 text-green-600" />}
                                                label={`Approved: ${event.approvedCount}`}
                                                className="bg-green-50 border-green-200 text-green-700"
                                            />
                                            <StatBadge
                                                icon={<XCircle className="w-3.5 h-3.5 text-red-500" />}
                                                label={`Rejected: ${event.rejectedCount}`}
                                                className="bg-red-50 border-red-200 text-red-600"
                                            />
                                        </div>
                                    </div>
                                </div>
                            </div>
                        );
                    })}
                </div>
            )}
        </div>
    );
}

function StatBadge({ icon, label, className }) {
    return (
        <div className={`px-3 py-1.5 rounded-lg border flex items-center gap-1.5 text-xs font-medium ${className}`}>
            {icon}
            {label}
        </div>
    );
}