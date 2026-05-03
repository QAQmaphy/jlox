# jlox 架构文档

## 概述

jlox 是 Lox 编程语言的 Java 实现，参考 Robert Nystrom 的著作 [Crafting Interpreters](https://craftinginterpreters.com/)。采用递归下降解析方法，将源码逐步转换为 Token、AST，最终求值得到结果。

## 整体架构

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   源码       │────▶│  Scanner    │────▶│   Parser    │────▶│ Interpreter │
│  (String)    │     │  词法分析器  │     │  语法分析器   │     │   表达式求值 │
└─────────────┘     └─────────────┘     └─────────────┘     └─────────────┘
                           │                   │                   │
                           ▼                   ▼                   ▼
                      Token[]              Expr              Object (Value)
```

解释器执行流程：

1. **Scanner** 接收源码字符串，输出 `Token[]`
2. **Parser** 接收 `Token[]`，输出 `Expr`（AST）
3. **Interpreter** 接收 `Expr`，递归求值，输出 `Object`（值）

## 模块详解

### 1. Lox.java（入口）

主程序入口，支持两种运行模式：

- **脚本模式**：`java -cp out com.craftinginterpreters.lox.Lox script.lox`
- **REPL 模式**：`java -cp out com.craftinginterpreters.lox.Lox`

```java
public static void main(String[] args) {
    if (args.length > 1) { ... }
    else if (args.length == 1) { runFile(args[0]); }
    else { runPrompt(); }
}
```

职责：
- 协调 Scanner、Parser、Interpreter 的调用
- 管理错误状态（hadError, hadRuntimeError）
- 提供 `error()` 方法供各模块报告错误

### 2. Scanner.java（词法分析器）

将源码字符串转换为 Token 序列。

```java
List<Token> scanTokens()  // 入口方法
```

Token 识别规则：
- 单字符 tokens：`(` `)` `{` `}` `,` `.` `-` `+` `;` `*`
- 双字符 tokens：`!=` `==` `<=` `>=`
- 关键字：`and`, `class`, `else`, `false`, `for`, `fun`, `if`, `nil`, `or`, `print`, `return`, `super`, `this`, `true`, `var`, `while`
- 字面量：标识符、数字、字符串

内部状态：
- `source`：原始源码
- `start`/`current`：扫描区间
- `line`：行号追踪

### 3. Token.java & TokenType.java

**Token**：Token 的数据结构

```java
class Token {
    TokenType type;    // 类型
    String lexeme;     // 词素（源码中的原始文本）
    Object literal;    // 字面量值（数字、字符串等）
    int line;          // 行号
}
```

**TokenType**：所有 Token 类型的枚举

```java
enum TokenType {
    LEFT_PAREN, RIGHT_PAREN, ...  // 单字符
    BANG, BANG_EQUAL, ...         // 一或二字符
    IDENTIFIER, STRING, NUMBER,  // 字面量
    AND, CLASS, FALSE, ...       // 关键字
    EOF                           // 文件结束
}
```

### 4. Expr.java（AST 表达式节点）

使用 **Visitor 模式** 的 AST 节点层次结构。

```java
abstract class Expr {
    interface Visitor<R> { ... }  // Visitor 接口

    static class Binary  extends Expr { ... }
    static class Grouping extends Expr { ... }
    static class Literal  extends Expr { ... }
    static class Unary    extends Expr { ... }

    abstract <R> R accept(Visitor<R> visitor);
}
```

每个表达式节点：
- 持有子节点/值
- 实现 `accept()` 方法，调用 `visitor.visitXxxExpr(this)`

### 5. Parser.java（递归下降解析器）

将 Token 序列构建为 AST。

**解析方法对应运算符优先级**（从低到高）：

```
expression()    ← 入口，可扩展为语句级别
  └─ equality()     处理 == !=
       └─ comparison()   处理 > >= < <=
            └─ term()        处理 + -
                 └─ factor()     处理 * /
                      └─ unary()     处理 ! -
                           └─ primary()  处理字面量和分组
```

**关键方法**：
- `match(TokenType... types)`：尝试匹配并消费一个 Token
- `consume(TokenType, message)`：消费期望的 Token 或报错
- `synchronize()`：恐慌模式错误恢复，跳到下一个语句开头

### 6. Interpreter.java（表达式求值器）

递归遍历 AST，对表达式求值。

```java
class Interpreter implements Expr.Visitor<Object> {
    void interpret(Expr expression) { ... }
}
```

求值策略：
- `evaluate(Expr)` → `expr.accept(this)`（双分发）
- `visitXxxExpr` 处理具体节点类型，返回求值结果

支持运算：
- 算术：`+` `-` `*` `/`
- 比较：`>` `>=` `<` `<=` `==` `!=`
- 字符串拼接：`"hello" + " world"`
- 逻辑非：`!`
- 一元负号：`-`

### 7. AstPrinter.java（调试工具）

实现 `Expr.Visitor<String>`，以 S 表达式格式打印 AST。

```java
// 输入：(1 + 2) * 3
// 输出：(* (group (+ 1 2)) 3)
```

### 8. RuntimeError.java

运行时错误异常，携带出错的 Token 信息。

```java
class RuntimeError extends RuntimeException {
    final Token token;
}
```

### 9. GenerateAst.java（代码生成器）

AST 代码生成工具，位于 `src/com/craftinginterpreters/tool/`。

运行 `./gen.sh` 生成 `Expr.java`。

定义规范：
```java
defineAst(outputDir, "Expr", Arrays.asList(
    "Binary   : Expr left, Token operator, Expr right",
    "Grouping : Expr expression",
    "Literal  : Object value",
    "Unary    : Token operator, Expr right"
));
```

## 模块关系图

```
                    ┌─────────────────────────────────────────┐
                    │                  Lox                    │
                    │  (协调 Scanner/Parser/Interpreter)      │
                    └─────────────────────────────────────────┘
                                      │
              ┌───────────────────────┼───────────────────────┐
              ▼                       ▼                       ▼
      ┌───────────────┐       ┌───────────────┐       ┌───────────────┐
      │    Scanner    │       │    Parser     │       │  Interpreter  │
      │               │       │               │       │               │
      │ 输入: String  │       │ 输入: Token[] │       │ 输入: Expr    │
      │ 输出: Token[] │       │ 输出: Expr    │       │ 输出: Object  │
      └───────────────┘       └───────────────┘       └───────────────┘
              │                       │                       │
              │                       │                       │
              ▼                       ▼                       ▼
      ┌───────────────┐       ┌───────────────┐       ┌───────────────┐
      │     Token     │       │     Expr      │       │  RuntimeError │
      │  lexeme/type  │       │  AST 节点     │       │  异常携带token│
      │  literal/line  │       │  Visitor模式  │       │               │
      └───────────────┘       └───────────────┘       └───────────────┘
                                      │
                          ┌───────────┼───────────┐
                          ▼           ▼           ▼
                    ┌─────────┐ ┌─────────┐ ┌─────────┐
                    │ Binary  │ │Grouping │ │ Literal │
                    │Unary等  │ │         │ │         │
                    └─────────┘ └─────────┘ └─────────┘
```

## 数据结构转换

| 阶段 | 输入 | 输出 |
|------|------|------|
| Scanner | `"1 + 2 * 3"` | `Token(NUMBER,"1"), Token(PLUS,"+"), Token(NUMBER,"2"), Token(STAR,"*"), Token(NUMBER,"3")` |
| Parser | `Token[]` | `Expr.Binary(left=Expr.Literal(1), op=Token(PLUS), right=Expr.Binary(...))` |
| Interpreter | `Expr` | `7.0` |

## 文件结构

```
src/com/craftinginterpreters/
├── lox/
│   ├── Lox.java          # 入口，REPL/脚本执行
│   ├── Scanner.java      # 词法分析器
│   ├── Token.java        # Token 数据结构
│   ├── TokenType.java    # Token 类型枚举
│   ├── Expr.java         # AST 表达式节点（自动生成）
│   ├── AstPrinter.java   # AST 打印工具（调试）
│   ├── Parser.java       # 递归下降解析器
│   ├── Interpreter.java  # 表达式求值器
│   └── RuntimeError.java # 运行时异常
│
└── tool/
    └── GenerateAst.java  # AST 代码生成工具
```

## 编译与运行

```bash
# 编译
javac -d out src/com/craftinginterpreters/lox/*.java

# 生成 AST（修改 GenerateAst.java 后）
./gen.sh

# 运行脚本
java -cp out com.craftinginterpreters.lox.Lox script.lox

# REPL 交互
java -cp out com.craftinginterpreters.lox.Lox
```