import { useState, useEffect } from 'react';
import { Sidebar } from '../components/Sidebar';
import { ViewUserModal } from '../components/ViewUserModal';
import { EditUserModal } from '../components/EditUserModal';
import { ConfirmationModal } from '../components/ConfirmationModal';
import { Toast } from '../components/Toast';
import userManagementService from '../services/userManagementService';
import {
    Users,
    Eye,
    Edit,
    Search,
    Mail,
    Phone,
    Building2,
    Power,
    PowerOff,
    Loader2
} from 'lucide-react';

export default function UserManagementPage() {
    const [isSidebarOpen, setIsSidebarOpen] = useState(false);

    // State for users list
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    // State for filters
    const [searchQuery, setSearchQuery] = useState('');
    const [filterCategory, setFilterCategory] = useState('all');
    const [filterStatus, setFilterStatus] = useState('all');

    // State for modals
    const [viewingUser, setViewingUser] = useState(null);
    const [editingUser, setEditingUser] = useState(null);
    const [togglingUserId, setTogglingUserId] = useState(null);

    // State for confirmation modal
    const [confirmModal, setConfirmModal] = useState({
        isOpen: false,
        userId: null,
        currentStatus: null,
        type: 'danger'
    });

    // State for toast notification
    const [toast, setToast] = useState({
        isVisible: false,
        type: 'success',
        message: ''
    });

    // Show toast notification
    const showToast = (type, message) => {
        setToast({
            isVisible: true,
            type,
            message
        });
    };

    // Hide toast notification
    const hideToast = () => {
        setToast({
            ...toast,
            isVisible: false
        });
    };

    // Fetch users on component mount
    useEffect(() => {
        fetchUsers();
    }, []);

    // Fetch users with current filters
    const fetchUsers = async () => {
        setLoading(true);
        setError('');

        try {
            const filters = {
                searchQuery: searchQuery || undefined,
                category: filterCategory !== 'all' ? filterCategory : undefined,
                status: filterStatus !== 'all' ? filterStatus : undefined,
            };

            console.log('Fetching users with filters:', filters);
            const response = await userManagementService.searchUsers(filters);
            console.log('Fetch users response:', response);

            // Handle both wrapped and unwrapped responses
            let usersData;
            if (response.success) {
                usersData = response.data;
            } else if (Array.isArray(response)) {
                usersData = response;
            } else {
                setError('Unexpected response format');
                return;
            }

            setUsers(usersData);
            console.log('Users set:', usersData);
        } catch (err) {
            console.error('Error fetching users:', err);

            if (err.response?.status === 401) {
                setError('Unauthorized. Please login again.');
            } else if (err.response?.status === 403) {
                setError('Access denied. You need MPP permissions to access this page.');
            } else if (err.response?.status === 500) {
                setError('Server error. Please try again later.');
            } else if (err.message === 'Network Error') {
                setError('Cannot connect to server. Please check if backend is running.');
            } else {
                setError(err.response?.data?.message || 'Failed to load users. Please try again.');
            }
        } finally {
            setLoading(false);
        }
    };

    // Search with debounce
    useEffect(() => {
        const delayDebounce = setTimeout(() => {
            fetchUsers();
        }, 500);

        return () => clearTimeout(delayDebounce);
    }, [searchQuery, filterCategory, filterStatus]);

    // View user details
    const handleViewUser = async (userId) => {
        try {
            const response = await userManagementService.getUserDetails(userId);
            console.log('View user response:', response);

            let userData;
            if (response.success) {
                userData = response.data;
            } else if (response.userId) {
                userData = response;
            } else {
                throw new Error('Invalid response format');
            }

            setViewingUser(userData);
        } catch (err) {
            console.error('Error fetching user details:', err);
            showToast('error', 'Failed to load user details. Please try again.');
        }
    };

    // Edit user
    const handleEditUser = async (userId) => {
        try {
            const response = await userManagementService.getUserDetails(userId);
            console.log('Edit user response:', response);

            let userData;
            if (response.success) {
                userData = response.data;
            } else if (response.userId) {
                userData = response;
            } else {
                throw new Error('Invalid response format');
            }

            setEditingUser(userData);
        } catch (err) {
            console.error('Error fetching user details for edit:', err);
            showToast('error', 'Failed to load user details. Please try again.');
        }
    };

    // Save user changes
    const handleSaveUser = async (userData) => {
        try {
            const response = await userManagementService.updateUser(editingUser.userId, userData);
            console.log('Save user response:', response);

            if (response.success || response.userId) {
                fetchUsers();
                setEditingUser(null);
                showToast('success', 'User updated successfully!');
            } else {
                throw new Error('Update failed');
            }
        } catch (err) {
            console.error('Error updating user:', err);
            throw err;
        }
    };

    // Toggle user status - Show custom confirmation
    const handleToggleStatus = (userId, currentStatus) => {
        const type = currentStatus === 'ACTIVE' ? 'danger' : 'success';
        setConfirmModal({
            isOpen: true,
            userId: userId,
            currentStatus: currentStatus,
            type: type
        });
    };

    // Confirm toggle status
    const confirmToggleStatus = async () => {
        const { userId, currentStatus } = confirmModal;
        const action = currentStatus === 'ACTIVE' ? 'blocked' : 'activated';

        setTogglingUserId(userId);

        try {
            const response = await userManagementService.toggleUserStatus(userId);
            console.log('Toggle status response:', response);

            if (response.success || response.userId) {
                fetchUsers();
                showToast('success', `User ${action} successfully!`);
            } else {
                throw new Error('Status toggle failed');
            }
        } catch (err) {
            console.error('Error toggling user status:', err);
            showToast('error', 'Failed to update user status. Please try again.');
        } finally {
            setTogglingUserId(null);
        }
    };

    // Helper functions
    const getStatusColor = (status) => {
        switch (status) {
            case 'ACTIVE':
                return 'bg-green-100 text-green-700';
            case 'BLOCKED':
                return 'bg-red-100 text-red-700';
            case 'PENDING':
                return 'bg-yellow-100 text-yellow-700';
            default:
                return 'bg-gray-100 text-gray-700';
        }
    };

    const getUserCategoryColor = (category) => {
        switch (category) {
            case 'STUDENT':
                return 'bg-blue-100 text-blue-700';
            case 'NON_STUDENT':
                return 'bg-purple-100 text-purple-700';
            case 'MPP':
                return 'bg-orange-100 text-orange-700';
            default:
                return 'bg-gray-100 text-gray-700';
        }
    };

    const formatDate = (dateString) => {
        if (!dateString) return 'N/A';
        return new Date(dateString).toLocaleDateString('en-MY', {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
        });
    };

    return (
        <div className="flex min-h-screen bg-gray-50">
            <Sidebar currentView="user-management" />

            <div className="flex-1 md:ml-30">
                <div className="max-w-[1500px] mx-auto px-6 py-6 md:px-8">
                    <h1 className="text-3xl font-bold text-gray-900 mb-6">User Management</h1>

                    {/* Search and Filter Section */}
                    <div className="bg-white rounded-xl p-4 border border-gray-100 mb-6">
                        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                            <div className="relative">
                                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                                <input
                                    type="text"
                                    value={searchQuery}
                                    onChange={(e) => setSearchQuery(e.target.value)}
                                    placeholder="Search by name, email, or phone..."
                                    className="w-full pl-10 pr-4 py-2 rounded-lg border border-gray-200 focus:outline-none focus:border-purple-400"
                                />
                            </div>
                            <select
                                value={filterCategory}
                                onChange={(e) => setFilterCategory(e.target.value)}
                                className="px-4 py-2 rounded-lg border border-gray-200 focus:outline-none focus:border-purple-400"
                            >
                                <option value="all">All Categories</option>
                                <option value="MPP">MPP</option>
                                <option value="STUDENT">Student</option>
                                <option value="NON_STUDENT">Non-Student</option>
                            </select>
                            <select
                                value={filterStatus}
                                onChange={(e) => setFilterStatus(e.target.value)}
                                className="px-4 py-2 rounded-lg border border-gray-200 focus:outline-none focus:border-purple-400"
                            >
                                <option value="all">All Status</option>
                                <option value="ACTIVE">Active</option>
                                <option value="BLOCKED">Blocked</option>
                                <option value="PENDING">Pending</option>
                            </select>
                            <div className="flex items-center justify-center md:justify-start px-4 py-2 bg-purple-50 rounded-lg">
                                <p className="text-sm text-purple-700 font-medium">
                                    {users.length} {users.length === 1 ? 'user' : 'users'} found
                                </p>
                            </div>
                        </div>
                    </div>

                    {error && (
                        <div className="bg-red-50 border border-red-200 rounded-xl p-4 mb-6">
                            <p className="text-red-600">{error}</p>
                        </div>
                    )}

                    {loading ? (
                        <div className="bg-white rounded-xl p-12 border border-gray-100 text-center">
                            <Loader2 className="w-12 h-12 text-purple-600 mx-auto mb-4 animate-spin" />
                            <p className="text-gray-500">Loading users...</p>
                        </div>
                    ) : users.length === 0 ? (
                        <div className="bg-white rounded-xl p-12 border border-gray-100 text-center">
                            <Users className="w-12 h-12 text-gray-300 mx-auto mb-4" />
                            <p className="text-gray-500">No users found</p>
                        </div>
                    ) : (
                        <div className="space-y-4">
                            {users.map((user) => (
                                <div
                                    key={user.userId}
                                    className="bg-white rounded-xl p-6 border border-gray-100 hover:shadow-md transition-shadow"
                                >
                                    <div className="flex flex-col lg:flex-row lg:items-start justify-between gap-4">
                                        <div className="flex-1">
                                            <div className="flex items-start gap-3 mb-3">
                                                <div className="w-12 h-12 bg-purple-100 rounded-full flex items-center justify-center flex-shrink-0">
                                                    <span className="text-purple-600 font-semibold text-lg">
                                                        {user.userName.charAt(0).toUpperCase()}
                                                    </span>
                                                </div>
                                                <div className="flex-1 min-w-0">
                                                    <div className="flex items-center gap-2 mb-2 flex-wrap">
                                                        <h3 className="text-lg font-semibold text-gray-900">{user.userName}</h3>
                                                        <span className={`px-3 py-1 rounded-full text-xs font-medium ${getStatusColor(user.userStatus)}`}>
                                                            {user.userStatus}
                                                        </span>
                                                        <span className={`px-3 py-1 rounded-full text-xs font-medium ${getUserCategoryColor(user.userCategory)}`}>
                                                            {user.userCategory?.replace('_', ' ')}
                                                        </span>
                                                    </div>
                                                    <div className="space-y-1 text-sm text-gray-600">
                                                        <p className="flex items-center gap-2">
                                                            <Mail className="w-4 h-4 text-gray-400" />
                                                            {user.userEmail}
                                                        </p>
                                                        <p className="flex items-center gap-2">
                                                            <Phone className="w-4 h-4 text-gray-400" />
                                                            {user.userPhoneNumber}
                                                        </p>
                                                    </div>
                                                </div>
                                            </div>
                                            <div className="ml-15">
                                                <p className="text-sm text-gray-500 mb-2">
                                                    <span className="font-medium">{user.businesses?.length || 0}</span> Business
                                                    {user.businesses?.length !== 1 ? 'es' : ''} • Registered {formatDate(user.userRegisteredAt)}
                                                </p>
                                                {user.businesses && user.businesses.length > 0 && (
                                                    <div className="flex flex-wrap gap-2">
                                                        {user.businesses.map((business) => (
                                                            <span
                                                                key={business.businessId}
                                                                className="inline-flex items-center gap-1 px-2 py-1 bg-gray-50 rounded text-xs text-gray-700"
                                                            >
                                                                <Building2 className="w-3 h-3" />
                                                                {business.businessName}
                                                            </span>
                                                        ))}
                                                    </div>
                                                )}
                                            </div>
                                        </div>
                                        <div className="flex flex-wrap lg:flex-col gap-2 lg:min-w-[120px]">
                                            <button
                                                onClick={() => handleViewUser(user.userId)}
                                                className="flex-1 lg:flex-none px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors flex items-center justify-center gap-2 text-sm font-medium"
                                            >
                                                <Eye className="w-4 h-4" />
                                                View
                                            </button>
                                            <button
                                                onClick={() => handleEditUser(user.userId)}
                                                className="flex-1 lg:flex-none px-4 py-2 bg-purple-100 text-purple-700 rounded-lg hover:bg-purple-200 transition-colors flex items-center justify-center gap-2 text-sm font-medium"
                                            >
                                                <Edit className="w-4 h-4" />
                                                Edit
                                            </button>
                                            <button
                                                onClick={() => handleToggleStatus(user.userId, user.userStatus)}
                                                disabled={togglingUserId === user.userId}
                                                className={`flex-1 lg:flex-none px-4 py-2 rounded-lg transition-colors flex items-center justify-center gap-2 text-sm font-medium disabled:opacity-50 disabled:cursor-not-allowed ${
                                                    user.userStatus === 'ACTIVE'
                                                        ? 'bg-red-50 text-red-700 border border-red-200 hover:bg-red-100'
                                                        : 'bg-green-50 text-green-700 border border-green-200 hover:bg-green-100'
                                                }`}
                                            >
                                                {togglingUserId === user.userId ? (
                                                    <Loader2 className="w-4 h-4 animate-spin" />
                                                ) : user.userStatus === 'ACTIVE' ? (
                                                    <>
                                                        <PowerOff className="w-4 h-4" />
                                                        Block
                                                    </>
                                                ) : (
                                                    <>
                                                        <Power className="w-4 h-4" />
                                                        Activate
                                                    </>
                                                )}
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </div>

            {/* Toast Notification */}
            <Toast
                type={toast.type}
                message={toast.message}
                isVisible={toast.isVisible}
                onClose={hideToast}
                duration={3000}
            />

            {viewingUser && (
                <ViewUserModal
                    user={viewingUser}
                    onClose={() => setViewingUser(null)}
                />
            )}

            {editingUser && (
                <EditUserModal
                    user={editingUser}
                    onClose={() => setEditingUser(null)}
                    onSave={handleSaveUser}
                />
            )}

            <ConfirmationModal
                isOpen={confirmModal.isOpen}
                onClose={() => setConfirmModal({ ...confirmModal, isOpen: false })}
                onConfirm={confirmToggleStatus}
                title={confirmModal.currentStatus === 'ACTIVE' ? 'Block User' : 'Activate User'}
                message={
                    confirmModal.currentStatus === 'ACTIVE'
                        ? 'Are you sure you want to block this user? This user will be blocked and cannot access the system until activated again.'
                        : 'Are you sure you want to activate this user? This user will be able to access the system again.'
                }
                confirmText={confirmModal.currentStatus === 'ACTIVE' ? 'Block User' : 'Activate User'}
                cancelText="Cancel"
                type={confirmModal.type}
            />
        </div>
    );
}