import React, { useState, useContext } from 'react';
import { BUSINESS_CATEGORIES } from '../utils/businessConstants';
import { useAuth } from '../context/AuthContext';

const BusinessForm = ({ initialData = {}, onSubmit, isLoading, submitButtonText = 'Create Business' }) => {
  const { user } = useAuth();
  const isNonStudent = user?.userCategory === 'NON_STUDENT';

  const [formData, setFormData] = useState({
    businessName: initialData.businessName || '',
    businessCategory: initialData.businessCategory || '',
    businessDesc: initialData.businessDesc || '',
    ssmNumber: initialData.ssmNumber || '',
  });

  const [errors, setErrors] = useState({});
  const [customCategory, setCustomCategory] = useState('');
  const [useCustomCategory, setUseCustomCategory] = useState(
    initialData.businessCategory && !BUSINESS_CATEGORIES.includes(initialData.businessCategory)
  );

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
    // Clear error for this field
    if (errors[name]) {
      setErrors((prev) => ({
        ...prev,
        [name]: '',
      }));
    }
  };

  const handleCategoryChange = (e) => {
    const value = e.target.value;
    if (value === 'custom') {
      setUseCustomCategory(true);
      setFormData((prev) => ({ ...prev, businessCategory: '' }));
    } else {
      setUseCustomCategory(false);
      setFormData((prev) => ({ ...prev, businessCategory: value }));
    }
  };

  const handleCustomCategoryChange = (e) => {
    const value = e.target.value;
    setCustomCategory(value);
    setFormData((prev) => ({ ...prev, businessCategory: value }));
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

    if (isNonStudent && !formData.ssmNumber.trim()) {
      newErrors.ssmNumber = 'SSM number is required for non-student business owners';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (validate()) {
      onSubmit(formData);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      {/* Business Name */}
      <div>
        <label htmlFor="businessName" className="block text-sm font-medium text-gray-700 mb-2">
          Business Name <span className="text-red-500">*</span>
        </label>
        <input
          type="text"
          id="businessName"
          name="businessName"
          value={formData.businessName}
          onChange={handleChange}
          className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
            errors.businessName ? 'border-red-500' : 'border-gray-300'
          }`}
          placeholder="Enter business name"
        />
        {errors.businessName && (
          <p className="mt-1 text-sm text-red-500">{errors.businessName}</p>
        )}
      </div>

      {/* Business Category */}
      <div>
        <label htmlFor="businessCategory" className="block text-sm font-medium text-gray-700 mb-2">
          Business Category <span className="text-red-500">*</span>
        </label>
        {!useCustomCategory ? (
          <select
            id="businessCategory"
            name="businessCategory"
            value={formData.businessCategory}
            onChange={handleCategoryChange}
            className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
              errors.businessCategory ? 'border-red-500' : 'border-gray-300'
            }`}
          >
            <option value="">Select a category</option>
            {BUSINESS_CATEGORIES.map((category) => (
              <option key={category} value={category}>
                {category}
              </option>
            ))}
            <option value="custom">+ Add Custom Category</option>
          </select>
        ) : (
          <div className="space-y-2">
            <input
              type="text"
              value={customCategory}
              onChange={handleCustomCategoryChange}
              className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                errors.businessCategory ? 'border-red-500' : 'border-gray-300'
              }`}
              placeholder="Enter custom category"
            />
            <button
              type="button"
              onClick={() => {
                setUseCustomCategory(false);
                setCustomCategory('');
                setFormData((prev) => ({ ...prev, businessCategory: '' }));
              }}
              className="text-sm text-blue-600 hover:underline"
            >
              ← Back to predefined categories
            </button>
          </div>
        )}
        {errors.businessCategory && (
          <p className="mt-1 text-sm text-red-500">{errors.businessCategory}</p>
        )}
      </div>

      {/* Business Description */}
      <div>
        <label htmlFor="businessDesc" className="block text-sm font-medium text-gray-700 mb-2">
          Business Description
        </label>
        <textarea
          id="businessDesc"
          name="businessDesc"
          value={formData.businessDesc}
          onChange={handleChange}
          rows={4}
          className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          placeholder="Describe your business (optional)"
        />
      </div>

      {/* SSM Number (for NON_STUDENT only) */}
      {isNonStudent && (
        <div>
          <label htmlFor="ssmNumber" className="block text-sm font-medium text-gray-700 mb-2">
            SSM Number <span className="text-red-500">*</span>
          </label>
          <input
            type="text"
            id="ssmNumber"
            name="ssmNumber"
            value={formData.ssmNumber}
            onChange={handleChange}
            className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
              errors.ssmNumber ? 'border-red-500' : 'border-gray-300'
            }`}
            placeholder="Enter SSM registration number"
          />
          {errors.ssmNumber && (
            <p className="mt-1 text-sm text-red-500">{errors.ssmNumber}</p>
          )}
          <p className="mt-1 text-sm text-gray-500">
            SSM number is required for non-student business owners
          </p>
        </div>
      )}

      {/* Submit Button */}
      <div className="flex gap-4">
        <button
          type="submit"
          disabled={isLoading}
          className={`flex-1 py-3 px-6 rounded-lg text-white font-medium transition-colors ${
            isLoading
              ? 'bg-gray-400 cursor-not-allowed'
              : 'bg-blue-600 hover:bg-blue-700'
          }`}
        >
          {isLoading ? 'Processing...' : submitButtonText}
        </button>
      </div>
    </form>
  );
};

export default BusinessForm;
