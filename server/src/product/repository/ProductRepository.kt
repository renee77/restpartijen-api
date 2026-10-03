package com.restpartijen.api.product.repository

import com.restpartijen.api.persistence.Repository
import com.restpartijen.api.product.model.Product

/**
 * Persistence for surplus products (GI-1, decision 1.8).
 * The five standard operations come from Repository; product-specific queries are added here later.
 */
interface ProductRepository : Repository<Product>
