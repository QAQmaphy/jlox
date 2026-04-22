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
```

## 项目结构

```
src/com/craftinginterpreters/lox/
├── Lox.java         # 主程序入口，交互式解释器
├── Scanner.java     # 词法分析器，将源码转换为 Token 序列
├── Token.java       # Token 表示
├── TokenType.java   # Token 类型枚举
├── Expr.java        # AST 表达式节点（自动生成，使用 Visitor 模式）
├── AstPrinter.java  # 语法树打印程序（用于测试 AST 生成）
└── Parser.java      # 递归下降解析器

src/com/craftinginterpreters/tool/
└── GenerateAst.java # AST 生成工具
```

## 编译输出

编译输出到 `out/` 目录。

## 参考资料

- [Crafting Interpreters](https://craftinginterpreters.com/)
