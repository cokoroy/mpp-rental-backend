import { useState, useEffect } from 'react';
import {
  Calendar,
  Plus,
  Eye,
  MapPin,
  Clock,
  ChevronRight,
  ChevronLeft,
  XCircle,
  CheckCircle,
  ChevronDown,
  ChevronUp,
  Store,
  DollarSign,
  Users,
  Package,
  Ban,
  Search,
  Edit,
} from 'lucide-react';
import eventService from '../services/eventService';
import facilityService from '../services/facilityServices';

export function EventManagement() {
  // Modal states
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [currentStep, setCurrentStep] = useState(1);
  const [viewingEvent, setViewingEvent] = useState(null);
  const [editingEvent, setEditingEvent] = useState(null);
  const [viewingFacility, setViewingFacility] = useState(null);
  const [showCancelConfirm, setShowCancelConfirm] = useState(false);
  
  // Message states
  const [showSuccessMessage, setShowSuccessMessage] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');
  const [showErrorMessage, setShowErrorMessage] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [loading, setLoading] = useState(false);
  
  // Search and Filter states
  const [searchQuery, setSearchQuery] = useState('');
  const [filterStatus, setFilterStatus] = useState('all');
  
  // Facility expansion state
  const [expandedFacilities, setExpandedFacilities] = useState(new Set());
  
  // Event type custom field
  const [showOtherEventType, setShowOtherEventType] = useState(false);
  const [customEventType, setCustomEventType] = useState('');
  
  // Form data
  const [eventFormData, setEventFormData] = useState({
    eventName: '',
    eventVenue: '',
    eventStartDate: '',
    eventEndDate: '',
    eventStartTime: '',
    eventEndTime: '',
    eventType: '',
    eventDesc: '',
  });

  const [assignedFacilities, setAssignedFacilities] = useState(new Map());
  
  // Data states
  const [events, setEvents] = useState([]);
  const [facilityTemplates, setFacilityTemplates] = useState([]);

  // Fetch events on component mount and when filters change
  useEffect(() => {
    fetchEvents();
  }, [searchQuery, filterStatus]);

  // Fetch facility templates on mount
  useEffect(() => {
    fetchFacilityTemplates();
  }, []);

  const fetchEvents = async () => {
    try {
      setLoading(true);
      const filters = {
        searchQuery,
        eventStatus: filterStatus,
      };
      
      const data = await eventService.getAllEvents(filters);
      setEvents(data);
    } catch (error) {
      showError(error || 'Failed to fetch events');
    } finally {
      setLoading(false);
    }
  };

  const fetchFacilityTemplates = async () => {
    try {
      const data = await facilityService.getActiveFacilities();
      setFacilityTemplates(data);
    } catch (error) {
      console.error('Failed to fetch facilities:', error);
    }
  };

  const showSuccess = (message) => {
    setSuccessMessage(message);
    setShowSuccessMessage(true);
    setTimeout(() => setShowSuccessMessage(false), 3000);
  };

  const showError = (message) => {
    setErrorMessage(message);
    setShowErrorMessage(true);
    setTimeout(() => setShowErrorMessage(false), 5000);
  };

  const handleCreateEvent = async () => {
    try {
      setLoading(true);

      // Validate form
      if (!eventFormData.eventName || !eventFormData.eventVenue || 
          !eventFormData.eventStartDate || !eventFormData.eventEndDate ||
          !eventFormData.eventStartTime || !eventFormData.eventEndTime ||
          !eventFormData.eventType || !eventFormData.eventDesc) {
        showError('Please fill in all required fields');
        setLoading(false);
        return;
      }

      // Validate at least one facility assigned
      if (assignedFacilities.size === 0) {
        showError('Please assign at least one facility to the event');
        setLoading(false);
        return;
      }

      // Validate all assigned facilities have complete data
      for (const [facilityId, data] of assignedFacilities.entries()) {
        if (!data.quantity || data.quantity < 1 || 
            !data.maxPerBusiness || data.maxPerBusiness < 1 ||
            data.studentPrice === '' || data.studentPrice === null ||
            data.nonStudentPrice === '' || data.nonStudentPrice === null) {
          showError('Please complete all facility assignment fields');
          setLoading(false);
          return;
        }
      }

      // Build request payload
      const facilities = Array.from(assignedFacilities.values()).map(f => ({
        facilityId: parseInt(f.facilityId),
        quantity: parseInt(f.quantity),
        maxPerBusiness: parseInt(f.maxPerBusiness),
        studentPrice: parseFloat(f.studentPrice),
        nonStudentPrice: parseFloat(f.nonStudentPrice),
      }));

      const eventData = {
        eventName: eventFormData.eventName.trim(),
        eventVenue: eventFormData.eventVenue.trim(),
        eventStartDate: eventFormData.eventStartDate,
        eventEndDate: eventFormData.eventEndDate,
        eventStartTime: eventFormData.eventStartTime + ':00',
        eventEndTime: eventFormData.eventEndTime + ':00',
        eventType: showOtherEventType ? customEventType.trim() : eventFormData.eventType,
        eventDesc: eventFormData.eventDesc.trim(),
        facilities: facilities,
      };

      await eventService.createEvent(eventData);
      
      showSuccess('Event created successfully and facilities assigned');
      setShowCreateModal(false);
      resetForm();
      fetchEvents();
    } catch (error) {
      showError(error || 'Failed to create event');
    } finally {
      setLoading(false);
    }
  };

  const handleUpdateEvent = async () => {
    try {
      setLoading(true);

      // Validate at least one facility assigned
      if (assignedFacilities.size === 0) {
        showError('Please assign at least one facility to the event');
        setLoading(false);
        return;
      }

      // Build request payload
      const facilities = Array.from(assignedFacilities.values()).map(f => ({
        eventFacilityId: f.eventFacilityId || null,
        facilityId: parseInt(f.facilityId),
        quantity: parseInt(f.quantity),
        maxPerBusiness: parseInt(f.maxPerBusiness),
        studentPrice: parseFloat(f.studentPrice),
        nonStudentPrice: parseFloat(f.nonStudentPrice),
      }));

      const eventData = {
        eventName: eventFormData.eventName.trim(),
        eventVenue: eventFormData.eventVenue.trim(),
        eventStartDate: eventFormData.eventStartDate || null,
        eventEndDate: eventFormData.eventEndDate || null,
        eventStartTime: eventFormData.eventStartTime + ':00',
        eventEndTime: eventFormData.eventEndTime + ':00',
        eventType: showOtherEventType ? customEventType.trim() : eventFormData.eventType,
        eventDesc: eventFormData.eventDesc.trim(),
        facilities: facilities,
      };

      await eventService.updateEvent(editingEvent.eventId, eventData);
      
      showSuccess('Event updated successfully');
      setShowEditModal(false);
      setEditingEvent(null);
      resetForm();
      fetchEvents();
    } catch (error) {
      showError(error || 'Failed to update event');
    } finally {
      setLoading(false);
    }
  };

  const handleCancelEvent = async () => {
    try {
      await eventService.cancelEvent(viewingEvent.eventId);
      showSuccess('Event cancelled successfully');
      setShowCancelConfirm(false);
      setViewingEvent(null);
      fetchEvents();
    } catch (error) {
      showError(error || 'Failed to cancel event');
    }
  };

  const handleToggleApplicationStatus = async (event) => {
    try {
      await eventService.toggleApplicationStatus(event.eventId);
      showSuccess(
        event.eventApplicationStatus === 'OPEN' 
          ? 'Applications closed for event' 
          : 'Applications opened for event'
      );
      fetchEvents();
    } catch (error) {
      showError(error || 'Failed to toggle application status');
    }
  };

  const handleViewEvent = async (event) => {
    try {
      const eventWithFacilities = await eventService.getEventWithFacilities(event.eventId);
      setViewingEvent(eventWithFacilities);
    } catch (error) {
      showError(error || 'Failed to load event details');
    }
  };

  const handleEditEvent = async (event) => {
    try {
      setLoading(true);
      const eventWithFacilities = await eventService.getEventWithFacilities(event.eventId);
      
      setEditingEvent(eventWithFacilities);
      setEventFormData({
        eventName: eventWithFacilities.eventName,
        eventVenue: eventWithFacilities.eventVenue,
        eventStartDate: eventWithFacilities.eventStartDate,
        eventEndDate: eventWithFacilities.eventEndDate,
        eventStartTime: eventWithFacilities.eventStartTime.substring(0, 5),
        eventEndTime: eventWithFacilities.eventEndTime.substring(0, 5),
        eventType: eventWithFacilities.eventType,
        eventDesc: eventWithFacilities.eventDesc,
      });

      // Check if event type is custom (not in predefined list)
      if (!eventService.eventTypes.includes(eventWithFacilities.eventType)) {
        setShowOtherEventType(true);
        setCustomEventType(eventWithFacilities.eventType);
        setEventFormData(prev => ({ ...prev, eventType: 'Other' }));
      }

      // Load assigned facilities
      const facilitiesMap = new Map();
      eventWithFacilities.facilities.forEach(f => {
        facilitiesMap.set(f.facilityId.toString(), {
          eventFacilityId: f.eventFacilityId,
          facilityId: f.facilityId.toString(),
          quantity: f.quantityFacilityAvailable,
          maxPerBusiness: f.maxPerBusiness,
          studentPrice: f.facilityStudentPrice,
          nonStudentPrice: f.facilityNonStudentPrice,
        });
        setExpandedFacilities(prev => new Set([...prev, f.facilityId.toString()]));
      });
      setAssignedFacilities(facilitiesMap);
      
      setCurrentStep(1);
      setShowEditModal(true);
    } catch (error) {
      showError(error || 'Failed to load event for editing');
    } finally {
      setLoading(false);
    }
  };

  const resetForm = () => {
    setCurrentStep(1);
    setEventFormData({
      eventName: '',
      eventVenue: '',
      eventStartDate: '',
      eventEndDate: '',
      eventStartTime: '',
      eventEndTime: '',
      eventType: '',
      eventDesc: '',
    });
    setAssignedFacilities(new Map());
    setExpandedFacilities(new Set());
    setShowOtherEventType(false);
    setCustomEventType('');
  };

  const toggleFacilityExpansion = (facilityId) => {
    const newExpanded = new Set(expandedFacilities);
    if (newExpanded.has(facilityId)) {
      newExpanded.delete(facilityId);
      // Remove from assigned facilities if collapsed and not in edit mode
      if (!showEditModal) {
        const newAssigned = new Map(assignedFacilities);
        newAssigned.delete(facilityId);
        setAssignedFacilities(newAssigned);
      }
    } else {
      newExpanded.add(facilityId);
    }
    setExpandedFacilities(newExpanded);
  };

  const updateAssignedFacility = (facilityId, field, value) => {
    const newAssigned = new Map(assignedFacilities);
    const current = newAssigned.get(facilityId) || {
      facilityId,
      quantity: '',
      maxPerBusiness: '',
      studentPrice: '',
      nonStudentPrice: '',
    };
    newAssigned.set(facilityId, { ...current, [field]: value });
    setAssignedFacilities(newAssigned);
  };

  const handleNextStep = () => {
    // Validate Step 1 before moving to Step 2
    if (!eventFormData.eventName || !eventFormData.eventVenue || 
        !eventFormData.eventStartDate || !eventFormData.eventEndDate ||
        !eventFormData.eventStartTime || !eventFormData.eventEndTime ||
        !eventFormData.eventType || !eventFormData.eventDesc) {
      showError('Please fill in all required fields');
      return;
    }

    if (showOtherEventType && !customEventType.trim()) {
      showError('Please specify the custom event type');
      return;
    }

    setCurrentStep(2);
  };

  const handleBack = () => {
    if (currentStep === 2) {
      setCurrentStep(1);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Event Management</h1>
        <button
          onClick={() => {
            resetForm();
            setShowCreateModal(true);
          }}
          className="px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors flex items-center gap-2"
        >
          <Plus className="w-4 h-4" />
          New Event
        </button>
      </div>

      {/* Success Message */}
      {showSuccessMessage && (
        <div className="bg-green-50 border border-green-200 rounded-lg p-4 flex items-center gap-3">
          <CheckCircle className="w-5 h-5 text-green-600" />
          <p className="text-green-800">{successMessage}</p>
        </div>
      )}

      {/* Error Message */}
      {showErrorMessage && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-4 flex items-center gap-3">
          <XCircle className="w-5 h-5 text-red-600" />
          <p className="text-red-800">{errorMessage}</p>
        </div>
      )}

      {/* Search and Filter Bar */}
      <div className="bg-white rounded-xl p-4 border border-gray-100">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
              <input
                type="text"
                placeholder="Search events..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full pl-10 pr-4 py-2 rounded-lg border border-gray-200 focus:outline-none focus:border-purple-400"
              />
            </div>
          </div>

          <div>
            <select
              value={filterStatus}
              onChange={(e) => setFilterStatus(e.target.value)}
              className="w-full px-4 py-2 rounded-lg border border-gray-200 focus:outline-none focus:border-purple-400"
            >
              <option value="all">All Events</option>
              <option value="upcoming">Upcoming</option>
              <option value="active">Active</option>
              <option value="completed">Completed</option>
              <option value="cancelled">Cancelled</option>
            </select>
          </div>
        </div>
      </div>

      {/* Loading State */}
      {loading && events.length === 0 && (
        <div className="text-center py-12">
          <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-purple-600"></div>
          <p className="mt-4 text-gray-600">Loading events...</p>
        </div>
      )}

      {/* Empty State */}
      {!loading && events.length === 0 && (
        <div className="text-center py-12">
          <Calendar className="w-16 h-16 text-gray-400 mx-auto mb-4" />
          <p className="text-gray-600">No events found</p>
        </div>
      )}

      {/* Events List */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {events.map((event) => (
          <div
            key={event.eventId}
            className="bg-white rounded-xl p-6 border border-gray-100 hover:shadow-md transition-shadow"
          >
            <div className="flex items-start justify-between mb-4">
              <div className="flex-1">
                <h3 className="text-lg font-semibold text-gray-900 mb-2">{event.eventName}</h3>
                <span className={`inline-block px-3 py-1 rounded-full text-xs capitalize ${eventService.getStatusColor(event.eventStatus)}`}>
                  {event.eventStatus}
                </span>
              </div>
              <div className="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center flex-shrink-0">
                <Calendar className="w-6 h-6 text-purple-600" />
              </div>
            </div>

            <div className="space-y-2 mb-4">
              <p className="text-sm text-gray-600 flex items-start gap-2">
                <MapPin className="w-4 h-4 text-gray-400 flex-shrink-0 mt-0.5" />
                {event.eventVenue}
              </p>
              <p className="text-sm text-gray-600 flex items-center gap-2">
                <Calendar className="w-4 h-4 text-gray-400" />
                {eventService.formatDate(event.eventStartDate)} - {eventService.formatDate(event.eventEndDate)}
              </p>
              <p className="text-sm text-gray-600 flex items-center gap-2">
                <Clock className="w-4 h-4 text-gray-400" />
                {eventService.formatTime(event.eventStartTime)} - {eventService.formatTime(event.eventEndTime)}
              </p>
            </div>

            {/* Application Toggle for Upcoming and Active Events */}
            {(event.eventStatus === 'upcoming' || event.eventStatus === 'active') && (
              <div className="mb-4 p-3 bg-gray-50 rounded-lg border border-gray-200">
                <div className="flex items-center justify-between">
                  <div className="flex-1">
                    <p className="text-sm font-medium text-gray-700">Accept Applications</p>
                    <p className="text-xs text-gray-500 mt-0.5">
                      {event.eventApplicationStatus === 'OPEN' ? 'Applications are open' : 'Applications are closed'}
                    </p>
                  </div>
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      handleToggleApplicationStatus(event);
                    }}
                    className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors ${
                      event.eventApplicationStatus === 'OPEN' ? 'bg-purple-600' : 'bg-gray-300'
                    }`}
                  >
                    <span
                      className={`inline-block h-4 w-4 transform rounded-full bg-white transition-transform ${
                        event.eventApplicationStatus === 'OPEN' ? 'translate-x-6' : 'translate-x-1'
                      }`}
                    />
                  </button>
                </div>
              </div>
            )}

            {/* Action Buttons */}
            <div className="flex gap-2">
              <button
                onClick={() => handleViewEvent(event)}
                className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors flex items-center justify-center gap-2"
              >
                <Eye className="w-4 h-4" />
                View Details
              </button>
              
              {(event.eventStatus === 'upcoming' || event.eventStatus === 'active') && (
                <button
                  onClick={() => handleEditEvent(event)}
                  className="px-4 py-2 bg-purple-100 text-purple-700 rounded-lg hover:bg-purple-200 transition-colors flex items-center justify-center gap-2"
                >
                  <Edit className="w-4 h-4" />
                  Edit
                </button>
              )}
            </div>
          </div>
        ))}
      </div>


      {/* Modals will continue in next message due to length... */}
        {/* Create/Edit Event Modal */}
        {(showCreateModal || showEditModal) && (
            <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
                <div className="bg-white rounded-2xl max-w-4xl w-full max-h-[90vh] overflow-y-auto">
                    <div className="sticky top-0 bg-white border-b border-gray-200 p-6 flex items-center justify-between">
                        <div>
                            <h2 className="text-xl font-bold text-gray-900">
                                {currentStep === 1
                                    ? (showEditModal ? 'Edit Event' : 'Create New Event')
                                    : 'Assign Facilities to Event'}
                            </h2>
                            <p className="text-sm text-gray-500 mt-1">Step {currentStep} of 2</p>
                        </div>
                        <button
                            onClick={() => {
                                showCreateModal ? setShowCreateModal(false) : setShowEditModal(false);
                                resetForm();
                            }}
                            className="w-8 h-8 flex items-center justify-center rounded-lg hover:bg-gray-100 transition-colors"
                        >
                            <XCircle className="w-5 h-5 text-gray-500" />
                        </button>
                    </div>

                    <div className="p-6">
                        {/* Step 1: Event Details */}
                        {currentStep === 1 && (
                            <div className="space-y-6">
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-2">
                                        Event Title <span className="text-red-500">*</span>
                                    </label>
                                    <input
                                        type="text"
                                        value={eventFormData.eventName}
                                        onChange={(e) => setEventFormData({ ...eventFormData, eventName: e.target.value })}
                                        placeholder="e.g., Spring Bazaar 2024"
                                        className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:border-purple-400"
                                        required
                                    />
                                </div>

                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-2">
                                        Event Description <span className="text-red-500">*</span>
                                    </label>
                                    <textarea
                                        value={eventFormData.eventDesc}
                                        onChange={(e) => setEventFormData({ ...eventFormData, eventDesc: e.target.value })}
                                        placeholder="Describe the event..."
                                        rows={4}
                                        className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:border-purple-400 resize-none"
                                        required
                                    />
                                </div>

                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-2">
                                        Venue <span className="text-red-500">*</span>
                                    </label>
                                    <div className="relative">
                                        <MapPin className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                                        <input
                                            type="text"
                                            value={eventFormData.eventVenue}
                                            onChange={(e) => setEventFormData({ ...eventFormData, eventVenue: e.target.value })}
                                            placeholder="e.g., Main Campus Plaza"
                                            className="w-full pl-10 pr-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:border-purple-400"
                                            required
                                        />
                                    </div>
                                </div>

                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-2">
                                        Event Type <span className="text-red-500">*</span>
                                    </label>
                                    <select
                                        value={eventFormData.eventType}
                                        onChange={(e) => {
                                            const value = e.target.value;
                                            setEventFormData({ ...eventFormData, eventType: value });
                                            setShowOtherEventType(value === 'Other');
                                            if (value !== 'Other') {
                                                setCustomEventType('');
                                            }
                                        }}
                                        className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:border-purple-400"
                                        required
                                    >
                                        <option value="">Select event type</option>
                                        {eventService.eventTypes.map((type) => (
                                            <option key={type} value={type}>
                                                {type}
                                            </option>
                                        ))}
                                    </select>
                                </div>

                                {showOtherEventType && (
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-2">
                                            Specify Event Type <span className="text-red-500">*</span>
                                        </label>
                                        <input
                                            type="text"
                                            value={customEventType}
                                            onChange={(e) => setCustomEventType(e.target.value)}
                                            placeholder="Enter custom event type"
                                            className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:border-purple-400"
                                            required
                                        />
                                    </div>
                                )}

                                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-2">
                                            Start Date <span className="text-red-500">*</span>
                                        </label>
                                        <input
                                            type="date"
                                            value={eventFormData.eventStartDate}
                                            onChange={(e) => setEventFormData({ ...eventFormData, eventStartDate: e.target.value })}
                                            className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:border-purple-400"
                                            disabled={showEditModal && editingEvent?.eventStatus === 'active'}
                                            required
                                        />
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-2">
                                            End Date <span className="text-red-500">*</span>
                                        </label>
                                        <input
                                            type="date"
                                            value={eventFormData.eventEndDate}
                                            onChange={(e) => setEventFormData({ ...eventFormData, eventEndDate: e.target.value })}
                                            className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:border-purple-400"
                                            disabled={showEditModal && editingEvent?.eventStatus === 'active'}
                                            required
                                        />
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-2">
                                            Start Time <span className="text-red-500">*</span>
                                        </label>
                                        <input
                                            type="time"
                                            value={eventFormData.eventStartTime}
                                            onChange={(e) => setEventFormData({ ...eventFormData, eventStartTime: e.target.value })}
                                            className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:border-purple-400"
                                            required
                                        />
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-2">
                                            End Time <span className="text-red-500">*</span>
                                        </label>
                                        <input
                                            type="time"
                                            value={eventFormData.eventEndTime}
                                            onChange={(e) => setEventFormData({ ...eventFormData, eventEndTime: e.target.value })}
                                            className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:border-purple-400"
                                            required
                                        />
                                    </div>
                                </div>

                                <div className="pt-4 border-t border-gray-200">
                                    <button
                                        onClick={handleNextStep}
                                        className="w-full px-6 py-3 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors flex items-center justify-center gap-2"
                                    >
                                        Next: Assign Facilities
                                        <ChevronRight className="w-4 h-4" />
                                    </button>
                                </div>
                            </div>
                        )}

                        {/* Step 2: Assign Facilities - SEE NEXT SECTION */}
                        {/* Add this inside the currentStep === 2 condition */}
                        {currentStep === 2 && (
                            <div className="space-y-6">
                                <p className="text-gray-600">
                                    {showEditModal
                                        ? 'Edit existing facilities or add new ones for this event.'
                                        : 'Select facility templates and configure them for this event. Click on a facility to expand and set details.'}
                                </p>

                                <div className="space-y-3">
                                    {facilityTemplates.map((facility) => {
                                        const isExpanded = expandedFacilities.has(facility.facilityId.toString());
                                        const assignedData = assignedFacilities.get(facility.facilityId.toString());
                                        const isAssigned = assignedData && assignedData.quantity > 0;

                                        return (
                                            <div
                                                key={facility.facilityId}
                                                className={`border rounded-lg transition-all ${
                                                    isExpanded ? 'border-purple-300 bg-purple-50' : 'border-gray-200 bg-white'
                                                }`}
                                            >
                                                {/* Facility Header */}
                                                <div className="p-4 flex items-center justify-between">
                                                    <button
                                                        onClick={() => toggleFacilityExpansion(facility.facilityId.toString())}
                                                        className="flex-1 flex items-center gap-3 text-left"
                                                    >
                                                        <div
                                                            className={`w-10 h-10 rounded-lg flex items-center justify-center ${
                                                                isExpanded ? 'bg-purple-600' : 'bg-purple-100'
                                                            }`}
                                                        >
                                                            <Store className={`w-5 h-5 ${isExpanded ? 'text-white' : 'text-purple-600'}`} />
                                                        </div>
                                                        <div className="flex-1">
                                                            <p className="font-medium text-gray-900">{facility.facilityName}</p>
                                                            <p className="text-sm text-gray-500">
                                                                {facility.facilitySize} • {facility.facilityType}
                                                            </p>
                                                        </div>
                                                        {isExpanded ? (
                                                            <ChevronUp className="w-5 h-5 text-gray-400" />
                                                        ) : (
                                                            <ChevronDown className="w-5 h-5 text-gray-400" />
                                                        )}
                                                    </button>
                                                    <button
                                                        onClick={(e) => {
                                                            e.stopPropagation();
                                                            setViewingFacility(facility);
                                                        }}
                                                        className="ml-2 px-3 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors flex items-center gap-1 text-sm"
                                                    >
                                                        <Eye className="w-4 h-4" />
                                                        <span className="hidden sm:inline">Details</span>
                                                    </button>
                                                </div>

                                                {/* Expanded Facility Configuration */}
                                                {isExpanded && (
                                                    <div className="p-4 border-t border-purple-200 bg-white space-y-4">
                                                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                                            <div>
                                                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                                                    Quantity <span className="text-red-500">*</span>
                                                                </label>
                                                                <div className="relative">
                                                                    <Package className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400" />
                                                                    <input
                                                                        type="number"
                                                                        value={assignedData?.quantity || ''}
                                                                        onChange={(e) =>
                                                                            updateAssignedFacility(
                                                                                facility.facilityId.toString(),
                                                                                'quantity',
                                                                                e.target.value
                                                                            )
                                                                        }
                                                                        placeholder="0"
                                                                        min="0"
                                                                        className="w-full pl-10 pr-4 py-2 rounded-lg border border-gray-200 focus:outline-none focus:border-purple-400"
                                                                    />
                                                                </div>
                                                                <p className="text-xs text-gray-500 mt-1">Total units available</p>
                                                            </div>

                                                            <div>
                                                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                                                    Max per Business <span className="text-red-500">*</span>
                                                                </label>
                                                                <div className="relative">
                                                                    <Users className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400" />
                                                                    <input
                                                                        type="number"
                                                                        value={assignedData?.maxPerBusiness || ''}
                                                                        onChange={(e) =>
                                                                            updateAssignedFacility(
                                                                                facility.facilityId.toString(),
                                                                                'maxPerBusiness',
                                                                                e.target.value
                                                                            )
                                                                        }
                                                                        placeholder="0"
                                                                        min="0"
                                                                        className="w-full pl-10 pr-4 py-2 rounded-lg border border-gray-200 focus:outline-none focus:border-purple-400"
                                                                    />
                                                                </div>
                                                                <p className="text-xs text-gray-500 mt-1">Maximum units per business</p>
                                                            </div>

                                                            <div>
                                                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                                                    Student Price (RM) <span className="text-red-500">*</span>
                                                                </label>
                                                                <div className="relative">
                                                                    <DollarSign className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400" />
                                                                    <input
                                                                        type="number"
                                                                        value={assignedData?.studentPrice || ''}
                                                                        onChange={(e) =>
                                                                            updateAssignedFacility(
                                                                                facility.facilityId.toString(),
                                                                                'studentPrice',
                                                                                e.target.value
                                                                            )
                                                                        }
                                                                        placeholder={facility.facilityBaseStudentPrice}
                                                                        min="0"
                                                                        step="0.01"
                                                                        className="w-full pl-10 pr-4 py-2 rounded-lg border border-gray-200 focus:outline-none focus:border-purple-400"
                                                                    />
                                                                </div>
                                                                <p className="text-xs text-gray-500 mt-1">
                                                                    Default: RM {facility.facilityBaseStudentPrice}
                                                                </p>
                                                            </div>

                                                            <div>
                                                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                                                    Non-Student Price (RM) <span className="text-red-500">*</span>
                                                                </label>
                                                                <div className="relative">
                                                                    <DollarSign className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400" />
                                                                    <input
                                                                        type="number"
                                                                        value={assignedData?.nonStudentPrice || ''}
                                                                        onChange={(e) =>
                                                                            updateAssignedFacility(
                                                                                facility.facilityId.toString(),
                                                                                'nonStudentPrice',
                                                                                e.target.value
                                                                            )
                                                                        }
                                                                        placeholder={facility.facilityBaseNonstudentPrice}
                                                                        min="0"
                                                                        step="0.01"
                                                                        className="w-full pl-10 pr-4 py-2 rounded-lg border border-gray-200 focus:outline-none focus:border-purple-400"
                                                                    />
                                                                </div>
                                                                <p className="text-xs text-gray-500 mt-1">
                                                                    Default: RM {facility.facilityBaseNonstudentPrice}
                                                                </p>
                                                            </div>
                                                        </div>
                                                    </div>
                                                )}
                                            </div>
                                        );
                                    })}
                                </div>

                                <div className="pt-4 border-t border-gray-200 flex gap-3">
                                    <button
                                        onClick={handleBack}
                                        className="flex-1 px-6 py-3 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors flex items-center justify-center gap-2"
                                    >
                                        <ChevronLeft className="w-4 h-4" />
                                        Back
                                    </button>
                                    <button
                                        onClick={showEditModal ? handleUpdateEvent : handleCreateEvent}
                                        disabled={loading}
                                        className="flex-1 px-6 py-3 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors disabled:bg-gray-400"
                                    >
                                        {loading ? 'Processing...' : showEditModal ? 'Update Event' : 'Create Event'}
                                    </button>
                                </div>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        )}

        {/* View Event Modal */}
        {viewingEvent && (
            <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
                <div className="bg-white rounded-2xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
                    <div className="sticky top-0 bg-white border-b border-gray-200 p-6 flex items-center justify-between">
                        <h2 className="text-xl font-bold text-gray-900">Event Details</h2>
                        <div className="flex items-center gap-2">
                            {viewingEvent.eventStatus === 'upcoming' && (
                                <button
                                    onClick={() => setShowCancelConfirm(true)}
                                    className="px-4 py-2 bg-red-100 text-red-700 rounded-lg hover:bg-red-200 transition-colors flex items-center gap-2 text-sm"
                                >
                                    <Ban className="w-4 h-4" />
                                    Cancel Event
                                </button>
                            )}
                            <button
                                onClick={() => setViewingEvent(null)}
                                className="w-8 h-8 flex items-center justify-center rounded-lg hover:bg-gray-100 transition-colors"
                            >
                                <XCircle className="w-5 h-5 text-gray-500" />
                            </button>
                        </div>
                    </div>

                    <div className="p-6 space-y-6">
                        {/* Event Info */}
                        <div>
                            <h3 className="text-lg font-semibold text-gray-900 mb-2">
                                {viewingEvent.eventName}
                            </h3>
                            <span
                                className={`inline-block px-3 py-1 rounded-full text-sm capitalize ${eventService.getStatusColor(
                                    viewingEvent.eventStatus
                                )}`}
                            >
            {viewingEvent.eventStatus}
          </span>
                        </div>

                        <div className="space-y-4">
                            <div>
                                <p className="text-sm text-gray-500 mb-1">Description</p>
                                <p className="text-gray-900">{viewingEvent.eventDesc}</p>
                            </div>

                            <div>
                                <p className="text-sm text-gray-500 mb-1">Event Type</p>
                                <p className="text-gray-900">{viewingEvent.eventType}</p>
                            </div>

                            <div>
                                <p className="text-sm text-gray-500 mb-1">Venue</p>
                                <p className="text-gray-900 flex items-center gap-2">
                                    <MapPin className="w-4 h-4 text-gray-400" />
                                    {viewingEvent.eventVenue}
                                </p>
                            </div>

                            <div className="grid grid-cols-2 gap-4">
                                <div>
                                    <p className="text-sm text-gray-500 mb-1">Start Date</p>
                                    <p className="text-gray-900">
                                        {eventService.formatDate(viewingEvent.eventStartDate)}
                                    </p>
                                </div>
                                <div>
                                    <p className="text-sm text-gray-500 mb-1">End Date</p>
                                    <p className="text-gray-900">
                                        {eventService.formatDate(viewingEvent.eventEndDate)}
                                    </p>
                                </div>
                                <div>
                                    <p className="text-sm text-gray-500 mb-1">Start Time</p>
                                    <p className="text-gray-900">
                                        {eventService.formatTime(viewingEvent.eventStartTime)}
                                    </p>
                                </div>
                                <div>
                                    <p className="text-sm text-gray-500 mb-1">End Time</p>
                                    <p className="text-gray-900">
                                        {eventService.formatTime(viewingEvent.eventEndTime)}
                                    </p>
                                </div>
                                <div className="col-span-2">
                                    <p className="text-sm text-gray-500 mb-1">Created Date</p>
                                    <p className="text-gray-900">
                                        {eventService.formatDate(
                                            viewingEvent.eventCreateAt?.split('T')[0]
                                        )}
                                    </p>
                                </div>
                            </div>
                        </div>

                        {/* Assigned Facilities Section */}
                        {viewingEvent.facilities && viewingEvent.facilities.length > 0 && (
                            <div>
                                <h4 className="text-sm font-semibold text-gray-700 mb-3 pb-2 border-b">
                                    Assigned Facilities
                                </h4>
                                <div className="space-y-3">
                                    {viewingEvent.facilities.map((facility) => (
                                        <div
                                            key={facility.eventFacilityId}
                                            className="bg-gray-50 rounded-lg p-4 border border-gray-200"
                                        >
                                            <div className="flex items-start gap-3 mb-3">
                                                <div className="w-10 h-10 bg-purple-100 rounded-lg flex items-center justify-center flex-shrink-0">
                                                    <Store className="w-5 h-5 text-purple-600" />
                                                </div>
                                                <div className="flex-1">
                                                    <p className="font-medium text-gray-900">{facility.facilityName}</p>
                                                    <p className="text-sm text-gray-500">
                                                        {facility.facilitySize} • {facility.facilityType}
                                                    </p>
                                                </div>
                                            </div>
                                            <div className="grid grid-cols-2 gap-3 text-sm">
                                                <div>
                                                    <p className="text-gray-500">Quantity</p>
                                                    <p className="font-medium text-gray-900">
                                                        {facility.quantityFacilityAvailable} units
                                                    </p>
                                                </div>
                                                <div>
                                                    <p className="text-gray-500">Max per Business</p>
                                                    <p className="font-medium text-gray-900">
                                                        {facility.maxPerBusiness} units
                                                    </p>
                                                </div>
                                                <div>
                                                    <p className="text-gray-500">Student Price</p>
                                                    <p className="font-medium text-gray-900">
                                                        RM {facility.facilityStudentPrice}
                                                    </p>
                                                </div>
                                                <div>
                                                    <p className="text-gray-500">Non-Student Price</p>
                                                    <p className="font-medium text-gray-900">
                                                        RM {facility.facilityNonStudentPrice}
                                                    </p>
                                                </div>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        )}

        {/* Cancel Event Confirmation Dialog */}
        {showCancelConfirm && viewingEvent && (
            <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-[60]">
                <div className="bg-white rounded-2xl max-w-md w-full p-6">
                    <div className="flex items-center gap-3 mb-4">
                        <div className="w-12 h-12 bg-red-100 rounded-full flex items-center justify-center">
                            <Ban className="w-6 h-6 text-red-600" />
                        </div>
                        <div>
                            <h3 className="text-lg font-semibold text-gray-900">Cancel Event</h3>
                            <p className="text-sm text-gray-500">This action cannot be undone</p>
                        </div>
                    </div>

                    <p className="text-gray-600 mb-6">
                        Are you sure you want to cancel{' '}
                        <span className="font-medium">"{viewingEvent.eventName}"</span>? All
                        facility bookings and applications for this event will be affected.
                    </p>

                    <div className="flex gap-3">
                        <button
                            onClick={() => setShowCancelConfirm(false)}
                            className="flex-1 px-4 py-3 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
                        >
                            Keep Event
                        </button>
                        <button
                            onClick={handleCancelEvent}
                            className="flex-1 px-4 py-3 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors flex items-center justify-center gap-2"
                        >
                            <Ban className="w-4 h-4" />
                            Cancel Event
                        </button>
                    </div>
                </div>
            </div>
        )}

        {/* View Facility Details Modal */}
        {viewingFacility && (
            <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-[60]">
                <div className="bg-white rounded-2xl max-w-lg w-full max-h-[90vh] overflow-y-auto">
                    <div className="sticky top-0 bg-white border-b border-gray-200 p-6 flex items-center justify-between">
                        <h2 className="text-xl font-bold text-gray-900">Facility Details</h2>
                        <button
                            onClick={() => setViewingFacility(null)}
                            className="w-8 h-8 flex items-center justify-center rounded-lg hover:bg-gray-100 transition-colors"
                        >
                            <XCircle className="w-5 h-5 text-gray-500" />
                        </button>
                    </div>

                    <div className="p-6 space-y-6">
                        {/* Facility Icon */}
                        <div className="w-full h-64 rounded-lg overflow-hidden">
                            {viewingFacility.facilityImage ? (
                                <img
                                    src={facilityService.getFacilityImageUrl(viewingFacility.facilityImage)}
                                    alt={viewingFacility.facilityName}
                                    className="w-full h-full object-cover"
                                />
                            ) : (
                                <div className="w-full h-full bg-gradient-to-br from-purple-100 to-purple-50 flex items-center justify-center">
                                    <Store className="w-20 h-20 text-purple-400" />
                                </div>
                            )}
                        </div>


                        {/*<div className="w-full h-32 bg-gradient-to-br from-purple-100 to-purple-50 rounded-lg flex items-center justify-center">*/}
                        {/*    <Store className="w-16 h-16 text-purple-600" />*/}
                        {/*</div>*/}

                        {/* Facility Information */}
                        <div>
                            <h3 className="text-lg font-semibold text-gray-900 mb-4">
                                {viewingFacility.facilityName}
                            </h3>
                            <div className="grid grid-cols-2 gap-4 mb-6">
                                <div>
                                    <p className="text-sm text-gray-500 mb-1">Facility Size</p>
                                    <p className="font-medium text-gray-900">{viewingFacility.facilitySize}</p>
                                </div>
                                <div>
                                    <p className="text-sm text-gray-500 mb-1">Facility Type</p>
                                    <p className="font-medium text-gray-900">{viewingFacility.facilityType}</p>
                                </div>
                                <div>
                                    <p className="text-sm text-gray-500 mb-1">Default Student Price</p>
                                    <p className="font-medium text-gray-900">
                                        RM {viewingFacility.facilityBaseStudentPrice}
                                    </p>
                                </div>
                                <div>
                                    <p className="text-sm text-gray-500 mb-1">Default Non-Student Price</p>
                                    <p className="font-medium text-gray-900">
                                        RM {viewingFacility.facilityBaseNonstudentPrice}
                                    </p>
                                </div>
                            </div>

                            {viewingFacility.facilityDesc && (
                                <div className="mb-4">
                                    <p className="text-sm text-gray-500 mb-2">Description</p>
                                    <p className="text-gray-900">{viewingFacility.facilityDesc}</p>
                                </div>
                            )}

                            {viewingFacility.facility_usage && (
                                <div>
                                    <p className="text-sm text-gray-500 mb-2">Usage Guidelines</p>
                                    <p className="text-gray-900">{viewingFacility.facility_usage}</p>
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            </div>
        )}
    </div>
  );
}
