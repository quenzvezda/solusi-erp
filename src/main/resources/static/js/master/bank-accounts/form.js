document.addEventListener('DOMContentLoaded', function () {
    var btnSelectCoa = document.getElementById('btn-select-bank-account-coa');
    var coaIdInput = document.getElementById('bank-account-coa-id');
    var coaDisplayInput = document.getElementById('bank-account-coa-display');

    function openCoaSelector() {
        if (!window.ERP || !window.ERP.ModalSelector) return;

        window.ERP.ModalSelector.open({
            modalId: 'modal-bank-account-coa-selector',
            resultsId: 'bank-account-coa-selector-modal-body',
            url: '/master/bank-accounts/selectors/coa'
        });
    }

    function applyCoaSelection(button) {
        if (!button || !coaIdInput || !coaDisplayInput) return;

        var code = button.dataset.coaCode || '';
        var name = button.dataset.coaName || '';

        coaIdInput.value = button.dataset.coaId || '';
        coaDisplayInput.value = code && name ? code + ' - ' + name : (code || name);

        if (window.ERP && window.ERP.ModalSelector) {
            window.ERP.ModalSelector.close('modal-bank-account-coa-selector');
        }
    }

    if (btnSelectCoa) {
        btnSelectCoa.addEventListener('click', openCoaSelector);
    }

    document.body.addEventListener('click', function (evt) {
        var pickBtn = evt.target.closest('.js-bank-account-coa-pick');
        if (!pickBtn) return;

        applyCoaSelection(pickBtn);
    });
});
