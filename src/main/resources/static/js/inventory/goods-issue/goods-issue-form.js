(function () {
    'use strict';

    document.addEventListener('DOMContentLoaded', function () {
        var config = window.GoodsIssuePageConfig || {};
        var form = document.getElementById('gi-form');
        var lineContainer = document.getElementById('line-container');
        var rowTemplateSource = document.getElementById('row-template-source');
        var addLineButton = document.getElementById('btn-add-line');
        var emptyMessage = document.getElementById('empty-msg');
        var modalId = 'modal-gi-source-line-selector';
        var selectorResultsId = 'gi-source-line-selector-results';
        var currentDrawerRow = null;
        var formDirty = false;
        var suppressBeforeUnload = false;
        var nextIndex = findNextIndex();

        function warn(message) {
            if (window.ErpModal && typeof ErpModal.showWarning === 'function') {
                ErpModal.showWarning(message);
                return;
            }
            window.alert(message);
        }

        function getNumeric(input) {
            if (!input) return 0;
            if (window.ErpNumeric && typeof ErpNumeric.get === 'function') {
                var numericValue = ErpNumeric.get(input);
                return Number(numericValue || 0);
            }
            return Number(String(input.value || '0').replace(/\./g, '').replace(',', '.')) || 0;
        }

        function setNumeric(input, value) {
            if (!input) return;
            if (window.ErpNumeric && typeof ErpNumeric.set === 'function') {
                ErpNumeric.set(input, value || 0);
                return;
            }
            input.value = value == null ? '' : String(value);
        }

        function getInputValue(element) {
            if (!element) return '';
            if (element.tomselect) return element.tomselect.getValue();
            return element.value || '';
        }

        function setLookupValue(selectEl, id, name, subtext, payload) {
            if (!selectEl || !id) return;
            var value = String(id);
            var optionData = {
                id: value,
                value: value,
                name: name || value,
                text: name || value,
                subText: subtext || '',
                payload: payload || {}
            };

            if (selectEl.tomselect) {
                selectEl.tomselect.addOption(optionData);
                selectEl.tomselect.setValue(value, true);
                return;
            }

            var option = selectEl.querySelector('option[value="' + value.replace(/"/g, '\\"') + '"]');
            if (!option) {
                option = document.createElement('option');
                option.value = value;
                selectEl.appendChild(option);
            }
            option.textContent = name || value;
            if (subtext) option.setAttribute('data-subtext', subtext);
            option.selected = true;
            selectEl.value = value;
        }

        function setHidden(row, selector, value) {
            var input = row.querySelector(selector);
            if (input) input.value = value == null ? '' : String(value);
        }

        function findNextIndex() {
            if (!lineContainer) return 0;
            var max = -1;
            lineContainer.querySelectorAll('tr.line-row').forEach(function (row) {
                var index = Number(row.getAttribute('data-index'));
                if (!Number.isNaN(index)) max = Math.max(max, index);
            });
            return max + 1;
        }

        function updateEmptyMessage() {
            if (!emptyMessage || !lineContainer) return;
            emptyMessage.style.display = lineContainer.querySelector('tr.line-row') ? 'none' : '';
        }

        function rewriteLineIndices() {
            if (!lineContainer) return;
            lineContainer.querySelectorAll('tr.line-row').forEach(function (row, index) {
                row.setAttribute('data-index', String(index));
                row.querySelectorAll('[name]').forEach(function (input) {
                    input.name = input.name.replace(/lines\[\d+]/g, 'lines[' + index + ']');
                });
            });
            nextIndex = findNextIndex();
        }

        function createRowFromTemplate() {
            if (!rowTemplateSource || !rowTemplateSource.firstElementChild) return null;
            var html = rowTemplateSource.innerHTML.replace(/INDEX/g, String(nextIndex));
            var wrapper = document.createElement('tbody');
            wrapper.innerHTML = html;
            var row = wrapper.querySelector('tr.line-row');
            if (!row) return null;
            row.setAttribute('data-index', String(nextIndex));
            nextIndex += 1;
            return row;
        }

        function addBlankRow() {
            var row = createRowFromTemplate();
            if (!row || !lineContainer) return null;
            row.setAttribute('data-source-derived', 'false');
            lineContainer.appendChild(row);
            initializeLineRow(row);
            updateSummary();
            updateEmptyMessage();
            return row;
        }

        function isManualReference() {
            var referenceTypeInput = form ? form.querySelector('[name="referenceType"]') : null;
            var referenceType = referenceTypeInput && referenceTypeInput.value ? referenceTypeInput.value : config.referenceType;
            return String(referenceType || '') === String(config.manualReferenceType || 'MANUAL');
        }

        function currentReferenceLineIds() {
            if (!lineContainer) return [];
            return Array.from(lineContainer.querySelectorAll('input[name*=".referenceLineId"]'))
                .map(function (input) { return String(input.value || '').trim(); })
                .filter(Boolean);
        }

        function duplicateReferenceLine(referenceLineId) {
            if (!referenceLineId) return false;
            return currentReferenceLineIds().indexOf(String(referenceLineId)) >= 0;
        }

        function openSourceLineSelector() {
            if (!config.sourceLineSelectorUrl) {
                warn(config.selectorUnavailableMessage || 'Source line selector is not available.');
                return;
            }
            if (!config.referenceId && !isManualReference()) {
                warn(config.referenceRequiredMessage || 'Source reference is required before selecting lines.');
                return;
            }

            var url = new URL(config.sourceLineSelectorUrl, window.location.origin);
            if (config.referenceType) url.searchParams.set('referenceType', config.referenceType);
            if (config.referenceId) url.searchParams.set('referenceId', config.referenceId);
            currentReferenceLineIds().forEach(function (id) {
                url.searchParams.append('excludeReferenceLineIds', id);
            });

            if (window.ERP && window.ERP.ModalSelector) {
                window.ERP.ModalSelector.open({
                    modalId: modalId,
                    resultsId: selectorResultsId,
                    url: url.pathname + url.search
                });
                return;
            }

            warn(config.selectorHelperMissingMessage || 'Modal selector helper is not loaded.');
        }

        function parseSourceLinePayload(row) {
            var data = row.dataset || {};
            return {
                referenceLineId: data.referenceLineId,
                productId: data.productId,
                productName: data.productName,
                productSubtext: data.productSubtext,
                uomId: data.uomId,
                uomName: data.uomName,
                uomSubtext: data.uomSubtext,
                facilityId: data.facilityId,
                facilityName: data.facilityName,
                gridId: data.gridId,
                gridName: data.gridName,
                gridSubtext: data.gridSubtext,
                containerId: data.containerId,
                containerName: data.containerName,
                containerSubtext: data.containerSubtext,
                valuationRefType: data.valuationRefType,
                valuationRefId: data.valuationRefId,
                valuationRefLineId: data.valuationRefLineId,
                serialized: data.serialized === 'true',
                remainingQuantity: Number(data.remainingQuantity || data.quantityIssued || 0),
                baseQuantity: Number(data.baseQuantity || data.remainingQuantity || 0),
                unitCost: Number(data.unitCost || 0),
                inventoryAmount: Number(data.inventoryAmount || 0)
            };
        }

        function fillLineFromSource(row, payload) {
            row.setAttribute('data-source-derived', 'true');
            row.classList.add('source-derived');
            setHidden(row, 'input[name*=".referenceLineId"]', payload.referenceLineId);
            setHidden(row, '.input-serialized', payload.serialized ? 'true' : 'false');
            setHidden(row, '.input-uom-id', payload.uomId);
            setHidden(row, '.input-facility-id', payload.facilityId);
            setHidden(row, '.input-valuation-ref-type', payload.valuationRefType);
            setHidden(row, '.input-valuation-ref-id', payload.valuationRefId);
            setHidden(row, '.input-valuation-ref-line-id', payload.valuationRefLineId);

            setLookupValue(row.querySelector('.select-product'), payload.productId, payload.productName, payload.productSubtext, payload);
            setLookupValue(row.querySelector('.select-grid'), payload.gridId, payload.gridName, payload.gridSubtext, payload);
            setLookupValue(row.querySelector('.select-container'), payload.containerId, payload.containerName, payload.containerSubtext, payload);

            var facilityDisplay = row.querySelector('.line-facility-display');
            if (facilityDisplay) facilityDisplay.textContent = payload.facilityName || '';
            var uomDisplay = row.querySelector('.input-uom-display');
            if (uomDisplay) uomDisplay.value = payload.uomName || payload.uomSubtext || '';

            setNumeric(row.querySelector('.input-qty'), payload.remainingQuantity);
            setNumeric(row.querySelector('.input-base-qty'), payload.baseQuantity || payload.remainingQuantity);
            setNumeric(row.querySelector('.input-unit-cost'), payload.unitCost);
            setNumeric(row.querySelector('.input-inventory-amount'), payload.inventoryAmount || (payload.remainingQuantity * payload.unitCost));

            lockSourceDerivedRow(row);
        }

        function appendSelectedSourceLine(payload) {
            if (!payload || !payload.referenceLineId || duplicateReferenceLine(payload.referenceLineId)) {
                return false;
            }
            var row = addBlankRow();
            if (!row) return false;
            fillLineFromSource(row, payload);
            updateSummary();
            return true;
        }

        function closeSourceLineSelector() {
            if (window.ERP && window.ERP.ModalSelector) {
                window.ERP.ModalSelector.close(modalId);
            }
        }

        function applySelectedSourceLines() {
            var selectedRows = Array.from(document.querySelectorAll('#' + selectorResultsId + ' .js-gi-source-line-selector-item:checked'))
                .map(function (checkbox) { return checkbox.closest('tr'); })
                .filter(Boolean);
            if (selectedRows.length === 0) {
                warn(config.selectAtLeastOneSourceLine || 'Select at least one source line first.');
                return;
            }

            var added = 0;
            var existingReferenceLineIds = new Set(currentReferenceLineIds());
            selectedRows.forEach(function (row) {
                var payload = parseSourceLinePayload(row);
                if (payload.referenceLineId && existingReferenceLineIds.has(String(payload.referenceLineId))) {
                    return;
                }
                if (appendSelectedSourceLine(payload)) {
                    added += 1;
                    existingReferenceLineIds.add(String(payload.referenceLineId));
                }
            });

            if (added === 0) {
                warn(config.noNewSourceLineApplied || 'Selected source lines are already on this draft.');
                return;
            }
            closeSourceLineSelector();
        }

        function initCascadingLookups(row) {
            var gridSelect = row.querySelector('.select-grid');
            var containerSelect = row.querySelector('.select-container');

            if (gridSelect && !gridSelect.tomselect && typeof window.initLookup === 'function') {
                window.initLookup(gridSelect, 'inventory/grids', function () {
                    return { id: getHeaderFacilityId(row), key: 'facilityId' };
                });
            }

            if (containerSelect && !containerSelect.tomselect && typeof window.initLookup === 'function') {
                window.initLookup(containerSelect, 'inventory/containers', function () {
                    return { id: getInputValue(gridSelect), key: 'gridId' };
                });
            }

            if (containerSelect && containerSelect.tomselect) {
                containerSelect.tomselect.on('change', function (value) {
                    if (!value) return;
                    var option = containerSelect.tomselect.options[value];
                    var payload = option && option.payload ? option.payload : {};
                    applyContainerParentPayload(row, payload);
                });
            }
        }

        function applyContainerParentPayload(row, payload) {
            if (!payload) return;
            if (payload.gridId) {
                setLookupValue(row.querySelector('.select-grid'), payload.gridId, payload.gridName, payload.gridCode, payload);
            }
            if (payload.facilityId) {
                setHidden(row, '.input-facility-id', payload.facilityId);
                var facilityDisplay = row.querySelector('.line-facility-display');
                if (facilityDisplay) facilityDisplay.textContent = payload.facilityName || payload.facilityCode || '';
            }
        }

        function getHeaderFacilityId(row) {
            var lineFacility = row ? row.querySelector('.input-facility-id') : null;
            if (lineFacility && lineFacility.value) return lineFacility.value;
            var headerFacility = form ? form.querySelector('[name="facilityId"]') : null;
            return headerFacility ? headerFacility.value : '';
        }

        function lockSourceDerivedRow(row) {
            if (row.getAttribute('data-source-derived') !== 'true') return;
            row.querySelectorAll('.input-valuation-ref-type, .input-valuation-ref-id, .input-valuation-ref-line-id').forEach(function (input) {
                input.readOnly = true;
            });
            var productSelect = row.querySelector('.select-product');
            if (productSelect && productSelect.tomselect) {
                productSelect.tomselect.lock();
            }
        }

        function initializeLineRow(row) {
            if (!row) return;
            if (window.ERP && typeof window.ERP.initAutocompleteInContainer === 'function') {
                window.ERP.initAutocompleteInContainer(row);
            }
            initCascadingLookups(row);
            lockSourceDerivedRow(row);
            row.querySelectorAll('.erp-number-decimal').forEach(function (input) {
                if (window.ErpNumeric && typeof ErpNumeric.init === 'function') {
                    ErpNumeric.init(input);
                }
            });
        }

        function resetLineLocations() {
            if (!lineContainer) return;
            lineContainer.querySelectorAll('tr.line-row').forEach(function (row) {
                setLookupValue(row.querySelector('.select-grid'), '', '', '', {});
                setLookupValue(row.querySelector('.select-container'), '', '', '', {});
                setHidden(row, '.input-facility-id', getHeaderFacilityId(row));
            });
        }

        function handleHeaderFacilityChange() {
            if (!lineContainer || !lineContainer.querySelector('tr.line-row')) return;
            var message = config.facilityChangeConfirm || 'Changing the facility will reset line grid and container fields. Continue?';
            if (window.ErpModal && typeof ErpModal.confirm === 'function') {
                ErpModal.confirm(message, resetLineLocations);
                return;
            }
            warn(message);
        }

        function openDetailDrawer(row) {
            currentDrawerRow = row;
            var serialized = String(row.querySelector('.input-serialized')?.value || 'false') === 'true';
            var drawerId = serialized ? 'drawer-serial' : 'drawer-non-serial';
            var drawer = document.getElementById(drawerId);
            if (!drawer) return;

            setNumeric(drawer.querySelector('.input-qty-target'), getNumeric(row.querySelector('.input-qty')));
            setNumeric(drawer.querySelector('.input-qty-base'), getNumeric(row.querySelector('.input-base-qty')));
            populateDrawerUom(drawer, row);
            if (serialized) syncSerialInputs(drawer, row);

            if (window.ErpDrawer && typeof ErpDrawer.open === 'function') {
                ErpDrawer.open(drawerId);
            }
        }

        function populateDrawerUom(drawer, row) {
            var select = drawer.querySelector('.select-uom-target');
            if (!select) return;
            var currentUomId = row.querySelector('.input-uom-id')?.value || '';
            var currentUomName = row.querySelector('.input-uom-display')?.value || currentUomId;
            select.innerHTML = '';
            if (currentUomId) {
                var option = document.createElement('option');
                option.value = currentUomId;
                option.textContent = currentUomName;
                option.setAttribute('data-factor', '1');
                select.appendChild(option);
                select.value = currentUomId;
            }
        }

        function syncSerialInputs(drawer, row) {
            var container = drawer.querySelector('.serial-input-container');
            if (!container) return;
            var baseQty = Math.max(0, Math.floor(getNumeric(drawer.querySelector('.input-qty-base'))));
            var serials = String(row.querySelector('.input-serial-csv')?.value || '')
                .split(',')
                .map(function (serial) { return serial.trim(); })
                .filter(Boolean);
            container.innerHTML = '';
            for (var i = 0; i < baseQty; i++) {
                var input = document.createElement('input');
                input.type = 'text';
                input.className = 'form-control form-control-sm mb-2 input-sn-item';
                input.value = serials[i] || '';
                input.setAttribute('placeholder', 'Serial ' + (i + 1));
                container.appendChild(input);
            }
        }

        function saveDrawer(drawer) {
            if (!currentDrawerRow) return;
            var serialized = drawer.id === 'drawer-serial';
            var baseQty = getNumeric(drawer.querySelector('.input-qty-base'));
            if (serialized && !Number.isInteger(baseQty)) {
                warn(config.serialWholeNumberWarning || 'Serialized items require a whole base quantity.');
                return;
            }

            setNumeric(currentDrawerRow.querySelector('.input-qty'), getNumeric(drawer.querySelector('.input-qty-target')));
            setNumeric(currentDrawerRow.querySelector('.input-base-qty'), baseQty);
            var uomSelect = drawer.querySelector('.select-uom-target');
            if (uomSelect && uomSelect.value) {
                setHidden(currentDrawerRow, '.input-uom-id', uomSelect.value);
                var uomDisplay = currentDrawerRow.querySelector('.input-uom-display');
                if (uomDisplay) uomDisplay.value = uomSelect.options[uomSelect.selectedIndex]?.text || '';
            }
            if (serialized) {
                var serialCsv = Array.from(drawer.querySelectorAll('.input-sn-item'))
                    .map(function (input) { return input.value.trim(); })
                    .filter(Boolean)
                    .join(',');
                setHidden(currentDrawerRow, '.input-serial-csv', serialCsv);
            }
            updateSummary();
            if (window.ErpDrawer && typeof ErpDrawer.close === 'function') {
                ErpDrawer.close(drawer.id);
            }
        }

        function recalculateLine(row) {
            var qty = getNumeric(row.querySelector('.input-qty'));
            var baseQty = getNumeric(row.querySelector('.input-base-qty')) || qty;
            var unitCost = getNumeric(row.querySelector('.input-unit-cost'));
            if (!getNumeric(row.querySelector('.input-base-qty'))) {
                setNumeric(row.querySelector('.input-base-qty'), baseQty);
            }
            setNumeric(row.querySelector('.input-inventory-amount'), baseQty * unitCost);
        }

        function updateSummary() {
            if (!lineContainer) return;
            var rows = Array.from(lineContainer.querySelectorAll('tr.line-row'));
            var totalBaseQty = 0;
            var totalInventoryAmount = 0;
            var totalTaxAmount = 0;
            rows.forEach(function (row) {
                recalculateLine(row);
                totalBaseQty += getNumeric(row.querySelector('.input-base-qty'));
                totalInventoryAmount += getNumeric(row.querySelector('.input-inventory-amount'));
                totalTaxAmount += getNumeric(row.querySelector('.input-tax-amount'));
            });
            var lineCount = document.getElementById('summary-line-count');
            var baseQty = document.getElementById('summary-base-qty');
            var inventoryAmount = document.getElementById('summary-inventory-amount');
            var taxAmount = document.getElementById('summary-tax-amount');
            if (lineCount) lineCount.textContent = String(rows.length);
            if (baseQty) baseQty.textContent = totalBaseQty.toFixed(4);
            if (inventoryAmount) inventoryAmount.textContent = totalInventoryAmount.toFixed(2);
            if (taxAmount) taxAmount.textContent = totalTaxAmount.toFixed(2);
        }

        function validateBeforeSubmit(event) {
            function block(message) {
                event.preventDefault();
                event.stopImmediatePropagation();
                warn(message);
                return false;
            }

            var rows = lineContainer ? Array.from(lineContainer.querySelectorAll('tr.line-row')) : [];
            if (rows.length === 0) return block(config.lineRequiredMessage || 'Add at least one issue line.');

            for (var i = 0; i < rows.length; i++) {
                var row = rows[i];
                var lineNo = i + 1;
                var product = row.querySelector('.select-product');
                var grid = row.querySelector('.select-grid');
                var container = row.querySelector('.select-container');
                var serialized = String(row.querySelector('.input-serialized')?.value || 'false') === 'true';
                var serials = String(row.querySelector('.input-serial-csv')?.value || '').split(',').filter(Boolean);
                var baseQty = getNumeric(row.querySelector('.input-base-qty'));

                if (!getInputValue(product)) return block('Select product in line ' + lineNo + '.');
                if (getNumeric(row.querySelector('.input-qty')) <= 0) return block('Enter positive quantity in line ' + lineNo + '.');
                if (!row.querySelector('.input-uom-id')?.value) return block('Set UoM in line ' + lineNo + '.');
                if (!getHeaderFacilityId(row)) return block('Set facility in line ' + lineNo + '.');
                if (!getInputValue(grid)) return block('Set grid in line ' + lineNo + '.');
                if (!getInputValue(container)) return block('Set container in line ' + lineNo + '.');
                if (serialized && (!Number.isInteger(baseQty) || serials.length !== baseQty)) {
                    return block('Serial count must match whole base quantity in line ' + lineNo + '.');
                }
            }
            return true;
        }

        if (addLineButton) {
            addLineButton.addEventListener('click', function (event) {
                event.preventDefault();
                if (isManualReference()) {
                    addBlankRow();
                    return;
                }
                openSourceLineSelector();
            });
        }

        if (lineContainer) {
            lineContainer.querySelectorAll('tr.line-row').forEach(initializeLineRow);
            lineContainer.addEventListener('click', function (event) {
                var removeButton = event.target.closest('.btn-remove-line');
                if (removeButton) {
                    removeButton.closest('tr.line-row')?.remove();
                    rewriteLineIndices();
                    updateSummary();
                    updateEmptyMessage();
                    formDirty = true;
                    return;
                }
                var detailButton = event.target.closest('.btn-edit-detail');
                if (detailButton) {
                    openDetailDrawer(detailButton.closest('tr.line-row'));
                }
            });
            lineContainer.addEventListener('input', updateSummary);
            lineContainer.addEventListener('change', updateSummary);
        }

        document.body.addEventListener('click', function (event) {
            if (event.target.closest('.js-gi-source-line-selector-apply')) {
                applySelectedSourceLines();
                return;
            }
            var sourcePick = event.target.closest('.js-gi-source-line-selector-pick');
            if (sourcePick) {
                var row = sourcePick.closest('tr');
                if (row) {
                    appendSelectedSourceLine(parseSourceLinePayload(row));
                    closeSourceLineSelector();
                }
            }
        });

        document.querySelectorAll('#drawer-non-serial .btn-save-drawer, #drawer-serial .btn-save-drawer').forEach(function (button) {
            button.addEventListener('click', function () {
                saveDrawer(button.closest('.offcanvas'));
            });
        });

        if (form) {
            var headerFacility = form.querySelector('[name="facilityId"]:not([type="hidden"])');
            if (headerFacility) headerFacility.addEventListener('change', handleHeaderFacilityChange);
            form.addEventListener('submit', validateBeforeSubmit, true);
            form.addEventListener('input', function () { formDirty = true; });
            form.addEventListener('change', function () { formDirty = true; });
            form.querySelectorAll('[data-action-url]').forEach(function (button) {
                button.addEventListener('click', function () {
                    suppressBeforeUnload = true;
                    window.__erpSuppressBeforeUnload = true;
                    window.setTimeout(function () {
                        suppressBeforeUnload = false;
                        window.__erpSuppressBeforeUnload = false;
                    }, 15000);
                }, true);
            });
        }

        window.addEventListener('beforeunload', function (event) {
            if (!formDirty || suppressBeforeUnload || window.__erpSuppressBeforeUnload) return;
            event.preventDefault();
            event.returnValue = '';
        });

        updateSummary();
        updateEmptyMessage();
    });
})();
