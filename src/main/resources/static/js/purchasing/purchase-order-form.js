document.addEventListener('DOMContentLoaded', function () {
    var config = window.PurchaseOrderPageConfig || {};
    var isEditable = !config.isLocked;

    var lineContainer = document.getElementById('line-container');
    var templateSource = document.getElementById('row-template-source');
    var emptyMsg = document.getElementById('empty-msg');
    var btnAddLine = document.getElementById('btn-add-line');
    var poTypeRadios = document.querySelectorAll('input[name="poType"]');
    var prRefGroup = document.getElementById('pr-reference-group');
    var prIdInput = document.getElementById('input-pr-id');
    var prDisplayInput = document.getElementById('input-pr-display');
    var btnSelectPr = document.getElementById('btn-select-pr');
    var btnClearPr = document.getElementById('btn-clear-pr');
    var supplierSelect = document.getElementById('header-supplier');
    var facilitySelect = document.getElementById('header-facility');
    var currencySelect = document.getElementById('header-currency');

    var drawerEl = document.getElementById('drawer-line-detail');
    var drawerInstance = null;
    var activeRow = null;
    var previousPoType = getSelectedPoType();

    function getSelectedPoType() {
        var selected = document.querySelector('input[name="poType"]:checked');
        return selected ? selected.value : (config.poType || 'DIRECT');
    }

    function setSelectedPoType(value) {
        var target = document.querySelector('input[name="poType"][value="' + value + '"]');
        if (target) target.checked = true;
    }

    function warn(message) {
        if (!message) return;
        if (window.ErpModal) ErpModal.showWarning(message);
        else alert(message);
    }

    function confirmAction(message, callback) {
        if (window.ErpModal) {
            ErpModal.confirm(message, callback);
            return;
        }
        if (window.confirm(message) && typeof callback === 'function') {
            callback();
        }
    }

    function waitForLookupReady(selectEl, callback) {
        if (!selectEl) return;
        if (selectEl.tomselect) {
            callback(selectEl.tomselect);
            return;
        }
        var handler = function (evt) {
            if (evt.target !== selectEl) return;
            selectEl.removeEventListener('erp:lookup-initialized', handler);
            callback(selectEl.tomselect);
        };
        selectEl.addEventListener('erp:lookup-initialized', handler);
    }

    function lockLookup(selectEl) {
        waitForLookupReady(selectEl, function (ts) {
            if (!ts) return;
            ts.lock();
            ts.wrapper.classList.add('bg-body-tertiary');
            ts.wrapper.style.pointerEvents = 'none';
            ts.wrapper.style.opacity = '0.7';
        });
    }

    function unlockLookup(selectEl) {
        waitForLookupReady(selectEl, function (ts) {
            if (!ts) return;
            ts.unlock();
            ts.wrapper.classList.remove('bg-body-tertiary');
            ts.wrapper.style.pointerEvents = '';
            ts.wrapper.style.opacity = '';
        });
    }

    function setLookupValue(selectEl, id, text, subText, lockAfterSet) {
        if (!selectEl) return;
        var apply = function (ts) {
            if (!ts) return;
            if (id === null || id === undefined || id === '') {
                ts.clear(true);
                selectEl.value = '';
                if (lockAfterSet === false) unlockLookup(selectEl);
                return;
            }

            var stringId = String(id);
            if (!ts.options[stringId]) {
                ts.addOption({
                    id: stringId,
                    name: text || stringId,
                    subText: subText || ''
                });
            }
            ts.setValue(stringId, true);
            selectEl.value = stringId;
            if (lockAfterSet) lockLookup(selectEl);
            else unlockLookup(selectEl);
        };

        if (selectEl.tomselect) {
            apply(selectEl.tomselect);
        } else {
            waitForLookupReady(selectEl, apply);
        }
    }

    function clearLookupValue(selectEl) {
        setLookupValue(selectEl, null, null, null, false);
    }

    function setNumericInputValue(input, value) {
        if (!input) return;
        input.value = value == null ? '' : String(value);
        var autoNumeric = typeof AutoNumeric !== 'undefined' ? AutoNumeric.getAutoNumericElement(input) : null;
        if (autoNumeric) autoNumeric.set(input.value || 0);
    }

    function getNextIndex() {
        var rows = lineContainer ? lineContainer.querySelectorAll('.line-row') : [];
        var maxIndex = -1;
        rows.forEach(function (row) {
            var idx = parseInt(row.getAttribute('data-index') || '-1', 10);
            if (idx > maxIndex) maxIndex = idx;
        });
        return maxIndex + 1;
    }

    function updateEmptyMessage() {
        if (!emptyMsg || !lineContainer) return;
        emptyMsg.style.display = lineContainer.querySelectorAll('.line-row').length > 0 ? 'none' : '';
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
        if (totalInput) {
            totalInput.value = total.toFixed(2);
            var autoNumeric = typeof AutoNumeric !== 'undefined' ? AutoNumeric.getAutoNumericElement(totalInput) : null;
            if (autoNumeric) autoNumeric.set(totalInput.value);
        }
    }

    function attachRowRecalculation(row) {
        row.querySelectorAll('.input-qty, .input-unit-price').forEach(function (input) {
            input.addEventListener('input', function () { recalculateLineRow(row); });
            input.addEventListener('change', function () { recalculateLineRow(row); });
        });
    }

    function markDerivedRow(row) {
        row.dataset.derived = 'true';
        var productSelect = row.querySelector('.select-product');
        var uomSelect = row.querySelector('.select-uom');
        if (productSelect) lockLookup(productSelect);
        if (uomSelect) lockLookup(uomSelect);
    }

    function applyDerivedRowStateFromInputs(row) {
        var prLineInput = row.querySelector('.input-pr-line-id');
        if (prLineInput && prLineInput.value) {
            markDerivedRow(row);
        }
    }

    function initializeRow(row) {
        if (window.ERP && window.ERP.initAutocompleteInContainer) {
            window.ERP.initAutocompleteInContainer(row);
        }
        if (typeof initNumericInputs === 'function') {
            initNumericInputs(row);
        }
        attachRowRecalculation(row);
        applyDerivedRowStateFromInputs(row);
        recalculateLineRow(row);
    }

    function createRow(index) {
        var templateRow = templateSource ? templateSource.querySelector('.line-row') : null;
        if (!templateRow) return null;

        var newRow = templateRow.cloneNode(true);
        newRow.setAttribute('data-index', index);
        newRow.innerHTML = newRow.innerHTML.replace(/INDEX/g, index);
        newRow.querySelectorAll('.ts-wrapper').forEach(function (wrapper) { wrapper.remove(); });
        newRow.querySelectorAll('select.tomselect-initialized').forEach(function (selectEl) {
            selectEl.classList.remove('tomselect-initialized', 'tomselected', 'ts-hidden-accessible');
            selectEl.style.display = '';
        });
        return newRow;
    }

    function addManualLineRow() {
        if (!lineContainer) return null;
        var row = createRow(getNextIndex());
        if (!row) return null;
        lineContainer.appendChild(row);
        initializeRow(row);
        updateEmptyMessage();
        return row;
    }

    function currentPrLineIds() {
        if (!lineContainer) return [];
        return Array.from(lineContainer.querySelectorAll('.input-pr-line-id'))
            .map(function (input) { return input.value; })
            .filter(function (value) { return value !== ''; });
    }

    function clearAllLines() {
        if (!lineContainer) return;
        lineContainer.querySelectorAll('.line-row').forEach(function (row) { row.remove(); });
        updateEmptyMessage();
    }

    function clearDerivedHeader() {
        if (!config.isNew) return;
        clearLookupValue(supplierSelect);
        clearLookupValue(facilitySelect);
        clearLookupValue(currencySelect);
    }

    function clearStandardSelection(options) {
        var clearOptions = options || { clearHeader: true, clearLines: true };
        if (prIdInput) prIdInput.value = '';
        if (prDisplayInput) prDisplayInput.value = '';
        if (clearOptions.clearHeader) clearDerivedHeader();
        if (clearOptions.clearLines) clearAllLines();
    }

    function hasAnyLines() {
        return !!lineContainer && lineContainer.querySelectorAll('.line-row').length > 0;
    }

    function hasStandardSelection() {
        return !!(prIdInput && prIdInput.value) || currentPrLineIds().length > 0 || hasAnyLines();
    }

    function syncPrFieldState() {
        var poType = getSelectedPoType();
        if (prRefGroup) prRefGroup.style.display = poType === 'STANDARD' ? '' : 'none';
        if (prIdInput) prIdInput.disabled = poType !== 'STANDARD';
        if (btnSelectPr) btnSelectPr.disabled = !config.isNew || poType !== 'STANDARD';
        if (btnClearPr) btnClearPr.disabled = !config.isNew || poType !== 'STANDARD';
    }

    function openPrSelector() {
        if (!config.isNew || !window.ERP || !window.ERP.ModalSelector) return;
        window.ERP.ModalSelector.open({
            modalId: 'modal-po-pr-selector',
            resultsId: 'po-pr-selector-results',
            url: config.prSelectorUrl
        });
    }

    function buildPrLineSelectorUrl() {
        var params = new URLSearchParams();
        params.set('prId', prIdInput.value);
        currentPrLineIds().forEach(function (id) {
            params.append('excludePrLineIds', id);
        });
        return config.prLineSelectorUrl + '?' + params.toString();
    }

    function openPrLineSelector() {
        if (!window.ERP || !window.ERP.ModalSelector) return;
        if (!prIdInput || !prIdInput.value) {
            warn(config.prRequiredBeforeLines);
            return;
        }
        window.ERP.ModalSelector.open({
            modalId: 'modal-po-pr-line-selector',
            resultsId: 'po-pr-line-selector-results',
            url: buildPrLineSelectorUrl()
        });
    }

    function numberValue(value) {
        if (value === null || value === undefined || value === '') return null;
        var parsed = Number(value);
        return Number.isNaN(parsed) ? null : parsed;
    }

    function parsePrPayload(row) {
        return {
            prId: numberValue(row.dataset.prId),
            prCode: row.dataset.prCode || '',
            supplierId: numberValue(row.dataset.supplierId),
            supplierName: row.dataset.supplierName || '',
            supplierSubtext: row.dataset.supplierSubtext || '',
            facilityId: numberValue(row.dataset.facilityId),
            facilityName: row.dataset.facilityName || '',
            facilitySubtext: row.dataset.facilitySubtext || '',
            currencyId: numberValue(row.dataset.currencyId),
            currencyName: row.dataset.currencyName || '',
            currencySubtext: row.dataset.currencySubtext || ''
        };
    }

    function parsePrLinePayload(row) {
        return {
            prLineId: numberValue(row.dataset.prLineId),
            prId: numberValue(row.dataset.prId),
            prCode: row.dataset.prCode || '',
            productId: numberValue(row.dataset.productId),
            productName: row.dataset.productName || '',
            productSubtext: row.dataset.productSubtext || '',
            requestedQuantity: row.dataset.requestedQuantity || '',
            remainingQuantity: row.dataset.remainingQuantity || '',
            uomId: numberValue(row.dataset.uomId),
            uomName: row.dataset.uomName || '',
            uomSubtext: row.dataset.uomSubtext || '',
            estimatedUnitPrice: row.dataset.estimatedUnitPrice || '',
            requiredDate: row.dataset.requiredDate || '',
            note: row.dataset.note || ''
        };
    }

    function applySelectedPr(payload) {
        if (!prIdInput || !prDisplayInput) return;
        prIdInput.disabled = false;
        prIdInput.value = payload.prId != null ? String(payload.prId) : '';
        prDisplayInput.value = payload.prCode || '';
        setLookupValue(supplierSelect, payload.supplierId, payload.supplierName, payload.supplierSubtext, true);
        setLookupValue(facilitySelect, payload.facilityId, payload.facilityName, payload.facilitySubtext, true);
        setLookupValue(currencySelect, payload.currencyId, payload.currencyName, payload.currencySubtext, true);
    }

    function appendPrLineRow(payload) {
        if (!lineContainer) return;
        var row = createRow(getNextIndex());
        if (!row) return;
        lineContainer.appendChild(row);
        initializeRow(row);

        var prLineInput = row.querySelector('.input-pr-line-id');
        if (prLineInput) prLineInput.value = payload.prLineId != null ? String(payload.prLineId) : '';

        var qtyInput = row.querySelector('.input-qty');
        var unitPriceInput = row.querySelector('.input-unit-price');
        var noteInput = row.querySelector('.input-note');

        setNumericInputValue(qtyInput, payload.remainingQuantity);
        setNumericInputValue(unitPriceInput, payload.estimatedUnitPrice);
        if (qtyInput && payload.remainingQuantity) qtyInput.setAttribute('max', payload.remainingQuantity);
        if (noteInput) noteInput.value = payload.note || '';

        var productSelect = row.querySelector('.select-product');
        var uomSelect = row.querySelector('.select-uom');
        setLookupValue(productSelect, payload.productId, payload.productName, payload.productSubtext, true);
        setLookupValue(uomSelect, payload.uomId, payload.uomName, payload.uomSubtext, true);

        row.dataset.prLineId = payload.prLineId != null ? String(payload.prLineId) : '';
        markDerivedRow(row);
        recalculateLineRow(row);
        updateEmptyMessage();
    }

    if (poTypeRadios.length && config.isNew && isEditable) {
        poTypeRadios.forEach(function (radio) {
            radio.addEventListener('change', function () {
                var nextPoType = getSelectedPoType();
                if (nextPoType === previousPoType) {
                    syncPrFieldState();
                    return;
                }

                if (nextPoType === 'DIRECT' && hasStandardSelection()) {
                    setSelectedPoType(previousPoType);
                    confirmAction(config.confirmChangeType, function () {
                        clearStandardSelection({ clearHeader: true, clearLines: true });
                        previousPoType = 'DIRECT';
                        setSelectedPoType('DIRECT');
                        syncPrFieldState();
                    });
                    return;
                }

                if (nextPoType === 'STANDARD' && hasAnyLines()) {
                    setSelectedPoType(previousPoType);
                    confirmAction(config.confirmSwitchToStandard, function () {
                        clearAllLines();
                        previousPoType = 'STANDARD';
                        setSelectedPoType('STANDARD');
                        syncPrFieldState();
                        openPrSelector();
                    });
                    return;
                }

                previousPoType = nextPoType;
                syncPrFieldState();
                if (nextPoType === 'STANDARD' && !prIdInput.value) {
                    openPrSelector();
                }
            });
        });
    }

    if (btnSelectPr && isEditable && config.isNew) {
        btnSelectPr.addEventListener('click', function () {
            openPrSelector();
        });
    }

    if (btnClearPr && isEditable && config.isNew) {
        btnClearPr.addEventListener('click', function () {
            if (!hasStandardSelection()) {
                clearStandardSelection({ clearHeader: true, clearLines: true });
                return;
            }
            confirmAction(config.confirmChangePr, function () {
                clearStandardSelection({ clearHeader: true, clearLines: true });
            });
        });
    }

    if (btnAddLine && isEditable) {
        btnAddLine.addEventListener('click', function () {
            if (getSelectedPoType() === 'STANDARD') {
                openPrLineSelector();
                return;
            }

            addManualLineRow();
        });
    }

    if (lineContainer) {
        lineContainer.querySelectorAll('.line-row').forEach(function (row) {
            initializeRow(row);
        });

        lineContainer.addEventListener('click', function (evt) {
            var removeBtn = evt.target.closest('.btn-remove-line');
            if (removeBtn) {
                var rowToRemove = removeBtn.closest('.line-row');
                if (rowToRemove) {
                    rowToRemove.remove();
                    updateEmptyMessage();
                }
            }
        });
    }

    document.body.addEventListener('click', function (evt) {
        var prPickBtn = evt.target.closest('.js-pr-selector-pick');
        if (prPickBtn) {
            var prRow = prPickBtn.closest('tr');
            if (!prRow) return;
            var prPayload = parsePrPayload(prRow);

            var applyPrSelection = function () {
                clearStandardSelection({ clearHeader: true, clearLines: true });
                applySelectedPr(prPayload);
                if (window.ERP && window.ERP.ModalSelector) {
                    window.ERP.ModalSelector.close('modal-po-pr-selector');
                }
            };

            if (prIdInput && prIdInput.value && prIdInput.value !== String(prPayload.prId)) {
                confirmAction(config.confirmChangePr, applyPrSelection);
            } else {
                applyPrSelection();
            }
            return;
        }

        var applyLineBtn = evt.target.closest('.js-pr-line-selector-apply');
        if (applyLineBtn) {
            var selectedRows = Array.from(document.querySelectorAll('#po-pr-line-selector-results .js-pr-line-selector-item:checked'))
                .map(function (checkbox) { return checkbox.closest('tr'); })
                .filter(function (row) { return !!row; });

            if (selectedRows.length === 0) {
                warn(config.selectAtLeastOnePrLine);
                return;
            }

            selectedRows.forEach(function (row) {
                appendPrLineRow(parsePrLinePayload(row));
            });

            if (window.ERP && window.ERP.ModalSelector) {
                window.ERP.ModalSelector.close('modal-po-pr-line-selector');
            }
        }
    });

    function openLineDrawer(row) {
        activeRow = row;
        if (!drawerEl) return;

        var taxRateInput = row.querySelector('.input-tax-rate');
        var noteInput = row.querySelector('.input-note');
        document.getElementById('drawer-tax-rate').value = taxRateInput ? taxRateInput.value : '0.00';
        document.getElementById('drawer-note').value = noteInput ? noteInput.value : '';
        recalcDrawerTotals(row);

        if (!drawerInstance && typeof bootstrap !== 'undefined' && bootstrap.Offcanvas) {
            drawerInstance = new bootstrap.Offcanvas(drawerEl);
        }
        if (drawerInstance) drawerInstance.show();

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
            drawerTaxInput.addEventListener('input', function () {
                if (activeRow) recalcDrawerTotals(activeRow);
            });
            drawerTaxInput.addEventListener('change', function () {
                if (activeRow) recalcDrawerTotals(activeRow);
            });
        }
    }

    var btnSaveDrawer = document.getElementById('btn-save-drawer');
    if (btnSaveDrawer) {
        btnSaveDrawer.addEventListener('click', function () {
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

            var confirmBtn = document.getElementById('btn-confirm-submit-approval');
            var newBtn = confirmBtn.cloneNode(true);
            confirmBtn.parentNode.replaceChild(newBtn, confirmBtn);
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

            var modalProvider = window.bootstrap || window.tabler;
            if (modalProvider && modalProvider.Modal) {
                if (window.bootstrap && bootstrap.Modal && bootstrap.Modal.getOrCreateInstance) {
                    bootstrap.Modal.getOrCreateInstance(modalEl).show();
                } else {
                    new modalProvider.Modal(modalEl).show();
                }
            }
        });
    }

    if (!config.isNew && getSelectedPoType() === 'STANDARD' && prIdInput && prIdInput.value) {
        lockLookup(supplierSelect);
        lockLookup(facilitySelect);
        lockLookup(currencySelect);
    }

    syncPrFieldState();
    updateEmptyMessage();
});
