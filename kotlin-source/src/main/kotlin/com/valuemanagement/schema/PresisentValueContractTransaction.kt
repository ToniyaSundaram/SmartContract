package com.valuemanagement.schema

import net.corda.core.schemas.PersistentState
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

/**
 * An ValueContractTransactionState schema.
 * Design of the table [ValueContractTransaction] and columns are defined here.
 */



@Entity
@Table(name = "ValueContractTransaction")
class PresisentValueContractTransaction(

        //Revise AUV Toniya
        @Column(name = "auvId")
        var auvId: String,
        //Revise AUV Toniya

        @Column(name = "valueContractTransactionId")
        var linearId: UUID,

        @Column(name = "serviceCreditId")
        var serviceCreditId: UUID,

        @Column(name = "projectId")
        var projectId: Int,

        @Column(name = "lob")
        var lob: String,

        @Column(name = "leverCategory")
        var leverCategory: String,

        @Column(name = "valueImprovementProgram")
        var valueImprovementProgram: String,

        @Column(name = "valueCategory")
        var valueCategory: String,

        @Column(name = "theme")
        var theme: String,

        @Column(name = "valueAddDescription")
        var valueAddDescription: String,

        @Column(name = "agreedServiceCredits")
        var agreedServiceCredits: Int,

        @Column(name = "revisedServiceCredits")
        var revisedServiceCredits: Int,

        @Column(name = "attachments")
        var attachments: String,

        @Column(name = "implementationDate")
        var implementationDate: Date,

        @Column(name = "internalComments")
        var internalComments: String,

        @Column(name = "customerComments")
        var customerComments: String,

        @Column(name = "status")
        var status: String,

        @Column(name = "isimplemented")//Iteration 2 changes
        var isimplemented:Boolean,

        @Column(name = "isbeyondcommitted")//Iteration 2 changes
        var isbeyondcommited:Boolean,

        @Column(name= "project_scope") //Iteration 2 changes -- TOniya
        var projectScope: String,

        @Column(name = "transactionapproverName")
        var transactionapproverName: String

) : PersistentState() {
        constructor() : this
        (
                "",
                UUID.randomUUID(),
                UUID.randomUUID(),
                0,
                "",
                "",
                "",
                "",
                "",
                "",
                0,
                0,
                "",
                Date(),
                "",
                "",
                "",
                false,
                false,
                "",
                ""
        )

}
