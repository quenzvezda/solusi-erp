(function () {
    "use strict";

    var form = document.getElementById("vendor-bill-form");
    var lineBody = document.getElementById("vendor-bill-lines-body");
    var selectedGrIds = document.getElementById("selected-gr-ids");

    if (!form || !lineBody || !selectedGrIds) {
        return;
    }

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

    function updateEmptyRow() {
        var emptyRow = document.getElementById("vendor-bill-lines-empty");
        if (emptyRow) {
            emptyRow.style.display = lineBody.querySelector(".vendor-bill-line-row") ? "none" : "";
        }
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
            '<td><input type="text" class="form-control form-control-sm text-end" name="lines[' + index + '].qtyBilled" value="' + escapeHtml(qty) + '"></td>' +
            '<td>' +
                '<input type="hidden" name="lines[' + index + '].uomId" value="' + escapeHtml(line.uomId) + '">' +
                '<input type="hidden" name="lines[' + index + '].uomName" value="' + uomName + '">' +
                '<span>' + uomName + '</span>' +
            '</td>' +
            '<td class="text-end">' + escapeHtml(unitPrice) + '</td>' +
            '<td class="text-end">' + escapeHtml(taxAmount) + '</td>' +
            '<td class="text-end">' +
                '<button type="button" class="btn btn-white btn-icon text-danger js-vb-line-remove">' +
                    '<i class="ti ti-trash"></i>' +
                '</button>' +
            '</td>';
        lineBody.appendChild(row);
        updateEmptyRow();
        return true;
    }

    document.addEventListener("click", function (event) {
        var removeButton = event.target.closest(".js-vb-line-remove");
        if (removeButton) {
            removeButton.closest(".vendor-bill-line-row").remove();
            updateEmptyRow();
        }
    });
})();
