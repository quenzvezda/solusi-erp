document.addEventListener('DOMContentLoaded', function () {
    const config = window.PurchaseOrderPageConfig || {};
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

    function recalculateLineRow(row) {
        var qtyInput = row.querySelector('.input-qty');
        var priceInput = row.querySelector('.input-unit-price');
        var taxRateInput = row.querySelector('.input-tax-rate');

        if (!qtyInput || !priceInput || !taxRateInput) return;

        var qty = parseFloat((qtyInput.value || '0').replace(/,/g, '')) || 0;
        var price = parseFloat((priceInput.value || '0').replace(/,/g, '')) || 0;
        var taxRate = parseFloat((taxRateInput.value || '0').replace(/,/g, '')) || 0;

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
        [subtotalInput, taxInput, totalInput].forEach(function(input) {
            if (input && typeof AutoNumeric !== 'undefined') {
                var an = AutoNumeric.getAutoNumericElement(input);
                if (an) an.set(input.value);
            }
        });
    }

    // Attach recalculation to existing rows
    lineContainer.querySelectorAll('.line-row').forEach(function(row) {
        row.querySelectorAll('.input-qty, .input-unit-price, .input-tax-rate').forEach(function(input) {
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

            // Remove stale TomSelect markup from cloned row so it can be re-initialized
            newRow.querySelectorAll('.ts-wrapper').forEach(function(w) { w.remove(); });
            newRow.querySelectorAll('select.tomselect-initialized').forEach(function(s) {
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

            // Attach recalculation to new row
            newRow.querySelectorAll('.input-qty, .input-unit-price, .input-tax-rate').forEach(function(input) {
                input.addEventListener('input', function() { recalculateLineRow(newRow); });
                input.addEventListener('change', function() { recalculateLineRow(newRow); });
            });
        });
    }

    lineContainer.addEventListener('click', function (e) {
        var btn = e.target.closest('.btn-remove-line');
        if (btn) {
            var row = btn.closest('.line-row');
            if (row) {
                row.remove();
                updateEmptyMessage();
            }
        }
    });

    updateEmptyMessage();
});
