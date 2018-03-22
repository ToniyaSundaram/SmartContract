/*
 * Copyright (c) 2017. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.valuemanagement.api

import com.valuemanagement.flow.ApproveServiceCreditFlow.Approver
import com.valuemanagement.flow.ApproveValueTransactionFlow.ValueContractApprover
import com.valuemanagement.flow.AuthorizeServiceCreditFlow.Authorizer
import com.valuemanagement.flow.AuthorizeValueTransactionFlow.ValueContractAuthorizer
import com.valuemanagement.flow.CreateServiceCreditFlow.Initiator
import com.valuemanagement.flow.ExecuteValueTransactionFlow.ValueContractInitiator
import com.valuemanagement.flow.ReviseServiceCreditFlow.RevisedInitiator
import com.valuemanagement.flow.ReviseValueTransactionFlow.RevisedValueContractApprover
import com.valuemanagement.model.ServiceCreditVO
import com.valuemanagement.model.ValueContractTransactionVO
import com.valuemanagement.schema.PersistentServiceCredits
import com.valuemanagement.schema.PresisentValueContractTransaction
import com.valuemanagement.state.CreateServiceCreditState
import com.valuemanagement.state.ValueContractTransactionState
import com.valuemanagement.validation.ValueArticulationValidation
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.FlowProgressHandle
import net.corda.core.messaging.startTrackedFlow
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.builder
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.getOrThrow
import net.corda.core.utilities.loggerFor
import org.slf4j.Logger
import java.io.File
import java.io.InputStream
import java.util.*
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status.BAD_REQUEST

val SERVICE_NAMES = listOf("Controller", "Network Map Service")

// This API is accessible from /api/valueArticulation. All paths specified below are relative to it.
@Path("valueArticulation")
class ValueArticulationApi(private val rpcOps: CordaRPCOps) {
    private val myLegalName: CordaX500Name = rpcOps.nodeInfo().legalIdentities.first().name

    companion object {
        private val logger: Logger = loggerFor<ValueArticulationApi>()
    }

    /**
     * Returns the node's name.
     */

    @GET
    @Path("me")
    @Produces(MediaType.APPLICATION_JSON)
    fun whoami() = mapOf("me" to myLegalName)

    /**
     * Returns all parties registered with the [NetworkMapService]. These names can be used to look up identities
     * using the [IdentityService].
     */

    @GET
    @Path("peers")
    @Produces(MediaType.APPLICATION_JSON)
    fun getPeers(): Map<String, List<CordaX500Name>> {
        val nodeInfo = rpcOps.networkMapSnapshot()
        return mapOf("peers" to nodeInfo
                .map { it.legalIdentities.first().name }
                //filter out myself, notary and eventual network map started by driver
                .filter { it.organisation !in (SERVICE_NAMES + myLegalName.organisation) })
    }

    /**
     * Displays all Service Credits states that exist in the node's vault.
     */



    @GET
    @Path("serviceCredits")
    @Produces(MediaType.APPLICATION_JSON)
    fun getIOUs(): List<ServiceCreditVO> {
        // Extract the IOUState StateAndRefs from the vault.
        val iouStateAndRefs = rpcOps.vaultQueryBy<CreateServiceCreditState>().states

        // Map each StateAndRef to its IOUState.
        val iouStates = iouStateAndRefs.map { it.state.data.serviceCreditVO }

        return iouStates
    }

    /**
     * Displays all Value Contract Transaction states that exist in the node's vault.
     */

    @GET
    @Path("ValueContract")
    @Produces(MediaType.APPLICATION_JSON)
    fun getIOU(): List<ValueContractTransactionVO> {
        // Extract the IOUState StateAndRefs from the vault.
        val iouStateAndRefs = rpcOps.vaultQueryBy<ValueContractTransactionState>().states

        // Map each StateAndRef to its IOUState.
        val iouStates = iouStateAndRefs.map { it.state.data.valueContractTransactionVO }

        return iouStates
    }


    /**
     * Opens the uploaded attachment
     */

    @GET
    @Path("viewattachment")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
            //fun getAttachment() = rpcOps.openAttachment(SecureHash.parse(attachmentHash))
    fun getAttachment(@QueryParam("attachmentHash") attachmentHash: String) : Response {
        val inputString = rpcOps.openAttachment(SecureHash.parse(attachmentHash)).bufferedReader().use { it.readText() }
        val file = File("OutputFile.txt")
        file.writeText(inputString)

        return Response.ok("File downloaded successfully in the path").build();
    }


    /**
     * Initiates a flow to agree an Service Credits between two parties.( SDM Cognizant and SDD Cognizant)
     *
     * Once the flow finishes it will have written the Service Credit to ledger. Both the InitiatorCognizant and the AuthorizerCognizant will be able to
     * see it when calling /api/valueArticulation/serviceCredits on their respective nodes.
     *
     * This end-point takes a Party name parameter as part of the path. If the serving node can't find the other party
     * in its network map cache, it will return an HTTP bad request.
     *
     * The flow is invoked asynchronously. It returns a future when the flow's call() method returns.
     */

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("create-serviceCredits")
    fun createServiceCredits(serviceCreditVO : ServiceCreditVO): Response {
        // Business validation starts here
        if (serviceCreditVO.serviceCredit <= 0 && serviceCreditVO.serviceCredit >= 99999) {
            return Response.status(BAD_REQUEST).entity("Query parameter 'serviceCredit' must be non-negative or greater than 99999.\n").build()
        }
        if(serviceCreditVO.status.equals("submit for authorization")) {
            serviceCreditVO.status=="submitted"
        }else {
            serviceCreditVO.status=="submitted"
        }

        // Business validation ends here
        serviceCreditVO.auvId = UUID.randomUUID().toString();
        val ctsAuthorizer = CordaX500Name("AuthorizerCognizant", "New York", "US")

        val authorizer = rpcOps.wellKnownPartyFromX500Name(ctsAuthorizer) ?:
        return Response.status(BAD_REQUEST).entity("Party named $ctsAuthorizer cannot be found.\n").build()



        val stratCognizant = CordaX500Name("StrategicCognizant", "London", "GB")

        val strategicCognizant = rpcOps.wellKnownPartyFromX500Name(stratCognizant) ?:
        return Response.status(BAD_REQUEST).entity("Party named $stratCognizant cannot be found.\n").build()


        //Attachment code starts here
        val path = serviceCreditVO.attachmentPath
        if (serviceCreditVO.attachmentPath == null || serviceCreditVO.attachmentPath == "" || serviceCreditVO.attachmentPath == " ") {
            return Response.status(BAD_REQUEST).entity(" 'AttachmentPath' not exist").build()}

        val pathName = path!!.substringBeforeLast(".")
        val fileName = pathName.substringAfterLast("/")

        if (fileName == "" || fileName == " ") {
            return Response.status(BAD_REQUEST).entity(" 'File' not exist").build()
        }
        val attachmentInputStream: InputStream = File(serviceCreditVO.attachmentPath).inputStream()
        val attachmentHash = rpcOps.uploadAttachment(attachmentInputStream).toString()
        serviceCreditVO.attachment = attachmentHash
        return try {
            val flowHandle = rpcOps.startTrackedFlow(::Initiator, serviceCreditVO, authorizer,strategicCognizant)
            flowHandle.progress.subscribe { println(">> $it") }
            // Extract the IOUState StateAndRefs from the vault.
            flowHandle.returnValue.getOrThrow()
            val iouStateAndRefs = rpcOps.vaultQueryBy<CreateServiceCreditState>().states
            // Map each StateAndRef to its IOUState.
            val iouStates = iouStateAndRefs.map { it.state.data.serviceCreditVO }
            Response.ok(iouStates.last(), MediaType.APPLICATION_JSON).build()
        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            Response.status(BAD_REQUEST).entity(ex.message!!).build()
        }
        //Attachment code ends here
    }

    /**
     * Initiates a flow to authorize a Service Credits between two parties.( SDD Cognizant and Customer)
     *
     * Once the flow finishes it will have written the Service Credit to ledger. Both the AuthorizerCognizant and the Customer will be able to
     * see it when calling /api/valueArticulation/serviceCredits on their respective nodes.
     *
     * This end-point takes a Party name parameter as part of the path. If the serving node can't find the other party
     * in its network map cache, it will return an HTTP bad request.
     *
     * The flow is invoked asynchronously. It returns a future when the flow's call() method returns.
     */

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("authorize-serviceCredits")
    fun authorizeServiceCredits(serviceCreditVO : ServiceCreditVO ): Response {
        // Business validation starts here
        // service credit validation
        if (serviceCreditVO.serviceCredit <= 0 && serviceCreditVO.serviceCredit >= 99999) {
            return Response.status(BAD_REQUEST).entity("Query parameter 'serviceCredit' must be non-negative or greater than 99999.\n").build()
        }

        // Status field validation
        if(serviceCreditVO.status.equals("submit for approval")) {
            serviceCreditVO.status=="Authorized"
        }
        // Business validation ends here

        val tactCustomer = CordaX500Name("TacticalCustomer", "Paris", "FR")

        val tacticalCustomer = rpcOps.wellKnownPartyFromX500Name(tactCustomer) ?:
        return Response.status(BAD_REQUEST).entity("Party named $tactCustomer cannot be found.\n").build()

        val ctsInitiator = CordaX500Name("InitiatorCognizant", "London", "GB")

        val initiator = rpcOps.wellKnownPartyFromX500Name(ctsInitiator) ?:
        return Response.status(BAD_REQUEST).entity("Party named $ctsInitiator cannot be found.\n").build()

        val ctsAuthorizer = CordaX500Name("AuthorizerCognizant", "New York", "US")

        val authorizer = rpcOps.wellKnownPartyFromX500Name(ctsAuthorizer) ?:
        return Response.status(BAD_REQUEST).entity("Party named $ctsAuthorizer cannot be found.\n").build()

        val stratCognizant = CordaX500Name("StrategicCognizant", "London", "GB")

        val strategicCognizant = rpcOps.wellKnownPartyFromX500Name(stratCognizant) ?:
        return Response.status(BAD_REQUEST).entity("Party named $stratCognizant cannot be found.\n").build()

        val stratCustomer = CordaX500Name("StrategicCustomer", "Paris", "FR")

        val strategicCustomer = rpcOps.wellKnownPartyFromX500Name(stratCustomer) ?:
        return Response.status(BAD_REQUEST).entity("Party named $stratCustomer cannot be found.\n").build()


        return try {
            val flowHandle = rpcOps.startTrackedFlow(::Authorizer, serviceCreditVO, initiator ,authorizer ,tacticalCustomer,strategicCognizant,strategicCustomer)
            flowHandle.progress.subscribe { println(">> $it") }
            flowHandle.returnValue.getOrThrow()

            // Extract the IOUState StateAndRefs from the vault.
            val iouStateAndRefs = rpcOps.vaultQueryBy<CreateServiceCreditState>().states

            // Map each StateAndRef to its IOUState.
            val iouStates = iouStateAndRefs.map { it.state.data.serviceCreditVO }

            Response.ok(iouStates.last(),MediaType.APPLICATION_JSON).build()


        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            Response.status(BAD_REQUEST).entity(ex.message!!).build()
        }
    }


    /**
     * Initiates a flow to approve a Service Credits between two parties.( Customer and SDD)
     *
     * Once the flow finishes it will have written the Service Credit to ledger. Both the AuthorizerCognizant and the Customer will be able to
     * see it when calling /api/valueArticulation/serviceCredits on their respective nodes.
     *
     * This end-point takes a Party name parameter as part of the path. If the serving node can't find the other party
     * in its network map cache, it will return an HTTP bad request.
     *
     * The flow is invoked asynchronously. It returns a future when the flow's call() method returns.
     */

    @POST
    @Path("approve-serviceCredits")
    fun approveServiceCredits(serviceCreditVO : ServiceCreditVO): Response {

        // Business validation starts here
        // service credit validation
        if (serviceCreditVO.serviceCredit <= 0 && serviceCreditVO.serviceCredit >= 99999) {
            return Response.status(BAD_REQUEST).entity("Query parameter 'serviceCredit' must be non-negative or greater than 99999.\n").build()
        }

        // Status field validation
        if(serviceCreditVO.status.equals("submit to approve")) {
            serviceCreditVO.status=="Approved"
        }
        // Business validation ends here


        val ctsInitiator = CordaX500Name("InitiatorCognizant", "London", "GB")

        val initiator = rpcOps.wellKnownPartyFromX500Name(ctsInitiator) ?:
        return Response.status(BAD_REQUEST).entity("Party named $ctsInitiator cannot be found.\n").build()

        val ctsAuthorizer = CordaX500Name("AuthorizerCognizant", "New York", "US")

        val authorizer = rpcOps.wellKnownPartyFromX500Name(ctsAuthorizer) ?:
        return Response.status(BAD_REQUEST).entity("Party named $ctsAuthorizer cannot be found.\n").build()

        val tactCustomer = CordaX500Name("TacticalCustomer", "Paris", "FR")

        val tacticalCustomer = rpcOps.wellKnownPartyFromX500Name(tactCustomer) ?:
        return Response.status(BAD_REQUEST).entity("Party named $tactCustomer cannot be found.\n").build()

        val stratCognizant = CordaX500Name("StrategicCognizant", "London", "GB")

        val strategicCognizant = rpcOps.wellKnownPartyFromX500Name(stratCognizant) ?:
        return Response.status(BAD_REQUEST).entity("Party named $stratCognizant cannot be found.\n").build()

        val stratCustomer = CordaX500Name("StrategicCustomer", "Paris", "FR")

        val strategicCustomer = rpcOps.wellKnownPartyFromX500Name(stratCustomer) ?:
        return Response.status(BAD_REQUEST).entity("Party named $stratCustomer cannot be found.\n").build()

        return try {
            val flowHandle = rpcOps.startTrackedFlow(::Approver, serviceCreditVO,initiator ,authorizer ,tacticalCustomer,strategicCognizant,strategicCustomer)
            flowHandle.progress.subscribe { println(">> $it") }
            flowHandle.returnValue.getOrThrow()

            // Extract the IOUState StateAndRefs from the vault.

            val iouStateAndRefs = rpcOps.vaultQueryBy<CreateServiceCreditState>().states

            // Map each StateAndRef to its IOUState.
            val iouStates = iouStateAndRefs.map { it.state.data.serviceCreditVO }

            Response.ok(iouStates.last(),MediaType.APPLICATION_JSON).build()

        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            Response.status(BAD_REQUEST).entity(ex.message!!).build()
        }
    }

    /**
     * Initiates a flow to create value contract transaction for respective service credit Id.

     * Once finishes the creation, it ll create new value contract transaction Id for authorize the VCT.

     * If the lever category is "Retention" then the InitiatorCognizant, the AuthorizerCognizant and the OperationalCustomer  will be able to see it when calling /api/valueArticulation/ValueContract on their respective nodes.

     * If the lever category is "Addition or Premium" then the InitiatorCognizant and the AuthorizerCognizant will be able to see it when calling /api/valueArticulation/ValueContract on their respective nodes.

     * This end-point takes a Party name parameter as part of the path. If the serving node can't find the other party
    in its network map cache, it will return an HTTP bad request.

     * The flow is invoked asynchronously. It returns a future when the flow's call() method returns.
     */

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("execute-ValueContractTransaction")
    fun executeValueTransaction(valueContractTransactionVO : ValueContractTransactionVO): Response {

        if (valueContractTransactionVO.agreedServiceCredits <= 0) {
            return Response.status(BAD_REQUEST).entity("Query parameter 'serviceCredit' must be non-negative.\n").build()
        }

        val ctsInitiator = CordaX500Name("InitiatorCognizant", "London", "GB")

        val initiator = rpcOps.wellKnownPartyFromX500Name(ctsInitiator) ?:
        return Response.status(BAD_REQUEST).entity("Party named $ctsInitiator cannot be found.\n").build()

        val tactCustomer = CordaX500Name("TacticalCustomer", "Paris", "FR")

        val tacticalCustomer = rpcOps.wellKnownPartyFromX500Name(tactCustomer) ?:
        return Response.status(BAD_REQUEST).entity("Party named $tactCustomer cannot be found.\n").build()

        val optCustomer = CordaX500Name("OperationalCustomer", "Paris", "FR")

        val operationalCustomer = rpcOps.wellKnownPartyFromX500Name(optCustomer) ?:
        return Response.status(BAD_REQUEST).entity("Party named $optCustomer cannot be found.\n").build()

        val ctsAuthorizer = CordaX500Name("AuthorizerCognizant", "New York", "US")

        val authorizer = rpcOps.wellKnownPartyFromX500Name(ctsAuthorizer) ?:
        return Response.status(BAD_REQUEST).entity("Party named $ctsAuthorizer cannot be found.\n").build()

        val stratCognizant = CordaX500Name("StrategicCognizant", "London", "GB")

        val strategicCognizant = rpcOps.wellKnownPartyFromX500Name(stratCognizant) ?:
        return Response.status(BAD_REQUEST).entity("Party named $stratCognizant cannot be found.\n").build()

        val stratCustomer = CordaX500Name("StrategicCustomer", "Paris", "FR")

        val strategicCustomer = rpcOps.wellKnownPartyFromX500Name(stratCustomer) ?:
        return Response.status(BAD_REQUEST).entity("Party named $stratCustomer cannot be found.\n").build()

        val counterParties= ArrayList<Party>()

        counterParties.add(strategicCognizant)

        counterParties.add(strategicCustomer)

        counterParties.add(tacticalCustomer)

        return try {

            val expression = builder { PersistentServiceCredits::linearId.equal(valueContractTransactionVO.serviceCreditId) }

            val qryCriteria = QueryCriteria.VaultCustomQueryCriteria(expression)

            val qryCriteriaUnconsumed = QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED)

            val vaultState = rpcOps.vaultQueryBy<CreateServiceCreditState>(qryCriteria.and(qryCriteriaUnconsumed)).states.singleOrNull()

            val serviceCredit = vaultState?.state?.data?.serviceCreditVO

            val flowHandle: FlowProgressHandle<SignedTransaction>

            isBeyondCommitted(valueContractTransactionVO)

            if (null != serviceCredit) {

                if (serviceCredit.status == "Approved") {
                    //Planned Type
                    //If the VCT type is Planned it will call the ValueContractInitiator Flow else it will call ImplementVCT flow
                    if (!valueContractTransactionVO.isimplemented) {
                        flowHandle = rpcOps.startTrackedFlow(::ValueContractInitiator, valueContractTransactionVO, initiator, authorizer, operationalCustomer,counterParties)
                        flowHandle.progress.subscribe { println(">> $it") }
                        flowHandle.returnValue.getOrThrow()
                    }

                    //Implementation Type
                    else if (valueContractTransactionVO.isimplemented) {
                        //Iteration 4 remove the if condition
                        val validate = validateImplementation(valueContractTransactionVO)
                        if (validate) {
                                flowHandle = rpcOps.startTrackedFlow(::RevisedValueContractApprover, valueContractTransactionVO, initiator, authorizer, operationalCustomer, counterParties)
                                flowHandle.progress.subscribe { println(">> $it") }
                                flowHandle.returnValue.getOrThrow()
                        } else {
                            return Response.status(BAD_REQUEST).entity("VCT must be Approved\n").build()
                        }
                    }
                    val iouStateAndRefs = rpcOps.vaultQueryBy<ValueContractTransactionState>().states
                    val iouStates = iouStateAndRefs.map { it.state.data.valueContractTransactionVO }
                    Response.ok(iouStates.last(), MediaType.APPLICATION_JSON).build()
                }
                else {
                    return Response.status(BAD_REQUEST).entity("Needs to be Approved by Customer.\n").build()
                }
            } else {
                return Response.status(BAD_REQUEST).entity("This AUV already consumed.").build()
            }

        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            Response.status(BAD_REQUEST).entity(ex.message!!).build()
        }

    }


    /**
     * Initiates a flow to Authorize value contract transaction for respective service credit Id which is from create VCT.

     * Once finishes the Authorization, it ll create new value contract transaction Id for Approve the VCT.

     * the InitiatorCognizant, the AuthorizerCognizant and the OperationalCustomer  will be able to see it when calling /api/valueArticulation/ValueContract on their respective nodes.

     * This end-point takes a Party name parameter as part of the path. If the serving node can't find the other party
    in its network map cache, it will return an HTTP bad request.

     * The flow is invoked asynchronously. It returns a future when the flow's call() method returns.
     */

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("authorize-ValueContractTransaction")
    fun authorizeValueContractTransaction(valueContractTransactionVO : ValueContractTransactionVO): Response {
        if (valueContractTransactionVO.agreedServiceCredits <= 0) {
            return Response.status(BAD_REQUEST).entity("Query parameter 'serviceCredit' must be non-negative.\n").build()
        }

        val ctsInitiator = CordaX500Name("InitiatorCognizant", "London", "GB")

        val initiator = rpcOps.wellKnownPartyFromX500Name(ctsInitiator) ?:
        return Response.status(BAD_REQUEST).entity("Party named $ctsInitiator cannot be found.\n").build()

        val tactCustomer = CordaX500Name("TacticalCustomer", "Paris", "FR")

        val tacticalCustomer = rpcOps.wellKnownPartyFromX500Name(tactCustomer) ?:
        return Response.status(BAD_REQUEST).entity("Party named $tactCustomer cannot be found.\n").build()

        val optCustomer = CordaX500Name("OperationalCustomer", "Paris", "FR")

        val operationalCustomer = rpcOps.wellKnownPartyFromX500Name(optCustomer) ?:
        return Response.status(BAD_REQUEST).entity("Party named $optCustomer cannot be found.\n").build()

        val ctsAuthorizer = CordaX500Name("AuthorizerCognizant", "New York", "US")

        val authorizer = rpcOps.wellKnownPartyFromX500Name(ctsAuthorizer) ?:
        return Response.status(BAD_REQUEST).entity("Party named $ctsAuthorizer cannot be found.\n").build()

        val stratCognizant = CordaX500Name("StrategicCognizant", "London", "GB")

        val strategicCognizant = rpcOps.wellKnownPartyFromX500Name(stratCognizant) ?:
        return Response.status(BAD_REQUEST).entity("Party named $stratCognizant cannot be found.\n").build()

        val stratCustomer = CordaX500Name("StrategicCustomer", "Paris", "FR")

        val strategicCustomer = rpcOps.wellKnownPartyFromX500Name(stratCustomer) ?:
        return Response.status(BAD_REQUEST).entity("Party named $stratCustomer cannot be found.\n").build()

        val counterParties= ArrayList<Party>()

        counterParties.add(strategicCognizant)

        counterParties.add(strategicCustomer)

        counterParties.add(tacticalCustomer)


        return try {
            isBeyondCommitted(valueContractTransactionVO)
            val flowHandle = rpcOps.startTrackedFlow(::ValueContractAuthorizer, valueContractTransactionVO, initiator ,authorizer ,operationalCustomer,counterParties)
            flowHandle.progress.subscribe { println(">> $it") }

            flowHandle.returnValue.getOrThrow()
            // Extract the IOUState StateAndRefs from the vault.

            val iouStateAndRefs = rpcOps.vaultQueryBy<ValueContractTransactionState>().states

            // Map each StateAndRef to its IOUState.
            val iouStates = iouStateAndRefs.map { it.state.data.valueContractTransactionVO }


            Response.ok(iouStates.last(),MediaType.APPLICATION_JSON).build()

        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            Response.status(BAD_REQUEST).entity(ex.message!!).build()
        }
    }

    /**
     * Initiates a flow to Approve value contract transaction for respective service credit Id which is from Authorize VCT.

     * Once finishes the Approval, it ll create new final value contract transaction Id for future.

     * the InitiatorCognizant, the AuthorizerCognizant and the OperationalCustomer  will be able to see it when calling /api/valueArticulation/ValueContract on their respective nodes.

     * This end-point takes a Party name parameter as part of the path. If the serving node can't find the other party
    in its network map cache, it will return an HTTP bad request.

     * The flow is invoked asynchronously. It returns a future when the flow's call() method returns.
     */


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("approve-ValueContractTransaction")
    fun approveValueContractTransaction(valueContractTransactionVO : ValueContractTransactionVO ): Response {
        if (valueContractTransactionVO.agreedServiceCredits <= 0)
        {
            return Response.status(BAD_REQUEST).entity("Query parameter 'serviceCredit' must be non-negative.\n").build()
        }

        val ctsInitiator = CordaX500Name("InitiatorCognizant", "London", "GB")

        val initiator = rpcOps.wellKnownPartyFromX500Name(ctsInitiator) ?:
        return Response.status(BAD_REQUEST).entity("Party named $ctsInitiator cannot be found.\n").build()

        val tactCustomer = CordaX500Name("TacticalCustomer", "Paris", "FR")

        val tacticalCustomer = rpcOps.wellKnownPartyFromX500Name(tactCustomer) ?:
        return Response.status(BAD_REQUEST).entity("Party named $tactCustomer cannot be found.\n").build()

        val optCustomer = CordaX500Name("OperationalCustomer", "Paris", "FR")

        val operationalCustomer = rpcOps.wellKnownPartyFromX500Name(optCustomer) ?:
        return Response.status(BAD_REQUEST).entity("Party named $optCustomer cannot be found.\n").build()

        val ctsAuthorizer = CordaX500Name("AuthorizerCognizant", "New York", "US")

        val authorizer = rpcOps.wellKnownPartyFromX500Name(ctsAuthorizer) ?:
        return Response.status(BAD_REQUEST).entity("Party named $ctsAuthorizer cannot be found.\n").build()

        val stratCognizant = CordaX500Name("StrategicCognizant", "London", "GB")

        val strategicCognizant = rpcOps.wellKnownPartyFromX500Name(stratCognizant) ?:
        return Response.status(BAD_REQUEST).entity("Party named $stratCognizant cannot be found.\n").build()

        val stratCustomer = CordaX500Name("StrategicCustomer", "Paris", "FR")

        val strategicCustomer = rpcOps.wellKnownPartyFromX500Name(stratCustomer) ?:
        return Response.status(BAD_REQUEST).entity("Party named $stratCustomer cannot be found.\n").build()

        val counterParties= ArrayList<Party>()

        counterParties.add(strategicCognizant)

        counterParties.add(strategicCustomer)

        counterParties.add(tacticalCustomer)


        return try {
            val flowHandle = rpcOps.startTrackedFlow(::ValueContractApprover, valueContractTransactionVO,initiator ,authorizer ,operationalCustomer,counterParties)
            flowHandle.progress.subscribe { println(">> $it") }

            flowHandle.returnValue.getOrThrow()
            // Extract the IOUState StateAndRefs from the vault.
            val iouStateAndRefs = rpcOps.vaultQueryBy<ValueContractTransactionState>().states

            // Map each StateAndRef to its IOUState.
            val iouStates = iouStateAndRefs.map { it.state.data.valueContractTransactionVO }


            Response.ok(iouStates.last(),MediaType.APPLICATION_JSON).build()


        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            Response.status(BAD_REQUEST).entity(ex.message!!).build()
        }
    }

    /**
     * Initiates a flow to revise  the excecuted vct between two parties.( Initiator cognizant and customer tactical group)
     *
     * Once the flow finishes it will have revised the Service Credit to ledger. Both the initiatorCognizant and the Customer will be able to
     * see it when calling /api/valueArticulation/revise-ValueContractTransaction on their respective nodes.
     *
     * This end-point takes the valueContractTransactionVO object as the parameter. If the serving node can't find the other party
     * in its network map cache, it will return an HTTP bad request.
     *
     * The flow is invoked asynchronously. It returns a future when the flow's call() method returns.
     */


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("revise-ValueContractTransaction")
    fun reviseValueContractTransaction(valueContractTransactionVO : ValueContractTransactionVO ): Response {

        if (valueContractTransactionVO.agreedServiceCredits <= 0)
        {
            return Response.status(BAD_REQUEST).entity("Query parameter 'serviceCredit' must be non-negative.\n").build()
        }

        val ctsInitiator = CordaX500Name("InitiatorCognizant", "London", "GB")

        val initiator = rpcOps.wellKnownPartyFromX500Name(ctsInitiator) ?:
        return Response.status(BAD_REQUEST).entity("Party named $ctsInitiator cannot be found.\n").build()

        val tactCustomer = CordaX500Name("TacticalCustomer", "Paris", "FR")

        val tacticalCustomer = rpcOps.wellKnownPartyFromX500Name(tactCustomer) ?:
        return Response.status(BAD_REQUEST).entity("Party named $tactCustomer cannot be found.\n").build()

        val optCustomer = CordaX500Name("OperationalCustomer", "Paris", "FR")

        val operationalCustomer = rpcOps.wellKnownPartyFromX500Name(optCustomer) ?:
        return Response.status(BAD_REQUEST).entity("Party named $optCustomer cannot be found.\n").build()

        val ctsAuthorizer = CordaX500Name("AuthorizerCognizant", "New York", "US")

        val authorizer = rpcOps.wellKnownPartyFromX500Name(ctsAuthorizer) ?:
        return Response.status(BAD_REQUEST).entity("Party named $ctsAuthorizer cannot be found.\n").build()

        val stratCognizant = CordaX500Name("StrategicCognizant", "London", "GB")

        val strategicCognizant = rpcOps.wellKnownPartyFromX500Name(stratCognizant) ?:
        return Response.status(BAD_REQUEST).entity("Party named $stratCognizant cannot be found.\n").build()

        val stratCustomer = CordaX500Name("StrategicCustomer", "Paris", "FR")

        val strategicCustomer = rpcOps.wellKnownPartyFromX500Name(stratCustomer) ?:
        return Response.status(BAD_REQUEST).entity("Party named $stratCustomer cannot be found.\n").build()

        val counterParties= ArrayList<Party>()

        counterParties.add(strategicCognizant)

        counterParties.add(strategicCustomer)

        counterParties.add(tacticalCustomer)


        return try {
            val flowHandle = rpcOps.startTrackedFlow(::RevisedValueContractApprover, valueContractTransactionVO,initiator ,authorizer,operationalCustomer,counterParties)
            flowHandle.progress.subscribe { println(">> $it") }

            flowHandle.returnValue.getOrThrow()
            // Extract the IOUState StateAndRefs from the vault.
            val iouStateAndRefs = rpcOps.vaultQueryBy<ValueContractTransactionState>().states

            // Map each StateAndRef to its IOUState.
            val iouStates = iouStateAndRefs.map { it.state.data.valueContractTransactionVO }

            Response.ok(iouStates.last(),MediaType.APPLICATION_JSON).build()


        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            Response.status(BAD_REQUEST).entity(ex.message!!).build()
        }

    }

    /**
     * Initiates a flow to authorize a Service Credits between two parties.( SDD Cognizant and Customer)
     *
     * Once the flow finishes it will have written the Service Credit to ledger. Both the AuthorizerCognizant and the Customer will be able to
     * see it when calling /api/valueArticulation/serviceCredits on their respective nodes.
     *
     * This end-point takes a Party name parameter as part of the path. If the serving node can't find the other party
     * in its network map cache, it will return an HTTP bad request.
     *
     * The flow is invoked asynchronously. It returns a future when the flow's call() method returns.
     */

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("revise-serviceCredits")
    fun reviseServiceCredits(serviceCreditVO : ServiceCreditVO ): Response {
        // Business validation starts here
        // service credit validation
        if (serviceCreditVO.serviceCredit <= 0 && serviceCreditVO.serviceCredit >= 99999) {
            return Response.status(BAD_REQUEST).entity("Query parameter 'serviceCredit' must be non-negative or greater than 99999.\n").build()
        }

        // Status field validation
        serviceCreditVO.status=="Revised"

        // Business validation ends here

        val ctsAuthorizer = CordaX500Name("AuthorizerCognizant", "New York", "US")

        val authorizer = rpcOps.wellKnownPartyFromX500Name(ctsAuthorizer) ?:
        return Response.status(BAD_REQUEST).entity("Party named $ctsAuthorizer cannot be found.\n").build()

        val tactCustomer = CordaX500Name("TacticalCustomer", "Paris", "FR")

        val tacticalCustomer = rpcOps.wellKnownPartyFromX500Name(tactCustomer) ?:
        return Response.status(BAD_REQUEST).entity("Party named $tactCustomer cannot be found.\n").build()

        val stratCognizant = CordaX500Name("StrategicCognizant", "London", "GB")

        val strategicCognizant = rpcOps.wellKnownPartyFromX500Name(stratCognizant) ?:
        return Response.status(BAD_REQUEST).entity("Party named $stratCognizant cannot be found.\n").build()




        //Attachment code starts here
        val path = serviceCreditVO.attachmentPath
        if (serviceCreditVO.attachmentPath == null || serviceCreditVO.attachmentPath == "" || serviceCreditVO.attachmentPath == " ") {
            return Response.status(BAD_REQUEST).entity(" 'AttachmentPath' not exist").build()}

        val pathName = path!!.substringBeforeLast(".")
        val fileName = pathName.substringAfterLast("/")

        if (fileName == "" || fileName == " ") {
            return Response.status(BAD_REQUEST).entity(" 'File' not exist").build()
        }
        val attachmentInputStream: InputStream = File(serviceCreditVO.attachmentPath).inputStream()
        val attachmentHash = rpcOps.uploadAttachment(attachmentInputStream).toString()
        serviceCreditVO.attachment = attachmentHash
        return try {
            val flowHandle = rpcOps.startTrackedFlow(::RevisedInitiator, serviceCreditVO, authorizer, tacticalCustomer,strategicCognizant)
            flowHandle.progress.subscribe { println(">> $it") }
            // Extract the IOUState StateAndRefs from the vault.
            flowHandle.returnValue.getOrThrow()
            val iouStateAndRefs = rpcOps.vaultQueryBy<CreateServiceCreditState>().states
            // Map each StateAndRef to its IOUState.
            val iouStates = iouStateAndRefs.map { it.state.data.serviceCreditVO }
            Response.ok(iouStates.last(), MediaType.APPLICATION_JSON).build()
        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            Response.status(BAD_REQUEST).entity(ex.message!!).build()
        }
        //Attachment code ends here
    }

    /**
     *It gets the input as list of Business Unit.

     *Queries and  returns the list of AUV and VCT for the respective Business Unit.

     *And set the updated balance Service Credits by calling getBalance with paramaters ServiceCredit and
    the  queryCriteria to query the vct's by ServiceCreditId

     */

    @POST
    @Path("GetAll_AUV_VCT_byBU")
    fun getAuvVctByBU(@QueryParam("list") list: List<String>): Response
    {
        var serviceCreditVO = ArrayList<ServiceCreditVO>(list.size)

        for(i in list.indices){

            val getAUV = builder { PersistentServiceCredits::businessUnit.equal(list[i]) }
            val auvQryCriteria = QueryCriteria.VaultCustomQueryCriteria(getAUV)
            val auvServiceCreditStates = rpcOps.vaultQueryBy<CreateServiceCreditState>(auvQryCriteria).states
            val AUVStates = auvServiceCreditStates.map { it.state.data.serviceCreditVO }

            for (j in AUVStates.indices) {

                val auvServiceCredits = AUVStates[j].serviceCredit

                val getVCT = builder { PresisentValueContractTransaction::auvId.equal(AUVStates[j].auvId) }
                val vctQryCriteria = QueryCriteria.VaultCustomQueryCriteria(getVCT)

                val vctServiceCreditStates = rpcOps.vaultQueryBy<ValueContractTransactionState>(vctQryCriteria).states
                val VCTStates = vctServiceCreditStates.map { it.state.data.valueContractTransactionVO }

                AUVStates[j].balanceServiceCredits = getbalance(auvServiceCredits,vctQryCriteria);



                AUVStates[j].valueContractTransactionVO = VCTStates as ArrayList<ValueContractTransactionVO>;



            }

            serviceCreditVO.addAll(AUVStates as ArrayList<ServiceCreditVO>)
        }
        return  Response.ok(serviceCreditVO, MediaType.APPLICATION_JSON_TYPE).build()

    }

    /**
     *It gets the input as list of Project IDs.

     *Queries and  returns the list of AUV and VCT for the respective Project IDs.

     *And set the updated balance Service Credits by calling getBalance with paramaters ServiceCredit and
    the  queryCriteria to query the vct's by ServiceCreditId

     */


    @POST
    @Path("view-ProjectDetails")
    @Consumes(MediaType.APPLICATION_JSON)
    fun getProjectDetails(@QueryParam("projects")Projects: List<String>) : Response {

        var valueContractTransactionVO = ArrayList<ServiceCreditVO>()


        for(i in  Projects.indices) {
            // Querying the Project Details from Service Credit table and VCT table

            val projectName= builder { PersistentServiceCredits::projectName.equal(Projects[i]) }
            val projectqryCriteria  = QueryCriteria.VaultCustomQueryCriteria(projectName)
            val auvvaultStateAndRef =rpcOps.vaultQueryBy<CreateServiceCreditState>(projectqryCriteria).states
            val  auvvaultState  = auvvaultStateAndRef.map {  it.state.data.serviceCreditVO }
            //  println("auvvaultState============>"+auvvaultStateAndRef)


            for (j in auvvaultState.indices){

                var auvServiceCredits = auvvaultState[j].serviceCredit
                // Querying the Service Credit from   ValueContractTransactionState Vault
                val serviceCreditId= builder { PresisentValueContractTransaction::auvId.equal(auvvaultState[j].auvId) }
                val serviceCreditIdqryCriteria = QueryCriteria.VaultCustomQueryCriteria(serviceCreditId)

                val allvctvaultStateAndRef = rpcOps.vaultQueryBy<ValueContractTransactionState>(serviceCreditIdqryCriteria).states
                val allvctvaultState  = allvctvaultStateAndRef.map {  it.state.data.valueContractTransactionVO }

                auvvaultState[j].balanceServiceCredits = getbalance(auvServiceCredits,serviceCreditIdqryCriteria);
                auvvaultState[j].valueContractTransactionVO = allvctvaultState as ArrayList<ValueContractTransactionVO>;

            }

            valueContractTransactionVO.addAll(auvvaultState as ArrayList<ServiceCreditVO>)

        }
        return Response.ok(
                valueContractTransactionVO, MediaType.APPLICATION_JSON
        ).build()

    }



    /**
     *It gets the input as list of Account name.

     *Queries and  returns the list of AUV and VCT for the respective Account name.

     *And set the updated balance Service Credits by calling getBalance with paramaters ServiceCredit and
    the  queryCriteria to query the vct's by ServiceCreditId

     */

    @POST
    @Path("GetAll_AUV_VCT_ByAccount")
    fun AccountDetails(@QueryParam("list") account : List<String>) : Response {

        val AUVandVCT = ArrayList<ServiceCreditVO>()

        for (i in account.indices) {

            val getAUV = builder { PersistentServiceCredits::accountName.equal(account[i]) }

            val auvQryCriteria = QueryCriteria.VaultCustomQueryCriteria(getAUV)
            val auvServiceCreditStates = rpcOps.vaultQueryBy<CreateServiceCreditState>(auvQryCriteria).states
            val AUVStates = auvServiceCreditStates.map { it.state.data.serviceCreditVO }

            for (j in AUVStates.indices) {

                val auvServiceCredits = AUVStates[j].serviceCredit
                val getVCT = builder { PresisentValueContractTransaction::auvId.equal(AUVStates[j].auvId) }
                val VCTQueryCriteria = QueryCriteria.VaultCustomQueryCriteria(getVCT)
                val VCTVaultStateAndRef = rpcOps.vaultQueryBy<ValueContractTransactionState>(VCTQueryCriteria).states
                val VCTVaultState = VCTVaultStateAndRef.map { it.state.data.valueContractTransactionVO }

                AUVStates[j].balanceServiceCredits = getbalance(auvServiceCredits,VCTQueryCriteria);


                AUVStates[j].valueContractTransactionVO = VCTVaultState as ArrayList<ValueContractTransactionVO>
            }
            AUVandVCT.addAll(AUVStates)
        }
        return Response.ok(AUVandVCT, MediaType.APPLICATION_JSON).build()

    }



    @POST
    @Path("GetAll_AUV_ByProjectId")
    fun GetAUVByProjectId(@QueryParam("projectid") projectid: List<Int>): Response
    {
        val AUV = ArrayList<ServiceCreditVO>()

        for (i in projectid.indices)
        {

            val getAUV = builder { PersistentServiceCredits::projectId.equal(projectid[i]) }

            val auvQryCriteria = QueryCriteria.VaultCustomQueryCriteria(getAUV)

            val VCTVaultStateAndRef  = rpcOps.vaultQueryBy<CreateServiceCreditState>(auvQryCriteria).states

            val AUVStates = VCTVaultStateAndRef.map { it.state.data.serviceCreditVO}

            AUV.addAll(AUVStates)
        }

        return Response.ok(AUV, MediaType.APPLICATION_JSON).build()
    }


    @POST
    @Path("GetAll_VCT_ByProjectId")
    fun GetVCTByProjectid(@QueryParam("projectId") projectId : List<Int>) : Response
    {

        val VCT = ArrayList<ValueContractTransactionVO>()

        for (i in projectId.indices) {

            val getVCT = builder { PresisentValueContractTransaction::projectId.equal(projectId[i]) }

            val vctQryCriteria = QueryCriteria.VaultCustomQueryCriteria(getVCT)

            val vctStateandRef = rpcOps.vaultQueryBy<ValueContractTransactionState>(vctQryCriteria).states

            val VCTStates = vctStateandRef.map { it.state.data.valueContractTransactionVO }

            VCT.addAll(VCTStates )
        }

        return Response.ok(VCT, MediaType.APPLICATION_JSON).build()
    }



    //This functions queries unconsumed state for the planned VCT and checks the VCTType and Status for validation
    //This function validates  whether it is planned and approved before implemented
    private fun validateImplementation(valueContractTransactionVO: ValueContractTransactionVO): Boolean {
        //included a try in iteration 4
        try {
            val expression = builder { PresisentValueContractTransaction::linearId.equal(valueContractTransactionVO.valueContractTransactionId) }
            val qryCriteria = QueryCriteria.VaultCustomQueryCriteria(expression)


            val qryCriteriaUnconsumed = QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED)
            val vaultState = rpcOps.vaultQueryBy<ValueContractTransactionState>(qryCriteria.and(qryCriteriaUnconsumed)).states.singleOrNull()


            val VCTtype = vaultState?.state?.data?.valueContractTransactionVO?.isimplemented
            val VCTStatus = vaultState?.state?.data?.valueContractTransactionVO?.status


            if((!VCTtype!! && VCTStatus=="Approved")){
                 return true
              }
        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            return false
        }

        return false

    }

    /**
     * This function queries all the approved value contract transactions for one AUV from H2 Table
    and sends the data to BalanceCalculation function in  ValueArticulationValidation() method

     *Input : Agreed upon ServiceCredit and the  queryCriteria to query the vct's by ServiceCreditId
    Output Calculated Balance.
     */


    public fun getbalance(auvServiceCredits: Int, serviceCreditIdqryCriteria: QueryCriteria.VaultCustomQueryCriteria<PresisentValueContractTransaction>):Int  {

        val approved= builder { PresisentValueContractTransaction::status.equal("Approved")}
        val approvedqryCriteria = QueryCriteria.VaultCustomQueryCriteria(approved)

        // Checked whether it is implemented
        val implemented= builder { PresisentValueContractTransaction::isimplemented.equal(true) }
        val implementCriteria = QueryCriteria.VaultCustomQueryCriteria(implemented)

        // Extract the StateAndRefs from the vault.
        val vctvaultStateAndRef = rpcOps.vaultQueryBy<ValueContractTransactionState>(serviceCreditIdqryCriteria.and(approvedqryCriteria).and(implementCriteria)).states

        val vctvaultState  = vctvaultStateAndRef.map {  it.state.data.valueContractTransactionVO }
        var validationObj = ValueArticulationValidation()
        var balanceServiceCredits = validationObj.getbalance(auvServiceCredits,vctvaultState)

        return balanceServiceCredits;

    }

    private fun isBeyondCommitted (valueContractTransactionVO:ValueContractTransactionVO): Boolean {

        // checking if the service credits submitted is beyond committed

        val auvexpression = builder { PersistentServiceCredits::auvId.equal(valueContractTransactionVO.auvId) }
        val auvqryCriteria = QueryCriteria.VaultCustomQueryCriteria(auvexpression)

        val auvunconsumedqryCriteria = QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED)

        val auvvaultStateAndRef = rpcOps.vaultQueryBy<CreateServiceCreditState>(auvqryCriteria.and(auvunconsumedqryCriteria)).states.singleOrNull()
        val baseservicecredits  = auvvaultStateAndRef!!.state.data.serviceCreditVO.serviceCredit

        val vctexpression= builder { PresisentValueContractTransaction::auvId.equal(valueContractTransactionVO.auvId)  } //Revise AUV Toniya
        val vctqryCriteria = QueryCriteria.VaultCustomQueryCriteria(vctexpression)

        val auvbalance= getbalance(baseservicecredits,vctqryCriteria)
        val balance = auvbalance-valueContractTransactionVO.agreedServiceCredits
        if(balance<0){
            valueContractTransactionVO.isbeyondcommited=true

        }
        return false
    }


}
