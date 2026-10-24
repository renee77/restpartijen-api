package com.restpartijen.api.testsupport

/**
 * A real Open Food Facts "product found" response for barcode 3017620422003 (Nutella), from
 * GET /api/v3/product/3017620422003?fields=product_name,allergens_tags on 22 Oct 2026.
 * The pieces are split before a `{` or `[`; together they form the exact response body.
 */
const val NUTELLA_BODY =
    """{"code":"3017620422003","errors":[],"product":""" +
        """{"allergens_tags":["en:milk","en:nuts","en:soybeans"],"product_name":"Nutella"},"result":""" +
        """{"id":"product_found","lc_name":"Product found","name":"Product found"},"status":"success","warnings":[]}"""
