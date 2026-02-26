import { Sidebar } from '../components/Sidebar';
import { FacilityApproval } from '../components/FacilityApproval';

export default function FacilityApprovalPage() {
    return (
        <div className="flex h-screen bg-gray-50">
            <Sidebar currentView="facility-approvals" />
            <main className="flex-1 overflow-y-auto p-8">
                <FacilityApproval />
            </main>
        </div>
    );
}