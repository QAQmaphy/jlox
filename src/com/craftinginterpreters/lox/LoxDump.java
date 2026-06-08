package com.craftinginterpreters.lox;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

// 诊断打印工具：输入 .lox 源文件，输出三栏信息 ——
//   1. Token 表（词法分析结果）
//   2. AST 语法树（语法分析结果）
//   3. 语义分析（变量绑定深度解析）
// 用于答辩时展示解释器各阶段内部产物。
public class LoxDump {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Usage: java com.craftinginterpreters.lox.LoxDump <script.lox>");
            System.exit(64);
        }

        byte[] bytes = Files.readAllBytes(Paths.get(args[0]));
        String source = new String(bytes, Charset.defaultCharset());

        // ── 第 1 阶段：词法分析 → Token 表 ──────────────────
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        printTokenTable(tokens);

        // ── 第 2 阶段：语法分析 → AST ────────────────────────
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        if (Lox.hadError) {
            System.out.println("\n[Parse error — AST may be incomplete]");
            Lox.hadError = false;
        }

        System.out.println();
        printSeparator("SYNTAX TREE (AST)");
        System.out.println();

        AstPrinter printer = new AstPrinter();
        System.out.println(printer.print(statements));

        // ── 第 3 阶段：语义分析 → 变量绑定 ──────────────────
        Interpreter interpreter = new Interpreter();
        Resolver resolver = new Resolver(interpreter);
        resolver.resolve(statements);

        if (Lox.hadError) {
            System.out.println("\n[Resolver error — analysis may be incomplete]");
            Lox.hadError = false;
        }

        System.out.println();
        printSeparator("SEMANTIC ANALYSIS — Variable Resolution");
        System.out.println();
        printResolverInfo(interpreter);
    }

    // ── Token 表格 ─────────────────────────────────────────

    private static void printTokenTable(List<Token> tokens) {
        printSeparator("TOKEN TABLE");
        System.out.println();
        String fmt = "  %-4s  %-18s  %-16s  %s%n";
        System.out.printf(fmt, "Line", "Type", "Lexeme", "Literal");
        System.out.println("  " + "─".repeat(4) + "  " + "─".repeat(18) + "  " + "─".repeat(16) + "  " + "─".repeat(12));

        for (Token t : tokens) {
            String literal = formatLiteral(t.literal);
            System.out.printf(fmt, t.line, t.type, "'" + t.lexeme + "'", literal);
        }
        System.out.println();
        System.out.println("  Total: " + tokens.size() + " tokens");
    }

    // ── 语义分析信息 ───────────────────────────────────────

    private static void printResolverInfo(Interpreter interpreter) {
        Map<Expr, Integer> locals = interpreter.getLocals();

        if (locals.isEmpty()) {
            System.out.println("  (no local variable bindings — all references are global)");
            return;
        }

        System.out.println("  Variable bindings resolved (locals map):");
        System.out.println("  " + "─".repeat(55));
        System.out.printf("  %-6s  %-12s  %-15s  %s%n", "Line", "Name", "Depth", "Scope");
        System.out.println("  " + "─".repeat(55));

        for (Map.Entry<Expr, Integer> entry : locals.entrySet()) {
            Expr expr = entry.getKey();
            int depth = entry.getValue();
            String name = extractName(expr);
            int line = extractLine(expr);
            String scope = describeDepth(depth);

            System.out.printf("  %-6d  %-12s  %-15s  %s%n", line, name, depth + " hops", scope);
        }
        System.out.println("  " + "─".repeat(55));
        System.out.println("  Total bindings: " + locals.size());
    }

    // 从 Expr 节点提取变量名
    private static String extractName(Expr expr) {
        if (expr instanceof Expr.Variable) {
            return ((Expr.Variable) expr).name.lexeme;
        }
        if (expr instanceof Expr.Assign) {
            return ((Expr.Assign) expr).name.lexeme;
        }
        if (expr instanceof Expr.This) {
            return "this";
        }
        return "?";
    }

    // 从 Expr 节点提取行号
    private static int extractLine(Expr expr) {
        if (expr instanceof Expr.Variable) {
            return ((Expr.Variable) expr).name.line;
        }
        if (expr instanceof Expr.Assign) {
            return ((Expr.Assign) expr).name.line;
        }
        if (expr instanceof Expr.This) {
            return ((Expr.This) expr).keyword.line;
        }
        return 0;
    }

    // 对深度作友好描述
    private static String describeDepth(int depth) {
        if (depth == 0) return "local (current)";
        if (depth == 1) return "enclosing (1 up)";
        return "enclosing (" + depth + " up)";
    }

    // ── 格式化工具 ─────────────────────────────────────────

    private static void printSeparator(String title) {
        int width = 60;
        int pad = (width - title.length() - 2) / 2;
        String line = "═".repeat(width);
        System.out.println("╔" + line + "╗");
        System.out.println("║" + " ".repeat(pad) + title + " ".repeat(width - pad - title.length() - 2) + "║");
        System.out.println("╚" + line + "╝");
    }

    private static String formatLiteral(Object literal) {
        if (literal == null) return "nil";
        if (literal instanceof String) return "\"" + literal + "\"";
        if (literal instanceof Double) {
            String text = literal.toString();
            if (text.endsWith(".0")) text = text.substring(0, text.length() - 2);
            return text;
        }
        return literal.toString();
    }
}
