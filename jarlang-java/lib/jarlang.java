package lib;

import java.util.*;

/**
 * Jarlang Programming Language Implementation in Java
 * Translated from jarlang.mojo
 * 
 * Contains: Lexer, Parser, AST Nodes, Interpreter, and Utilities
 */

//////////////////////////////
/// ERROR HANDLING FOR JARLANG ///
//////////////////////////////

// Exception classes for different error types
class IllegalCharError extends Exception {
    private String position;
    
    public IllegalCharError(String message, String position) {
        super(message);
        this.position = position;
    }
    
    public String getPosition() { return position; }
    
    @Override
    public String toString() {
        return "IllegalCharError: " + getMessage() + " " + position;
    }
}

class SyntaxError extends Exception {
    private String position;
    
    public SyntaxError(String message, String position) {
        super(message);
        this.position = position;
    }
    
    public String getPosition() { return position; }
    
    @Override
    public String toString() {
        return "SyntaxError: " + getMessage() + " " + position;
    }
}

class InterpreterError extends Exception {
    public InterpreterError(String message) {
        super(message);
    }
}

//////////////////////////////
/// TOKEN CLASS ///
//////////////////////////////

class Token {
    private String type;
    private String value;
    private Position posStart;
    private Position posEnd;
    
    public Token(String type, String value) {
        this.type = type;
        this.value = value;
        this.posStart = null;
        this.posEnd = null;
    }
    
    public Token(String type, String value, Position posStart, Position posEnd) {
        this.type = type;
        this.value = value;
        this.posStart = posStart;
        this.posEnd = posEnd;
    }
    
    // Getters
    public String getType() { return type; }
    public String getValue() { return value; }
    public Position getPosStart() { return posStart; }
    public Position getPosEnd() { return posEnd; }
    
    @Override
    public String toString() {
        if (!value.isEmpty()) {
            return type + ":" + value;
        }
        return type;
    }
    
    public Token copy() {
        return new Token(type, value, posStart, posEnd);
    }
}

//////////////////////////////
/// POSITION TRACKING ///
//////////////////////////////

class Position {
    private int idx;
    private int line;
    private int column;
    
    public Position(int idx, int line, int column) {
        this.idx = idx;
        this.line = line;
        this.column = column;
    }
    
    public void advance(String currentChar) {
        this.idx++;
        this.column++;
        
        if ("\n".equals(currentChar)) {
            this.line++;
            this.column = 0;
        }
    }
    
    public void advance() {
        advance(null);
    }
    
    public Position copy() {
        return new Position(idx, line, column);
    }
    
    // Getters
    public int getIdx() { return idx; }
    public int getLine() { return line; }
    public int getColumn() { return column; }
    
    @Override
    public String toString() {
        return "line " + line + ", column " + column + " (idx: " + idx + ")";
    }
}

//////////////////////////////
/// LEXER CLASS ///
//////////////////////////////

class JarlangLexer {
    private String filename;
    private String text;
    private Position pos;
    private String currentChar;
    
    public JarlangLexer(String filename, String text) {
        this.filename = filename;
        this.text = text;
        this.pos = new Position(-1, 0, -1);
        this.currentChar = "";
        advance();
    }
    
    private void advance() {
        pos.advance(currentChar);
        if (pos.getIdx() < text.length()) {
            currentChar = String.valueOf(text.charAt(pos.getIdx()));
        } else {
            currentChar = "";
        }
    }
    
    private boolean isDigit(String c) {
        return CONSTANTS.DIGITS.contains(c);
    }
    
    public List<Token> tokenize() throws IllegalCharError {
        List<Token> tokens = new ArrayList<>();
        
        while (!currentChar.isEmpty()) {
            // Skip whitespace
            if (" \t\n".contains(currentChar)) {
                advance();
            }
            // Handle numbers
            else if (isDigit(currentChar)) {
                tokens.add(makeNumber());
            }
            // Handle operators and parentheses
            else if ("+".equals(currentChar)) {
                tokens.add(new Token(CONSTANTS.TT_PLUS, currentChar));
                advance();
            }
            else if ("-".equals(currentChar)) {
                tokens.add(new Token(CONSTANTS.TT_MINUS, currentChar));
                advance();
            }
            else if ("*".equals(currentChar)) {
                tokens.add(new Token(CONSTANTS.TT_MUL, currentChar));
                advance();
            }
            else if ("/".equals(currentChar)) {
                tokens.add(new Token(CONSTANTS.TT_DIV, currentChar));
                advance();
            }
            else if ("(".equals(currentChar)) {
                tokens.add(new Token(CONSTANTS.TT_LPAREN, currentChar));
                advance();
            }
            else if (")".equals(currentChar)) {
                tokens.add(new Token(CONSTANTS.TT_RPAREN, currentChar));
                advance();
            }
            else if ("^".equals(currentChar)) {
                tokens.add(new Token(CONSTANTS.TT_POW, currentChar));
                advance();
            }
            // Handle comparison operators
            else if ("=".equals(currentChar)) {
                tokens.add(new Token(CONSTANTS.TT_EQ, currentChar));
                advance();
            }
            else if ("!".equals(currentChar)) {
                tokens.add(new Token(CONSTANTS.TT_NE, currentChar));
                advance();
            }
            else if ("<".equals(currentChar)) {
                tokens.add(new Token(CONSTANTS.TT_LT, currentChar));
                advance();
            }
            else if (">".equals(currentChar)) {
                tokens.add(new Token(CONSTANTS.TT_GT, currentChar));
                advance();
            }
            // Handle punctuation
            else if (",".equals(currentChar)) {
                tokens.add(new Token(CONSTANTS.TT_COMMA, currentChar));
                advance();
            }
            else if (":".equals(currentChar)) {
                tokens.add(new Token(CONSTANTS.TT_COLON, currentChar));
                advance();
            }
            else if (";".equals(currentChar)) {
                tokens.add(new Token(CONSTANTS.TT_SEMI, currentChar));
                advance();
            }
            else if ("{".equals(currentChar)) {
                tokens.add(new Token(CONSTANTS.TT_LBRACE, currentChar));
                advance();
            }
            else if ("}".equals(currentChar)) {
                tokens.add(new Token(CONSTANTS.TT_RBRACE, currentChar));
                advance();
            }
            // Handle comments
            else if ("#".equals(currentChar)) {
                advance(); // Skip '#'
                // Skip rest of line
                while (!currentChar.isEmpty() && !"\n".equals(currentChar)) {
                    advance();
                }
                if ("\n".equals(currentChar)) {
                    advance();
                }
            }
            // Handle string literals
            else if ("\"".equals(currentChar)) {
                advance(); // Skip opening quote
                StringBuilder strValue = new StringBuilder();
                while (!currentChar.isEmpty() && !"\"".equals(currentChar)) {
                    strValue.append(currentChar);
                    advance();
                }
                if ("\"".equals(currentChar)) {
                    advance(); // Skip closing quote
                    tokens.add(new Token(CONSTANTS.TT_STRING, strValue.toString()));
                } else {
                    throw new IllegalCharError("Unterminated string literal: " + strValue.toString(), 
                                             "at position " + pos.toString());
                }
            }
            // Handle illegal characters
            else if (!currentChar.isEmpty()) {
                String illegalChar = currentChar;
                advance();
                throw new IllegalCharError("Illegal character '" + illegalChar + "'", 
                                         "at position " + pos.toString());
            }
        }
        
        tokens.add(new Token(CONSTANTS.TT_EOF, ""));
        return tokens;
    }
    
    private Token makeNumber() {
        StringBuilder numStr = new StringBuilder();
        int dotCount = 0;
        
        while (!currentChar.isEmpty() && (isDigit(currentChar) || ".".equals(currentChar))) {
            if (".".equals(currentChar)) {
                if (dotCount == 1) break;
                dotCount++;
                numStr.append(".");
            } else {
                numStr.append(currentChar);
            }
            advance();
        }
        
        if (dotCount == 0) {
            return new Token(CONSTANTS.TT_INT, numStr.toString());
        } else {
            return new Token(CONSTANTS.TT_FLOAT, numStr.toString());
        }
    }
}

//////////////////////////////
/// AST NODE CLASSES ///
//////////////////////////////

// Abstract base class for all AST nodes
abstract class ASTNode {
    public abstract double evaluate() throws InterpreterError;
    public abstract String getNodeType();
}

// Number node for literals
class NumberNode extends ASTNode {
    private String value;
    
    public NumberNode(Token token) {
        this.value = token.getValue();
    }
    
    public NumberNode(String value) {
        this.value = value;
    }
    
    public String getValue() { return value; }
    
    @Override
    public double evaluate() throws InterpreterError {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new InterpreterError("Invalid number format: " + value);
        }
    }
    
    @Override
    public String getNodeType() { return "number"; }
    
    @Override
    public String toString() {
        return value;
    }
}

// Binary operation node
class BinOpNode extends ASTNode {
    private NumberNode left;
    private Token opToken;
    private NumberNode right;
    
    public BinOpNode(NumberNode left, Token opToken, NumberNode right) {
        this.left = left;
        this.opToken = opToken;
        this.right = right;
    }
    
    public NumberNode getLeft() { return left; }
    public Token getOpToken() { return opToken; }
    public NumberNode getRight() { return right; }
    
    @Override
    public double evaluate() throws InterpreterError {
        double leftVal = left.evaluate();
        double rightVal = right.evaluate();
        
        String opType = opToken.getType();
        
        if (CONSTANTS.TT_PLUS.equals(opType)) {
            return leftVal + rightVal;
        } else if (CONSTANTS.TT_MINUS.equals(opType)) {
            return leftVal - rightVal;
        } else if (CONSTANTS.TT_MUL.equals(opType)) {
            return leftVal * rightVal;
        } else if (CONSTANTS.TT_DIV.equals(opType)) {
            if (rightVal != 0) {
                return leftVal / rightVal;
            } else {
                throw new InterpreterError("Division by zero");
            }
        } else {
            throw new InterpreterError("Unknown operator: " + opType);
        }
    }
    
    @Override
    public String getNodeType() { return "binop"; }
    
    @Override
    public String toString() {
        return " {" + left.toString() + "}(" + opToken.toString() + "){" + right.toString() + "}";
    }
}

//////////////////////////////
/// PARSER CLASS ///
//////////////////////////////

class JarlangParser {
    private List<Token> tokens;
    private int tokIdx;
    private Token currentToken;
    
    public JarlangParser(List<Token> tokens) {
        this.tokens = new ArrayList<>(tokens);
        this.tokIdx = -1;
        this.currentToken = null;
        advance();
    }
    
    private Token advance() {
        tokIdx++;
        if (tokIdx < tokens.size()) {
            currentToken = tokens.get(tokIdx);
        } else {
            currentToken = null;
        }
        return currentToken;
    }
    
    public ASTNode parse() throws SyntaxError {
        if (currentToken == null) {
            throw new SyntaxError("No tokens to parse", "at token position " + tokIdx);
        }
        return expr();
    }
    
    private ASTNode expr() throws SyntaxError {
        ASTNode leftNode = term();
        
        while (currentToken != null && 
               (CONSTANTS.TT_PLUS.equals(currentToken.getType()) || 
                CONSTANTS.TT_MINUS.equals(currentToken.getType()))) {
            
            Token opToken = currentToken;
            advance();
            
            ASTNode rightNode = term();
            
            // For now, only handle simple number operations
            if (leftNode instanceof NumberNode && rightNode instanceof NumberNode) {
                leftNode = new BinOpNode((NumberNode) leftNode, opToken, (NumberNode) rightNode);
            } else {
                throw new SyntaxError("Complex expressions not yet supported", 
                                    "at token position " + tokIdx);
            }
        }
        
        return leftNode;
    }
    
    private ASTNode term() throws SyntaxError {
        ASTNode leftNode = factor();
        
        while (currentToken != null && 
               (CONSTANTS.TT_MUL.equals(currentToken.getType()) || 
                CONSTANTS.TT_DIV.equals(currentToken.getType()))) {
            
            Token opToken = currentToken;
            advance();
            
            ASTNode rightNode = factor();
            
            // For now, only handle simple number operations
            if (leftNode instanceof NumberNode && rightNode instanceof NumberNode) {
                leftNode = new BinOpNode((NumberNode) leftNode, opToken, (NumberNode) rightNode);
            } else {
                throw new SyntaxError("Complex expressions not yet supported", 
                                    "at token position " + tokIdx);
            }
        }
        
        return leftNode;
    }
    
    private ASTNode factor() throws SyntaxError {
        Token tok = currentToken;
        
        if (tok != null && (CONSTANTS.TT_INT.equals(tok.getType()) || 
                           CONSTANTS.TT_FLOAT.equals(tok.getType()))) {
            advance();
            return new NumberNode(tok);
        } else if (tok != null && CONSTANTS.TT_LPAREN.equals(tok.getType())) {
            advance();
            ASTNode node = expr();
            
            if (currentToken != null && CONSTANTS.TT_RPAREN.equals(currentToken.getType())) {
                advance();
                return node;
            } else {
                throw new SyntaxError("Expected ')'", "at token position " + tokIdx);
            }
        } else {
            throw new SyntaxError("Expected int, float or '('", "at token position " + tokIdx);
        }
    }
}

//////////////////////////////
/// INTERPRETER CLASS ///
//////////////////////////////

class JarlangInterpreter {
    
    public double visit(ASTNode node) throws InterpreterError {
        return node.evaluate();
    }
    
    public double interpret(ASTNode ast) throws InterpreterError {
        return visit(ast);
    }
}

//////////////////////////////
/// UTILITY RUNNER METHODS ///
//////////////////////////////

class JarlangRunners {
    
    public static List<Token> runLexer(String filename, String text) throws IllegalCharError {
        JarlangLexer lexer = new JarlangLexer(filename, text);
        return lexer.tokenize();
    }
    
    public static class ParseResult {
        public ASTNode ast;
        public IllegalCharError lexError;
        public SyntaxError parseError;
        
        public ParseResult(ASTNode ast, IllegalCharError lexError, SyntaxError parseError) {
            this.ast = ast;
            this.lexError = lexError;
            this.parseError = parseError;
        }
    }
    
    public static ParseResult runParser(String filename, String text) {
        try {
            List<Token> tokens = runLexer(filename, text);
            JarlangParser parser = new JarlangParser(tokens);
            ASTNode ast = parser.parse();
            return new ParseResult(ast, null, null);
        } catch (IllegalCharError e) {
            return new ParseResult(null, e, null);
        } catch (SyntaxError e) {
            return new ParseResult(null, null, e);
        }
    }
    
    public static double runInterpreter(ASTNode ast) throws InterpreterError {
        JarlangInterpreter interpreter = new JarlangInterpreter();
        return interpreter.interpret(ast);
    }
}

//////////////////////////////
/// TEST UTILITIES ///
//////////////////////////////

class JarlangTestUtils {
    
    public static String testLexer(String text) {
        JarlangLexer lexer = new JarlangLexer("<stdin>", text);
        
        StringBuilder result = new StringBuilder();
        result.append("Lexer initialized with text: '").append(text).append("'\n");
        result.append("Testing lexer functionality...\n");
        
        try {
            List<Token> tokens = lexer.tokenize();
            result.append("Successfully tokenized ").append(tokens.size()).append(" tokens:\n");
            for (Token token : tokens) {
                result.append("  ").append(token.toString()).append("\n");
            }
        } catch (IllegalCharError e) {
            result.append("Lexer error: ").append(e.toString()).append("\n");
        }
        
        return result.toString();
    }
}
