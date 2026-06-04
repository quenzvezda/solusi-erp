(function () {
    'use strict';

    function initTargetContainerLookups() {
        document.querySelectorAll('[data-target-container-select]').forEach(function (select) {
            if (select.tomselect || typeof window.initLookup !== 'function') {
                return;
            }
            window.initLookup(select, select.getAttribute('data-lookup-path'));
            select.classList.add('tomselect-initialized');
        });
    }

    function updateSummary() {
        var lineCount = document.querySelectorAll('#gi-cancel-lines tbody tr').length;
        var targetCount = Array.from(document.querySelectorAll('[data-target-container-select]'))
            .filter(function (select) {
                return select.tomselect ? Boolean(select.tomselect.getValue()) : Boolean(select.value);
            })
            .length;
        var lineCountEl = document.getElementById('gi-cancel-line-count');
        var targetCountEl = document.getElementById('gi-cancel-target-count');
        if (lineCountEl) lineCountEl.textContent = String(lineCount);
        if (targetCountEl) targetCountEl.textContent = String(targetCount);
    }

    function validateBeforeSubmit(event) {
        var missingTarget = Array.from(document.querySelectorAll('[data-target-container-select]'))
            .some(function (select) {
                return select.tomselect ? !select.tomselect.getValue() : !select.value;
            });
        if (!missingTarget) {
            return;
        }
        event.preventDefault();
        event.stopImmediatePropagation();
        if (window.ErpModal && typeof window.ErpModal.showWarning === 'function') {
            window.ErpModal.showWarning(window.ErpI18n?.giCancelTargetRequired || 'Target container is required.');
        }
    }

    document.addEventListener('DOMContentLoaded', function () {
        var form = document.getElementById('gi-cancel-form');
        initTargetContainerLookups();
        updateSummary();
        document.querySelectorAll('[data-target-container-select]').forEach(function (select) {
            select.addEventListener('change', updateSummary);
            select.addEventListener('erp:lookup-initialized', updateSummary);
        });
        if (form) {
            form.addEventListener('submit', validateBeforeSubmit, true);
        }
    });
})();
