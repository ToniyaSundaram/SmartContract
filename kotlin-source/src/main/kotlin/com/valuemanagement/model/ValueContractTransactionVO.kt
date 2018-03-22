package com.valuemanagement.model

import net.corda.core.serialization.CordaSerializable
import java.util.*
import javax.persistence.*

/**
 * Model class for the ValueContractTransaction to get and set the values at any point of the time.
 * And also hashcode,equals and toString are defined for respective identities.
 */

@CordaSerializable
class ValueContractTransactionVO
{
    var auvId: String? = null //Revise AUV

    var valueContractTransactionId: UUID = UUID.randomUUID()

    var serviceCreditId:UUID = UUID.randomUUID()

    var projectId:Int=0

    var lob: String? = null

    var leverCategory: String? = null

    var valueImprovementProgram: String? = null

    var valueCategory: String? = null

    var theme: String? = null

    var valueAddDescription: String? = null

    var agreedServiceCredits: Int=0

    var revisedServiceCredits: Int=0

    var attachments : String? = null

    var implementationDate: Date? =null

    var internalComments: String? =null

    var customerComments: String? =null

    var status: String?= null

    var isimplemented : Boolean=false

    var isbeyondcommited : Boolean=false

    var projectScope: String? = "Base" //Iteration 2 changes -- TOniya

    var transactionapproverName: String? =null

    //listing the comments

    var internalcommentList=ArrayList<String>()

    var customercommentList=ArrayList<String>()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ValueContractTransactionVO

        if (auvId != other.auvId) return false
        if (valueContractTransactionId != other.valueContractTransactionId) return false
        if (serviceCreditId != other.serviceCreditId) return false
        if (projectId != other.projectId) return false
        if (lob != other.lob) return false
        if (leverCategory != other.leverCategory) return false
        if (valueImprovementProgram != other.valueImprovementProgram) return false
        if (valueCategory != other.valueCategory) return false
        if (theme != other.theme) return false
        if (valueAddDescription != other.valueAddDescription) return false
        if (agreedServiceCredits != other.agreedServiceCredits) return false
        if (revisedServiceCredits != other.revisedServiceCredits) return false
        if (attachments != other.attachments) return false
        if (implementationDate != other.implementationDate) return false
        if (internalComments != other.internalComments) return false
        if (customerComments != other.customerComments) return false
        if (status != other.status) return false
        if (isimplemented != other.isimplemented) return false
        if (isbeyondcommited != other.isbeyondcommited) return false
        if (projectScope != other.projectScope) return false
        if (transactionapproverName != other.transactionapproverName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = auvId?.hashCode() ?: 0
        result = 31 * result + valueContractTransactionId.hashCode()
        result = 31 * result + serviceCreditId.hashCode()
        result = 31 * result + projectId
        result = 31 * result + (lob?.hashCode() ?: 0)
        result = 31 * result + (leverCategory?.hashCode() ?: 0)
        result = 31 * result + (valueImprovementProgram?.hashCode() ?: 0)
        result = 31 * result + (valueCategory?.hashCode() ?: 0)
        result = 31 * result + (theme?.hashCode() ?: 0)
        result = 31 * result + (valueAddDescription?.hashCode() ?: 0)
        result = 31 * result + agreedServiceCredits
        result = 31 * result + revisedServiceCredits
        result = 31 * result + (attachments?.hashCode() ?: 0)
        result = 31 * result + (implementationDate?.hashCode() ?: 0)
        result = 31 * result + (internalComments?.hashCode() ?: 0)
        result = 31 * result + (customerComments?.hashCode() ?: 0)
        result = 31 * result + (status?.hashCode() ?: 0)
        result = 31 * result + isimplemented.hashCode()
        result = 31 * result + isbeyondcommited.hashCode()
        result = 31 * result + (projectScope?.hashCode() ?: 0)
        result = 31 * result + (transactionapproverName?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "ValueContractTransactionVO(auvId=$auvId, valueContractTransactionId=$valueContractTransactionId, serviceCreditId=$serviceCreditId, projectId=$projectId, lob=$lob, leverCategory=$leverCategory, valueImprovementProgram=$valueImprovementProgram, valueCategory=$valueCategory, theme=$theme, valueAddDescription=$valueAddDescription, agreedServiceCredits=$agreedServiceCredits, revisedServiceCredits=$revisedServiceCredits, attachments=$attachments, implementationDate=$implementationDate, internalComments=$internalComments, customerComments=$customerComments, status=$status, isimplemented=$isimplemented, isbeyondcommited=$isbeyondcommited, projectScope=$projectScope, transactionapproverName=$transactionapproverName)"
    }


}