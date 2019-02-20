package com.ns4d.contactCollector.model

/**
 * Phone number
 */
class Phone(var phoneNo: String, private var label: String?, var type: Int) {

    override fun toString(): String {
        return "$label: $phoneNo"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Phone

        if (phoneNo != other.phoneNo) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = phoneNo.hashCode()
        result = 31 * result + type
        return result
    }


}