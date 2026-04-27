document.addEventListener('DOMContentLoaded', function () {
    var config = window.GoodsReceiptPageConfig || {};
    var lineContainer = document.getElementById('line-container');
    var templateSource = document.getElementById('row-template-source');
    var emptyMsg = document.getElementById('empty-msg');
    var btnAddLine = document.getElementById('btn-add-line');
    var form = document.getElementById('gr-form');
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
                    decimalPlaces: 4,
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
        if (!selectEl || typeof initLookup !== 'function') return null;
        return initLookup(selectEl, path);
    }

    function initializeLineRow(row) {
        initializeDecimalFields(row);

        var productSelect = row.querySelector('.select-product');
        var containerSelect = row.querySelector('.select-container');
        var removeBtn = row.querySelector('.btn-remove-line');
        var editBtn = row.querySelector('.btn-edit-detail');
        var uomDisplay = row.querySelector('.input-uom-display');
        var serializedInput = row.querySelector('.input-serialized');
        var serialInput = row.querySelector('.input-sn-single');

        setupLookup(productSelect, 'inventory/products');
        setupLookup(containerSelect, 'inventory/containers');

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

    function createNewRow() {
        if (!templateSource || !lineContainer) return null;
        var template = templateSource.querySelector('tr');
        if (!template) return null;

        var html = template.outerHTML.replace(/\[INDEX\]/g, '[' + nextIndex + ']').replace(/INDEX/g, nextIndex);
        var tempDiv = document.createElement('div');
        tempDiv.innerHTML = html;

        var newRow = tempDiv.querySelector('tr');
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

    updateEmptyMessage();

    if (form) {
        form.addEventListener('submit', function (e) {
            if (lineContainer && lineContainer.querySelectorAll('tr.line-row').length === 0) {
                e.preventDefault();
                warn('Please add at least one receipt line.');
                return false;
            }

            var lines = lineContainer.querySelectorAll('tr.line-row');
            for (var i = 0; i < lines.length; i++) {
                var productInput = lines[i].querySelector('[name*=".productId"]');
                var qtyInput = lines[i].querySelector('.input-qty');
                var uomInput = lines[i].querySelector('.input-uom-id');

                if (!productInput || !productInput.value) {
                    e.preventDefault();
                    warn('Please select a product in line ' + (i + 1));
                    return false;
                }
                if (!qtyInput || getNumericValue(qtyInput) <= 0) {
                    e.preventDefault();
                    warn('Please enter a valid quantity in line ' + (i + 1));
                    return false;
                }
                if (!uomInput || !uomInput.value) {
                    e.preventDefault();
                    warn('Please set the UoM detail in line ' + (i + 1));
                    return false;
                }
            }
        });

        var formDirty = false;
        form.addEventListener('change', function () {
            formDirty = true;
        });
        form.addEventListener('input', function () {
            formDirty = true;
        });

        window.addEventListener('beforeunload', function (e) {
            if (formDirty && form.offsetParent !== null) {
                e.preventDefault();
                e.returnValue = '';
            }
        });
    }
});
