package core.stages.translator

import core.stages.parser.Expr
import core.stages.parser.Stmt
import core.stages.translator.components.Environment
import core.stages.translator.components.ExprComponent
import core.utils.MessageProvider
import java.lang.Exception
import kotlin.math.exp

class Compiler: ExprComponent(), Stmt.Visitor<Unit> {

    fun compile(stmts: ArrayList<Stmt>): Boolean {
        environment = Environment()

        for (stmt in stmts) {
            try {
                stmt.accept(this)
            } catch (exception: Exception) {  }
        }

        return !MessageProvider.hasError
    }

    override fun visitExpression(stmt: Stmt.Expression) {
        accept(stmt.expr)
    }

    override fun visitPrint(stmt: Stmt.Print) {  }

    override fun visitBlock(stmt: Stmt.Block) {
        val previousEnvironment = environment
        environment = Environment(environment)
        for (scopeStmt in stmt.stmts) {
            scopeStmt.accept(this)
        }
        environment = previousEnvironment
    }

    override fun visitVar(stmt: Stmt.Var) {
        val value = accept(stmt.expr)
        environment.define(stmt.name, value!!)
    }

    override fun visitIf(stmt: Stmt.If) {
        when(accept(stmt.condition)) {
            is Boolean -> {
                stmt.thenBranch.accept(this)
                stmt.elseBranch?.accept(this)
            }
            else -> MessageProvider.error("Could not cast condition to boolean", getLine(stmt.condition))
        }
    }

    override fun visitFor(stmt: Stmt.For) {
        stmt.init?.accept(this)
        when(accept(stmt.condition)) {
            !is Boolean -> MessageProvider.error("Could not cast condition to boolean", getLine(stmt.condition))
        }
        stmt.body.accept(this)
        stmt.iterate?.accept(this)
    }

    override fun visitAssign(expr: Expr.Assign): Any? {
        val value = accept(expr.value)
        environment.check(expr.name, value!!)
        return value
    }

}