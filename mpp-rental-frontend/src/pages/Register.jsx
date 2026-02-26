import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Register = () => {
  const navigate = useNavigate();
  const { register } = useAuth();

  const [formData, setFormData] = useState({
    userName: '',
    userEmail: '',
    userPhoneNumber: '',
    userPassword: '',
    confirmPassword: '',
    userCategory: 'STUDENT',
    userAddress: '',
    bankName: '',
    bankAccNumber: '',
  });

  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [apiError, setApiError] = useState('');
  const [success, setSuccess] = useState(false);

  // Handle input changes
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
    // Clear error for this field
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: '' }));
    }
  };

  // Validate form
  const validate = () => {
    const newErrors = {};

    // Name validation
    if (!formData.userName.trim()) {
      newErrors.userName = 'Name is required';
    } else if (formData.userName.trim().length < 2) {
      newErrors.userName = 'Name must be at least 2 characters';
    }

    // Email validation
    if (!formData.userEmail) {
      newErrors.userEmail = 'Email is required';
    } else if (!/\S+@\S+\.\S+/.test(formData.userEmail)) {
      newErrors.userEmail = 'Email is invalid';
    }

    // Phone validation
    if (!formData.userPhoneNumber) {
      newErrors.userPhoneNumber = 'Phone number is required';
    } else if (!/^[0-9]{10,15}$/.test(formData.userPhoneNumber)) {
      newErrors.userPhoneNumber = 'Phone number must be 10-15 digits';
    }

    // Password validation
    if (!formData.userPassword) {
      newErrors.userPassword = 'Password is required';
    } else if (formData.userPassword.length < 8) {
      newErrors.userPassword = 'Password must be at least 8 characters';
    } else if (!/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/.test(formData.userPassword)) {
      newErrors.userPassword = 'Password must contain uppercase, lowercase, and number';
    }

    // Confirm password validation
    if (!formData.confirmPassword) {
      newErrors.confirmPassword = 'Please confirm your password';
    } else if (formData.userPassword !== formData.confirmPassword) {
      newErrors.confirmPassword = 'Passwords do not match';
    }

    // Bank name validation
    if (!formData.bankName.trim()) {
      newErrors.bankName = 'Bank name is required';
    }

    // Bank account validation
    if (!formData.bankAccNumber) {
      newErrors.bankAccNumber = 'Bank account number is required';
    } else if (!/^[0-9]{10,20}$/.test(formData.bankAccNumber)) {
      newErrors.bankAccNumber = 'Bank account number must be 10-20 digits';
    }

    return newErrors;
  };

  // Handle form submission
  const handleSubmit = async (e) => {
    e.preventDefault();
    setApiError('');

    // Validate
    const newErrors = validate();
    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    setLoading(true);

    try {
      const result = await register(formData);

      if (result.success) {
        setSuccess(true);
        // Redirect to login after 2 seconds
        setTimeout(() => {
          navigate('/login');
        }, 2000);
      } else {
        setApiError(result.error || 'Registration failed. Please try again.');
      }
    } catch (error) {
      setApiError(error || 'An error occurred. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary-50 to-primary-100 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-2xl mx-auto">
        {/* Header */}
        <div className="text-center mb-8">
          <h2 className="text-3xl font-bold text-gray-900">Create Account</h2>
          <p className="mt-2 text-gray-600">
            Join MPP Business Rental System
          </p>
        </div>

        {/* Registration Card */}
        <div className="bg-white rounded-2xl shadow-xl p-8">
          {/* Success Message */}
          {success && (
            <div className="mb-6 p-4 bg-green-50 border border-green-200 rounded-lg">
              <p className="text-sm text-green-600">
                ✓ Registration successful! Redirecting to login...
              </p>
            </div>
          )}

          {/* API Error Message */}
          {apiError && (
            <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg">
              <p className="text-sm text-red-600">{apiError}</p>
            </div>
          )}

          {/* Registration Form */}
          <form onSubmit={handleSubmit} className="space-y-6">
            {/* Personal Information Section */}
            <div>
              <h3 className="text-lg font-semibold text-gray-900 mb-4">Personal Information</h3>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {/* Full Name */}
                <div className="md:col-span-2">
                  <label htmlFor="userName" className="block text-sm font-medium text-gray-700 mb-2">
                    Full Name <span className="text-red-500">*</span>
                  </label>
                  <input
                    id="userName"
                    name="userName"
                    type="text"
                    value={formData.userName}
                    onChange={handleChange}
                    className={`input-field ${errors.userName ? 'border-red-500' : ''}`}
                    placeholder="Enter your full name"
                  />
                  {errors.userName && (
                    <p className="mt-1 text-sm text-red-600">{errors.userName}</p>
                  )}
                </div>

                {/* Email */}
                <div>
                  <label htmlFor="userEmail" className="block text-sm font-medium text-gray-700 mb-2">
                    Email Address <span className="text-red-500">*</span>
                  </label>
                  <input
                    id="userEmail"
                    name="userEmail"
                    type="email"
                    value={formData.userEmail}
                    onChange={handleChange}
                    className={`input-field ${errors.userEmail ? 'border-red-500' : ''}`}
                    placeholder="your.email@example.com"
                  />
                  {errors.userEmail && (
                    <p className="mt-1 text-sm text-red-600">{errors.userEmail}</p>
                  )}
                </div>

                {/* Phone Number */}
                <div>
                  <label htmlFor="userPhoneNumber" className="block text-sm font-medium text-gray-700 mb-2">
                    Phone Number <span className="text-red-500">*</span>
                  </label>
                  <input
                    id="userPhoneNumber"
                    name="userPhoneNumber"
                    type="tel"
                    value={formData.userPhoneNumber}
                    onChange={handleChange}
                    className={`input-field ${errors.userPhoneNumber ? 'border-red-500' : ''}`}
                    placeholder="0123456789"
                  />
                  {errors.userPhoneNumber && (
                    <p className="mt-1 text-sm text-red-600">{errors.userPhoneNumber}</p>
                  )}
                </div>

                {/* User Category */}
                <div>
                  <label htmlFor="userCategory" className="block text-sm font-medium text-gray-700 mb-2">
                    User Category <span className="text-red-500">*</span>
                  </label>
                  <select
                    id="userCategory"
                    name="userCategory"
                    value={formData.userCategory}
                    onChange={handleChange}
                    className="input-field"
                  >
                    <option value="STUDENT">Student</option>
                    <option value="NON_STUDENT">Non-Student</option>
                    <option value="MPP">MPP</option>
                  </select>
                </div>

                {/* Address */}
                <div className="md:col-span-2">
                  <label htmlFor="userAddress" className="block text-sm font-medium text-gray-700 mb-2">
                    Address
                  </label>
                  <textarea
                    id="userAddress"
                    name="userAddress"
                    rows="2"
                    value={formData.userAddress}
                    onChange={handleChange}
                    className="input-field"
                    placeholder="Enter your address"
                  />
                </div>
              </div>
            </div>

            {/* Account Security Section */}
            <div>
              <h3 className="text-lg font-semibold text-gray-900 mb-4">Account Security</h3>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {/* Password */}
                <div>
                  <label htmlFor="userPassword" className="block text-sm font-medium text-gray-700 mb-2">
                    Password <span className="text-red-500">*</span>
                  </label>
                  <input
                    id="userPassword"
                    name="userPassword"
                    type="password"
                    value={formData.userPassword}
                    onChange={handleChange}
                    className={`input-field ${errors.userPassword ? 'border-red-500' : ''}`}
                    placeholder="Enter password"
                  />
                  {errors.userPassword && (
                    <p className="mt-1 text-sm text-red-600">{errors.userPassword}</p>
                  )}
                  <p className="mt-1 text-xs text-gray-500">
                    Must be 8+ characters with uppercase, lowercase, and number
                  </p>
                </div>

                {/* Confirm Password */}
                <div>
                  <label htmlFor="confirmPassword" className="block text-sm font-medium text-gray-700 mb-2">
                    Confirm Password <span className="text-red-500">*</span>
                  </label>
                  <input
                    id="confirmPassword"
                    name="confirmPassword"
                    type="password"
                    value={formData.confirmPassword}
                    onChange={handleChange}
                    className={`input-field ${errors.confirmPassword ? 'border-red-500' : ''}`}
                    placeholder="Confirm password"
                  />
                  {errors.confirmPassword && (
                    <p className="mt-1 text-sm text-red-600">{errors.confirmPassword}</p>
                  )}
                </div>
              </div>
            </div>

            {/* Bank Information Section */}
            <div>
              <h3 className="text-lg font-semibold text-gray-900 mb-4">Bank Information</h3>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {/* Bank Name */}
                <div>
                  <label htmlFor="bankName" className="block text-sm font-medium text-gray-700 mb-2">
                    Bank Name <span className="text-red-500">*</span>
                  </label>
                  <input
                    id="bankName"
                    name="bankName"
                    type="text"
                    value={formData.bankName}
                    onChange={handleChange}
                    className={`input-field ${errors.bankName ? 'border-red-500' : ''}`}
                    placeholder="e.g., Maybank"
                  />
                  {errors.bankName && (
                    <p className="mt-1 text-sm text-red-600">{errors.bankName}</p>
                  )}
                </div>

                {/* Bank Account Number */}
                <div>
                  <label htmlFor="bankAccNumber" className="block text-sm font-medium text-gray-700 mb-2">
                    Bank Account Number <span className="text-red-500">*</span>
                  </label>
                  <input
                    id="bankAccNumber"
                    name="bankAccNumber"
                    type="text"
                    value={formData.bankAccNumber}
                    onChange={handleChange}
                    className={`input-field ${errors.bankAccNumber ? 'border-red-500' : ''}`}
                    placeholder="1234567890"
                  />
                  {errors.bankAccNumber && (
                    <p className="mt-1 text-sm text-red-600">{errors.bankAccNumber}</p>
                  )}
                </div>
              </div>
            </div>

            {/* Submit Button */}
            <div className="pt-4">
              <button
                type="submit"
                disabled={loading || success}
                className="w-full btn-primary flex items-center justify-center"
              >
                {loading ? (
                  <>
                    <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    Creating Account...
                  </>
                ) : (
                  'Create Account'
                )}
              </button>
            </div>
          </form>

          {/* Login Link */}
          <div className="mt-6 text-center">
            <p className="text-sm text-gray-600">
              Already have an account?{' '}
              <Link to="/login" className="font-medium text-primary-600 hover:text-primary-500">
                Sign in here
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Register;