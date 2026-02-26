import React from 'react';
import { Power, CheckCircle } from 'lucide-react';

const ActivateBusinessModal = ({ isOpen, onClose, business, onConfirm, isActivating }) => {
  if (!isOpen || !business) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg shadow-xl max-w-md w-full">
        {/* Header */}
        <div className="px-6 py-4 border-b border-gray-200">
          <h2 className="text-xl font-semibold text-gray-800">Activate Business</h2>
        </div>

        {/* Content */}
        <div className="px-6 py-4">
          <div className="flex items-start gap-4">
            <div className="flex-shrink-0">
              <div className="w-12 h-12 bg-green-100 rounded-full flex items-center justify-center">
                <Power className="w-6 h-6 text-green-600" />
              </div>
            </div>
            <div className="flex-1">
              <p className="text-gray-700 mb-2">
                Are you sure you want to activate{' '}
                <span className="font-semibold text-gray-900">"{business.businessName}"</span>?
              </p>
              <p className="text-sm text-green-600">
                This business will be activated and can submit facility applications again.
              </p>
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="px-6 py-4 border-t border-gray-200 flex gap-3">
          <button
            onClick={onClose}
            disabled={isActivating}
            className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Cancel
          </button>
          <button
            onClick={() => onConfirm(business.businessId, business.businessName)}
            disabled={isActivating}
            className="flex-1 px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
          >
            {isActivating ? (
              <>
                <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                Activating...
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
  );
};

export default ActivateBusinessModal;
