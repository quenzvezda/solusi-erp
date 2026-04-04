/**
 * ApprovalUI — Digital Signature & Approval Action Handler
 *
 * Depends on:
 *   - SignaturePad (loaded via CDN in master.html)
 *   - Bootstrap 5 Modal
 *   - HTMX (for timeline reload)
 *   - ErpModal / ErpFormHandler (global ERP helpers)
 */

const ApprovalUI = (() => {
    let signaturePad = null;

    /**
     * Initialize SignaturePad on the canvas.
     * Must be called after the modal is fully shown (Bootstrap 'shown.bs.modal' event).
     */
    function initSignaturePad() {
        const canvas = document.getElementById('signature-canvas');
        if (!canvas || !window.SignaturePad) return;

        // Resize canvas to actual pixel dimensions to avoid blurry rendering
        const ratio = Math.max(window.devicePixelRatio || 1, 1);
        canvas.width = canvas.offsetWidth * ratio;
        canvas.height = canvas.offsetHeight * ratio;
        canvas.getContext('2d').scale(ratio, ratio);

        if (signaturePad) {
            signaturePad.clear();
        } else {
            signaturePad = new SignaturePad(canvas, {
                backgroundColor: 'rgb(255, 255, 255)',
                penColor: 'rgb(0, 0, 0)',
                minWidth: 1,
                maxWidth: 3,
            });
        }
    }

    /**
     * Open the Approve modal with signature capture.
     */
    function openApproveModal() {
        const modal = new bootstrap.Modal(document.getElementById('modal-approve-signature'));
        const modalEl = document.getElementById('modal-approve-signature');

        // Init pad after modal is fully visible so canvas has correct dimensions
        modalEl.addEventListener('shown.bs.modal', function handler() {
            initSignaturePad();
            modalEl.removeEventListener('shown.bs.modal', handler);
        });

        // Reset state
        document.getElementById('approve-notes').value = '';
        hideError('signature-error');
        modal.show();
    }

    /**
     * Open the Reject modal.
     */
    function openRejectModal() {
        document.getElementById('reject-notes').value = '';
        hideError('reject-notes-error');
        const modal = new bootstrap.Modal(document.getElementById('modal-reject-approval'));
        modal.show();
    }

    /**
     * Clear the signature pad.
     */
    function clearSignature() {
        if (signaturePad) signaturePad.clear();
        hideError('signature-error');
    }

    /**
     * Submit an APPROVE action with signature.
     */
    function submitApprove() {
        if (!signaturePad || signaturePad.isEmpty()) {
            showError('signature-error');
            return;
        }

        const requestId = getRequestId();
        const signatureBase64 = signaturePad.toDataURL('image/png');
        const notes = document.getElementById('approve-notes').value || '';

        sendApprovalRequest(requestId, 'APPROVE', notes, signatureBase64, 'modal-approve-signature');
    }

    /**
     * Submit a REJECT action with mandatory notes.
     */
    function submitReject() {
        const notes = document.getElementById('reject-notes').value.trim();
        if (!notes) {
            showError('reject-notes-error');
            return;
        }

        const requestId = getRequestId();
        sendApprovalRequest(requestId, 'REJECT', notes, null, 'modal-reject-approval');
    }

    /**
     * Core AJAX call to POST /approval/{requestId}/process
     */
    function sendApprovalRequest(requestId, action, notes, signatureBase64, modalId) {
        const payload = { action, notes, signatureBase64 };

        const csrfHeader = document.cookie.match(/XSRF-TOKEN=([^;]+)/)?.[1] ?? '';
        const csrfHeaderName = document.body.getAttribute('hx-headers')
            ? JSON.parse(document.body.getAttribute('hx-headers').replace(/&quot;/g, '"'))
            : {};

        const headers = {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
        };
        // Merge CSRF header from hx-headers if available
        Object.assign(headers, csrfHeaderName);

        fetch(`/common/approval/${requestId}/process`, {
            method: 'POST',
            headers,
            body: JSON.stringify(payload),
        })
        .then(async res => {
            const data = await res.json();
            if (!res.ok) {
                throw new Error(data.message || 'Server error');
            }
            return data;
        })
        .then(data => {
            // Close modal
            const modalEl = document.getElementById(modalId);
            bootstrap.Modal.getInstance(modalEl)?.hide();

            // Trigger HTMX timeline reload via custom event
            document.body.dispatchEvent(new CustomEvent('approvalProcessed'));

            // Show success toast / modal
            if (window.ErpModal) {
                ErpModal.showSuccess(data.message || 'Action completed.');
            }

            // Hide action buttons — no more approvals allowed
            const btnContainer = document.getElementById('approval-action-buttons');
            if (btnContainer) btnContainer.classList.add('d-none');
        })
        .catch(err => {
            if (window.ErpModal) {
                ErpModal.showError(err.message || 'An unexpected error occurred.');
            } else {
                alert(err.message);
            }
        });
    }

    function getRequestId() {
        return document.getElementById('current-approval-request-id')?.value;
    }

    function showError(id) {
        const el = document.getElementById(id);
        if (el) el.classList.remove('d-none');
    }

    function hideError(id) {
        const el = document.getElementById(id);
        if (el) el.classList.add('d-none');
    }

    return { openApproveModal, openRejectModal, clearSignature, submitApprove, submitReject };
})();
