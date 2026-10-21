package com.restpartijen.api.product.repository

import com.restpartijen.api.persistence.Repository
import com.restpartijen.api.product.model.SurplusProduct

/**
 * Persistence for surplus products (GI-1, decision 1.8).
 * The five standard operations come from Repository; the product-specific queries are declared here.
 */
interface ProductRepository : Repository<SurplusProduct> {

    /** The current offer of [supplierId]: its LISTED and RESERVED products, ordered by id (A4, US-03). */
    suspend fun findBySupplier(supplierId: Long): List<SurplusProduct>
}