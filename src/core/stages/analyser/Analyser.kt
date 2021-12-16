package core.stages.analyser

import core.models.Token
import core.utils.MessageProvider

class Analyser {

    private var current = 0
    private var tokens = ArrayList<Token>()

    fun process(tokens: ArrayList<Token>) {
        this.tokens = tokens
        current = 0

        var node: Node? = Node.Start()
        while ( node !is Node.End && isNotAtEnd() ) {
            node = node?.accept(advance())
            if (node == null) {
                var token = peek()
                while (token.type != Token.Type.EOL && isNotAtEnd()) {
                    token = advance()
                }
                node = Node.Final()
            }
        }
        node?.accept(tokens.last())
    }

    private fun isAtEnd() = current >= tokens.size - 1
    private fun isNotAtEnd() = !isAtEnd()
    private fun advance() = tokens[current++]
    private fun peek() = tokens[current]

    abstract class Node {

        companion object {
            private var braceLevel = 0
            private var parenLevel = 0
            private var ifStmt = 0

            private var analyserEnvironment = AnalyserEnvironment()
        }


        class Start: Node() {
            override fun accept(token: Token): Node? {
                braceLevel = 0
                parenLevel = 0
                analyserEnvironment = AnalyserEnvironment()

                return when(token.type) {
                    Token.Type.EOF -> exit(token)
                    Token.Type.Var -> Init()
                    Token.Type.Identifier -> {
                        if (analyserEnvironment.isNotExist(token.data as String)) {
                            MessageProvider.error("Undefined variable name \"${token.data}\"", token.line)
                            return null
                        }
                        Name()
                    }
                    Token.Type.LeftParen -> Input().also { parenLevel++ }
                    Token.Type.LeftBrace -> Final().also {
                        analyserEnvironment.enter()
                        braceLevel++
                    }
                    Token.Type.Inverse, Token.Type.Minus -> Input()
                    Token.Type.Int, Token.Type.Decimal, Token.Type.True, Token.Type.False, Token.Type.String -> Output()
                    Token.Type.For -> For().also {
                        analyserEnvironment.enter()
                        braceLevel++
                    }
                    Token.Type.If -> If().also {
                        analyserEnvironment.enter()
                        braceLevel++;
                        ifStmt++
                    }
                    Token.Type.Print, Token.Type.Println -> Print()
                    Token.Type.EOL -> Final()
                    else -> {
                        MessageProvider.error("Undefined syntax construction ${token.type}", token.line)
                        null
                    }
                }
            }
        }

        class Final: Node() {
            override fun accept(token: Token): Node? {

                parenLevel = 0

                return when(token.type) {
                    Token.Type.EOF -> exit(token)
                    Token.Type.Var -> Init()
                    Token.Type.Identifier -> {
                        if (analyserEnvironment.isNotExist(token.data as String)) {
                            MessageProvider.error("Undefined variable name \"${token.data}\"", token.line)
                            return null
                        }
                        Name()
                    }
                    Token.Type.LeftParen, Token.Type.Inverse, Token.Type.Minus -> Input()
                    Token.Type.Int, Token.Type.Decimal, Token.Type.True, Token.Type.False, Token.Type.String -> Output()
                    Token.Type.For -> For().also {
                        analyserEnvironment.enter()
                        braceLevel++
                    }
                    Token.Type.If -> If().also {
                        analyserEnvironment.enter()
                        braceLevel++;
                        ifStmt++
                    }
                    Token.Type.Print, Token.Type.Println -> Print()
                    Token.Type.LeftBrace -> this.also {
                        analyserEnvironment.enter()
                        braceLevel++
                    }
                    Token.Type.RightBrace -> CloseBrace().also {
                        analyserEnvironment.exit()
                        braceLevel--
                    }
                    Token.Type.EOL -> this
                    else -> {
                        MessageProvider.error("Undefined syntax construction ${token.type}", token.line)
                        null
                    }
                }
            }
        }

        class CloseBrace: Node() {
            override fun accept(token: Token): Node? {
                return when(token.type) {
                    Token.Type.EOF -> exit(token)
                    Token.Type.EOL -> Final()
                    Token.Type.Else -> {
                        if (ifStmt != 0)
                            return ElseBranch().also { ifStmt-- }
                        else
                            braceLevel++
                        MessageProvider.error("Could not found an if statement to else branch", token.line)
                        null
                    }
                    else -> {
                        MessageProvider.error("Expected new line, but not found", token.line)
                        null
                    }
                }
            }
        }

        class Init: Node() {
            override fun accept(token: Token): Node? {
                return when(token.type) {
                    Token.Type.Identifier -> {
                        if (analyserEnvironment.isExist(token.data as String)) {
                            MessageProvider.error("Such variable name \"${token.data}\" reserved", token.line)
                            return null
                        }
                        analyserEnvironment.define(token.data)
                        InitName()
                    }
                    else -> {
                        MessageProvider.error("Expected identifier name, but not found", token.line)
                        null
                    }
                }
            }
        }

        class InitName: Node() {
            override fun accept(token: Token): Node? {
                return when(token.type) {
                    Token.Type.Equal -> Input()
                    else -> {
                        MessageProvider.error("Expected \"=\", but not found", token.line)
                        null
                    }
                }
            }
        }

        class Name : Node() {
            override fun accept(token: Token): Node? {
                return when(token.type) {
                    Token.Type.Equal, Token.Type.Plus, Token.Type.Minus, Token.Type.Star, Token.Type.Slash, Token.Type.Percent,
                    Token.Type.EqualEqual, Token.Type.NotEqual, Token.Type.Greater, Token.Type.GreaterOrEqual,
                    Token.Type.Less, Token.Type.LessOrEqual, Token.Type.And, Token.Type.Or
                         -> Input()
                    else -> {
                        MessageProvider.error("Expected \"=\" or binary operator, but not found", token.line)
                        null
                    }
                }
            }
        }

        class Input: Node() {
            override fun accept(token: Token): Node? {
                return when(token.type) {
                    Token.Type.LeftParen -> this.also { parenLevel++ }
                    Token.Type.Inverse, Token.Type.Minus -> this
                    Token.Type.Int, Token.Type.Decimal, Token.Type.True, Token.Type.False, Token.Type.String -> Output()
                    Token.Type.Identifier -> {
                        if (analyserEnvironment.isNotExist(token.data as String)) {
                            MessageProvider.error("Undefined variable name \"${token.data}\"", token.line)
                            return null
                        }
                        Output()
                    }
                    else -> {
                        MessageProvider.error("Expected expression, but not found", token.line)
                        null
                    }
                }
            }
        }

        class Output: Node() {
            override fun accept(token: Token): Node? {
                return when(token.type) {
                    Token.Type.RightParen -> this.also { parenLevel-- }
                    Token.Type.Plus, Token.Type.Minus, Token.Type.Star, Token.Type.Slash, Token.Type.Percent,
                    Token.Type.EqualEqual, Token.Type.NotEqual, Token.Type.Greater, Token.Type.GreaterOrEqual,
                    Token.Type.Less, Token.Type.LessOrEqual, Token.Type.And, Token.Type.Or
                        -> Input()
                    Token.Type.EOL -> {
                        if (parenLevel != 0) {
                            MessageProvider.error("Expected \"${if (parenLevel > 0) ')' else '('}\", but not found", token.line)
                            return null
                        }
                        Final()
                    }
                    Token.Type.EOF -> exit(token)
                    else -> {
                        MessageProvider.error("Expected expression, but not found", token.line)
                        null
                    }
                }
            }
        }

        class For: Node() {
            override fun accept(token: Token): Node? {
                return when(token.type) {
                    Token.Type.LeftParen -> ForInitHeader()
                    else -> {
                        MessageProvider.error("Expected \"(\", but not found", token.line)
                        null
                    }
                }
            }
        }

        class ForInitHeader: Node() {
            override fun accept(token: Token): Node? {
                return when(token.type) {
                    Token.Type.Semicolon -> ForInput()
                    Token.Type.Var -> ForInit()
                    else -> {
                        MessageProvider.error("Expected variable initialization or \";\", but not found", token.line)
                        null
                    }
                }
            }
        }

        class ForInit: Node() {
            override fun accept(token: Token): Node? {
                return when(token.type) {
                    Token.Type.Identifier -> {
                        if (analyserEnvironment.isExist(token.data as String)) {
                            MessageProvider.error("Such variable name \"${token.data}\" reserved", token.line)
                            return null
                        }
                        analyserEnvironment.define(token.data as String)
                        ForInitName()
                    }
                    else -> {
                        MessageProvider.error("Expected identifier, but not found", token.line)
                        null
                    }
                }
            }
        }

        class ForInitName: Node() {
            override fun accept(token: Token): Node? {
                return when(token.type) {
                    Token.Type.Equal -> ForInitInput()
                    else -> {
                        MessageProvider.error("Expected \"=\", but not found", token.line)
                        null
                    }
                }
            }
        }

        class ForInitInput: Node() {
            override fun accept(token: Token): Node? {
                return when(token.type) {
                    Token.Type.LeftParen -> this.also { parenLevel++ }
                    Token.Type.Inverse, Token.Type.Minus -> this
                    Token.Type.Int, Token.Type.Decimal, Token.Type.True, Token.Type.False, Token.Type.String -> ForInitOutput()
                    Token.Type.Identifier -> {
                        if (analyserEnvironment.isNotExist(token.data as String)) {
                            MessageProvider.error("Undefined variable name \"${token.data}\"", token.line)
                            return null
                        }
                        ForInitOutput()
                    }
                    else -> {
                        MessageProvider.error("Expected expression, but not found in for initialize construction", token.line)
                        null
                    }
                }
            }
        }

        class ForInitOutput: Node() {
            override fun accept(token: Token): Node? {
                return when(token.type) {
                    Token.Type.RightParen -> this.also { parenLevel-- }
                    Token.Type.Plus, Token.Type.Minus, Token.Type.Star, Token.Type.Slash, Token.Type.Percent,
                    Token.Type.EqualEqual, Token.Type.NotEqual, Token.Type.Greater, Token.Type.GreaterOrEqual,
                    Token.Type.Less, Token.Type.LessOrEqual, Token.Type.And, Token.Type.Or
                         -> ForInitInput()
                    Token.Type.Semicolon -> {
                        if (parenLevel != 0) {
                            MessageProvider.error("Expected \"${if (parenLevel > 0) ')' else '('}\", but not found in for initialize construction", token.line)
                            return null
                        }
                        ForInput()
                    }
                    else -> {
                        MessageProvider.error("Expected expression, but not found in for initialize construction", token.line)
                        null
                    }
                }
            }
        }

        class ForInput: Node() {
            override fun accept(token: Token): Node? {
                return when(token.type) {
                    Token.Type.LeftParen -> this.also { parenLevel++ }
                    Token.Type.Inverse, Token.Type.Minus -> this
                    Token.Type.Int, Token.Type.Decimal, Token.Type.True, Token.Type.False, Token.Type.String -> ForOutput()
                    Token.Type.Identifier -> {
                        if (analyserEnvironment.isNotExist(token.data as String)) {
                            MessageProvider.error("Undefined variable name \"${token.data}\"", token.line)
                            return null
                        }
                        ForOutput()
                    }
                    else -> {
                        MessageProvider.error("Expected expression, but not found in for condition construction", token.line)
                        null
                    }
                }
            }
        }

        class ForOutput: Node() {
            override fun accept(token: Token): Node? {
                return when(token.type) {
                    Token.Type.RightParen -> this.also { parenLevel-- }
                    Token.Type.Plus, Token.Type.Minus, Token.Type.Star, Token.Type.Slash, Token.Type.Percent,
                    Token.Type.EqualEqual, Token.Type.NotEqual, Token.Type.Greater, Token.Type.GreaterOrEqual,
                    Token.Type.Less, Token.Type.LessOrEqual, Token.Type.And, Token.Type.Or
                        -> ForInput()
                    Token.Type.Semicolon -> {
                        if (parenLevel != 0) {
                            MessageProvider.error("Expected \"${if (parenLevel > 0) ')' else '('}\", but not found in for condition construction", token.line)
                            return null
                        }
                        ForSetHeader()
                    }
                    else -> {
                        MessageProvider.error("Expected expression, but not found in for condition construction", token.line)
                        null
                    }
                }
            }
        }

        class ForSetHeader: Node() {
            override fun accept(token: Token): Node? {
                return when(token.type) {
                    Token.Type.RightParen -> ForEnd()
                    Token.Type.Identifier -> {
                        if (analyserEnvironment.isNotExist(token.data as String)) {
                            MessageProvider.error("Undefined variable name \"${token.data}\"", token.line)
                            return null
                        }
                        ForSetName()
                    }
                    else -> {
                        MessageProvider.error("Expected expression or \"${if (parenLevel > 0) ')' else '('}\", but not found in for iterate construction", token.line)
                        null
                    }
                }
            }
        }

        class ForSetName: Node() {
            override fun accept(token: Token): Node? {
                return when(token.type) {
                    Token.Type.Equal -> ForSetInput()
                    else -> {
                        MessageProvider.error("Expected \"=\", but not found in for iterate construction", token.line)
                        null
                    }
                }
            }
        }

        class ForSetInput: Node() {
            override fun accept(token: Token): Node? {
                return when(token.type) {
                    Token.Type.LeftParen -> this.also { parenLevel++ }
                    Token.Type.Inverse, Token.Type.Minus -> this
                    Token.Type.Int, Token.Type.Decimal, Token.Type.True, Token.Type.False, Token.Type.String -> ForSetExp()
                    Token.Type.Identifier -> {
                        if (analyserEnvironment.isNotExist(token.data as String)) {
                            MessageProvider.error("Undefined variable name \"${token.data}\"", token.line)
                            return null
                        }
                        ForSetExp()
                    }
                    else -> {
                        MessageProvider.error("Expected expression, but not found in for iterate construction", token.line)
                        null
                    }
                }
            }
        }

        class ForSetExp: Node() {
            override fun accept(token: Token): Node? {
                return when(token.type) {
                    Token.Type.RightParen -> ForSetGroupEnd().also { parenLevel-- }
                    Token.Type.Plus, Token.Type.Minus, Token.Type.Star, Token.Type.Slash, Token.Type.Percent,
                    Token.Type.EqualEqual, Token.Type.NotEqual, Token.Type.Greater, Token.Type.GreaterOrEqual,
                    Token.Type.Less, Token.Type.LessOrEqual, Token.Type.And, Token.Type.Or
                        -> ForSetInput()
                    else -> {
                        MessageProvider.error("Expected expression, but not found in for iterate construction", token.line)
                        null
                    }
                }
            }
        }

        class ForSetGroupEnd: Node() {
            override fun accept(token: Token): Node? {
                return when(token.type) {
                    Token.Type.RightParen -> this.also { parenLevel-- }
                    Token.Type.Plus, Token.Type.Minus, Token.Type.Star, Token.Type.Slash, Token.Type.Percent,
                    Token.Type.EqualEqual, Token.Type.NotEqual, Token.Type.Greater, Token.Type.GreaterOrEqual,
                    Token.Type.Less, Token.Type.LessOrEqual, Token.Type.And, Token.Type.Or
                        -> ForSetInput()
                    Token.Type.LeftBrace -> {
                        if (parenLevel != -1) {
                            MessageProvider.error("Expected \"${if (parenLevel > -1) ')' else '('}\", but not found in for iterate construction", token.line)
                            return null
                        }
                        CloseBrace().also { parenLevel++; }
                    }
                    else -> {
                        MessageProvider.error("Expected expression, but not found in for iterate construction", token.line)
                        null
                    }
                }
            }
        }

        class ForEnd: Node() {
            override fun accept(token: Token): Node? {
                return when(token.type) {
                    Token.Type.LeftBrace -> CloseBrace()
                    else -> {
                        MessageProvider.error("Expected new line, but not found", token.line)
                        null
                    }
                }
            }
        }

        class If: Node() {
            override fun accept(token: Token): Node? {
                return when(token.type) {
                    Token.Type.LeftParen -> this.also { parenLevel++ }
                    Token.Type.Inverse, Token.Type.Minus -> this
                    Token.Type.Int, Token.Type.Decimal, Token.Type.True, Token.Type.False, Token.Type.String -> IfOutput()
                    Token.Type.Identifier -> {
                        if (analyserEnvironment.isNotExist(token.data as String)) {
                            MessageProvider.error("Undefined variable name \"${token.data}\"", token.line)
                            return null
                        }
                        IfOutput()
                    }
                    else -> {
                        MessageProvider.error("Expected expression, but not found", token.line)
                        null
                    }
                }
            }
        }

        class IfOutput: Node() {
            override fun accept(token: Token): Node? {
                return when(token.type) {
                    Token.Type.RightParen -> this.also { parenLevel-- }
                    Token.Type.Plus, Token.Type.Minus, Token.Type.Star, Token.Type.Slash, Token.Type.Percent,
                    Token.Type.EqualEqual, Token.Type.NotEqual, Token.Type.Greater, Token.Type.GreaterOrEqual,
                    Token.Type.Less, Token.Type.LessOrEqual, Token.Type.And, Token.Type.Or
                        -> If()
                    Token.Type.LeftBrace -> {
                        if (parenLevel != 0) {
                            MessageProvider.error("Expected \"${if (parenLevel > 0) ')' else '('}\", but not found", token.line)
                            return null
                        }
                        CloseBrace()
                    }
                    else -> {
                        MessageProvider.error("Expected expression, but not found", token.line)
                        null
                    }
                }
            }
        }

        class ElseBranch: Node() {
            override fun accept(token: Token): Node? {
                return when(token.type) {
                    Token.Type.If -> If().also {
                        analyserEnvironment.enter()
                        ifStmt++
                        braceLevel++
                    }
                    Token.Type.LeftBrace -> CloseBrace().also {
                        analyserEnvironment.enter()
                        braceLevel++
                    }
                    else -> {
                        MessageProvider.error("Expected if or \"{\", but not found", token.line)
                        null
                    }
                }
            }
        }

        class Print: Node() {
            override fun accept(token: Token): Node? {
                return when(token.type) {
                    Token.Type.EOL -> Final()
                    Token.Type.EOF -> exit(token)
                    Token.Type.LeftParen -> PrintInput().also { parenLevel++ }
                    Token.Type.Inverse, Token.Type.Minus -> PrintInput()
                    Token.Type.Int, Token.Type.Decimal, Token.Type.True, Token.Type.False, Token.Type.String -> PrintOutput()
                    Token.Type.Identifier -> {
                        if (analyserEnvironment.isNotExist(token.data as String)) {
                            MessageProvider.error("Undefined variable name \"${token.data}\"", token.line)
                            return null
                        }
                        PrintOutput()
                    }
                    else -> {
                        MessageProvider.error("Expected expression, but not found", token.line)
                        null
                    }
                }
            }
        }

        class PrintInput: Node() {
            override fun accept(token: Token): Node? {
                return when(token.type) {
                    Token.Type.LeftParen -> this.also { parenLevel++ }
                    Token.Type.Inverse, Token.Type.Minus -> this
                    Token.Type.Int, Token.Type.Decimal, Token.Type.True, Token.Type.False, Token.Type.String -> PrintOutput()
                    Token.Type.Identifier -> {
                        if (analyserEnvironment.isNotExist(token.data as String)) {
                            MessageProvider.error("Undefined variable name \"${token.data}\"", token.line)
                            return null
                        }
                        PrintOutput()
                    }
                    else -> {
                        MessageProvider.error("Expected expression, but not found", token.line)
                        null
                    }
                }
            }
        }

        class PrintOutput: Node() {
            override fun accept(token: Token): Node? {
                return when(token.type) {
                    Token.Type.RightParen -> this.also { parenLevel-- }
                    Token.Type.Plus, Token.Type.Minus, Token.Type.Star, Token.Type.Slash, Token.Type.Percent,
                    Token.Type.EqualEqual, Token.Type.NotEqual, Token.Type.Greater, Token.Type.GreaterOrEqual,
                    Token.Type.Less, Token.Type.LessOrEqual, Token.Type.And, Token.Type.Or
                        -> PrintInput()
                    Token.Type.EOL -> {
                        if (parenLevel != 0) {
                            MessageProvider.error("Expected \"${if (parenLevel > 0) ')' else '('}\", but not found", token.line)
                            return null
                        }
                        Final()
                    }
                    Token.Type.EOF -> exit(token)
                    else -> {
                        MessageProvider.error("Expected expression, but not found", token.line)
                        null
                    }
                }
            }
        }

        class End: Node() {
            override fun accept(token: Token): Node? {
                if (braceLevel != 0) {
                    MessageProvider.error("Expected \"${if (braceLevel > 0) '}' else '{'}\", but not found", token.line)
                    return null
                }
                if (parenLevel != 0) {
                    MessageProvider.error("Expected \"${if (parenLevel > 0) ')' else '('}\", but not found", token.line)
                    return null
                }
                return this
            }
        }

        abstract fun accept(token: Token): Node?

        fun exit(token: Token): Node? {
            return End().accept(token)
        }
    }

}