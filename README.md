# jlox

Lox 编程语言的 Java 实现，参考 Robert Nystrom 的著作 [Crafting Interpreters](https://craftinginterpreters.com/)。

## 常用命令

```bash
# 生成 AST 类（修改 GenerateAst.java 后需要运行）
./gen.sh

# 运行 Lox 解释器
./run.sh [script.lox]

# 编译所有源文件
javac -d out src/com/craftinginterpreters/lox/*.java

# 运行 REPL（交互式）
java -cp out com.craftinginterpreters.lox.Lox

# 运行脚本
java -cp out com.craftinginterpreters.lox.Lox script.lox
```

## 项目结构

```
src/com/craftinginterpreters/lox/
├── Lox.java         # 主程序入口，支持 REPL 和脚本执行
├── Scanner.java     # 词法分析器，将源码转换为 Token 序列（第4章）
├── Token.java       # Token 数据结构
├── TokenType.java   # Token 类型枚举
├── Expr.java        # AST 表达式节点（自动生成，使用 Visitor 模式）
├── AstPrinter.java  # 语法树打印程序（调试用）
├── Parser.java      # 递归下降解析器，含恐慌模式错误恢复（第6章）
├── Interpreter.java # 表达式求值器（第7章）
└── RuntimeError.java # 运行时错误

src/com/craftinginterpreters/tool/
└── GenerateAst.java # AST 代码生成工具
```

## 完成进度

| 章节 | 内容 | 状态 |
|------|------|------|
| 第4章 | 词法分析（Scanner） | ✅ 已完成 |
| 第6章 | 语法分析（Parser） | ✅ 已完成 |
| 第7章 | 表达式求值（Interpreter） | ⬜ 待实现 |
| 第8章 | 语句和状态 | ⬜ 待实现 |
| 第9章 | 控制流 | ⬜ 待实现 |
| 第10章 | 函数 | ⬜ 待实现 |
| 第11章 | 变量解析 | ⬜ 待实现 |
| 第12章 | 类 | ⬜ 待实现 |
| 第13章 | 继承 | ⬜ 待实现 |

## 目前支持的功能

- 词法分析：识别关键字、标识符、数字、字符串、运算符等所有 Token 类型
- 语法分析：支持二元运算（`+` `-` `*` `/` `==` `!=` `<` `>` `<=` `>=`）、一元运算（`!` `-`）、分组 `()`、字面量
- 运算符优先级：通过递归下降函数嵌套隐式处理
- 错误恢复：恐慌模式（Panic Mode），一次运行可报告多个语法错误
- AST 可视化：通过 AstPrinter 打印语法树结构

## 编译输出

编译输出到 `out/` 目录。

## 参考资料

- [Crafting Interpreters](https://craftinginterpreters.com/)