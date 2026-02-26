import { AlertCircle, X } from 'lucide-react';

export function ConfirmationModal({
                                      isOpen,
                                      onClose,
                                      onConfirm,
                                      title,
                                      message,
                                      confirmText = 'Confirm',
                                      cancelText = 'Cancel',
                                      type = 'danger' // 'danger' or 'success'
                                  }) {
    if (!isOpen) return null;

    const getTypeStyles = () => {
        switch (type) {
            case 'danger':
                return {
                    icon: 'bg-red-100 text-red-600',
                    button: 'bg-red-600 hover:bg-red-700 text-white',
                    text: 'text-red-600'
                };
            case 'success':
                return {
                    icon: 'bg-green-100 text-green-600',
                    button: 'bg-green-600 hover:bg-green-700 text-white',
                    text: 'text-green-600'
                };
            default:
                return {
                    icon: 'bg-red-100 text-red-600',
                    button: 'bg-red-600 hover:bg-red-700 text-white',
                    text: 'text-red-600'
                };
        }
    };

    const styles = getTypeStyles();

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
            <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-xl">
                {/* Header */}
                <div className="flex items-center justify-between mb-4">
                    <h3 className="text-xl font-semibold text-gray-900">{title}</h3>
                    <button
                        onClick={onClose}
                        className="w-8 h-8 flex items-center justify-center rounded-lg hover:bg-gray-100 transition-colors"
                    >
                        <X className="w-5 h-5 text-gray-500" />
                    </button>
                </div>

                {/* Content */}
                <div className="flex gap-4 mb-6">
                    <div className={`w-12 h-12 rounded-full flex items-center justify-center flex-shrink-0 ${styles.icon}`}>
                        <AlertCircle className="w-6 h-6" />
                    </div>
                    <div className="flex-1">
                        <p className={`text-base ${styles.text} font-medium mb-1`}>
                            {message}
                        </p>
                    </div>
                </div>

                {/* Action Buttons */}
                <div className="flex gap-3">
                    <button
                        onClick={onClose}
                        className="flex-1 px-4 py-3 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors font-medium"
                    >
                        {cancelText}
                    </button>
                    <button
                        onClick={() => {
                            onConfirm();
                            onClose();
                        }}
                        className={`flex-1 px-4 py-3 rounded-lg transition-colors font-medium ${styles.button}`}
                    >
                        {confirmText}
                    </button>
                </div>
            </div>
        </div>
    );
}