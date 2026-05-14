(function () {
    "use strict";

    var form = document.getElementById("vendor-bill-form");
    var lineBody = document.getElementById("vendor-bill-lines-body");
    var selectedGrIds = document.getElementById("selected-gr-ids");

    if (!form || !lineBody || !selectedGrIds) {
        return;
    }

    var exchangeRateInput = form.querySelector('[name="exchangeRate"]');
    var recapDpp = document.getElementById("vb-recap-dpp");
    var recapTax = document.getElementById("vb-recap-tax");
    var recapTotal = document.getElementById("vb-recap-total");
    var recapTotalBase = document.getElementById("vb-recap-total-base");
    var lineRequiredMessage = form.dataset.lineRequiredMessage || "Add at least one vendor bill line.";
    var nextLineIndex = lineBody.querySelectorAll(".vendor-bill-line-row").length;
    var selectedGrIdSet = new Set(Array.from(selectedGrIds.querySelectorAll("input"))
        .map(function (input) { return input.value; })
        .filter(Boolean));

    function escapeHtml(value) {
        return String(value == null ? "" : value)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }

    function parseDecimal(value) {
        if (value == null) {
            return 0;
        }
        var normalized = String(value).replace(/,/g, "").trim();
        var parsed = Number(normalized);
        return Number.isFinite(parsed) ? parsed : 0;
    }

    function formatDecimal(value) {
        return value.toLocaleString(undefined, {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        });
    }

    function getNumericValue(input) {
        if (window.ErpNumeric && input) {
            return ErpNumeric.get(input);
        }
        return parseDecimal(input ? input.value : 0);
    }

    function setNumericValue(input, value) {
        if (window.ErpNumeric && input) {
            ErpNumeric.set(input, value);
        } else if (input) {
            input.value = formatDecimal(value);
        }
    }

    function calculateLineAmounts(row) {
        var qtyInput = row.querySelector('[name$=".qtyBilled"]');
        var qtyBilled = getNumericValue(qtyInput);
        var quantityReceived = parseDecimal(row.dataset.quantityReceived || qtyBilled);
        var inventoryAmount = parseDecimal(row.dataset.inventoryAmount);
        var taxAmount = parseDecimal(row.dataset.taxAmount);
        var ratio = quantityReceived === 0 ? 0 : qtyBilled / quantityReceived;

        return {
            dpp: inventoryAmount * ratio,
            tax: taxAmount * ratio
        };
    }

    function updateRecap() {
        var dpp = 0;
        var tax = 0;
        Array.from(lineBody.querySelectorAll(".vendor-bill-line-row")).forEach(function (row) {
            var amounts = calculateLineAmounts(row);
            dpp += amounts.dpp;
            tax += amounts.tax;
            var dppCell = row.querySelector(".js-vb-line-dpp");
            var taxCell = row.querySelector(".js-vb-line-tax");
            if (dppCell) {
                dppCell.textContent = formatDecimal(amounts.dpp);
            }
            if (taxCell) {
                taxCell.textContent = formatDecimal(amounts.tax);
            }
        });

        var total = dpp + tax;
        var exchangeRate = parseDecimal(exchangeRateInput ? exchangeRateInput.value : 1);
        if (recapDpp) recapDpp.textContent = formatDecimal(dpp);
        if (recapTax) recapTax.textContent = formatDecimal(tax);
        if (recapTotal) recapTotal.textContent = formatDecimal(total);
        if (recapTotalBase) recapTotalBase.textContent = formatDecimal(total * exchangeRate);
    }

    function updateEmptyRow() {
        var emptyRow = document.getElementById("vendor-bill-lines-empty");
        if (emptyRow) {
            emptyRow.style.display = lineBody.querySelector(".vendor-bill-line-row") ? "none" : "";
        }
        updateRecap();
    }

    function addGrId(grId) {
        if (!grId || selectedGrIdSet.has(String(grId))) {
            return;
        }
        selectedGrIdSet.add(String(grId));
        var input = document.createElement("input");
        input.type = "hidden";
        input.name = "grIds[" + (selectedGrIdSet.size - 1) + "]";
        input.value = grId;
        selectedGrIds.appendChild(input);
    }

    function lineExists(grLineId) {
        return !!lineBody.querySelector('[data-gr-line-id="' + CSS.escape(String(grLineId)) + '"]');
    }

    function appendLine(line) {
        if (!line || lineExists(line.grLineId)) {
            return false;
        }

        var index = nextLineIndex++;
        var productName = escapeHtml(line.productName);
        var uomName = escapeHtml(line.uomName);
        var unitPrice = line.unitPrice == null ? "" : line.unitPrice;
        var taxAmount = line.taxAmount == null ? "" : line.taxAmount;
        var inventoryAmount = line.inventoryAmount == null ? "" : line.inventoryAmount;
        var qty = line.outstandingQty == null ? line.quantityReceived : line.outstandingQty;

        var row = document.createElement("tr");
        row.className = "vendor-bill-line-row";
        row.dataset.grLineId = line.grLineId;
        row.dataset.quantityReceived = qty;
        row.dataset.inventoryAmount = inventoryAmount;
        row.dataset.taxAmount = taxAmount;
        row.innerHTML =
            '<td>' +
                '<input type="hidden" name="lines[' + index + '].grLineId" value="' + escapeHtml(line.grLineId) + '">' +
                '<input type="hidden" name="lines[' + index + '].productId" value="' + escapeHtml(line.productId) + '">' +
                '<input type="hidden" name="lines[' + index + '].productName" value="' + productName + '">' +
                '<input type="hidden" name="lines[' + index + '].description" value="' + productName + '">' +
                '<input type="hidden" name="lines[' + index + '].unitPrice" value="' + escapeHtml(unitPrice) + '">' +
                '<input type="hidden" name="lines[' + index + '].inventoryAmount" value="' + escapeHtml(inventoryAmount) + '">' +
                '<input type="hidden" name="lines[' + index + '].taxAmount" value="' + escapeHtml(taxAmount) + '">' +
                '<span>' + productName + '</span>' +
            '</td>' +
            '<td><input type="text" class="form-control form-control-sm erp-input-sm erp-number-decimal text-end js-vb-qty" name="lines[' + index + '].qtyBilled" value="' + escapeHtml(formatDecimal(qty)) + '"></td>' +
            '<td>' +
                '<input type="hidden" name="lines[' + index + '].uomId" value="' + escapeHtml(line.uomId) + '">' +
                '<input type="hidden" name="lines[' + index + '].uomName" value="' + uomName + '">' +
                '<span>' + uomName + '</span>' +
            '</td>' +
            '<td class="text-end">' + escapeHtml(unitPrice) + '</td>' +
            '<td class="text-end js-vb-line-dpp">0.00</td>' +
            '<td class="text-end js-vb-line-tax">0.00</td>' +
            '<td class="text-end">' +
                '<button type="button" class="btn btn-white btn-icon text-danger js-vb-line-remove">' +
                    '<i class="ti ti-trash"></i>' +
                '</button>' +
            '</td>';
        lineBody.appendChild(row);
        var qtyInput = row.querySelector('[name$=".qtyBilled"]');
        setNumericValue(qtyInput, qty);
        if (typeof window.initNumericInputs === "function") {
            window.initNumericInputs(row);
        }
        updateEmptyRow();
        return true;
    }

    function normalizeQtyInputs() {
        Array.from(lineBody.querySelectorAll('[name$=".qtyBilled"]')).forEach(function (input) {
            setNumericValue(input, getNumericValue(input));
        });
    }

    document.addEventListener("click", function (event) {
        var removeButton = event.target.closest(".js-vb-line-remove");
        if (removeButton) {
            removeButton.closest(".vendor-bill-line-row").remove();
            updateEmptyRow();
        }
    });

    form.addEventListener("input", function (event) {
        if (event.target.matches('[name="exchangeRate"], [name$=".qtyBilled"]')) {
            updateRecap();
        }
    });

    form.addEventListener("submit", function (event) {
        if (!lineBody.querySelector(".vendor-bill-line-row")) {
            event.preventDefault();
            event.stopImmediatePropagation();
            if (window.ErpFormHandler && typeof ErpFormHandler.showWarning === "function") {
                ErpFormHandler.showWarning(lineRequiredMessage);
            } else if (window.ErpModal && typeof ErpModal.showError === "function") {
                ErpModal.showError(lineRequiredMessage);
            }
        }
    }, true);

    normalizeQtyInputs();
    updateEmptyRow();
})();
