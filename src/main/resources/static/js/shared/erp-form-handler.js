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

    /**
     * Sets a value in an object based on a path (e.g., "lines[0].productId").
     * Supports merging into existing arrays if the target is an array.
     */
    const setDeepValue = function(obj, path, value) {
        const parts = path.split(/[\[\].]+/).filter(p => p !== '');
        let current = obj;
        for (let i = 0; i < parts.length; i++) {
            const part = parts[i];
            const nextPart = parts[i + 1];
            const isNextArray = nextPart !== undefined && !isNaN(parseInt(nextPart));

            if (i === parts.length - 1) {
                current[part] = value;
            } else {
                if (!current[part]) {
                    current[part] = isNextArray ? [] : {};
                }
                current = current[part];
            }
        }
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
        showAlert(message, 'success', 'ti-check');
    }

    /**
     * Display a warning message. Used by Smart Delete when entity is deactivated instead of deleted.
     */
    const showWarning = function(message) {
        showAlert(message, 'warning', 'ti-alert-triangle');
    }

    const showAlert = function(message, type, icon) {
        // Find appropriate container for alerts (Same logic as checkPendingSuccess)
        let alertContainer = document.querySelector('.alert-container') || 
                             document.querySelector('.page-body .page-body .container-xl') ||
                             document.querySelector('.page-body .container-xl');
        
        if (alertContainer) {
            // Remove existing ajax alerts to prevent stacking
            const existingAlerts = alertContainer.querySelectorAll('.alert-ajax-global');
            existingAlerts.forEach(el => el.remove());

            const alertHtml = `
                <div class="alert alert-${type} alert-dismissible fade show alert-ajax-global" role="alert">
                    <div class="d-flex">
                        <div><i class="ti ${icon} icon alert-icon"></i></div>
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
        const method = form.dataset.method || 'POST';

        form.dataset.isSubmitting = 'true';
        let skipSubmittingReset = false;

        clearErrors(form);
        if (indicator) indicator.style.display = 'inline-block';
        if (submitBtn) submitBtn.disabled = true;

        try {
            const data = {};
            const elements = form.querySelectorAll('input, select, textarea');
            
            // Temporary storage for aggregating multi-value fields (checkboxes with same name, multi-select)
            const multiValueFields = new Map();

            elements.forEach(el => {
                if (!el.name || el.disabled || el.type === 'file' || el.name === '_csrf') return;
                
                // Determine if this field should be part of a collection
                const isMultiSelect = el.tagName === 'SELECT' && el.multiple;
                const isCheckbox = el.type === 'checkbox';
                
                // We check if there are other elements with the same name to treat it as a collection
                // Or if the name explicitly ends with []
                const sameNameCount = form.querySelectorAll(`[name="${CSS.escape(el.name)}"]`).length;
                const isExplicitArray = el.name.endsWith('[]');
                const cleanName = isExplicitArray ? el.name.slice(0, -2) : el.name;

                if (isMultiSelect) {
                    const values = Array.from(el.selectedOptions).map(opt => opt.value === "" ? null : opt.value);
                    setDeepValue(data, cleanName, values);
                } else if (isCheckbox) {
                    if (el.name.startsWith('_')) return; // Ignore spring hidden helper fields

                    if (sameNameCount > 1 || isExplicitArray) {
                        if (!multiValueFields.has(cleanName)) multiValueFields.set(cleanName, []);
                        if (el.checked) {
                            multiValueFields.get(cleanName).push(el.value);
                        }
                    } else {
                        // Single checkbox acts as boolean
                        setDeepValue(data, cleanName, el.checked);
                    }
                } else if (el.type === 'radio') {
                    if (el.checked) {
                        setDeepValue(data, cleanName, el.value === "" ? null : el.value);
                    }
                } else if (el.classList.contains('erp-number-decimal') || el.classList.contains('erp-number-integer')) {
                    let numericValue = null;
                    const instance = typeof AutoNumeric !== 'undefined' ? AutoNumeric.getAutoNumericElement(el) : null;
                    if (instance) {
                        numericValue = instance.getNumber();
                    } else {
                        numericValue = el.value === "" ? null : parseFloat(el.value.replace(/,/g, ''));
                    }
                    setDeepValue(data, cleanName, numericValue);
                } else {
                    setDeepValue(data, cleanName, el.value === "" ? null : el.value);
                }
            });

            // Merge aggregated multi-value fields into the final data object
            multiValueFields.forEach((values, name) => {
                setDeepValue(data, name, values);
            });

            const csrfToken = document.querySelector('input[name="_csrf"]')?.value;
            const response = await fetch(form.action, {
                method: method,
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json',
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
                    skipSubmittingReset = true;
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
            if (!skipSubmittingReset) {
                delete form.dataset.isSubmitting;
            }
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
            // Use CSS.escape for fields with brackets like lines[0].quantity
            const input = form.querySelector(`[name="${field}"]`) || form.querySelector(`[name="${CSS.escape(field)}"]`);
            if (input) {
                input.classList.add('is-invalid');
                const errorDiv = document.createElement('div');
                errorDiv.className = 'invalid-feedback invalid-feedback-ajax d-block';
                errorDiv.textContent = errors[field];
                
                // Handle TomSelect wrapper if exists
                if (input.tomselect && input.tomselect.wrapper) {
                    input.tomselect.wrapper.classList.add('is-invalid-ts');
                    input.tomselect.wrapper.parentNode.appendChild(errorDiv);
                } else {
                    input.parentNode.appendChild(errorDiv);
                }
            } else {
                unmappedErrors.push(`${field}: ${errors[field]}`);
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

    window.ErpFormHandler = {
        init: init,
        showSuccess: showSuccess,
        showWarning: showWarning
    };

    return window.ErpFormHandler;
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

    // Listen for custom "erp:show-warning" events (used by Smart Delete for soft-delete feedback)
    document.body.addEventListener('erp:show-warning', function(evt) {
        const message = evt.detail.value || evt.detail.message || "Action completed with warning";
        ErpFormHandler.showWarning(message);
    });
});

document.body.addEventListener('htmx:afterSwap', function() {
    ErpFormHandler.init();
});
