document.addEventListener('DOMContentLoaded', function () {
    const config = window.PurchaseRequisitionPageConfig || {};

    const lineContainer = document.getElementById('line-container');
    const emptyMsg = document.getElementById('empty-msg');
    const headerRequesterSelect = document.querySelector('select[name="requesterId"]');
    const headerFacilitySelect = document.querySelector('select[name="facilityId"]');
    const headerSupplierSelect = document.querySelector('select[name="suggestedSupplierId"]');
    const headerCurrencySelect = document.querySelector('select[name="currencyId"]');

    // ── Grand Total recap ──────────────────────────────────────────────────────
    function parseNumeric(el) {
        if (!el) return 0;
        const raw = el.value.replace(/[^0-9.,-]/g, '').replace(/,/g, '');
        return parseFloat(raw) || 0;
    }

    function updateLineTotal(row) {
        const qty = parseNumeric(row.querySelector('.input-qty'));
        const price = parseNumeric(row.querySelector('.input-price'));
        const total = qty * price;
        const totalEl = row.querySelector('.line-total');
        if (totalEl) {
            totalEl.value = total.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
        }
        return total;
    }

    function updateGrandTotal() {
        let grand = 0;
        lineContainer.querySelectorAll('.line-row').forEach(function (row) {
            grand += updateLineTotal(row);
        });
        const grandEl = document.getElementById('recap-grand-total');
        if (grandEl) {
            grandEl.textContent = grand.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
        }
    }

    function waitForTomSelect(el, cb, attempts) {
        if (!el) return;
        if (el.tomselect) { cb(el.tomselect); return; }
        if ((attempts || 0) < 20) setTimeout(function () { waitForTomSelect(el, cb, (attempts || 0) + 1); }, 100);
    }

    function getSelectValue(selectEl) {
        if (!selectEl) return null;
        return selectEl.tomselect ? selectEl.tomselect.getValue() : selectEl.value;
    }

    function withProductId(payload, productId) {
        return Object.assign({ productId: productId }, payload || {});
    }

    function setNumericInputValue(input, value) {
        if (!input) return;
        if (typeof ErpNumeric !== 'undefined') {
            ErpNumeric.set(input, value);
        } else {
            input.value = value != null ? String(value) : '';
        }
        input.dispatchEvent(new Event('input', { bubbles: true }));
    }

    function clearLineItems(message) {
        if (!lineContainer || lineContainer.querySelectorAll('.line-row').length === 0) return;
        lineContainer.innerHTML = '';
        updateEmptyMessage();
        updateGrandTotal();
        if (message && window.ErpModal) {
            ErpModal.showWarning(message);
        }
    }

    function getRowRequiredDate(row) {
        const requiredDateInput = row.querySelector('input[name$=".requiredDate"]');
        return requiredDateInput ? requiredDateInput.value : '';
    }

    function resolveProductPayload(row, productValue) {
        const productSelect = row.querySelector('.select-product');
        if (!productSelect || !productValue) return Promise.resolve(null);

        const productItem = productSelect.tomselect ? productSelect.tomselect.options[productValue] : null;
        const payload = productItem && productItem.payload;
        if (payload && payload.uomId) {
            return Promise.resolve(withProductId(payload, productValue));
        }

        return fetch('/api/lookup/inventory/products/' + encodeURIComponent(productValue))
            .then(function (r) { return r.json(); })
            .then(function (data) {
                const p = data && data.payload;
                if (p && p.uomId) {
                    return withProductId(p, productValue);
                }
                return null;
            })
            .catch(function (err) {
                console.warn('SPL product lookup failed:', err);
                return null;
            });
    }

    function refreshSplPrice(row) {
        const productSelect = row.querySelector('.select-product');
        const productValue = productSelect ? getSelectValue(productSelect) : null;
        if (!productValue) return;

        resolveProductPayload(row, productValue).then(function (payload) {
            if (!payload) return;
            autoFillPriceFromSpl(row, payload, headerSupplierSelect, headerCurrencySelect, row);
        });
    }

    // Delegate input events for qty/price on all current and future rows
    lineContainer.addEventListener('input', function (e) {
        if (e.target.matches('.input-qty') || e.target.matches('.input-price')) {
            updateGrandTotal();
        }
    });

    // ── UoM auto-fill when product is selected ─────────────────────────────────
    function initRowBehaviours(row) {
        const productSelect = row.querySelector('.select-product');
        const supplierSelect = row.querySelector('.select-supplier') || headerSupplierSelect;
        const requiredDateInput = row.querySelector('input[name$=".requiredDate"]');
        if (!productSelect) return;

        waitForTomSelect(productSelect, function (ts) {
            ts.on('change', function (value) {
                const uomSelect = row.querySelector('.select-uom');
                if (!uomSelect || !uomSelect.tomselect) return;

                if (!value) {
                    const uomTs = uomSelect.tomselect;
                    uomTs.clear();
                    // Re-enable UoM visually
                    uomTs.control.style.pointerEvents = 'auto';
                    uomTs.control.style.opacity = '1';
                    return;
                }

                const item = ts.options[value];
                const payload = item && item.payload;
                if (payload && payload.uomId) {
                    const uomTs = uomSelect.tomselect;
                    const uomIdStr = String(payload.uomId);
                    if (!uomTs.options[uomIdStr]) {
                        uomTs.addOption({ id: payload.uomId, name: payload.uomName || payload.uomCode || uomIdStr });
                    }
                    uomTs.setValue(uomIdStr);
                    uomSelect.value = uomIdStr;
                    // Lock UoM visually without disabling the underlying select (so form submission includes it)
                    uomTs.control.style.pointerEvents = 'none';
                    uomTs.control.style.opacity = '0.6';
                    refreshSplPrice(row);
                } else {
                    // No payload yet — fetch from API
                    resolveProductPayload(row, value).then(function (payloadData) {
                        if (!payloadData || !payloadData.uomId) return;
                        const uomTs = uomSelect.tomselect;
                        const uomIdStr = String(payloadData.uomId);
                        if (!uomTs.options[uomIdStr]) {
                            uomTs.addOption({ id: payloadData.uomId, name: payloadData.uomName || payloadData.uomCode || uomIdStr });
                        }
                        uomTs.setValue(uomIdStr);
                        uomSelect.value = uomIdStr;
                        // Lock UoM visually without disabling the underlying select (so form submission includes it)
                        uomTs.control.style.pointerEvents = 'none';
                        uomTs.control.style.opacity = '0.6';
                        refreshSplPrice(row);
                    });
                }
            });
        });

        // Also trigger SPL auto-fill when supplier is selected
        if (supplierSelect) {
            waitForTomSelect(supplierSelect, function (ts) {
                ts.on('change', function () {
                    refreshSplPrice(row);
                });
            });
        }

        if (requiredDateInput) {
            requiredDateInput.addEventListener('change', function () {
                refreshSplPrice(row);
            });
            requiredDateInput.addEventListener('input', function () {
                refreshSplPrice(row);
            });
        }
    }

    function autoFillPriceFromSpl(row, productPayload, supplierSelect, currencySelect, rowContext) {
        const supplierId = getSelectValue(supplierSelect);
        const currencyId = getSelectValue(currencySelect);
        const asOfDate = rowContext ? getRowRequiredDate(rowContext) : '';
        
        if (!supplierId || !productPayload) return;

        const productId = productPayload.id || productPayload.productId;
        const uomId = productPayload.uomId;

        if (!productId || !uomId || !currencyId) return;

        // Call SPL endpoint to get price
        fetch('/purchasing/purchase-requisitions/api/spl-price?' +
            'supplierId=' + encodeURIComponent(supplierId) +
            '&productId=' + encodeURIComponent(productId) +
            '&uomId=' + encodeURIComponent(uomId) +
            '&currencyId=' + encodeURIComponent(currencyId) +
            (asOfDate ? '&requiredDate=' + encodeURIComponent(asOfDate) : ''))
            .then(function (res) {
                if (res.status === 204) return null; // No SPL found
                if (!res.ok) throw new Error('Failed to fetch SPL price');
                return res.json();
            })
            .then(function (spl) {
                const priceInput = row.querySelector('.input-price');
                const quantityInput = row.querySelector('.input-qty');
                if (spl && spl.unitPrice) {
                    setNumericInputValue(priceInput, spl.unitPrice);
                    if (quantityInput && spl.minQuantity != null) {
                        setNumericInputValue(quantityInput, spl.minQuantity);
                    }
                } else if (priceInput) {
                    setNumericInputValue(priceInput, 0);
                }
            })
            .catch(function (err) {
                console.warn('SPL auto-fill failed:', err);
            });
    }

    // Init behaviours on pre-existing rows
    lineContainer.querySelectorAll('.line-row').forEach(function (row) {
        initRowBehaviours(row);
    });

    // Compute initial totals for pre-existing rows
    updateGrandTotal();

    if (config.isLocked) return;

    // ── Add Line ───────────────────────────────────────────────────────────────
    const templateSource = document.getElementById('row-template-source');
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

    function hasRequiredHeaderSelections() {
        return !!(
            getSelectValue(headerRequesterSelect) &&
            getSelectValue(headerFacilitySelect) &&
            getSelectValue(headerSupplierSelect) &&
            getSelectValue(headerCurrencySelect)
        );
    }

    function bindHeaderReset(selectEl) {
        if (!selectEl) return;
        let previousValue = getSelectValue(selectEl);
        selectEl.addEventListener('change', function () {
            const nextValue = getSelectValue(selectEl);
            if (nextValue === previousValue) return;
            previousValue = nextValue;
            clearLineItems(config.headerChangeWarning || 'Header data changed. Existing line items were cleared.');
        });
    }

    bindHeaderReset(headerRequesterSelect);
    bindHeaderReset(headerFacilitySelect);
    bindHeaderReset(headerSupplierSelect);
    bindHeaderReset(headerCurrencySelect);

    if (btnAddLine) {
        btnAddLine.addEventListener('click', function () {
            if (!hasRequiredHeaderSelections()) {
                if (window.ErpModal) {
                    ErpModal.showWarning(config.lineRequirementWarning || 'Please select Requester, Facility, Supplier, and Currency before adding line items.');
                }
                return;
            }
            const index = getNextIndex();
            const templateRow = templateSource.querySelector('.line-row');
            const newRow = templateRow.cloneNode(true);
            newRow.setAttribute('data-index', index);

            newRow.innerHTML = newRow.innerHTML.replace(/INDEX/g, index);

            // Clean up stale Flatpickr markup copied from template row so the
            // date input can be freshly initialized after the row is appended.
            newRow.querySelectorAll('input.flatpickr-alt-input').forEach(function(el) { el.remove(); });
            newRow.querySelectorAll('input.flatpickr-input[type="hidden"]').forEach(function(el) {
                el.setAttribute('type', 'date');
                el.classList.remove('flatpickr-input');
            });
            newRow.querySelectorAll('[data-picker-initialized]').forEach(function(el) {
                el.removeAttribute('data-picker-initialized');
            });

            // Remove stale TomSelect markup from cloned row so it can be re-initialized
            newRow.querySelectorAll('.ts-wrapper').forEach(w => w.remove());
            newRow.querySelectorAll('select.tomselect-initialized').forEach(s => {
                s.classList.remove('tomselect-initialized', 'tomselected', 'ts-hidden-accessible');
                s.style.display = '';
            });

            lineContainer.appendChild(newRow);
            updateEmptyMessage();

            // Re-initialize date pickers on newly added row
            if (window.ErpDateTimePicker) window.ErpDateTimePicker.init(newRow);

            // Re-initialize autocomplete on new row
            if (window.ERP && window.ERP.initAutocompleteInContainer) {
                window.ERP.initAutocompleteInContainer(newRow);
            }

            // Re-initialize numeric inputs on new row
            if (typeof initNumericInputs === 'function') {
                initNumericInputs(newRow);
            }

            initRowBehaviours(newRow);
        });
    }

    lineContainer.addEventListener('click', function (e) {
        const btn = e.target.closest('.btn-remove-line');
        if (btn) {
            const row = btn.closest('.line-row');
            if (row) {
                row.remove();
                updateEmptyMessage();
                updateGrandTotal();
            }
        }
    });

    // ── Submit for Approval handler ────────────────────────────────────────────
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

            new (window.bootstrap || window.tabler).Modal(modalEl).show();
        });
    }

    updateEmptyMessage();
});
