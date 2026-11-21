
"use strict";

const api = document.getElementById("seller-page").dataset.api;
const form = document.getElementById("seller-form");
const rows = document.getElementById("seller-rows");
const message = document.getElementById("message");
const formTitle = document.getElementById("form-title");

function field(name) {
    return form.elements.namedItem(name);
}

// Send requests to the same REST API tested in Postman.
async function request(url, options = {}) {
    const response = await fetch(url, options);

    if (!response.ok) {
        const error = await response.json().catch(() => ({}));
        throw new Error(
            error.message || error.detail ||
            `Request failed: ${response.status}`
        );
    }

    if (response.status === 204) {
        return null;
    }

    return response.json();
}

function clearForm() {
    form.reset();
    field("id").value = "";
    formTitle.textContent = "Create seller";
}

function editSeller(seller) {
    for (const name of [
        "id", "sellerName", "email", "businessType", "city", "rating"
    ]) {
        field(name).value = seller[name] ?? "";
    }

    formTitle.textContent = `Edit seller ${seller.id}`;
    field("sellerName").focus();
}

async function deleteSeller(seller) {
    if (!confirm(`Delete seller "${seller.sellerName}"?`)) {
        return;
    }

    try {
        await request(`${api}/${seller.id}`, { method: "DELETE" });

        if (field("id").value === String(seller.id)) {
            clearForm();
        }

        message.textContent = "Seller deleted.";
        await loadSellers();
    } catch (error) {
        message.textContent = error.message;
    }
}

async function loadSellers() {
    const sellers = await request(api);
    rows.replaceChildren();

    if (sellers.length === 0) {
        const row = rows.insertRow();
        const cell = row.insertCell();
        cell.colSpan = 7;
        cell.textContent = "No sellers found.";
        return;
    }

    for (const seller of sellers) {
        const row = rows.insertRow();

        // Use textContent so seller data is displayed as text, not HTML.
        for (const name of [
            "id", "sellerName", "email", "businessType", "city", "rating"
        ]) {
            row.insertCell().textContent = seller[name] ?? "";
        }

        const actions = row.insertCell();

        const edit = document.createElement("button");
        edit.type = "button";
        edit.textContent = "Edit";
        edit.addEventListener("click", () => editSeller(seller));

        const remove = document.createElement("button");
        remove.type = "button";
        remove.textContent = "Delete";
        remove.addEventListener("click", () => deleteSeller(seller));

        actions.append(edit, " ", remove);
    }
}

form.addEventListener("submit", async (event) => {
    event.preventDefault();

    const id = field("id").value;
    const seller = {
        sellerName: field("sellerName").value.trim(),
        email: field("email").value.trim(),
        businessType: field("businessType").value,
        city: field("city").value.trim(),
        rating: Number(field("rating").value)
    };

    const saveButton = form.querySelector('[type="submit"]');
    saveButton.disabled = true;

    try {
        await request(id ? `${api}/${id}` : api, {
            method: id ? "PUT" : "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(seller)
        });

        clearForm();
        message.textContent = id ? "Seller updated." : "Seller created.";
        await loadSellers();
    } catch (error) {
        message.textContent = error.message;
    } finally {
        saveButton.disabled = false;
    }
});

document.getElementById("clear-form")
    .addEventListener("click", clearForm);

document.getElementById("refresh").addEventListener("click", () => {
    loadSellers().catch(error => {
        message.textContent = error.message;
    });
});

loadSellers().catch(error => {
    message.textContent = error.message;
});