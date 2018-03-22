/*
 * Copyright (c) 2017.  $Revision:  1.0
 *
 * <AVM_ANALYTICS>
 *  This file contains proprietary information of Cognizant AVM.
 *  Copying or reproduction without prior written approval is prohibited.
 *  All rights reserved
 *  </AVM_ANALYTICS>
 */

package com.valuemanagement.model

import kotlinx.html.InputType
import net.corda.core.serialization.CordaSerializable
import net.corda.core.serialization.SerializationWhitelist
import java.time.Instant
import java.util.*
import kotlin.collections.ArrayList

/**
 * Model class for the ServiceCredit to get and set the values at any point of the time.
 * And also hashcode,equals and toString are defined for respective identities.
 */


@CordaSerializable
class ServiceCreditVO  {

    var auvId: String? = null //Revise AUV

    var serviceCreditId: UUID = UUID.randomUUID();

    var businessUnit: String? = null

    var accountName: String? = null

    var projectId: Int=0

    var projectName: String? = null

    var customerName: String? = null

    var lob: String? = null

    var serviceCredit: Int = 0

    var internalComments: String? = null

    var comments: String? = null

    var startYear: String ? = null

    var endYear: String ? = null

    var periodOfContract: Int = 0

    var attachmentPath: String? = null

    var attachment: String? = null

    var status: String? = null

    var cognizantAuthorizers: String? = null

    var customerApprovers: String? = null

    var balanceServiceCredits: Int=0

    var projectScope: String? = "Base" //Iteration 2 changes -- TOniya

    var valueContractTransactionVO= ArrayList<ValueContractTransactionVO>()

    //IsserviceCreditCommitted

    var internalcommentList=ArrayList<String>()

    var commentList=ArrayList<String>()

    var serviceCreditCommitted: Boolean=true

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ServiceCreditVO

        if (auvId != other.auvId) return false
        if (serviceCreditId != other.serviceCreditId) return false
        if (businessUnit != other.businessUnit) return false
        if (accountName != other.accountName) return false
        if (projectId != other.projectId) return false
        if (projectName != other.projectName) return false
        if (customerName != other.customerName) return false
        if (lob != other.lob) return false
        if (serviceCredit != other.serviceCredit) return false
        if (internalComments != other.internalComments) return false
        if (comments != other.comments) return false
        if (startYear != other.startYear) return false
        if (endYear != other.endYear) return false
        if (periodOfContract != other.periodOfContract) return false
        if (attachmentPath != other.attachmentPath) return false
        if (attachment != other.attachment) return false
        if (status != other.status) return false
        if (cognizantAuthorizers != other.cognizantAuthorizers) return false
        if (customerApprovers != other.customerApprovers) return false
        if (balanceServiceCredits != other.balanceServiceCredits) return false
        if (projectScope != other.projectScope) return false
        if (valueContractTransactionVO != other.valueContractTransactionVO) return false

        return true
    }

    override fun hashCode(): Int {
        var result = auvId?.hashCode() ?: 0
        result = 31 * result + serviceCreditId.hashCode()
        result = 31 * result + (businessUnit?.hashCode() ?: 0)
        result = 31 * result + (accountName?.hashCode() ?: 0)
        result = 31 * result + projectId
        result = 31 * result + (projectName?.hashCode() ?: 0)
        result = 31 * result + (customerName?.hashCode() ?: 0)
        result = 31 * result + (lob?.hashCode() ?: 0)
        result = 31 * result + serviceCredit
        result = 31 * result + (internalComments?.hashCode() ?: 0)
        result = 31 * result + (comments?.hashCode() ?: 0)
        result = 31 * result + (startYear?.hashCode() ?: 0)
        result = 31 * result + (endYear?.hashCode() ?: 0)
        result = 31 * result + periodOfContract
        result = 31 * result + (attachmentPath?.hashCode() ?: 0)
        result = 31 * result + (attachment?.hashCode() ?: 0)
        result = 31 * result + (status?.hashCode() ?: 0)
        result = 31 * result + (cognizantAuthorizers?.hashCode() ?: 0)
        result = 31 * result + (customerApprovers?.hashCode() ?: 0)
        result = 31 * result + balanceServiceCredits
        result = 31 * result + (projectScope?.hashCode() ?: 0)
        result = 31 * result + valueContractTransactionVO.hashCode()
        return result
    }

    override fun toString(): String {
        return "ServiceCreditVO(auvId=$auvId, serviceCreditId=$serviceCreditId, businessUnit=$businessUnit, accountName=$accountName, projectId=$projectId, projectName=$projectName, customerName=$customerName, lob=$lob, serviceCredit=$serviceCredit, internalComments=$internalComments, comments=$comments, startYear=$startYear, endYear=$endYear, periodOfContract=$periodOfContract, attachmentPath=$attachmentPath, attachment=$attachment, status=$status, cognizantAuthorizers=$cognizantAuthorizers, customerApprovers=$customerApprovers, balanceServiceCredits=$balanceServiceCredits, projectScope=$projectScope, valueContractTransactionVO=$valueContractTransactionVO)"
    }


}
