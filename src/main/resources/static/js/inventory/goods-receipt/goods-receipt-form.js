document.addEventListener('DOMContentLoaded', function () {
    var config = window.GoodsReceiptPageConfig || {};
    var isEditable = config.isEditable !== false;

    var lineContainer = document.getElementById('line-container');
    var templateSource = document.getElementById('row-template-source');
    var emptyMsg = document.getElementById('empty-msg');
    var btnAddLine = document.getElementById('btn-add-line');
    var form = document.getElementById('gr-form');

    var nextIndex = (lineContainer && lineContainer.querySelectorAll('tr').length) || 0;

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

    function initializeAutonumericFields(container) {
        var decimalInputs = container.querySelectorAll('.erp-number-decimal');
        decimalInputs.forEach(function(input) {
            if (input.autonumeric) return; // Already initialized
            try {
                new AutoNumeric(input, {
                    currencySymbol: '',
                    decimalCharacter: '.',
                    digitGroupSeparator: ',',
                    decimalPlaces: 4,
                    modifyValueOnWheel: false
                });
            } catch (e) {
                console.warn('AutoNumeric initialization failed', e);
            }
        });
    }

    function initializeLineRow(row, index) {
        var productSelect = row.querySelector('[name="lines[' + index + '].productId"]');
        var uomSelect = row.querySelector('[name="lines[' + index + '].uomId"]');
        var containerSelect = row.querySelector('[name="lines[' + index + '].containerId"]');
        var qtyInput = row.querySelector('.input-qty');
        var serialCheckbox = row.querySelector('.chk-serialized');
        var serialNumberInput = row.querySelector('.input-serial-number');
        var removeBtn = row.querySelector('.btn-remove-line');

        // Initialize lookups
        if (productSelect && !productSelect.tomselect) {
            initializeLookup(productSelect, 'inventory/products');
        }
        if (uomSelect && !uomSelect.tomselect) {
            initializeLookup(uomSelect, 'inventory/uoms');
        }
        if (containerSelect && !containerSelect.tomselect) {
            initializeLookup(containerSelect, 'inventory/containers');
        }

        // Initialize numeric fields
        if (qtyInput && !qtyInput.autonumeric) {
            try {
                new AutoNumeric(qtyInput, {
                    currencySymbol: '',
                    decimalCharacter: '.',
                    digitGroupSeparator: ',',
                    decimalPlaces: 4,
                    modifyValueOnWheel: false
                });
            } catch (e) {
                console.warn('AutoNumeric init failed', e);
            }
        }

        // Handle serialized checkbox
        if (serialCheckbox) {
            serialCheckbox.addEventListener('change', function() {
                serialNumberInput.disabled = !this.checked;
                if (!this.checked) serialNumberInput.value = '';
            });
            // Initial state
            serialNumberInput.disabled = !serialCheckbox.checked;
        }

        // Remove line button
        if (removeBtn) {
            removeBtn.addEventListener('click', function(e) {
                e.preventDefault();
                row.remove();
                updateEmptyMessage();
            });
        }
    }

    function initializeLookup(selectEl, path) {
        if (!selectEl) return;
        // TomSelect initialization will be handled by the fragment/shared lookup logic
        // Just trigger event if needed
        selectEl.addEventListener('erp:lookup-initialized', function() {
            // Lookup ready
        });
    }

    function createNewRow() {
        if (!templateSource) return;
        
        var template = templateSource.querySelector('tr');
        if (!template) return;

        var newRow = template.cloneNode(true);
        newRow.setAttribute('data-index', nextIndex);
        newRow.classList.remove('line-row');

        // Replace INDEX with actual index in all attributes and names
        var html = newRow.outerHTML;
        html = html.replace(/\[INDEX\]/g, '[' + nextIndex + ']');
        html = html.replace(/INDEX/g, nextIndex);

        var tempDiv = document.createElement('div');
        tempDiv.innerHTML = html;
        newRow = tempDiv.querySelector('tr');

        lineContainer.appendChild(newRow);
        initializeLineRow(newRow, nextIndex);
        nextIndex++;

        updateEmptyMessage();
        return newRow;
    }

    function updateEmptyMessage() {
        if (!emptyMsg || !lineContainer) return;
        var hasRows = lineContainer.querySelectorAll('tr').length > 0;
        emptyMsg.style.display = hasRows ? 'none' : '';
    }

    // Add Line Button
    if (btnAddLine) {
        btnAddLine.addEventListener('click', function(e) {
            e.preventDefault();
            createNewRow();
        });
    }

    // Initialize existing rows
    if (lineContainer) {
        var rows = lineContainer.querySelectorAll('tr.line-row');
        rows.forEach(function(row, idx) {
            var indexAttr = row.getAttribute('data-index');
            var index = indexAttr !== null ? parseInt(indexAttr) : idx;
            initializeLineRow(row, index);
            if (index >= nextIndex) nextIndex = index + 1;
        });
    }

    updateEmptyMessage();

    // Form Validation
    if (form) {
        form.addEventListener('submit', function(e) {
            // Validate at least one line exists
            if (lineContainer && lineContainer.querySelectorAll('tr').length === 0) {
                e.preventDefault();
                warn('Please add at least one receipt line.');
                return false;
            }

            // Validate required fields in each line
            var lines = lineContainer.querySelectorAll('tr');
            for (var i = 0; i < lines.length; i++) {
                var productInput = lines[i].querySelector('[name*=".productId"]');
                var qtyInput = lines[i].querySelector('.input-qty');
                var uomInput = lines[i].querySelector('[name*=".uomId"]');

                if (!productInput || !productInput.value) {
                    e.preventDefault();
                    warn('Please select a product in line ' + (i + 1));
                    return false;
                }
                if (!qtyInput || !qtyInput.value || parseFloat(qtyInput.value) <= 0) {
                    e.preventDefault();
                    warn('Please enter a valid quantity in line ' + (i + 1));
                    return false;
                }
                if (!uomInput || !uomInput.value) {
                    e.preventDefault();
                    warn('Please select a UoM in line ' + (i + 1));
                    return false;
                }
            }
        });
    }

    // Dirty check for unsaved changes
    var formDirty = false;
    if (form) {
        var allInputs = form.querySelectorAll('input, select, textarea');
        allInputs.forEach(function(input) {
            input.addEventListener('change', function() {
                formDirty = true;
            });
        });

        window.addEventListener('beforeunload', function(e) {
            if (formDirty && form.offsetParent !== null) { // Form is visible
                e.preventDefault();
                e.returnValue = '';
            }
        });
    }
});
