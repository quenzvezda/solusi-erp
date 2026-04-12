document.addEventListener('DOMContentLoaded', function () {
    const config = window.PurchaseOrderPageConfig || {};
    if (config.isLocked) return;

    const lineContainer = document.getElementById('line-container');
    const templateSource = document.getElementById('row-template-source');
    const emptyMsg = document.getElementById('empty-msg');
    const btnAddLine = document.getElementById('btn-add-line');

    // === PO Type Toggle ===
    const poTypeRadios = document.querySelectorAll('input[name="poType"]');
    const prRefGroup = document.getElementById('pr-reference-group');

    function togglePrReference() {
        const selected = document.querySelector('input[name="poType"]:checked');
        if (!selected || !prRefGroup) return;
        if (selected.value === 'STANDARD') {
            prRefGroup.style.display = '';
            loadApprovedPrs();
        } else {
            prRefGroup.style.display = 'none';
        }
    }

    function loadApprovedPrs() {
        const supplierInput = document.querySelector('input[name="supplierId"]');
        const supplierId = supplierInput ? supplierInput.value : '';
        const selectEl = document.getElementById('select-pr-reference');
        if (!selectEl) return;

        let url = '/purchasing/purchase-requisitions/api/approved';
        if (supplierId) url += '?supplierId=' + encodeURIComponent(supplierId);

        fetch(url, {
            headers: { 'Accept': 'application/json', [config.csrfHeader]: config.csrfToken }
        })
        .then(function(res) { return res.json(); })
        .then(function(data) {
            const currentVal = selectEl.value;
            // Keep first option
            while (selectEl.options.length > 1) selectEl.remove(1);
            data.forEach(function(item) {
                var opt = document.createElement('option');
                opt.value = item.id;
                opt.textContent = item.text;
                selectEl.appendChild(opt);
            });
            if (currentVal) selectEl.value = currentVal;
        })
        .catch(function() {});
    }

    poTypeRadios.forEach(function(radio) {
        radio.addEventListener('change', togglePrReference);
    });

    // Re-load PR list when supplier changes
    var supplierHiddenInput = document.querySelector('input[name="supplierId"]');
    if (supplierHiddenInput) {
        var observer = new MutationObserver(function() {
            var selected = document.querySelector('input[name="poType"]:checked');
            if (selected && selected.value === 'STANDARD') loadApprovedPrs();
        });
        observer.observe(supplierHiddenInput, { attributes: true, attributeFilter: ['value'] });
        // Also listen for input event from TomSelect
        supplierHiddenInput.addEventListener('change', function() {
            var selected = document.querySelector('input[name="poType"]:checked');
            if (selected && selected.value === 'STANDARD') loadApprovedPrs();
        });
    }

    // Initial toggle
    if (config.poType === 'STANDARD') {
        var stdRadio = document.getElementById('po-type-standard');
        if (stdRadio) stdRadio.checked = true;
    }
    togglePrReference();

    // === Line Management ===
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

    function recalculateLineRow(row) {
        var qtyInput = row.querySelector('.input-qty');
        var priceInput = row.querySelector('.input-unit-price');
        var taxRateInput = row.querySelector('.input-tax-rate');

        if (!qtyInput || !priceInput) return;

        var qty = parseFloat((qtyInput.value || '0').replace(/,/g, '')) || 0;
        var price = parseFloat((priceInput.value || '0').replace(/,/g, '')) || 0;
        var taxRate = taxRateInput ? (parseFloat((taxRateInput.value || '0').replace(/,/g, '')) || 0) : 0;

        var subtotal = qty * price;
        var tax = subtotal * taxRate;
        var total = subtotal + tax;

        var subtotalInput = row.querySelector('[name$=".lineSubtotal"]');
        var taxInput = row.querySelector('[name$=".lineTax"]');
        var totalInput = row.querySelector('[name$=".lineTotal"]');

        if (subtotalInput) subtotalInput.value = subtotal.toFixed(2);
        if (taxInput) taxInput.value = tax.toFixed(2);
        if (totalInput) totalInput.value = total.toFixed(2);

        // Update AutoNumeric if present
        [totalInput].forEach(function(input) {
            if (input && typeof AutoNumeric !== 'undefined') {
                var an = AutoNumeric.getAutoNumericElement(input);
                if (an) an.set(input.value);
            }
        });
    }

    // Attach recalculation to existing rows
    lineContainer.querySelectorAll('.line-row').forEach(function(row) {
        row.querySelectorAll('.input-qty, .input-unit-price').forEach(function(input) {
            input.addEventListener('input', function() { recalculateLineRow(row); });
            input.addEventListener('change', function() { recalculateLineRow(row); });
        });
    });

    if (btnAddLine) {
        btnAddLine.addEventListener('click', function () {
            var index = getNextIndex();
            var templateRow = templateSource.querySelector('.line-row');
            var newRow = templateRow.cloneNode(true);
            newRow.setAttribute('data-index', index);

            newRow.innerHTML = newRow.innerHTML.replace(/INDEX/g, index);

            // Remove stale TomSelect markup from cloned row
            newRow.querySelectorAll('.ts-wrapper').forEach(function(w) { w.remove(); });
            newRow.querySelectorAll('select.tomselect-initialized').forEach(function(s) {
                s.classList.remove('tomselect-initialized', 'tomselected', 'ts-hidden-accessible');
                s.style.display = '';
            });

            lineContainer.appendChild(newRow);
            updateEmptyMessage();

            if (window.ERP && window.ERP.initAutocompleteInContainer) {
                window.ERP.initAutocompleteInContainer(newRow);
            }
            if (typeof initNumericInputs === 'function') {
                initNumericInputs(newRow);
            }

            newRow.querySelectorAll('.input-qty, .input-unit-price').forEach(function(input) {
                input.addEventListener('input', function() { recalculateLineRow(newRow); });
                input.addEventListener('change', function() { recalculateLineRow(newRow); });
            });
        });
    }

    // === Line Drawer ===
    var drawerEl = document.getElementById('drawer-line-detail');
    var drawerInstance = null;
    var activeRow = null;

    function openLineDrawer(row) {
        activeRow = row;
        if (!drawerEl) return;

        var taxRateInput = row.querySelector('.input-tax-rate');
        var noteInput = row.querySelector('.input-note');

        document.getElementById('drawer-tax-rate').value = taxRateInput ? taxRateInput.value : '0.00';
        document.getElementById('drawer-note').value = noteInput ? noteInput.value : '';

        recalcDrawerTotals(row);

        if (!drawerInstance) {
            drawerInstance = new bootstrap.Offcanvas(drawerEl);
        }
        drawerInstance.show();

        if (typeof initNumericInputs === 'function') {
            initNumericInputs(drawerEl);
        }
    }

    function recalcDrawerTotals(row) {
        var qtyInput = row.querySelector('.input-qty');
        var priceInput = row.querySelector('.input-unit-price');
        var drawerTaxRate = document.getElementById('drawer-tax-rate');

        var qty = parseFloat((qtyInput ? qtyInput.value : '0').replace(/,/g, '')) || 0;
        var price = parseFloat((priceInput ? priceInput.value : '0').replace(/,/g, '')) || 0;
        var taxRate = parseFloat((drawerTaxRate ? drawerTaxRate.value : '0').replace(/,/g, '')) || 0;

        var subtotal = qty * price;
        var tax = subtotal * taxRate;
        var total = subtotal + tax;

        document.getElementById('drawer-subtotal').textContent = subtotal.toFixed(2);
        document.getElementById('drawer-tax').textContent = tax.toFixed(2);
        document.getElementById('drawer-total').textContent = total.toFixed(2);
    }

    if (drawerEl) {
        var drawerTaxInput = document.getElementById('drawer-tax-rate');
        if (drawerTaxInput) {
            drawerTaxInput.addEventListener('input', function() {
                if (activeRow) recalcDrawerTotals(activeRow);
            });
            drawerTaxInput.addEventListener('change', function() {
                if (activeRow) recalcDrawerTotals(activeRow);
            });
        }
    }

    var btnSaveDrawer = document.getElementById('btn-save-drawer');
    if (btnSaveDrawer) {
        btnSaveDrawer.addEventListener('click', function() {
            if (!activeRow) return;

            var taxRateInput = activeRow.querySelector('.input-tax-rate');
            var noteInput = activeRow.querySelector('.input-note');

            if (taxRateInput) taxRateInput.value = document.getElementById('drawer-tax-rate').value;
            if (noteInput) noteInput.value = document.getElementById('drawer-note').value;

            recalculateLineRow(activeRow);

            if (drawerInstance) drawerInstance.hide();
            activeRow = null;
        });
    }

    // Delegate edit + remove clicks
    lineContainer.addEventListener('click', function (e) {
        var editBtn = e.target.closest('.btn-edit-line');
        if (editBtn) {
            var row = editBtn.closest('.line-row');
            if (row) openLineDrawer(row);
            return;
        }
        var btn = e.target.closest('.btn-remove-line');
        if (btn) {
            var row = btn.closest('.line-row');
            if (row) {
                row.remove();
                updateEmptyMessage();
            }
        }
    });

    // === Submit for Approval handler ===
    var btnSubmitPo = document.getElementById('btn-submit-po');
    if (btnSubmitPo) {
        btnSubmitPo.addEventListener('click', function () {
            var modalEl = document.getElementById('modal-submit-approval');
            if (!modalEl) return;

            modalEl.addEventListener('shown.bs.modal', function handler() {
                var selectEl = document.getElementById('submit-approver');
                if (selectEl && !selectEl.tomselect && typeof initLookup === 'function') {
                    initLookup(selectEl, selectEl.getAttribute('data-lookup-path'));
                }
                modalEl.removeEventListener('shown.bs.modal', handler);
            });

            var selectEl = document.getElementById('submit-approver');
            if (selectEl && selectEl.tomselect) selectEl.tomselect.clear();
            var errEl = document.getElementById('submit-approver-error');
            if (errEl) errEl.classList.add('d-none');

            var btn = document.getElementById('btn-confirm-submit-approval');
            var newBtn = btn.cloneNode(true);
            btn.parentNode.replaceChild(newBtn, btn);
            newBtn.addEventListener('click', function () {
                var approverSelect = document.getElementById('submit-approver');
                var approverId = approverSelect.tomselect ? approverSelect.tomselect.getValue() : approverSelect.value;
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
                    window.location.href = '/purchasing/purchase-orders';
                })
                .catch(function (err) {
                    if (window.ErpModal) ErpModal.showError(err.message);
                    else alert(err.message);
                });
            });

            new (window.bootstrap || window.tabler).Modal(modalEl).show();
        });
    }

    updateEmptyMessage();
});
