/**
 * ERP Standard Form Handler for AJAX Submissions.
 * Automatically initializes any form with data-ajax-form="true".
 * Uses global ErpI18n for localized user messages.
 */
const ErpFormHandler = (function () {
    
    // Fallback translations if ErpI18n is not loaded
    const i18n = window.ErpI18n || {
        errorTitle: 'An Error Occurred',
        serverError: 'An unexpected server error occurred.',
        networkError: 'Connection error. Please check your network.',
        successGeneric: 'Action completed successfully.'
    };

    const init = function () {
        document.querySelectorAll('form[data-ajax-form="true"]:not(.ajax-initialized)').forEach(form => {
            form.addEventListener('submit', handleFormSubmit);
            form.classList.add('ajax-initialized');
            console.log(`[ERP-FORM] Initialized AJAX form: ${form.id || 'unnamed'}`);
        });
    };

    const handleFormSubmit = async function (event) {
        event.preventDefault();
        const form = event.target;
        const submitBtn = form.querySelector('[type="submit"]');
        const indicator = form.querySelector('.spinner-border') || document.getElementById('loading-indicator');
        const redirectUrl = form.dataset.redirectOnSuccess;

        clearErrors(form);
        if (indicator) indicator.style.display = 'inline-block';
        if (submitBtn) submitBtn.disabled = true;

        try {
            const data = {};
            
            // Iterate through all form elements to get clean data
            const elements = form.querySelectorAll('input, select, textarea');
            elements.forEach(el => {
                if (!el.name || el.disabled || el.type === 'file' || el.name === '_csrf') return;

                let value = el.value;

                if (el.type === 'checkbox') {
                    // Specific checkbox handling for Spring
                    if (!el.name.startsWith('_')) {
                        data[el.name] = el.checked;
                    }
                } else if (el.classList.contains('erp-number-decimal') || el.classList.contains('erp-number-integer')) {
                    // UNFORMAT NUMERIC DATA
                    if (typeof AutoNumeric !== 'undefined' && AutoNumeric.getAutoNumericElement(el)) {
                        data[el.name] = AutoNumeric.getAutoNumericElement(el).getNumber();
                    } else {
                        data[el.name] = value === "" ? null : value.replace(/,/g, '');
                    }
                } else {
                    // CONVERT EMPTY STRING TO NULL: Crucial for Jackson Enum/Long parsing
                    data[el.name] = value === "" ? null : value;
                }
            });

            const csrfToken = document.querySelector('input[name="_csrf"]')?.value;

            const response = await fetch(form.action, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': csrfToken
                },
                body: JSON.stringify(data)
            });

            // Robust JSON parsing
            let result = {};
            const contentType = response.headers.get("content-type");
            if (contentType && contentType.indexOf("application/json") !== -1) {
                result = await response.json();
            } else {
                const text = await response.text();
                throw new Error(text || i18n.serverError);
            }

            if (response.ok && result.success) {
                if (redirectUrl) {
                    setTimeout(() => {
                        window.location.href = redirectUrl;
                    }, 300);
                } else {
                    alert(result.message || i18n.successGeneric);
                }
            } else if (response.status === 400 && result.validationErrors) {
                displayFieldErrors(form, result.validationErrors);
            } else {
                displayGlobalError(form, result.message || i18n.serverError);
            }
        } catch (error) {
            console.error('[ERP-FORM] Submission failed:', error);
            const errorMsg = error.message && error.message.length < 200 ? error.message : i18n.networkError;
            displayGlobalError(form, errorMsg);
        } finally {
            if (indicator) indicator.style.display = 'none';
            if (submitBtn) submitBtn.disabled = false;
        }
    };

    const clearErrors = function (form) {
        form.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));
        form.querySelectorAll('.is-invalid-ts').forEach(el => el.classList.remove('is-invalid-ts'));
        form.querySelectorAll('.invalid-feedback-ajax').forEach(el => el.remove());
        const existingAlert = form.querySelector('.alert-ajax-global');
        if (existingAlert) existingAlert.remove();
    };

    const displayFieldErrors = function (form, errors) {
        for (const field in errors) {
            const input = form.querySelector(`[name="${field}"]`);
            if (input) {
                input.classList.add('is-invalid');
                const errorDiv = document.createElement('div');
                errorDiv.className = 'invalid-feedback invalid-feedback-ajax d-block';
                errorDiv.textContent = errors[field];

                if (input.tomselect && input.tomselect.wrapper) {
                    input.tomselect.wrapper.classList.add('is-invalid-ts');
                    input.tomselect.wrapper.parentNode.appendChild(errorDiv);
                } else {
                    input.parentNode.appendChild(errorDiv);
                }
            }
        }
    };

    const displayGlobalError = function (form, message) {
        const alertHtml = `
            <div class="alert alert-danger alert-dismissible alert-ajax-global" role="alert">
                <div class="d-flex">
                    <div><i class="ti ti-alert-circle icon alert-icon"></i></div>
                    <div>
                        <h4 class="alert-title">${i18n.errorTitle}</h4>
                        <div class="text-secondary">${message}</div>
                    </div>
                </div>
                <a class="btn-close" data-bs-dismiss="alert" aria-label="close"></a>
            </div>`;
        
        let container = form.querySelector('.alert-container') || form.querySelector('.card-body');
        if (container) {
            container.insertAdjacentHTML('afterbegin', alertHtml);
            container.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
    };

    return {
        init: init
    };
})();

document.addEventListener('DOMContentLoaded', ErpFormHandler.init);
document.body.addEventListener('htmx:afterSwap', ErpFormHandler.init);
