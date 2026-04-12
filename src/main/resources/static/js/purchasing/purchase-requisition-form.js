document.addEventListener('DOMContentLoaded', function () {
    const config = window.PurchaseRequisitionPageConfig || {};
    if (config.isLocked) return;

    const lineContainer = document.getElementById('line-container');
    const templateSource = document.getElementById('row-template-source');
    const emptyMsg = document.getElementById('empty-msg');
    const btnAddLine = document.getElementById('btn-add-line');

    function getNextIndex() {
        const rows = lineContainer.querySelectorAll('.line-row');
        let maxIndex = -1;
        rows.forEach(function (row) {
            const idx = parseInt(row.getAttribute('data-index') || '-1', 10);
            if (idx > maxIndex) maxIndex = idx;
        });
        return maxIndex + 1;
    }

    function updateEmptyMessage() {
        if (emptyMsg) {
            const hasRows = lineContainer.querySelectorAll('.line-row').length > 0;
            emptyMsg.style.display = hasRows ? 'none' : '';
        }
    }

    if (btnAddLine) {
        btnAddLine.addEventListener('click', function () {
            const index = getNextIndex();
            const templateRow = templateSource.querySelector('.line-row');
            const newRow = templateRow.cloneNode(true);
            newRow.setAttribute('data-index', index);

            newRow.innerHTML = newRow.innerHTML.replace(/INDEX/g, index);

            // Remove stale TomSelect markup from cloned row so it can be re-initialized
            newRow.querySelectorAll('.ts-wrapper').forEach(w => w.remove());
            newRow.querySelectorAll('select.tomselect-initialized').forEach(s => {
                s.classList.remove('tomselect-initialized', 'tomselected', 'ts-hidden-accessible');
                s.style.display = '';
            });

            lineContainer.appendChild(newRow);
            updateEmptyMessage();

            // Re-initialize autocomplete on new row
            if (window.ERP && window.ERP.initAutocompleteInContainer) {
                window.ERP.initAutocompleteInContainer(newRow);
            }

            // Re-initialize numeric inputs on new row
            if (typeof initNumericInputs === 'function') {
                initNumericInputs(newRow);
            }
        });
    }

    lineContainer.addEventListener('click', function (e) {
        const btn = e.target.closest('.btn-remove-line');
        if (btn) {
            const row = btn.closest('.line-row');
            if (row) {
                row.remove();
                updateEmptyMessage();
            }
        }
    });

    // Submit for Approval handler
    const btnSubmitPr = document.getElementById('btn-submit-pr');
    if (btnSubmitPr) {
        btnSubmitPr.addEventListener('click', function () {
            const modalEl = document.getElementById('modal-submit-approval');
            if (!modalEl) return;

            // Init TomSelect for approver lookup after modal is shown
            modalEl.addEventListener('shown.bs.modal', function handler() {
                const selectEl = document.getElementById('submit-approver');
                if (selectEl && !selectEl.tomselect && typeof initLookup === 'function') {
                    initLookup(selectEl, selectEl.getAttribute('data-lookup-path'));
                }
                modalEl.removeEventListener('shown.bs.modal', handler);
            });

            // Reset state
            const selectEl = document.getElementById('submit-approver');
            if (selectEl && selectEl.tomselect) selectEl.tomselect.clear();
            const errEl = document.getElementById('submit-approver-error');
            if (errEl) errEl.classList.add('d-none');

            // Bind confirm button
            const btn = document.getElementById('btn-confirm-submit-approval');
            const newBtn = btn.cloneNode(true);
            btn.parentNode.replaceChild(newBtn, btn);
            newBtn.addEventListener('click', function () {
                const approverSelect = document.getElementById('submit-approver');
                const approverId = approverSelect.tomselect ? approverSelect.tomselect.getValue() : approverSelect.value;
                if (!approverId) {
                    if (errEl) errEl.classList.remove('d-none');
                    return;
                }
                if (errEl) errEl.classList.add('d-none');

                fetch(config.submitUrl + '?approverId=' + encodeURIComponent(approverId), {
                    method: 'POST',
                    headers: {
                        'Accept': 'application/json',
                        [config.csrfHeader]: config.csrfToken
                    }
                })
                .then(function (res) { return res.json().then(function (data) { return { ok: res.ok, data: data }; }); })
                .then(function (result) {
                    if (!result.ok) throw new Error(result.data.message || 'Error');
                    var closeBtn = modalEl.querySelector('[data-bs-dismiss="modal"]');
                    if (closeBtn) closeBtn.click();
                    window.location.href = '/purchasing/purchase-requisitions';
                })
                .catch(function (err) {
                    if (window.ErpModal) ErpModal.showError(err.message);
                    else alert(err.message);
                });
            });

            new bootstrap.Modal(modalEl).show();
        });
    }

    updateEmptyMessage();
});
