# jlox

Lox 编程语言的 Java 实现，参考 Robert Nystrom 的著作 [Crafting Interpreters](https://zaslee.github.io/craftinginterpreters/contents.html)。

采用递归下降解析 + 树遍历求值的经典解释器架构。

## 常用命令

```bash
# 生成 AST 类（修改 GenerateAst.java 后需要运行）
./gen.sh

# 运行脚本
./run.sh script.lox

# REPL 交互模式
./run.sh

# 手动编译
javac -d out src/com/craftinginterpreters/lox/*.java
```

## 架构概览

```
源码 (String) → Scanner → Token[] → Parser → Stmt[] (AST) → Interpreter → 求值结果
```

- **Scanner**（词法分析）: 将源码字符串转换为 Token 序列
- **Parser**（语法分析）: 递归下降解析，将 Token 序列构建为 AST
- **Interpreter**（解释求值）: 遍历 AST 执行语句和求值表达式
- **Environment**: 静态作用域，通过链式结构实现嵌套环境
- **Loxcallable**: 可调用对象的抽象接口，为函数实现奠定基础

AST 节点（`Expr.java`、`Stmt.java`）由 `GenerateAst.java` 自动生成，使用 Visitor 模式。

## 完成进度

| 章节 | 内容 | 状态 |
|------|------|------|
| 第4章 | 词法分析（Scanner） | ✅ |
| 第6章 | 语法分析（Parser） | ✅ |
| 第7章 | 表达式求值 | ✅ |
| 第8章 | 语句和状态（变量、赋值、print） | ✅ |
| 第9-10章 | 作用域 | ✅ |
| 第11章 | 控制流（if/while/for/逻辑操作符） | ✅ |
| 第12章 | 函数 | 🚧 |
| 第13章 | 类 | ⬜ |

## 目前支持的功能

- **表达式**: 算术（`+ - * /`）、比较（`> >= < <=`）、相等（`== !=`）、逻辑（`! and or`）、一元负号（`-`）、字符串拼接、函数调用
- **语句**: `var` 声明和初始化、赋值、`print`、表达式语句、`if`/`else`、`while`、`for`
- **作用域**: 块 `{}` 创建嵌套作用域，变量遮蔽，内层可访问外层变量
- **错误处理**: 语法错误恐慌模式恢复 + 运行时错误定位

## 项目结构

```
src/com/craftinginterpreters/lox/
├── Lox.java          # 入口，协调全流程、错误管理
├── Scanner.java      # 词法分析
├── Token.java        # Token 数据结构
├── TokenType.java    # Token 类型枚举
├── Expr.java         # AST 表达式节点（自动生成）
├── Stmt.java         # AST 语句节点（自动生成）
├── Parser.java       # 递归下降解析器
├── Interpreter.java  # 树遍历解释器
├── Environment.java  # 作用域环境（链式）
├── Loxcallable.java  # 可调用对象接口
└── RuntimeError.java # 运行时异常

src/com/craftinginterpreters/tool/
└── GenerateAst.java  # AST 代码生成工具
```

## 参考资料

- [Crafting Interpreters](https://zaslee.github.io/craftinginterpreters/contents.html)
