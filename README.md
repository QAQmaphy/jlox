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
源码 (String) → Scanner → Token[] → Parser → AST (Stmt[]) → Resolver → Interpreter → 求值结果
                                                                   │
                                                                   └── 计算每个变量的
                                                                       作用域深度，存入
                                                                       locals 表
```

### 各阶段职责

| 阶段 | 文件 | 输入 | 输出 | 核心工作 |
|------|------|------|------|---------|
| 词法分析 | `Scanner.java` | 源码字符串 | `List<Token>` | 将字符序列切成有意义的 Token（关键字、标识符、运算符、字面量） |
| 语法分析 | `Parser.java` | `List<Token>` | `List<Stmt>`（AST） | 递归下降解析，按运算符优先级构建 AST 树 |
| 语义分析 | `Resolver.java` | AST | `Map<Expr, Integer>`（locals 表） | 遍历 AST 进行名称解析，计算每个变量的作用域深度 |
| 解释执行 | `Interpreter.java` | AST + locals 表 | 程序输出 | 遍历 AST 执行语句、求值表达式 |

### Parser — 如何构建 AST

Parser 使用**递归下降 + 运算符优先级爬升**：

- **语句层**：`declaration()` 按 Token 类型分发 → `classDeclaration()` / `function()` / `varDeclaration()` / `statement()`。statement 再按 `if`/`for`/`while`/`print`/`return`/`{}` 分发到对应的解析方法。`for` 循环在解析时直接脱糖为 `while` + 初始化/递增块的组合。
- **表达式层**：每种优先级对应一个方法，从低到高逐级调用：
  `assignment()` → `equality()` → `comparison()` → `term()` → `factor()` → `unary()` → `call()` → `primary()`
  每级方法先解析左操作数（调用下一级），然后 while 循环匹配当前级运算符，构造 `Expr.Binary` 节点。`primary()` 是递归终点，直接消费字面量 Token 创建叶子节点。
- **错误恢复**：恐慌模式 — 遇到语法错误时跳过 Token 直到找到语句边界（`;` 或下一个关键字），可以在一次编译中报告多个错误。

### Resolver — 如何进行名称解析

Resolver 实现 `Expr.Visitor<Void>` 和 `Stmt.Visitor<Void>`，是 Parser 和 Interpreter 之间的语义分析阶段：

- **作用域栈**（`Stack<Map<String, Boolean>>`）：模拟词法作用域嵌套。进入 `{}` 块时 `push`，离开时 `pop`；Map 存变量名 → `false`（已声明未定义）/ `true`（已定义可用）。
- **两步协议**（`declare` → `define`）：`visitVarStmt` 先 `declare(name)` 标记 `false`，再解析初始化表达式，最后 `define(name)` 翻转为 `true`。`visitVariableExpr` 检测到 `false` 时报告 "Can't read local variable in its own initializer"。
- **深度计算**（`resolveLocal`）：从作用域栈顶向底遍历，找到声明该变量的作用域后，计算 `depth = scopes.size() - 1 - index`，存入解释器的 `locals` 表。
- **函数处理**：`visitFunctionStmt` 先 `declare`+`define` 函数名（使递归可用），再进入 `resolveFunction` 创建新作用域、声明参数、解析函数体。`visitReturnStmt` 通过 `FunctionType` 枚举检查 `return` 是否在函数/方法内。
- **类处理**：`visitClassStmt` 立即声明并定义类名，在类作用域中预注入 `this`，然后对各方法调用 `resolveFunction`（标记为 `METHOD`）。

### Interpreter — 如何执行

Interpreter 同样实现 Visitor，持有 Resolver 产出的 `locals` 表：

- **变量查找**：`lookUpVariable` 从 `locals` 表取 `depth`，调用 `environment.getAt(depth, name)` 沿环境链跳固定层数取变量 — O(1) 而非逐层遍历。
- **Environment**：链式结构，每个环境有 `enclosing` 父指针。`ancestor(depth)` 沿链上跳 depth 层，`getAt(depth, name)` 直接在目标环境中取值。
- **函数调用**：`LoxFunction.call()` 创建新 `Environment`（父环境是函数定义时捕获的环境，实现闭包），在其中绑定参数，执行函数体。`return` 通过抛出 `Return` 异常来展开调用栈。
- **类与实例**：`LoxClass` 持有方法表，调用时创建 `LoxInstance`。实例通过 `Map<Token, Object>` 存储字段，属性读写通过 `Get`/`Set` 表达式实现。

### Parser vs Resolver vs Interpreter 的区别

三者都在"遍历 AST"，但角色完全不同：

| | Parser | Resolver | Interpreter |
|---|---|---|---|
| 是否实现 Visitor | 否 | 是（Expr + Stmt） | 是（Expr + Stmt） |
| 产物 | AST 树 | locals 表（depth 映射） | 程序执行结果 |
| 遍历方式 | 递归下降建造树 | 无条件遍历所有分支 | 按条件只走一条分支 |
| 操作对象 | Token 序列 | 作用域栈（元数据） | 环境链（运行时数据） |

## 完成进度

| 章节 | 内容 | 状态 |
|------|------|------|
| 第4章 | 词法分析（Scanner） | ✅ |
| 第6章 | 语法分析（Parser） | ✅ |
| 第7章 | 表达式求值 | ✅ |
| 第8章 | 语句和状态（变量、赋值、print） | ✅ |
| 第9-10章 | 作用域 | ✅ |
| 第11章 | 控制流（if/while/for/逻辑操作符） | ✅ |
| 第12章 | 函数 | ✅ |
| 第13章 | 解析与绑定（Resolver） | ✅ |
| 第14章 | 类 | ✅ |

## 目前支持的功能

- **表达式**: 算术（`+ - * /`）、比较（`> >= < <=`）、相等（`== !=`）、逻辑（`! and or`）、一元负号（`-`）、字符串拼接、函数调用、属性访问（`.`）、属性赋值（`. = `）
- **语句**: `var` 声明和初始化、赋值、`print`、表达式语句、`if`/`else`、`while`、`for`、函数声明（`fun`）、`return`、类声明（`class`）
- **函数**: 用户定义函数、参数传递、闭包（捕获定义时环境）、原生函数（`clock`）、方法
- **类**: 类声明、实例化（无参数构造）、属性读写（`get`/`set`）、方法定义、`this` 引用
- **作用域**: 块 `{}` 创建嵌套作用域，变量遮蔽，内层可访问外层变量
- **语义分析**: Resolver 在运行前进行名称解析，检测 `var a = a;` 等自引用错误、重复声明、顶层 `return`，计算变量作用域深度实现 O(1) 变量查找
- **错误处理**: 语法错误恐慌模式恢复 + 运行时错误定位 + 语义错误静态检测

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
├── Resolver.java     # 语义分析（名称解析）
├── Interpreter.java  # 树遍历解释器
├── Environment.java  # 作用域环境（链式）
├── LoxCallable.java  # 可调用对象接口
├── LoxFunction.java  # 用户定义函数（闭包支持）
├── LoxClass.java     # 类定义
├── LoxInstance.java  # 类实例
├── Return.java       # 返回值异常（用于 return 实现）
└── RuntimeError.java # 运行时异常

src/com/craftinginterpreters/tool/
└── GenerateAst.java  # AST 代码生成工具
```

## 参考资料

- [Crafting Interpreters](https://zaslee.github.io/craftinginterpreters/contents.html)
