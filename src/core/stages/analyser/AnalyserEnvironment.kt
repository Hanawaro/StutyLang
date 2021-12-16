package core.stages.analyser

import core.models.Token
import core.stages.translator.components.Environment
import java.util.*
import kotlin.collections.HashSet

class AnalyserEnvironment {

    private val environments = Stack<HashSet<String>>().apply {
        push(HashSet())
    }


    fun enter() {
        environments.push(HashSet())
    }

    fun exit() {
        if (environments.size != 1)
            environments.pop()
    }

    fun define(identifier: String) {
        environments.lastElement().add(identifier)
    }

    fun isExist(identifier: String) = environments.lastElement().contains(identifier)
    fun isNotExist(identifier: String): Boolean {
        var exist = false
        for (environment in environments)
            if (environment.contains(identifier))
                exist = true
        return !exist
    }
}