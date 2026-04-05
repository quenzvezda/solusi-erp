/**
 * ERP Global Date/Time Picker Helper (Flatpickr)
 * Auto-initializes date, time, and datetime-local inputs.
 */
const ErpDateTimePicker = (function() {
    const SELECTOR = 'input[type="date"], input[type="time"], input[type="datetime-local"], input[data-picker]';
    let observerStarted = false;

    function resolveLocale() {
        const htmlLang = (document.documentElement.getAttribute('lang') || '').toLowerCase();
        if (htmlLang.startsWith('id')) return 'id';

        const langCookie = document.cookie
            .split(';')
            .map(item => item.trim())
            .find(item => item.startsWith('lang='));
        if (langCookie) {
            const langValue = decodeURIComponent(langCookie.split('=')[1] || '').toLowerCase();
            if (langValue.startsWith('id')) return 'id';
        }
        return 'default';
    }

    function detectMode(input) {
        const explicitMode = (input.getAttribute('data-picker') || '').toLowerCase();
        if (explicitMode) return explicitMode;
        return (input.getAttribute('type') || '').toLowerCase();
    }

    function buildOptions(input) {
        const mode = detectMode(input);
        const options = {
            allowInput: true,
            disableMobile: true,
            locale: resolveLocale()
        };

        if (mode === 'time') {
            options.enableTime = true;
            options.noCalendar = true;
            options.time_24hr = true;
            options.dateFormat = 'H:i';
        } else if (mode === 'datetime' || mode === 'datetime-local') {
            options.enableTime = true;
            options.time_24hr = true;
            options.dateFormat = 'Y-m-d\\TH:i';
        } else {
            options.dateFormat = 'Y-m-d';
        }

        const stepSeconds = Number(input.getAttribute('step'));
        if (Number.isFinite(stepSeconds) && stepSeconds >= 60) {
            options.minuteIncrement = Math.max(1, Math.round(stepSeconds / 60));
        } else if (mode === 'time' || mode === 'datetime' || mode === 'datetime-local') {
            options.minuteIncrement = 5;
        }

        const minValue = input.getAttribute('min');
        const maxValue = input.getAttribute('max');
        if (minValue) options.minDate = minValue;
        if (maxValue) options.maxDate = maxValue;

        return options;
    }

    function initElement(input) {
        if (!input || input.disabled || input.readOnly) return;
        if (input._flatpickr || input.dataset.pickerInitialized === 'true') return;
        if (typeof window.flatpickr !== 'function') return;

        window.flatpickr(input, buildOptions(input));
        input.dataset.pickerInitialized = 'true';
    }

    function init(context = document) {
        if (context instanceof HTMLInputElement) {
            initElement(context);
        }

        if (context && typeof context.querySelectorAll === 'function') {
            context.querySelectorAll(SELECTOR).forEach(initElement);
        }

        startObserver();
    }

    function startObserver() {
        if (observerStarted || typeof MutationObserver === 'undefined' || !document.body) return;
        observerStarted = true;

        const observer = new MutationObserver(mutations => {
            mutations.forEach(mutation => {
                mutation.addedNodes.forEach(node => {
                    if (!(node instanceof HTMLElement)) return;
                    if (typeof node.matches === 'function' && node.matches(SELECTOR)) {
                        initElement(node);
                    }
                    if (typeof node.querySelectorAll === 'function') {
                        node.querySelectorAll(SELECTOR).forEach(initElement);
                    }
                });
            });
        });

        observer.observe(document.body, { childList: true, subtree: true });
    }

    return { init };
})();

window.ErpDateTimePicker = ErpDateTimePicker;
