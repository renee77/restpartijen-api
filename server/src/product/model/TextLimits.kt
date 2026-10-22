package com.restpartijen.api.product.model

/**
 * The longest name or other text a product may have (§10.2).
 * Matches the varchar(255) columns in ProductsTable; a longer text would fail on INSERT with a 500.
 * Here, not in dto, because the request validator and the barcode lookup both need it (US-01, US-02).
 */
const val MAX_TEXT_LENGTH = 255
