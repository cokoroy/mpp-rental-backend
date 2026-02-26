import React, { useState, useEffect, useContext } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import businessService from '../services/businessService';
import { BUSINESS_CATEGORIES } from '../utils/businessConstants';
import { Sidebar } from '../components/Sidebar';
import { Building2, Upload, X } from 'lucide-react';

const EditBusinessPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useContext(AuthContext);
  const isNonStudent = user?.userCategory === 'NON_STUDENT';
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);

  const [business, setBusiness] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const [error, setError] = useState('');
  const [selectedFile, setSelectedFile] = useState(null);

  const [formData, setFormData] = useState({
    businessName: '',
    ssmNumber: '',
    businessCategory: '',
    businessDesc: '',
  });

  const [errors, setErrors] = useState({});

  useEffect(() => {
    fetchBusinessDetails();
  }, [id]);

  const fetchBusinessDetails = async () => {
    try {
      setIsLoading(true);
      setError('');
      const response = await businessService.getBusinessById(id);
      console.log('Business Details Response:', response);

      let businessData;
      if (response.data) {
        businessData = response.data;
      } else if (response.businessId) {
        businessData = response;
      } else {
        setError('Invalid response format');
        return;
      }

      setBusiness(businessData);
      setFormData({
        businessName: businessData.businessName || '',
        ssmNumber: businessData.ssmNumber || '',
        businessCategory: businessData.businessCategory || '',
        businessDesc: businessData.businessDesc || '',
      });
    } catch (err) {
      console.error('Fetch details error:', err);
      setError(err.message || 'Failed to load business details');
    } finally {
      setIsLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
    if (errors[name]) {
      setErrors((prev) => ({
        ...prev,
        [name]: '',
      }));
    }
  };

  const handleFileSelect = (e) => {
    const file = e.target.files[0];
    if (!file) return;

    // Validate file type
    const allowedTypes = ['application/pdf', 'image/jpeg', 'image/png'];
    if (!allowedTypes.includes(file.type)) {
      setError('Invalid file type. Please upload PDF, JPG, or PNG files only.');
      return;
    }

    // Validate file size (5MB)
    if (file.size > 5 * 1024 * 1024) {
      setError('File size must not exceed 5MB');
      return;
    }

    setSelectedFile(file);
    setError('');
  };

  const handleUploadDocument = async () => {
    if (!selectedFile) {
      setError('Please select a file to upload');
      return;
    }

    try {
      setIsUploading(true);
      setError('');
      await businessService.uploadSSMDocument(id, selectedFile);
      setSelectedFile(null);
      // Refresh business data
      await fetchBusinessDetails();
    } catch (err) {
      setError(err.message || 'Failed to upload document');
    } finally {
      setIsUploading(false);
    }
  };

  const validate = () => {
    const newErrors = {};

    if (!formData.businessName.trim()) {
      newErrors.businessName = 'Business name is required';
    } else if (formData.businessName.length < 3) {
      newErrors.businessName = 'Business name must be at least 3 characters';
    }

    if (!formData.businessCategory.trim()) {
      newErrors.businessCategory = 'Business category is required';
    }

    if (!formData.businessDesc.trim()) {
      newErrors.businessDesc = 'Business description is required';
    }

    if (isNonStudent && !formData.ssmNumber.trim()) {
      newErrors.ssmNumber = 'SSM number is required for non-student business owners';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!validate()) {
      return;
    }

    try {
      setIsSaving(true);
      setError('');

      console.log('Updating business:', formData);
      await businessService.updateBusiness(id, formData);

      // Navigate back to details page
      navigate(`/business/${id}`);
    } catch (err) {
      console.error('Update error:', err);
      setError(err.message || 'Failed to update business');
    } finally {
      setIsSaving(false);
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
        <Sidebar currentView="business" />
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
          <span className="font-semibold text-purple-600">Edit Business</span>
          <div className="w-10" />
        </div>

        {/* Content area */}
        <div className="flex-1 overflow-auto">
          <div className="max-w-4xl mx-auto p-4 sm:p-6 lg:p-8">
            {/* Header */}
            <div className="mb-6">
              
              <h1 className="text-2xl sm:text-3xl font-bold text-gray-900 mb-2">Business</h1>
              <p className="text-gray-600">Manage your Business Information</p>
            </div>

            {/* Error Message */}
            {error && (
              <div className="mb-6 bg-red-50 border border-red-200 rounded-lg p-4">
                <p className="text-red-800">{error}</p>
              </div>
            )}

            {/* Form Card */}
            <div className="bg-white rounded-xl p-6 sm:p-8 border border-gray-100 mb-6">
              <div className="flex items-center gap-3 mb-6">
                <div className="w-10 h-10 bg-purple-100 rounded-lg flex items-center justify-center">
                  <Building2 className="w-5 h-5 text-purple-600" />
                </div>
                <h2 className="text-xl font-semibold text-gray-900">Edit Business</h2>
              </div>

              <form onSubmit={handleSubmit} className="space-y-6">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  {/* Business Name */}
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Business Name <span className="text-red-500">*</span>
                    </label>
                    <input
                      type="text"
                      name="businessName"
                      value={formData.businessName}
                      onChange={handleChange}
                      className={`w-full px-4 py-2 rounded-lg border ${
                        errors.businessName ? 'border-red-500' : 'border-gray-200'
                      } focus:outline-none focus:border-purple-400`}
                    />
                    {errors.businessName && (
                      <p className="mt-1 text-sm text-red-500">{errors.businessName}</p>
                    )}
                  </div>

                  {/* SSM Number */}
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      SSM Number {isNonStudent && <span className="text-red-500">*</span>}
                    </label>
                    <input
                      type="text"
                      name="ssmNumber"
                      value={formData.ssmNumber}
                      onChange={handleChange}
                      className={`w-full px-4 py-2 rounded-lg border ${
                        errors.ssmNumber ? 'border-red-500' : 'border-gray-200'
                      } focus:outline-none focus:border-purple-400`}
                    />
                    {errors.ssmNumber && (
                      <p className="mt-1 text-sm text-red-500">{errors.ssmNumber}</p>
                    )}
                  </div>

                  {/* Business Category */}
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Business Category <span className="text-red-500">*</span>
                    </label>
                    <select
                      name="businessCategory"
                      value={formData.businessCategory}
                      onChange={handleChange}
                      className={`w-full px-4 py-2 rounded-lg border ${
                        errors.businessCategory ? 'border-red-500' : 'border-gray-200'
                      } focus:outline-none focus:border-purple-400`}
                    >
                      <option value="">Select category</option>
                      {BUSINESS_CATEGORIES.map((cat) => (
                        <option key={cat} value={cat}>
                          {cat}
                        </option>
                      ))}
                    </select>
                    {errors.businessCategory && (
                      <p className="mt-1 text-sm text-red-500">{errors.businessCategory}</p>
                    )}
                  </div>

                  {/* Business Description */}
                  <div className="md:col-span-2">
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Business Description <span className="text-red-500">*</span>
                    </label>
                    <textarea
                      name="businessDesc"
                      value={formData.businessDesc}
                      onChange={handleChange}
                      rows={4}
                      className={`w-full px-4 py-2 rounded-lg border ${
                        errors.businessDesc ? 'border-red-500' : 'border-gray-200'
                      } focus:outline-none focus:border-purple-400 resize-none`}
                    />
                    {errors.businessDesc && (
                      <p className="mt-1 text-sm text-red-500">{errors.businessDesc}</p>
                    )}
                  </div>

                  {/* Upload SSM Document */}
                  <div className="md:col-span-2">
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      SSM Document {business?.ssmDocument && '(Current: ' + business.ssmDocument + ')'}
                    </label>
                    
                    <div className="space-y-3">
                      <div className="flex items-center gap-3">
                        <input
                          type="file"
                          onChange={handleFileSelect}
                          accept=".pdf,.jpg,.jpeg,.png"
                          className="flex-1 text-sm text-gray-500 file:mr-4 file:py-2 file:px-4 file:rounded-lg file:border-0 file:text-sm file:font-medium file:bg-purple-50 file:text-purple-700 hover:file:bg-purple-100"
                        />
                        {selectedFile && (
                          <button
                            type="button"
                            onClick={() => setSelectedFile(null)}
                            className="p-2 text-gray-400 hover:text-gray-600"
                          >
                            <X className="w-5 h-5" />
                          </button>
                        )}
                      </div>

                      {selectedFile && (
                        <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                          <span className="text-sm text-gray-700">{selectedFile.name}</span>
                          <button
                            type="button"
                            onClick={handleUploadDocument}
                            disabled={isUploading}
                            className="px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors text-sm disabled:opacity-50"
                          >
                            {isUploading ? 'Uploading...' : 'Upload'}
                          </button>
                        </div>
                      )}

                      <p className="text-xs text-gray-500">
                        Allowed: PDF, JPG, PNG (Max 5MB)
                      </p>
                    </div>
                  </div>
                </div>

                {/* Submit Buttons */}
                <div className="flex gap-3 pt-4 border-t border-gray-200">
                  <button
                    type="button"
                    onClick={() => navigate(`/business/${id}`)}
                    disabled={isSaving}
                    className="flex-1 px-6 py-3 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors disabled:opacity-50"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    disabled={isSaving}
                    className="flex-1 px-6 py-3 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors disabled:opacity-50"
                  >
                    {isSaving ? 'Saving...' : 'Save Changes'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default EditBusinessPage;
