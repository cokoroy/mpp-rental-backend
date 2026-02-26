import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import userService from '../services/userService';

const EditProfile = () => {
  const navigate = useNavigate();
  const { user, updateUser } = useAuth();

  // Profile Form State
  const [profileForm, setProfileForm] = useState({
    userName: '',
    userPhoneNumber: '',
    userAddress: '',
    bankName: '',
    bankAccNumber: '',
  });

  // Password Form State
  const [passwordForm, setPasswordForm] = useState({
    currentPassword: '',
    newPassword: '',
    confirmNewPassword: '',
  });

  const [profileErrors, setProfileErrors] = useState({});
  const [passwordErrors, setPasswordErrors] = useState({});
  const [profileLoading, setProfileLoading] = useState(false);
  const [passwordLoading, setPasswordLoading] = useState(false);
  const [profileSuccess, setProfileSuccess] = useState('');
  const [passwordSuccess, setPasswordSuccess] = useState('');
  const [profileError, setProfileError] = useState('');
  const [passwordError, setPasswordError] = useState('');
  const [loading, setLoading] = useState(true);

  // Fetch profile data on mount
  useEffect(() => {
    fetchProfile();
  }, []);

  const fetchProfile = async () => {
    try {
      setLoading(true);
      const response = await userService.getProfile();
      if (response.success) {
        const profile = response.data;
        setProfileForm({
          userName: profile.userName || '',
          userPhoneNumber: profile.userPhoneNumber || '',
          userAddress: profile.userAddress || '',
          bankName: profile.bankName || '',
          bankAccNumber: profile.bankAccNumber || '',
        });
      }
    } catch (err) {
      setProfileError(err || 'Failed to load profile');
    } finally {
      setLoading(false);
    }
  };

  // Handle profile form changes
  const handleProfileChange = (e) => {
    const { name, value } = e.target;
    setProfileForm((prev) => ({ ...prev, [name]: value }));
    if (profileErrors[name]) {
      setProfileErrors((prev) => ({ ...prev, [name]: '' }));
    }
  };

  // Handle password form changes
  const handlePasswordChange = (e) => {
    const { name, value } = e.target;
    setPasswordForm((prev) => ({ ...prev, [name]: value }));
    if (passwordErrors[name]) {
      setPasswordErrors((prev) => ({ ...prev, [name]: '' }));
    }
  };

  // Validate profile form
  const validateProfile = () => {
    const errors = {};

    if (!profileForm.userName.trim()) {
      errors.userName = 'Name is required';
    } else if (profileForm.userName.trim().length < 2) {
      errors.userName = 'Name must be at least 2 characters';
    }

    if (!profileForm.userPhoneNumber) {
      errors.userPhoneNumber = 'Phone number is required';
    } else if (!/^[0-9]{10,15}$/.test(profileForm.userPhoneNumber)) {
      errors.userPhoneNumber = 'Phone number must be 10-15 digits';
    }

    if (profileForm.bankAccNumber && !/^[0-9]{10,20}$/.test(profileForm.bankAccNumber)) {
      errors.bankAccNumber = 'Bank account number must be 10-20 digits';
    }

    return errors;
  };

  // Validate password form
  const validatePassword = () => {
    const errors = {};

    if (!passwordForm.currentPassword) {
      errors.currentPassword = 'Current password is required';
    }

    if (!passwordForm.newPassword) {
      errors.newPassword = 'New password is required';
    } else if (passwordForm.newPassword.length < 8) {
      errors.newPassword = 'Password must be at least 8 characters';
    } else if (!/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/.test(passwordForm.newPassword)) {
      errors.newPassword = 'Password must contain uppercase, lowercase, and number';
    }

    if (!passwordForm.confirmNewPassword) {
      errors.confirmNewPassword = 'Please confirm your new password';
    } else if (passwordForm.newPassword !== passwordForm.confirmNewPassword) {
      errors.confirmNewPassword = 'Passwords do not match';
    }

    return errors;
  };

  // Handle profile update
  const handleProfileSubmit = async (e) => {
    e.preventDefault();
    setProfileSuccess('');
    setProfileError('');

    const errors = validateProfile();
    if (Object.keys(errors).length > 0) {
      setProfileErrors(errors);
      return;
    }

    setProfileLoading(true);

    try {
      const response = await userService.updateProfile(profileForm);

      if (response.success) {
        setProfileSuccess('Profile updated successfully!');
        updateUser(response.data);
        setTimeout(() => setProfileSuccess(''), 3000);
      } else {
        setProfileError(response.error || 'Failed to update profile');
      }
    } catch (error) {
      setProfileError(error || 'An error occurred. Please try again.');
    } finally {
      setProfileLoading(false);
    }
  };

  // Handle password change
  const handlePasswordSubmit = async (e) => {
    e.preventDefault();
    setPasswordSuccess('');
    setPasswordError('');

    const errors = validatePassword();
    if (Object.keys(errors).length > 0) {
      setPasswordErrors(errors);
      return;
    }

    setPasswordLoading(true);

    try {
      const response = await userService.changePassword(passwordForm);

      if (response.success) {
        setPasswordSuccess('Password changed successfully!');
        setPasswordForm({
          currentPassword: '',
          newPassword: '',
          confirmNewPassword: '',
        });
        setTimeout(() => setPasswordSuccess(''), 3000);
      } else {
        setPasswordError(response.error || 'Failed to change password');
      }
    } catch (error) {
      setPasswordError(error || 'An error occurred. Please try again.');
    } finally {
      setPasswordLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading profile...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex justify-between items-center">
            <div>
              <h1 className="text-2xl font-bold text-gray-900">Edit Profile</h1>
              <p className="text-sm text-gray-600">Update your personal information</p>
            </div>
            <button
              onClick={() => navigate('/dashboard')}
              className="btn-secondary"
            >
              Back to Dashboard
            </button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="space-y-6">
          {/* Profile Information Card */}
          <div className="card">
            <h2 className="text-xl font-semibold text-gray-900 mb-6">Profile Information</h2>

            {/* Success Message */}
            {profileSuccess && (
              <div className="mb-4 p-4 bg-green-50 border border-green-200 rounded-lg">
                <p className="text-sm text-green-600">✓ {profileSuccess}</p>
              </div>
            )}

            {/* Error Message */}
            {profileError && (
              <div className="mb-4 p-4 bg-red-50 border border-red-200 rounded-lg">
                <p className="text-sm text-red-600">{profileError}</p>
              </div>
            )}

            <form onSubmit={handleProfileSubmit} className="space-y-6">
              {/* Personal Information */}
              <div>
                <h3 className="text-lg font-medium text-gray-900 mb-4">Personal Details</h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {/* Full Name */}
                  <div className="md:col-span-2">
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Full Name <span className="text-red-500">*</span>
                    </label>
                    <input
                      name="userName"
                      type="text"
                      value={profileForm.userName}
                      onChange={handleProfileChange}
                      className={`input-field ${profileErrors.userName ? 'border-red-500' : ''}`}
                    />
                    {profileErrors.userName && (
                      <p className="mt-1 text-sm text-red-600">{profileErrors.userName}</p>
                    )}
                  </div>

                  {/* Phone Number */}
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Phone Number <span className="text-red-500">*</span>
                    </label>
                    <input
                      name="userPhoneNumber"
                      type="tel"
                      value={profileForm.userPhoneNumber}
                      onChange={handleProfileChange}
                      className={`input-field ${profileErrors.userPhoneNumber ? 'border-red-500' : ''}`}
                    />
                    {profileErrors.userPhoneNumber && (
                      <p className="mt-1 text-sm text-red-600">{profileErrors.userPhoneNumber}</p>
                    )}
                  </div>

                  {/* Address */}
                  <div className="md:col-span-2">
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Address
                    </label>
                    <textarea
                      name="userAddress"
                      rows="3"
                      value={profileForm.userAddress}
                      onChange={handleProfileChange}
                      className="input-field"
                    />
                  </div>
                </div>
              </div>

              {/* Bank Information */}
              <div className="pt-6 border-t border-gray-200">
                <h3 className="text-lg font-medium text-gray-900 mb-4">Bank Information</h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {/* Bank Name */}
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Bank Name
                    </label>
                    <input
                      name="bankName"
                      type="text"
                      value={profileForm.bankName}
                      onChange={handleProfileChange}
                      className="input-field"
                    />
                  </div>

                  {/* Bank Account Number */}
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Bank Account Number
                    </label>
                    <input
                      name="bankAccNumber"
                      type="text"
                      value={profileForm.bankAccNumber}
                      onChange={handleProfileChange}
                      className={`input-field ${profileErrors.bankAccNumber ? 'border-red-500' : ''}`}
                    />
                    {profileErrors.bankAccNumber && (
                      <p className="mt-1 text-sm text-red-600">{profileErrors.bankAccNumber}</p>
                    )}
                  </div>
                </div>
              </div>

              {/* Submit Button */}
              <div className="pt-6 border-t border-gray-200">
                <button
                  type="submit"
                  disabled={profileLoading}
                  className="btn-primary"
                >
                  {profileLoading ? 'Saving...' : 'Save Changes'}
                </button>
              </div>
            </form>
          </div>

          {/* Change Password Card */}
          <div className="card">
            <h2 className="text-xl font-semibold text-gray-900 mb-6">Change Password</h2>

            {/* Success Message */}
            {passwordSuccess && (
              <div className="mb-4 p-4 bg-green-50 border border-green-200 rounded-lg">
                <p className="text-sm text-green-600">✓ {passwordSuccess}</p>
              </div>
            )}

            {/* Error Message */}
            {passwordError && (
              <div className="mb-4 p-4 bg-red-50 border border-red-200 rounded-lg">
                <p className="text-sm text-red-600">{passwordError}</p>
              </div>
            )}

            <form onSubmit={handlePasswordSubmit} className="space-y-4">
              {/* Current Password */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Current Password <span className="text-red-500">*</span>
                </label>
                <input
                  name="currentPassword"
                  type="password"
                  value={passwordForm.currentPassword}
                  onChange={handlePasswordChange}
                  className={`input-field ${passwordErrors.currentPassword ? 'border-red-500' : ''}`}
                  placeholder="Enter current password"
                />
                {passwordErrors.currentPassword && (
                  <p className="mt-1 text-sm text-red-600">{passwordErrors.currentPassword}</p>
                )}
              </div>

              {/* New Password */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  New Password <span className="text-red-500">*</span>
                </label>
                <input
                  name="newPassword"
                  type="password"
                  value={passwordForm.newPassword}
                  onChange={handlePasswordChange}
                  className={`input-field ${passwordErrors.newPassword ? 'border-red-500' : ''}`}
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
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Confirm New Password <span className="text-red-500">*</span>
                </label>
                <input
                  name="confirmNewPassword"
                  type="password"
                  value={passwordForm.confirmNewPassword}
                  onChange={handlePasswordChange}
                  className={`input-field ${passwordErrors.confirmNewPassword ? 'border-red-500' : ''}`}
                  placeholder="Confirm new password"
                />
                {passwordErrors.confirmNewPassword && (
                  <p className="mt-1 text-sm text-red-600">{passwordErrors.confirmNewPassword}</p>
                )}
              </div>

              {/* Submit Button */}
              <div className="pt-4">
                <button
                  type="submit"
                  disabled={passwordLoading}
                  className="btn-primary"
                >
                  {passwordLoading ? 'Changing Password...' : 'Change Password'}
                </button>
              </div>
            </form>
          </div>
        </div>
      </main>
    </div>
  );
};

export default EditProfile;