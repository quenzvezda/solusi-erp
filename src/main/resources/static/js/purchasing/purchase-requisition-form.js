document.addEventListener('DOMContentLoaded', function () {
    const config = window.PurchaseRequisitionPageConfig || {};
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

    if (btnAddLine) {
        btnAddLine.addEventListener('click', function () {
            const index = getNextIndex();
            const templateRow = templateSource.querySelector('.line-row');
            const newRow = templateRow.cloneNode(true);
            newRow.setAttribute('data-index', index);

            newRow.innerHTML = newRow.innerHTML.replace(/INDEX/g, index);

            lineContainer.appendChild(newRow);
            updateEmptyMessage();

            // Re-initialize autocomplete on new row if ERP utility exists
            if (window.ERP && window.ERP.initAutocompleteInContainer) {
                window.ERP.initAutocompleteInContainer(newRow);
            }
        });
    }

    lineContainer.addEventListener('click', function (e) {
        const btn = e.target.closest('.btn-remove-line');
        if (btn) {
            const row = btn.closest('.line-row');
            if (row) {
                row.remove();
                updateEmptyMessage();
            }
        }
    });

    updateEmptyMessage();
});
