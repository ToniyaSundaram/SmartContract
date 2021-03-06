package com.valuemanagement.flow

import co.paralleluniverse.fibers.Suspendable
import com.valuemanagement.contract.ValueTransactionContract
import com.valuemanagement.flow.ApproveValueTransactionFlow.Acceptor
import com.valuemanagement.model.ValueContractTransactionVO
import com.valuemanagement.schema.PresisentValueContractTransaction
import com.valuemanagement.state.ValueContractTransactionState
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
 * This flow allows two parties (the [Initiator] and the [Acceptor]) to come to an agreement about the Value Contract for respective Service Credit encapsulated
 * within an [ValueContractTransactionState].
 *
 * The [Acceptor] always accepts a valid Value Contract.
 *
 * These flows have deliberately been implemented by using only the call() method for ease of understanding. In
 * practice we would recommend splitting up the various stages of the flow into sub-routines.
 *
 * All methods called within the [FlowLogic] sub-class need to be annotated with the @Suspendable annotation.
 */

object ApproveValueTransactionFlow {
    @InitiatingFlow
    @StartableByRPC
    class ValueContractApprover(val valueContractTransactionVO : ValueContractTransactionVO,
                                val initiatorCognizant: Party,
                                val authorizerCognizant: Party,
                                val operationalCustomer: Party,
                                val counterParties:ArrayList<Party>): FlowLogic<SignedTransaction>() {
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
            // Generate an unsigned transaction.s

            val expression = builder { PresisentValueContractTransaction::linearId.equal(valueContractTransactionVO.valueContractTransactionId) }

            val qryCriteria = QueryCriteria.VaultCustomQueryCriteria(expression)
            val qryCriteriaUnconsumed = QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED)
            val vaultState = serviceHub.vaultService.queryBy<ValueContractTransactionState>(qryCriteria.and(qryCriteriaUnconsumed)).states.singleOrNull()

            // val vaultNewState = vaultState?.state?.data?.copy(status = "Authorized")
            val valueContractTransactionState = ValueContractTransactionState(valueContractTransactionVO,operationalCustomer,listOf(authorizerCognizant,initiatorCognizant,counterParties[0],counterParties[1],counterParties[2]));
            valueContractTransactionVO.valueContractTransactionId = valueContractTransactionState.linearId.id
            val txCommand = Command(ValueTransactionContract.Commands.Approve(), valueContractTransactionState.participants.map { it.owningKey })

            val txBuilder = TransactionBuilder(notary).withItems(StateAndContract(valueContractTransactionState, ValueTransactionContract.VALUE_TRANSACTION_CONTRACT_ID), txCommand)

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
            val authorizercognizant = initiateFlow(authorizerCognizant)

            val initiatorcognizant = initiateFlow(initiatorCognizant)

            val strategiccognizant = initiateFlow(counterParties[0])

            val strategiccustomer = initiateFlow(counterParties[1])

            val tacticalcustomer=initiateFlow(counterParties[2])

            progressTracker.currentStep = GATHERING_SIGS
            // Send the state to the counterparty, and receive it back with their signature.
            val fullySignedTx = subFlow(CollectSignaturesFlow(partSignedTx, setOf(authorizercognizant,initiatorcognizant,strategiccognizant,strategiccustomer,tacticalcustomer), GATHERING_SIGS.childProgressTracker()))

            // Stage 5.
            progressTracker.currentStep = FINALISING_TRANSACTION
            // Notarise and record the transaction in both parties' vaults.

            return subFlow(FinalityFlow(fullySignedTx, FINALISING_TRANSACTION.childProgressTracker()))

        }
    }

    @InitiatedBy(ValueContractApprover::class)
    class Acceptor(val otherPartyFlow: FlowSession) : FlowLogic<SignedTransaction>() {
        @Suspendable
        override fun call(): SignedTransaction {
            val signTransactionFlow = object : SignTransactionFlow(otherPartyFlow) {
                override fun checkTransaction(stx: SignedTransaction) = requireThat {
                    val output = stx.tx.outputs.single().data
                    "This must be an Service Credit transaction." using (output is ValueContractTransactionState)
                    val valueContractCredits = output as ValueContractTransactionState
                    "The ServiceCredit's value can't be too high." using (valueContractCredits.valueContractTransactionVO.agreedServiceCredits < 99999)
                }
            }

            return subFlow(signTransactionFlow)
        }
    }
}