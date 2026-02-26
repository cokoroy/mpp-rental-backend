import React, { useState, useEffect, useContext } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import businessService from '../services/businessService';
import { Sidebar } from '../components/Sidebar';
import BlockBusinessModal from '../components/BlockBusinessModal';
import ActivateBusinessModal from '../components/ActivateBusinessModal';
import { Building2, AlertCircle, FileText, Download, ArrowLeft, Power, PowerOff } from 'lucide-react';

const BusinessDetailsPageMPP = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useContext(AuthContext);
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);

  const [business, setBusiness] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [blockModalOpen, setBlockModalOpen] = useState(false);
  const [activateModalOpen, setActivateModalOpen] = useState(false);
  const [isBlocking, setIsBlocking] = useState(false);
  const [isActivating, setIsActivating] = useState(false);

  useEffect(() => {
    fetchBusinessDetails();
  }, [id]);

  const fetchBusinessDetails = async () => {
    try {
      setIsLoading(true);
      setError('');
      
      console.log('=== FETCHING BUSINESS DETAILS ===');
      console.log('Business ID:', id);
      
      const response = await businessService.getBusinessDetailForMPP(id);
      console.log('Raw Response:', response);
      console.log('Response.data:', response.data);
      console.log('Response.data.ssmDocument:', response.data?.ssmDocument);

      // Handle different response structures (same as Business Owner page)
      let businessData;
      if (response.data) {
        businessData = response.data;
      } else if (response.businessId) {
        businessData = response;
      } else {
        setError('Invalid response format');
        return;
      }

      console.log('Final Business Data:', businessData);
      console.log('SSM Document:', businessData.ssmDocument);
      console.log('Has SSM Document?', !!businessData.ssmDocument);
      
      setBusiness(businessData);
    } catch (err) {
      console.error('Fetch details error:', err);
      setError(err.message || 'Failed to load business details');
    } finally {
      setIsLoading(false);
    }
  };

  const handleBlockBusiness = async (businessId, businessName) => {
    try {
      setIsBlocking(true);
      setError('');
      await businessService.blockBusiness(id, 'Blocked by administrator');
      setSuccessMessage('Business blocked successfully');
      setBlockModalOpen(false);
      fetchBusinessDetails(); // Refresh data
      setTimeout(() => setSuccessMessage(''), 3000);
    } catch (err) {
      setError(err.message || 'Failed to block business');
    } finally {
      setIsBlocking(false);
    }
  };

  const handleActivateBusiness = async (businessId, businessName) => {
    try {
      setIsActivating(true);
      setError('');
      await businessService.activateBusiness(id);
      setSuccessMessage('Business activated successfully');
      setActivateModalOpen(false);
      fetchBusinessDetails(); // Refresh data
      setTimeout(() => setSuccessMessage(''), 3000);
    } catch (err) {
      setError(err.message || 'Failed to activate business');
    } finally {
      setIsActivating(false);
    }
  };

  const handleDownloadDocument = async () => {
    if (!business?.ssmDocument) {
      setError('No document available to download');
      return;
    }

    try {
      // Construct the download URL (same as Business Owner page)
      const downloadUrl = `http://localhost:8080/api/business/${id}/ssm-document`;
      
      // Get the token
      const token = localStorage.getItem('token');
      
      // Fetch the file
      const response = await fetch(downloadUrl, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      if (!response.ok) {
        throw new Error('Failed to download document');
      }

      // Get the blob
      const blob = await response.blob();
      
      // Create download link
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = business.ssmDocument || 'ssm-document';
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);

      setSuccessMessage('Document downloaded successfully');
      setTimeout(() => setSuccessMessage(''), 3000);
    } catch (err) {
      console.error('Download error:', err);
      setError('Failed to download document');
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
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

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-gray-50">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading business details...</p>
        </div>
      </div>
    );
  }

  if (error && !business) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
        <div className="bg-white rounded-lg shadow-md p-8 max-w-md w-full text-center">
          <AlertCircle className="w-16 h-16 text-red-500 mx-auto mb-4" />
          <h2 className="text-xl font-semibold text-gray-900 mb-2">Error Loading Business</h2>
          <p className="text-gray-600 mb-6">{error}</p>
          <button
            onClick={() => navigate('/mpp/businesses')}
            className="bg-purple-600 text-white px-6 py-2 rounded-lg hover:bg-purple-700 transition-colors"
          >
            Back to Business Management
          </button>
        </div>
      </div>
    );
  }

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
          <span className="font-semibold text-purple-600">Business Details</span>
          <div className="w-10" />
        </div>

        {/* Content area */}
        <div className="flex-1 overflow-auto">
          <div className="max-w-4xl mx-auto p-4 sm:p-6 lg:p-8">
            {/* Back Button */}
            <button
              onClick={() => navigate('/mpp/businesses')}
              className="flex items-center gap-2 text-gray-600 hover:text-purple-600 mb-6 transition-colors"
            >
              <ArrowLeft className="w-5 h-5" />
              <span>Back to Business Management</span>
            </button>

            {/* Header */}
            <div className="mb-6">
              <h1 className="text-2xl font-semibold text-gray-900 mb-2">Business Details</h1>
              <p className="text-gray-600">View and manage business information</p>
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

            {/* Business Details Card */}
            <div className="bg-white rounded-2xl border border-gray-100 overflow-hidden">
              <div className="bg-white border-b border-gray-200 p-6 flex items-center justify-between">
                <h2 className="text-xl font-semibold text-gray-900">Business Information</h2>
                <span
                  className={`px-3 py-1 rounded-full text-sm font-medium capitalize ${getStatusColor(
                    business?.businessStatus
                  )}`}
                >
                  {business?.businessStatus}
                </span>
              </div>

              <div className="p-6 space-y-6">
                {/* Business & Owner Information Grid */}
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div>
                    <p className="text-sm text-gray-500 mb-1">Business Name</p>
                    <p className="text-gray-900 font-medium">{business?.businessName}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-500 mb-1">SSM Number</p>
                    <p className="text-gray-900">{business?.ssmNumber || 'N/A'}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-500 mb-1">Category</p>
                    <p className="text-gray-900">{business?.businessCategory}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-500 mb-1">Registered Date</p>
                    <p className="text-gray-900">{formatDate(business?.businessRegisteredAt)}</p>
                  </div>

                  {/* Owner Information */}
                  <div className="sm:col-span-2 pt-4 border-t border-gray-200">
                    <p className="text-sm font-medium text-gray-700 mb-3">Business Owner Information</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-500 mb-1">Owner Name</p>
                    <p className="text-gray-900">{business?.ownerName}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-500 mb-1">Owner Email</p>
                    <p className="text-gray-900">{business?.ownerEmail}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-500 mb-1">Owner Phone</p>
                    <p className="text-gray-900">{business?.ownerPhoneNumber}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-500 mb-1">Owner Category</p>
                    <p className="text-gray-900">{business?.ownerCategory}</p>
                  </div>
                </div>

                {/* Business Description */}
                <div className="pt-4 border-t border-gray-200">
                  <p className="text-sm text-gray-500 mb-2">Business Description</p>
                  <p className="text-gray-900">{business?.businessDesc}</p>
                </div>

                {/* SSM Document Section */}
                <div className="pt-4 border-t border-gray-200">
                  <p className="text-sm font-medium text-gray-700 mb-3">SSM Document</p>

                  {business?.ssmDocument ? (
                    <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors">
                      <div className="flex items-center gap-3">
                        <div className="w-10 h-10 bg-purple-100 rounded-lg flex items-center justify-center">
                          <FileText className="w-5 h-5 text-purple-600" />
                        </div>
                        <div>
                          <p className="text-sm text-gray-900 font-medium">SSM Certificate</p>
                          <p className="text-xs text-gray-500">{business.ssmDocument}</p>
                        </div>
                      </div>
                      <button
                        onClick={handleDownloadDocument}
                        className="px-3 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors flex items-center gap-2 text-sm"
                      >
                        <Download className="w-4 h-4" />
                        Download
                      </button>
                    </div>
                  ) : (
                    <div className="p-4 bg-gray-50 rounded-lg border border-gray-200 text-center">
                      <FileText className="w-8 h-8 text-gray-400 mx-auto mb-2" />
                      <p className="text-sm text-gray-600">No document uploaded</p>
                    </div>
                  )}
                </div>

                {/* Status Management Section */}
                <div className="pt-6 border-t border-gray-200">
                  <div className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                    <div>
                      <p className="text-gray-900 font-medium mb-1">Business Status Management</p>
                      <p className="text-sm text-gray-500">
                        {business?.businessStatus === 'ACTIVE'
                          ? 'Block this business to prevent new applications'
                          : 'Activate this business to allow applications'}
                      </p>
                    </div>
                    <button
                      onClick={() => {
                        if (business?.businessStatus === 'ACTIVE') {
                          setBlockModalOpen(true);
                        } else {
                          setActivateModalOpen(true);
                        }
                      }}
                      className={`px-6 py-3 rounded-lg transition-colors flex items-center gap-2 font-medium ${
                        business?.businessStatus === 'ACTIVE'
                          ? 'bg-red-600 text-white hover:bg-red-700'
                          : 'bg-green-600 text-white hover:bg-green-700'
                      }`}
                    >
                      {business?.businessStatus === 'ACTIVE' ? (
                        <>
                          <PowerOff className="w-4 h-4" />
                          Block Business
                        </>
                      ) : (
                        <>
                          <Power className="w-4 h-4" />
                          Activate Business
                        </>
                      )}
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Block Business Modal */}
      <BlockBusinessModal
        isOpen={blockModalOpen}
        onClose={() => setBlockModalOpen(false)}
        business={business}
        onConfirm={handleBlockBusiness}
        isBlocking={isBlocking}
      />

      {/* Activate Business Modal */}
      <ActivateBusinessModal
        isOpen={activateModalOpen}
        onClose={() => setActivateModalOpen(false)}
        business={business}
        onConfirm={handleActivateBusiness}
        isActivating={isActivating}
      />
    </div>
  );
};

export default BusinessDetailsPageMPP;