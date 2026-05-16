(function () {
    "use strict";

    var form = document.getElementById("vendor-payment-form");
    if (!form) return;

    var vendorSelect = document.getElementById("vp-vendor");
    var currencySelect = document.getElementById("vp-currency");
    var bankAccountIdInput = document.getElementById("bankAccountId");
    var bankAccountDisplay = document.getElementById("bankAccountDisplay");
    var btnSelectBank = document.getElementById("btn-select-bank-account");
    var btnClearBank = document.getElementById("btn-clear-bank-account");
    var allocationLines = document.getElementById("allocation-lines");
    var recapPaymentAmount = document.getElementById("recap-payment-amount");
    var recapApplied = document.getElementById("recap-applied");
    var recapUnapplied = document.getElementById("recap-unapplied");

    var MODAL_ID = "bank-account-modal";
    var RESULTS_ID = "bank-account-selector-body";

    function getPaymentAmountInput() {
        return form.querySelector('[name="paymentAmount"]');
    }

    function formatNumber(val) {
        return val.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    }

    function getVendorId() {
        if (!vendorSelect) return null;
        return vendorSelect.value || null;
    }

    function getCurrencyId() {
        if (!currencySelect) return null;
        return currencySelect.value || null;
    }

    // --- Recap Calculation ---
    function updateRecap() {
        var paymentInput = getPaymentAmountInput();
        var paymentAmount = paymentInput && window.ErpNumeric ? ErpNumeric.get(paymentInput) : 0;

        var paidInputs = allocationLines.querySelectorAll(".paid-amount-input");
        var totalApplied = 0;
        paidInputs.forEach(function (input) {
            totalApplied += window.ErpNumeric ? ErpNumeric.get(input) : 0;
        });

        var unapplied = paymentAmount - totalApplied;

        if (recapPaymentAmount) recapPaymentAmount.textContent = formatNumber(paymentAmount);
        if (recapApplied) recapApplied.textContent = formatNumber(totalApplied);
        if (recapUnapplied) {
            recapUnapplied.textContent = formatNumber(unapplied);
            recapUnapplied.classList.toggle("text-danger", Math.abs(unapplied) > 0.001);
            recapUnapplied.classList.toggle("text-success", Math.abs(unapplied) <= 0.001);
        }
    }

    // --- Allocation Lines ---
    function clearAllocationLines() {
        if (allocationLines) allocationLines.innerHTML = "";
        updateRecap();
    }

    function renderAllocationLines(bills) {
        if (!allocationLines) return;
        allocationLines.innerHTML = "";

        bills.forEach(function (bill, index) {
            var tr = document.createElement("tr");
            tr.setAttribute("data-index", index);
            tr.innerHTML =
                '<td>' +
                    '<input type="hidden" name="lines[' + index + '].vendorBillId" value="' + bill.vendorBillId + '">' +
                    '<span>' + escapeHtml(bill.billCode) + '</span>' +
                    '<input type="hidden" name="lines[' + index + '].billCode" value="' + escapeHtml(bill.billCode) + '">' +
                '</td>' +
                '<td class="text-end">' +
                    '<span class="outstanding-display">' + formatNumber(bill.outstandingAmount) + '</span>' +
                    '<input type="hidden" name="lines[' + index + '].outstandingAmount" value="' + bill.outstandingAmount + '">' +
                '</td>' +
                '<td class="text-end">' +
                    '<input type="text" class="form-control form-control-sm text-end paid-amount-input erp-num-decimal"' +
                    ' name="lines[' + index + '].paidAmount" value="0" data-autonumeric="currency">' +
                '</td>' +
                '<td class="text-center">' +
                    '<button type="button" class="btn btn-sm btn-ghost-danger remove-line-btn">' +
                        '<i class="ti ti-trash"></i>' +
                    '</button>' +
                '</td>';
            allocationLines.appendChild(tr);
        });

        if (window.initNumericInputs) initNumericInputs();
        updateRecap();
    }

    function escapeHtml(str) {
        if (!str) return "";
        return String(str).replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;").replace(/'/g, "&#039;");
    }

    function loadPayableBills() {
        var vendorId = getVendorId();
        var currencyId = getCurrencyId();
        if (!vendorId || !currencyId) {
            clearAllocationLines();
            return;
        }

        fetch("/accounts-payable/vendor-payments/payable-bills?vendorId=" + vendorId + "&currencyId=" + currencyId, {
            headers: { "Accept": "application/json" }
        })
        .then(function (res) { return res.json(); })
        .then(function (response) {
            if (response.data && Array.isArray(response.data)) {
                renderAllocationLines(response.data);
            } else {
                clearAllocationLines();
            }
        })
        .catch(function () {
            clearAllocationLines();
        });
    }

    // --- Autocomplete Cascading ---
    function waitForLookupReady(selectEl, callback) {
        if (!selectEl) return;
        if (selectEl.tomselect) {
            callback(selectEl.tomselect);
            return;
        }
        var handler = function (evt) {
            if (evt.target !== selectEl) return;
            selectEl.removeEventListener("erp:lookup-initialized", handler);
            callback(selectEl.tomselect);
        };
        selectEl.addEventListener("erp:lookup-initialized", handler);
    }

    waitForLookupReady(vendorSelect, function (ts) {
        ts.on("change", function () {
            clearAllocationLines();
            clearBankAccount();
            loadPayableBills();
        });
    });

    waitForLookupReady(currencySelect, function (ts) {
        ts.on("change", function () {
            clearAllocationLines();
            clearBankAccount();
            loadPayableBills();
        });
    });

    // --- Bank Account Modal Selector ---
    function clearBankAccount() {
        if (bankAccountIdInput) bankAccountIdInput.value = "";
        if (bankAccountDisplay) bankAccountDisplay.value = "";
    }

    if (btnSelectBank) {
        btnSelectBank.addEventListener("click", function () {
            var currencyId = getCurrencyId();
            if (!currencyId) {
                if (window.ErpModal) ErpModal.showWarning("Please select a currency first.");
                return;
            }
            ERP.ModalSelector.open({
                modalId: MODAL_ID,
                resultsId: RESULTS_ID,
                url: "/accounts-payable/vendor-payments/selectors/bank-accounts?currencyId=" + currencyId
            });
        });
    }

    if (btnClearBank) {
        btnClearBank.addEventListener("click", function () {
            clearBankAccount();
        });
    }

    document.addEventListener("click", function (e) {
        var btn = e.target.closest(".js-bank-account-select");
        if (!btn) return;

        var id = btn.getAttribute("data-id");
        var name = btn.getAttribute("data-name");

        if (bankAccountIdInput) bankAccountIdInput.value = id;
        if (bankAccountDisplay) bankAccountDisplay.value = name;

        ERP.ModalSelector.close(MODAL_ID);
    });

    // --- Line Removal ---
    if (allocationLines) {
        allocationLines.addEventListener("click", function (e) {
            var btn = e.target.closest(".remove-line-btn");
            if (!btn) return;
            btn.closest("tr").remove();
            reindexLines();
            updateRecap();
        });

        allocationLines.addEventListener("input", function (e) {
            if (e.target.classList.contains("paid-amount-input")) {
                updateRecap();
            }
        });
    }

    function reindexLines() {
        var rows = allocationLines.querySelectorAll("tr");
        rows.forEach(function (row, idx) {
            row.setAttribute("data-index", idx);
            row.querySelectorAll("[name]").forEach(function (input) {
                input.name = input.name.replace(/lines\[\d+\]/, "lines[" + idx + "]");
            });
        });
    }

    // --- Payment Amount Change ---
    var paymentAmountInput = getPaymentAmountInput();
    if (paymentAmountInput) {
        paymentAmountInput.addEventListener("input", updateRecap);
        paymentAmountInput.addEventListener("change", updateRecap);
    }

    // --- Form Validation (capture phase) ---
    form.addEventListener("submit", function (e) {
        var paidInputs = allocationLines.querySelectorAll(".paid-amount-input");
        var hasLine = false;
        var totalApplied = 0;
        paidInputs.forEach(function (input) {
            var val = window.ErpNumeric ? ErpNumeric.get(input) : 0;
            if (val > 0) hasLine = true;
            totalApplied += val;
        });

        if (!hasLine) {
            e.preventDefault();
            e.stopImmediatePropagation();
            if (window.ErpModal) ErpModal.showWarning("At least one bill must have a paid amount greater than 0.");
            return;
        }

        var paymentInput = getPaymentAmountInput();
        var paymentAmount = paymentInput && window.ErpNumeric ? ErpNumeric.get(paymentInput) : 0;
        var unapplied = Math.abs(paymentAmount - totalApplied);

        if (unapplied > 0.001) {
            e.preventDefault();
            e.stopImmediatePropagation();
            if (window.ErpModal) ErpModal.showWarning("Payment amount must equal the total applied amount (unapplied must be 0).");
            return;
        }
    }, true);

    // --- Initial Recap ---
    updateRecap();

})();
