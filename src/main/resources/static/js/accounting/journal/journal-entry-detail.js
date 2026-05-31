(function () {
  function csrfHeaders() {
    var token = document.querySelector('meta[name="_csrf"]');
    var header = document.querySelector('meta[name="_csrf_header"]');
    var headers = { "Content-Type": "application/json" };
    if (token && header) headers[header.content] = token.content;
    return headers;
  }

  function idFromPath() {
    var parts = window.location.pathname.split("/").filter(Boolean);
    return parts[parts.length - 1];
  }

  function postJson(url, body) {
    return fetch(url, {
      method: "POST",
      headers: csrfHeaders(),
      body: body == null ? null : JSON.stringify(body)
    }).then(function (response) {
      if (!response.ok) throw new Error("Request failed");
      return response.json();
    });
  }

  document.addEventListener("DOMContentLoaded", function () {
    var id = idFromPath();
    var postButton = document.getElementById("btn-post-journal");
    var reverseButton = document.getElementById("btn-reverse-journal");
    var confirmButton = document.getElementById("btn-confirm-reversal");
    var modalElement = document.getElementById("journal-reversal-modal");
    var modal = modalElement && window.bootstrap ? new bootstrap.Modal(modalElement) : null;

    if (postButton) {
      postButton.addEventListener("click", function () {
        var performPost = function () {
          postButton.disabled = true;
          postJson("/accounting/journal-entries/" + id + "/post")
            .then(function () { window.location.reload(); })
            .catch(function (error) {
              postButton.disabled = false;
              if (window.ErpModal) ErpModal.showError(error.message);
            });
        };
        var message = postButton.getAttribute("data-confirm-message");
        if (window.ErpModal && message) {
          ErpModal.confirm(message, performPost);
        } else {
          performPost();
        }
      });
    }

    if (reverseButton && modal) {
      reverseButton.addEventListener("click", function () { modal.show(); });
    }

    if (confirmButton) {
      confirmButton.addEventListener("click", function () {
        var date = document.getElementById("reversal-posting-date").value;
        confirmButton.disabled = true;
        postJson("/accounting/journal-entries/" + id + "/reverse", { postingDate: date })
          .then(function (payload) {
            var data = payload && payload.data ? payload.data : {};
            var codeMatch = data.journalCode ? String(data.journalCode).match(/JNL-(\d+)/) : null;
            var newId = data.id || (codeMatch ? Number(codeMatch[1]) : id);
            sessionStorage.setItem("erp_pending_success", "Journal reversed.");
            window.location.href = "/accounting/journal-entries/" + newId;
          })
          .catch(function (error) {
            confirmButton.disabled = false;
            if (window.ErpModal) ErpModal.showError(error.message);
          });
      });
    }
  });
})();
