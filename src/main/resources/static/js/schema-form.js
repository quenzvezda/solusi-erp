document.addEventListener('DOMContentLoaded', function () {
    const linesContainer = document.getElementById('lines-container');
    const btnAddLine = document.getElementById('btn-add-line');
    const lineTemplate = document.getElementById('line-template');
    const eventTypeSelect = document.getElementById('event-type-select');
    const hiddenEventType = document.getElementById('hidden-event-type');

    let activeTargetRow = null;

    function getCurrentEventType() {
        if (hiddenEventType) return hiddenEventType.value;
        return eventTypeSelect.value;
    }

    function filterVariables() {
        const currentEvent = getCurrentEventType();
        document.querySelectorAll('.variable-select').forEach(select => {
            let hasVisible = false;
            Array.from(select.options).forEach(opt => {
                if (opt.getAttribute('data-event') === currentEvent) {
                    opt.style.display = '';
                    hasVisible = true;
                } else {
                    opt.style.display = 'none';
                    if (opt.selected) {
                        opt.selected = false;
                    }
                }
            });
            if (hasVisible && select.selectedIndex === -1) {
                // Select first visible
                for (let i=0; i<select.options.length; i++) {
                    if (select.options[i].style.display !== 'none') {
                        select.selectedIndex = i;
                        break;
                    }
                }
            }
        });
    }

    if (eventTypeSelect) {
        eventTypeSelect.addEventListener('change', filterVariables);
    }
    
    // Initial filter
    filterVariables();

    function updateLineIndices() {
        document.querySelectorAll('.schema-line').forEach((row, index) => {
            row.querySelectorAll('input, select').forEach(input => {
                if (input.name) {
                    input.name = input.name.replace(/lines\[\d+\]/, `lines[${index}]`);
                }
            });
        });
    }

    if (btnAddLine) {
        btnAddLine.addEventListener('click', function () {
            const index = document.querySelectorAll('.schema-line').length;
            const content = lineTemplate.innerHTML.replace(/INDEX/g, index);
            linesContainer.insertAdjacentHTML('beforeend', content);
            filterVariables();
        });
    }

    linesContainer.addEventListener('click', function (e) {
        if (e.target.closest('.btn-remove-line')) {
            e.target.closest('.schema-line').remove();
            updateLineIndices();
        }

        const btnSelect = e.target.closest('.btn-select-account');
        if (btnSelect) {
            activeTargetRow = btnSelect.closest('.schema-line');
            if (window.ERP && window.ERP.ModalSelector) {
                window.ERP.ModalSelector.open({
                    modalId: 'modal-schema-account-selector',
                    resultsId: 'schema-account-selector-results',
                    url: '/accounting/schemas/selectors/accounts'
                });
            }
        }
    });

    document.body.addEventListener('click', function (evt) {
        var pickBtn = evt.target.closest('.js-schema-account-pick');
        if (!pickBtn || !activeTargetRow) return;
        
        var row = pickBtn.closest('tr');
        if (!row) return;
        
        const idInput = activeTargetRow.querySelector('.account-id');
        const displayInput = activeTargetRow.querySelector('.account-display');
        
        if (idInput) idInput.value = row.getAttribute('data-id');
        if (displayInput) displayInput.value = row.getAttribute('data-code') + ' - ' + row.getAttribute('data-name');
        
        if (window.ERP && window.ERP.ModalSelector) {
            window.ERP.ModalSelector.close('modal-schema-account-selector');
        }
        activeTargetRow = null;
    });
});