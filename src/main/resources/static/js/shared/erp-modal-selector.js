(function () {
    window.ERP = window.ERP || {};

    function replaceTargetHtml(resultsId, html) {
        var target = document.getElementById(resultsId);
        if (!target) return;

        var wrapper = document.createElement('div');
        wrapper.innerHTML = html.trim();
        var next = wrapper.firstElementChild;
        if (!next) return;
        target.replaceWith(next);
    }

    function getModalProvider() {
        return window.bootstrap || window.tabler;
    }

    function getModalInstance(modalEl) {
        var modalProvider = getModalProvider();
        if (!modalEl || !modalProvider || !modalProvider.Modal) return null;

        if (window.bootstrap && bootstrap.Modal && bootstrap.Modal.getOrCreateInstance) {
            return bootstrap.Modal.getOrCreateInstance(modalEl);
        }

        return new modalProvider.Modal(modalEl);
    }

    function getOpenBootstrapModalInstance(modalEl) {
        if (!modalEl || !window.bootstrap || !bootstrap.Modal || !bootstrap.Modal.getInstance) return null;
        return bootstrap.Modal.getInstance(modalEl);
    }

    window.ERP.ModalSelector = {
        open: function (options) {
            var modalEl = document.getElementById(options.modalId);
            if (!modalEl || !options.resultsId || !options.url) return;

            var modal = getModalInstance(modalEl);
            if (window.htmx) {
                window.htmx.ajax('GET', options.url, {
                    target: '#' + options.resultsId,
                    swap: 'outerHTML'
                });
            } else {
                fetch(options.url, { headers: { 'HX-Request': 'true' } })
                    .then(function (response) { return response.text(); })
                    .then(function (html) { replaceTargetHtml(options.resultsId, html); });
            }

            if (modal) modal.show();
        },
        close: function (modalId) {
            var modalEl = document.getElementById(modalId);
            var modal = getOpenBootstrapModalInstance(modalEl);
            if (modal && typeof modal.hide === 'function') {
                modal.hide();
                return;
            }

            if (modalEl) {
                var closeBtn = modalEl.querySelector('[data-bs-dismiss="modal"]');
                if (closeBtn) closeBtn.click();
            }
        }
    };
})();
