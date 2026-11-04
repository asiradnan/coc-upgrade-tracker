package com.asiradnan.cocupgradetracker.data

internal class JsonMap(private val map: Map<String, Any?>) {
    fun has(key: String): Boolean = map.containsKey(key) && map[key] != null

    fun optString(key: String, default: String = ""): String =
        (map[key] as? String) ?: map[key]?.toString() ?: default

    fun optInt(key: String, default: Int = 0): Int = when (val v = map[key]) {
        is Number -> v.toInt()
        is String -> v.toIntOrNull() ?: default
        else -> default
    }

    fun optLong(key: String, default: Long = 0L): Long = when (val v = map[key]) {
        is Number -> v.toLong()
        is String -> v.toLongOrNull() ?: default
        else -> default
    }

    fun optBoolean(key: String, default: Boolean = false): Boolean = when (val v = map[key]) {
        is Boolean -> v
        is Number -> v.toInt() != 0
        is String -> v.equals("true", ignoreCase = true)
        else -> default
    }

    @Suppress("UNCHECKED_CAST")
    fun optArray(key: String): List<JsonMap>? {
        val list = map[key] as? List<*> ?: return null
        return list.mapNotNull { item ->
            (item as? Map<String, Any?>)?.let { JsonMap(it) }
        }
    }
}

internal class MinimalJsonParser(private val input: String) {
    private var pos = 0

    fun parseRootObject(): JsonMap {
        skipWhitespace()
        val value = parseValue()
        @Suppress("UNCHECKED_CAST")
        val map = value as? Map<String, Any?>
            ?: throw IllegalArgumentException("Root JSON element must be an object")
        return JsonMap(map)
    }

    private fun parseValue(): Any? {
        skipWhitespace()
        if (pos >= input.length) throw IllegalArgumentException("Unexpected end of JSON")
        return when (val ch = input[pos]) {
            '{' -> parseObject()
            '[' -> parseArray()
            '"' -> parseString()
            't', 'f' -> parseBoolean()
            'n' -> parseNull()
            '-', in '0'..'9' -> parseNumber()
            else -> throw IllegalArgumentException("Unexpected character '$ch' at position $pos")
        }
    }

    private fun parseObject(): Map<String, Any?> {
        expectChar('{')
        skipWhitespace()
        val result = linkedMapOf<String, Any?>()
        if (pos < input.length && input[pos] == '}') {
            pos++
            return result
        }
        while (pos < input.length) {
            skipWhitespace()
            val key = parseString()
            skipWhitespace()
            expectChar(':')
            val value = parseValue()
            result[key] = value
            skipWhitespace()
            if (pos < input.length && input[pos] == ',') {
                pos++
                skipWhitespace()
                if (pos < input.length && input[pos] == '}') {
                    pos++
                    return result
                }
            } else if (pos < input.length && input[pos] == '}') {
                pos++
                return result
            } else {
                throw IllegalArgumentException("Expected ',' or '}' in object at position $pos")
            }
        }
        throw IllegalArgumentException("Unterminated JSON object")
    }

    private fun parseArray(): List<Any?> {
        expectChar('[')
        skipWhitespace()
        val result = mutableListOf<Any?>()
        if (pos < input.length && input[pos] == ']') {
            pos++
            return result
        }
        while (pos < input.length) {
            val value = parseValue()
            result.add(value)
            skipWhitespace()
            if (pos < input.length && input[pos] == ',') {
                pos++
                skipWhitespace()
                if (pos < input.length && input[pos] == ']') {
                    pos++
                    return result
                }
            } else if (pos < input.length && input[pos] == ']') {
                pos++
                return result
            } else {
                throw IllegalArgumentException("Expected ',' or ']' in array at position $pos")
            }
        }
        throw IllegalArgumentException("Unterminated JSON array")
    }

    private fun parseString(): String {
        expectChar('"')
        val sb = StringBuilder()
        while (pos < input.length) {
            val ch = input[pos++]
            when (ch) {
                '"' -> return sb.toString()
                '\\' -> {
                    if (pos >= input.length) throw IllegalArgumentException("Unterminated string escape")
                    when (val esc = input[pos++]) {
                        '"', '\\', '/' -> sb.append(esc)
                        'b' -> sb.append('\b')
                        'f' -> sb.append('\u000C')
                        'n' -> sb.append('\n')
                        'r' -> sb.append('\r')
                        't' -> sb.append('\t')
                        'u' -> {
                            if (pos + 4 > input.length) throw IllegalArgumentException("Invalid unicode escape")
                            val hex = input.substring(pos, pos + 4)
                            sb.append(hex.toInt(16).toChar())
                            pos += 4
                        }
                        else -> sb.append(esc)
                    }
                }
                else -> sb.append(ch)
            }
        }
        throw IllegalArgumentException("Unterminated string literal")
    }

    private fun parseNumber(): Number {
        val start = pos
        if (input[pos] == '-') pos++
        while (pos < input.length && input[pos] in '0'..'9') pos++
        var isDouble = false
        if (pos < input.length && input[pos] == '.') {
            isDouble = true
            pos++
            while (pos < input.length && input[pos] in '0'..'9') pos++
        }
        if (pos < input.length && (input[pos] == 'e' || input[pos] == 'E')) {
            isDouble = true
            pos++
            if (pos < input.length && (input[pos] == '+' || input[pos] == '-')) pos++
            while (pos < input.length && input[pos] in '0'..'9') pos++
        }
        val raw = input.substring(start, pos)
        return if (isDouble) {
            raw.toDoubleOrNull() ?: 0.0
        } else {
            raw.toLongOrNull() ?: 0L
        }
    }

    private fun parseBoolean(): Boolean {
        return when {
            input.startsWith("true", pos) -> {
                pos += 4
                true
            }
            input.startsWith("false", pos) -> {
                pos += 5
                false
            }
            else -> throw IllegalArgumentException("Invalid boolean token at position $pos")
        }
    }

    private fun parseNull(): Any? {
        if (input.startsWith("null", pos)) {
            pos += 4
            return null
        }
        throw IllegalArgumentException("Invalid null token at position $pos")
    }

    private fun skipWhitespace() {
        while (pos < input.length && input[pos].isWhitespace()) {
            pos++
        }
    }

    private fun expectChar(expected: Char) {
        skipWhitespace()
        if (pos >= input.length || input[pos] != expected) {
            throw IllegalArgumentException("Expected '$expected' at position $pos")
        }
        pos++
    }
}

