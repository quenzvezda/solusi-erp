document.addEventListener('DOMContentLoaded', function () {
    const config = window.PurchaseRequisitionPageConfig || {};

    const lineContainer = document.getElementById('line-container');
    const emptyMsg = document.getElementById('empty-msg');

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

    // Delegate input events for qty/price on all current and future rows
    lineContainer.addEventListener('input', function (e) {
        if (e.target.matches('.input-qty') || e.target.matches('.input-price')) {
            updateGrandTotal();
        }
    });

    // ── UoM auto-fill when product is selected ─────────────────────────────────
    function initRowBehaviours(row) {
        const productSelect = row.querySelector('.select-product');
        const supplierSelect = row.querySelector('.select-supplier');
        const currencyIdHiddenInput = document.querySelector('input[name="currencyId"]');
        if (!productSelect) return;

        function waitForTomSelect(el, cb, attempts) {
            if (el.tomselect) { cb(el.tomselect); return; }
            if ((attempts || 0) < 20) setTimeout(function () { waitForTomSelect(el, cb, (attempts || 0) + 1); }, 100);
        }

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
                    
                    // Try to auto-fill price from SPL if supplier is also selected
                    autoFillPriceFromSpl(row, payload, supplierSelect, currencyIdHiddenInput);
                } else {
                    // No payload yet — fetch from API
                    fetch('/api/lookup/inventory/products/' + encodeURIComponent(value))
                        .then(function (r) { return r.json(); })
                        .then(function (data) {
                            const p = data && data.payload;
                            if (p && p.uomId) {
                                const uomTs = uomSelect.tomselect;
                                const uomIdStr = String(p.uomId);
                                if (!uomTs.options[uomIdStr]) {
                                    uomTs.addOption({ id: p.uomId, name: p.uomName || p.uomCode || uomIdStr });
                                }
                                uomTs.setValue(uomIdStr);
                                uomSelect.value = uomIdStr;
                                // Lock UoM visually without disabling the underlying select (so form submission includes it)
                                uomTs.control.style.pointerEvents = 'none';
                                uomTs.control.style.opacity = '0.6';
                                
                                // Try to auto-fill price from SPL if supplier is also selected
                                autoFillPriceFromSpl(row, p, supplierSelect, currencyIdHiddenInput);
                            }
                        })
                        .catch(function () {});
                }
            });
        });

        // Also trigger SPL auto-fill when supplier is selected
        if (supplierSelect) {
            waitForTomSelect(supplierSelect, function (ts) {
                ts.on('change', function () {
                    const product = productSelect.tomselect ? productSelect.tomselect.getValue() : productSelect.value;
                    if (product) {
                        const productItem = productSelect.tomselect ? productSelect.tomselect.options[product] : null;
                        const payload = productItem && productItem.payload;
                        if (payload || product) {
                            autoFillPriceFromSpl(row, payload || { productId: product }, supplierSelect, currencyIdHiddenInput);
                        }
                    }
                });
            });
        }
    }

    function autoFillPriceFromSpl(row, productPayload, supplierSelect, currencyIdHiddenInput) {
        const supplierId = supplierSelect && supplierSelect.tomselect ? 
            supplierSelect.tomselect.getValue() : supplierSelect.value;
        const currencyId = currencyIdHiddenInput ? currencyIdHiddenInput.value : null;
        
        if (!supplierId || !productPayload) return;

        const productId = productPayload.id || productPayload.productId;
        const uomId = productPayload.uomId;

        if (!productId || !uomId || !currencyId) return;

        // Call SPL endpoint to get price
        fetch('/purchasing/purchase-requisitions/api/spl-price?' +
            'supplierId=' + encodeURIComponent(supplierId) +
            '&productId=' + encodeURIComponent(productId) +
            '&uomId=' + encodeURIComponent(uomId) +
            '&currencyId=' + encodeURIComponent(currencyId))
            .then(function (res) {
                if (res.status === 204) return null; // No SPL found
                if (!res.ok) throw new Error('Failed to fetch SPL price');
                return res.json();
            })
            .then(function (spl) {
                if (spl && spl.unitPrice) {
                    const priceInput = row.querySelector('.input-price');
                    if (priceInput) {
                        // Format price for display
                        const formattedPrice = parseFloat(spl.unitPrice).toLocaleString('en-US', { 
                            minimumFractionDigits: 2, 
                            maximumFractionDigits: 4 
                        });
                        priceInput.value = formattedPrice;
                        // Trigger input event to update totals
                        priceInput.dispatchEvent(new Event('input', { bubbles: true }));
                    }
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
