"use strict";

/* ---------------------------------------------------------------- public */

(function () {
    var form = document.getElementById("ask-form");
    var result = document.getElementById("ask-result");
    var msg = document.getElementById("ask-message");
    var track = document.getElementById("ask-track");
    var errorEl = document.getElementById("ask-error");
    if (!form) return;

    form.addEventListener("submit", function (e) {
        e.preventDefault();
        errorEl.textContent = "";
        track.textContent = "";
        result.classList.add("hidden");

        var body = {
            question: document.getElementById("question").value,
            askerName: document.getElementById("askerName").value || null,
            askerEmail: document.getElementById("askerEmail").value || null,
            category: document.getElementById("category").value || null
        };

        fetch("/api/ama/ask", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(body)
        })
            .then(function (res) {
                return res.json().then(function (data) { return { ok: res.ok, data: data }; });
            })
            .then(function (out) {
                if (!out.ok) {
                    errorEl.textContent = out.data.error || "Something went wrong.";
                    return;
                }
                result.classList.remove("hidden");
                msg.textContent = out.data.message || "Question received.";
                if (out.data.answer) {
                    msg.textContent = out.data.answer;
                    return;
                }
                track.textContent = "Track your question: reference " + out.data.reference;
                poll(out.data.reference);
            })
            .catch(function () { errorEl.textContent = "Network error. Please try again."; });
    });

    function poll(reference) {
        var tries = 0;
        var timer = setInterval(function () {
            if (tries++ > 20) { clearInterval(timer); track.textContent = "Still pending — check back later."; return; }
            fetch("/api/ama/questions/" + reference)
                .then(function (res) { return res.json(); })
                .then(function (data) {
                    if (data.answer) {
                        clearInterval(timer);
                        track.textContent = "";
                        msg.textContent = data.answer;
                    } else {
                        track.textContent = "Status: " + data.status + " — we'll update you here.";
                    }
                })
                .catch(function () { });
        }, 4000);
    }
})();

/* ---------------------------------------------------------------- admin */

var currentStatus = "";
var questionsCache = [];

function setStatusTab(status) {
    currentStatus = status;
    document.querySelectorAll(".tab").forEach(function (b) {
        b.classList.toggle("active", b.dataset.status === status);
    });
}

function loadAdmin(status) {
    setStatusTab(status || "");
    currentStatus = status || "";
    var url = "/api/ama/admin/questions?size=100";
    if (currentStatus) url += "&status=" + currentStatus;
    fetch(url)
        .then(function (r) { return r.json(); })
        .then(function (data) {
            questionsCache = data.items || [];
            renderQuestions();
        })
        .catch(function () {
            document.getElementById("question-list").innerHTML =
                '<p class="error">Failed to load. Are you signed in?</p>';
        });
}

function renderQuestions() {
    var el = document.getElementById("question-list");
    if (!el) return;
    if (!questionsCache.length) { el.innerHTML = '<p class="muted">Nothing here.</p>'; return; }

    el.innerHTML = questionsCache.map(function (q) {
        var a = q.answer ? '<div class="a">' + escapeHtml(q.answer.content) + "</div>" : "";
        var meta = q.askerName || q.askerEmail ? "from " + escapeHtml(q.askerName || q.askerEmail)
            + (q.askerEmail ? " <" + escapeHtml(q.askerEmail) + ">" : "") + " · " : "";
        return '<div class="qitem">'
            + "<div class=\"q\">" + escapeHtml(q.question) + "</div>"
            + '<div class="meta"><span class="badge ' + q.status + '">' + q.status + "</span> · "
            + meta + q.mode + "</div>"
            + a
            + '<div class="actions">'
            + (q.status === "DRAFT" ? '<button class="small secondary" onclick="approve(\'' + q.id + '\')">Approve draft</button>' : "")
            + '<button class="small" onclick="manualAnswer(\'' + q.id + '\')">Write answer</button>'
            + '<button class="small secondary" onclick="act(\'' + q.id + '\',\'reject\')">Reject</button>'
            + '<button class="small secondary" onclick="act(\'' + q.id + '\',\'archive\')">Archive</button>'
            + '<button class="small danger" onclick="act(\'' + q.id + '\',\'delete\')">Delete</button>'
            + "</div></div>";
    }).join("");
}

function approve(id) {
    fetch("/api/ama/admin/questions/" + id + "/approve", { method: "POST" })
        .then(function (r) { return r.json(); })
        .then(function () { loadAdmin(currentStatus); })
        .catch(function () { alert("Approve failed."); });
}

function manualAnswer(id) {
    var content = prompt("Write the answer to publish:");
    if (!content) return;
    fetch("/api/ama/admin/questions/" + id + "/answer", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ content: content, approvedBy: "admin" })
    })
        .then(function (r) { return r.json(); })
        .then(function () { loadAdmin(currentStatus); })
        .catch(function () { alert("Publishing failed."); });
}

function act(id, action) {
    var method = action === "delete" ? "DELETE" : "POST";
    var url = "/api/ama/admin/questions/" + id + (action === "delete" ? "" : "/" + action);
    fetch(url, { method: method })
        .then(function () { loadAdmin(currentStatus); })
        .catch(function () { alert("Action failed."); });
}

/* ---------------------------------------------------------------- knowledge */

function loadKnowledge() {
    var el = document.getElementById("knowledge-list");
    if (!el) return;
    fetch("/api/ama/admin/knowledge")
        .then(function (r) { return r.json(); })
        .then(function (items) {
            if (!items.length) { el.innerHTML = '<p class="muted">No entries yet.</p>'; return; }
            el.innerHTML = items.map(function (k) {
                return '<div class="qitem">'
                    + '<div class="q">' + escapeHtml(k.question) + "</div>"
                    + '<div class="meta">' + escapeHtml(k.category || "general")
                    + (k.active ? "" : " · inactive") + "</div>"
                    + '<div class="a">' + escapeHtml(k.answer) + "</div>"
                    + '<div class="actions"><button class="small danger" onclick="deleteKnowledge(\'' + k.id + '\')">Delete</button></div>'
                    + "</div>";
            }).join("");
        })
        .catch(function () { el.innerHTML = '<p class="error">Failed to load knowledge base.</p>'; });
}

function deleteKnowledge(id) {
    if (!confirm("Delete this knowledge entry?")) return;
    fetch("/api/ama/admin/knowledge/" + id, { method: "DELETE" })
        .then(function () { loadKnowledge(); })
        .catch(function () { alert("Delete failed."); });
}

(function () {
    var form = document.getElementById("knowledge-form");
    if (!form) return;
    form.addEventListener("submit", function (e) {
        e.preventDefault();
        var body = {
            category: document.getElementById("kbCat").value.trim() || "general",
            question: document.getElementById("kbQ").value.trim(),
            answer: document.getElementById("kbA").value.trim(),
            keywords: document.getElementById("kbKw").value.split(",").map(function (s) { return s.trim(); }).filter(Boolean)
        };
        fetch("/api/ama/admin/knowledge", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(body)
        })
            .then(function (r) { return r.json(); })
            .then(function () {
                form.reset();
                loadKnowledge();
            })
            .catch(function () { alert("Save failed."); });
    });
})();

document.addEventListener("DOMContentLoaded", function () {
    if (document.getElementById("question-list")) loadAdmin(currentStatus);
    loadKnowledge();
});

/* ---------------------------------------------------------------- util */

function escapeHtml(s) {
    return String(s == null ? "" : s)
        .replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;").replace(/'/g, "&#39;");
}