import { useState, useEffect, useContext } from 'react';
import {
    Calendar,
    MapPin,
    Search,
    Filter,
    XCircle,
    Store,
    DollarSign,
    CheckCircle,
    Clock,
    Plus,
    Minus,
    Eye,
    CalendarDays,
    Users,
} from 'lucide-react';
import { AuthContext } from '../context/AuthContext';
import boEventService from '../services/boEventService';
import businessService from '../services/businessService';
import { Toast } from './Toast';

export function BOEvents() {
    const { user } = useContext(AuthContext);

    // ==================== STATE ====================

    // View states
    const [selectedEvent, setSelectedEvent] = useState(null);
    const [showApplicationForm, setShowApplicationForm] = useState(false);
    const [viewingFacility, setViewingFacility] = useState(null);
    const [loadingDetail, setLoadingDetail] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [loading, setLoading] = useState(false);

    // Data states
    const [events, setEvents] = useState([]);
    const [eventDetail, setEventDetail] = useState(null);
    const [myBusinesses, setMyBusinesses] = useState([]);

    // Filter states
    const [searchQuery, setSearchQuery] = useState('');
    const [filterStatus, setFilterStatus] = useState('all');
    const [dateRangeStart, setDateRangeStart] = useState('');
    const [dateRangeEnd, setDateRangeEnd] = useState('');
    const [dateFilterOpen, setDateFilterOpen] = useState(false);

    // Application form states
    const [selectedBusiness, setSelectedBusiness] = useState('');
    const [facilityQuantities, setFacilityQuantities] = useState(new Map());

    // Toast state
    const [toast, setToast] = useState({ isVisible: false, type: 'success', message: '' });

    // ==================== EFFECTS ====================

    useEffect(() => {
        fetchEvents();
    }, [searchQuery, filterStatus]);

    useEffect(() => {
        fetchMyBusinesses();
    }, []);

    // Close date filter popover when clicking outside
    useEffect(() => {
        const handleClickOutside = (e) => {
            if (dateFilterOpen && !e.target.closest('.date-filter-wrapper')) {
                setDateFilterOpen(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, [dateFilterOpen]);

    // ==================== API CALLS ====================

    const fetchEvents = async () => {
        try {
            setLoading(true);
            const data = await boEventService.getEvents({ searchQuery, eventStatus: filterStatus });
            setEvents(data || []);
        } catch (error) {
            showToast('error', error || 'Failed to load events');
        } finally {
            setLoading(false);
        }
    };

    const fetchMyBusinesses = async () => {
        try {
            const response = await businessService.getMyBusinesses();
            // Handle both response shapes
            if (Array.isArray(response)) {
                setMyBusinesses(response);
            } else if (response?.data && Array.isArray(response.data)) {
                setMyBusinesses(response.data);
            } else {
                setMyBusinesses([]);
            }
        } catch (error) {
            // Non-critical error — don't show toast, just set empty
            setMyBusinesses([]);
        }
    };

    const fetchEventDetail = async (eventId) => {
        try {
            setLoadingDetail(true);
            const data = await boEventService.getEventWithFacilities(eventId);
            setEventDetail(data);
        } catch (error) {
            showToast('error', error || 'Failed to load event details');
            setSelectedEvent(null);
        } finally {
            setLoadingDetail(false);
        }
    };

    // ==================== HANDLERS ====================

    const handleViewDetails = (event) => {
        setSelectedEvent(event);
        setFacilityQuantities(new Map());
        setSelectedBusiness('');
        fetchEventDetail(event.eventId);
    };

    const handleCloseDetail = () => {
        setSelectedEvent(null);
        setEventDetail(null);
        setFacilityQuantities(new Map());
        setSelectedBusiness('');
    };

    const handleProceedToApplication = () => {
        setShowApplicationForm(true);
    };

    const handleBackToDetail = () => {
        setShowApplicationForm(false);
    };

    const handleSubmitApplication = async (e) => {
        e.preventDefault();

        if (!selectedBusiness) {
            showToast('error', 'Please select a business');
            return;
        }

        if (facilityQuantities.size === 0) {
            showToast('error', 'Please select at least one facility');
            return;
        }

        try {
            setSubmitting(true);

            const facilities = Array.from(facilityQuantities.entries()).map(
                ([eventFacilityId, quantity]) => ({
                    eventFacilityId: parseInt(eventFacilityId),
                    quantity,
                })
            );

            const payload = {
                businessId: parseInt(selectedBusiness),
                facilities,
            };

            await boEventService.submitApplications(payload);

            // Success
            showToast('success', 'Your application has been successfully submitted!');

            // Reset and close
            setShowApplicationForm(false);
            setSelectedEvent(null);
            setEventDetail(null);
            setFacilityQuantities(new Map());
            setSelectedBusiness('');
        } catch (error) {
            showToast('error', error || 'Failed to submit application');
        } finally {
            setSubmitting(false);
        }
    };

    const updateFacilityQuantity = (eventFacilityId, change) => {
        if (!eventDetail) return;

        const facility = eventDetail.facilities.find(
            (f) => f.eventFacilityId === eventFacilityId
        );
        if (!facility) return;

        const newQuantities = new Map(facilityQuantities);
        const current = newQuantities.get(eventFacilityId) || 0;

        // Max allowed = min(remainingQuota, quantityFacilityAvailable)
        const maxAllowed = Math.min(
            facility.remainingQuota,
            facility.quantityFacilityAvailable
        );
        const newQty = Math.max(0, Math.min(maxAllowed, current + change));

        if (newQty === 0) {
            newQuantities.delete(eventFacilityId);
        } else {
            newQuantities.set(eventFacilityId, newQty);
        }

        setFacilityQuantities(newQuantities);
    };

    // ==================== DATE FILTER HELPERS ====================

    const clearDateFilter = () => {
        setDateRangeStart('');
        setDateRangeEnd('');
        setDateFilterOpen(false);
    };

    const hasDateFilter = dateRangeStart || dateRangeEnd;

    const formatDateDisplay = () => {
        if (!dateRangeStart && !dateRangeEnd) return '';
        if (dateRangeStart && dateRangeEnd) {
            return `${boEventService.formatDate(dateRangeStart)} - ${boEventService.formatDate(dateRangeEnd)}`;
        }
        if (dateRangeStart) return `From ${boEventService.formatDate(dateRangeStart)}`;
        if (dateRangeEnd) return `Until ${boEventService.formatDate(dateRangeEnd)}`;
        return '';
    };

    // Client-side date filter on top of server results
    const filteredEvents = events.filter((event) => {
        if (!dateRangeStart && !dateRangeEnd) return true;
        const eventStart = new Date(event.eventStartDate);
        const eventEnd = new Date(event.eventEndDate);
        if (dateRangeStart && dateRangeEnd) {
            const filterStart = new Date(dateRangeStart);
            const filterEnd = new Date(dateRangeEnd);
            return eventStart <= filterEnd && eventEnd >= filterStart;
        }
        if (dateRangeStart) {
            return eventEnd >= new Date(dateRangeStart);
        }
        if (dateRangeEnd) {
            return eventStart <= new Date(dateRangeEnd);
        }
        return true;
    });

    // ==================== PRICE HELPERS ====================

    const getApplicablePrice = (facility) => {
        if (!facility) return 0;
        // Use applicablePrice from backend (already computed per user category)
        return facility.applicablePrice || 0;
    };

    const calculateTotalPrice = () => {
        if (!eventDetail || !selectedBusiness) return 0;
        let total = 0;
        facilityQuantities.forEach((quantity, eventFacilityId) => {
            const facility = eventDetail.facilities.find(
                (f) => f.eventFacilityId === eventFacilityId
            );
            if (facility) {
                total += parseFloat(getApplicablePrice(facility)) * quantity;
            }
        });
        return total.toFixed(2);
    };

    const getUserCategoryLabel = () => {
        if (!user) return '';
        return user.userCategory === 'STUDENT' ? 'Student Rates Applied' : 'Non-Student Rates Applied';
    };

    // ==================== TOAST HELPER ====================

    const showToast = (type, message) => {
        setToast({ isVisible: true, type, message });
    };

    const hideToast = () => {
        setToast((prev) => ({ ...prev, isVisible: false }));
    };

    // ==================== RENDER ====================

    const isApplicationOpen = eventDetail?.eventApplicationStatus === 'OPEN';
    const hasSelectedFacilities = facilityQuantities.size > 0;

    return (
        <div className="space-y-6">

            {/* Toast */}
            <Toast
                type={toast.type}
                message={toast.message}
                isVisible={toast.isVisible}
                onClose={hideToast}
            />

            {/* Page Header */}
            <div>
                <h1 className="text-2xl font-bold text-gray-900 mb-1">Events</h1>
                <p className="text-gray-600">Browse and apply for upcoming events and facilities</p>
            </div>

            {/* Filter Section */}
            <div className="bg-white rounded-xl border border-gray-100 p-6">
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">

                    {/* Search */}
                    <div className="relative">
                        <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                        <input
                            type="text"
                            placeholder="Search events..."
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                            className="w-full pl-12 pr-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:border-purple-400"
                        />
                    </div>

                    {/* Status Filter */}
                    <div className="relative">
                        <Filter className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400 pointer-events-none z-10" />
                        <select
                            value={filterStatus}
                            onChange={(e) => setFilterStatus(e.target.value)}
                            className="w-full pl-12 pr-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:border-purple-400 bg-white appearance-none cursor-pointer"
                        >
                            <option value="all">All Events</option>
                            <option value="upcoming">Upcoming Events</option>
                            <option value="active">Ongoing Events</option>
                            <option value="completed">Completed Events</option>
                        </select>
                    </div>

                    {/* Date Range Filter */}
                    <div className="relative date-filter-wrapper">
                        <CalendarDays className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400 pointer-events-none z-10" />
                        <input
                            type="text"
                            placeholder="Filter by date range..."
                            value={formatDateDisplay()}
                            readOnly
                            className={`w-full pl-12 pr-4 py-3 rounded-lg border focus:outline-none cursor-pointer transition-colors ${
                                hasDateFilter
                                    ? 'border-purple-400 bg-purple-50 text-purple-900'
                                    : 'border-gray-200 focus:border-purple-400'
                            }`}
                            onClick={() => setDateFilterOpen(!dateFilterOpen)}
                        />
                        {dateFilterOpen && (
                            <div className="absolute left-0 top-full mt-2 bg-white border border-gray-200 rounded-lg shadow-lg p-4 z-20 w-full min-w-[300px]">
                                <div className="space-y-3">
                                    <div>
                                        <label className="block text-sm text-gray-600 mb-1">Start Date</label>
                                        <input
                                            type="date"
                                            value={dateRangeStart}
                                            onChange={(e) => setDateRangeStart(e.target.value)}
                                            max={dateRangeEnd || undefined}
                                            className="w-full px-3 py-2 rounded-lg border border-gray-200 focus:outline-none focus:border-purple-400"
                                        />
                                    </div>
                                    <div>
                                        <label className="block text-sm text-gray-600 mb-1">End Date</label>
                                        <input
                                            type="date"
                                            value={dateRangeEnd}
                                            onChange={(e) => setDateRangeEnd(e.target.value)}
                                            min={dateRangeStart || undefined}
                                            className="w-full px-3 py-2 rounded-lg border border-gray-200 focus:outline-none focus:border-purple-400"
                                        />
                                    </div>
                                </div>
                                <div className="mt-4 flex justify-between gap-2">
                                    <button
                                        type="button"
                                        onClick={clearDateFilter}
                                        className="px-4 py-2 text-sm text-gray-600 hover:text-gray-800 transition-colors"
                                    >
                                        Clear
                                    </button>
                                    <button
                                        type="button"
                                        onClick={() => setDateFilterOpen(false)}
                                        className="px-4 py-2 text-sm bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors"
                                    >
                                        Apply
                                    </button>
                                </div>
                            </div>
                        )}
                    </div>

                </div>
            </div>

            {/* Event Cards */}
            {loading ? (
                <div className="flex items-center justify-center py-16">
                    <div className="w-8 h-8 border-4 border-purple-200 border-t-purple-600 rounded-full animate-spin" />
                </div>
            ) : (
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                    {filteredEvents.length > 0 ? (
                        filteredEvents.map((event) => (
                            <div
                                key={event.eventId}
                                className="bg-white rounded-xl border border-gray-100 p-6 hover:shadow-md transition-shadow space-y-4"
                            >
                                {/* Event Header */}
                                <div>
                                    <div className="flex items-start justify-between mb-2">
                                        <h3 className="text-base font-semibold text-gray-900 flex-1 pr-2">
                                            {event.eventName}
                                        </h3>
                                        <span
                                            className={`px-3 py-1 rounded-full text-xs font-medium capitalize flex-shrink-0 ${boEventService.getStatusStyle(
                                                event.eventStatus
                                            )}`}
                                        >
                      {event.eventStatus === 'active' ? 'Ongoing' : event.eventStatus}
                    </span>
                                    </div>
                                    <p className="text-sm text-gray-600 line-clamp-2">{event.eventDesc}</p>
                                </div>

                                {/* Event Details */}
                                <div className="space-y-2">
                                    <div className="flex items-center gap-2 text-sm text-gray-600">
                                        <MapPin className="w-4 h-4 text-gray-400 flex-shrink-0" />
                                        <span>{event.eventVenue}</span>
                                    </div>
                                    <div className="flex items-center gap-2 text-sm text-gray-600">
                                        <Calendar className="w-4 h-4 text-gray-400 flex-shrink-0" />
                                        <span>
                      {boEventService.formatDate(event.eventStartDate)} -{' '}
                                            {boEventService.formatDate(event.eventEndDate)}
                    </span>
                                    </div>
                                </div>

                                {/* View Details Button */}
                                <button
                                    onClick={() => handleViewDetails(event)}
                                    className="w-full px-4 py-3 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors font-medium"
                                >
                                    View Details
                                </button>
                            </div>
                        ))
                    ) : (
                        <div className="col-span-2 text-center py-16 text-gray-500">
                            No events found matching your criteria
                        </div>
                    )}
                </div>
            )}

            {/* ==================== EVENT DETAIL MODAL ==================== */}
            {selectedEvent && !showApplicationForm && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
                    <div className="bg-white rounded-2xl max-w-4xl w-full max-h-[90vh] overflow-y-auto">

                        {/* Modal Header */}
                        <div className="sticky top-0 bg-white border-b border-gray-200 p-6 flex items-center justify-between z-10">
                            <h2 className="text-xl font-semibold text-gray-900">Event Details</h2>
                            <button
                                onClick={handleCloseDetail}
                                className="w-8 h-8 flex items-center justify-center rounded-lg hover:bg-gray-100 transition-colors"
                            >
                                <XCircle className="w-5 h-5 text-gray-500" />
                            </button>
                        </div>

                        {loadingDetail ? (
                            <div className="flex items-center justify-center py-16">
                                <div className="w-8 h-8 border-4 border-purple-200 border-t-purple-600 rounded-full animate-spin" />
                            </div>
                        ) : eventDetail ? (
                            <div className="p-6 space-y-6">

                                {/* Event Info */}
                                <div className="space-y-4">
                                    <div>
                                        <div className="flex items-start justify-between mb-3">
                                            <h3 className="text-lg font-semibold text-gray-900">
                                                {eventDetail.eventName}
                                            </h3>
                                            <span
                                                className={`px-3 py-1 rounded-full text-sm font-medium capitalize ${boEventService.getStatusStyle(
                                                    eventDetail.eventStatus
                                                )}`}
                                            >
                        {eventDetail.eventStatus === 'active' ? 'Ongoing' : eventDetail.eventStatus}
                      </span>
                                        </div>
                                        <p className="text-gray-600">{eventDetail.eventDesc}</p>
                                    </div>

                                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                        <div className="flex items-start gap-3 p-4 bg-gray-50 rounded-lg">
                                            <MapPin className="w-5 h-5 text-purple-600 mt-0.5 flex-shrink-0" />
                                            <div>
                                                <p className="text-sm text-gray-500 mb-1">Venue</p>
                                                <p className="font-medium text-gray-900">{eventDetail.eventVenue}</p>
                                            </div>
                                        </div>
                                        <div className="flex items-start gap-3 p-4 bg-gray-50 rounded-lg">
                                            <Calendar className="w-5 h-5 text-purple-600 mt-0.5 flex-shrink-0" />
                                            <div>
                                                <p className="text-sm text-gray-500 mb-1">Date & Time</p>
                                                <p className="font-medium text-gray-900">
                                                    {boEventService.formatDateLong(eventDetail.eventStartDate)}
                                                </p>
                                                <p className="font-medium text-gray-900">
                                                    to {boEventService.formatDateLong(eventDetail.eventEndDate)}
                                                </p>
                                            </div>
                                        </div>
                                    </div>

                                    {/* Application Closed Banner */}
                                    {!isApplicationOpen && (
                                        <div className="bg-gray-50 border border-gray-200 rounded-lg p-4 flex items-center gap-3">
                                            <XCircle className="w-5 h-5 text-gray-500 flex-shrink-0" />
                                            <p className="text-sm text-gray-600">
                                                Applications are currently <span className="font-semibold">closed</span> for this event.
                                            </p>
                                        </div>
                                    )}
                                </div>

                                {/* Available Facilities */}
                                <div>
                                    <h3 className="text-base font-semibold text-gray-900 mb-2">
                                        Available Facilities
                                    </h3>
                                    {isApplicationOpen && (
                                        <p className="text-sm text-gray-600 mb-4">
                                            Select the quantity you need for each facility type. You can apply for
                                            multiple facilities at once.
                                        </p>
                                    )}

                                    <div className="space-y-3">
                                        {eventDetail.facilities.map((facility) => {
                                            const quantity = facilityQuantities.get(facility.eventFacilityId) || 0;
                                            const maxAllowed = Math.min(
                                                facility.remainingQuota,
                                                facility.quantityFacilityAvailable
                                            );
                                            const isFullyBooked = facility.quantityFacilityAvailable === 0;
                                            const noQuota = facility.remainingQuota <= 0;
                                            const hasPending = facility.hasPendingApplication;
                                            const canApply = isApplicationOpen && !isFullyBooked && !noQuota && !hasPending;

                                            return (
                                                <div
                                                    key={facility.eventFacilityId}
                                                    className={`border rounded-lg p-5 transition-all ${
                                                        quantity > 0
                                                            ? 'border-purple-300 bg-purple-50'
                                                            : 'border-gray-200'
                                                    }`}
                                                >
                                                    {/* Facility Header */}
                                                    <div className="flex items-start justify-between gap-4 mb-4">
                                                        <div className="flex-1">
                                                            <h4 className="font-semibold text-gray-900 mb-1">
                                                                {facility.facilityName}
                                                            </h4>
                                                            <p className="text-sm text-gray-600">{facility.facilitySize}</p>
                                                            <p className="text-xs text-purple-600 mt-1">
                                                                Max {facility.maxPerBusiness} per business
                                                            </p>
                                                        </div>
                                                        <div className="text-right flex-shrink-0">
                                                            <div className="flex items-center gap-2 text-sm text-gray-600 mb-1">
                                                                <Store className="w-4 h-4" />
                                                                <span>
                                  {facility.quantityFacilityAvailable} available
                                </span>
                                                            </div>
                                                            {isFullyBooked && (
                                                                <span className="text-xs text-red-600 font-medium">
                                  Fully booked
                                </span>
                                                            )}
                                                            {!isFullyBooked && facility.quantityFacilityAvailable < 5 && (
                                                                <span className="text-xs text-orange-600 font-medium">
                                  Limited availability
                                </span>
                                                            )}
                                                        </div>
                                                    </div>

                                                    {/* Prices */}
                                                    <div className="grid grid-cols-2 gap-4 mb-4">
                                                        <div>
                                                            <p className="text-xs text-gray-500 mb-1">Student Price</p>
                                                            <p className="font-medium text-gray-900 flex items-center gap-1">
                                                                <DollarSign className="w-4 h-4" />
                                                                RM {parseFloat(facility.facilityStudentPrice).toFixed(2)}
                                                            </p>
                                                        </div>
                                                        <div>
                                                            <p className="text-xs text-gray-500 mb-1">Non-Student Price</p>
                                                            <p className="font-medium text-gray-900 flex items-center gap-1">
                                                                <DollarSign className="w-4 h-4" />
                                                                RM {parseFloat(facility.facilityNonStudentPrice).toFixed(2)}
                                                            </p>
                                                        </div>
                                                    </div>

                                                    {/* Status tags for pending / no quota */}
                                                    {hasPending && (
                                                        <div className="mb-3 px-3 py-2 bg-yellow-50 border border-yellow-200 rounded-lg">
                                                            <p className="text-xs text-yellow-700 font-medium">
                                                                You have a pending application for this facility. Wait for it to be reviewed before applying again.
                                                            </p>
                                                        </div>
                                                    )}
                                                    {!hasPending && noQuota && !isFullyBooked && (
                                                        <div className="mb-3 px-3 py-2 bg-gray-50 border border-gray-200 rounded-lg">
                                                            <p className="text-xs text-gray-600 font-medium">
                                                                You have reached the maximum quota for this facility.
                                                            </p>
                                                        </div>
                                                    )}

                                                    {/* Bottom row: View Details + Quantity Selector */}
                                                    <div className="flex items-center justify-between pt-4 border-t border-gray-200">
                                                        <button
                                                            type="button"
                                                            onClick={() => setViewingFacility(facility)}
                                                            className="px-4 py-2 text-sm bg-purple-100 text-purple-700 rounded-lg hover:bg-purple-200 transition-colors flex items-center gap-2"
                                                        >
                                                            <Eye className="w-4 h-4" />
                                                            View Details
                                                        </button>

                                                        {isApplicationOpen && (
                                                            <div className="flex items-center gap-3">
                                                                <button
                                                                    type="button"
                                                                    onClick={() =>
                                                                        updateFacilityQuantity(facility.eventFacilityId, -1)
                                                                    }
                                                                    disabled={quantity === 0 || !canApply}
                                                                    className="w-8 h-8 rounded-lg border border-gray-300 flex items-center justify-center hover:bg-gray-100 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                                                                >
                                                                    <Minus className="w-4 h-4 text-gray-600" />
                                                                </button>
                                                                <span className="w-12 text-center font-medium text-gray-900">
                                  {quantity}
                                </span>
                                                                <button
                                                                    type="button"
                                                                    onClick={() =>
                                                                        updateFacilityQuantity(facility.eventFacilityId, 1)
                                                                    }
                                                                    disabled={quantity >= maxAllowed || !canApply}
                                                                    className="w-8 h-8 rounded-lg border border-gray-300 flex items-center justify-center hover:bg-gray-100 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                                                                >
                                                                    <Plus className="w-4 h-4 text-gray-600" />
                                                                </button>
                                                            </div>
                                                        )}
                                                    </div>
                                                </div>
                                            );
                                        })}
                                    </div>
                                </div>

                                {/* Proceed to Application Summary */}
                                {hasSelectedFacilities && isApplicationOpen && (
                                    <div className="bg-purple-50 border border-purple-200 rounded-lg p-4">
                                        <div className="flex items-center justify-between">
                                            <div>
                                                <p className="font-medium text-gray-900 mb-1">
                                                    {facilityQuantities.size}{' '}
                                                    {facilityQuantities.size === 1 ? 'facility' : 'facilities'} selected
                                                </p>
                                                <p className="text-sm text-gray-600">
                                                    Total:{' '}
                                                    {Array.from(facilityQuantities.values()).reduce((a, b) => a + b, 0)}{' '}
                                                    unit(s)
                                                </p>
                                            </div>
                                            <button
                                                onClick={handleProceedToApplication}
                                                className="px-6 py-3 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors font-medium"
                                            >
                                                Proceed to Application
                                            </button>
                                        </div>
                                    </div>
                                )}
                            </div>
                        ) : null}
                    </div>
                </div>
            )}

            {/* ==================== SUBMIT APPLICATION MODAL ==================== */}
            {showApplicationForm && eventDetail && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
                    <div className="bg-white rounded-2xl max-w-3xl w-full max-h-[90vh] overflow-y-auto">

                        {/* Modal Header */}
                        <div className="sticky top-0 bg-white border-b border-gray-200 p-6 flex items-center justify-between z-10">
                            <h2 className="text-xl font-semibold text-gray-900">Submit Application</h2>
                            <button
                                onClick={() => setShowApplicationForm(false)}
                                className="w-8 h-8 flex items-center justify-center rounded-lg hover:bg-gray-100 transition-colors"
                            >
                                <XCircle className="w-5 h-5 text-gray-500" />
                            </button>
                        </div>

                        <form onSubmit={handleSubmitApplication} className="p-6 space-y-6">

                            {/* Event Information */}
                            <div className="bg-purple-50 border border-purple-100 rounded-lg p-4">
                                <h4 className="font-semibold text-gray-900 mb-3">Event Information</h4>
                                <div className="grid grid-cols-2 gap-3 text-sm">
                                    <div>
                                        <p className="text-gray-500">Event</p>
                                        <p className="font-medium text-gray-900">{eventDetail.eventName}</p>
                                    </div>
                                    <div>
                                        <p className="text-gray-500">Venue</p>
                                        <p className="font-medium text-gray-900">{eventDetail.eventVenue}</p>
                                    </div>
                                </div>
                            </div>

                            {/* Selected Facilities */}
                            <div>
                                <h4 className="font-semibold text-gray-900 mb-3">Selected Facilities</h4>
                                <div className="space-y-2">
                                    {Array.from(facilityQuantities.entries()).map(([eventFacilityId, quantity]) => {
                                        const facility = eventDetail.facilities.find(
                                            (f) => f.eventFacilityId === eventFacilityId
                                        );
                                        if (!facility) return null;
                                        return (
                                            <div
                                                key={eventFacilityId}
                                                className="flex items-center justify-between p-3 bg-gray-50 rounded-lg"
                                            >
                                                <div>
                                                    <p className="font-medium text-gray-900">{facility.facilityName}</p>
                                                    <p className="text-sm text-gray-600">{facility.facilitySize}</p>
                                                </div>
                                                <p className="font-medium text-gray-900">Quantity: {quantity}</p>
                                            </div>
                                        );
                                    })}
                                </div>
                            </div>

                            {/* Select Business */}
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    Select Business <span className="text-red-500">*</span>
                                </label>
                                <div className="relative">
                                    <Users className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                                    <select
                                        value={selectedBusiness}
                                        onChange={(e) => setSelectedBusiness(e.target.value)}
                                        className="w-full pl-10 pr-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:border-purple-400 bg-white"
                                        required
                                    >
                                        <option value="">Choose a business</option>
                                        {myBusinesses.map((business) => (
                                            <option key={business.businessId} value={business.businessId}>
                                                {business.businessName}
                                            </option>
                                        ))}
                                    </select>
                                </div>
                                {myBusinesses.length === 0 && (
                                    <p className="text-xs text-red-500 mt-1">
                                        You have no active businesses. Please register a business first.
                                    </p>
                                )}
                            </div>

                            {/* Price Breakdown */}
                            {selectedBusiness && (
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-2">
                                        Price Breakdown
                                    </label>
                                    <div className="bg-gray-50 rounded-lg p-4 space-y-2">
                                        {Array.from(facilityQuantities.entries()).map(([eventFacilityId, quantity]) => {
                                            const facility = eventDetail.facilities.find(
                                                (f) => f.eventFacilityId === eventFacilityId
                                            );
                                            if (!facility) return null;
                                            const unitPrice = parseFloat(getApplicablePrice(facility));
                                            const subtotal = (unitPrice * quantity).toFixed(2);
                                            return (
                                                <div
                                                    key={eventFacilityId}
                                                    className="flex items-center justify-between text-sm"
                                                >
                          <span className="text-gray-600">
                            {facility.facilityName} x {quantity} @ RM {unitPrice.toFixed(2)}
                          </span>
                                                    <span className="font-medium text-gray-900">RM {subtotal}</span>
                                                </div>
                                            );
                                        })}
                                        <div className="pt-2 border-t border-gray-200 flex items-center justify-between">
                                            <div className="flex items-center gap-2">
                                                <DollarSign className="w-5 h-5 text-gray-400" />
                                                <span className="font-medium text-gray-900">Total</span>
                                            </div>
                                            <span className="font-semibold text-gray-900">RM {calculateTotalPrice()}</span>
                                        </div>
                                        <p className="text-xs text-gray-500 pt-1">{getUserCategoryLabel()}</p>
                                    </div>
                                </div>
                            )}

                            {/* Pending Review Info */}
                            <div className="bg-blue-50 border border-blue-100 rounded-lg p-4 flex items-start gap-3">
                                <Clock className="w-5 h-5 text-blue-600 mt-0.5 flex-shrink-0" />
                                <div>
                                    <p className="text-sm text-blue-900">
                                        Your application will be submitted with status:{' '}
                                        <span className="font-semibold">Pending Review</span>
                                    </p>
                                    <p className="text-sm text-blue-700 mt-1">
                                        You will be notified once the MPP administrator reviews your application.
                                    </p>
                                </div>
                            </div>

                            {/* Form Actions */}
                            <div className="flex gap-3 pt-4 border-t border-gray-200">
                                <button
                                    type="button"
                                    onClick={handleBackToDetail}
                                    className="flex-1 px-6 py-3 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors font-medium"
                                >
                                    Back
                                </button>
                                <button
                                    type="submit"
                                    disabled={submitting || !selectedBusiness}
                                    className="flex-1 px-6 py-3 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors font-medium disabled:opacity-50 disabled:cursor-not-allowed"
                                >
                                    {submitting ? 'Submitting...' : 'Submit Application'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* ==================== FACILITY DETAIL MODAL ==================== */}
            {viewingFacility && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-[60]">
                    <div className="bg-white rounded-2xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">

                        {/* Modal Header */}
                        <div className="sticky top-0 bg-white border-b border-gray-200 p-6 flex items-center justify-between z-10">
                            <h2 className="text-xl font-semibold text-gray-900">Facility Details</h2>
                            <button
                                onClick={() => setViewingFacility(null)}
                                className="w-8 h-8 flex items-center justify-center rounded-lg hover:bg-gray-100 transition-colors"
                            >
                                <XCircle className="w-5 h-5 text-gray-500" />
                            </button>
                        </div>

                        <div className="p-6 space-y-6">
                            {/* Facility Image */}
                            <div className="w-full h-64 rounded-lg overflow-hidden">
                                {viewingFacility.facilityImage ? (
                                    <img
                                        src={`http://localhost:8080/uploads/facilities/${viewingFacility.facilityImage}`}
                                        alt={viewingFacility.facilityName}
                                        className="w-full h-full object-cover"
                                    />
                                ) : (
                                    <div className="w-full h-full bg-gradient-to-br from-purple-100 to-purple-50 flex items-center justify-center">
                                        <Store className="w-20 h-20 text-purple-400" />
                                    </div>
                                )}
                            </div>

                            {/* Facility Info */}
                            <div>
                                <h3 className="text-lg font-semibold text-gray-900 mb-4">
                                    {viewingFacility.facilityName}
                                </h3>
                                <div className="grid grid-cols-2 gap-4 mb-6">
                                    <div>
                                        <p className="text-sm text-gray-500 mb-1">Size</p>
                                        <p className="font-medium text-gray-900">{viewingFacility.facilitySize}</p>
                                    </div>
                                    {viewingFacility.facilityType && (
                                        <div>
                                            <p className="text-sm text-gray-500 mb-1">Type</p>
                                            <p className="font-medium text-gray-900">{viewingFacility.facilityType}</p>
                                        </div>
                                    )}
                                    <div>
                                        <p className="text-sm text-gray-500 mb-1">Student Price</p>
                                        <p className="font-medium text-gray-900">
                                            RM {parseFloat(viewingFacility.facilityStudentPrice).toFixed(2)}
                                        </p>
                                    </div>
                                    <div>
                                        <p className="text-sm text-gray-500 mb-1">Non-Student Price</p>
                                        <p className="font-medium text-gray-900">
                                            RM {parseFloat(viewingFacility.facilityNonStudentPrice).toFixed(2)}
                                        </p>
                                    </div>
                                    <div>
                                        <p className="text-sm text-gray-500 mb-1">Availability</p>
                                        <p className="font-medium text-gray-900">
                                            {viewingFacility.quantityFacilityAvailable} available
                                        </p>
                                    </div>
                                    <div>
                                        <p className="text-sm text-gray-500 mb-1">Max Per Business</p>
                                        <p className="font-medium text-gray-900">
                                            {viewingFacility.maxPerBusiness} units
                                        </p>
                                    </div>
                                </div>

                                <div className="space-y-4">
                                    {viewingFacility.facilityDesc && (
                                        <div>
                                            <p className="text-sm text-gray-500 mb-1">Description</p>
                                            <p className="text-gray-900">{viewingFacility.facilityDesc}</p>
                                        </div>
                                    )}
                                    {viewingFacility.facilityUsage && (
                                        <div>
                                            <p className="text-sm text-gray-500 mb-1">Usage Guidelines</p>
                                            <p className="text-gray-900">{viewingFacility.facilityUsage}</p>
                                        </div>
                                    )}
                                    {viewingFacility.facilityRemark && (
                                        <div>
                                            <p className="text-sm text-gray-500 mb-1">Important Notes</p>
                                            <p className="text-gray-900">{viewingFacility.facilityRemark}</p>
                                        </div>
                                    )}
                                </div>
                            </div>
                        </div>

                        {/* Modal Footer */}
                        <div className="border-t border-gray-200 p-6 flex justify-end">
                            <button
                                onClick={() => setViewingFacility(null)}
                                className="px-6 py-3 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition-colors font-medium"
                            >
                                Close
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}