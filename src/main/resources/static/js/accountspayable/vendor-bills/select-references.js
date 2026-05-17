(function () {
    "use strict";

    var form = document.getElementById("vendor-bill-reference-form");
    var continueButton = document.getElementById("btn-continue-references");
    var message = document.getElementById("reference-validation-message");

    if (!form || !continueButton || !message) {
        return;
    }

    function selectedBoxes() {
        return Array.from(form.querySelectorAll(".js-reference-checkbox:checked"));
    }

    function showMessage(text) {
        message.textContent = text;
        message.classList.remove("d-none");
    }

    function hideMessage() {
        message.textContent = "";
        message.classList.add("d-none");
    }

    function validateSelection() {
        var selected = selectedBoxes();
        continueButton.disabled = true;
        hideMessage();

        if (selected.length === 0) {
            return;
        }

        var vendorId = selected[0].dataset.vendorId;
        var currencyId = selected[0].dataset.currencyId;
        var mixedVendor = selected.some(function (box) { return box.dataset.vendorId !== vendorId; });
        var mixedCurrency = selected.some(function (box) { return box.dataset.currencyId !== currencyId; });

        if (mixedVendor) {
            showMessage("All selected references must have the same vendor.");
            return;
        }
        if (mixedCurrency) {
            showMessage("All selected references must have the same currency.");
            return;
        }

        continueButton.disabled = false;
    }

    form.addEventListener("change", function (event) {
        if (event.target.classList.contains("js-reference-checkbox")) {
            validateSelection();
        }
    });

    form.addEventListener("submit", function (event) {
        validateSelection();
        if (continueButton.disabled) {
            event.preventDefault();
        }
    });
})();
