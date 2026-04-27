/**
 * ERP Common Utilities and Global Helpers
 */
const ErpModal = (function() {
    return {
        showError: function(message, title) {
            const msgEl = document.getElementById('error-modal-message');
            const titleEl = document.getElementById('error-modal-title');
            const btn = document.getElementById('btn-trigger-error-modal');
            if (msgEl) msgEl.innerHTML = message;
            if (titleEl && title) titleEl.textContent = title;
            if (btn) btn.click();
        },
        showSuccess: function(message) {
            const container = document.getElementById('erp-success-toast-container');
            if (!container) return;
            const toast = document.createElement('div');
            toast.className = 'alert alert-success alert-dismissible fade show shadow-sm mb-2';
            toast.setAttribute('role', 'alert');
            toast.innerHTML = `<i class="ti ti-circle-check me-2"></i>${message}<button type="button" class="btn-close" data-bs-dismiss="alert"></button>`;
            container.appendChild(toast);
            setTimeout(() => { toast.classList.remove('show'); setTimeout(() => toast.remove(), 200); }, 4000);
        },
        showWarning: function(message, title) {
            const msgEl = document.getElementById('warning-modal-message');
            const titleEl = document.getElementById('warning-modal-title');
            const btn = document.getElementById('btn-trigger-warning-modal');
            if (msgEl) msgEl.innerHTML = message;
            if (titleEl && title) titleEl.textContent = title;
            if (btn) btn.click();
        },
        confirm: function(message, callback, title) {
            const msgEl = document.getElementById('confirm-modal-message');
            const titleEl = document.getElementById('confirm-modal-title');
            const yesBtn = document.getElementById('confirm-modal-btn-yes');
            const triggerBtn = document.getElementById('btn-trigger-confirm-modal');
            
            if (msgEl) msgEl.innerHTML = message;
            if (titleEl && title) titleEl.textContent = title;
            
            if (yesBtn) {
                const newBtn = yesBtn.cloneNode(true);
                yesBtn.parentNode.replaceChild(newBtn, yesBtn);
                newBtn.addEventListener('click', function() {
                    const modalEl = document.getElementById('modal-global-confirm');
                    const modalInstance = (typeof bootstrap !== 'undefined' && bootstrap.Modal) ? bootstrap.Modal.getInstance(modalEl) : null;
                    if (modalInstance) modalInstance.hide();
                    else {
                        const closeBtn = modalEl.querySelector('[data-bs-dismiss="modal"]');
                        if (closeBtn) closeBtn.click();
                    }
                    if (typeof callback === 'function') callback();
                });
            }
            if (triggerBtn) triggerBtn.click();
        }
    };
})();

/**
 * ERP Action Helper for standardized Confirm -> Validate -> POST workflow
 */
const ErpAction = {
    confirmAndSubmit: function(message, options) {
        const { url, method = 'POST', onBefore, title } = options;
        if (onBefore && typeof onBefore === 'function' && !onBefore()) return;

        ErpModal.confirm(message, () => {
            const form = document.createElement('form');
            form.method = method;
            form.action = url;
            const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
            const csrfParam = document.querySelector('meta[name="_csrf_parameter"]')?.content || '_csrf';
            if (csrfToken) {
                const input = document.createElement('input');
                input.type = 'hidden';
                input.name = csrfParam;
                input.value = csrfToken;
                form.appendChild(input);
            }
            document.body.appendChild(form);
            form.submit();
        }, title);
    }
};

/**
 * ERP Form Helper for Direct Button Actions
 */
const ErpForm = {
    /**
     * Perform a POST action via AJAX from a button element.
     * Button should have a data-*-url attribute (e.g., data-send-url, data-complete-url, data-action-url).
     * Optional: data-confirm-message for confirmation before action.
     */
    postAction: function(buttonEl) {
        if (!buttonEl) return;
        
        // Extract URL from any data-*-url attribute
        let url = null;
        for (let attr of buttonEl.attributes) {
            if (attr.name.match(/^data-.*-url$/) && attr.value) {
                url = attr.value;
                break;
            }
        }
        
        if (!url) {
            console.warn('[ErpForm] Button action missing data-*-url attribute (e.g., data-send-url, data-complete-url)');
            return;
        }

        // Check for confirmation message
        const confirmMessage = buttonEl.getAttribute('data-confirm-message');
        
        const performAction = () => {
            // Disable button to prevent double-click
            const wasDisabled = buttonEl.disabled;
            buttonEl.disabled = true;
            
            // Get CSRF token
            const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';
            
            // Perform fetch with CSRF in header
            fetch(url, {
                method: 'POST',
                headers: {
                    [csrfHeader]: csrfToken || ''
                }
            })
            .then(response => {
                return response.json().catch(() => ({
                    success: false,
                    message: 'Server error: Invalid response format'
                })).then(data => {
                    // Check if response indicates failure (HTTP error or API error)
                    if (!response.ok || data.success === false) {
                        throw new Error(data.message || 'Action failed');
                    }
                    return data;
                });
            })
            .then(data => {
                // Success - show toast and reload
                const message = data.message || 'Action completed successfully';
                ErpModal.showSuccess(message);
                
                // Reload page after success
                setTimeout(() => { window.location.reload(); }, 1500);
            })
            .catch(error => {
                console.error('[ErpForm] Action failed:', error);
                ErpModal.showError(error.message || 'Action failed. Please try again.');
                buttonEl.disabled = wasDisabled;
            });
        };
        
        // If confirmation is required, show it; otherwise perform action immediately
        if (confirmMessage) {
            ErpModal.confirm(confirmMessage, performAction);
        } else {
            performAction();
        }
    }
};

/**
 * ERP Side Drawer Helper
 */
const ErpDrawer = (function() {
    return {
        open: function(id, title) {
            const drawerEl = document.getElementById(id);
            if (!drawerEl) return;
            if (title) {
                const titleEl = drawerEl.querySelector('.offcanvas-title');
                if (titleEl) titleEl.textContent = title;
            }
            
            const triggerBtn = document.getElementById('btn-trigger-' + id);
            if (triggerBtn) {
                triggerBtn.click();
            } else {
                // Generic Bootstrap 5 Offcanvas Trigger
                const btn = document.createElement('button');
                btn.setAttribute('data-bs-toggle', 'offcanvas');
                btn.setAttribute('data-bs-target', '#' + id);
                btn.style.display = 'none';
                document.body.appendChild(btn);
                btn.click();
                btn.remove();
            }
        },
        close: function(id) {
            const drawerEl = document.getElementById(id);
            if (!drawerEl) return;
            const closeBtn = drawerEl.querySelector('[data-bs-dismiss="offcanvas"]');
            if (closeBtn) closeBtn.click();
        }
    };
})();

/**
 * Robust AutoNumeric Helper (Generic)
 */
const ErpNumeric = {
    set: (el, val) => {
        if (!el) return;
        const instance = (typeof AutoNumeric !== 'undefined') ? AutoNumeric.getAutoNumericElement(el) : null;
        if (instance) instance.set(val);
        else el.value = val;
    },
    get: (el) => {
        if (!el) return 0;
        const instance = (typeof AutoNumeric !== 'undefined') ? AutoNumeric.getAutoNumericElement(el) : null;
        return instance ? instance.getNumber() : (parseFloat(el.value.replace(/,/g, '')) || 0);
    }
};

/**
 * ERP Line/Row Manager (Generic)
 */
class ErpLineManager {
    constructor(containerId, templateSourceId, options = {}) {
        this.container = document.getElementById(containerId.replace('#', ''));
        this.templateSource = document.getElementById(templateSourceId.replace('#', ''));
        this.emptyMsg = document.getElementById(options.emptyMsgId || 'empty-msg');
        this.onUpdate = options.onUpdate || null;
    }

    addRow(customSetup = null) {
        const index = this.container.querySelectorAll('.line-row').length;
        let html = this.templateSource.innerHTML.replace(/INDEX/g, index)
                                               .replace(/data-autonumeric="true"/g, '');
        const temp = document.createElement('tbody');
        temp.innerHTML = html;
        const row = temp.querySelector('tr');
        this.container.appendChild(row);
        
        initNumericInputs(row);
        if (customSetup) customSetup(row);
        this.updateIndexes();
        return row;
    }

    updateIndexes() {
        let index = 0;
        this.container.querySelectorAll('.line-row').forEach(row => {
            row.dataset.index = index;
            row.querySelectorAll('input, select, textarea').forEach(input => {
                if (input.name) {
                    input.name = input.name.replace(/\[\d+\]/, `[${index}]`);
                }
            });
            index++;
        });
        if (this.emptyMsg) this.emptyMsg.style.display = this.container.children.length > 0 ? 'none' : '';
        if (this.onUpdate) this.onUpdate();
    }

    removeRow(row) {
        row.remove();
        this.updateIndexes();
    }
}

// Global Exports
window.ErpModal = ErpModal;
window.ErpDrawer = ErpDrawer;
window.ErpAction = ErpAction;
window.ErpForm = ErpForm;
window.ErpNumeric = ErpNumeric;

/**
 * Global Debounce Function
 */
function debounce(fn, delay) {
    let timer;
    return function (...args) {
        clearTimeout(timer);
        timer = setTimeout(() => fn.apply(this, args), delay);
    };
}

/**
 * Initialize AutoNumeric for all inputs with numeric classes
 */
function initNumericInputs(container = document) {
    if (typeof AutoNumeric === 'undefined') return;
    const decimalOptions = { digitGroupSeparator: ',', decimalCharacter: '.', decimalPlaces: 2, minimumValue: '0', unformatOnSubmit: false, modifyValueOnWheel: false, allowDecimalPadding: true };
    const integerOptions = { digitGroupSeparator: ',', decimalPlaces: 0, minimumValue: '0', unformatOnSubmit: false, modifyValueOnWheel: false };

    const setupElement = (el) => {
        if (el.hasAttribute('data-autonumeric') || el.closest('#row-template-source')) return;
        if (el.classList.contains('erp-number-decimal')) { new AutoNumeric(el, decimalOptions); el.setAttribute('data-autonumeric', 'true'); }
        else if (el.classList.contains('erp-number-integer')) { new AutoNumeric(el, integerOptions); el.setAttribute('data-autonumeric', 'true'); }
    };

    if (container instanceof HTMLInputElement) setupElement(container);
    else container.querySelectorAll('.erp-number-decimal, .erp-number-integer').forEach(setupElement);
}

/**
 * Initialize TomSelect for generic lookups
 */
function initLookup(el, lookupPath, parentProvider = null) {
    if (!el || typeof TomSelect === 'undefined') return null;
    if (el.tomselect) el.tomselect.destroy();

    const ts = new TomSelect(el, {
        valueField: 'id', labelField: 'name', searchField: ['name'], placeholder: '-- Select --', allowEmptyOption: false, preload: 'focus', 
        onInitialize: function() {
            this.wrapper.classList.add('erp-input-ts');
            if (el.closest('.line-row')) this.wrapper.classList.add('erp-input-ts-sm');
            this.control.style.width = '100%';
            
            const initialOption = el.querySelector('option[selected], option[value]:not([value=""])');
            if (initialOption) {
                const val = initialOption.value;
                if (this.options[val]) {
                    const subText = initialOption.getAttribute('data-subtext');
                    if (subText) this.options[val].subText = subText;
                    const payload = {};
                    Array.from(initialOption.attributes).forEach(attr => {
                        if (attr.name.startsWith('data-payload-')) payload[attr.name.replace('data-payload-', '')] = attr.value;
                    });
                    this.options[val].payload = payload;
                    this.refreshOptions(false);
                    // Re-render selected item so subText shows on initial load
                    if (subText) {
                        this.removeItem(val, true);
                        this.addItem(val, true);
                    }
                }
            }
        },
        load: debounce(function(q, callback) {
            const separator = lookupPath.includes('?') ? '&' : '?';
            let url = `/api/lookup/${lookupPath}${separator}q=${encodeURIComponent(q)}&limit=10`;
            if (parentProvider) {
                const parent = parentProvider();
                if (parent && parent.id) url += `&${parent.key || 'parentId'}=${parent.id}`;
            }
            fetch(url).then(r => r.json()).then(callback).catch(() => callback([]));
        }, 150),
        render: {
            option: (data, escape) => {
                if (!data.id) return '';
                let sub = data.subText ? `<small class="text-muted d-block" style="font-size:0.75em">${escape(data.subText)}</small>` : '';
                return `<div class="py-1"><div>${escape(data.name)}</div>${sub}</div>`;
            },
            item: (data, escape) => {
                let sub = data.subText ? `<small class="text-muted ms-1" style="font-size:0.8em;opacity:0.7">${escape(data.subText)}</small>` : '';
                return `<span>${escape(data.name)}${sub}</span>`;
            }
        }
    });

    if (parentProvider) {
        ts.on('dropdown_open', () => { ts.clearCache(); ts.clearOptions(); ts.load(''); });
    } else {
        ts.on('dropdown_open', () => {
            const value = ts.getValue();
            if (value && ts.options[value]) {
                const currentOption = ts.options[value];
                // Sembunyikan label yang terpilih agar tidak dobel dengan teks input
                ts.control.querySelectorAll('.item').forEach(item => { item.style.display = 'none'; });
                
                if (!ts.control_input.value) {
                    ts.setTextboxValue(currentOption.name);
                    // Refresh options agar dropdown muncul tanpa mengunci query ke server secara paksa
                    ts.refreshOptions(false);
                }
            }
        });
        ts.on('dropdown_close', () => {
            // Kembalikan tampilan label yang terpilih
            ts.control.querySelectorAll('.item').forEach(item => { item.style.display = ''; });
            // Kosongkan teks input agar saat diklik lagi dimulai dari kondisi bersih/placeholder
            if (ts.getValue()) ts.setTextboxValue('');
        });
    }
    el.dispatchEvent(new CustomEvent('erp:lookup-initialized', { bubbles: true, detail: { tomselect: ts } }));
    return ts;
}

/**
 * Automatically initialize all lookups with data-lookup-path attribute
 */
function initAllLookups(container = document) {
    container.querySelectorAll('select[data-lookup-path]:not(.tomselect-initialized)').forEach(el => {
        initLookup(el, el.getAttribute('data-lookup-path'));
        el.classList.add('tomselect-initialized');
    });
}

window.ERP = window.ERP || {};
window.ERP.initAutocompleteInContainer = initAllLookups;
