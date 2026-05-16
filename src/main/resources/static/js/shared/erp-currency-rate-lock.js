(function () {
    "use strict";

    window.ERP = window.ERP || {};

    window.ERP.CurrencyRateLock = {
        init: function (options) {
            var currencySelectId = options.currencySelectId;
            var rateInputSelector = options.rateInputSelector;
            var defaultRate = options.defaultRate || "1";

            var selectEl = document.getElementById(currencySelectId);
            var rateInput = document.querySelector(rateInputSelector);
            if (!selectEl || !rateInput) return;

            function lockRate() {
                rateInput.setAttribute("readonly", "readonly");
                rateInput.readOnly = true;
                rateInput.classList.add("bg-body-tertiary");
                if (window.ErpNumeric) {
                    ErpNumeric.set(rateInput, defaultRate);
                } else {
                    rateInput.value = defaultRate;
                }
            }

            function unlockRate() {
                rateInput.removeAttribute("readonly");
                rateInput.readOnly = false;
                rateInput.classList.remove("bg-body-tertiary");
            }

            function checkCurrency(ts) {
                var value = ts.getValue();
                var domSelectedOption = selectEl.querySelector('option[value="' + value + '"]');
                var item = value ? ts.options[value] : null;
                var payload = item && item.payload ? item.payload : {};
                if (!value) {
                    unlockRate();
                    return;
                }
                var isDefault = payload.isDefault === true
                    || payload.isDefault === "true"
                    || (domSelectedOption && domSelectedOption.getAttribute("data-payload-is-default") === "true");
                if (isDefault) {
                    lockRate();
                } else {
                    unlockRate();
                }
            }

            function bind(ts) {
                checkCurrency(ts);
                ts.on("change", function () {
                    checkCurrency(ts);
                });
            }

            if (selectEl.tomselect) {
                bind(selectEl.tomselect);
            } else {
                selectEl.addEventListener("erp:lookup-initialized", function handler(evt) {
                    if (evt.target !== selectEl) return;
                    selectEl.removeEventListener("erp:lookup-initialized", handler);
                    bind(selectEl.tomselect);
                });
            }
        }
    };
})();
