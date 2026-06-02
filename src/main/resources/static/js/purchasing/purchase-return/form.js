(function () {
    'use strict';

    document.addEventListener('DOMContentLoaded', function () {
        var config = window.PurchaseReturnPageConfig || {};
        var form = document.getElementById('purchase-return-form');
        var container = document.getElementById('line-container');
        var headerReason = document.getElementById('header-reason');
        var activeSerialRow = null;
        var dirty = false;
        var lineManager = new ErpLineManager('line-container', 'row-template-source', {
            emptyMsgId: 'empty-msg',
            onUpdate: updateSummary
        });

        function warn(message) {
            if (typeof ErpModal !== 'undefined' && typeof ErpModal.showWarning === 'function') {
                ErpModal.showWarning(message);
            }
        }

        function numeric(input) {
            return input && window.ErpNumeric ? Number(ErpNumeric.get(input) || 0) : 0;
        }

        function setNumeric(input, value) {
            if (input && window.ErpNumeric) ErpNumeric.set(input, value || 0);
        }

        function field(row, name) {
            return row.querySelector('[name$=".' + name + '"]');
        }

        function setField(row, name, value) {
            var input = field(row, name);
            if (input) input.value = value == null ? '' : String(value);
        }

        function displayNumber(value) {
            return Number(value || 0).toLocaleString('en-US', {minimumFractionDigits: 2, maximumFractionDigits: 2});
        }

        function selectionKeys() {
            return Array.from(container.querySelectorAll('.input-selection-key')).map(function (input) {
                return input.value;
            }).filter(Boolean);
        }

        function selectedSerialKeys() {
            var keys = [];
            container.querySelectorAll('.line-row').forEach(function (row) {
                var grLineId = field(row, 'goodsReceiptLineId');
                var containerId = field(row, 'containerId');
                var serials = field(row, 'serialNumbers');
                String(serials ? serials.value : '').split(',').map(function (serial) {
                    return serial.trim();
                }).filter(Boolean).forEach(function (serial) {
                    keys.push(grLineId.value + ':' + containerId.value + ':' + serial);
                });
            });
            return keys;
        }

        function sourcePayload(sourceRow) {
            var data = sourceRow.dataset;
            return {
                selectionKey: data.selectionKey,
                goodsReceiptLineId: data.goodsReceiptLineId,
                productId: data.productId,
                productName: data.productName,
                productCode: data.productSubtext,
                uomId: data.uomId,
                uomName: data.uomName,
                uomCode: data.uomSubtext,
                facilityId: data.facilityId,
                facilityName: data.facilityName,
                gridId: data.gridId,
                gridName: data.gridName,
                gridCode: data.gridSubtext,
                containerId: data.containerId,
                containerName: data.containerName,
                containerCode: data.containerSubtext,
                serialized: data.serialized === 'true',
                outstandingQuantity: Number(data.outstandingQuantity || 0),
                valuationReferenceType: data.valuationReferenceType,
                valuationReferenceId: data.valuationReferenceId,
                valuationReferenceLineId: data.valuationReferenceLineId,
                unitCost: Number(data.unitCost || 0),
                inventoryAmount: Number(data.inventoryAmount || 0),
                taxReversalAmount: Number(data.taxReversalAmount || 0),
                clearingAmount: Number(data.clearingAmount || 0)
            };
        }

        function configureRow(row, payload) {
            row.dataset.selectionKey = payload.selectionKey || '';
            [
                'selectionKey', 'goodsReceiptLineId', 'productId', 'productName', 'productCode',
                'uomId', 'uomName', 'uomCode', 'facilityId', 'facilityName', 'gridId', 'gridName',
                'gridCode', 'containerId', 'containerName', 'containerCode', 'outstandingQuantity',
                'valuationReferenceType', 'valuationReferenceId', 'valuationReferenceLineId',
                'unitCost', 'inventoryAmount', 'taxReversalAmount', 'clearingAmount'
            ].forEach(function (name) { setField(row, name, payload[name]); });
            setField(row, 'serialized', payload.serialized ? 'true' : 'false');
            setField(row, 'baseQuantity', 0);
            setNumeric(field(row, 'quantity'), 0);

            row.querySelector('.line-product-name').textContent = payload.productName || '';
            row.querySelector('.line-product-subtext').textContent = (payload.productCode || '') + ' / ' + (payload.uomCode || '');
            row.querySelector('.line-container-name').textContent = payload.containerName || payload.containerCode || '';
            row.querySelector('.line-container-subtext').textContent =
                (payload.facilityName || '') + ' / ' + (payload.gridCode || '') + ' / ' + (payload.containerCode || '');
            row.querySelector('.line-outstanding-display').textContent = displayNumber(payload.outstandingQuantity);
            initializeRow(row);
        }

        function initializeRow(row) {
            var serialized = field(row, 'serialized') && field(row, 'serialized').value === 'true';
            var qty = field(row, 'quantity');
            var serialButton = row.querySelector('.btn-select-serials');
            var placeholder = row.querySelector('.non-serial-placeholder');
            if (qty) qty.readOnly = serialized;
            if (serialButton) serialButton.classList.toggle('d-none', !serialized);
            if (placeholder) placeholder.classList.toggle('d-none', serialized);
            refreshSerialCount(row);
        }

        function refreshSerialCount(row) {
            var serials = String(field(row, 'serialNumbers') ? field(row, 'serialNumbers').value : '')
                .split(',').map(function (serial) { return serial.trim(); }).filter(Boolean);
            var count = row.querySelector('.serial-count');
            if (count) count.textContent = String(serials.length);
            if (field(row, 'serialized') && field(row, 'serialized').value === 'true') {
                setNumeric(field(row, 'quantity'), serials.length);
                setField(row, 'baseQuantity', serials.length);
                setField(row, 'inventoryAmount', serials.length * Number(field(row, 'unitCost').value || 0));
            }
        }

        function updateSummary() {
            var count = 0;
            var totalQty = 0;
            var totalAmount = 0;
            container.querySelectorAll('.line-row').forEach(function (row) {
                var qty = numeric(field(row, 'quantity'));
                if (qty > 0) count += 1;
                totalQty += qty;
                totalAmount += qty * Number(field(row, 'unitCost').value || 0);
            });
            document.getElementById('summary-line-count').textContent = String(count);
            document.getElementById('summary-total-qty').textContent = displayNumber(totalQty);
            document.getElementById('summary-total-amount').textContent = displayNumber(totalAmount);
        }

        function appendSource(payload) {
            if (!payload.selectionKey || selectionKeys().indexOf(payload.selectionKey) >= 0) return false;
            var row = lineManager.addRow(function (newRow) { configureRow(newRow, payload); });
            if (headerReason && headerReason.value) field(row, 'reason').value = headerReason.value;
            return true;
        }

        function openGrLineSelector() {
            var url = new URL(config.grLineSelectorUrl, window.location.origin);
            url.searchParams.set('goodsReceiptId', config.goodsReceiptId);
            selectionKeys().forEach(function (key) { url.searchParams.append('excludedSelectionKeys', key); });
            window.ERP.ModalSelector.open({
                modalId: 'modal-purchase-return-gr-lines',
                resultsId: 'purchase-return-gr-line-selector-results',
                url: url.pathname + url.search
            });
        }

        function openSerialSelector(row) {
            activeSerialRow = row;
            var url = new URL(config.serialSelectorUrl, window.location.origin);
            url.searchParams.set('goodsReceiptId', config.goodsReceiptId);
            url.searchParams.set('goodsReceiptLineId', field(row, 'goodsReceiptLineId').value);
            selectedSerialKeys().forEach(function (key) { url.searchParams.append('excludedSelectionKeys', key); });
            window.ERP.ModalSelector.open({
                modalId: 'modal-purchase-return-serials',
                resultsId: 'purchase-return-serial-selector-results',
                url: url.pathname + url.search
            });
        }

        function applyGrLines() {
            var checked = document.querySelectorAll('#purchase-return-gr-line-selector-results .js-purchase-return-gr-line-item:checked');
            if (!checked.length) return warn(config.selectAtLeastOne);
            checked.forEach(function (checkbox) { appendSource(sourcePayload(checkbox.closest('tr'))); });
            window.ERP.ModalSelector.close('modal-purchase-return-gr-lines');
            updateSummary();
        }

        function findSerializedRow(payload) {
            return Array.from(container.querySelectorAll('.line-row')).find(function (row) {
                return field(row, 'serialized').value === 'true'
                    && field(row, 'goodsReceiptLineId').value === payload.goodsReceiptLineId
                    && field(row, 'containerId').value === payload.containerId;
            });
        }

        function applySerials() {
            var checked = document.querySelectorAll('#purchase-return-serial-selector-results .js-purchase-return-serial-item:checked');
            if (!checked.length) return warn(config.selectAtLeastOne);
            checked.forEach(function (checkbox) {
                var sourceRow = checkbox.closest('tr');
                var payload = sourcePayload(sourceRow);
                var serial = sourceRow.dataset.serialNumber;
                var row = findSerializedRow(payload);
                if (!row) {
                    payload.selectionKey = payload.goodsReceiptLineId + ':' + payload.containerId;
                    appendSource(payload);
                    row = findSerializedRow(payload);
                }
                var serials = String(field(row, 'serialNumbers').value || '').split(',').map(function (value) {
                    return value.trim();
                }).filter(Boolean);
                if (serials.indexOf(serial) < 0) serials.push(serial);
                setField(row, 'serialNumbers', serials.join(','));
                refreshSerialCount(row);
            });
            activeSerialRow = null;
            window.ERP.ModalSelector.close('modal-purchase-return-serials');
            updateSummary();
        }

        function validateBeforeSubmit(event) {
            var positiveLines = 0;
            var error = null;
            container.querySelectorAll('.line-row').forEach(function (row) {
                var qty = numeric(field(row, 'quantity'));
                if (qty <= 0 || error) return;
                positiveLines += 1;
                var outstanding = Number(field(row, 'outstandingQuantity').value || 0);
                var reason = field(row, 'reason').value;
                var note = field(row, 'note').value.trim();
                var serialized = field(row, 'serialized').value === 'true';
                var serials = String(field(row, 'serialNumbers').value || '').split(',').filter(Boolean);
                if (qty > outstanding) error = config.quantityExceeded;
                else if (!reason) error = config.reasonRequired;
                else if (reason === 'OTHER' && !note) error = config.otherNoteRequired;
                else if (serialized && (serials.length === 0 || serials.length !== qty)) error = config.serialRequired;
            });
            if (positiveLines === 0 && !error) error = config.lineRequired;
            if (!error) return;
            event.preventDefault();
            event.stopImmediatePropagation();
            warn(error);
        }

        document.getElementById('btn-add-line').addEventListener('click', openGrLineSelector);
        container.addEventListener('click', function (event) {
            var remove = event.target.closest('.btn-remove-line');
            if (remove) lineManager.removeRow(remove.closest('.line-row'));
            var serial = event.target.closest('.btn-select-serials');
            if (serial) openSerialSelector(serial.closest('.line-row'));
        });
        container.addEventListener('input', function () { dirty = true; updateSummary(); });
        document.addEventListener('click', function (event) {
            if (event.target.closest('.js-purchase-return-gr-line-apply')) applyGrLines();
            if (event.target.closest('.js-purchase-return-serial-apply')) applySerials();
        });
        if (headerReason) headerReason.addEventListener('change', function () {
            container.querySelectorAll('.input-line-reason').forEach(function (select) {
                if (!select.value) select.value = headerReason.value;
            });
        });
        form.addEventListener('submit', validateBeforeSubmit, true);
        form.addEventListener('input', function () { dirty = true; });
        window.addEventListener('beforeunload', function (event) {
            if (!dirty || window.__erpSuppressBeforeUnload || form.dataset.isSubmitting === 'true') return;
            event.preventDefault();
            event.returnValue = '';
        });
        container.querySelectorAll('.line-row').forEach(initializeRow);
        updateSummary();
    });
})();
