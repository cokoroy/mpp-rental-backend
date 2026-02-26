import { Sidebar } from '../components/Sidebar';
import { EventManagement } from '../components/EventManagement.jsx';

export default function EventManagementPage() {
  return (
    <div className="flex h-screen bg-gray-50">
      <Sidebar currentView="event-management" />
      <main className="flex-1 overflow-y-auto p-8">
        <EventManagement />
      </main>
    </div>
  );
}
