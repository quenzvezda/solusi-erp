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
                rateInput.value = defaultRate;
                rateInput.setAttribute("readonly", "readonly");
                if (window.AutoNumeric) {
                    var anInstance = AutoNumeric.getAutoNumericElement(rateInput);
                    if (anInstance) {
                        anInstance.set(parseFloat(defaultRate));
                        anInstance.options.readOnly(true);
                    }
                }
            }

            function unlockRate() {
                rateInput.removeAttribute("readonly");
                if (window.AutoNumeric) {
                    var anInstance = AutoNumeric.getAutoNumericElement(rateInput);
                    if (anInstance) {
                        anInstance.options.readOnly(false);
                    }
                }
            }

            function checkCurrency(ts) {
                var value = ts.getValue();
                if (!value) {
                    unlockRate();
                    return;
                }
                var item = ts.options[value];
                var payload = item && item.payload ? item.payload : {};
                if (payload.isDefault === true) {
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
