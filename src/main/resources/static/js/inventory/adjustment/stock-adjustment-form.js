(function () {
    const cfg = window.StockAdjustmentPageConfig || {};

    function syncSerialRows(drawer, count, row) {
        const container = drawer.querySelector('.serial-input-container');
        const existingSns = (row.querySelector('.input-sn-single')?.value || '')
            .split(',')
            .map(s => s.trim())
            .filter(Boolean);

        const rows = Array.from(container.querySelectorAll('tr'));
        if (rows.length > count) {
            for (let i = rows.length - 1; i >= count; i--) {
                rows[i].remove();
            }
            return;
        }

        for (let i = rows.length; i < count; i++) {
            const tr = document.createElement('tr');
            tr.innerHTML = `<td class="text-center small text-secondary">${i + 1}</td>
                            <td><input type="text" class="form-control form-control-sm input-sn-item" value="${existingSns[i] || ''}"></td>
                            <td class="text-end text-secondary">1.00</td>`;
            container.appendChild(tr);
        }
    }

    function createLookup(el, lookupPath, parentProvider = null) {
        if (!el || typeof TomSelect === 'undefined') return null;
        if (el.tomselect) el.tomselect.destroy();

        const ts = new TomSelect(el, {
            valueField: 'id',
            labelField: 'name',
            searchField: ['name'],
            placeholder: '-- Select --',
            allowEmptyOption: false,
            preload: 'focus',
            load: debounce(function (q, callback) {
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
                    const sub = data.subText ? `<small class="text-muted d-block" style="font-size:0.75em">${escape(data.subText)}</small>` : '';
                    return `<div class="py-1"><div>${escape(data.name)}</div>${sub}</div>`;
                },
                item: (data, escape) => {
                    const sub = data.subText ? `<small class="text-muted ms-1" style="font-size:0.8em;opacity:0.7">${escape(data.subText)}</small>` : '';
                    return `<span>${escape(data.name)}${sub}</span>`;
                }
            }
        });

        if (parentProvider) {
            ts.on('dropdown_open', () => {
                ts.clearCache();
                ts.clearOptions();
                ts.load('');
            });
        }

        return ts;
    }

    function setupUomDrawer(drawer, row, isSerial, onSave) {
        const tsProd = row.querySelector('.select-product')?.tomselect;
        if (!tsProd) return false;

        const productId = tsProd.getValue();
        if (!productId) {
            ErpModal.showWarning('Please select a product first.');
            return false;
        }

        const uomEl = drawer.querySelector('.select-uom-target');
        const qtyTargetEl = drawer.querySelector('.input-qty-target');
        const qtyBaseEl = drawer.querySelector('.input-qty-base');
        const prodData = tsProd.options[productId];
        const baseUomAlias = prodData?.payload?.uomName || row.querySelector('.input-uom-alias')?.value || '-';
        const baseUomId = prodData?.payload?.uomId || row.querySelector('.input-uom-id')?.value || '';
        const selectedUomId = row.querySelector('.input-uom-id')?.value || baseUomId;
        const currentFactor = parseFloat(row.querySelector('.input-uom-factor')?.value) || 1;
        const currentBaseQty = ErpNumeric.get(row.querySelector('.input-qty'));

        drawer.querySelectorAll('.txt-base-uom').forEach(el => el.textContent = baseUomAlias);

        function updateCalculations() {
            const opt = uomEl.options[uomEl.selectedIndex];
            if (!opt) return;

            const factor = parseFloat(opt.getAttribute('data-factor')) || 1;
            const targetQty = ErpNumeric.get(qtyTargetEl);
            const resultBase = targetQty * factor;

            drawer.querySelectorAll('.txt-target-uom').forEach(el => el.textContent = opt.text);
            drawer.querySelectorAll('.txt-factor').forEach(el => el.textContent = factor.toFixed(2));
            ErpNumeric.set(qtyBaseEl, resultBase);

            const displayEl = drawer.querySelector('.input-qty-base-display');
            if (displayEl) displayEl.textContent = resultBase.toLocaleString(undefined, { minimumFractionDigits: 2 });

            if (isSerial) {
                const count = Math.floor(resultBase);
                const totalQtyEl = drawer.querySelector('.txt-total-qty');
                if (totalQtyEl) totalQtyEl.textContent = count;
                syncSerialRows(drawer, count, row);
            }
        }

        function populateUomDropdown(data) {
            // When TomSelect has no payload (pre-edit rows with static HTML options),
            // baseUomId falls back to input-uom-id which holds the *selected* UOM, not
            // the product base UOM. Use isBase flag from the API response to resolve
            // the correct base UOM instead.
            const baseFromApi = data && data.find(u => u.isBase);
            const resolvedBaseUomId = baseFromApi ? String(baseFromApi.uomId) : String(baseUomId);
            const resolvedBaseUomAlias = baseFromApi ? baseFromApi.uomName : baseUomAlias;

            uomEl.innerHTML = '';

            const baseOpt = document.createElement('option');
            baseOpt.value = resolvedBaseUomId;
            baseOpt.text = resolvedBaseUomAlias;
            baseOpt.setAttribute('data-factor', '1.00');
            uomEl.appendChild(baseOpt);

            (data || []).forEach(u => {
                if (!u.isBase) {
                    const opt = document.createElement('option');
                    opt.value = u.uomId;
                    opt.text = u.uomName;
                    const factor = typeof u.conversionFactor === 'object'
                        ? u.conversionFactor.parsedValue
                        : u.conversionFactor;
                    opt.setAttribute('data-factor', factor || 1.00);
                    uomEl.appendChild(opt);
                }
            });

            // Update base UOM labels with the resolved value (overrides the initial
            // optimistic value set before the fetch, which may have been wrong for
            // pre-edit rows)
            drawer.querySelectorAll('.txt-base-uom').forEach(el => el.textContent = resolvedBaseUomAlias);

            uomEl.value = selectedUomId;
            initNumericInputs(drawer);
            ErpNumeric.set(qtyTargetEl, currentBaseQty / currentFactor);
            updateCalculations();
        }

        fetch(`/api/lookup/inventory/uom-conversions?productId=${productId}`, {
            headers: { Accept: 'application/json' }
        })
            .then(r => {
                if (!r.ok) throw new Error(`HTTP ${r.status}`);
                return r.json();
            })
            .then(populateUomDropdown)
            .catch(() => populateUomDropdown([]));

        uomEl.onchange = updateCalculations;
        qtyTargetEl.addEventListener('autoNumeric:rawValueModified', updateCalculations);

        drawer.querySelector('.btn-save-drawer').onclick = () => {
            const opt = uomEl.options[uomEl.selectedIndex];
            if (!opt) return;

            row.querySelector('.input-uom-id').value = uomEl.value;
            row.querySelector('.input-uom-alias').value = opt.text;
            row.querySelector('.input-uom-factor').value = opt.getAttribute('data-factor');
            ErpNumeric.set(row.querySelector('.input-qty'), ErpNumeric.get(qtyBaseEl));

            if (isSerial) {
                const sns = Array.from(drawer.querySelectorAll('.input-sn-item'))
                    .map(el => el.value.trim())
                    .filter(Boolean);
                row.querySelector('.input-sn-single').value = sns.join(',');
            }

            if (typeof onSave === 'function') onSave();
            ErpDrawer.close(drawer.id);
        };

        return true;
    }

    function setupRowLogic(row, headerFacility, calculateTotals, lineManager) {
        const tsProd = createLookup(row.querySelector('.select-product'), 'inventory/products');
        const tsGrid = createLookup(row.querySelector('.select-grid'), 'inventory/grids', () => ({ id: headerFacility.value, key: 'facilityId' }));
        const tsBin = createLookup(row.querySelector('.select-container'), 'inventory/containers', () => {
            const gridId = tsGrid.getValue();
            return gridId ? { id: gridId, key: 'gridId' } : { id: headerFacility.value, key: 'facilityId' };
        });

        if (!tsProd || !tsGrid || !tsBin) return;

        tsProd.on('change', (val) => {
            if (!val) return;
            const p = tsProd.options[val].payload || {};
            row.querySelector('.input-uom-id').value = p.uomId || '';
            row.querySelector('.input-uom-alias').value = p.uomName || '';
            row.querySelector('.input-serialized').value = (p.isSerialized === true || p.isSerialized === 'true') ? 'true' : 'false';
            ErpNumeric.set(row.querySelector('.input-price'), p.lastCost || 0);
            calculateTotals();
        });

        tsGrid.on('change', (val) => {
            if (!val) {
                tsBin.clear();
                tsBin.clearOptions();
            }
        });

        tsBin.on('change', (val) => {
            if (!val) return;
            const p = tsBin.options[val].payload || {};
            if (p.gridId && tsGrid.getValue() != p.gridId) {
                tsGrid.addOption({ id: p.gridId, name: p.gridName, subText: p.gridCode });
                tsGrid.setValue(p.gridId);
            }
        });

        row.querySelector('.btn-edit-detail').onclick = () => {
            const isSerialized = row.querySelector('.input-serialized').value === 'true';
            const drawerId = isSerialized ? 'drawer-serial' : 'drawer-non-serial';
            if (setupUomDrawer(document.getElementById(drawerId), row, isSerialized, calculateTotals)) {
                ErpDrawer.open(drawerId);
            }
        };

        row.querySelector('.btn-remove-line').onclick = () => {
            lineManager.removeRow(row);
        };
    }

    function initStockAdjustmentForm() {
        const lineContainer = document.getElementById('line-container');
        if (!lineContainer || typeof TomSelect === 'undefined') return;

        const btnAddLine = document.getElementById('btn-add-line');
        const headerFacility = document.getElementById('header-facility');
        const headerCurrency = document.getElementById('header-currency');
        const headerRate = document.getElementById('header-rate');
        const recapOriginal = document.getElementById('recap-total-original');
        const recapLocal = document.getElementById('recap-total-local');
        const labelRecapOriginal = document.getElementById('label-recap-original');

        const lineManager = new ErpLineManager('line-container', 'row-template-source', { onUpdate: calculateTotals });

        function calculateTotals() {
            let total = 0;
            const rate = ErpNumeric.get(headerRate) || 1;

            lineContainer.querySelectorAll('.line-row').forEach(row => {
                const sub = ErpNumeric.get(row.querySelector('.input-qty')) * ErpNumeric.get(row.querySelector('.input-price'));
                row.querySelector('.line-total').textContent = sub.toLocaleString(undefined, { minimumFractionDigits: 2 });
                total += sub;
            });

            recapOriginal.textContent = total.toLocaleString(undefined, { minimumFractionDigits: 2 });
            recapLocal.textContent = (total * rate).toLocaleString(undefined, { minimumFractionDigits: 2 });
        }

        function setupNewRow(row) {
            setupRowLogic(row, headerFacility, calculateTotals, lineManager);
        }

        if (btnAddLine) {
            btnAddLine.onclick = () => {
                if (!headerFacility.value) {
                    ErpModal.showWarning((cfg.msgSelectFirst || 'Please select {0} first!').replace('{0}', cfg.labelFacility || 'Facility'));
                    return;
                }
                const row = lineManager.addRow();
                if (row) setupNewRow(row);
            };
        }

        lineContainer.addEventListener('input', calculateTotals);
        headerRate.addEventListener('input', calculateTotals);

        new TomSelect(headerCurrency, {
            onChange: () => {
                const opt = headerCurrency.options[headerCurrency.selectedIndex];
                labelRecapOriginal.textContent = `Total (${opt.getAttribute('data-alias') || opt.text})`;
                calculateTotals();
            }
        });

        const facilityLookup = createLookup(headerFacility, 'inventory/facilities');
        if (facilityLookup) {
            facilityLookup.on('change', () => {
                if (lineContainer.children.length > 0) {
                    ErpModal.confirm(cfg.confirmChangeFacility || 'Changing the facility will remove all existing items. Proceed?', () => {
                        lineContainer.innerHTML = '';
                        lineManager.updateIndexes();
                    });
                }
            });
        }

        lineContainer.querySelectorAll('.line-row').forEach(setupNewRow);
        lineManager.updateIndexes();

        const btnProcess = document.getElementById('btn-process-inventory');
        if (btnProcess) {
            btnProcess.onclick = () => ErpAction.confirmAndSubmit(cfg.processConfirm || 'Are you sure?', {
                url: btnProcess.dataset.processUrl || '',
                onBefore: () => lineContainer.children.length > 0 || (ErpModal.showWarning('Add at least one item.') && false)
            });
        }

        if (cfg.isLocked) {
            document.querySelectorAll('button, input, select, textarea').forEach(el => {
                if (!el.classList.contains('btn-close') && el.id !== 'global-menu-search') el.disabled = true;
            });
        }
    }

    let booted = false;
    function boot() {
        if (booted) return;
        if (typeof TomSelect === 'undefined' || typeof ErpLineManager === 'undefined' || typeof ErpDrawer === 'undefined') {
            window.setTimeout(boot, 50);
            return;
        }
        booted = true;
        initStockAdjustmentForm();
    }

    if (document.readyState === 'complete') {
        boot();
    } else {
        window.addEventListener('load', boot, { once: true });
    }

    window.StockAdjustmentForm = {
        init: initStockAdjustmentForm,
        setupUomDrawer
    };
})();
