package com.restpartijen.api.persistence

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

/**
 * Runs [block] in one database transaction, off the threads that handle requests (GI-1, decisions 1.4 and 1.6).
 *
 * JDBC blocks the thread until the database answers, so the work moves to Dispatchers.IO.
 * Everything in [block] commits together, or rolls back together when an exception is thrown.
 * Use it once per service call; repositories called inside it join the same transaction.
 */
suspend fun <T> dbQuery(block: suspend () -> T): T =
    withContext(Dispatchers.IO) {
        suspendTransaction { block() }
    }
