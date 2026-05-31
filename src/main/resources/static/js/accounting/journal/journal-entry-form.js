(function () {
  function numericValue(input) {
    if (!input) return 0;
    if (window.ErpNumeric && ErpNumeric.get) {
      var value = ErpNumeric.get(input);
      return Number(value || 0);
    }
    return Number(String(input.value || "0").replace(/,/g, "")) || 0;
  }

  function rows() {
    return Array.from(document.querySelectorAll("#journal-lines-table tbody tr"));
  }

  function reindexRows() {
    rows().forEach(function (row, index) {
      row.querySelectorAll("[name]").forEach(function (input) {
        input.name = input.name.replace(/lines\[\d+]/, "lines[" + index + "]");
      });
    });
  }

  function calculateTotals() {
    var debit = 0;
    var credit = 0;
    rows().forEach(function (row) {
      debit += numericValue(row.querySelector('[name$=".debitAmount"]'));
      credit += numericValue(row.querySelector('[name$=".creditAmount"]'));
    });
    var balanced = Math.abs(debit - credit) < 0.0001 && debit > 0 && rows().length >= 2;
    document.dispatchEvent(new CustomEvent("journal:balance", { detail: { debit: debit, credit: credit, balanced: balanced } }));
    return balanced;
  }

  function addLine() {
    var tbody = document.querySelector("#journal-lines-table tbody");
    var first = tbody && tbody.querySelector("tr");
    if (!tbody || !first) return;
    var clone = first.cloneNode(true);
    clone.querySelectorAll("input").forEach(function (input) {
      input.value = "";
    });
    tbody.appendChild(clone);
    reindexRows();
    calculateTotals();
  }

  document.addEventListener("DOMContentLoaded", function () {
    var form = document.getElementById("journal-entry-form");
    var addButton = document.getElementById("btn-add-journal-line");
    if (addButton) addButton.addEventListener("click", addLine);

    document.addEventListener("click", function (event) {
      var removeButton = event.target.closest(".btn-remove-journal-line");
      if (!removeButton) return;
      removeButton.closest("tr").remove();
      reindexRows();
      calculateTotals();
    });

    document.addEventListener("input", function (event) {
      if (event.target.matches('[name$=".debitAmount"], [name$=".creditAmount"], [name="exchangeRate"]')) {
        calculateTotals();
      }
    });

    if (window.ERP && ERP.CurrencyRateLock) {
      ERP.CurrencyRateLock.init({
        currencySelectId: "currencyId",
        rateInputSelector: '[name="exchangeRate"]'
      });
    }

    if (form) {
      form.addEventListener("submit", function (event) {
        if (!calculateTotals()) {
          event.preventDefault();
          event.stopImmediatePropagation();
          if (window.ErpModal) {
            ErpModal.showWarning("Journal must have at least two balanced lines.");
          }
        }
      }, true);
    }

    calculateTotals();
  });
})();
