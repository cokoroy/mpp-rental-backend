import React, { useState, useEffect, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import businessService from '../services/businessService';
import { Sidebar } from '../components/Sidebar';
import BlockBusinessModal from '../components/BlockBusinessModal';
import ActivateBusinessModal from '../components/ActivateBusinessModal';
import { 
  Building2, 
  Search, 
  Eye, 
  Power, 
  PowerOff,
  AlertCircle 
} from 'lucide-react';

const BusinessManagementPage = () => {
  const navigate = useNavigate();
  const { user } = useContext(AuthContext);
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);

  // State
  const [businesses, setBusinesses] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  // Filter state
  const [searchQuery, setSearchQuery] = useState('');
  const [filterCategory, setFilterCategory] = useState('');
  const [filterStatus, setFilterStatus] = useState('');

  // Modal state
  const [blockModalOpen, setBlockModalOpen] = useState(false);
  const [activateModalOpen, setActivateModalOpen] = useState(false);
  const [selectedBusiness, setSelectedBusiness] = useState(null);
  const [isBlocking, setIsBlocking] = useState(false);
  const [isActivating, setIsActivating] = useState(false);

  // Categories (match your backend)
  const categories = [
    'Food & Beverage',
    'Clothing',
    'Services',
    'Arts & Crafts',
    'Technology',
    'Other'
  ];

  // Fetch businesses on component mount and when filters change
  useEffect(() => {
    fetchBusinesses();
  }, [searchQuery, filterCategory, filterStatus]);

  const fetchBusinesses = async () => {
    try {
      setIsLoading(true);
      setError('');

      const filters = {
        searchQuery: searchQuery || undefined,
        businessCategory: filterCategory || undefined,
        businessStatus: filterStatus || undefined,
      };

      const response = await businessService.getAllBusinessesForMPP(filters);
      console.log('Service Response:', response);
      
      // businessService already extracts response.data
      // response structure is: { success: true, message: "...", data: [...] }
      const businessesData = Array.isArray(response) ? response : (response.data || []);
      
      console.log('Businesses Data:', businessesData);
      console.log('Count:', businessesData.length);
      
      setBusinesses(businessesData);
    } catch (err) {
      console.error('Fetch businesses error:', err);
      setError(err.message || 'Failed to load businesses');
    } finally {
      setIsLoading(false);
    }
  };

  const handleBlockBusiness = async (businessId, businessName) => {
    try {
      setIsBlocking(true);
      await businessService.blockBusiness(businessId, 'Blocked by administrator');
      setSuccessMessage(`Business "${businessName}" has been blocked successfully`);
      setBlockModalOpen(false);
      setSelectedBusiness(null);
      fetchBusinesses(); // Refresh list
      
      setTimeout(() => setSuccessMessage(''), 3000);
    } catch (err) {
      setError(err.message || 'Failed to block business');
      setTimeout(() => setError(''), 3000);
    } finally {
      setIsBlocking(false);
    }
  };

  const openBlockModal = (business) => {
    setSelectedBusiness(business);
    setBlockModalOpen(true);
  };

  const handleActivateBusiness = async (businessId, businessName) => {
    try {
      setIsActivating(true);
      await businessService.activateBusiness(businessId);
      setSuccessMessage(`Business "${businessName}" has been activated successfully`);
      setActivateModalOpen(false);
      setSelectedBusiness(null);
      fetchBusinesses(); // Refresh list
      
      setTimeout(() => setSuccessMessage(''), 3000);
    } catch (err) {
      setError(err.message || 'Failed to activate business');
      setTimeout(() => setError(''), 3000);
    } finally {
      setIsActivating(false);
    }
  };

  const openActivateModal = (business) => {
    setSelectedBusiness(business);
    setActivateModalOpen(true);
  };

  const handleViewBusiness = (businessId) => {
    navigate(`/mpp/businesses/${businessId}`);
  };

  const getStatusColor = (status) => {
    switch (status?.toUpperCase()) {
      case 'ACTIVE':
        return 'bg-green-100 text-green-700';
      case 'BLOCKED':
        return 'bg-red-100 text-red-700';
      default:
        return 'bg-gray-100 text-gray-700';
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
  };

  return (
    <div className="min-h-screen bg-gray-50 flex">
      {/* Mobile overlay */}
      {isSidebarOpen && (
        <div
          className="fixed inset-0 bg-black bg-opacity-50 z-40 lg:hidden"
          onClick={() => setIsSidebarOpen(false)}
        />
      )}

      {/* Sidebar */}
      <div
        className={`fixed lg:static inset-y-0 left-0 z-50 transform transition-transform duration-300 ease-in-out ${
          isSidebarOpen ? 'translate-x-0' : '-translate-x-full'
        } lg:translate-x-0`}
      >
        <Sidebar currentView="business-management" />
      </div>

      {/* Main content */}
      <div className="flex-1 flex flex-col min-w-0">
        {/* Mobile header */}
        <div className="lg:hidden bg-white border-b border-gray-200 px-4 py-3 flex items-center justify-between sticky top-0 z-30">
          <button
            onClick={() => setIsSidebarOpen(!isSidebarOpen)}
            className="p-2 rounded-lg hover:bg-gray-100"
          >
            <svg className="w-6 h-6 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
            </svg>
          </button>
          <span className="font-semibold text-purple-600">Business Management</span>
          <div className="w-10" />
        </div>

        {/* Content area */}
        <div className="flex-1 overflow-auto">
          <div className="max-w-7xl mx-auto p-4 sm:p-6 lg:p-8">
            {/* Header */}
            <div className="mb-6">
              <h1 className="text-2xl font-semibold text-gray-900 mb-2">Business</h1>
              <p className="text-gray-600">Manage your Business Information</p>
            </div>

            {/* Success Message */}
            {successMessage && (
              <div className="mb-6 bg-green-50 border border-green-200 rounded-lg p-4">
                <p className="text-green-800">{successMessage}</p>
              </div>
            )}

            {/* Error Message */}
            {error && (
              <div className="mb-6 bg-red-50 border border-red-200 rounded-lg p-4">
                <p className="text-red-800">{error}</p>
              </div>
            )}

            {/* Search and Filter Section */}
            <div className="bg-white rounded-xl p-4 border border-gray-100 mb-6">
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                {/* Search Input */}
                <div className="relative">
                  <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                  <input
                    type="text"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    placeholder="Search by name, SSM, or owner..."
                    className="w-full pl-10 pr-4 py-2 rounded-lg border border-gray-200 focus:outline-none focus:border-purple-400"
                  />
                </div>

                {/* Category Filter */}
                <select
                  value={filterCategory}
                  onChange={(e) => setFilterCategory(e.target.value)}
                  className="px-4 py-2 rounded-lg border border-gray-200 focus:outline-none focus:border-purple-400"
                >
                  <option value="">All Categories</option>
                  {categories.map((cat) => (
                    <option key={cat} value={cat}>
                      {cat}
                    </option>
                  ))}
                </select>

                {/* Status Filter */}
                <select
                  value={filterStatus}
                  onChange={(e) => setFilterStatus(e.target.value)}
                  className="px-4 py-2 rounded-lg border border-gray-200 focus:outline-none focus:border-purple-400"
                >
                  <option value="">All Status</option>
                  <option value="ACTIVE">Active</option>
                  <option value="BLOCKED">Blocked</option>
                </select>
              </div>
            </div>

            {/* Loading State */}
            {isLoading && (
              <div className="flex items-center justify-center py-12">
                <div className="text-center">
                  <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-600 mx-auto"></div>
                  <p className="mt-4 text-gray-600">Loading businesses...</p>
                </div>
              </div>
            )}

            {/* Business List */}
            {!isLoading && businesses.length === 0 && (
              <div className="bg-white rounded-xl p-12 border border-gray-100 text-center">
                <Building2 className="w-12 h-12 text-gray-300 mx-auto mb-4" />
                <p className="text-gray-500">No businesses found</p>
              </div>
            )}

            {!isLoading && businesses.length > 0 && (
              <div className="space-y-4">
                {businesses.map((business) => (
                  <div
                    key={business.businessId}
                    className="bg-white rounded-xl p-6 border border-gray-100 hover:shadow-md transition-shadow"
                  >
                    <div className="flex flex-col lg:flex-row lg:items-start justify-between gap-4">
                      {/* Business Info */}
                      <div className="flex-1">
                        <div className="flex items-start gap-3 mb-3">
                          <div className="w-10 h-10 bg-purple-100 rounded-lg flex items-center justify-center flex-shrink-0">
                            <Building2 className="w-5 h-5 text-purple-600" />
                          </div>
                          <div className="flex-1 min-w-0">
                            <div className="flex items-center gap-2 mb-1 flex-wrap">
                              <h3 className="text-lg font-semibold text-gray-900">{business.businessName}</h3>
                              <span
                                className={`px-3 py-1 rounded-full text-xs font-medium capitalize ${getStatusColor(
                                  business.businessStatus
                                )}`}
                              >
                                {business.businessStatus}
                              </span>
                            </div>
                            <p className="text-sm text-gray-500 mb-2">
                              SSM: {business.ssmNumber || 'N/A'}
                            </p>
                            <p className="text-sm text-gray-600 mb-2">
                              Owner: {business.ownerName} ({business.ownerEmail})
                            </p>
                            <p className="text-sm text-gray-600 line-clamp-2">
                              {business.businessDesc}
                            </p>
                          </div>
                        </div>

                        <div className="grid grid-cols-2 sm:grid-cols-3 gap-2 text-sm text-gray-600 ml-13">
                          <div>
                            <span className="text-gray-500">Category: </span>
                            {business.businessCategory}
                          </div>
                          <div>
                            <span className="text-gray-500">Registered: </span>
                            {formatDate(business.businessRegisteredAt)}
                          </div>
                          <div>
                            <span className="text-gray-500">Owner Type: </span>
                            {business.ownerCategory}
                          </div>
                        </div>
                      </div>

                      {/* Action Buttons */}
                      <div className="flex flex-wrap lg:flex-col gap-2">
                        <button
                          onClick={() => handleViewBusiness(business.businessId)}
                          className="flex-1 lg:flex-none px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors flex items-center justify-center gap-2 text-sm"
                        >
                          <Eye className="w-4 h-4" />
                          View
                        </button>

                        {business.businessStatus === 'ACTIVE' && (
                          <button
                            onClick={() => openBlockModal(business)}
                            className="flex-1 lg:flex-none px-4 py-2 bg-red-50 text-red-700 border border-red-200 rounded-lg hover:bg-red-100 transition-colors flex items-center justify-center gap-2 text-sm"
                          >
                            <PowerOff className="w-4 h-4" />
                            <span className="hidden sm:inline">Block</span>
                          </button>
                        )}

                        {business.businessStatus === 'BLOCKED' && (
                          <button
                            onClick={() => openActivateModal(business)}
                            className="flex-1 lg:flex-none px-4 py-2 bg-green-50 text-green-700 border border-green-200 rounded-lg hover:bg-green-100 transition-colors flex items-center justify-center gap-2 text-sm"
                          >
                            <Power className="w-4 h-4" />
                            <span className="hidden sm:inline">Activate</span>
                          </button>
                        )}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Block Business Modal */}
      <BlockBusinessModal
        isOpen={blockModalOpen}
        onClose={() => {
          setBlockModalOpen(false);
          setSelectedBusiness(null);
        }}
        business={selectedBusiness}
        onConfirm={handleBlockBusiness}
        isBlocking={isBlocking}
      />

      {/* Activate Business Modal */}
      <ActivateBusinessModal
        isOpen={activateModalOpen}
        onClose={() => {
          setActivateModalOpen(false);
          setSelectedBusiness(null);
        }}
        business={selectedBusiness}
        onConfirm={handleActivateBusiness}
        isActivating={isActivating}
      />
    </div>
  );
};

export default BusinessManagementPage;