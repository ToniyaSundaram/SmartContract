package com.valuemanagement.state


import com.valuemanagement.model.ValueContractTransactionVO
import com.valuemanagement.schema.ValueArticulationSchemaV1
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import com.valuemanagement.schema.PresisentValueContractTransaction
import java.util.*

/**
 * The state object recording Value Contract agreements between two parties.
 *
 * A state must implement [ContractState] or one of its descendants.
 *
 * @param ValueContractTransactionVO the value of the IOU.
 * @param InitiatorCognizant the party initiating the Value Contract.
 * @param Acceptor the list of parties to authorizing and accepting the Value Contract.
 */

data class ValueContractTransactionState(val valueContractTransactionVO: ValueContractTransactionVO,
                                         val initiator: Party,
                                         val acceptor: List<Party>,
                                         override val linearId: UniqueIdentifier = UniqueIdentifier()):
        LinearState, QueryableState {
    /** The public keys of the involved parties. */
    override val participants: List<AbstractParty> get() = listOf(initiator) + acceptor;

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is ValueArticulationSchemaV1 -> PresisentValueContractTransaction(
                    this.valueContractTransactionVO.auvId.toString(),
                    this.linearId.id,
                    this.valueContractTransactionVO.serviceCreditId,
                    this.valueContractTransactionVO.projectId,
                    this.valueContractTransactionVO.lob.toString(),
                    this.valueContractTransactionVO.leverCategory.toString(),
                    this.valueContractTransactionVO.valueImprovementProgram.toString(),
                    this.valueContractTransactionVO.valueCategory.toString(),
                    this.valueContractTransactionVO.theme.toString(),
                    this.valueContractTransactionVO.valueAddDescription.toString(),
                    this.valueContractTransactionVO.agreedServiceCredits,
                    this.valueContractTransactionVO.revisedServiceCredits,
                    this.valueContractTransactionVO.attachments.toString(),
                    this.valueContractTransactionVO.implementationDate as Date,
                    this.valueContractTransactionVO.internalComments.toString(),
                    this.valueContractTransactionVO.customerComments.toString(),
                    this.valueContractTransactionVO.status.toString(),
                    this.valueContractTransactionVO.isimplemented ,
                    this.valueContractTransactionVO.isbeyondcommited ,
                    this.valueContractTransactionVO.projectScope.toString(),
                    this.valueContractTransactionVO.transactionapproverName.toString()

            )
            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }
    }

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(ValueArticulationSchemaV1)
}

