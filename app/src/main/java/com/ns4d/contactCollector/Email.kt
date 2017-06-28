package com.ns4d.contactCollector

/**
 * Email Address
 */
class Email(var value: String, var type: String) {

    override fun toString(): String {
        return "$type: $value"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as Email

        if (value != other.value) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = value.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }
}