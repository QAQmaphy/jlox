package com.craftinginterpreters.lox;

import java.util.List;

// AST 美化打印器：将表达式树和语句树输出为可读的 S-表达式格式，
// 带层级缩进，用于答辩时展示 Parser 生成的语法树结构。
class AstPrinter implements Expr.Visitor<String>, Stmt.Visitor<String> {

    String print(List<Stmt> statements) {
        StringBuilder sb = new StringBuilder();
        sb.append("(program");
        if (statements.isEmpty()) {
            sb.append(")");
            return sb.toString();
        }
        sb.append("\n");
        for (Stmt s : statements) {
            sb.append(indent(s.accept(this), 2));
            sb.append("\n");
        }
        sb.append(")");
        return sb.toString();
    }

    // ── 工具方法 ───────────────────────────────────────

    // 在字符串的每一行前添加 n 个空格的缩进
    private static String indent(String s, int n) {
        String pad = " ".repeat(n);
        return pad + s.replace("\n", "\n" + pad);
    }

    // 渲染带有子语句体的结构，如 block / function body / class body
    private String formatBody(String name, List<Stmt> body) {
        StringBuilder sb = new StringBuilder();
        sb.append("(").append(name);
        if (body.isEmpty()) {
            sb.append(")");
            return sb.toString();
        }
        sb.append("\n");
        for (Stmt s : body) {
            sb.append(indent(s.accept(this), 2));
            sb.append("\n");
        }
        sb.append(")");
        return sb.toString();
    }

    // ── Expr 访问者（返回紧凑的内联字符串）──────────────

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitCallExpr(Expr.Call expr) {
        StringBuilder sb = new StringBuilder();
        sb.append("(call ");
        sb.append(expr.callee.accept(this));
        for (Expr arg : expr.arguments) {
            sb.append(" ");
            sb.append(arg.accept(this));
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String visitGetExpr(Expr.Get expr) {
        return "(. " + expr.object.accept(this) + " " + expr.name.lexeme + ")";
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return "(group " + expr.expression.accept(this) + ")";
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) return "nil";
        if (expr.value instanceof String) return "\"" + expr.value + "\"";
        if (expr.value instanceof Boolean) return expr.value.toString();
        if (expr.value instanceof Double) {
            String text = expr.value.toString();
            if (text.endsWith(".0")) text = text.substring(0, text.length() - 2);
            return text;
        }
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return "(" + expr.operator.lexeme + " " + expr.right.accept(this) + ")";
    }

    @Override
    public String visitLogicalExpr(Expr.Logical expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitSetExpr(Expr.Set expr) {
        return "(set " + expr.object.accept(this) + " " + expr.name.lexeme + " " + expr.value.accept(this) + ")";
    }

    @Override
    public String visitThisExpr(Expr.This expr) {
        return "this";
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        return expr.name.lexeme;
    }

    @Override
    public String visitAssignExpr(Expr.Assign expr) {
        return "(= " + expr.name.lexeme + " " + expr.value.accept(this) + ")";
    }

    // ── Stmt 访问者（可能包含多行输出）──────────────────

    @Override
    public String visitExpressionStmt(Stmt.Expression stmt) {
        return "(expr " + stmt.expression.accept(this) + ")";
    }

    @Override
    public String visitPrintStmt(Stmt.Print stmt) {
        return "(print " + stmt.expression.accept(this) + ")";
    }

    @Override
    public String visitVarStmt(Stmt.Var stmt) {
        if (stmt.initializer != null) {
            return "(var " + stmt.name.lexeme + " " + stmt.initializer.accept(this) + ")";
        }
        return "(var " + stmt.name.lexeme + ")";
    }

    @Override
    public String visitBlockStmt(Stmt.Block stmt) {
        return formatBody("block", stmt.statements);
    }

    @Override
    public String visitIfStmt(Stmt.If stmt) {
        StringBuilder sb = new StringBuilder();
        sb.append("(if ").append(stmt.condition.accept(this));
        sb.append("\n");
        sb.append(indent(stmt.thenBranch.accept(this), 2));
        if (stmt.elseBranch != null) {
            sb.append("\n");
            sb.append(indent(stmt.elseBranch.accept(this), 2));
        }
        sb.append("\n)");
        return sb.toString();
    }

    @Override
    public String visitWhileStmt(Stmt.While stmt) {
        StringBuilder sb = new StringBuilder();
        sb.append("(while ").append(stmt.condition.accept(this));
        sb.append("\n");
        sb.append(indent(stmt.body.accept(this), 2));
        sb.append("\n)");
        return sb.toString();
    }

    @Override
    public String visitFunctionStmt(Stmt.Function stmt) {
        StringBuilder sb = new StringBuilder();
        sb.append("(fun ").append(stmt.name.lexeme).append(" (");
        for (int i = 0; i < stmt.params.size(); i++) {
            if (i > 0) sb.append(" ");
            sb.append(stmt.params.get(i).lexeme);
        }
        sb.append(")");
        sb.append("\n");
        sb.append(indent(formatBody("body", stmt.body), 2));
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String visitReturnStmt(Stmt.Return stmt) {
        if (stmt.value != null) {
            return "(return " + stmt.value.accept(this) + ")";
        }
        return "(return)";
    }

    @Override
    public String visitClassStmt(Stmt.Class stmt) {
        StringBuilder sb = new StringBuilder();
        sb.append("(class ").append(stmt.name.lexeme);
        if (stmt.methods.isEmpty()) {
            sb.append(")");
            return sb.toString();
        }
        sb.append("\n");
        for (Stmt.Function method : stmt.methods) {
            sb.append(indent(visitFunctionStmt(method), 2));
            sb.append("\n");
        }
        sb.append(")");
        return sb.toString();
    }

    // ── 私有辅助 ────────────────────────────────────────

    private String parenthesize(String name, Expr... exprs) {
        StringBuilder sb = new StringBuilder();
        sb.append("(").append(name);
        for (Expr expr : exprs) {
            sb.append(" ");
            sb.append(expr.accept(this));
        }
        sb.append(")");
        return sb.toString();
    }
}
