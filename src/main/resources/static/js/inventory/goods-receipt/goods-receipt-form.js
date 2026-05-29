document.addEventListener('DOMContentLoaded', function () {
    var config = window.GoodsReceiptPageConfig || {};
    var lineContainer = document.getElementById('line-container');
    var templateSource = document.getElementById('row-template-source');
    var emptyMsg = document.getElementById('empty-msg');
    var btnAddLine = document.getElementById('btn-add-line');
    var form = document.getElementById('gr-form');
    var referenceTypeInput = form ? form.querySelector('input[name="referenceType"]') : null;
    var referenceIdInput = form ? form.querySelector('input[name="referenceId"]') : null;
    var nextIndex = (lineContainer && lineContainer.querySelectorAll('tr.line-row').length) || 0;

    function warn(message) {
        if (!message) return;
        if (window.ErpModal) ErpModal.showWarning(message);
        else alert(message);
    }

    function initializeDecimalFields(container) {
        var decimalInputs = container.querySelectorAll('.erp-number-decimal');
        decimalInputs.forEach(function (input) {
            if (input.autonumeric || input.getAttribute('data-autonumeric') === 'true') return;
            try {
                new AutoNumeric(input, {
                    currencySymbol: '',
                    decimalCharacter: '.',
                    digitGroupSeparator: ',',
                    decimalPlaces: 2,
                    modifyValueOnWheel: false
                });
                input.setAttribute('data-autonumeric', 'true');
            } catch (e) {
                console.warn('AutoNumeric initialization failed', e);
            }
        });
    }

    function getNumericValue(input) {
        if (!input) return 0;
        var instance = typeof AutoNumeric !== 'undefined' ? AutoNumeric.getAutoNumericElement(input) : null;
        if (instance) return parseFloat(instance.getNumber()) || 0;
        return parseFloat((input.value || '0').replace(/,/g, '')) || 0;
    }

    function setNumericValue(input, value) {
        if (!input) return;
        var instance = typeof AutoNumeric !== 'undefined' ? AutoNumeric.getAutoNumericElement(input) : null;
        if (instance) instance.set(value);
        else input.value = value;
    }

    function formatQuantity(value) {
        if (!isFinite(value)) return '0.0000';
        return Number(value).toLocaleString(undefined, {
            minimumFractionDigits: 4,
            maximumFractionDigits: 4
        });
    }

    function isWholeNumber(value) {
        return Math.abs(value - Math.round(value)) < 0.0000001;
    }

    function parseSerials(value) {
        if (!value) return [];
        return value.split(',')
            .map(function (item) { return item.trim(); })
            .filter(Boolean);
    }

    function waitForLookupReady(selectEl, callback) {
        if (!selectEl || typeof callback !== 'function') return;
        var attempts = 0;
        var maxAttempts = 50;
        (function tick() {
            if (selectEl.tomselect) {
                callback(selectEl.tomselect);
                return;
            }
            attempts += 1;
            if (attempts >= maxAttempts) return;
            setTimeout(tick, 50);
        })();
    }

    function setLookupValue(selectEl, value, text, subtext) {
        if (!selectEl || value === null || value === undefined || value === '') return;
        waitForLookupReady(selectEl, function (ts) {
            var key = String(value);
            if (!ts.options[key]) {
                ts.addOption({
                    id: key,
                    name: text || key,
                    subText: subtext || '',
                    payload: {
                        uomId: selectEl.closest('tr')?.querySelector('.input-uom-id')?.value,
                        uomName: selectEl.closest('tr')?.querySelector('.input-uom-alias')?.value,
                        isSerialized: selectEl.closest('tr')?.querySelector('.input-serialized')?.value === 'true'
                    }
                });
            }
            ts.setValue(key, true);
        });
    }

    function lockLookup(selectEl) {
        if (!selectEl) return;
        waitForLookupReady(selectEl, function (ts) {
            if (!ts) return;
            ts.lock();
            ts.wrapper.classList.add('bg-body-tertiary');
            ts.wrapper.style.pointerEvents = 'none';
            ts.wrapper.style.opacity = '0.7';
        });
    }

    function hasReferenceLine(row) {
        var referenceLineInput = row ? row.querySelector('input[name$=".referenceLineId"]') : null;
        return !!(referenceLineInput && referenceLineInput.value);
    }

    function hasPurchaseOrderReference() {
        return referenceTypeInput
            && referenceIdInput
            && referenceTypeInput.value === 'PURCHASE_ORDER'
            && referenceIdInput.value;
    }

    function currentReferenceLineIds() {
        if (!lineContainer) return [];
        return Array.from(lineContainer.querySelectorAll('input[name$=".referenceLineId"]'))
            .map(function (input) { return input.value; })
            .filter(function (value) { return !!value; });
    }

    function buildPoLineSelectorUrl() {
        var params = new URLSearchParams();
        params.set('referenceType', referenceTypeInput.value);
        params.set('referenceId', referenceIdInput.value);
        currentReferenceLineIds().forEach(function (id) {
            params.append('excludeReferenceLineIds', id);
        });
        return config.poLineSelectorUrl + '?' + params.toString();
    }

    function ensurePoLineSelectorModal() {
        var modalEl = document.getElementById('modal-gr-po-line-selector');
        if (modalEl) return modalEl;

        var wrapper = document.createElement('div');
        wrapper.innerHTML = [
            '<div class="modal modal-blur fade" id="modal-gr-po-line-selector" tabindex="-1" aria-hidden="true">',
            '  <div class="modal-dialog modal-xl">',
            '    <div class="modal-content">',
            '      <div class="modal-header">',
            '        <h5 class="modal-title">' + (config.poLineSelectorTitle || 'Choose Purchase Order Lines') + '</h5>',
            '        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>',
            '      </div>',
            '      <div class="modal-body p-0">',
            '        <div class="card border-0 rounded-0"><div class="card-body p-0"><div id="gr-po-line-selector-results"></div></div></div>',
            '      </div>',
            '    </div>',
            '  </div>',
            '</div>'
        ].join('');
        modalEl = wrapper.firstElementChild;
        if (!modalEl) return null;
        document.body.appendChild(modalEl);
        return modalEl;
    }

    function openPoLineSelector() {
        if (!config.poLineSelectorUrl) {
            warn(config.poLineSelectorUnavailable || 'PO line selector is unavailable.');
            return;
        }

        var modalEl = ensurePoLineSelectorModal();
        if (!modalEl) {
            warn(config.poLineSelectorUnavailable || 'PO line selector is unavailable.');
            return;
        }

        var url = buildPoLineSelectorUrl();
        if (window.ERP && window.ERP.ModalSelector) {
            window.ERP.ModalSelector.open({
                modalId: 'modal-gr-po-line-selector',
                resultsId: 'gr-po-line-selector-results',
                url: url
            });
            return;
        }

        if (window.htmx) {
            window.htmx.ajax('GET', url, {
                target: '#gr-po-line-selector-results',
                swap: 'outerHTML'
            });
        } else {
            fetch(url, { headers: { 'HX-Request': 'true' } })
                .then(function (response) { return response.text(); })
                .then(function (html) {
                    var target = document.getElementById('gr-po-line-selector-results');
                    if (!target) return;
                    var wrapper = document.createElement('div');
                    wrapper.innerHTML = html.trim();
                    var next = wrapper.firstElementChild;
                    if (next) target.replaceWith(next);
                });
        }

        if (window.bootstrap && window.bootstrap.Modal && window.bootstrap.Modal.getOrCreateInstance) {
            window.bootstrap.Modal.getOrCreateInstance(modalEl).show();
        }
    }

    function parsePoLinePayload(row) {
        return {
            referenceLineId: row.dataset.referenceLineId || '',
            productId: row.dataset.productId || '',
            productName: row.dataset.productName || '',
            productSubtext: row.dataset.productSubtext || '',
            uomId: row.dataset.uomId || '',
            uomName: row.dataset.uomName || '',
            uomSubtext: row.dataset.uomSubtext || '',
            remainingQuantity: row.dataset.remainingQuantity || '',
            serialized: row.dataset.serialized === 'true'
        };
    }

    function appendSelectedPoLine(payload) {
        var newRow = createNewRow();
        if (!newRow) return false;

        var referenceLineInput = newRow.querySelector('input[name$=".referenceLineId"]');
        var productSelect = newRow.querySelector('.select-product');
        var uomIdInput = newRow.querySelector('.input-uom-id');
        var uomAliasInput = newRow.querySelector('.input-uom-alias');
        var uomDisplayInput = newRow.querySelector('.input-uom-display');
        var serializedInput = newRow.querySelector('.input-serialized');
        var qtyInput = newRow.querySelector('.input-qty');
        var remainingQtyInput = newRow.querySelector('.input-remaining-qty');

        if (referenceLineInput) referenceLineInput.value = payload.referenceLineId;
        if (uomIdInput) uomIdInput.value = payload.uomId;
        if (uomAliasInput) uomAliasInput.value = payload.uomName;
        if (uomDisplayInput) uomDisplayInput.value = payload.uomName;
        if (serializedInput) serializedInput.value = payload.serialized ? 'true' : 'false';
        if (qtyInput) setNumericValue(qtyInput, 0);
        if (remainingQtyInput) setNumericValue(remainingQtyInput, payload.remainingQuantity || 0);

        setLookupValue(productSelect, payload.productId, payload.productName, payload.productSubtext);
        lockLookup(productSelect);
        updateDetailSummary(newRow);
        return true;
    }

    function updateEmptyMessage() {
        if (!emptyMsg || !lineContainer) return;
        emptyMsg.style.display = lineContainer.querySelectorAll('tr.line-row').length > 0 ? 'none' : '';
    }

    function reindexRows() {
        if (!lineContainer) return;
        lineContainer.querySelectorAll('tr.line-row').forEach(function (row, index) {
            row.setAttribute('data-index', index);
            row.querySelectorAll('[name]').forEach(function (input) {
                input.name = input.name.replace(/\[\d+\]/, '[' + index + ']');
            });
        });
        nextIndex = lineContainer.querySelectorAll('tr.line-row').length;
    }

    function updateDetailSummary(row) {
        if (!row) return;
        var isSerialized = row.querySelector('.input-serialized')?.value === 'true';
        var badge = row.querySelector('.detail-mode-label');
        var summary = row.querySelector('.detail-summary');
        var serialCount = parseSerials(row.querySelector('.input-sn-single')?.value).length;

        if (badge) {
            badge.textContent = isSerialized ? 'Serialized' : 'Standard';
            badge.classList.toggle('bg-azure-lt', isSerialized);
            badge.classList.toggle('text-azure', isSerialized);
            badge.classList.toggle('bg-secondary-lt', !isSerialized);
            badge.classList.toggle('text-secondary', !isSerialized);
        }

        if (summary) {
            if (isSerialized) {
                summary.textContent = serialCount > 0
                    ? serialCount + ' serial(s) drafted'
                    : 'Serial numbers pending';
            } else {
                summary.textContent = 'Set quantity and UoM';
            }
        }
    }

    function syncSerialRows(drawer, count, row) {
        var container = drawer.querySelector('.serial-input-container');
        var existingSerials = parseSerials(row.querySelector('.input-sn-single')?.value);
        var rows = Array.from(container.querySelectorAll('tr'));

        if (rows.length > count) {
            for (var i = rows.length - 1; i >= count; i--) {
                rows[i].remove();
            }
            rows = Array.from(container.querySelectorAll('tr'));
        }

        for (var index = rows.length; index < count; index++) {
            var tr = document.createElement('tr');

            var numberCell = document.createElement('td');
            numberCell.className = 'text-center small text-secondary';
            numberCell.textContent = String(index + 1);

            var serialCell = document.createElement('td');
            var serialInput = document.createElement('input');
            serialInput.type = 'text';
            serialInput.className = 'form-control form-control-sm input-sn-item';
            serialInput.value = existingSerials[index] || '';
            serialCell.appendChild(serialInput);

            var qtyCell = document.createElement('td');
            qtyCell.className = 'text-end text-secondary';
            qtyCell.textContent = '1.00';

            tr.appendChild(numberCell);
            tr.appendChild(serialCell);
            tr.appendChild(qtyCell);
            container.appendChild(tr);
        }
    }

    function populateUomOptions(drawer, row, selectedUomId, currentQty, isSerialized) {
        var productSelect = row.querySelector('.select-product');
        var productId = productSelect && productSelect.tomselect ? productSelect.tomselect.getValue() : null;
        var productOption = productId && productSelect.tomselect ? productSelect.tomselect.options[productId] : null;
        var payload = productOption && productOption.payload ? productOption.payload : {};
        var fallbackBaseId = payload.uomId || row.querySelector('.input-uom-id')?.value || '';
        var fallbackBaseName = payload.uomName || row.querySelector('.input-uom-alias')?.value || '-';
        var drawerUom = drawer.querySelector('.select-uom-target');
        var qtyTarget = drawer.querySelector('.input-qty-target');
        var qtyBase = drawer.querySelector('.input-qty-base');
        var totalQty = drawer.querySelector('.txt-total-qty');

        function updateCalculations() {
            var selectedOption = drawerUom.options[drawerUom.selectedIndex];
            if (!selectedOption) return;

            var factor = parseFloat(selectedOption.getAttribute('data-factor')) || 1;
            var targetQty = getNumericValue(qtyTarget);
            var baseQty = targetQty * factor;

            drawer.querySelectorAll('.txt-target-uom').forEach(function (el) {
                el.textContent = selectedOption.text;
            });
            drawer.querySelectorAll('.txt-factor').forEach(function (el) {
                el.textContent = factor.toFixed(2);
            });
            drawer.querySelectorAll('.txt-base-uom').forEach(function (el) {
                el.textContent = fallbackBaseName;
            });

            setNumericValue(qtyBase, baseQty);
            var qtyBaseDisplay = drawer.querySelector('.input-qty-base-display');
            if (qtyBaseDisplay) qtyBaseDisplay.textContent = formatQuantity(baseQty);
            if (totalQty) totalQty.textContent = formatQuantity(baseQty);
            if (isSerialized) syncSerialRows(drawer, Math.max(0, Math.floor(baseQty)), row);
        }

        function applyOptions(data) {
            var options = Array.isArray(data) ? data : [];
            var baseOptionFromApi = options.find(function (item) { return item.isBase; });
            var baseUomId = baseOptionFromApi ? String(baseOptionFromApi.uomId) : String(fallbackBaseId || '');
            var baseUomName = baseOptionFromApi ? baseOptionFromApi.uomName : fallbackBaseName;

            fallbackBaseName = baseUomName;
            drawerUom.innerHTML = '';

            if (baseUomId) {
                var baseOption = document.createElement('option');
                baseOption.value = baseUomId;
                baseOption.text = baseUomName;
                baseOption.setAttribute('data-factor', '1.00');
                drawerUom.appendChild(baseOption);
            }

            options.forEach(function (item) {
                if (item.isBase) return;
                var option = document.createElement('option');
                option.value = item.uomId;
                option.text = item.uomName;
                option.setAttribute('data-factor', item.conversionFactor || 1);
                drawerUom.appendChild(option);
            });

            drawer.querySelectorAll('.txt-base-uom').forEach(function (el) {
                el.textContent = baseUomName;
            });

            if (selectedUomId && Array.from(drawerUom.options).some(function (option) { return option.value === String(selectedUomId); })) {
                drawerUom.value = String(selectedUomId);
            } else if (drawerUom.options.length > 0) {
                drawerUom.selectedIndex = 0;
            }

            initializeDecimalFields(drawer);
            setNumericValue(qtyTarget, currentQty);
            updateCalculations();
            drawerUom.onchange = updateCalculations;
            qtyTarget.addEventListener('autoNumeric:rawValueModified', updateCalculations);
            qtyTarget.oninput = updateCalculations;
        }

        if (!productId) {
            applyOptions([]);
            return;
        }

        fetch('/api/lookup/inventory/uom-conversions?productId=' + encodeURIComponent(productId), {
            headers: { Accept: 'application/json' }
        })
            .then(function (response) {
                if (!response.ok) throw new Error('HTTP ' + response.status);
                return response.json();
            })
            .then(applyOptions)
            .catch(function () {
                applyOptions([]);
            });
    }

    function setupDrawer(drawer, row, isSerialized) {
        var productSelect = row.querySelector('.select-product');
        if (!productSelect || !productSelect.tomselect || !productSelect.tomselect.getValue()) {
            warn(config.selectProductFirst || 'Please select a product first.');
            return false;
        }

        var currentQty = getNumericValue(row.querySelector('.input-qty'));
        var selectedUomId = row.querySelector('.input-uom-id')?.value || '';

        if (isSerialized) {
            var serialContainer = drawer.querySelector('.serial-input-container');
            if (serialContainer) serialContainer.innerHTML = '';
        }

        populateUomOptions(drawer, row, selectedUomId, currentQty, isSerialized);

        var saveButton = drawer.querySelector('.btn-save-drawer');
        saveButton.onclick = function () {
            var selectedOption = drawer.querySelector('.select-uom-target').options[drawer.querySelector('.select-uom-target').selectedIndex];
            if (!selectedOption) return;
            var baseQty = getNumericValue(drawer.querySelector('.input-qty-base'));

            if (isSerialized && !isWholeNumber(baseQty)) {
                warn(config.serialWholeNumberWarning || 'Serialized items require a whole base quantity.');
                return;
            }

            row.querySelector('.input-uom-id').value = drawer.querySelector('.select-uom-target').value;
            row.querySelector('.input-uom-alias').value = selectedOption.text;
            row.querySelector('.input-uom-display').value = selectedOption.text;
            setNumericValue(row.querySelector('.input-qty'), getNumericValue(drawer.querySelector('.input-qty-target')));

            if (isSerialized) {
                var serials = Array.from(drawer.querySelectorAll('.input-sn-item'))
                    .map(function (input) { return input.value.trim(); })
                    .filter(Boolean);
                row.querySelector('.input-sn-single').value = serials.join(',');
            } else {
                row.querySelector('.input-sn-single').value = '';
            }

            updateDetailSummary(row);
            ErpDrawer.close(drawer.id);
        };

        return true;
    }

    function setupLookup(selectEl, path) {
        if (!selectEl || selectEl.tomselect || typeof initLookup !== 'function') return null;
        return initLookup(selectEl, path);
    }

    function initializeLineRow(row) {
        initializeDecimalFields(row);
        if (window.ERP && window.ERP.initAutocompleteInContainer) {
            window.ERP.initAutocompleteInContainer(row);
        }

        var productSelect = row.querySelector('.select-product');
        var containerSelect = row.querySelector('.select-container');
        var removeBtn = row.querySelector('.btn-remove-line');
        var editBtn = row.querySelector('.btn-edit-detail');
        var uomDisplay = row.querySelector('.input-uom-display');
        var serializedInput = row.querySelector('.input-serialized');
        var serialInput = row.querySelector('.input-sn-single');

        setupLookup(productSelect, 'inventory/products');
        setupLookup(containerSelect, 'inventory/containers');

        if (hasReferenceLine(row)) {
            lockLookup(productSelect);
        }

        if (productSelect && productSelect.tomselect) {
            productSelect.tomselect.on('change', function (value) {
                var option = value ? productSelect.tomselect.options[value] : null;
                var payload = option && option.payload ? option.payload : {};
                var isSerialized = payload.isSerialized === true || payload.isSerialized === 'true';

                serializedInput.value = isSerialized ? 'true' : 'false';
                row.querySelector('.input-uom-id').value = payload.uomId || '';
                row.querySelector('.input-uom-alias').value = payload.uomName || '';
                if (uomDisplay) uomDisplay.value = payload.uomName || '';
                if (!isSerialized && serialInput) serialInput.value = '';

                updateDetailSummary(row);
            });
        }

        if (editBtn) {
            editBtn.onclick = function () {
                var isSerialized = serializedInput.value === 'true';
                var drawerId = isSerialized ? 'drawer-serial' : 'drawer-non-serial';
                var drawer = document.getElementById(drawerId);
                if (!drawer) return;
                if (setupDrawer(drawer, row, isSerialized)) {
                    ErpDrawer.open(drawerId);
                }
            };
        }

        if (removeBtn) {
            removeBtn.onclick = function (e) {
                e.preventDefault();
                row.remove();
                reindexRows();
                updateEmptyMessage();
            };
        }

        updateDetailSummary(row);
    }

    function createRowFromTemplate(index) {
        var template = templateSource ? templateSource.querySelector('tr') : null;
        if (!template) return null;

        var newRow = template.cloneNode(true);
        newRow.innerHTML = newRow.innerHTML.replace(/\[INDEX\]/g, '[' + index + ']').replace(/INDEX/g, index);
        newRow.querySelectorAll('.ts-wrapper').forEach(function (wrapper) { wrapper.remove(); });
        newRow.querySelectorAll('select.tomselect-initialized').forEach(function (selectEl) {
            selectEl.classList.remove('tomselect-initialized', 'tomselected', 'ts-hidden-accessible');
            selectEl.style.display = '';
        });
        return newRow;
    }

    function createNewRow() {
        if (!lineContainer) return null;

        var newRow = createRowFromTemplate(nextIndex);
        if (!newRow) return null;

        newRow.setAttribute('data-index', nextIndex);
        lineContainer.appendChild(newRow);
        initializeLineRow(newRow);
        nextIndex++;
        updateEmptyMessage();
        return newRow;
    }

    if (btnAddLine) {
        btnAddLine.addEventListener('click', function (e) {
            e.preventDefault();
            if (hasPurchaseOrderReference()) {
                openPoLineSelector();
                return;
            }
            createNewRow();
        });
    }

    if (lineContainer) {
        lineContainer.querySelectorAll('tr.line-row').forEach(function (row) {
            initializeLineRow(row);
            var index = parseInt(row.getAttribute('data-index'), 10);
            if (!isNaN(index) && index >= nextIndex) nextIndex = index + 1;
        });
    }

    document.body.addEventListener('click', function (evt) {
        var applyBtn = evt.target.closest('.js-gr-po-line-selector-apply');
        if (!applyBtn) return;

        var selectedRows = Array.from(document.querySelectorAll('#gr-po-line-selector-results .js-gr-po-line-selector-item:checked'))
            .map(function (checkbox) { return checkbox.closest('tr'); })
            .filter(function (row) { return !!row; });

        if (selectedRows.length === 0) {
            warn(config.selectAtLeastOnePoLine || 'Select at least one PO line first.');
            return;
        }

        var existingReferenceLineIds = new Set(currentReferenceLineIds());
        var appendedCount = 0;
        selectedRows.forEach(function (row) {
            var payload = parsePoLinePayload(row);
            if (payload.referenceLineId && existingReferenceLineIds.has(payload.referenceLineId)) {
                return;
            }
            if (appendSelectedPoLine(payload)) {
                appendedCount += 1;
            }
            if (payload.referenceLineId) {
                existingReferenceLineIds.add(payload.referenceLineId);
            }
        });

        if (appendedCount === 0) {
            warn(config.noNewPoLineApplied || 'Selected PO lines are already on this draft.');
            return;
        }

        if (window.ERP && window.ERP.ModalSelector) {
            window.ERP.ModalSelector.close('modal-gr-po-line-selector');
            return;
        }

        var modalEl = document.getElementById('modal-gr-po-line-selector');
        if (modalEl && window.bootstrap && window.bootstrap.Modal) {
            var modalInstance = window.bootstrap.Modal.getInstance(modalEl)
                || (window.bootstrap.Modal.getOrCreateInstance ? window.bootstrap.Modal.getOrCreateInstance(modalEl) : null);
            if (modalInstance) modalInstance.hide();
        }
    });

    updateEmptyMessage();

    if (form) {
        form.addEventListener('submit', function (e) {
            function blockSubmit(message) {
                e.preventDefault();
                e.stopImmediatePropagation();
                warn(message);
                return false;
            }

            if (lineContainer && lineContainer.querySelectorAll('tr.line-row').length === 0) {
                return blockSubmit('Please add at least one receipt line.');
            }

            var lines = lineContainer.querySelectorAll('tr.line-row');
            for (var i = 0; i < lines.length; i++) {
                var productInput = lines[i].querySelector('[name*=".productId"]');
                var qtyInput = lines[i].querySelector('.input-qty');
                var uomInput = lines[i].querySelector('.input-uom-id');
                var containerInput = lines[i].querySelector('.select-container');
                var rawContainerValue = containerInput && containerInput.tomselect
                    ? containerInput.tomselect.getValue()
                    : (containerInput ? containerInput.value : '');
                var containerValue = rawContainerValue == null ? '' : String(rawContainerValue).trim();

                if (!productInput || !productInput.value) {
                    return blockSubmit('Please select a product in line ' + (i + 1));
                }
                if (!qtyInput || getNumericValue(qtyInput) <= 0) {
                    return blockSubmit('Please enter a valid quantity in line ' + (i + 1));
                }
                if (!uomInput || !uomInput.value) {
                    return blockSubmit('Please set the UoM detail in line ' + (i + 1));
                }
                if (!containerValue || containerValue === 'null' || containerValue === 'undefined') {
                    return blockSubmit((config.containerRequired || 'Please select a container in line') + ' ' + (i + 1));
                }
            }
        }, true);

        var formDirty = false;

        form.addEventListener('change', function () {
            formDirty = true;
        });
        form.addEventListener('input', function () {
            formDirty = true;
        });

        window.addEventListener('beforeunload', function (e) {
            // Don't show warning if form is currently submitting
            if (formDirty && !form.dataset.isSubmitting && !window.__erpSuppressBeforeUnload && form.offsetParent !== null) {
                e.preventDefault();
                e.returnValue = '';
            }
        });
    }
});
