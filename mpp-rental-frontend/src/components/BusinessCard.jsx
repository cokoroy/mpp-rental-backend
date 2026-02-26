import React from 'react';
import { useNavigate } from 'react-router-dom';
import { STATUS_COLORS } from '../utils/businessConstants';

const BusinessCard = ({ business, onDelete, onUploadSSM }) => {
  const navigate = useNavigate();

  const handleViewDetails = () => {
    navigate(`/business/${business.businessId}`);
  };

  return (
    <div className="bg-white rounded-lg shadow-md hover:shadow-lg transition-shadow duration-300 p-6">
      {/* Header */}
      <div className="flex justify-between items-start mb-4">
        <div className="flex-1">
          <h3 className="text-xl font-semibold text-gray-800 mb-1">
            {business.businessName}
          </h3>
          <p className="text-sm text-gray-500">{business.businessCategory}</p>
        </div>
        
        {/* Status Badge */}
        <span
          className={`px-3 py-1 rounded-full text-xs font-medium ${
            STATUS_COLORS[business.businessStatus] || 'bg-gray-100 text-gray-800'
          }`}
        >
          {business.businessStatus}
        </span>
      </div>

      {/* Description */}
      {business.businessDesc && (
        <p className="text-gray-600 text-sm mb-4 line-clamp-2">
          {business.businessDesc}
        </p>
      )}

      {/* Details */}
      <div className="space-y-2 mb-4">
        {business.ssmNumber && (
          <div className="flex items-center text-sm">
            <span className="text-gray-500 w-24">SSM Number:</span>
            <span className="text-gray-700 font-medium">{business.ssmNumber}</span>
          </div>
        )}
        
        <div className="flex items-center text-sm">
          <span className="text-gray-500 w-24">Registered:</span>
          <span className="text-gray-700">
            {new Date(business.businessRegisteredAt).toLocaleDateString()}
          </span>
        </div>

        {business.ssmDocument && (
          <div className="flex items-center text-sm">
            <span className="text-gray-500 w-24">SSM Doc:</span>
            <span className="text-green-600 text-xs">✓ Uploaded</span>
          </div>
        )}
      </div>

      {/* Actions */}
      <div className="flex gap-2 pt-4 border-t border-gray-200">
        <button
          onClick={handleViewDetails}
          className="flex-1 bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors text-sm font-medium"
        >
          View Details
        </button>
        
        {!business.ssmDocument && business.businessStatus === 'ACTIVE' && (
          <button
            onClick={() => onUploadSSM(business)}
            className="flex-1 bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700 transition-colors text-sm font-medium"
          >
            Upload SSM
          </button>
        )}
        
        {business.businessStatus === 'ACTIVE' && (
          <button
            onClick={() => onDelete(business)}
            className="px-4 py-2 bg-red-100 text-red-600 rounded-lg hover:bg-red-200 transition-colors text-sm font-medium"
          >
            Delete
          </button>
        )}
      </div>
    </div>
  );
};

export default BusinessCard;
