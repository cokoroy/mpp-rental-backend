// Business Categories
export const BUSINESS_CATEGORIES = [
  'Food & Beverage',
  'Clothing & Fashion',
  'Electronics',
  'Books & Stationery',
  'Accessories',
  'Handicrafts',
  'Services',
  'Health & Beauty',
  'Sports & Fitness',
  'Others',
];

// Business Status
export const BUSINESS_STATUS = {
  ACTIVE: 'ACTIVE',
  BLOCKED: 'BLOCKED',
  INACTIVE: 'INACTIVE',
};

// Status Colors for UI
export const STATUS_COLORS = {
  ACTIVE: 'bg-green-100 text-green-800',
  BLOCKED: 'bg-red-100 text-red-800',
  INACTIVE: 'bg-gray-100 text-gray-800',
};

// User Categories
export const USER_CATEGORIES = {
  STUDENT: 'STUDENT',
  NON_STUDENT: 'NON_STUDENT',
  MPP: 'MPP',
};

// File Upload Constants
export const MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
export const ALLOWED_FILE_TYPES = ['application/pdf', 'image/jpeg', 'image/png'];
export const ALLOWED_FILE_EXTENSIONS = ['.pdf', '.jpg', '.jpeg', '.png'];
