import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';

// Auth Pages
import Login from './pages/Login';
import Register from './pages/Register';

// Dashboard & Profile
import Dashboard from './pages/Dashboard';
import Profile from './pages/Profile';
import EditProfile from './pages/EditProfile';

// Business Owner Pages
import MyBusinessesPage from './pages/MyBusinessesPage';
import CreateBusinessPage from './pages/CreateBusinessPage';
import BusinessDetailsPage from './pages/BusinessDetailsPage';
import EditBusinessPage from './pages/EditBusinessPage';

// MPP Business Management Pages
import BusinessManagementPage from './pages/BusinessManagementPage';
import BusinessDetailsPageMPP from './pages/BusinessDetailsPageMPP';
import UserManagementPage from './pages/UserManagementPage';


// MPP Facility Management Pages
import FacilityManagementPage from './pages/FacilityManagementPage';
import EventManagementPage from "./pages/EventManagementPage.jsx";
import BOEventsPage from "./pages/BOEventsPage.jsx";
import FacilityApprovalPage from './pages/FacilityApprovalPage';


function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          {/* Public Routes */}
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />

          {/* Protected Routes */}
          <Route
            path="/*"
            element={
              <ProtectedRoute>
                <Routes>
                  {/* Dashboard */}
                  <Route path="/dashboard" element={<Dashboard />} />

                  {/* Profile */}
                  <Route path="/profile" element={<Profile />} />
                  <Route path="/profile/edit" element={<EditProfile />} />

                  {/* Business Owner Routes */}
                  <Route path="/business" element={<MyBusinessesPage />} />
                  <Route path="/business/create" element={<CreateBusinessPage />} />
                  <Route path="/business/:id" element={<BusinessDetailsPage />} />
                  <Route path="/business/:id/edit" element={<EditBusinessPage />} />

                    {/* Business Owner - Events & Applications */}
                    <Route path="/events" element={<BOEventsPage />} />

                  {/* MPP Business Management Routes */}
                  <Route path="/mpp/businesses" element={<BusinessManagementPage />} />
                  <Route path="/mpp/businesses/:id" element={<BusinessDetailsPageMPP />} />

                  {/* MPP Facility Management Routes */}
                  <Route path="/mpp/facilities" element={<FacilityManagementPage />} />
                    <Route path="/mpp/events" element={<EventManagementPage />} />

                    <Route path="/mpp/approvals" element={<FacilityApprovalPage />} />


                    {/* MPP User Management Routes */}
                    <Route path="/user-management" element={<UserManagementPage  />} />

                  {/* Default redirect */}
                  <Route path="/" element={<Navigate to="/login" replace />} />

                </Routes>
              </ProtectedRoute>
            }
          />

          {/* Catch all - redirect to login */}
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App;