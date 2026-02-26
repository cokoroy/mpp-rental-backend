import { Sidebar } from '../components/Sidebar';
import { BOEvents } from '../components/BOEvents';

export default function BOEventsPage() {
    return (
        <div className="flex h-screen bg-gray-50">
            <Sidebar currentView="events" />
            <main className="flex-1 overflow-y-auto p-8">
                <BOEvents />
            </main>
        </div>
    );
}