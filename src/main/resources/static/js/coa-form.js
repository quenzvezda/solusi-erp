document.addEventListener('DOMContentLoaded', function () {
    // Elements
    var parentIdInput = document.getElementById('input-parent-id');
    var parentDisplayInput = document.getElementById('input-parent-display');
    var parentNameInput = document.getElementById('input-parent-name');
    var parentCodeInput = document.getElementById('input-parent-code');
    var levelInput = document.getElementById('input-level');
    var btnSelectParent = document.getElementById('btn-select-parent');
    var btnClearParent = document.getElementById('btn-clear-parent');
    var accountTypeSelect = document.querySelector('[name="accountType"]');
    var codeInput = document.querySelector('[name="code"]');
    var codePrefixHint = document.getElementById('code-prefix-hint');
    var codePrefixText = document.getElementById('code-prefix-text');

    // Account type prefix mapping
    var typePrefixMap = {
        'ASSET': '1xxx',
        'LIABILITY': '2xxx',
        'EQUITY': '3xxx',
        'REVENUE': '4xxx',
        'EXPENSE': '5xxx'
    };

    // Initialize
    init();

    function init() {
        setupModalSelector();
        setupAccountTypeListener();
        setupClearParent();
        updatePrefixHint();
    }

    // --- Modal Selector ---
    function setupModalSelector() {
        if (btnSelectParent) {
            btnSelectParent.addEventListener('click', openParentSelector);
        }

        // Delegate pick button clicks
        document.addEventListener('click', function (evt) {
            var pickBtn = evt.target.closest('.js-coa-parent-pick');
            if (pickBtn) {
                handleParentPick(pickBtn);
            }
        });
    }

    function openParentSelector() {
        if (!window.ERP
            || !window.ERP.ModalSelector
            || !document.getElementById('modal-coa-parent-selector')) {
            return;
        }

        window.ERP.ModalSelector.open({
            modalId: 'modal-coa-parent-selector',
            resultsId: 'coa-parent-selector-results',
            url: '/accounting/coa/selectors/parent'
        });
    }

    function handleParentPick(pickBtn) {
        var row = pickBtn.closest('tr');
        if (!row) return;

        var id = row.getAttribute('data-id');
        var code = row.getAttribute('data-code');
        var name = row.getAttribute('data-name');
        var level = parseInt(row.getAttribute('data-level')) || 1;

        // Set parent values
        parentIdInput.value = id;
        parentDisplayInput.value = code + ' - ' + name;
        if (parentNameInput) parentNameInput.value = name;
        if (parentCodeInput) parentCodeInput.value = code;

        // Auto-derive level
        levelInput.value = level + 1;

        // Close modal
        if (window.ERP && window.ERP.ModalSelector) {
            window.ERP.ModalSelector.close('modal-coa-parent-selector');
        }
    }

    // --- Clear Parent ---
    function setupClearParent() {
        if (btnClearParent) {
            btnClearParent.addEventListener('click', function () {
                parentIdInput.value = '';
                parentDisplayInput.value = '';
                if (parentNameInput) parentNameInput.value = '';
                if (parentCodeInput) parentCodeInput.value = '';
                levelInput.value = 1;

                // Hide clear button after clearing
                btnClearParent.style.display = 'none';
            });
        }
    }

    // --- Account Type Prefix Hint ---
    function setupAccountTypeListener() {
        if (accountTypeSelect) {
            accountTypeSelect.addEventListener('change', updatePrefixHint);
        }
    }

    function updatePrefixHint() {
        if (!accountTypeSelect || !codePrefixHint || !codePrefixText) return;

        var selectedType = accountTypeSelect.value;
        var prefix = typePrefixMap[selectedType];

        if (prefix && codeInput && !codeInput.readOnly) {
            codePrefixText.textContent = 'Suggested prefix: ' + prefix;
            codePrefixHint.style.display = 'block';
        } else {
            codePrefixHint.style.display = 'none';
        }
    }
});
