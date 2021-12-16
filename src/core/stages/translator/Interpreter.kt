package core.stages.translator

import core.stages.parser.Expr
import core.stages.parser.Stmt
import core.stages.translator.components.Environment
import core.stages.translator.components.ExprComponent
import core.utils.MessageProvider

class Interpreter: ExprComponent(), Stmt.Visitor<Unit> {

    fun run(stmts: ArrayList<Stmt>) {

        environment = Environment()

        for (stmt in stmts)
            stmt.accept(this)
    }

    override fun visitExpression(stmt: Stmt.Expression) {
        accept(stmt.expr)
    }

    override fun visitPrint(stmt: Stmt.Print) {
        val expr = accept(stmt.expr)
        if (stmt.withNewLine)
            println(expr)
        else
            print(expr)
    }

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
        when(val condition = accept(stmt.condition)) {
            is Boolean -> {
                if (condition)
                    stmt.thenBranch.accept(this)
                else
                    stmt.elseBranch?.accept(this)
            }
            else -> MessageProvider.critical("Could not cast condition to boolean", getLine(stmt.condition))
        }
    }

    override fun visitFor(stmt: Stmt.For) {
        stmt.init?.accept(this)
        while (when(val condition = accept(stmt.condition)) {
                    is Boolean -> condition
                    else -> {
                        MessageProvider.critical("Could not cast condition to boolean", getLine(stmt.condition))
                        false
                    }
                }) {

            stmt.body.accept(this)
            stmt.iterate?.accept(this)
        }
    }

    override fun visitAssign(expr: Expr.Assign): Any? {
        val value = accept(expr.value)
        environment.assign(expr.name, value!!)
        return value
    }

}