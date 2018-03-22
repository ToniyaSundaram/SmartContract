/*
 * Copyright (c) 2017. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.valuemanagement.contract

import com.valuemanagement.state.CreateServiceCreditState
import com.valuemanagement.validation.ValueArticulationValidation
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction

/**
 * A implementation of a basic smart contract in Corda.

 * This contract enforces rules regarding the creation of a valid [CreateServiceCreditState], which in turn encapsulates an [ServiceCredit].

 * For a new [ServiceCredit] to be issued onto the ledger, a transaction is required which takes:
 * - Zero input states.
 * - One output state: the new [ServiceCredit].
 * - An Create() command with the public keys of both the lender and the borrower.
 *
 * All contracts must sub-class the [Contract] interface.
 */
open class ServiceCreditContract : Contract {
    companion object {
        @JvmStatic
        val SERVICE_CREDIT_CONTRACT_ID = "com.valuemanagement.contract.ServiceCreditContract"
    }

    /**
     * The verify() function of all the states' contracts must not throw an exception for a transaction to be
    considered valid.

     * Depends on the contract commands call, it ll do some constraint validations.


     */


    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<ServiceCreditContract.Commands>()

        if(command.value is Commands.Create){

            requireThat {
                // Generic constraints around the Service Credit transaction.

                "No inputs should be consumed when issuing an Service Credit." using (tx.inputs.isEmpty())

                "Only one output state should be created." using (tx.outputs.size == 1)

                val out = tx.outputsOfType<CreateServiceCreditState>().single()

                "Only SDM can create Smart Contract" using (out.initiator != out.acceptor.get(0))

                "All of the participants must be signers." using (command.signers.containsAll(out.participants.map { it.owningKey }))

                // Service Credits-specific constraints.
                if(out.serviceCreditVO.serviceCreditCommitted)
                "The Service Credit's value must be non-negative." using (out.serviceCreditVO.serviceCredit > 0)

                val d= ValueArticulationValidation()

                "End Year should be future Date." using (d.validateEndDate(out.serviceCreditVO.endYear)==true)

            }
        }
        else if (command.value is Commands.Update) {
            requireThat {
                // Generic constraints around the Service Credit transaction.

                "Inputs should be consumed when Authorizing a Service Credit." using (tx.inputs.isNotEmpty())

                val out = tx.outputsOfType<CreateServiceCreditState>().single()

                "Only SDD/Bu Head/Cp can authorize the Smart Contract." using ((out.initiator != out.acceptor.get(0)) && (out.initiator != out.acceptor.get(1)))

                "All of the participants must be signers." using (command.signers.containsAll(out.participants.map { it.owningKey }))

                // Service Credits-specific constraints.

                "The Service Credit's value must be non-negative." using (out.serviceCreditVO.serviceCredit > 0 && out.serviceCreditVO.serviceCredit <=99999)

            }
        }
        else if (command.value is Commands.Approve) {
            requireThat {
                // Generic constraints around the Service Credit transaction.

                "Inputs should be consumed when approving a Service Credit." using (tx.inputs.isNotEmpty())

                val out = tx.outputsOfType<CreateServiceCreditState>().single()

                "Only Customer can approve the Smart Contract." using ((out.initiator != out.acceptor.get(0)) && (out.initiator != out.acceptor.get(1)))

                "All of the participants must be signers." using (command.signers.containsAll(out.participants.map { it.owningKey }))

                // Service Credits-specific constraints.

                "The Service Credit's value must be non-negative." using (out.serviceCreditVO.serviceCredit> 0)
            }
        } // Iteration 2 change
        // Revise AUV --toniya
        else if (command.value is Commands.Revise) {
            requireThat {
                // Generic constraints around the Service Credit transaction.

                "Inputs should be consumed when approving a Service Credit." using (tx.inputs.isNotEmpty())

                val out = tx.outputsOfType<CreateServiceCreditState>().single()

                "Only SDM can create Smart Contract" using (out.initiator != out.acceptor.get(0))

                "All of the participants must be signers." using (command.signers.containsAll(out.participants.map { it.owningKey }))

                // Service Credits-specific constraints.

                "The Service Credit's value must be non-negative." using (out.serviceCreditVO.serviceCredit > 0)

                val d= ValueArticulationValidation()

                "End Year should be future Date." using (d.validateEndDate(out.serviceCreditVO.endYear)==true)
            }
        } // Revise AUV --toniya
        else if (command.value is Commands.Implement) {
            requireThat {
                // Generic constraints around the Service Credit transaction.

                "Inputs should be consumed when implementing a Service Credit." using (tx.inputs.isNotEmpty())

                val out = tx.outputsOfType<CreateServiceCreditState>().single()

                "Only SDM can create a implemented Smart Contract" using (out.initiator != out.acceptor.get(0))

                "All of the participants must be signers." using (command.signers.containsAll(out.participants.map { it.owningKey }))

                // Service Credits-specific constraints.

                "The Service Credit's value must be non-negative." using (out.serviceCreditVO.serviceCredit > 0)

                val d= ValueArticulationValidation()

                "End Year should be future Date." using (d.validateEndDate(out.serviceCreditVO.endYear)==true)
            }
        } // Iteration 2 change
    }

    /**
     * This contract implements commands, Create, Update, Approve.
     */
    interface Commands : CommandData {
        class Create : Commands
        class Update : Commands
        class Approve : Commands
        class Implement : Commands // Iteration 2 change
        class Revise : Commands // Revise AUV -- toniya
    }
}
