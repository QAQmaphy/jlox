package com.craftinginterpreters.lox;

import static com.craftinginterpreters.lox.TokenType.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Scanner {

    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("fun", FUN);
        keywords.put("if", IF);
        keywords.put("nil", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("var", VAR);
        keywords.put("while", WHILE);

    }

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        return source.charAt(current++);

    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(' ->
                addToken(LEFT_PAREN);
            case ')' ->
                addToken(RIGHT_PAREN);
            case '{' ->
                addToken(LEFT_BRACE);
            case '}' ->
                addToken(RIGHT_BRACE);
            case ',' ->
                addToken(COMMA);
            case '.' ->
                addToken(DOT);
            case '-' ->
                addToken(MINUS);
            case '+' ->
                addToken(PLUS);
            case ';' ->
                addToken(SEMICOLON);
            case '*' ->
                addToken(STAR);
            case '!' ->
                addToken(match('=') ? BANG_EQUAL : BANG);
            case '=' ->
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
            case '<' ->
                addToken(match('=') ? LESS_EQUAL : LESS);
            case '>' ->
                addToken(match('=') ? GREATER_EQUAL : GREATER);
            case '/' -> {
                //如果发现下一个字符还是/,就说明这是一行注释,不断循环一直到有换行符或是到达文件末尾
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) {
                        advance();
                    }

                } else {
                    addToken(SLASH);
                }
            }
            case ' ', '\r', '\t' -> {
            }
            case '\n' ->
                line++;
            case '"' ->
                string();
            default -> {
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();

                } else {
                    Lox.error(line, "Unexpected charcter.");
                }
            }
        }

    }

    private void identifier() {
        while (isAlphaNumeric(peek())) {
            advance();
        }

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) {
            type = IDENTIFIER;
        }
        addToken(type);

    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z')
                || (c >= 'A' && c <= 'Z')
                || c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    @SuppressWarnings("UnnecessaryTemporaryOnConversionFromString")
    private void number() {
        while (isDigit(peek())) {
            advance();
        }
        if (peek() == '.' && isDigit(peekNext())) {
            advance();
            while (isDigit(peek())) {
                advance();
            }
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private void string() {

        while (peek() != '"' && !isAtEnd()) {
            //先看有没有换行
            if (peek() == '\n') {
                line++;
            }

            advance();
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;

        }
        advance();
        //去掉引号作为转义后的值
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    //匹配下一个字符
    private boolean match(char expected) {
        if (isAtEnd()) {
            return false;
        }
        if (source.charAt(current) != expected) {
            return false;
        }

        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) {
            return '\0';
        }
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) {
            return '\0';
        }
        return source.charAt(current + 1);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
}
