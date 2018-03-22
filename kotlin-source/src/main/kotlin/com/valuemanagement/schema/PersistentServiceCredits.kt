package com.valuemanagement.schema

import net.corda.core.schemas.PersistentState
import java.security.Timestamp
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

/**
 * An CreateServiceCreditState schema.
 * Design of the table[service_credits] and columns are defined here.
 */


@Entity
@Table(name = "service_credits")
class PersistentServiceCredits (

        //Revise AUV Toniya
        @Column(name = "auvId")
        var auvId: String,
        //Revise AUV Toniya

        @Column(name = "initiator")
        var initiatorName: String,

        @Column(name = "acceptor")
        var acceptorName: String,

        @Column(name= "business_unit")
        var businessUnit: String,

        @Column(name= "account_name")
        var accountName: String,

        @Column(name= "project_Id")
        var projectId: Int,

        @Column(name= "project_name")
        var projectName: String,

        @Column(name= "customer_name")
        var customerName: String,

        @Column(name= "line_of_business")
        var lineOfBusiness: String,

        @Column(name= "starting_yearofcontract")
        var startingYearOfContract: String,

        @Column(name= "ending_yearofcontract")
        var endingYearOfContract: String,

        @Column(name= "period_of_contract")
        var periodOfContract: Int,

        @Column(name = "serviceCredits")
        var serviceCredits: Int,

        @Column(name = "AttachmentId")
        var attachmentHash: String,

        /* @Column(name= "due_date")
             var dueDate: Date,*/

        @Column(name= "comments")
        var comments: String,

        @Column(name= "customer_discussion")
        var customerDiscussion: String,

        @Column(name = "status")
        var status: String,

        @Column(name= "authorizer_name")
        var authorizerName: String,

        @Column(name= "approver_name")
        var approverName: String,

        @Column(name= "project_scope") //Iteration 2 changes -- TOniya
        var projectScope: String,

        @Column(name = "serviceCredit")
        var linearId: UUID
) : PersistentState(){
        constructor() : this(
                "","","","","",0,"",
                "","","","",0,0,
                "","","","","","","", UUID.randomUUID()
        );

}
