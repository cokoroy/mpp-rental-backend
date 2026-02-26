import { useEffect } from 'react';
import { CheckCircle, XCircle, AlertCircle, X } from 'lucide-react';

export function Toast({ type = 'success', message, isVisible, onClose, duration = 3000 }) {
    useEffect(() => {
        if (isVisible && duration > 0) {
            const timer = setTimeout(() => {
                onClose();
            }, duration);

            return () => clearTimeout(timer);
        }
    }, [isVisible, duration, onClose]);

    if (!isVisible) return null;

    const getToastStyles = () => {
        switch (type) {
            case 'success':
                return {
                    bg: 'bg-green-50 border-green-200',
                    text: 'text-green-800',
                    icon: <CheckCircle className="w-5 h-5 text-green-600" />,
                };
            case 'error':
                return {
                    bg: 'bg-red-50 border-red-200',
                    text: 'text-red-800',
                    icon: <XCircle className="w-5 h-5 text-red-600" />,
                };
            case 'warning':
                return {
                    bg: 'bg-yellow-50 border-yellow-200',
                    text: 'text-yellow-800',
                    icon: <AlertCircle className="w-5 h-5 text-yellow-600" />,
                };
            default:
                return {
                    bg: 'bg-blue-50 border-blue-200',
                    text: 'text-blue-800',
                    icon: <AlertCircle className="w-5 h-5 text-blue-600" />,
                };
        }
    };

    const styles = getToastStyles();

    return (
        <div className="fixed top-4 right-4 z-50 animate-slide-in-right">
            <div className={`flex items-center gap-3 px-4 py-3 rounded-lg border shadow-lg ${styles.bg} min-w-[320px] max-w-md`}>
                <div className="flex-shrink-0">
                    {styles.icon}
                </div>
                <p className={`flex-1 text-sm font-medium ${styles.text}`}>
                    {message}
                </p>
                <button
                    onClick={onClose}
                    className="flex-shrink-0 hover:opacity-70 transition-opacity"
                >
                    <X className={`w-4 h-4 ${styles.text}`} />
                </button>
            </div>
        </div>
    );
}

