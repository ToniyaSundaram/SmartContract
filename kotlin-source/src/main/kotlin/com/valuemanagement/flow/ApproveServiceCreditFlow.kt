/*
 * Copyright (c) 2017. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.valuemanagement.flow

import co.paralleluniverse.fibers.Suspendable
import com.valuemanagement.contract.ServiceCreditContract
import com.valuemanagement.flow.ApproveServiceCreditFlow.Acceptor
import com.valuemanagement.model.ServiceCreditVO
import com.valuemanagement.schema.PersistentServiceCredits
import com.valuemanagement.state.CreateServiceCreditState
import net.corda.core.contracts.Command
import net.corda.core.contracts.StateAndContract
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.Vault
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.builder
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

/**
 * This flow allows two parties (the [Initiator] and the [Acceptor]) to come to an agreement about the Service Credit encapsulated
 * within an [CreateServiceCreditState].
 *
 * The [Acceptor] always accepts a valid Service Credit.
 *
 * These flows have deliberately been implemented by using only the call() method for ease of understanding. In
 * practice we would recommend splitting up the various stages of the flow into sub-routines.
 *
 * All methods called within the [FlowLogic] sub-class need to be annotated with the @Suspendable annotation.
 */

object ApproveServiceCreditFlow {
    @InitiatingFlow
    @StartableByRPC
    class Approver(val serviceCreditVO: ServiceCreditVO,
                   val initiatorCognizant: Party,
                   val authorizerCognizant: Party,
                   val tacticalCustomer: Party,
                   val strategicCognizant: Party,
                   val strategicCustomer: Party
                   ) : FlowLogic<SignedTransaction>() {
        /**
         * The progress tracker checkpoints each stage of the flow and outputs the specified messages when each
         * checkpoint is reached in the code. See the 'progressTracker.currentStep' expressions within the call() function.
         */
        companion object {
            object GENERATING_TRANSACTION : ProgressTracker.Step("Generating transaction based on new ServiceCredit.")
            object VERIFYING_TRANSACTION : ProgressTracker.Step("Verifying contract constraints.")
            object SIGNING_TRANSACTION : ProgressTracker.Step("Signing transaction with our private key.")
            object GATHERING_SIGS : ProgressTracker.Step("Gathering the counterparty's signature.") {
                override fun childProgressTracker() = CollectSignaturesFlow.tracker()
            }

            object FINALISING_TRANSACTION : ProgressTracker.Step("Obtaining notary signature and recording transaction.") {
                override fun childProgressTracker() = FinalityFlow.tracker()
            }

            fun tracker() = ProgressTracker(
                    GENERATING_TRANSACTION,
                    VERIFYING_TRANSACTION,
                    SIGNING_TRANSACTION,
                    GATHERING_SIGS,
                    FINALISING_TRANSACTION
            )
        }

        override val progressTracker = tracker()

        /**
         * The flow logic is encapsulated within the call() method.
         */
        @Suspendable
        override fun call(): SignedTransaction {

            val notary = serviceHub.networkMapCache.notaryIdentities[0]
            // Stage 1.
            progressTracker.currentStep = GENERATING_TRANSACTION
            // Generate an unsigned transaction.

            val expression = builder { PersistentServiceCredits::linearId.equal(serviceCreditVO.serviceCreditId) }

            val qryCriteria = QueryCriteria.VaultCustomQueryCriteria(expression)
            val qryCriteriaUnconsumed = QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED)
            val vaultState = serviceHub.vaultService.queryBy<CreateServiceCreditState>(qryCriteria.and(qryCriteriaUnconsumed)).states.singleOrNull()

            // val vaultNewState = vaultState?.state?.data?.copy(status = "Authorized")
            val serviceCreditState = CreateServiceCreditState(serviceCreditVO,tacticalCustomer,listOf(authorizerCognizant,initiatorCognizant,strategicCognizant,strategicCustomer));
            serviceCreditVO.serviceCreditId = serviceCreditState.linearId.id
            val txCommand = Command(ServiceCreditContract.Commands.Approve(), serviceCreditState.participants.map { it.owningKey })
            val txBuilder = TransactionBuilder(notary).withItems(StateAndContract(serviceCreditState, ServiceCreditContract.SERVICE_CREDIT_CONTRACT_ID), txCommand)

            if(vaultState != null) {
                txBuilder.addInputState(vaultState)
            }

            // Stage 2.
            progressTracker.currentStep = VERIFYING_TRANSACTION
            // Verify that the transaction is valid.
            txBuilder.verify(serviceHub)

            // Stage 3.
            progressTracker.currentStep = SIGNING_TRANSACTION
            // Sign the transaction.
            val partSignedTx = serviceHub.signInitialTransaction(txBuilder)

            // Stage 4.
            val authorizer = initiateFlow(authorizerCognizant)

            val initiator = initiateFlow(initiatorCognizant)

            val strategiccognizant = initiateFlow(strategicCognizant)

            val strategiccustomer = initiateFlow(strategicCustomer)


            progressTracker.currentStep = GATHERING_SIGS
            // Send the state to the counterparty, and receive it back with their signature.
            val fullySignedTx = subFlow(CollectSignaturesFlow(partSignedTx, setOf(authorizer,initiator,strategiccognizant,strategiccustomer), GATHERING_SIGS.childProgressTracker()))

            // Stage 5.
            progressTracker.currentStep = FINALISING_TRANSACTION
            // Notarise and record the transaction in both parties' vaults.

            return subFlow(FinalityFlow(fullySignedTx, FINALISING_TRANSACTION.childProgressTracker()))

        }
    }

    @InitiatedBy(Approver::class)
    class Acceptor(val otherPartyFlow: FlowSession) : FlowLogic<SignedTransaction>() {
        @Suspendable
        override fun call(): SignedTransaction {
            val signTransactionFlow = object : SignTransactionFlow(otherPartyFlow) {
                override fun checkTransaction(stx: SignedTransaction) = requireThat {
                    val output = stx.tx.outputs.single().data
                    "This must be an Service Credit transaction." using (output is CreateServiceCreditState)
                    val serviceCredits = output as CreateServiceCreditState
                    //business validation
                    "The ServiceCredit's value can't be too high." using (serviceCredits.serviceCreditVO.serviceCredit < 99999)
                }
            }

            return subFlow(signTransactionFlow)
        }
    }


}