import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import userService from '../services/userService';
import { Sidebar } from '../components/Sidebar';

const Profile = () => {
  const navigate = useNavigate();
  const { user, updateUser } = useAuth();
  const [isEditing, setIsEditing] = useState(false);
  const [isChangingPassword, setIsChangingPassword] = useState(false);
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [success, setSuccess] = useState('');
  const [error, setError] = useState('');

  const [formData, setFormData] = useState({
    userName: '',
    userEmail: '',
    userPhoneNumber: '',
    userAddress: '',
    userCategory: '',
    bankName: '',
    bankAccNumber: '',
  });

  const [passwordData, setPasswordData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmNewPassword: '',
  });

  const [profileErrors, setProfileErrors] = useState({});
  const [passwordErrors, setPasswordErrors] = useState({});
  const [profileInfo, setProfileInfo] = useState(null);

  useEffect(() => {
    fetchProfile();
  }, []);

  const fetchProfile = async () => {
    try {
      setLoading(true);
      const response = await userService.getProfile();
      if (response.success) {
        const profile = response.data;
        setProfileInfo(profile);
        setFormData({
          userName: profile.userName || '',
          userEmail: profile.userEmail || '',
          userPhoneNumber: profile.userPhoneNumber || '',
          userAddress: profile.userAddress || '',
          userCategory: profile.userCategory || '',
          bankName: profile.bankName || '',
          bankAccNumber: profile.bankAccNumber || '',
        });
      }
    } catch (err) {
      setError(err || 'Failed to load profile');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    // Clear error for this field
    if (profileErrors[name]) {
      setProfileErrors((prev) => ({ ...prev, [name]: '' }));
    }
  };

  const handlePasswordChange = (e) => {
    const { name, value } = e.target;
    setPasswordData((prev) => ({ ...prev, [name]: value }));
    // Clear error for this field
    if (passwordErrors[name]) {
      setPasswordErrors((prev) => ({ ...prev, [name]: '' }));
    }
  };

  // Validate profile form
  const validateProfile = () => {
    const errors = {};

    if (!formData.userName.trim()) {
      errors.userName = 'Name is required';
    } else if (formData.userName.trim().length < 2) {
      errors.userName = 'Name must be at least 2 characters';
    }

    if (!formData.userPhoneNumber) {
      errors.userPhoneNumber = 'Phone number is required';
    } else if (!/^[0-9]{10,15}$/.test(formData.userPhoneNumber)) {
      errors.userPhoneNumber = 'Phone number must be 10-15 digits';
    }

    if (formData.bankAccNumber && !/^[0-9]{10,20}$/.test(formData.bankAccNumber)) {
      errors.bankAccNumber = 'Bank account number must be 10-20 digits';
    }

    return errors;
  };

  // Validate password form
  const validatePassword = () => {
    const errors = {};

    if (!passwordData.currentPassword) {
      errors.currentPassword = 'Current password is required';
    }

    if (!passwordData.newPassword) {
      errors.newPassword = 'New password is required';
    } else if (passwordData.newPassword.length < 8) {
      errors.newPassword = 'Password must be at least 8 characters';
    } else if (!/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/.test(passwordData.newPassword)) {
      errors.newPassword = 'Password must contain uppercase, lowercase, and number';
    }

    if (!passwordData.confirmNewPassword) {
      errors.confirmNewPassword = 'Please confirm your new password';
    } else if (passwordData.newPassword !== passwordData.confirmNewPassword) {
      errors.confirmNewPassword = 'Passwords do not match';
    }

    return errors;
  };

  const handleSave = async () => {
    setSuccess('');
    setError('');

    // Validate profile
    const errors = validateProfile();
    if (Object.keys(errors).length > 0) {
      setProfileErrors(errors);
      return;
    }

    setSaving(true);

    try {
      const response = await userService.updateProfile(formData);

      if (response.success) {
        setSuccess('Profile updated successfully!');
        updateUser(response.data);
        setProfileInfo(response.data);
        setIsEditing(false);
        setProfileErrors({});
        setTimeout(() => setSuccess(''), 3000);
      } else {
        setError(response.error || 'Failed to update profile');
      }
    } catch (err) {
      setError(err || 'An error occurred. Please try again.');
    } finally {
      setSaving(false);
    }
  };

  const handlePasswordSubmit = async () => {
    setSuccess('');
    setError('');

    // Validate password
    const errors = validatePassword();
    if (Object.keys(errors).length > 0) {
      setPasswordErrors(errors);
      return;
    }

    setSaving(true);

    try {
      const response = await userService.changePassword(passwordData);

      if (response.success) {
        setSuccess('Password changed successfully!');
        setPasswordData({
          currentPassword: '',
          newPassword: '',
          confirmNewPassword: '',
        });
        setPasswordErrors({});
        setIsChangingPassword(false);
        setTimeout(() => setSuccess(''), 3000);
      } else {
        setError(response.error || 'Failed to change password');
      }
    } catch (err) {
      setError(err || 'An error occurred. Please try again.');
    } finally {
      setSaving(false);
    }
  };

  const handleCancel = () => {
    setIsEditing(false);
    setError('');
    setProfileErrors({});
    // Reset form data to original values
    if (profileInfo) {
      setFormData({
        userName: profileInfo.userName || '',
        userEmail: profileInfo.userEmail || '',
        userPhoneNumber: profileInfo.userPhoneNumber || '',
        userAddress: profileInfo.userAddress || '',
        userCategory: profileInfo.userCategory || '',
        bankName: profileInfo.bankName || '',
        bankAccNumber: profileInfo.bankAccNumber || '',
      });
    }
  };

  const handleCancelPassword = () => {
    setIsChangingPassword(false);
    setError('');
    setPasswordErrors({});
    setPasswordData({
      currentPassword: '',
      newPassword: '',
      confirmNewPassword: '',
    });
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-gray-50">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-600"></div>
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
        <Sidebar currentView="profile" />
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
          <span className="font-semibold text-purple-600">Profile</span>
          <div className="w-10" />
        </div>

        {/* Content area */}
        <div className="flex-1 overflow-auto">
          <div className="max-w-7xl mx-auto p-4 sm:p-6 lg:p-8">
            {/* Header */}
            <div className="flex items-center justify-between mb-6">
              <div>
                <h1 className="text-2xl font-semibold text-gray-900 mb-2">Profile</h1>
                <p className="text-gray-600">Manage your Profile Information</p>
              </div>
              {!isEditing && !isChangingPassword && (
                <button
                  onClick={() => setIsEditing(true)}
                  className="px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors"
                >
                  Edit Profile
                </button>
              )}
            </div>

            {/* Success/Error Messages */}
            {success && (
              <div className="mb-6 p-4 bg-green-50 border border-green-200 rounded-lg">
                <p className="text-sm text-green-600">✓ {success}</p>
              </div>
            )}

            {error && (
              <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg">
                <p className="text-sm text-red-600">{error}</p>
              </div>
            )}

            {/* Profile Card */}
            <div className="bg-white rounded-xl p-6 sm:p-8 border border-gray-100 mb-6">
              {/* Profile Header */}
              <div className="flex flex-col sm:flex-row items-center gap-6 mb-8 pb-8 border-b border-gray-200">
                <div className="w-24 h-24 bg-gradient-to-br from-purple-100 to-pink-100 rounded-full flex items-center justify-center">
                  <svg className="w-16 h-16 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                  </svg>
                </div>
                <div className="text-center sm:text-left">
                  <h2 className="text-xl font-semibold text-gray-900 mb-1">{profileInfo?.userName || 'User'}</h2>
                  <p className="text-gray-500 mb-2">
                    {profileInfo?.userCategory === 'MPP' ? 'MPP Administrator' : 'Business Owner'}
                  </p>
                  <span className={`inline-block px-3 py-1 rounded-full text-sm font-medium ${
                    profileInfo?.userStatus === 'ACTIVE'
                      ? 'bg-green-100 text-green-700'
                      : profileInfo?.userStatus === 'PENDING'
                      ? 'bg-yellow-100 text-yellow-700'
                      : 'bg-red-100 text-red-700'
                  }`}>
                    {profileInfo?.userStatus === 'ACTIVE' ? 'Active Account' : profileInfo?.userStatus}
                  </span>
                </div>
              </div>

              {/* Action Buttons */}
              {isEditing && (
                <div className="flex justify-end gap-2 mb-6">
                  <button
                    onClick={handleCancel}
                    className="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
                  >
                    Cancel
                  </button>
                  <button
                    onClick={handleSave}
                    disabled={saving}
                    className="px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors flex items-center gap-2 disabled:opacity-50"
                  >
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                    </svg>
                    {saving ? 'Saving...' : 'Save Changes'}
                  </button>
                </div>
              )}

              {/* Profile Form */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {/* Full Name */}
                <div>
                  <label className="block text-gray-700 font-medium mb-2">Full Name</label>
                  <div className="relative">
                    <svg className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                    </svg>
                    <input
                      type="text"
                      name="userName"
                      value={formData.userName}
                      onChange={handleChange}
                      disabled={!isEditing}
                      className={`w-full pl-10 pr-4 py-2 rounded-lg border ${
                        profileErrors.userName ? 'border-red-500' : 'border-gray-200'
                      } focus:outline-none focus:border-purple-400 disabled:bg-gray-50 disabled:text-gray-600`}
                    />
                  </div>
                  {profileErrors.userName && (
                    <p className="mt-1 text-sm text-red-600">{profileErrors.userName}</p>
                  )}
                </div>

                {/* Email Address */}
                <div>
                  <label className="block text-gray-700 font-medium mb-2">Email Address</label>
                  <div className="relative">
                    <svg className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                    </svg>
                    <input
                      type="email"
                      name="userEmail"
                      value={formData.userEmail}
                      disabled
                      className="w-full pl-10 pr-4 py-2 rounded-lg border border-gray-200 bg-gray-50 text-gray-600 cursor-not-allowed"
                    />
                  </div>
                </div>

                {/* Phone Number */}
                <div>
                  <label className="block text-gray-700 font-medium mb-2">Phone Number</label>
                  <div className="relative">
                    <svg className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
                    </svg>
                    <input
                      type="tel"
                      name="userPhoneNumber"
                      value={formData.userPhoneNumber}
                      onChange={handleChange}
                      disabled={!isEditing}
                      className={`w-full pl-10 pr-4 py-2 rounded-lg border ${
                        profileErrors.userPhoneNumber ? 'border-red-500' : 'border-gray-200'
                      } focus:outline-none focus:border-purple-400 disabled:bg-gray-50 disabled:text-gray-600`}
                    />
                  </div>
                  {profileErrors.userPhoneNumber && (
                    <p className="mt-1 text-sm text-red-600">{profileErrors.userPhoneNumber}</p>
                  )}
                </div>

                {/* Address */}
                <div>
                  <label className="block text-gray-700 font-medium mb-2">Address</label>
                  <div className="relative">
                    <svg className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                    </svg>
                    <input
                      type="text"
                      name="userAddress"
                      value={formData.userAddress}
                      onChange={handleChange}
                      disabled={!isEditing}
                      className="w-full pl-10 pr-4 py-2 rounded-lg border border-gray-200 focus:outline-none focus:border-purple-400 disabled:bg-gray-50 disabled:text-gray-600"
                    />
                  </div>
                </div>

                {/* User Category */}
                <div>
                  <label className="block text-gray-700 font-medium mb-2">User Category</label>
                  <input
                    type="text"
                    value={formData.userCategory}
                    disabled
                    className="w-full px-4 py-2 rounded-lg border border-gray-200 bg-gray-50 text-gray-600 cursor-not-allowed"
                  />
                </div>

                {/* Bank Name */}
                <div>
                  <label className="block text-gray-700 font-medium mb-2">Bank Name</label>
                  {isEditing ? (
                    <select
                      name="bankName"
                      value={formData.bankName}
                      onChange={handleChange}
                      className="w-full px-4 py-2 rounded-lg border border-gray-200 focus:outline-none focus:border-purple-400"
                    >
                      <option value="">Select your bank</option>
                      <option value="Maybank">Maybank</option>
                      <option value="CIMB Bank">CIMB Bank</option>
                      <option value="Public Bank">Public Bank</option>
                      <option value="RHB Bank">RHB Bank</option>
                      <option value="Hong Leong Bank">Hong Leong Bank</option>
                      <option value="AmBank">AmBank</option>
                      <option value="Bank Islam">Bank Islam</option>
                      <option value="Bank Rakyat">Bank Rakyat</option>
                    </select>
                  ) : (
                    <input
                      type="text"
                      value={formData.bankName}
                      disabled
                      className="w-full px-4 py-2 rounded-lg border border-gray-200 bg-gray-50 text-gray-600"
                    />
                  )}
                </div>

                {/* Bank Account Number */}
                <div>
                  <label className="block text-gray-700 font-medium mb-2">Bank Account Number</label>
                  <input
                    type="text"
                    name="bankAccNumber"
                    value={formData.bankAccNumber}
                    onChange={handleChange}
                    disabled={!isEditing}
                    className={`w-full px-4 py-2 rounded-lg border ${
                      profileErrors.bankAccNumber ? 'border-red-500' : 'border-gray-200'
                    } focus:outline-none focus:border-purple-400 disabled:bg-gray-50 disabled:text-gray-600`}
                  />
                  {profileErrors.bankAccNumber && (
                    <p className="mt-1 text-sm text-red-600">{profileErrors.bankAccNumber}</p>
                  )}
                </div>
              </div>
            </div>

            {/* Change Password Card */}
            <div className="bg-white rounded-xl p-6 sm:p-8 border border-gray-100 mb-6">
              <div className="flex items-center justify-between mb-6">
                <div>
                  <h3 className="text-lg font-semibold text-gray-900">Change Password</h3>
                  <p className="text-sm text-gray-600 mt-1">Update your password to keep your account secure</p>
                </div>
                {!isChangingPassword && !isEditing && (
                  <button
                    onClick={() => setIsChangingPassword(true)}
                    className="px-4 py-2 border border-purple-600 text-purple-600 rounded-lg hover:bg-purple-50 transition-colors"
                  >
                    Change Password
                  </button>
                )}
              </div>

              {isChangingPassword && (
                <>
                  <div className="space-y-4 mb-6">
                    {/* Current Password */}
                    <div>
                      <label className="block text-gray-700 font-medium mb-2">
                        Current Password <span className="text-red-500">*</span>
                      </label>
                      <input
                        type="password"
                        name="currentPassword"
                        value={passwordData.currentPassword}
                        onChange={handlePasswordChange}
                        className={`w-full px-4 py-2 rounded-lg border ${
                          passwordErrors.currentPassword ? 'border-red-500' : 'border-gray-200'
                        } focus:outline-none focus:border-purple-400`}
                        placeholder="Enter current password"
                      />
                      {passwordErrors.currentPassword && (
                        <p className="mt-1 text-sm text-red-600">{passwordErrors.currentPassword}</p>
                      )}
                    </div>

                    {/* New Password */}
                    <div>
                      <label className="block text-gray-700 font-medium mb-2">
                        New Password <span className="text-red-500">*</span>
                      </label>
                      <input
                        type="password"
                        name="newPassword"
                        value={passwordData.newPassword}
                        onChange={handlePasswordChange}
                        className={`w-full px-4 py-2 rounded-lg border ${
                          passwordErrors.newPassword ? 'border-red-500' : 'border-gray-200'
                        } focus:outline-none focus:border-purple-400`}
                        placeholder="Enter new password"
                      />
                      {passwordErrors.newPassword && (
                        <p className="mt-1 text-sm text-red-600">{passwordErrors.newPassword}</p>
                      )}
                      <p className="mt-1 text-xs text-gray-500">
                        Must be 8+ characters with uppercase, lowercase, and number
                      </p>
                    </div>

                    {/* Confirm New Password */}
                    <div>
                      <label className="block text-gray-700 font-medium mb-2">
                        Confirm New Password <span className="text-red-500">*</span>
                      </label>
                      <input
                        type="password"
                        name="confirmNewPassword"
                        value={passwordData.confirmNewPassword}
                        onChange={handlePasswordChange}
                        className={`w-full px-4 py-2 rounded-lg border ${
                          passwordErrors.confirmNewPassword ? 'border-red-500' : 'border-gray-200'
                        } focus:outline-none focus:border-purple-400`}
                        placeholder="Confirm new password"
                      />
                      {passwordErrors.confirmNewPassword && (
                        <p className="mt-1 text-sm text-red-600">{passwordErrors.confirmNewPassword}</p>
                      )}
                    </div>
                  </div>

                  <div className="flex justify-end gap-2">
                    <button
                      onClick={handleCancelPassword}
                      className="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
                    >
                      Cancel
                    </button>
                    <button
                      onClick={handlePasswordSubmit}
                      disabled={saving}
                      className="px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors disabled:opacity-50"
                    >
                      {saving ? 'Changing...' : 'Change Password'}
                    </button>
                  </div>
                </>
              )}
            </div>

            {/* Account Information Card */}
            <div className="bg-white rounded-xl p-6 border border-gray-100">
              <h3 className="text-lg font-semibold text-gray-900 mb-4">Account Information</h3>
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 text-sm">
                <div>
                  <p className="text-gray-500 mb-1">Member Since</p>
                  <p className="text-gray-900 font-medium">
                    {profileInfo?.userRegisteredAt ? new Date(profileInfo.userRegisteredAt).toLocaleDateString() : 'N/A'}
                  </p>
                </div>
                <div>
                  <p className="text-gray-500 mb-1">Last Login</p>
                  <p className="text-gray-900 font-medium">
                    {profileInfo?.userLastLogin ? new Date(profileInfo.userLastLogin).toLocaleString() : 'N/A'}
                  </p>
                </div>
                <div>
                  <p className="text-gray-500 mb-1">Account Status</p>
                  <p className={`font-medium ${
                    profileInfo?.emailVerified ? 'text-green-600' : 'text-yellow-600'
                  }`}>
                    {profileInfo?.emailVerified ? 'Verified' : 'Pending Verification'}
                  </p>
                </div>
                <div>
                  <p className="text-gray-500 mb-1">Total Applications</p>
                  <p className="text-gray-900 font-medium">12</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Profile;