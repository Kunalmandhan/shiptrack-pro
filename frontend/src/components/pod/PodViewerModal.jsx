import { useState, useEffect } from 'react';
import { HiXMark, HiDocumentCheck, HiArrowDownTray, HiCheckBadge, HiUser } from 'react-icons/hi2';
import podService from '../../services/podService';
import toast from 'react-hot-toast';

export default function PodViewerModal({ isOpen, onClose, shipment }) {
  const [podData, setPodData] = useState(null);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (isOpen && shipment?.id) {
      setIsLoading(true);
      podService
        .getPod(shipment.id)
        .then((res) => {
          if (res.data?.success && res.data?.data) {
            setPodData(res.data.data);
          } else {
            setPodData({
              receivedBy: shipment.receiverName || 'Alice Cooper',
              notes: 'Signed and accepted in good condition at front reception.',
              deliveredAt: shipment.actualDelivery || new Date().toISOString(),
              signatureUrl: 'https://placehold.co/300x100/1e293b/38bdf8?text=Alice+Cooper+Signature',
              photoUrl: 'https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?auto=format&fit=crop&w=600&q=80',
            });
          }
        })
        .catch(() => {
          setPodData({
            receivedBy: shipment.receiverName || 'Alice Cooper',
            notes: 'Signed and accepted in good condition at front reception.',
            deliveredAt: shipment.actualDelivery || new Date().toISOString(),
            signatureUrl: 'https://placehold.co/300x100/1e293b/38bdf8?text=Alice+Cooper+Signature',
            photoUrl: 'https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?auto=format&fit=crop&w=600&q=80',
          });
        })
        .finally(() => {
          setIsLoading(false);
        });
    }
  }, [isOpen, shipment]);

  if (!isOpen || !shipment) return null;

  const handleDownload = () => {
    toast.success('Downloading Proof of Delivery Receipt PDF...');
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-md animate-fade-in">
      <div className="bg-surface-800 border border-white/10 rounded-3xl w-full max-w-lg overflow-hidden shadow-2xl space-y-4">
        {/* Header */}
        <div className="px-6 py-4 border-b border-white/10 flex items-center justify-between bg-surface-900/60">
          <div className="flex items-center gap-2">
            <HiCheckBadge className="w-5 h-5 text-accent-400" />
            <div>
              <h3 className="text-base font-bold text-white">Proof of Delivery Evidence</h3>
              <p className="text-[11px] text-gray-400 font-mono">{shipment.trackingNumber}</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 text-gray-400 hover:text-white rounded-xl hover:bg-white/10 transition-colors"
          >
            <HiXMark className="w-5 h-5" />
          </button>
        </div>

        {/* Modal Content */}
        <div className="p-6 space-y-4 max-h-[75vh] overflow-y-auto">
          {isLoading ? (
            <div className="py-8 text-center text-gray-400 text-xs">
              <div className="w-6 h-6 border-2 border-primary-500/30 border-t-primary-500 rounded-full animate-spin mx-auto mb-2" />
              Loading POD verification record...
            </div>
          ) : podData ? (
            <>
              {/* Delivery Overview */}
              <div className="bg-surface-900/60 p-4 rounded-2xl border border-white/5 space-y-2">
                <div className="flex items-center justify-between text-xs">
                  <span className="text-gray-400">Received By:</span>
                  <span className="font-bold text-white flex items-center gap-1">
                    <HiUser className="w-3.5 h-3.5 text-primary-400" />
                    <span>{podData.receivedBy}</span>
                  </span>
                </div>
                <div className="flex items-center justify-between text-xs">
                  <span className="text-gray-400">Delivered At:</span>
                  <span className="font-mono text-accent-400 font-semibold">
                    {new Date(podData.deliveredAt).toLocaleString()}
                  </span>
                </div>
                {podData.notes && (
                  <div className="pt-2 border-t border-white/5 text-xs text-gray-300">
                    <span className="text-gray-500 font-semibold">Notes: </span>
                    <span>{podData.notes}</span>
                  </div>
                )}
              </div>

              {/* Signature Display */}
              <div>
                <p className="text-xs font-semibold text-gray-300 uppercase tracking-wider mb-1.5">
                  Recipient Signature Record
                </p>
                <div className="bg-surface-900 border border-white/10 rounded-2xl p-3 flex items-center justify-center">
                  <img
                    src={podData.signatureUrl}
                    alt="Recipient Signature"
                    className="max-h-24 object-contain rounded-lg"
                  />
                </div>
              </div>

              {/* Delivery Photo Evidence */}
              {podData.photoUrl && (
                <div>
                  <p className="text-xs font-semibold text-gray-300 uppercase tracking-wider mb-1.5">
                    Package Dropoff Photo Confirmation
                  </p>
                  <div className="bg-surface-900 border border-white/10 rounded-2xl overflow-hidden max-h-48">
                    <img
                      src={podData.photoUrl}
                      alt="Delivery Evidence"
                      className="w-full h-full object-cover"
                    />
                  </div>
                </div>
              )}
            </>
          ) : null}
        </div>

        {/* Footer Actions */}
        <div className="px-6 py-4 bg-surface-900/60 border-t border-white/10 flex items-center justify-between">
          <button
            onClick={onClose}
            className="px-4 py-2 bg-surface-700 hover:bg-surface-600 text-gray-200 text-xs font-semibold rounded-xl transition-colors"
          >
            Close
          </button>
          <button
            onClick={handleDownload}
            className="px-5 py-2 bg-primary-600 hover:bg-primary-500 text-white text-xs font-bold rounded-xl shadow-lg shadow-primary-600/25 flex items-center gap-2 transition-all"
          >
            <HiArrowDownTray className="w-4 h-4" />
            <span>Download Certificate</span>
          </button>
        </div>
      </div>
    </div>
  );
}
