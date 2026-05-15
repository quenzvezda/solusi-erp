document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('vendor-payment-form');
    if (!form) return;

    const paymentAmountInput = document.getElementById('paymentAmount');
    const recapPaymentAmount = document.getElementById('recap-payment-amount');
    const recapApplied = document.getElementById('recap-applied');
    const recapUnapplied = document.getElementById('recap-unapplied');

    function parseNumeric(value) {
        if (!value) return 0;
        const cleaned = value.replace(/,/g, '');
        return parseFloat(cleaned) || 0;
    }

    function formatNumber(value) {
        return value.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    }

    function updateRecap() {
        const paymentAmount = parseNumeric(paymentAmountInput?.value);
        const paidInputs = document.querySelectorAll('.paid-amount-input');
        let totalApplied = 0;
        paidInputs.forEach(function (input) {
            totalApplied += parseNumeric(input.value);
        });
        const unapplied = paymentAmount - totalApplied;

        if (recapPaymentAmount) recapPaymentAmount.textContent = formatNumber(paymentAmount);
        if (recapApplied) recapApplied.textContent = formatNumber(totalApplied);
        if (recapUnapplied) {
            recapUnapplied.textContent = formatNumber(unapplied);
            recapUnapplied.classList.toggle('text-danger', unapplied !== 0);
            recapUnapplied.classList.toggle('text-success', unapplied === 0);
        }
    }

    document.getElementById('allocation-lines')?.addEventListener('input', function (e) {
        if (e.target.classList.contains('paid-amount-input')) {
            updateRecap();
        }
    });

    if (paymentAmountInput) {
        paymentAmountInput.addEventListener('input', updateRecap);
    }

    document.getElementById('allocation-lines')?.addEventListener('click', function (e) {
        const btn = e.target.closest('.remove-line-btn');
        if (btn) {
            btn.closest('tr').remove();
            updateRecap();
        }
    });

    updateRecap();
});
