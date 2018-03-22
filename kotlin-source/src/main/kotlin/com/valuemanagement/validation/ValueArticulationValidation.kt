package com.valuemanagement.validation

import com.valuemanagement.model.ValueContractTransactionVO
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@Suppress("DEPRECATED_IDENTITY_EQUALS")
class ValueArticulationValidation {

    // A function to validate the date

    /** properDate function takes the Date in this format "yyyy-MM-dd", and returns a boolean output

     *  This function checks whether the input date is future data

     */

    fun validateEndDate(input: String?):Boolean {

        try {
            val currentdate =  LocalDateTime.now().toString()
            val moddate = currentdate.toUpperCase().replace("T", " ")
            val str = moddate.split(" ")
            println(str[0])
            val todaysdate = LocalDate.parse(str[0], DateTimeFormatter.ISO_DATE)
            println("Todays Date $todaysdate")

            val sdf = SimpleDateFormat("yyyy-MM-dd")
            val date1 = sdf.parse(input)
            val date2 = sdf.parse(todaysdate.toString())
            if (date1 > date2) {
                println("Date1 is after Date2")
                return true
            } else if (date1.compareTo(date2) < 0) {
                println("Date1 is before Date2")
                return false
            } else return if (date1.compareTo(date2) === 0) {
                println("Date1 is equal to Date2")
                false
            } else {
                println("Any other")
                false
            }
        }catch (ex: Exception) {
            println(ex.message)
            println("There was a problem in Date comparision")
            return false
        }

    }

    /**
     * getbalance function takes the serviceCredit in AUV as baseServiceCredits and the list of vct under that AUV  as input.
        Output will be the  balance serviceCredit

     * This function adds all the VCT serviceCredits as totalagreedServiceCredits executed
         totalagreedServiceCredits will be  subtracted by the baseServiceCredits to get the balance
     */

    fun getbalance (baseServiceCredits: Int, vctObj: List<ValueContractTransactionVO>) : Int {
        var totalagreedServiceCredits = 0
        try {
            vctObj.indices.forEach({ i ->
                val agreedServiceCredits = vctObj[i].agreedServiceCredits
                totalagreedServiceCredits = totalagreedServiceCredits + agreedServiceCredits
            })
            val balanceCredit: Int = baseServiceCredits - totalagreedServiceCredits
            return balanceCredit
        } catch (ex: Exception) {
            println(ex.message)
            return 0
        }
    }


}