package core.stages.scanner

import core.models.Token
import core.utils.MessageProvider

class Scanner {

    private lateinit var source: String
    private var tokens = ArrayList<Token>()

    private var start = 0
    private var current = 0
    private var line = 1


    fun getTokens(source: String): ArrayList<Token> {
        this.source = source
        this.tokens.clear()
        start = 0
        current = 0
        line = 1

        while (isNotAtEnd()) {
            start = current

            when(val symbol = advance()) {
                '+' -> addToken(Token.Type.Plus)
                '-' -> addToken(Token.Type.Minus)
                '/' -> addToken(Token.Type.Slash)
                '%' -> addToken(Token.Type.Percent)
                '*' -> addToken(Token.Type.Star)
                ';' -> addToken(Token.Type.Semicolon)
                '{' -> addToken(Token.Type.LeftBrace)
                '}' -> addToken(Token.Type.RightBrace)
                '(' -> addToken(Token.Type.LeftParen)
                ')' -> addToken(Token.Type.RightParen)

                '<' -> addToken(if (match('=')) Token.Type.LessOrEqual else Token.Type.Less)
                '>' -> addToken(if (match('=')) Token.Type.GreaterOrEqual else Token.Type.Greater)
                '=' -> addToken(if (match('=')) Token.Type.EqualEqual else Token.Type.Equal)
                '!' -> {
                    if (match('='))
                        addToken(Token.Type.NotEqual)
                    else
                        MessageProvider.error("Could not recognize symbol \"!\"", line)
                }

                ' ', '\r', '\t' -> {  }
                '\n' -> {
                    if (tokens.isNotEmpty() && tokens.last().type != Token.Type.EOL) {
                        addToken(Token.Type.EOL)
                    }
                    line++
                }

                '"' -> string()
                in '0'..'9' -> number()
                in 'a'..'z', in 'A'..'Z', '_' -> identifier()

                else -> {
                    MessageProvider.error("Could not recognize symbol \"$symbol\"")
                }
            }

        }

        addToken(Token.Type.EOF)

        return tokens
    }

    private fun isAtEnd() = current >= source.length
    private fun isNotAtEnd() = !isAtEnd()

    private fun advance() = source[current++]
    private fun peek() = if (isAtEnd()) 0.toChar() else source[current]
    private fun peekNext() = if (current + 1 >= source.length) 0.toChar() else source[current + 1]
    private fun match(symbol: Char): Boolean {
        if (isAtEnd() || source[current] != symbol) return false
        advance()
        return true
    }
    private fun prepareStringParse() { start++; current-- }
    private fun resetAfterStringParse() { current++ }

    private fun string() {
        while (peek() != '"' && isNotAtEnd()) {
            if (peek() == '\n') {
                line++
                MessageProvider.error("Unterminated string \"${source.substring(start, current)}\"", line)
            }
            advance()
        }

        if (isAtEnd()) {
            MessageProvider.error("Unterminated string \"${source.substring(start, current)}\"", line)
            return
        }

        advance()

        prepareStringParse()
        addToken(Token.Type.String, String::class.java)
        resetAfterStringParse()

    }

    private fun number() {
        while (peek().isDigit()) advance();

        if (peek() == '.' && peekNext().isDigit()) {
            advance();
            while (peek().isDigit()) advance();
            addToken(Token.Type.Decimal, Double::class.java)
        } else {
            addToken(Token.Type.Int, Int::class.java)
        }
    }

    private fun identifier() {
        while (peek() in 'a'..'z' || peek() in 'A'..'Z' || peek() == '_' || peek().isDigit()) advance();

        val keyWord = checkKeyWord()
        if (keyWord != null)
            addToken(keyWord)
        else
            addToken(Token.Type.Identifier, String::class.java)
    }

    private fun checkKeyWord(): Token.Type? {
        return Token.keyWords[source.substring(start, current)]
    }

    private fun addToken(typeToken: Token.Type, typeClass: Class<*>? = null) {
        val data: Any? = if (typeClass != null) {
            val value = source.substring(start, current)
            when(typeClass) {
                Int::class.java -> value.toInt()
                Double::class.java -> value.toDouble()
                Boolean::class.java -> value.toBoolean()
                String::class.java -> value
                else -> {
                    MessageProvider.error("Could not parse $value to $typeClass")
                    null
                }
            }
        } else {
            null
        }

        tokens.add(Token(
                line = line,
                data = data,
                type = typeToken
        ))
    }

}