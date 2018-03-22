package com.valuemanagement.flow

import co.paralleluniverse.fibers.Suspendable
import com.valuemanagement.contract.ValueTransactionContract
import com.valuemanagement.flow.ExecuteValueTransactionFlow.Acceptor
import com.valuemanagement.model.ValueContractTransactionVO
import com.valuemanagement.schema.PersistentServiceCredits
import com.valuemanagement.schema.PresisentValueContractTransaction
import com.valuemanagement.state.CreateServiceCreditState
import com.valuemanagement.state.ValueContractTransactionState
import com.valuemanagement.validation.ValueArticulationValidation
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
 * This flow allows two parties (the [Initiator] and the [Acceptor]) to come to an agreement about the Value Contract for respective Service Credits encapsulated
 * within an [ValueContractTransactionState].
 *
 * This flow will go to Authorization only when the Lever Category is "Addition or Premium" and visible to [Initiator] and [AuthorizerCognizant].
 *
 * OtherWise it ll go to Acceptor directly visible to [Initiator], [AuthorizerCognizant], [OperationalCustomer].
 *
 * The [Acceptor] always accepts a valid Value Contract.
 *
 * These flows have deliberately been implemented by using only the call() method for ease of understanding. In
 * practice we would recommend splitting up the various stages of the flow into sub-routines.
 *
 * All methods called within the [FlowLogic] sub-class need to be annotated with the @Suspendable annotation.
 */

object ExecuteValueTransactionFlow {
    @InitiatingFlow
    @StartableByRPC
    class ValueContractInitiator(val valueContractTransactionVO: ValueContractTransactionVO,
                                 val initiatorCognizant: Party,
                                 val authorizerCognizant: Party,
                                 val operationalCustomer: Party,
                                 val counterParties:ArrayList<Party>
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
            // Obtain a reference to the notary we want to use.
            val notary = serviceHub.networkMapCache.notaryIdentities[0]
            // Stage 1.
            progressTracker.currentStep = GENERATING_TRANSACTION
            // Generate an unsigned transaction.

            // COMMENTED CODE TO BE REMOVED
            //Iteration 2 Changes Start Here

            // calling the function getbalanceservicecredits to calculate the total committed value
            /*val balance = getbalanceservicecredits(valueContractTransactionVO)
            val balanceauv = balance-valueContractTransactionVO.agreedServiceCredits */
           /* if(balanceauv<0){
                valueContractTransactionVO.isbeyondcommited=true
            }*/
            // COMMENTED CODE TO BE REMOVED

            //have to check approver for respective lob
            val valueTransactionState:ValueContractTransactionState
            if(valueContractTransactionVO.valueCategory!= "Retention" || valueContractTransactionVO.isbeyondcommited==true )
            {
                valueTransactionState = ValueContractTransactionState(valueContractTransactionVO, serviceHub.myInfo.legalIdentities.first(), listOf(authorizerCognizant,counterParties[0]));
            }
            else
            {
                valueTransactionState = ValueContractTransactionState(valueContractTransactionVO, serviceHub.myInfo.legalIdentities.first(), listOf(operationalCustomer,authorizerCognizant,counterParties[0],counterParties[1],counterParties[2]));
            }

            //Iteration Changes 2 Ends Here
            valueContractTransactionVO.valueContractTransactionId = valueTransactionState.linearId.id
            val txCommand = Command(ValueTransactionContract.Commands.Create(), valueTransactionState.participants.map { it.owningKey })
            val txBuilder = TransactionBuilder(notary).withItems(StateAndContract(valueTransactionState, ValueTransactionContract.VALUE_TRANSACTION_CONTRACT_ID), txCommand)

            // Stage 2.
            progressTracker.currentStep = VERIFYING_TRANSACTION
            // Verify that the transaction is valid.
            txBuilder.verify(serviceHub)

            // Stage 3.
            progressTracker.currentStep = SIGNING_TRANSACTION
            // Sign the transaction.
            val partSignedTx = serviceHub.signInitialTransaction(txBuilder)

            // Stage 4.

            val operationalcustomer = initiateFlow(operationalCustomer)

            val authorizercognizant = initiateFlow(authorizerCognizant)

            val strategiccognizant = initiateFlow(counterParties[0])

            val strategiccustomer = initiateFlow(counterParties[1])

            val tacticalcustomer=initiateFlow((counterParties[2]))

            progressTracker.currentStep = GATHERING_SIGS

            val fullySignedTx:SignedTransaction
            // Send the state to the counterparty, and receive it back with their signature.

            //Iteration 2 Changes Start Here
            if((null != valueContractTransactionVO.valueCategory && valueContractTransactionVO.valueCategory!= "Retention")|| valueContractTransactionVO.isbeyondcommited==true)
            {
                fullySignedTx = subFlow(CollectSignaturesFlow(partSignedTx, setOf(authorizercognizant,strategiccognizant), GATHERING_SIGS.childProgressTracker()))
            }
            else
            {

                fullySignedTx = subFlow(CollectSignaturesFlow(partSignedTx, setOf(operationalcustomer,authorizercognizant,strategiccognizant,strategiccustomer,tacticalcustomer), GATHERING_SIGS.childProgressTracker()))
            }
            //Iteration 2 Changes Ends Here

            // Stage 5.
            progressTracker.currentStep = FINALISING_TRANSACTION
            // Notarise and record the transaction in both parties' vaults.

            var extraParticipants: Set<Party> = setOf(operationalCustomer)
            return subFlow(FinalityFlow(fullySignedTx, extraParticipants, FINALISING_TRANSACTION.childProgressTracker()))

        }

        //Commented code  to be removed
        // function "getbalanceservicecredits' queries the committed service credits , queries all the approved value contract transactions
        // which will be the input for 'getbalance' function and returns the balance service credits

        /*fun getbalanceservicecredits(valueContractTransactionVO: ValueContractTransactionVO) : Int{

            val auvexpression = builder { PersistentServiceCredits::linearId.equal(valueContractTransactionVO.serviceCreditId) }
            val auvqryCriteria = QueryCriteria.VaultCustomQueryCriteria(auvexpression)

            val auvunconsumedqryCriteria = QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED)

            val auvvaultStateAndRef = serviceHub.vaultService.queryBy<CreateServiceCreditState>(auvqryCriteria.and(auvunconsumedqryCriteria)).states.singleOrNull()
            val baseservicecredits  = auvvaultStateAndRef!!.state.data.serviceCreditVO.serviceCredit

            val vctexpression= builder { PresisentValueContractTransaction::serviceCreditId.equal(valueContractTransactionVO.serviceCreditId)  }
            val vctqryCriteria = QueryCriteria.VaultCustomQueryCriteria(vctexpression)

            val vctapproved= builder { PresisentValueContractTransaction::status.equal("Approved")}
            val vctapprovedqryCriteria = QueryCriteria.VaultCustomQueryCriteria(vctapproved)

            val vctvaultStateAndRef = serviceHub.vaultService.queryBy<ValueContractTransactionState>(vctqryCriteria.and(vctapprovedqryCriteria)).states
            val vctvaultState  = vctvaultStateAndRef.map {  it.state.data.valueContractTransactionVO }

            var balanceServiceCredits=0
            var validationObj = ValueArticulationValidation()
            balanceServiceCredits = validationObj.getbalance(baseservicecredits, vctvaultState)
            return balanceServiceCredits
        }*/
        //Commented code  to be removed
        //Iteration 2 Changes Start Here
    }

    @InitiatedBy(ValueContractInitiator::class)
    class Acceptor(val otherPartyFlow: FlowSession) : FlowLogic<SignedTransaction>() {
        @Suspendable
        override fun call(): SignedTransaction {
            val signTransactionFlow = object : SignTransactionFlow(otherPartyFlow) {
                override fun checkTransaction(stx: SignedTransaction) = requireThat {
                    val output = stx.tx.outputs.single().data
                    "This must be an Value Contract transaction." using (output is ValueContractTransactionState)
                    val valueContractCredits = output as ValueContractTransactionState
                    "The ValueContractCredit's value can't be too high." using (valueContractCredits.valueContractTransactionVO.agreedServiceCredits < 99999)
                }
            }
            return subFlow(signTransactionFlow)
        }
    }
}