package com.valuemanagement

import net.corda.core.serialization.SerializationWhitelist
import net.corda.core.transactions.TransactionBuilder

import java.util.*
import javax.persistence.Id

class Plugin : SerializationWhitelist {
    override val whitelist: List<Class<*>>
        get() = listOf(
                Date::class.java,
                TransactionBuilder::class.java,
                Id::class.java
        )
}