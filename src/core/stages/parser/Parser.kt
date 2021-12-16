package core.stages.parser

import core.models.Token
import core.stages.analyser.Analyser
import core.utils.MessageProvider
import java.lang.Exception
import kotlin.math.exp

class Parser {

    private var current = 0
    private var tokens = ArrayList<Token>()

    fun process(tokens: ArrayList<Token>): ArrayList<Stmt> {
        this.tokens = tokens
        current = 0

        val statements = ArrayList<Stmt>()

        while (isNotAtEnd()) {
            try {
                if (isNotAtEnd())
                    statements.add(statement())
            } catch (exception: Exception) {
                var token = peek()
                while (token.type != Token.Type.EOL || token.type != Token.Type.LeftBrace && isNotAtEnd()) {
                    token = advance()
                }
            }
        }

        return statements
    }

    private fun statement(): Stmt {
        if (match(arrayOf(Token.Type.Var))) return varStatement()
        if (match(arrayOf(Token.Type.Print))) return printStatement()
        if (match(arrayOf(Token.Type.Println))) return printStatement(true)
        if (match(arrayOf(Token.Type.If))) return ifStatement()
        if (match(arrayOf(Token.Type.For))) return forStatement()
        if (match(arrayOf(Token.Type.LeftBrace))) {
            advance()
            return Stmt.Block(block())
        }

        return expressionStatement()
    }

    private fun ifStatement(): Stmt {
        val condition = or()
        advance()
        advance()
        val thenBranch = Stmt.Block(block())
        var elseBranch: Stmt? = null
        if (match(arrayOf(Token.Type.Else))) {
            elseBranch = if (match(arrayOf(Token.Type.If)))
                ifStatement()
            else {
                advance()
                advance()
                Stmt.Block(block())
            }
        }

        return Stmt.If(condition, thenBranch, elseBranch)
    }

    private fun forStatement(): Stmt {
        advance()

        var init: Stmt? = null
        if (match(arrayOf(Token.Type.Var)))
            init = varStatement()
        else
            advance()

        val condition = or()
        advance()

        var iterate: Stmt? = null
        if (peek().type == Token.Type.Identifier)
            iterate = expressionStatement()
        else
            advance()

        advance()
        advance()
        val body = Stmt.Block(block())

        return Stmt.Block(arrayListOf(Stmt.For(init, condition, iterate, body)))
    }

    private fun varStatement(): Stmt {
        val name = advance()
        advance()
        val expr = or()
        advance()
        return Stmt.Var(name, expr)
    }

    private fun expressionStatement(): Stmt {
        val expr = expression()
        advance()
        return Stmt.Expression(expr)
    }

    private fun printStatement(withNewLine: Boolean = false): Stmt {

        if (match(arrayOf(Token.Type.EOL, Token.Type.EOF)))
            return Stmt.Print(Expr.Literal("", previous().line), withNewLine)

        val expr = expression()
        advance()
        return Stmt.Print(expr, withNewLine)
    }



    private fun expression(): Expr {
        val expr = or()
        if (match(arrayOf(Token.Type.Equal))) {
            val value = or()
            return Expr.Assign((expr as Expr.Variable).name, value)
        }
        return expr
    }

    private fun or(): Expr {
        var expr = and()

        while (match(arrayOf(Token.Type.Or))) {
            val operator = previous()
            val right = and()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun and(): Expr {
        var expr = equality()

        while (match(arrayOf(Token.Type.And))) {
            val operator = previous()
            val right = equality()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun equality(): Expr {
        var expr = comparison()

        while (match(arrayOf(Token.Type.EqualEqual, Token.Type.NotEqual))) {
            val operator = previous()
            val right = comparison()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun comparison(): Expr {
        var expr = term()

        while (match(arrayOf(Token.Type.Less, Token.Type.LessOrEqual, Token.Type.Greater, Token.Type.GreaterOrEqual))) {
            val operator = previous()
            val right = term()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun term(): Expr {
        var expr = factor()

        while (match(arrayOf(Token.Type.Plus, Token.Type.Minus))) {
            val operator = previous()
            val right = factor()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun factor(): Expr {
        var expr = unary()

        while (match(arrayOf(Token.Type.Star, Token.Type.Slash, Token.Type.Percent))) {
            val operator = previous()
            val right = unary()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun unary(): Expr {
        if (match(arrayOf(Token.Type.Inverse, Token.Type.Minus))) {
            val operator = previous()
            val expr = unary()

            return Expr.Unary(operator, expr)
        }
        return primary()
    }

    private fun primary(): Expr {

        if (match(arrayOf(Token.Type.False))) return Expr.Literal(false, previous().line)
        if (match(arrayOf(Token.Type.True))) return Expr.Literal(true, previous().line)
        if (match(arrayOf(Token.Type.Int))) return Expr.Literal(previous().data as Int, previous().line)
        if (match(arrayOf(Token.Type.Decimal))) return Expr.Literal(previous().data as Double, previous().line)
        if (match(arrayOf(Token.Type.String))) return Expr.Literal(previous().data as String, previous().line)

        if (match(arrayOf(Token.Type.Identifier))) return Expr.Variable(previous())

        if (match(arrayOf(Token.Type.LeftParen))) return Expr.Group(or()).also { advance() }

        MessageProvider.error("Unexpected ${peek().type}", peek().line).also { advance() }
        throw Exception()
    }

    private fun isAtEnd() = current >= tokens.size || peek().type == Token.Type.EOF
    private fun isNotAtEnd() = !isAtEnd()

    private fun peek() = tokens[current]
    private fun check(type: Token.Type) = if (isAtEnd()) false else peek().type == type

    private fun advance(): Token = tokens[current++]
    private fun previous(): Token =  tokens[current - 1]

    private fun match(types: Array<Token.Type>): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }
        return false
    }

    private fun block(): ArrayList<Stmt> {
        val list = ArrayList<Stmt>()
        while (!check(Token.Type.RightBrace)) {
            list.add(statement())
        }
        advance()
        if (peek().type == Token.Type.EOL)
            advance()
        return list
    }

}