(function () {
  "use strict";

  function numericValue(input) {
    if (!input) return 0;
    if (window.ErpNumeric && typeof window.ErpNumeric.get === "function") {
      var value = window.ErpNumeric.get(input);
      return Number(value || 0);
    }
    return Number(String(input.value || "0").replace(/,/g, "")) || 0;
  }

  function setNumeric(input, value) {
    if (!input) return;
    if (window.ErpNumeric && typeof window.ErpNumeric.set === "function") {
      window.ErpNumeric.set(input, value);
    } else {
      input.value = value;
    }
  }

  function rows() {
    return Array.from(document.querySelectorAll("#dma-lines-body .dma-line-row"));
  }

  function reindex() {
    rows().forEach(function (row, index) {
      row.querySelectorAll("[name]").forEach(function (input) {
        input.name = input.name.replace(/lines\[\d+\]/, "lines[" + index + "]");
      });
    });
    recalc();
  }

  function recalc() {
    var applied = rows().reduce(function (sum, row) {
      return sum + numericValue(row.querySelector(".dma-applied-input"));
    }, 0);
    var remaining = Number(document.querySelector("#debitMemoDisplay")?.dataset.remaining || 0);
    document.getElementById("dma-recap-applied").textContent = applied.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    document.getElementById("dma-recap-remaining").textContent = Math.max(remaining - applied, 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    document.getElementById("dma-recap-lines").textContent = String(rows().length);
  }

  function addVendorBill(row) {
    var id = String(row.dataset.id || "");
    if (!id) return;
    if (document.querySelector('.dma-line-row[data-vendor-bill-id="' + id + '"]')) return;
    var index = rows().length;
    var tr = document.createElement("tr");
    tr.className = "dma-line-row";
    tr.dataset.vendorBillId = id;
    tr.innerHTML =
      '<td><input type="hidden" name="lines[' + index + '].vendorBillId" value="' + id + '">' +
      '<input type="hidden" name="lines[' + index + '].vendorBillCode" value="' + (row.dataset.code || "") + '">' +
      '<span class="dma-line-code"></span></td>' +
      '<td class="text-end"><span class="dma-line-outstanding"></span></td>' +
      '<td><input type="text" class="form-control form-control-sm text-end dma-applied-input erp-number-decimal" name="lines[' + index + '].appliedGrossOriginal"></td>' +
      '<td class="text-center"><button type="button" class="btn btn-sm btn-ghost-danger js-dma-remove-line"><i class="ti ti-trash"></i></button></td>';
    tr.querySelector(".dma-line-code").textContent = row.dataset.code || id;
    tr.querySelector(".dma-line-outstanding").textContent = Number(row.dataset.outstanding || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    document.getElementById("dma-lines-body").appendChild(tr);
    if (typeof window.initNumericInputs === "function") {
      window.initNumericInputs(tr);
    }
    setNumeric(tr.querySelector(".dma-applied-input"), row.dataset.outstanding || "0");
    reindex();
  }

  document.addEventListener("click", function (event) {
    var remove = event.target.closest(".js-dma-remove-line");
    if (remove) {
      remove.closest(".dma-line-row")?.remove();
      reindex();
      return;
    }

    var debitMemo = event.target.closest(".js-dma-debit-memo-option");
    if (debitMemo) {
      document.getElementById("debitMemoId").value = debitMemo.dataset.id || "";
      document.getElementById("debitMemoCode").value = debitMemo.dataset.code || "";
      var display = document.getElementById("debitMemoDisplay");
      display.value = debitMemo.dataset.code || "";
      display.dataset.remaining = debitMemo.dataset.remaining || "0";
      window.ERP?.ModalSelector?.close("debit-memo-selector-modal");
      recalc();
      return;
    }

    var vendorBill = event.target.closest(".js-dma-vendor-bill-option");
    if (vendorBill) {
      addVendorBill(vendorBill);
      window.ERP?.ModalSelector?.close("vendor-bill-selector-modal");
    }
  });

  document.addEventListener("input", function (event) {
    if (event.target.matches(".dma-applied-input")) recalc();
  });

  document.addEventListener("DOMContentLoaded", function () {
    var selectDebitMemo = document.getElementById("btn-select-debit-memo");
    var selectVendorBill = document.getElementById("btn-select-vendor-bill");
    if (selectDebitMemo && window.ERP && window.ERP.ModalSelector) {
      selectDebitMemo.addEventListener("click", function () {
        var vendorBillId = rows()[0]?.dataset.vendorBillId;
        if (!vendorBillId) return;
        window.ERP.ModalSelector.open({
          modalId: "debit-memo-selector-modal",
          resultsId: "debit-memo-selector-body",
          url: "/accounts-payable/debit-memo-allocations/selectors/debit-memos?vendorBillId=" + encodeURIComponent(vendorBillId) +
            "&_=" + Date.now()
        });
      });
    }
    if (selectVendorBill && window.ERP && window.ERP.ModalSelector) {
      selectVendorBill.addEventListener("click", function () {
        var debitMemoId = document.getElementById("debitMemoId").value;
        if (!debitMemoId) return;
        window.ERP.ModalSelector.open({
          modalId: "vendor-bill-selector-modal",
          resultsId: "vendor-bill-selector-body",
          url: "/accounts-payable/debit-memo-allocations/selectors/vendor-bills?debitMemoId=" + encodeURIComponent(debitMemoId) +
            "&_=" + Date.now()
        });
      });
    }
    document.getElementById("debit-memo-allocation-form")?.addEventListener("submit", function (event) {
      if (!document.getElementById("debitMemoId").value || rows().length === 0) {
        event.preventDefault();
        event.stopPropagation();
      }
    }, true);
    if (typeof window.initNumericInputs === "function") {
      window.initNumericInputs(document.getElementById("dma-lines-body"));
    }
    recalc();
  });
})();
