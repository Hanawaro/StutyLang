package core.stages.translator.components

import core.models.Token

class Environment(
        private val enclosing: Environment? = null
) {
    private val variables = HashMap<String, Any>()

    fun define(identifier: Token, value: Any) {
        if (variables.containsKey(identifier.data as String))
            throw Exception("Variable \"$identifier\" are already exist", identifier.line)
        variables[identifier.data] = value
    }

    fun get(identifier: Token): Any {
        if (variables.contains(identifier.data))
            return variables[identifier.data]!!

        if (enclosing != null)
            return enclosing.get(identifier)

        throw Exception("Undefined variable \"${identifier.data}\"", identifier.line)
    }

    fun assign(identifier: Token, value: Any) {
        if (variables.containsKey(identifier.data)) {
            check(identifier, value)
            variables[identifier.data as String] = value
            return
        }

        if (enclosing != null) {
            enclosing.assign(identifier, value)
            return
        }

        throw Exception("Undefined variable \"${identifier.data}\"", identifier.line)
    }

    fun check(identifier: Token, value: Any) {
        return when (variables[identifier.data]) {
            is Int -> {
                when (value) {
                    is Int, is Double -> {  }
                    else -> throw Exception("Could not cast \"$value\" to int", identifier.line)
                }
            }
            is Double -> {
                when (value) {
                    is Int, is Double -> {  }
                    else -> throw Exception("Could not cast \"$value\" to decimal", identifier.line)
                }
            }
            is Boolean -> {
                when (value) {
                    is Boolean -> {  }
                    else -> throw Exception("Could not cast \"$value\" to boolean", identifier.line)
                }
            }
            is String -> {
                when (value) {
                    is String -> {  }
                    else -> throw Exception("Could not cast \"$value\" to string", identifier.line)
                }
            }
            else -> { }
        }
    }

    class Exception(private val text: String, val line: Int? = null): kotlin.Exception() {
        override val message: String?
            get() = text
    }
}