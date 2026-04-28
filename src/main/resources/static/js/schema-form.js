document.addEventListener('DOMContentLoaded', function () {
    var debitAccountIdInput = document.getElementById('input-debit-account-id');
    var debitAccountNameInput = document.getElementById('input-debit-account-name');
    var debitAccountDisplayInput = document.getElementById('input-debit-account-display');
    var creditAccountIdInput = document.getElementById('input-credit-account-id');
    var creditAccountNameInput = document.getElementById('input-credit-account-name');
    var creditAccountDisplayInput = document.getElementById('input-credit-account-display');
    var btnSelectDebitAccount = document.getElementById('btn-select-debit-account');
    var btnSelectCreditAccount = document.getElementById('btn-select-credit-account');
    var btnClearDebitAccount = document.getElementById('btn-clear-debit-account');
    var btnClearCreditAccount = document.getElementById('btn-clear-credit-account');

    var activeTarget = null;

    if (btnSelectDebitAccount) {
        btnSelectDebitAccount.addEventListener('click', function () {
            openSelector('debit');
        });
    }

    if (btnSelectCreditAccount) {
        btnSelectCreditAccount.addEventListener('click', function () {
            openSelector('credit');
        });
    }

    if (btnClearDebitAccount) {
        btnClearDebitAccount.addEventListener('click', function () {
            clearAccount('debit');
        });
    }

    if (btnClearCreditAccount) {
        btnClearCreditAccount.addEventListener('click', function () {
            clearAccount('credit');
        });
    }

    document.body.addEventListener('click', function (evt) {
        var pickBtn = evt.target.closest('.js-schema-account-pick');
        if (!pickBtn || !activeTarget) return;

        var row = pickBtn.closest('tr');
        if (!row) return;

        applyAccountSelection(activeTarget, {
            id: row.getAttribute('data-id'),
            code: row.getAttribute('data-code'),
            name: row.getAttribute('data-name')
        });

        if (window.ERP && window.ERP.ModalSelector) {
            window.ERP.ModalSelector.close('modal-schema-account-selector');
        }
        activeTarget = null;
    });

    function openSelector(target) {
        if (!window.ERP
            || !window.ERP.ModalSelector
            || !document.getElementById('modal-schema-account-selector')) {
            return;
        }

        activeTarget = target;
        window.ERP.ModalSelector.open({
            modalId: 'modal-schema-account-selector',
            resultsId: 'schema-account-selector-results',
            url: '/accounting/schemas/selectors/accounts'
        });
    }

    function applyAccountSelection(target, account) {
        var displayValue = account.code && account.name ? (account.code + ' - ' + account.name) : '';

        if (target === 'debit') {
            if (debitAccountIdInput) debitAccountIdInput.value = account.id || '';
            if (debitAccountNameInput) debitAccountNameInput.value = displayValue;
            if (debitAccountDisplayInput) debitAccountDisplayInput.value = displayValue;
            if (btnClearDebitAccount) btnClearDebitAccount.style.display = account.id ? 'inline-block' : 'none';
            return;
        }

        if (creditAccountIdInput) creditAccountIdInput.value = account.id || '';
        if (creditAccountNameInput) creditAccountNameInput.value = displayValue;
        if (creditAccountDisplayInput) creditAccountDisplayInput.value = displayValue;
        if (btnClearCreditAccount) btnClearCreditAccount.style.display = account.id ? 'inline-block' : 'none';
    }

    function clearAccount(target) {
        if (target === 'debit') {
            if (debitAccountIdInput) debitAccountIdInput.value = '';
            if (debitAccountNameInput) debitAccountNameInput.value = '';
            if (debitAccountDisplayInput) debitAccountDisplayInput.value = '';
            if (btnClearDebitAccount) btnClearDebitAccount.style.display = 'none';
            return;
        }

        if (creditAccountIdInput) creditAccountIdInput.value = '';
        if (creditAccountNameInput) creditAccountNameInput.value = '';
        if (creditAccountDisplayInput) creditAccountDisplayInput.value = '';
        if (btnClearCreditAccount) btnClearCreditAccount.style.display = 'none';
    }
});
