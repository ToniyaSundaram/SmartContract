/*
 * Copyright (c) 2017. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.valuemanagement.state


import com.valuemanagement.schema.ValueArticulationSchemaV1
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import com.valuemanagement.model.ServiceCreditVO
import com.valuemanagement.schema.PersistentServiceCredits
import java.sql.Timestamp
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * The state object recording Service Credit agreements between two parties.
 *
 * A state must implement [ContractState] or one of its descendants.
 *
 * @param ServiceCreditVO the value of the IOU.
 * @param InitiatorCognizant the party initiating the Service Credits.
 * @param Acceptor the list of parties to authorizing and accepting the Service Credits.
 */
data class CreateServiceCreditState(val serviceCreditVO: ServiceCreditVO,
                                    val initiator: Party,
                                    val acceptor: List<Party>,
                                    override val linearId: UniqueIdentifier = UniqueIdentifier()):
        LinearState, QueryableState {
    /** The public keys of the involved parties. */
    override val participants: List<AbstractParty> get() = listOf(initiator) + acceptor;

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is ValueArticulationSchemaV1 -> PersistentServiceCredits(
                    this.serviceCreditVO.auvId.toString(), //Revise AUV Toniya
                    this.initiator.name.toString(),
                    this.acceptor.toString(),
                    this.serviceCreditVO.businessUnit.toString(),
                    this.serviceCreditVO.accountName.toString(),
                    this.serviceCreditVO.projectId,
                    this.serviceCreditVO.projectName.toString(),
                    this.serviceCreditVO.customerName.toString(),
                    this.serviceCreditVO.lob.toString(),
                    this.serviceCreditVO.startYear.toString(),
                    this.serviceCreditVO.endYear.toString(),
                    this.serviceCreditVO.periodOfContract,
                    this.serviceCreditVO.serviceCredit,
                    this.serviceCreditVO.attachment.toString(),
                    // this.serviceCreditVO.dueDate as Date ,
                    this.serviceCreditVO.comments.toString(),
                    this.serviceCreditVO.internalComments.toString(),
                    this.serviceCreditVO.status.toString(),
                    this.serviceCreditVO.cognizantAuthorizers.toString(),
                    this.serviceCreditVO.customerApprovers.toString(),
                    this.serviceCreditVO.projectScope.toString(),//Iteration 2 changes -- TOniya
                    this.linearId.id
            )
            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }
    }

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(ValueArticulationSchemaV1)
}
