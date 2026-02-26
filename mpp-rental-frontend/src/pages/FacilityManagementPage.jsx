import { Sidebar } from '../components/Sidebar';
import { FacilityManagement } from '../components/FacilityManagement';

export default function FacilityManagementPage() {
  return (
    <div className="flex h-screen bg-gray-50">
      <Sidebar currentView="facility-management" />
      <main className="flex-1 overflow-y-auto">
        <div className="max-w-7xl mx-auto p-8">
          <FacilityManagement />
        </div>
      </main>
    </div>
  );
}
