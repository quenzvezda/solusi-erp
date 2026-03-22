/**
 * ERP Standard Form Handler for AJAX Submissions.
 * Automatically initializes any form with data-ajax-form="true".
 * Uses sessionStorage to persist success messages across manual redirects.
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

        // CHECK FOR PENDING SUCCESS MESSAGE FROM PREVIOUS PAGE (REDIRECT)
        checkPendingSuccess();
    };

    /**
     * Display a success message manually. Useful for external triggers (like HTMX events).
     */
    const showSuccess = function(message) {
        // Find appropriate container for alerts (Same logic as checkPendingSuccess)
        let alertContainer = document.querySelector('.alert-container') || 
                             document.querySelector('.page-body .page-body .container-xl') ||
                             document.querySelector('.page-body .container-xl');
        
        if (alertContainer) {
            // Remove existing ajax alerts to prevent stacking
            const existingAlerts = alertContainer.querySelectorAll('.alert-ajax-global');
            existingAlerts.forEach(el => el.remove());

            const alertHtml = `
                <div class="alert alert-success alert-dismissible fade show alert-ajax-global" role="alert">
                    <div class="d-flex">
                        <div><i class="ti ti-check icon alert-icon"></i></div>
                        <div>${message}</div>
                    </div>
                    <a class="btn-close" data-bs-dismiss="alert" aria-label="close"></a>
                </div>`;
            
            alertContainer.insertAdjacentHTML('afterbegin', alertHtml);
            alertContainer.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
    }

    const checkPendingSuccess = function() {
        const pendingMsg = sessionStorage.getItem('erp_pending_success');
        if (pendingMsg) {
            console.log('[ERP-FORM] Found pending success message, displaying...');
            showSuccess(pendingMsg);
            // Clear so it doesn't show again on F5
            sessionStorage.removeItem('erp_pending_success');
        }
    }

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
            const elements = form.querySelectorAll('input, select, textarea');
            elements.forEach(el => {
                if (!el.name || el.disabled || el.type === 'file' || el.name === '_csrf') return;
                let value = el.value;
                if (el.type === 'checkbox') {
                    if (!el.name.startsWith('_')) data[el.name] = el.checked;
                } else if (el.classList.contains('erp-number-decimal') || el.classList.contains('erp-number-integer')) {
                    if (typeof AutoNumeric !== 'undefined' && AutoNumeric.getAutoNumericElement(el)) {
                        data[el.name] = (el.value === "") ? null : AutoNumeric.getAutoNumericElement(el).getNumber();
                    } else {
                        data[el.name] = value === "" ? null : value.replace(/,/g, '');
                    }
                } else {
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
                    sessionStorage.setItem('erp_pending_success', result.message || i18n.successGeneric);
                    setTimeout(() => {
                        window.location.href = redirectUrl;
                    }, 100);
                } else {
                    showSuccess(result.message || i18n.successGeneric);
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
        let unmappedErrors = [];
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
            } else {
                unmappedErrors.push(errors[field]);
            }
        }
        if (unmappedErrors.length > 0) {
            displayGlobalError(form, unmappedErrors.join('<br>'));
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
        init: init,
        showSuccess: showSuccess
    };
})();

/**
 * GLOBAL EVENT LISTENERS for HTMX & Shared Actions
 */
document.addEventListener('DOMContentLoaded', function() {
    ErpFormHandler.init();

    // Listen for custom "erp:show-success" events (can be triggered by server via HX-Trigger)
    document.body.addEventListener('erp:show-success', function(evt) {
        const message = evt.detail.value || evt.detail.message || "Action successful";
        ErpFormHandler.showSuccess(message);
    });
});

document.body.addEventListener('htmx:afterSwap', function() {
    ErpFormHandler.init();
});
