(function () {
  function csrfHeaders() {
    var token = document.querySelector('meta[name="_csrf"]');
    var header = document.querySelector('meta[name="_csrf_header"]');
    var headers = {
      "Content-Type": "application/json",
      "Accept": "application/json",
      "X-Requested-With": "XMLHttpRequest"
    };
    if (token && header) headers[header.content] = token.content;
    return headers;
  }

  function actionError(message, status) {
    var error = new Error(message || "Action failed");
    error.status = status;
    return error;
  }

  function parsePayload(response) {
    return response.text().then(function (text) {
      if (!text) return {};
      try {
        return JSON.parse(text);
      } catch (e) {
        return { message: text };
      }
    });
  }

  function showActionError(error) {
    if (!window.ErpModal) return;
    var message = error && error.message ? error.message : "Action failed. Please try again.";
    if (error && error.status && error.status < 500 && ErpModal.showWarning) {
      ErpModal.showWarning(message);
      return;
    }
    ErpModal.showError(message);
  }

  function hideReversalError() {
    var alert = document.getElementById("journal-reversal-error");
    if (!alert) return;
    alert.textContent = "";
    alert.classList.add("d-none");
  }

  function showReversalError(error) {
    var alert = document.getElementById("journal-reversal-error");
    var message = error && error.message ? error.message : "Action failed. Please try again.";
    if (!alert) {
      showActionError(error);
      return;
    }
    alert.textContent = message;
    alert.classList.remove("d-none");
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
      return parsePayload(response).then(function (payload) {
        if (!response.ok || payload.success === false) {
          throw actionError(payload.message || response.statusText, response.status);
        }
        return payload;
      });
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
              showActionError(error);
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
      reverseButton.addEventListener("click", function () {
        hideReversalError();
        modal.show();
      });
    }

    if (confirmButton) {
      confirmButton.addEventListener("click", function () {
        var date = document.getElementById("reversal-posting-date").value;
        hideReversalError();
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
            showReversalError(error);
          });
      });
    }
  });
})();
