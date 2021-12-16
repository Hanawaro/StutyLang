package core

import core.models.Token
import core.stages.analyser.Analyser
import core.stages.translator.Interpreter
import core.stages.parser.Parser
import core.stages.parser.Stmt
import core.stages.scanner.Scanner
import core.stages.translator.Compiler
import core.utils.MessageProvider
import java.io.*
import java.lang.Exception

object LangCore {

    private val scanner = Scanner()
    private val analyser = Analyser()
    private val parser = Parser()
    private val interpreter = Interpreter()
    private val compiler = Compiler()

    fun compile(path: String): Boolean {

        if (!File(path).exists()) {
            println("There is no such file")
            return false
        }

        val tokens = scanner.getTokens(getSource(path))

        if (MessageProvider.hasError)
            return false

        analyser.process(tokens)

        if (MessageProvider.hasError)
            return false

        val statements = parser.process(tokens)
        if (compiler.compile(statements)) {
            val compilePath = path.substring(0, path.lastIndexOf('.')) + ".bsl"
            val ous = ObjectOutputStream(FileOutputStream(compilePath))
            ous.writeObject(statements)

            return true
        }

        return false
    }

    fun run(path: String) {

        if (!File(path).exists()) {
            println("There is no such file")
            return
        }

        val ois = ObjectInputStream(FileInputStream(path))

        try {
            interpreter.run(ois.readObject() as ArrayList<Stmt>)
        } catch (exception: Exception) {
            println(exception.message)
        }
    }

    private fun getSource(path: String): String {
        val file = File(path)
        if (file.exists())
            return String(file.readBytes())

        MessageProvider.error("Could not open file ${file.name}")
        return ""
    }

}