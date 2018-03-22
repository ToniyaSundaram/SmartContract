package com.valuemanagement.contract



import com.valuemanagement.state.CreateServiceCreditState
import com.valuemanagement.state.ValueContractTransactionState
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.Requirements.using
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction

/**
 * It ll triggered from flow to enables function verify() to check the smart contract
 */

open class ValueTransactionContract: Contract {
    companion object {
        @JvmStatic
        val VALUE_TRANSACTION_CONTRACT_ID = "com.valuemanagement.contract.ValueTransactionContract"
    }

    /**
     * The verify() function of all the states' contracts must not throw an exception for a transaction to be
    considered valid.

     * Depends on the contract commands call, it ll do some constraint validations.


     */

    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<ValueTransactionContract.Commands>()

        if (command.value is ValueTransactionContract.Commands.Create) {

            requireThat {
                // Generic constraints around the Service Credit transaction.

                "No inputs should be consumed when issuing an Service Credit." using (tx.inputs.isEmpty())

                "Only one output state should be created." using (tx.outputs.size == 1)

                val out = tx.outputsOfType<ValueContractTransactionState>().single()

                "Only SDM can create Smart Contract" using (out.initiator != out.acceptor.get(0))

                "All of the participants must be signers." using (command.signers.containsAll(out.participants.map { it.owningKey }))

                // Service Credits-specific constraints.

                "The Service Credit's value must be non-negative." using (out.valueContractTransactionVO.agreedServiceCredits > 0)
            }
        }
        else if (command.value is ValueTransactionContract.Commands.Update) {
            requireThat {
                // Generic constraints around the Service Credit transaction.

                "Inputs should be consumed when issuing  Service Credit." using (tx.inputs.isNotEmpty())

                "Only one output state should be created." using (tx.outputs.size == 1)

                val out = tx.outputsOfType<ValueContractTransactionState>().single()

                "Only SDD/Bu Head/Cp can authorize the Smart Contract." using ((out.initiator != out.acceptor.get(0)) && (out.initiator != out.acceptor.get(1)))

                "All of the participants must be signers." using (command.signers.containsAll(out.participants.map { it.owningKey }))

                // Service Credits-specific constraints.

                "The Service Credit's value must be non-negative." using (out.valueContractTransactionVO.agreedServiceCredits > 0)
            }
        }
        else if (command.value is ValueTransactionContract.Commands.Approve) {
            requireThat {
                // Generic constraints around the Service Credit transaction.

                "Authorization Required." using (tx.inputs.isNotEmpty())

                "Only one output state should be created." using (tx.outputs.size == 1)

                val out = tx.outputsOfType<ValueContractTransactionState>().single()

                "Only Customer can approve the Smart Contract." using ((out.initiator != out.acceptor.get(0)) && (out.initiator != out.acceptor.get(1)))

                "All of the participants must be signers." using (command.signers.containsAll(out.participants.map { it.owningKey }))

                // Service Credits-specific constraints.

                "The Service Credit's value must be non-negative." using (out.valueContractTransactionVO.agreedServiceCredits> 0)
            }
        }

        else if (command.value is ValueTransactionContract.Commands.Revise) {
            requireThat {
                // Generic constraints around the Service Credit transaction.
                "Inputs should be consumed while implementing Service Credit." using (tx.inputs.isNotEmpty()) // iteration 4

                "Only one output state should be created." using (tx.outputs.size == 1)

                val out = tx.outputsOfType<ValueContractTransactionState>().single()

                "Only SDM can revise  Value Contract Transaction" using (out.initiator != out.acceptor.get(0))

                "All of the participants must be signers." using (command.signers.containsAll(out.participants.map { it.owningKey }))

                // Service Credits-specific constraints.

                "The Service Credit's value must be non-negative." using (out.valueContractTransactionVO.agreedServiceCredits > 0)
            }
        }


    }

    interface Commands : CommandData {
        class Create : Commands
        class Update : Commands
        class Approve : Commands
        class Revise : Commands
    }
}