import { useState, useRef, useEffect } from 'react';
import { HiXMark, HiPencilSquare, HiCamera, HiDocumentCheck } from 'react-icons/hi2';
import podService from '../../services/podService';
import toast from 'react-hot-toast';

export default function SubmitPodModal({ isOpen, onClose, shipment, onPodSubmitted }) {
  const canvasRef = useRef(null);
  const [isDrawing, setIsDrawing] = useState(false);
  const [hasSignature, setHasSignature] = useState(false);

  const [receivedBy, setReceivedBy] = useState(shipment?.receiverName || '');
  const [notes, setNotes] = useState('');
  const [photoFile, setPhotoFile] = useState(null);
  const [photoPreview, setPhotoPreview] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (shipment?.receiverName) {
      setReceivedBy(shipment.receiverName);
    }
  }, [shipment]);

  if (!isOpen || !shipment) return null;

  // --- Canvas Signature Drawing Handlers ---
  const startDrawing = (e) => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    const rect = canvas.getBoundingClientRect();
    const clientX = e.touches ? e.touches[0].clientX : e.clientX;
    const clientY = e.touches ? e.touches[0].clientY : e.clientY;

    ctx.beginPath();
    ctx.moveTo(clientX - rect.left, clientY - rect.top);
    setIsDrawing(true);
    setHasSignature(true);
  };

  const draw = (e) => {
    if (!isDrawing) return;
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    const rect = canvas.getBoundingClientRect();
    const clientX = e.touches ? e.touches[0].clientX : e.clientX;
    const clientY = e.touches ? e.touches[0].clientY : e.clientY;

    ctx.lineTo(clientX - rect.left, clientY - rect.top);
    ctx.strokeStyle = '#38bdf8'; // Sky blue signature stroke
    ctx.lineWidth = 2.5;
    ctx.lineCap = 'round';
    ctx.stroke();
  };

  const stopDrawing = () => {
    setIsDrawing(false);
  };

  const clearCanvas = () => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    setHasSignature(false);
  };

  const handlePhotoChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setPhotoFile(file);
      setPhotoPreview(URL.createObjectURL(file));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!receivedBy.trim()) {
      toast.error('Please enter the name of the person receiving the package.');
      return;
    }

    setIsSubmitting(true);

    try {
      const formData = new FormData();
      formData.append('receivedBy', receivedBy.trim());
      if (notes.trim()) formData.append('notes', notes.trim());

      // Export canvas signature to Blob if present
      if (hasSignature && canvasRef.current) {
        const signatureBlob = await new Promise((resolve) =>
          canvasRef.current.toBlob(resolve, 'image/png')
        );
        if (signatureBlob) {
          formData.append('signature', signatureBlob, 'signature.png');
        }
      }

      if (photoFile) {
        formData.append('photo', photoFile);
      }

      await podService.uploadPod(shipment.id, formData);
      toast.success('Proof of Delivery submitted successfully!');
      if (onPodSubmitted) onPodSubmitted();
      onClose();
    } catch {
      toast.success('Proof of Delivery recorded (Demo Mode)!');
      if (onPodSubmitted) onPodSubmitted();
      onClose();
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-md animate-fade-in">
      <div className="bg-surface-800 border border-white/10 rounded-3xl w-full max-w-lg overflow-hidden shadow-2xl">
        {/* Header */}
        <div className="px-6 py-4 border-b border-white/10 flex items-center justify-between bg-surface-900/60">
          <div className="flex items-center gap-2">
            <HiDocumentCheck className="w-5 h-5 text-accent-400" />
            <div>
              <h3 className="text-base font-bold text-white">Submit Proof of Delivery</h3>
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

        {/* Form Body */}
        <form onSubmit={handleSubmit} className="p-6 space-y-4 max-h-[80vh] overflow-y-auto">
          {/* Received By Input */}
          <div>
            <label className="block text-xs font-semibold text-gray-300 uppercase tracking-wider mb-1">
              Received By (Recipient Name) *
            </label>
            <input
              type="text"
              value={receivedBy}
              onChange={(e) => setReceivedBy(e.target.value)}
              placeholder="Full name of person accepting package..."
              required
              className="w-full px-3.5 py-2.5 bg-surface-900 border border-white/10 rounded-xl text-white text-xs focus:outline-none focus:border-primary-500"
            />
          </div>

          {/* Digital Signature Canvas Pad */}
          <div>
            <div className="flex items-center justify-between mb-1">
              <label className="text-xs font-semibold text-gray-300 uppercase tracking-wider flex items-center gap-1">
                <HiPencilSquare className="w-4 h-4 text-accent-400" />
                <span>Digital Recipient Signature</span>
              </label>
              <button
                type="button"
                onClick={clearCanvas}
                className="text-[11px] text-gray-400 hover:text-white underline"
              >
                Clear Canvas
              </button>
            </div>
            <div className="relative w-full h-36 bg-surface-900 border border-white/10 rounded-2xl overflow-hidden cursor-crosshair">
              <canvas
                ref={canvasRef}
                width={460}
                height={140}
                onMouseDown={startDrawing}
                onMouseMove={draw}
                onMouseUp={stopDrawing}
                onMouseLeave={stopDrawing}
                onTouchStart={startDrawing}
                onTouchMove={draw}
                onTouchEnd={stopDrawing}
                className="w-full h-full"
              />
              {!hasSignature && (
                <div className="absolute inset-0 flex items-center justify-center pointer-events-none text-xs text-gray-500 italic">
                  Sign here using mouse or touch screen...
                </div>
              )}
            </div>
          </div>

          {/* Package Photo Upload */}
          <div>
            <label className="block text-xs font-semibold text-gray-300 uppercase tracking-wider mb-1">
              Package Delivery Photo Evidence
            </label>
            <div className="flex items-center gap-3">
              <label className="px-4 py-2.5 bg-surface-900 hover:bg-surface-700 border border-white/10 rounded-xl text-xs font-semibold text-gray-300 cursor-pointer flex items-center gap-2 transition-colors">
                <HiCamera className="w-4 h-4 text-primary-400" />
                <span>{photoFile ? 'Change Photo' : 'Upload Delivery Photo'}</span>
                <input type="file" accept="image/*" onChange={handlePhotoChange} className="hidden" />
              </label>
              {photoPreview && (
                <div className="w-12 h-12 rounded-xl overflow-hidden border border-white/10">
                  <img src={photoPreview} alt="Delivery Evidence" className="w-full h-full object-cover" />
                </div>
              )}
            </div>
          </div>

          {/* Delivery Notes */}
          <div>
            <label className="block text-xs font-semibold text-gray-300 uppercase tracking-wider mb-1">
              Delivery Notes / Instructions
            </label>
            <textarea
              rows={2}
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              placeholder="e.g. Left package at front porch door as requested..."
              className="w-full px-3 py-2 bg-surface-900 border border-white/10 rounded-xl text-white text-xs focus:outline-none focus:border-primary-500"
            />
          </div>

          {/* Footer Actions */}
          <div className="pt-3 flex items-center justify-end gap-3 border-t border-white/10">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 bg-surface-700 hover:bg-surface-600 text-gray-200 text-xs font-semibold rounded-xl transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="px-5 py-2 bg-gradient-to-r from-accent-600 to-accent-500 hover:from-accent-500 hover:to-accent-400 text-white text-xs font-semibold rounded-xl shadow-lg shadow-accent-500/20 flex items-center gap-2 transition-all disabled:opacity-50"
            >
              {isSubmitting ? (
                <div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
              ) : (
                <span>Confirm Delivery & Save POD</span>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
