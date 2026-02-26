import { RotateCcw, X, AlertTriangle, CreditCard } from 'lucide-react';

export function RevertModal({ isOpen, onClose, onConfirm, hasPaid = false, isBulk = false, count = 1, loading = false }) {
    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
            <div className="bg-white rounded-2xl max-w-md w-full shadow-xl">
                {/* Header */}
                <div className="flex items-center justify-between p-6 border-b border-gray-100">
                    <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-full bg-amber-100 flex items-center justify-center">
                            <RotateCcw className="w-5 h-5 text-amber-600" />
                        </div>
                        <h3 className="text-lg font-semibold text-gray-900">
                            {isBulk ? `Revert ${count} Application${count > 1 ? 's' : ''}` : 'Revert to Pending'}
                        </h3>
                    </div>
                    <button
                        onClick={onClose}
                        className="w-8 h-8 flex items-center justify-center rounded-lg hover:bg-gray-100 transition-colors"
                    >
                        <X className="w-4 h-4 text-gray-500" />
                    </button>
                </div>

                {/* Body */}
                <div className="p-6 space-y-4">
                    <p className="text-sm text-gray-600">
                        {isBulk
                            ? `Are you sure you want to revert ${count} selected application${count > 1 ? 's' : ''} back to Pending?`
                            : 'Are you sure you want to revert this application back to Pending?'}
                    </p>

                    {/* Payment Status Warning */}
                    {!isBulk && (
                        <div className={`rounded-lg p-4 flex items-start gap-3 ${
                            hasPaid
                                ? 'bg-red-50 border border-red-200'
                                : 'bg-blue-50 border border-blue-200'
                        }`}>
                            {hasPaid ? (
                                <>
                                    <AlertTriangle className="w-5 h-5 text-red-600 flex-shrink-0 mt-0.5" />
                                    <div>
                                        <p className="text-sm font-semibold text-red-800">Payment Already Made</p>
                                        <p className="text-sm text-red-700 mt-0.5">
                                            The business owner has <strong>already paid</strong> for this facility. Reverting will not automatically refund the payment. Please handle the refund separately.
                                        </p>
                                    </div>
                                </>
                            ) : (
                                <>
                                    <CreditCard className="w-5 h-5 text-blue-600 flex-shrink-0 mt-0.5" />
                                    <div>
                                        <p className="text-sm font-semibold text-blue-800">No Payment Made</p>
                                        <p className="text-sm text-blue-700 mt-0.5">
                                            The business owner has <strong>not yet paid</strong> for this facility. The pending payment record will be removed on revert.
                                        </p>
                                    </div>
                                </>
                            )}
                        </div>
                    )}

                    {isBulk && (
                        <div className="bg-amber-50 border border-amber-200 rounded-lg p-4 flex items-start gap-3">
                            <AlertTriangle className="w-5 h-5 text-amber-600 flex-shrink-0 mt-0.5" />
                            <p className="text-sm text-amber-800">
                                Unpaid payment records will be removed. If any applications have been paid, those payments will <strong>not</strong> be automatically refunded.
                            </p>
                        </div>
                    )}

                    {/* What happens */}
                    <div className="bg-gray-50 rounded-lg p-4">
                        <p className="text-xs font-semibold text-gray-600 uppercase tracking-wide mb-2">What will happen</p>
                        <ul className="space-y-1.5">
                            <li className="text-sm text-gray-600 flex items-center gap-2">
                                <span className="w-1.5 h-1.5 rounded-full bg-gray-400 flex-shrink-0" />
                                Application status will be set back to <strong>Pending</strong>
                            </li>
                            <li className="text-sm text-gray-600 flex items-center gap-2">
                                <span className="w-1.5 h-1.5 rounded-full bg-gray-400 flex-shrink-0" />
                                Facility quota will be <strong>restored</strong> (for approved applications)
                            </li>
                            <li className="text-sm text-gray-600 flex items-center gap-2">
                                <span className="w-1.5 h-1.5 rounded-full bg-gray-400 flex-shrink-0" />
                                Unpaid payment records will be <strong>deleted</strong>
                            </li>
                        </ul>
                    </div>
                </div>

                {/* Footer */}
                <div className="flex gap-3 p-6 pt-0">
                    <button
                        onClick={onClose}
                        disabled={loading}
                        className="flex-1 px-4 py-2.5 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors text-sm font-medium disabled:opacity-50"
                    >
                        Cancel
                    </button>
                    <button
                        onClick={onConfirm}
                        disabled={loading}
                        className="flex-1 px-4 py-2.5 bg-amber-600 text-white rounded-lg hover:bg-amber-700 transition-colors text-sm font-medium disabled:opacity-50 flex items-center justify-center gap-2"
                    >
                        {loading ? (
                            <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                        ) : (
                            <RotateCcw className="w-4 h-4" />
                        )}
                        Revert to Pending
                    </button>
                </div>
            </div>
        </div>
    );
}