import { useState, useEffect } from 'react';
import {
  Store,
  Plus,
  Eye,
  Edit,
  Upload,
  XCircle,
  Save,
  DollarSign,
  Maximize2,
  Tag,
  CheckCircle,
  Power,
  PowerOff,
  X,
  Search,
  Trash2,
  AlertCircle
} from 'lucide-react';
import facilityService from '../services/facilityServices';

export function FacilityManagement() {
  const [showAddModal, setShowAddModal] = useState(false);
  const [editingFacility, setEditingFacility] = useState(null);
  const [viewingFacility, setViewingFacility] = useState(null);
  const [imageFile, setImageFile] = useState(null);
  const [imagePreview, setImagePreview] = useState(null);
  const [fullscreenImage, setFullscreenImage] = useState(null);
  const [showSuccessMessage, setShowSuccessMessage] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');
  const [showErrorMessage, setShowErrorMessage] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [loading, setLoading] = useState(false);
  
  // Search and Filter states
  const [searchQuery, setSearchQuery] = useState('');
  const [filterType, setFilterType] = useState('all');
  const [filterSize, setFilterSize] = useState('all');
  const [filterStatus, setFilterStatus] = useState('all');
  
  // Other option states
  const [showOtherType, setShowOtherType] = useState(false);
  const [showOtherUsage, setShowOtherUsage] = useState(false);
  const [showOtherSize, setShowOtherSize] = useState(false);
  const [customType, setCustomType] = useState('');
  const [customUsage, setCustomUsage] = useState('');
  const [customSize, setCustomSize] = useState('');
  
  const [formData, setFormData] = useState({
    facilityName: '',
    facilitySize: 'Medium',
    facilityType: '',
    facilityDesc: '',
    usage: '',
    remark: '',
    facilityBaseStudentPrice: '',
    facilityBaseNonstudentPrice: '',
    facilityStatus: 'active',
  });

  const [facilities, setFacilities] = useState([]);

  const facilityTypes = [
    'Stall',
    'Booth',
    'Kiosk',
    'Counter',
    'Other',
  ];

  const facilityUsages = [
    'Food vendors - ready-to-eat meals and beverages',
    'Retail - clothing, accessories, and merchandise',
    'Artisan crafts - handmade products and artwork',
    'Service providers - repairs, consultations, and information',
    'Technology - electronics and digital services',
    'Fashion and accessories - clothing and jewelry',
    'Health and beauty - wellness products and services',
    'General merchandise and mixed products',
    'Other',
  ];

  // Fetch facilities on component mount and when filters change
  useEffect(() => {
    fetchFacilities();
  }, [searchQuery, filterType, filterSize, filterStatus]);

  const fetchFacilities = async () => {
    try {
      setLoading(true);
      const filters = {
        searchQuery,
        facilityType: filterType,
        facilitySize: filterSize,
        facilityStatus: filterStatus,
      };
      
      const data = await facilityService.getAllFacilities(filters);
      setFacilities(data);
    } catch (error) {
      showError(error || 'Failed to fetch facilities');
    } finally {
      setLoading(false);
    }
  };

  const handleImageChange = (e) => {
    const file = e.target.files?.[0];
    if (file) {
      // Validate file type
      if (!file.type.startsWith('image/')) {
        showError('Please select an image file');
        return;
      }
      
      // Validate file size (5MB)
      if (file.size > 5 * 1024 * 1024) {
        showError('Image size must not exceed 5MB');
        return;
      }
      
      setImageFile(file);
      
      // Create preview
      const reader = new FileReader();
      reader.onload = (e) => {
        setImagePreview(e.target?.result);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    try {
      setLoading(true);
      
      // Prepare facility data
      const facilityData = {
        facilityName: formData.facilityName,
        facilitySize: formData.facilitySize,
        facilityType: formData.facilityType,
        facilityDesc: formData.facilityDesc,
        usage: formData.usage,
        remark: formData.remark || '',
        facilityBaseStudentPrice: parseFloat(formData.facilityBaseStudentPrice),
        facilityBaseNonstudentPrice: parseFloat(formData.facilityBaseNonstudentPrice),
        facilityStatus: formData.facilityStatus,
      };

      let response;
      if (editingFacility) {
        // Update existing facility
        // Pass imageFile (can be null if no new image, or actual file if new image selected)
        response = await facilityService.updateFacility(
          editingFacility.facilityId,
          facilityData,
          imageFile, // This will be null if user removed image and didn't add new one
          !imagePreview // Pass flag to indicate if image should be removed
        );
        showSuccess('Facility updated successfully');
      } else {
        // Create new facility
        // Only pass imageFile if it exists (user selected an image)
        response = await facilityService.createFacility(
          facilityData, 
          imageFile // Will be null if no image selected
        );
        showSuccess('Facility created successfully');
      }
      
      // Refresh facility list
      await fetchFacilities();
      
      // Close modal and reset form
      closeModal();
    } catch (error) {
      showError(error || 'Failed to save facility');
    } finally {
      setLoading(false);
    }
  };

  const handleEdit = (facility) => {
    setEditingFacility(facility);
    setFormData({
      facilityName: facility.facilityName,
      facilitySize: facility.facilitySize,
      facilityType: facility.facilityType,
      facilityDesc: facility.facilityDesc,
      usage: facility.usage,
      remark: facility.remark || '',
      facilityBaseStudentPrice: facility.facilityBaseStudentPrice.toString(),
      facilityBaseNonstudentPrice: facility.facilityBaseNonstudentPrice.toString(),
      facilityStatus: facility.facilityStatus,
    });
    
    // Set image preview if facility has image
    if (facility.facilityImage) {
      setImagePreview(facilityService.getFacilityImageUrl(facility.facilityImage));
    }
    
    setShowAddModal(true);
  };

  const toggleStatus = async (facility) => {
    try {
      setLoading(true);
      await facilityService.toggleFacilityStatus(facility.facilityId);
      
      const newStatus = facility.facilityStatus === 'active' ? 'inactive' : 'active';
      showSuccess(`Facility ${newStatus === 'active' ? 'activated' : 'deactivated'} successfully`);
      
      // Refresh facility list
      await fetchFacilities();
    } catch (error) {
      showError(error || 'Failed to update facility status');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (facilityId) => {
    if (window.confirm('Are you sure you want to delete this facility? This action cannot be undone.')) {
      try {
        setLoading(true);
        await facilityService.deleteFacility(facilityId);
        showSuccess('Facility deleted successfully');
        
        // Refresh facility list
        await fetchFacilities();
        
        // Close modals
        setShowAddModal(false);
        setEditingFacility(null);
      } catch (error) {
        showError(error || 'Failed to delete facility');
      } finally {
        setLoading(false);
      }
    }
  };

  const closeModal = () => {
    setShowAddModal(false);
    setEditingFacility(null);
    setImageFile(null);
    setImagePreview(null);
    setShowOtherType(false);
    setShowOtherUsage(false);
    setShowOtherSize(false);
    setCustomType('');
    setCustomUsage('');
    setCustomSize('');
    setFormData({
      facilityName: '',
      facilitySize: 'Medium',
      facilityType: '',
      facilityDesc: '',
      usage: '',
      remark: '',
      facilityBaseStudentPrice: '',
      facilityBaseNonstudentPrice: '',
      facilityStatus: 'active',
    });
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

  const getSizeColor = (size) => {
    switch (size) {
      case 'Small':
        return 'bg-blue-50 text-blue-700 border-blue-200';
      case 'Medium':
        return 'bg-purple-50 text-purple-700 border-purple-200';
      case 'Large':
        return 'bg-pink-50 text-pink-700 border-pink-200';
      default:
        return 'bg-gray-50 text-gray-700 border-gray-200';
    }
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { day: 'numeric', month: 'short', year: 'numeric' });
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-gray-900">Facility Management</h1>
        <button
          onClick={() => {
            closeModal();
            setShowAddModal(true);
          }}
          className="px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors flex items-center gap-2"
        >
          <Plus className="w-4 h-4" />
          Add Facility
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
          <AlertCircle className="w-5 h-5 text-red-600" />
          <p className="text-red-800">{errorMessage}</p>
        </div>
      )}

      {/* Search and Filter Bar */}
      <div className="bg-white rounded-xl p-4 border border-gray-100">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          {/* Search */}
          <div className="md:col-span-1">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
              <input
                type="text"
                placeholder="Search facilities..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full pl-10 pr-4 py-2 rounded-lg border border-gray-200 focus:outline-none focus:border-purple-400"
              />
            </div>
          </div>

          {/* Filter by Type */}
          <div>
            <select
              value={filterType}
              onChange={(e) => setFilterType(e.target.value)}
              className="w-full px-4 py-2 rounded-lg border border-gray-200 focus:outline-none focus:border-purple-400"
            >
              <option value="all">All Types</option>
              {facilityTypes.filter(type => type !== 'Other').map((type) => (
                <option key={type} value={type}>
                  {type}
                </option>
              ))}
            </select>
          </div>

          {/* Filter by Size */}
          <div>
            <select
              value={filterSize}
              onChange={(e) => setFilterSize(e.target.value)}
              className="w-full px-4 py-2 rounded-lg border border-gray-200 focus:outline-none focus:border-purple-400"
            >
              <option value="all">All Sizes</option>
              <option value="Small">Small</option>
              <option value="Medium">Medium</option>
              <option value="Large">Large</option>
            </select>
          </div>

          {/* Filter by Status */}
          <div>
            <select
              value={filterStatus}
              onChange={(e) => setFilterStatus(e.target.value)}
              className="w-full px-4 py-2 rounded-lg border border-gray-200 focus:outline-none focus:border-purple-400"
            >
              <option value="all">All Status</option>
              <option value="active">Active</option>
              <option value="inactive">Inactive</option>
            </select>
          </div>
        </div>
      </div>

      {/* Loading State */}
      {loading && (
        <div className="text-center py-8">
          <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-purple-600"></div>
          <p className="text-gray-500 mt-2">Loading facilities...</p>
        </div>
      )}

      {/* Facilities List View */}
      {!loading && (
        <div className="space-y-3">
          {facilities.length === 0 ? (
            <div className="bg-white rounded-xl border border-gray-100 p-8 text-center">
              <Store className="w-12 h-12 text-gray-400 mx-auto mb-3" />
              <p className="text-gray-500">No facilities found</p>
            </div>
          ) : (
            facilities.map((facility) => (
              <div
                key={facility.facilityId}
                className="bg-white rounded-xl border border-gray-100 p-5 hover:shadow-md transition-shadow"
              >
                <div className="flex flex-col md:flex-row gap-4 md:gap-5">
                  {/* Left: Facility Image */}
                  <div className="flex-shrink-0">
                    <div 
                      className="w-full md:w-32 h-48 md:h-32 rounded-lg overflow-hidden cursor-pointer hover:opacity-80 transition-opacity"
                      onClick={() => {
                        if (facility.facilityImage) {
                          setFullscreenImage(facilityService.getFacilityImageUrl(facility.facilityImage));
                        }
                      }}
                    >
                      {facility.facilityImage ? (
                        <img 
                          src={facilityService.getFacilityImageUrl(facility.facilityImage)}
                          alt={facility.facilityName}
                          className="w-full h-full object-cover"
                        />
                      ) : (
                        <div className="w-full h-full bg-gradient-to-br from-purple-100 to-purple-50 flex items-center justify-center">
                          <Store className="w-10 h-10 text-purple-400" />
                        </div>
                      )}
                    </div>
                  </div>

                  {/* Middle: Facility Info */}
                  <div className="flex-1 min-w-0">
                    <div className="flex items-start justify-between mb-2">
                      <div className="flex-1">
                        <h3 className="text-gray-900 mb-2">{facility.facilityName}</h3>
                        <div className="flex flex-wrap gap-2 mb-3">
                          <span className={`px-3 py-1 rounded-full text-xs border ${getSizeColor(facility.facilitySize)}`}>
                            {facility.facilitySize}
                          </span>
                          <span className="px-3 py-1 bg-gray-50 text-gray-700 border border-gray-200 rounded-full text-xs">
                            {facility.facilityType}
                          </span>
                          <span className={`px-3 py-1 rounded-full text-xs border ${
                            facility.facilityStatus === 'active' 
                              ? 'bg-green-50 text-green-700 border-green-200' 
                              : 'bg-red-50 text-red-700 border-red-200'
                          }`}>
                            {facility.facilityStatus === 'active' ? 'Active' : 'Inactive'}
                          </span>
                        </div>
                      </div>
                    </div>

                    <div className="grid grid-cols-2 gap-3 mb-3">
                      <div>
                        <p className="text-xs text-gray-500 mb-1">Student Price</p>
                        <p className="text-gray-900">RM {facility.facilityBaseStudentPrice}</p>
                      </div>
                      <div>
                        <p className="text-xs text-gray-500 mb-1">Non-student Price</p>
                        <p className="text-gray-900">RM {facility.facilityBaseNonstudentPrice}</p>
                      </div>
                    </div>

                    {/* <p className="text-sm text-gray-500">
                      Created on: {formatDate(facility.facilityCreateAt)}
                    </p> */}
                  </div>

                  {/* Right: Actions */}
                  <div className="flex md:flex-col gap-2 flex-shrink-0">
                    <button
                      onClick={() => setViewingFacility(facility)}
                      className="flex-1 md:flex-none px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors flex items-center justify-center gap-2 text-sm whitespace-nowrap"
                    >
                      <Eye className="w-4 h-4" />
                      <span className="hidden sm:inline">View</span>
                    </button>
                    <button
                      onClick={() => handleEdit(facility)}
                      className="flex-1 md:flex-none px-4 py-2 bg-purple-50 text-purple-700 border border-purple-200 rounded-lg hover:bg-purple-100 transition-colors flex items-center justify-center gap-2 text-sm whitespace-nowrap"
                    >
                      <Edit className="w-4 h-4" />
                      <span className="hidden sm:inline">Edit</span>
                    </button>
                    <button
                      onClick={() => toggleStatus(facility)}
                      className={`flex-1 md:flex-none px-4 py-2 rounded-lg transition-colors flex items-center justify-center gap-2 text-sm whitespace-nowrap ${
                        facility.facilityStatus === 'active'
                          ? 'bg-red-50 text-red-700 border border-red-200 hover:bg-red-100'
                          : 'bg-green-50 text-green-700 border border-green-200 hover:bg-green-100'
                      }`}
                      disabled={loading}
                    >
                      {facility.facilityStatus === 'active' ? (
                        <>
                          <PowerOff className="w-4 h-4" />
                          <span className="hidden sm:inline">Deactivate</span>
                        </>
                      ) : (
                        <>
                          <Power className="w-4 h-4" />
                          <span className="hidden sm:inline">Activate</span>
                        </>
                      )}
                    </button>
                  </div>
                </div>
              </div>
            ))
          )}
        </div>
      )}

      {/* Add/Edit Facility Modal */}
      {showAddModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-2xl max-w-3xl w-full max-h-[90vh] overflow-y-auto">
            <div className="sticky top-0 bg-white border-b border-gray-200 p-6 flex items-center justify-between">
              <h2 className="text-gray-900">
                {editingFacility ? 'Edit Facility Template' : 'Add New Facility Template'}
              </h2>
              <button
                onClick={closeModal}
                className="w-8 h-8 flex items-center justify-center rounded-lg hover:bg-gray-100 transition-colors"
              >
                <XCircle className="w-5 h-5 text-gray-500" />
              </button>
            </div>

            <form onSubmit={handleSubmit} className="p-6 space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {/* Facility Name */}
                <div className="md:col-span-2">
                  <label className="block text-gray-700 mb-2">
                    Facility Name <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    value={formData.facilityName}
                    onChange={(e) => setFormData({ ...formData, facilityName: e.target.value })}
                    placeholder="e.g., Standard Food Facility"
                    className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:border-purple-400"
                    required
                  />
                </div>

                {/* Facility Size */}
                <div>
                  <label className="block text-gray-700 mb-2">
                    Facility Size <span className="text-red-500">*</span>
                  </label>
                  <div className="relative">
                    <Maximize2 className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                    <select
                      value={showOtherSize ? 'Other' : formData.facilitySize}
                      onChange={(e) => {
                        const value = e.target.value;
                        if (value === 'Other') {
                          setShowOtherSize(true);
                          setFormData({ ...formData, facilitySize: customSize });
                        } else {
                          setShowOtherSize(false);
                          setCustomSize('');
                          setFormData({ ...formData, facilitySize: value });
                        }
                      }}
                      className="w-full pl-10 pr-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:border-purple-400"
                      required={!showOtherSize}
                    >
                      <option value="">Select size</option>
                      <option value="Small">Small (10x10 ft)</option>
                      <option value="Medium">Medium (10x20 ft)</option>
                      <option value="Large">Large (20x20 ft)</option>
                      <option value="Other">Other</option>
                    </select>
                  </div>
                  {showOtherSize && (
                    <input
                      type="text"
                      value={customSize}
                      onChange={(e) => {
                        setCustomSize(e.target.value);
                        setFormData({ ...formData, facilitySize: e.target.value });
                      }}
                      placeholder="Enter custom facility size"
                      className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:border-purple-400 mt-2"
                      required
                    />
                  )}
                </div>

                {/* Facility Type */}
                <div>
                  <label className="block text-gray-700 mb-2">
                    Facility Type <span className="text-red-500">*</span>
                  </label>
                  <div className="relative">
                    <Tag className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                    <select
                      value={showOtherType ? 'Other' : formData.facilityType}
                      onChange={(e) => {
                        const value = e.target.value;
                        if (value === 'Other') {
                          setShowOtherType(true);
                          setFormData({ ...formData, facilityType: customType });
                        } else {
                          setShowOtherType(false);
                          setCustomType('');
                          setFormData({ ...formData, facilityType: value });
                        }
                      }}
                      className="w-full pl-10 pr-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:border-purple-400"
                      required={!showOtherType}
                    >
                      <option value="">Select type</option>
                      {facilityTypes.map((type) => (
                        <option key={type} value={type}>
                          {type}
                        </option>
                      ))}
                    </select>
                  </div>
                  {showOtherType && (
                    <input
                      type="text"
                      value={customType}
                      onChange={(e) => {
                        setCustomType(e.target.value);
                        setFormData({ ...formData, facilityType: e.target.value });
                      }}
                      placeholder="Enter custom facility type"
                      className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:border-purple-400 mt-2"
                      required
                    />
                  )}
                </div>

                {/* Student Price */}
                <div>
                  <label className="block text-gray-700 mb-2">
                    Base Price (Student) <span className="text-red-500">*</span>
                  </label>
                  <div className="relative">
                    <DollarSign className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                    <input
                      type="number"
                      value={formData.facilityBaseStudentPrice}
                      onChange={(e) => setFormData({ ...formData, facilityBaseStudentPrice: e.target.value })}
                      placeholder="0.00"
                      min="0"
                      step="0.01"
                      className="w-full pl-10 pr-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:border-purple-400"
                      required
                    />
                  </div>
                  <p className="text-xs text-gray-500 mt-1">Price in RM for student vendors</p>
                </div>

                {/* Non-Student Price */}
                <div>
                  <label className="block text-gray-700 mb-2">
                    Base Price (Non-student) <span className="text-red-500">*</span>
                  </label>
                  <div className="relative">
                    <DollarSign className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                    <input
                      type="number"
                      value={formData.facilityBaseNonstudentPrice}
                      onChange={(e) => setFormData({ ...formData, facilityBaseNonstudentPrice: e.target.value })}
                      placeholder="0.00"
                      min="0"
                      step="0.01"
                      className="w-full pl-10 pr-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:border-purple-400"
                      required
                    />
                  </div>
                  <p className="text-xs text-gray-500 mt-1">Price in RM for non-student vendors</p>
                </div>

                {/* Facility Description */}
                <div className="md:col-span-2">
                  <label className="block text-gray-700 mb-2">
                    Facility Description <span className="text-red-500">*</span>
                  </label>
                  <textarea
                    value={formData.facilityDesc}
                    onChange={(e) => setFormData({ ...formData, facilityDesc: e.target.value })}
                    placeholder="Describe the facility features, amenities, and ideal usage..."
                    rows={3}
                    className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:border-purple-400 resize-none"
                    required
                  />
                </div>

                {/* Facility Usage */}
                <div className="md:col-span-2">
                  <label className="block text-gray-700 mb-2">
                    Facility Usage <span className="text-red-500">*</span>
                  </label>
                  <select
                    value={showOtherUsage ? 'Other' : formData.usage}
                    onChange={(e) => {
                      const value = e.target.value;
                      if (value === 'Other') {
                        setShowOtherUsage(true);
                        setFormData({ ...formData, usage: customUsage });
                      } else {
                        setShowOtherUsage(false);
                        setCustomUsage('');
                        setFormData({ ...formData, usage: value });
                      }
                    }}
                    className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:border-purple-400"
                    required={!showOtherUsage}
                  >
                    <option value="">Select recommended usage</option>
                    {facilityUsages.map((usage) => (
                      <option key={usage} value={usage}>
                        {usage}
                      </option>
                    ))}
                  </select>
                  {showOtherUsage && (
                    <input
                      type="text"
                      value={customUsage}
                      onChange={(e) => {
                        setCustomUsage(e.target.value);
                        setFormData({ ...formData, usage: e.target.value });
                      }}
                      placeholder="Enter custom facility usage"
                      className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:border-purple-400 mt-2"
                      required
                    />
                  )}
                </div>

                {/* Facility Remark */}
                <div className="md:col-span-2">
                  <label className="block text-gray-700 mb-2">
                    Facility Remark <span className="text-gray-500 text-sm">(Optional)</span>
                  </label>
                  <textarea
                    value={formData.remark}
                    onChange={(e) => setFormData({ ...formData, remark: e.target.value })}
                    placeholder="Internal remarks or special notes for this facility..."
                    rows={2}
                    className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:border-purple-400 resize-none"
                  />
                </div>

                {/* Facility Status */}
                <div className="md:col-span-2">
                  <label className="block text-gray-700 mb-3">
                    Facility Status <span className="text-red-500">*</span>
                  </label>
                  <div className="flex gap-4">
                    <label className="flex items-center gap-3 px-4 py-3 border-2 rounded-lg cursor-pointer transition-colors hover:bg-gray-50"
                      style={{
                        borderColor: formData.facilityStatus === 'active' ? '#a855f7' : '#e5e7eb',
                        backgroundColor: formData.facilityStatus === 'active' ? '#faf5ff' : 'white'
                      }}
                    >
                      <input
                        type="radio"
                        name="status"
                        value="active"
                        checked={formData.facilityStatus === 'active'}
                        onChange={(e) => setFormData({ ...formData, facilityStatus: e.target.value })}
                        className="w-4 h-4 text-purple-600"
                      />
                      <span className="text-gray-700">Active</span>
                    </label>
                    <label className="flex items-center gap-3 px-4 py-3 border-2 rounded-lg cursor-pointer transition-colors hover:bg-gray-50"
                      style={{
                        borderColor: formData.facilityStatus === 'inactive' ? '#a855f7' : '#e5e7eb',
                        backgroundColor: formData.facilityStatus === 'inactive' ? '#faf5ff' : 'white'
                      }}
                    >
                      <input
                        type="radio"
                        name="status"
                        value="inactive"
                        checked={formData.facilityStatus === 'inactive'}
                        onChange={(e) => setFormData({ ...formData, facilityStatus: e.target.value })}
                        className="w-4 h-4 text-purple-600"
                      />
                      <span className="text-gray-700">Inactive</span>
                    </label>
                  </div>
                  <p className="text-xs text-gray-500 mt-2">
                    Inactive facilities cannot be assigned to new events
                  </p>
                </div>

                {/* Facility Image Upload */}
                <div className="md:col-span-2">
                  <label className="block text-gray-700 mb-2">
                    Facility Image <span className="text-gray-500 text-sm">(Optional)</span>
                  </label>
                  
                  {imagePreview ? (
                    <div className="space-y-3">
                      <div className="relative w-full h-64 rounded-lg overflow-hidden border-2 border-gray-200">
                        <img 
                          src={imagePreview} 
                          alt="Preview"
                          className="w-full h-full object-cover"
                        />
                        <button
                          type="button"
                          onClick={() => {
                            setImagePreview(null);
                            setImageFile(null);
                          }}
                          className="absolute top-2 right-2 w-8 h-8 bg-red-500 text-white rounded-full flex items-center justify-center hover:bg-red-600 transition-colors"
                        >
                          <X className="w-4 h-4" />
                        </button>
                      </div>
                      <label className="w-full px-4 py-3 border-2 border-dashed border-gray-300 rounded-lg text-gray-600 hover:border-purple-400 hover:text-purple-600 transition-colors flex items-center justify-center gap-2 cursor-pointer">
                        <Upload className="w-5 h-5" />
                        Change Image
                        <input
                          type="file"
                          accept="image/*"
                          onChange={handleImageChange}
                          className="hidden"
                        />
                      </label>
                    </div>
                  ) : (
                    <label className="border-2 border-dashed border-gray-300 rounded-lg p-8 text-center hover:border-purple-400 transition-colors cursor-pointer block">
                      <Upload className="w-8 h-8 text-gray-400 mx-auto mb-2" />
                      <p className="text-gray-600 mb-1">Click to upload or drag and drop</p>
                      <p className="text-sm text-gray-400">PNG, JPG up to 5MB</p>
                      <input
                        type="file"
                        accept="image/*"
                        onChange={handleImageChange}
                        className="hidden"
                      />
                    </label>
                  )}
                </div>
              </div>

              {/* Form Actions */}
              <div className="flex gap-3 pt-4 border-t border-gray-200">
                {editingFacility && (
                  <button
                    type="button"
                    onClick={() => handleDelete(editingFacility.facilityId)}
                    className="px-6 py-3 bg-red-50 text-red-700 border border-red-200 rounded-lg hover:bg-red-100 transition-colors flex items-center justify-center gap-2"
                    disabled={loading}
                  >
                    <Trash2 className="w-4 h-4" />
                    Delete
                  </button>
                )}
                <button
                  type="button"
                  onClick={closeModal}
                  className="flex-1 px-6 py-3 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
                  disabled={loading}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="flex-1 px-6 py-3 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors flex items-center justify-center gap-2"
                  disabled={loading}
                >
                  {loading ? (
                    <>
                      <div className="inline-block animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                      Saving...
                    </>
                  ) : (
                    <>
                      <Save className="w-4 h-4" />
                      {editingFacility ? 'Update Facility' : 'Save Facility'}
                    </>
                  )}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* View Facility Modal */}
      {viewingFacility && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-2xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
            <div className="sticky top-0 bg-white border-b border-gray-200 p-6 flex items-center justify-between">
              <h2 className="text-gray-900">Facility Details</h2>
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

              {/* Facility Information */}
              <div>
                <h3 className="text-gray-900 mb-4">{viewingFacility.facilityName}</h3>
                <div className="grid grid-cols-2 gap-4 mb-6">
                  <div>
                    <p className="text-sm text-gray-500 mb-1">Facility Size</p>
                    <span className={`inline-block px-3 py-1 rounded-full text-sm border ${getSizeColor(viewingFacility.facilitySize)}`}>
                      {viewingFacility.facilitySize}
                    </span>
                  </div>
                  <div>
                    <p className="text-sm text-gray-500 mb-1">Facility Type</p>
                    <p className="text-gray-900">{viewingFacility.facilityType}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-500 mb-1">Student Price</p>
                    <p className="text-gray-900">RM {viewingFacility.facilityBaseStudentPrice}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-500 mb-1">Non-Student Price</p>
                    <p className="text-gray-900">RM {viewingFacility.facilityBaseNonstudentPrice}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-500 mb-1">Status</p>
                    <span className={`inline-block px-3 py-1 rounded-full text-sm border ${
                      viewingFacility.facilityStatus === 'active'
                        ? 'bg-green-50 text-green-700 border-green-200'
                        : 'bg-red-50 text-red-700 border-red-200'
                    }`}>
                      {viewingFacility.facilityStatus === 'active' ? 'Active' : 'Inactive'}
                    </span>
                  </div>
                  <div>
                    <p className="text-sm text-gray-500 mb-1">Created Date</p>
                    <p className="text-gray-900">{formatDate(viewingFacility.facilityCreateAt)}</p>
                  </div>
                </div>
                
                <div className="space-y-4">
                  <div>
                    <p className="text-sm text-gray-500 mb-2">Description</p>
                    <p className="text-gray-900">{viewingFacility.facilityDesc}</p>
                  </div>
                  
                  <div>
                    <p className="text-sm text-gray-500 mb-2">Usage Guidelines</p>
                    <p className="text-gray-900">{viewingFacility.usage}</p>
                  </div>
                  
                  {viewingFacility.remark && (
                    <div>
                      <p className="text-sm text-gray-500 mb-2">Internal Remarks</p>
                      <p className="text-gray-900">{viewingFacility.remark}</p>
                    </div>
                  )}
                </div>
              </div>

              {/* Action Buttons */}
              {/* <div className="flex gap-3 pt-4 border-t border-gray-200">
                <button
                  onClick={() => toggleStatus(viewingFacility)}
                  className={`flex-1 px-6 py-3 rounded-lg transition-colors flex items-center justify-center gap-2 ${
                    viewingFacility.facilityStatus === 'active'
                      ? 'bg-red-50 text-red-700 border border-red-200 hover:bg-red-100'
                      : 'bg-green-50 text-green-700 border border-green-200 hover:bg-green-100'
                  }`}
                  disabled={loading}
                >
                  {viewingFacility.facilityStatus === 'active' ? (
                    <>
                      <PowerOff className="w-4 h-4" />
                      Deactivate
                    </>
                  ) : (
                    <>
                      <Power className="w-4 h-4" />
                      Activate
                    </>
                  )}
                </button>
                <button
                  onClick={() => {
                    setViewingFacility(null);
                    handleEdit(viewingFacility);
                  }}
                  className="flex-1 px-6 py-3 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors flex items-center justify-center gap-2"
                >
                  <Edit className="w-4 h-4" />
                  Edit Facility Template
                </button>
              </div> */}
            </div>
          </div>
        </div>
      )}

      {/* Image Preview Modal */}
      {fullscreenImage && (
        <div 
          className="fixed inset-0 bg-black bg-opacity-75 flex items-center justify-center p-4 z-50"
          onClick={() => setFullscreenImage(null)}
        >
          <div className="relative max-w-4xl w-full">
            <button
              onClick={() => setFullscreenImage(null)}
              className="absolute -top-12 right-0 w-10 h-10 bg-white rounded-full flex items-center justify-center hover:bg-gray-100 transition-colors"
            >
              <X className="w-5 h-5 text-gray-700" />
            </button>
            <img 
              src={fullscreenImage} 
              alt="Preview"
              className="w-full h-auto rounded-lg"
            />
          </div>
        </div>
      )}
    </div>
  );
}