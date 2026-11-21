/*
 * Student name: YOUR NAME
 * Student number: YOUR STUDENT NUMBER
 * Submission date: YYYY-MM-DD
 */
"use strict";

const api = document.getElementById("product-page").dataset.api;
const form = document.getElementById("product-form");
const rows = document.getElementById("product-rows");
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
    formTitle.textContent = "Create product";
}

function editProduct(product) {
    for (const name of [
        "id", "productName", "category", "price",
        "stockQuantity", "sellerId"
    ]) {
        field(name).value = product[name] ?? "";
    }

    field("availability").checked = product.availability;
    formTitle.textContent = `Edit product ${product.id}`;
    field("productName").focus();
}

async function deleteProduct(product) {
    if (!confirm(`Delete product "${product.productName}"?`)) {
        return;
    }

    try {
        await request(`${api}/${product.id}`, { method: "DELETE" });

        if (field("id").value === String(product.id)) {
            clearForm();
        }

        message.textContent = "Product deleted.";
        await loadProducts();
    } catch (error) {
        message.textContent = error.message;
    }
}

async function loadProducts() {
    const products = await request(api);
    rows.replaceChildren();

    if (products.length === 0) {
        const cell = rows.insertRow().insertCell();
        cell.colSpan = 8;
        cell.textContent = "No products found.";
        return;
    }

    for (const product of products) {
        const row = rows.insertRow();

        for (const name of [
            "id", "productName", "category", "price",
            "stockQuantity", "availability", "sellerId"
        ]) {
            row.insertCell().textContent = product[name] ?? "";
        }

        const actions = row.insertCell();

        const edit = document.createElement("button");
        edit.type = "button";
        edit.textContent = "Edit";
        edit.addEventListener("click", () => editProduct(product));

        const remove = document.createElement("button");
        remove.type = "button";
        remove.textContent = "Delete";
        remove.addEventListener("click", () => deleteProduct(product));

        actions.append(edit, " ", remove);
    }
}

form.addEventListener("submit", async (event) => {
    event.preventDefault();

    const id = field("id").value;
    const product = {
        productName: field("productName").value.trim(),
        category: field("category").value.trim(),
        price: Number(field("price").value),
        stockQuantity: Number(field("stockQuantity").value),
        availability: field("availability").checked,
        sellerId: Number(field("sellerId").value)
    };

    const saveButton = form.querySelector('[type="submit"]');
    saveButton.disabled = true;

    try {
        await request(id ? `${api}/${id}` : api, {
            method: id ? "PUT" : "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(product)
        });

        clearForm();
        message.textContent = id ? "Product updated." : "Product created.";
        await loadProducts();
    } catch (error) {
        message.textContent = error.message;
    } finally {
        saveButton.disabled = false;
    }
});

document.getElementById("clear-form")
    .addEventListener("click", clearForm);

document.getElementById("refresh").addEventListener("click", () => {
    loadProducts().catch(error => {
        message.textContent = error.message;
    });
});

loadProducts().catch(error => {
    message.textContent = error.message;
});