import { XCircle, Building2, Mail, Phone, MapPin, CreditCard, Calendar, Key } from 'lucide-react';

export function ViewUserModal({ user, onClose }) {
    if (!user) return null;

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
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
            <div className="bg-white rounded-2xl max-w-4xl w-full max-h-[90vh] overflow-y-auto">
                {/* Header */}
                <div className="sticky top-0 bg-white border-b border-gray-200 p-6 flex items-center justify-between">
                    <h2 className="text-2xl font-semibold text-gray-900">User Details</h2>
                    <button
                        onClick={onClose}
                        className="w-8 h-8 flex items-center justify-center rounded-lg hover:bg-gray-100 transition-colors"
                    >
                        <XCircle className="w-5 h-5 text-gray-500" />
                    </button>
                </div>

                <div className="p-6 space-y-6">
                    {/* Personal Information */}
                    <div>
                        <h3 className="text-lg font-semibold text-gray-900 mb-4">Personal Information</h3>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <div>
                                <p className="text-sm text-gray-500 mb-1">Full Name</p>
                                <p className="text-gray-900 font-medium">{user.userName}</p>
                            </div>
                            <div>
                                <p className="text-sm text-gray-500 mb-1">Email</p>
                                <div className="flex items-center gap-2">
                                    <Mail className="w-4 h-4 text-gray-400" />
                                    <p className="text-gray-900">{user.userEmail}</p>
                                </div>
                            </div>
                            <div>
                                <p className="text-sm text-gray-500 mb-1">Phone Number</p>
                                <div className="flex items-center gap-2">
                                    <Phone className="w-4 h-4 text-gray-400" />
                                    <p className="text-gray-900">{user.userPhoneNumber}</p>
                                </div>
                            </div>
                            <div>
                                <p className="text-sm text-gray-500 mb-1">Address</p>
                                <div className="flex items-center gap-2">
                                    <MapPin className="w-4 h-4 text-gray-400" />
                                    <p className="text-gray-900">{user.userAddress || 'Not provided'}</p>
                                </div>
                            </div>
                            <div>
                                <p className="text-sm text-gray-500 mb-1">User Category</p>
                                <span className={`inline-block px-3 py-1 rounded-full text-xs font-medium ${getUserCategoryColor(user.userCategory)}`}>
                  {user.userCategory?.replace('_', ' ')}
                </span>
                            </div>
                            <div>
                                <p className="text-sm text-gray-500 mb-1">Status</p>
                                <span className={`inline-block px-3 py-1 rounded-full text-xs font-medium ${getStatusColor(user.userStatus)}`}>
                  {user.userStatus}
                </span>
                            </div>
                        </div>
                    </div>

                    {/* Bank Account Information */}
                    <div>
                        <h3 className="text-lg font-semibold text-gray-900 mb-4">Bank Account Information</h3>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <div>
                                <p className="text-sm text-gray-500 mb-1">Bank Name</p>
                                <div className="flex items-center gap-2">
                                    <Building2 className="w-4 h-4 text-gray-400" />
                                    <p className="text-gray-900">{user.bankName || 'Not provided'}</p>
                                </div>
                            </div>
                            <div>
                                <p className="text-sm text-gray-500 mb-1">Bank Account Number</p>
                                <div className="flex items-center gap-2">
                                    <CreditCard className="w-4 h-4 text-gray-400" />
                                    <p className="text-gray-900">{user.bankAccNumber || 'Not provided'}</p>
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* Password */}
                    <div>
                        <h3 className="text-lg font-semibold text-gray-900 mb-4">Account Security</h3>
                        <div>
                            <p className="text-sm text-gray-500 mb-1">Password (Encrypted)</p>
                            <div className="flex items-center gap-2 bg-gray-50 p-3 rounded-lg">
                                <Key className="w-4 h-4 text-gray-400" />
                                <p className="text-gray-700 font-mono text-sm break-all">{user.userPassword}</p>
                            </div>
                            <p className="text-xs text-gray-500 mt-1">
                                This is the encrypted password stored in the database. Use the Edit function to change it.
                            </p>
                        </div>
                    </div>

                    {/* Account Information */}
                    <div>
                        <h3 className="text-lg font-semibold text-gray-900 mb-4">Account Information</h3>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <div>
                                <p className="text-sm text-gray-500 mb-1">Registered Date</p>
                                <div className="flex items-center gap-2">
                                    <Calendar className="w-4 h-4 text-gray-400" />
                                    <p className="text-gray-900">{formatDate(user.userRegisteredAt)}</p>
                                </div>
                            </div>
                            <div>
                                <p className="text-sm text-gray-500 mb-1">Last Login</p>
                                <div className="flex items-center gap-2">
                                    <Calendar className="w-4 h-4 text-gray-400" />
                                    <p className="text-gray-900">{formatDate(user.userLastLogin)}</p>
                                </div>
                            </div>
                            <div>
                                <p className="text-sm text-gray-500 mb-1">Email Verified</p>
                                <span className={`inline-block px-3 py-1 rounded-full text-xs font-medium ${
                                    user.emailVerified ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'
                                }`}>
                  {user.emailVerified ? 'Verified' : 'Not Verified'}
                </span>
                            </div>
                        </div>
                    </div>

                    {/* Registered Businesses */}
                    <div>
                        <h3 className="text-lg font-semibold text-gray-900 mb-4">
                            Registered Businesses ({user.businesses?.length || 0})
                        </h3>
                        {user.businesses && user.businesses.length > 0 ? (
                            <div className="space-y-3">
                                {user.businesses.map((business) => (
                                    <div key={business.businessId} className="flex items-center justify-between p-4 bg-gray-50 rounded-lg border border-gray-200">
                                        <div className="flex items-center gap-3">
                                            <div className="w-10 h-10 bg-purple-100 rounded-lg flex items-center justify-center">
                                                <Building2 className="w-5 h-5 text-purple-600" />
                                            </div>
                                            <div>
                                                <p className="text-gray-900 font-medium">{business.businessName}</p>
                                                <p className="text-sm text-gray-500">{business.businessCategory}</p>
                                                {business.ssmNumber && (
                                                    <p className="text-xs text-gray-400">SSM: {business.ssmNumber}</p>
                                                )}
                                            </div>
                                        </div>
                                        <span className={`px-3 py-1 rounded-full text-xs font-medium ${getStatusColor(business.businessStatus)}`}>
                      {business.businessStatus}
                    </span>
                                    </div>
                                ))}
                            </div>
                        ) : (
                            <div className="text-center py-8 bg-gray-50 rounded-lg border border-gray-200">
                                <Building2 className="w-12 h-12 text-gray-300 mx-auto mb-2" />
                                <p className="text-gray-500">No businesses registered</p>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}