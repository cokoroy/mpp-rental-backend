import React from 'react';
import { PowerOff, AlertCircle } from 'lucide-react';

const BlockBusinessModal = ({ isOpen, onClose, business, onConfirm, isBlocking }) => {
  if (!isOpen || !business) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg shadow-xl max-w-md w-full">
        {/* Header */}
        <div className="px-6 py-4 border-b border-gray-200">
          <h2 className="text-xl font-semibold text-gray-800">Block Business</h2>
        </div>

        {/* Content */}
        <div className="px-6 py-4">
          <div className="flex items-start gap-4">
            <div className="flex-shrink-0">
              <div className="w-12 h-12 bg-red-100 rounded-full flex items-center justify-center">
                <PowerOff className="w-6 h-6 text-red-600" />
              </div>
            </div>
            <div className="flex-1">
              <p className="text-gray-700 mb-2">
                Are you sure you want to block{' '}
                <span className="font-semibold text-gray-900">"{business.businessName}"</span>?
              </p>
              <p className="text-sm text-red-600">
                This business will be blocked and cannot submit new facility applications until activated again.
              </p>
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="px-6 py-4 border-t border-gray-200 flex gap-3">
          <button
            onClick={onClose}
            disabled={isBlocking}
            className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Cancel
          </button>
          <button
            onClick={() => onConfirm(business.businessId, business.businessName)}
            disabled={isBlocking}
            className="flex-1 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
          >
            {isBlocking ? (
              <>
                <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                Blocking...
              </>
            ) : (
              <>
                <PowerOff className="w-4 h-4" />
                Block Business
              </>
            )}
          </button>
        </div>
      </div>
    </div>
  );
};

export default BlockBusinessModal;
