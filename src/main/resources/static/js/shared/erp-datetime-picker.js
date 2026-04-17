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

    function isInsideTemplateSource(input) {
        return typeof input.closest === 'function' && input.closest('#row-template-source') !== null;
    }

    function parseExistingValue(raw) {
        if (!raw) return null;
        const d = new Date(raw);
        return isNaN(d.getTime()) ? null : d;
    }

    // rawValue is passed explicitly so buildOptions can use it regardless of
    // whether input.value has already been cleared.
    function buildOptions(input, rawValue) {
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
            options.altInput = true;
            options.altFormat = 'H:i';
        } else if (mode === 'datetime' || mode === 'datetime-local') {
            options.enableTime = true;
            options.time_24hr = true;
            options.dateFormat = 'Y-m-d\\TH:i';
            options.altInput = true;
            options.altFormat = 'd M Y H:i';
        } else {
            options.dateFormat = 'Y-m-d';
            options.altInput = true;
            options.altFormat = 'd M Y';
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

        // Pre-fill: parse the captured raw value via native Date to handle
        // ISO strings with seconds (e.g. "2026-04-03T00:00:00") that Flatpickr
        // would reject against dateFormat 'Y-m-d\TH:i'.
        const parsed = parseExistingValue(rawValue);
        if (parsed) {
            options.defaultDate = parsed;
        }

        return options;
    }

    function initElement(input) {
        if (!input || input.disabled || input.readOnly) return;
        if (isInsideTemplateSource(input)) return;
        if (input._flatpickr || input.dataset.pickerInitialized === 'true') return;
        if (typeof window.flatpickr !== 'function') return;

        // Capture value FIRST, then clear so Flatpickr won't attempt to parse
        // it against dateFormat and silently fall back to browser-native.
        const rawValue = input.value;
        input.value = '';

        const instance = window.flatpickr(input, buildOptions(input, rawValue));

        // Safety fallback: if Flatpickr still didn't select a date (e.g. truly
        // empty field), restore original value so a plain form submit is valid.
        if (!instance.selectedDates.length && rawValue) {
            input.value = rawValue;
        }

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
