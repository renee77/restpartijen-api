package com.restpartijen.api.persistence

import org.jetbrains.exposed.v1.core.Table

// Minimal table for the persistence tests, so persistence is tested without depending on a feature.
internal object TestTable : Table("test_rows") {
    val id = long("id").autoIncrement()
    val text = varchar("text", length = 50)
    override val primaryKey = PrimaryKey(id)
}
