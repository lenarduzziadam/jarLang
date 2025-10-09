package lib.parser;
import java.util.ArrayList;
import java.util.List;
import lib.tokens.*;
import lib.errors.*;
import lib.CONSTANTS;

//////////////////////////////
/// RECURSIVE DESCENT PARSER ///
//////////////////////////////

/**
 * JARLANG PARSER - Converts tokens into Abstract Syntax Tree (AST)
 * 
 * The parser is the second phase of language processing. It takes the flat
 * sequence of tokens from the lexer and builds a hierarchical tree structure
 * that represents the program's syntax and semantics.
 * 
 * PARSING ALGORITHM: Recursive Descent
 * This parser uses recursive descent, where each grammar rule becomes a method.
 * The methods call each other recursively to build the syntax tree.
 * 
 * JARLANG GRAMMAR (with exponentiation support):
 * expression → term ((commune | banish) term)*
 * term       → power ((rally | slash) power)*  
 * power      → factor (ascend factor)*     [NEW: exponentiation level]
 * factor     → number | gather expression disperse
 * number     → INT | FLOAT
 * 
 * OPERATOR PRECEDENCE (handled by grammar structure):
 * 1. Parentheses: gather (...) disperse  [highest precedence]
 * 2. Exponentiation: ascend (^)
 * 3. Multiplication/Division: rally (*), slash (/)
 * 4. Addition/Subtraction: commune (+), banish (-)  [lowest precedence]
 * 
 * RECURSIVE DESCENT ADVANTAGES:
 * - Easy to understand and implement
 * - Direct correspondence between grammar and code
 * - Natural handling of operator precedence
 * - Good error reporting capabilities
 * - Easily extensible for new language features
 * 
 * CURRENT FEATURES:
 * - Full binary operations with proper precedence
 * - Exponentiation support (^ / "ascend")
 * - Nested expressions and parentheses
 * - Complex expression trees
 * 
 * FUTURE ENHANCEMENTS:
 * - Variables, functions, and control flow
 * - Unary operators (-, +)
 * - Multiple expression statements
 */
class JarlangParser {
    private List<Token> tokens;    // Complete list of tokens from lexer
    private int tokIdx;            // Current position in token list
    private Token currentToken;    // Token currently being processed
    
    /**
     * Constructor initializes parser with token list
     * @param tokens List of tokens produced by the lexer
     */
    public JarlangParser(List<Token> tokens) {
        this.tokens = new ArrayList<>(tokens);  // Defensive copy
        this.tokIdx = -1;                       // Start before first token
        this.currentToken = null;
        advance();                              // Move to first token
    }
    
    /**
     * Advance to the next token in the list
     * This is the core method for moving through the token stream
     * @return The new current token (or null if at end)
     */
    private Token advance() {
        tokIdx++;
        if (tokIdx < tokens.size()) {
            currentToken = tokens.get(tokIdx);
        } else {
            currentToken = null;  // End of tokens
        }
        return currentToken;
    }
    
    /**
     * MAIN PARSING ENTRY POINT
     * 
     * This method starts the parsing process by calling the top-level
     * grammar rule (expression). It performs basic validation and
     * error handling before delegating to the recursive descent methods.
     * 
     * @return Root node of the constructed AST
     * @throws SyntaxError if the token sequence is invalid
     */
    public ASTNode parse() throws SyntaxError {
        if (currentToken == null) {
            throw new SyntaxError("No tokens to parse", "at token position " + tokIdx);
        }
        return expr();  // Start with top-level expression rule
    }
    
    /**
     * EXPRESSION PARSING - Top level of operator precedence
     * Grammar rule: expression → term ((commune | banish) term)*
     * 
     * This method handles addition and subtraction, which have the lowest
     * precedence in arithmetic expressions. It ensures left-associativity
     * by building the tree from left to right.
     * 
     * Examples:
     * - "3 + 5" → BinOpNode(3, +, 5)
     * - "3 + 5 - 2" → BinOpNode(BinOpNode(3, +, 5), -, 2)
     * - "3 * 5 + 2" → BinOpNode(BinOpNode(3, *, 5), +, 2)
     * - "2 + 3 * 4" → BinOpNode(2, +, BinOpNode(3, *, 4))
     * 
     * The left-associative parsing ensures that "a + b + c" is parsed as
     * "(a + b) + c" rather than "a + (b + c)".
     * 
     * @return AST node representing the parsed expression
     * @throws SyntaxError if expression is malformed
     */
    private ASTNode expr() throws SyntaxError {
        ASTNode leftNode = term();  // Start with higher-precedence term
        
        // Handle zero or more addition/subtraction operations
        while (currentToken != null && 
               (CONSTANTS.TT_PLUS.equals(currentToken.getType()) ||   // "commune"
                CONSTANTS.TT_MINUS.equals(currentToken.getType()))) { // "banish"
            
            Token opToken = currentToken;  // Save the operator
            advance();                     // Move past operator
            
            ASTNode rightNode = term();    // Parse right operand
            
            // Create binary operation node (now supports any ASTNode types)
            leftNode = new BinOpNode(leftNode, opToken, rightNode);
        }
        
        return leftNode;
    }
    
    /**
     * TERM PARSING - Middle level of operator precedence  
     * Grammar rule: term → power ((rally | slash) power)*
     * 
     * This method handles multiplication and division, which have higher
     * precedence than addition and subtraction but lower than exponentiation.
     * Like expr(), it maintains left-associativity.
     * 
     * Examples:
     * - "3 * 5" → BinOpNode(3, *, 5)
     * - "12 / 3 / 2" → BinOpNode(BinOpNode(12, /, 3), /, 2) = 2
     * - "3 + 4 * 5" → handled by expr() calling term() for "4 * 5"
     * - "2 ^ 3 * 4" → BinOpNode(BinOpNode(2, ^, 3), *, 4) = 32
     * 
     * @return AST node representing the parsed term
     * @throws SyntaxError if term is malformed
     */
    private ASTNode term() throws SyntaxError {
        ASTNode leftNode = power();  // Start with higher-precedence power
        
        // Handle zero or more multiplication/division operations
        while (currentToken != null && 
               (CONSTANTS.TT_MUL.equals(currentToken.getType()) ||   // "rally"
                CONSTANTS.TT_DIV.equals(currentToken.getType()))) { // "slash"
            
            Token opToken = currentToken;  // Save the operator
            advance();                     // Move past operator
            
            ASTNode rightNode = power();   // Parse right operand (now calls power())
            
            // Create binary operation node (now supports any ASTNode types)
            leftNode = new BinOpNode(leftNode, opToken, rightNode);
        }
        
        return leftNode;
    }
    
    /**
     * POWER PARSING - High precedence level for exponentiation
     * Grammar rule: power → factor (ascend factor)*
     * 
     * This method handles exponentiation, which has higher precedence
     * than multiplication and division. Unlike other operators,
     * exponentiation is RIGHT-associative, meaning "2^3^4" should be
     * parsed as "2^(3^4)" not "(2^3)^4".
     * 
     * Examples:
     * - "2 ^ 3" → BinOpNode(2, ^, 3) = 8
     * - "10 ^ 10" → BinOpNode(10, ^, 10) = 10000000000
     * - "2 ^ 3 ^ 2" → BinOpNode(2, ^, BinOpNode(3, ^, 2)) = 2^9 = 512
     * - "(2 + 3) ^ 4" → BinOpNode(BinOpNode(2, +, 3), ^, 4) = 625
     * 
     * NOTE: Right-associativity means we parse differently than other operators.
     * Instead of building left-to-right, we parse the right side recursively.
     * 
     * @return AST node representing the parsed power expression
     * @throws SyntaxError if power expression is malformed
     */
    private ASTNode power() throws SyntaxError {
        ASTNode leftNode = factor();  // Start with highest-precedence factor
        
        // Handle exponentiation (right-associative)
        if (currentToken != null && CONSTANTS.TT_POW.equals(currentToken.getType())) { // "ascend"
            Token opToken = currentToken;  // Save the operator
            advance();                     // Move past operator
            
            // RIGHT-ASSOCIATIVE: recursively call power() for right side
            // This ensures "2^3^4" becomes "2^(3^4)" not "(2^3)^4"
            ASTNode rightNode = power();   // Recursive call for right-associativity
            
            // Create binary operation node
            leftNode = new BinOpNode(leftNode, opToken, rightNode);
        }
        
        return leftNode;
    }
    
    /**
     * FACTOR PARSING - Highest precedence level
     * Grammar rule: factor → number | gather expression disperse
     * 
     * This method handles the atomic elements of expressions:
     * - Numeric literals (both integers and floats)
     * - Parenthesized expressions (which can contain any expression)
     * 
     * Parentheses have the highest precedence because they force
     * evaluation order regardless of operator precedence.
     * 
     * Examples:
     * - "42" → NumberNode("42")
     * - "3.14" → NumberNode("3.14")  
     * - "(3 + 5)" → result of parsing "3 + 5" as expression
     * - "((2 * 3) + 4)" → nested parenthesized expressions
     * 
     * @return AST node representing the parsed factor
     * @throws SyntaxError if factor is invalid
     */
    private ASTNode factor() throws SyntaxError {
        Token tok = currentToken;

        // CASE 0: Unary operators (ADD THIS HERE - before CASE 1)
        if (tok != null && (CONSTANTS.TT_MINUS.equals(tok.getType()) || 
                        CONSTANTS.TT_PLUS.equals(tok.getType()))) {
            // Handle unary minus and plus here
            advance();  // Consume the unary operator
            ASTNode factorNode = factor();  // Parse the factor
            return new UnaryOpNode(tok, factorNode);
        }
        
        // CASE 1: Numeric literal (integer or float)
        if (tok != null && (CONSTANTS.TT_INT.equals(tok.getType()) || 
                           CONSTANTS.TT_FLOAT.equals(tok.getType()))) {  // "int" or "float"
            advance();  // Consume the number token
            return new NumberNode(tok);
        } 

        // CASE 1.5: Pi constant
        else if (tok != null && CONSTANTS.TT_PI.equals(tok.getType())) {  // "pi"
            advance();  // Consume the pi token
            return new NumberNode(tok);
        }

        // CASE 2: Parenthesized expression
        else if (tok != null && CONSTANTS.TT_LPAREN.equals(tok.getType())) {  // "gather"
            advance();  // Consume opening parenthesis
            
            ASTNode node = expr();  // Recursively parse the inner expression
            
            // Expect closing parenthesis
            if (currentToken != null && CONSTANTS.TT_RPAREN.equals(currentToken.getType())) {  // "disperse"
                advance();  // Consume closing parenthesis
                return node;
            } else {
                throw new SyntaxError("Expected ')'", "at token position " + tokIdx);
            }
        } 
        // CASE 3: Invalid factor
        else {
            throw new SyntaxError("Expected int, float or '('", "at token position " + tokIdx);
        }
    }
}
