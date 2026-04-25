(function () {
    function attachDateRangeValidation(fromEl, toEl) {
        if (!fromEl || !toEl) return;

        const syncRange = function () {
            const fromValue = fromEl.value || null;

            if (toEl._flatpickr) {
                toEl._flatpickr.set('minDate', fromValue);
            }
            if (fromValue) {
                toEl.setAttribute('min', fromValue);
            } else {
                toEl.removeAttribute('min');
            }

            if (toEl.value && fromValue && toEl.value < fromValue) {
                if (toEl._flatpickr) {
                    toEl._flatpickr.clear();
                } else {
                    toEl.value = '';
                }
            }
        };

        fromEl.addEventListener('change', syncRange);
        fromEl.addEventListener('input', syncRange);
        toEl.addEventListener('change', syncRange);
        syncRange();
    }

    function lockUomInput(uomTs) {
        uomTs.lock();
        uomTs.wrapper.classList.add('bg-body-tertiary');
        uomTs.wrapper.style.pointerEvents = 'none';
    }

    function ensureUomOption(uomTs, uomId, uomName, uomCode) {
        if (!uomId) return;
        const id = String(uomId);
        if (!uomTs.options[id]) {
            uomTs.addOption({
                id: id,
                name: uomName || uomCode || id,
                subText: uomCode || ''
            });
        }
        uomTs.setValue(id, true);
    }

    async function resolveProductPayload(productTs, productId) {
        const selected = productTs.options[productId];
        if (selected && selected.payload && selected.payload.uomId) {
            return selected.payload;
        }

        const response = await fetch('/api/lookup/inventory/products/' + encodeURIComponent(productId), {
            headers: { 'Accept': 'application/json' }
        });
        if (!response.ok) return null;
        const lookup = await response.json();
        return lookup && lookup.payload ? lookup.payload : null;
    }

    function initAutoUom(productEl, uomEl) {
        if (!productEl || !uomEl || !productEl.tomselect || !uomEl.tomselect) return;

        const productTs = productEl.tomselect;
        const uomTs = uomEl.tomselect;

        lockUomInput(uomTs);

        const syncUom = async function () {
            const productId = productTs.getValue();
            if (!productId) {
                uomTs.clear(true);
                return;
            }

            const payload = await resolveProductPayload(productTs, productId);
            if (!payload || !payload.uomId) {
                uomTs.clear(true);
                return;
            }

            ensureUomOption(uomTs, payload.uomId, payload.uomName, payload.uomCode);
        };

        productTs.on('change', function () {
            syncUom();
        });

        syncUom();
    }

    function whenLookupReady(selectEl, callback) {
        if (!selectEl) return;
        if (selectEl.tomselect) {
            callback();
            return;
        }
        const handler = function (evt) {
            if (evt.target !== selectEl) return;
            selectEl.removeEventListener('erp:lookup-initialized', handler);
            callback();
        };
        selectEl.addEventListener('erp:lookup-initialized', handler);
    }

    function initSupplierPriceListForm() {
        const form = document.getElementById('spl-form');
        if (!form) return;

        const productEl = form.querySelector('select[name="productId"]');
        const uomEl = form.querySelector('select[name="uomId"]');
        const fromEl = form.querySelector('input[name="effectiveFrom"]');
        const toEl = form.querySelector('input[name="effectiveTo"]');

        whenLookupReady(productEl, function () {
            whenLookupReady(uomEl, function () {
                initAutoUom(productEl, uomEl);
            });
        });

        attachDateRangeValidation(fromEl, toEl);
    }

    document.addEventListener('DOMContentLoaded', initSupplierPriceListForm);
    document.body.addEventListener('htmx:afterSwap', function (evt) {
        if (!evt.detail || !evt.detail.target) return;
        if (evt.detail.target.querySelector && evt.detail.target.querySelector('#spl-form')) {
            initSupplierPriceListForm();
        }
    });
})();
