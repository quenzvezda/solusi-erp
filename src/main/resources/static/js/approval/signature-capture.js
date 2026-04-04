/**
 * ApprovalUI — 4-Action Approval Handler with Digital Signature
 *
 * Supports: APPROVE_AND_FINISH, APPROVE_AND_FORWARD, FORWARD, REJECTED
 *
 * Depends on:
 *   - SignaturePad (loaded via CDN in master.html)
 *   - Bootstrap 5 Modal
 *   - HTMX (for timeline reload)
 *   - ErpModal / ErpFormHandler (global ERP helpers)
 *   - TomSelect + initLookup (for approver autocomplete)
 */

const ApprovalUI = (() => {
    const signaturePads = {};

    /**
     * Initialize SignaturePad on a given canvas.
     */
    function initSignaturePad(canvasId) {
        const canvas = document.getElementById(canvasId);
        if (!canvas || !window.SignaturePad) return;

        const ratio = Math.max(window.devicePixelRatio || 1, 1);
        canvas.width = canvas.offsetWidth * ratio;
        canvas.height = canvas.offsetHeight * ratio;
        canvas.getContext('2d').scale(ratio, ratio);

        if (signaturePads[canvasId]) {
            signaturePads[canvasId].clear();
        } else {
            signaturePads[canvasId] = new SignaturePad(canvas, {
                backgroundColor: 'rgb(255, 255, 255)',
                penColor: 'rgb(0, 0, 0)',
                minWidth: 1,
                maxWidth: 3,
            });
        }
    }

    /**
     * Init TomSelect on approver select elements inside a modal.
     */
    function initApproverLookup(selectId) {
        const selectEl = document.getElementById(selectId);
        if (selectEl && !selectEl.tomselect && typeof initLookup === 'function') {
            initLookup(selectEl, selectEl.getAttribute('data-lookup-path'));
        }
    }

    // ─── Modal Openers ───

    function triggerModal(triggerId) {
        const btn = document.getElementById(triggerId);
        if (btn) btn.click();
    }

    function closeModal(modalId) {
        const modalEl = document.getElementById(modalId);
        if (!modalEl) return;
        const closeBtn = modalEl.querySelector('[data-bs-dismiss="modal"]');
        if (closeBtn) closeBtn.click();
    }

    function openApproveFinishModal() {
        const modalEl = document.getElementById('modal-approve-finish');
        modalEl.addEventListener('shown.bs.modal', function handler() {
            initSignaturePad('sig-canvas-approve-finish');
            modalEl.removeEventListener('shown.bs.modal', handler);
        });
        document.getElementById('approve-finish-notes').value = '';
        hideError('approve-finish-notes-error');
        hideError('sig-error-approve-finish');
        triggerModal('btn-trigger-modal-approve-finish');
    }

    function openApproveForwardModal() {
        const modalEl = document.getElementById('modal-approve-forward');
        modalEl.addEventListener('shown.bs.modal', function handler() {
            initSignaturePad('sig-canvas-approve-forward');
            initApproverLookup('approve-forward-approver');
            modalEl.removeEventListener('shown.bs.modal', handler);
        });
        document.getElementById('approve-forward-notes').value = '';
        const sel = document.getElementById('approve-forward-approver');
        if (sel && sel.tomselect) sel.tomselect.clear();
        hideError('approve-forward-notes-error');
        hideError('approve-forward-approver-error');
        hideError('sig-error-approve-forward');
        triggerModal('btn-trigger-modal-approve-forward');
    }

    function openForwardModal() {
        const modalEl = document.getElementById('modal-forward');
        modalEl.addEventListener('shown.bs.modal', function handler() {
            initApproverLookup('forward-approver');
            modalEl.removeEventListener('shown.bs.modal', handler);
        });
        document.getElementById('forward-notes').value = '';
        const sel = document.getElementById('forward-approver');
        if (sel && sel.tomselect) sel.tomselect.clear();
        hideError('forward-notes-error');
        hideError('forward-approver-error');
        triggerModal('btn-trigger-modal-forward');
    }

    function openRejectModal() {
        document.getElementById('reject-notes').value = '';
        hideError('reject-notes-error');
        triggerModal('btn-trigger-modal-reject');
    }

    // ─── Clear Signature ───

    function clearSignature(prefix) {
        const canvasId = 'sig-canvas-' + (prefix || 'approve-finish');
        if (signaturePads[canvasId]) signaturePads[canvasId].clear();
        hideError('sig-error-' + (prefix || 'approve-finish'));
    }

    // ─── Submit Handlers ───

    function submitApproveFinish() {
        const notes = document.getElementById('approve-finish-notes').value.trim();
        if (!notes) { showError('approve-finish-notes-error'); return; }
        hideError('approve-finish-notes-error');

        const pad = signaturePads['sig-canvas-approve-finish'];
        if (!pad || pad.isEmpty()) { showError('sig-error-approve-finish'); return; }
        hideError('sig-error-approve-finish');

        const requestId = getRequestId();
        const signatureBase64 = pad.toDataURL('image/png');
        sendApprovalRequest(requestId, 'APPROVE_AND_FINISH', notes, signatureBase64, null, 'modal-approve-finish');
    }

    function submitApproveForward() {
        const notes = document.getElementById('approve-forward-notes').value.trim();
        if (!notes) { showError('approve-forward-notes-error'); return; }
        hideError('approve-forward-notes-error');

        const sel = document.getElementById('approve-forward-approver');
        const targetApproverId = sel.tomselect ? sel.tomselect.getValue() : sel.value;
        if (!targetApproverId) { showError('approve-forward-approver-error'); return; }
        hideError('approve-forward-approver-error');

        const pad = signaturePads['sig-canvas-approve-forward'];
        if (!pad || pad.isEmpty()) { showError('sig-error-approve-forward'); return; }
        hideError('sig-error-approve-forward');

        const requestId = getRequestId();
        const signatureBase64 = pad.toDataURL('image/png');
        sendApprovalRequest(requestId, 'APPROVE_AND_FORWARD', notes, signatureBase64, parseInt(targetApproverId), 'modal-approve-forward');
    }

    function submitForward() {
        const notes = document.getElementById('forward-notes').value.trim();
        if (!notes) { showError('forward-notes-error'); return; }
        hideError('forward-notes-error');

        const sel = document.getElementById('forward-approver');
        const targetApproverId = sel.tomselect ? sel.tomselect.getValue() : sel.value;
        if (!targetApproverId) { showError('forward-approver-error'); return; }
        hideError('forward-approver-error');

        const requestId = getRequestId();
        sendApprovalRequest(requestId, 'FORWARD', notes, null, parseInt(targetApproverId), 'modal-forward');
    }

    function submitReject() {
        const notes = document.getElementById('reject-notes').value.trim();
        if (!notes) { showError('reject-notes-error'); return; }
        hideError('reject-notes-error');

        const requestId = getRequestId();
        sendApprovalRequest(requestId, 'REJECTED', notes, null, null, 'modal-reject-approval');
    }

    // ─── Core AJAX ───

    function sendApprovalRequest(requestId, action, notes, signatureBase64, targetApproverId, modalId) {
        const payload = { action, notes };
        if (signatureBase64) payload.signatureBase64 = signatureBase64;
        if (targetApproverId) payload.targetApproverId = targetApproverId;

        const csrfHeaderAttr = document.body.getAttribute('hx-headers');
        const headers = {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
        };
        if (csrfHeaderAttr) {
            try { Object.assign(headers, JSON.parse(csrfHeaderAttr.replace(/&quot;/g, '"'))); } catch (e) { /* ignore */ }
        }

        fetch(`/common/approval/${requestId}/process`, {
            method: 'POST',
            headers,
            body: JSON.stringify(payload),
        })
        .then(async res => {
            const data = await res.json();
            if (!res.ok) throw new Error(data.message || 'Server error');
            return data;
        })
        .then(data => {
            closeModal(modalId);
            document.body.dispatchEvent(new CustomEvent('approvalProcessed'));
            if (window.ErpModal) ErpModal.showSuccess(data.message || 'Action completed.');

            // Hide action buttons after processing
            const btnContainer = document.getElementById('approval-action-buttons');
            if (btnContainer) btnContainer.classList.add('d-none');
        })
        .catch(err => {
            if (window.ErpModal) ErpModal.showError(err.message || 'An unexpected error occurred.');
            else alert(err.message);
        });
    }

    // ─── Helpers ───

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

    return {
        openApproveFinishModal, openApproveForwardModal, openForwardModal, openRejectModal,
        clearSignature, submitApproveFinish, submitApproveForward, submitForward, submitReject
    };
})();
