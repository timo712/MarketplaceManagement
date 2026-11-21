
"use strict";

const api = document.getElementById("order-page").dataset.api;
const form = document.getElementById("order-form");
const rows = document.getElementById("order-rows");
const message = document.getElementById("message");
const formTitle = document.getElementById("form-title");

function field(name) {
    return form.elements.namedItem(name);
}

async function request(url, options = {}) {
    const response = await fetch(url, options);

    if (!response.ok) {
        const error = await response.json().catch(() => ({}));
        throw new Error(
            error.message || error.detail ||
            `Request failed: ${response.status}`
        );
    }

    return response.status === 204 ? null : response.json();
}

function clearForm() {
    form.reset();
    field("id").value = "";
    field("productId").readOnly = false;
    field("quantity").readOnly = false;
    formTitle.textContent = "Create order";
}

function editOrder(order) {
    for (const name of [
        "id", "customerName", "customerEmail",
        "productId", "quantity", "orderStatus"
    ]) {
        field(name).value = order[name] ?? "";
    }

    // Match the backend rule: product and quantity stay fixed.
    field("productId").readOnly = true;
    field("quantity").readOnly = true;

    formTitle.textContent = `Edit order ${order.id}`;
    field("customerName").focus();
}

async function deleteOrder(order) {
    if (!confirm(
        `Delete order ${order.id}? Stock will not be restored.`
    )) {
        return;
    }

    try {
        await request(`${api}/${order.id}`, { method: "DELETE" });

        if (field("id").value === String(order.id)) {
            clearForm();
        }

        message.textContent = "Order deleted.";
        await loadOrders();
    } catch (error) {
        message.textContent = error.message;
    }
}

async function loadOrders() {
    const orders = await request(api);
    rows.replaceChildren();

    if (orders.length === 0) {
        const cell = rows.insertRow().insertCell();
        cell.colSpan = 8;
        cell.textContent = "No orders found.";
        return;
    }

    for (const order of orders) {
        const row = rows.insertRow();

        for (const name of [
            "id", "customerName", "customerEmail",
            "productId", "quantity", "orderStatus"
        ]) {
            row.insertCell().textContent = order[name] ?? "";
        }

        row.insertCell().textContent = order.orderTime
            ? new Date(order.orderTime).toLocaleString()
            : "";

        const actions = row.insertCell();

        const edit = document.createElement("button");
        edit.type = "button";
        edit.textContent = "Edit";
        edit.addEventListener("click", () => editOrder(order));

        const remove = document.createElement("button");
        remove.type = "button";
        remove.textContent = "Delete";
        remove.addEventListener("click", () => deleteOrder(order));

        actions.append(edit, " ", remove);
    }
}

form.addEventListener("submit", async (event) => {
    event.preventDefault();

    const id = field("id").value;
    const order = {
        customerName: field("customerName").value.trim(),
        customerEmail: field("customerEmail").value.trim(),
        productId: Number(field("productId").value),
        quantity: Number(field("quantity").value),
        orderStatus: field("orderStatus").value
    };

    const saveButton = form.querySelector('[type="submit"]');
    saveButton.disabled = true;

    try {
        const saved = await request(id ? `${api}/${id}` : api, {
            method: id ? "PUT" : "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(order)
        });

        clearForm();
        message.textContent = id
            ? "Order updated."
            : `Order ${saved.id} created. Product stock reduced.`;

        await loadOrders();
    } catch (error) {
        message.textContent = error.message;
    } finally {
        saveButton.disabled = false;
    }
});

document.getElementById("clear-form")
    .addEventListener("click", clearForm);

document.getElementById("refresh").addEventListener("click", () => {
    loadOrders().catch(error => {
        message.textContent = error.message;
    });
});

loadOrders().catch(error => {
    message.textContent = error.message;
});