package com.craftinginterpreters.lox;

import static com.craftinginterpreters.lox.TokenType.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

class Parser {

  private static class ParseError extends RuntimeException {
  }
  private final List<Token> tokens;
  private int current = 0;

  Parser(List<Token> tokens) {
    this.tokens = tokens;
  }

  private Expr expression() {
    return assignment();
  }

  private Stmt declaration() {
    try {
      if (match(VAR)) {
        return varDeclaration();
      }
      return statement();
    } catch (ParseError error) {
      synchronize();
      return null;
    }
  }

  private Stmt statement() {
    if (match(IF)) {
      return ifStatement();
    }
    if(match(FOR))
    {
      return forStatement();
    }
    if (match(PRINT)) {
      return printStatement();
    }
    if(match(WHILE))
    {
      return whileStatement();
    }
    if (match(LEFT_BRACE)) {
      return new Stmt.Block(block());
    }
    return expressionStatement();
  }

  private Stmt whileStatement(){
    consume(LEFT_PAREN,"Expect '(' after 'while'.");

    Expr condition = expression();

    consume (RIGHT_PAREN,"Expect ')' after condition.");

    Stmt body = statement();
    return new Stmt.While(condition, body);
  }
  private Stmt forStatement()
  {
    consume(LEFT_PAREN,"Expect '(' after 'for'.");
    Stmt initializer;

    if(match(SEMICOLON))
    {
      initializer = null;
    }
    else if(match(VAR))
    {
      initializer = varDeclaration();
    }
    else {
      initializer = expressionStatement();
    }

    Expr condition = null;

    if(!check(SEMICOLON))
    {
      condition = expression();
    }
    consume(SEMICOLON,"Expect ';' after loop condition");

    Expr increment = null;

    if(!check(RIGHT_PAREN))
    {
      increment = expression();
    }
    consume(RIGHT_PAREN,"Expect ')' after for clause.");

    Stmt body = statement();

    //先对递增语句进行处理，非空的话就将递归语句加在循环体的下面
    //这样来构造出一个有递增条件的循环体。
    if(increment != null)
    {
      body = new Stmt.Block(
          Arrays.asList(
            body,
            new Stmt.Expression(increment)
            )
          )
    }
    //若条件表达式是null，表示这是一个死循环，直接设置为true
    if(condition ==null)condition = new Expr.Literal(true);

    //将条件表达式和循环体构造成一个while
    body = new Stmt.While(condition, body);
    //如果有初始化语句的话，就在while语句前执行一次
    if(initializer !=null)
    {
      body = new Stmt.Block(Arrays.asList(initializer,body));

    }
    return body;


  }
  private Stmt ifStatement() {
    //找后面的左括号
    consume(LEFT_PAREN, "Expect '(' after 'if'.");

    Expr condition = expression();

    consume(RIGHT_PAREN, "Expect ')' after 'if'.");

    Stmt thenBranch = statement();

    Stmt elseBranch = null;
    if (match(ELSE)) {
      elseBranch = statement();
    }
    return new Stmt.If(condition, thenBranch, elseBranch);

  }

  private Expr assignment() {

    //这一步除了if里的东西之外,就是之前的东西
    Expr expr = equality();
    if (match(EQUAL)) {
      Token equals = previous();
      //递归调用来保证右结合
      Expr value = assignment();
      //判断是不是合法的可赋值左值

      Token name = ((Expr.Variable) expr).name;
      return new Expr.Assign(name, value);
      //返回新的表达式节点,表示重新赋值
    }
    error(equals, "Invalid assignment target.");

  }
  return expr;
}

private Stmt printStatement() {
  Expr value = expression();
  consume(SEMICOLON, "Expect ';' after value.");
  return new Stmt.Print(value);
}

private Stmt varDeclaration() {
  //标识符(变量)的名字
  Token name = consume(IDENTIFIER, "Expect variable name.");

  Expr initializer = null;
  //检测到=后,把后面表达式的值赋给表达式变量
  if (match(EQUAL)) {
    initializer = expression();
  }

  consume(SEMICOLON, "Expect ';' after variable declaration.");
  //将该表达式和标识符进行绑定
  return new Stmt.Var(name, initializer);
}

private Stmt expressionStatement() {
  Expr expr = expression();
  consume(SEMICOLON, "Expect ';' after expression.");
  return new Stmt.Expression(expr);
}

private List<Stmt> block() {
  List<Stmt> statements = new ArrayList<>();
  while (!check(RIGHT_BRACE) && !isAtEnd()) {
    statements.add(declaration());

  }
  consume(RIGHT_BRACE, "Expect '}' after block.");
  return statements;
}

private Expr equality() {
  Expr expr = comparison();

  while (match(BANG_EQUAL, EQUAL_EQUAL)) {
    Token operator = previous();
    Expr right = comparison();

    expr = new Expr.Binary(expr, operator, right);
  }
  return expr;
}

private Expr comparison() {
  Expr expr = term();

  while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
    Token operator = previous();
    Expr right = term();
    expr = new Expr.Binary(expr, operator, right);

  }
  return expr;
}

private Expr term() {
  Expr expr = factor();

  while (match(MINUS, PLUS)) {
    Token operator = previous();
    Expr right = factor();
    expr = new Expr.Binary(expr, operator, right);

  }
  return expr;
}

private Expr factor() {
  Expr expr = unary();
  while (match(SLASH, STAR)) {
    Token operator = previous();
    Expr right = unary();
    expr = new Expr.Binary(expr, operator, right);
  }

  return expr;
}

private Expr unary() {
  if (match(BANG, MINUS)) {
    Token operator = previous();
    Expr right = unary();
    return new Expr.Unary(operator, right);
  }
  return primary();
}

private Expr primary() {
  if (match(FALSE)) {
    return new Expr.Literal(false);
  }
  if (match(TRUE)) {
    return new Expr.Literal(true);
  }
  if (match(NIL)) {
    return new Expr.Literal(null);
  }

  if (match(NUMBER, STRING)) {
    return new Expr.Literal(previous().literal);
  }
  if (match(IDENTIFIER)) {
    return new Expr.Variable(previous());
  }
  if (match(LEFT_PAREN)) {
    Expr expr = expression();
    consume(RIGHT_PAREN, "Expect ')' after expression.");
    return new Expr.Grouping(expr);
  }
  throw error(peek(), "Expect expression");
}

private boolean match(TokenType... types) {
  for (TokenType type : types) {
    if (check(type)) {
      advance();
      return true;
    }
  }

  return false;
}

private Token consume(TokenType type, String message) {
  if (check(type)) {
    return advance();
  }
  throw error(peek(), message);
}

private boolean check(TokenType type) {
  if (isAtEnd()) {
    return false;
  }
  return peek().type == type;
}

private boolean isAtEnd() {
  return peek().type == EOF;
}

private Token advance() {
  if (!isAtEnd()) {
    current++;
  }
  return previous();
}

private Token peek() {
  return tokens.get(current);
}

private Token previous() {
  return tokens.get(current - 1);
}

private ParseError error(Token token, String message) {
  Lox.error(token, message);
  return new ParseError();
}

private void synchronize() {
  //先消费掉一个token
  advance();

  //不断进行循环来消费接下来的token,直到同步点
  while (!isAtEnd()) {
    if (previous().type == SEMICOLON) {
      return;
    }

    switch (peek().type) {
      case CLASS, FUN, VAR, IF, WHILE, PRINT, RETURN -> {
        return;
      }

    }

    advance();
  }
}
//这里正式调用解析器,在一个循环中不断地将解析出的语句填入到一个语句列表中,在解析的过程中会
//有这些情况:1.判断出了var赋值语句,将标识符进行消费,然后匹配一个等号,然后继续将后面的表达式的值求出来,
//消费掉最后的分号;,用标识符和后面的表达式节点构造一个赋值语句节点
//2.判断到了print,大体上和1的情况类似
//3.不是上面两种情况,说明是一个表达式,直接解析出一个表达式

List<Stmt> parse() {
  List<Stmt> statements = new ArrayList<>();
  while (!isAtEnd()) {
    statements.add(declaration());

  }
  return statements;
}
}
