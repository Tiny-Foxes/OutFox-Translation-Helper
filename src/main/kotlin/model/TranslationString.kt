package com.tinyfoxes.translationhelper.model

data class TranslationString(val section: String?, val key: String, val translation: String, val linenumber: Int) {
    override fun equals(other: Any?): Boolean {
        if (other !is TranslationString) {
            return false
        }
        return (other.hashCode() == this.hashCode())
    }

    override fun hashCode(): Int {
        var result = section?.hashCode() ?: 0
        result = 31 * result + key.hashCode()
        return result
    }

    fun toId(): String {
        return "[$section]$key"
    }

    companion object {
        fun idToSectionAndKey(id: String) : Array<String>? {
            val regex = Regex("\\[(.+)\\](.+)")
            val results = regex.find(id) ?: return null
            return arrayOf(results.groupValues[1], results.groupValues[2])
        }
    }
}