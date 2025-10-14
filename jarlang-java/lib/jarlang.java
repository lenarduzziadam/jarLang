package lib;

import java.util.*;

/**
 * ===================================================================
 * JARLANG PROGRAMMING LANGUAGE - COMPLETE IMPLEMENTATION IN JAVA
 * ===================================================================
 * 
 * This file contains a complete implementation of the Jarlang programming language,
 * a warrior-themed mathematical expression language. The implementation follows
 * the classic interpreter pattern with distinct phases:
 * 
 * 1. LEXICAL ANALYSIS (Tokenization) - Breaks input text into meaningful tokens
 * 2. SYNTAX ANALYSIS (Parsing) - Builds an Abstract Syntax Tree (AST)
 * 3. SEMANTIC ANALYSIS (Interpretation) - Evaluates the AST to produce results
 * 
 * WARRIOR THEME:
 * Instead of standard mathematical operators, Jarlang uses warrior-themed tokens:
 * - commune (+) for addition
 * - banish (-) for subtraction  
 * - rally (*) for multiplication
 * - slash (/) for division
 * - gather (() for left parenthesis
 * - disperse ()) for right parenthesis
 * - ascend (^) for exponentiation
 * 
 * ARCHITECTURE:
 * - Error Classes: Custom exceptions with position tracking
 * - Token Class: Represents individual language elements
 * - Position Class: Tracks line/column for error reporting
 * - Lexer Class: Converts text into tokens
 * - AST Node Classes: Represent parsed program structure
 * - Parser Class: Builds AST from tokens using recursive descent
 * - Interpreter Class: Evaluates AST nodes to compute results
 * - Utility Classes: Helper functions for running the pipeline
 * 
 * Originally translated from Mojo implementation but enhanced with
 * Java's superior object-oriented features and exception handling.
 */

//////////////////////////////
/// ERROR HANDLING SYSTEM ///
//////////////////////////////

/**
 * CUSTOM EXCEPTION HIERARCHY
 * 
 * These custom exception classes provide detailed error information
 * including position tracking for better debugging and user feedback.
 * Each exception type represents a different phase of language processing.
 */

/**
 * IllegalCharError - Thrown during lexical analysis (tokenization)
 * 
 * This exception occurs when the lexer encounters a character that
 * is not part of the Jarlang language specification. For example:
 * - Unsupported symbols like '@', '$', etc.
 * - Invalid characters in strings or numbers
 * - Malformed tokens
 * 
 * The exception includes both the error message and position information
 * to help users locate and fix the problem in their source code.
 */
class IllegalCharError extends Exception {
    private String position;  // Stores line/column information
    
    /**
     * Constructor creates an IllegalCharError with message and position
     * @param message Descriptive error message (e.g., "Illegal character 'x'")
     * @param position Location in source code where error occurred
     */
    public IllegalCharError(String message, String position) {
        super(message);  // Pass message to parent Exception class
        this.position = position;
    }
    
    /**
     * Get the position where this error occurred
     * @return String describing line/column location
     */
    public String getPosition() { return position; }
    
    /**
     * Override toString to provide complete error information
     * This combines the error type, message, and position into a single string
     * for display to the user.
     */
    @Override
    public String toString() {
        return "IllegalCharError: " + getMessage() + " " + position;
    }
}

/**
 * SyntaxError - Thrown during parsing (syntax analysis)
 * 
 * This exception occurs when the parser encounters tokens that don't
 * follow the grammar rules of the Jarlang language. Examples include:
 * - Missing closing parentheses: "3 + (2 * 4"
 * - Invalid expression structure: "+ 3 *"
 * - Unexpected tokens: "3 + ) 4"
 * 
 * The parser uses recursive descent, so syntax errors are detected
 * when the current token doesn't match what the grammar expects.
 */
class SyntaxError extends Exception {
    private String position;  // Stores token position information
    
    /**
     * Constructor creates a SyntaxError with message and token position
     * @param message Descriptive error message (e.g., "Expected ')'")
     * @param position Token position where parsing failed
     */
    public SyntaxError(String message, String position) {
        super(message);
        this.position = position;
    }
    
    /**
     * Get the position where this syntax error occurred
     * @return String describing token position
     */
    public String getPosition() { return position; }
    
    /**
     * Override toString to provide complete syntax error information
     */
    @Override
    public String toString() {
        return "SyntaxError: " + getMessage() + " " + position;
    }
}

/**
 * InterpreterError - Thrown during evaluation (interpretation)
 * 
 * This exception occurs when the interpreter encounters runtime errors
 * while evaluating the Abstract Syntax Tree. Examples include:
 * - Division by zero: "5 / 0"
 * - Invalid number formats: malformed numeric literals
 * - Unknown operators: if new operators are added but not implemented
 * 
 * Unlike lexical and syntax errors which are caught at compile-time,
 * interpreter errors occur during execution of the parsed program.
 */
class InterpreterError extends Exception {
    /**
     * Constructor creates an InterpreterError with descriptive message
     * @param message What went wrong during evaluation
     */
    public InterpreterError(String message) {
        super(message);
    }
}

//////////////////////////////
/// TOKEN REPRESENTATION SYSTEM ///
//////////////////////////////

/**
 * TOKEN CLASS - The fundamental unit of lexical analysis
 * 
 * A token represents a meaningful unit in the source code, such as:
 * - Numbers: integers (42) and floats (3.14)
 * - Operators: commune (+), banish (-), rally (*), slash (/)
 * - Delimiters: gather ((), disperse ()), etc.
 * - Special tokens: EOF (end of file)
 * 
 * Each token stores:
 * - type: What kind of token it is (from CONSTANTS)
 * - value: The actual text/value of the token
 * - position info: Where it appeared in the source (optional)
 * 
 * Tokens are the output of the lexer and input to the parser.
 */
class Token {
    private String type;           // Token type from CONSTANTS (e.g., "commune", "int")
    private String value;          // Actual value (e.g., "42", "+", "3.14")
    private Position posStart;     // Where token starts in source code
    private Position posEnd;       // Where token ends in source code
    
    /**
     * Simple constructor for tokens without position tracking
     * Used for basic tokens where position isn't critical
     */
    public Token(String type, String value) {
        this.type = type;
        this.value = value;
        this.posStart = null;
        this.posEnd = null;
    }
    
    /**
     * Full constructor with position tracking for better error reporting
     * Used when precise error location is important
     */
    public Token(String type, String value, Position posStart, Position posEnd) {
        this.type = type;
        this.value = value;
        this.posStart = posStart;
        this.posEnd = posEnd;
    }
    
    // Getter methods for accessing token properties
    public String getType() { return type; }
    public String getValue() { return value; }
    public Position getPosStart() { return posStart; }
    public Position getPosEnd() { return posEnd; }
    
    /**
     * String representation for debugging and display
     * Shows type and value in a readable format
     */
    @Override
    public String toString() {
        if (!value.isEmpty()) {
            return type + ": '" + value + "'";  // e.g., "int:42", "commune:+"
        }
        return type;  // e.g., "end" for EOF token
    }
    
    /**
     * Create a copy of this token (defensive copying)
     * Useful when tokens need to be stored or passed around safely
     */
    public Token copy() {
        return new Token(type, value, posStart, posEnd);
    }
}

//////////////////////////////
/// POSITION TRACKING SYSTEM ///
//////////////////////////////

/**
 * POSITION CLASS - Tracks location in source code for error reporting
 * 
 * When users write Jarlang code and make mistakes, they need to know
 * exactly WHERE the error occurred. This class tracks:
 * - idx: Absolute character position in the entire input
 * - line: Line number (starting from 0)
 * - column: Column number within the line (starting from 0)
 * 
 * This information is crucial for:
 * - Providing helpful error messages
 * - IDE integration (syntax highlighting, error markers)
 * - Debugging complex expressions
 * 
 * The position advances as the lexer processes each character,
 * handling special cases like newlines that reset column position.
 */
class Position {
    private int idx;        // Absolute position in entire input (0-based)
    private int line;       // Line number (0-based) 
    private int column;     // Column within line (0-based)
    
    /**
     * Constructor initializes position tracking
     * @param idx Starting character index
     * @param line Starting line number
     * @param column Starting column number
     */
    public Position(int idx, int line, int column) {
        this.idx = idx;
        this.line = line;
        this.column = column;
    }
    
    /**
     * Advance position by one character, handling newlines specially
     * This method is called by the lexer for every character processed
     * @param currentChar The character we're advancing past (null if unknown)
     */
    public void advance(String currentChar) {
        this.idx++;      // Always increment absolute position
        this.column++;   // Move one column to the right
        
        // Special handling for newlines - they reset column and increment line
        if ("\n".equals(currentChar)) {
            this.line++;     // Move to next line
            this.column = 0; // Reset to start of line
        }
    }
    
    /**
     * Advance position without knowing the specific character
     * Used when character context isn't available
     */
    public void advance() {
        advance(null);
    }
    
    /**
     * Create an independent copy of this position
     * Useful for marking token start/end positions without affecting original
     */
    public Position copy() {
        return new Position(idx, line, column);
    }
    
    // Getter methods for accessing position information
    public int getIdx() { return idx; }
    public int getLine() { return line; }
    public int getColumn() { return column; }
    
    /**
     * Human-readable string representation for error messages
     * Converts 0-based internal representation to 1-based user-friendly format
     * 
     * NOTE: On line 1, column should match the index (both 0-based internally,
     * but line displays as 1 while column displays as actual index)
     */
    @Override
    public String toString() {
        return "line " + (line + 1) + ", column " + column + " (idx: " + idx + ")";
    }
}

//////////////////////////////
/// LEXICAL ANALYZER (TOKENIZER) ///
//////////////////////////////

/**
 * JARLANG LEXER - Converts source code text into meaningful tokens
 * 
 * The lexer is the first phase of language processing. It takes raw text
 * and breaks it into tokens (words, numbers, operators, etc.) that the
 * parser can understand.
 * 
 * LEXICAL ANALYSIS PROCESS:
 * 1. Read characters one by one from input text
 * 2. Group related characters into tokens (e.g., "123" becomes INT token)
 * 3. Skip whitespace and comments
 * 4. Handle special cases like string literals and multi-character operators
 * 5. Report errors for invalid characters
 * 
 * SUPPORTED TOKENS:
 * - Numbers: integers (42) and floats (3.14)
 * - Operators: +, -, *, /, ^, =, !, <, >
 * - Delimiters: (, ), {, }, comma, semicolon, colon
 * - Strings: "text in quotes"
 * - Comments: # single line, :guard/ multi-line /guard:
 * - EOF: End of file marker
 * 
 * The lexer maintains position tracking for error reporting and
 * handles warrior-themed token mapping through the CONSTANTS class.
 */
class JarlangLexer {
    private String filename;      // Source file name (for error reporting)
    private String text;          // Complete input text to tokenize
    private Position pos;         // Current position in text
    private String currentChar;   // Character currently being processed
    
    /**
     * Constructor initializes the lexer with input text
     * @param filename Name of source file (used in error messages)
     * @param text Complete source code to tokenize
     */
    public JarlangLexer(String filename, String text) {
        this.filename = filename;
        this.text = text;
        // Start position before first character
        this.pos = new Position(-1, 0, -1);
        this.currentChar = "";
        advance(); // Move to first character
    }
    
    /**
     * Advance to the next character in the input text
     * This method is called constantly during tokenization
     * It updates both position tracking and current character
     */
    private void advance() {
        pos.advance(currentChar);  // Update position with current char info
        if (pos.getIdx() < text.length()) {
            // Extract single character at current position
            currentChar = String.valueOf(text.charAt(pos.getIdx()));
        } else {
            // Reached end of input
            currentChar = "";
        }
    }
    
    /**
     * Check if a character is a digit (0-9)
     * Used for identifying numeric literals
     * @param c Character to test
     * @return true if character is a digit
     */
    private boolean isDigit(String c) {
        return CONSTANTS.DIGITS.contains(c);
    }
    
    /**
     * MAIN TOKENIZATION METHOD - The heart of lexical analysis
     * 
     * This method implements a finite state machine that processes
     * input character by character, building tokens as it goes.
     * 
     * TOKENIZATION ALGORITHM:
     * 1. Loop through all characters in input
     * 2. Skip whitespace (spaces, tabs, newlines)
     * 3. Recognize multi-character tokens (numbers, strings)
     * 4. Map single characters to warrior-themed tokens
     * 5. Handle special cases (comments, invalid characters)
     * 6. Add EOF token at the end
     * 
     * @return List of tokens representing the input program
     * @throws IllegalCharError if invalid characters are encountered
     */
    public List<Token> tokenize() throws IllegalCharError {
        List<Token> tokens = new ArrayList<>();
        
        // Main tokenization loop - process until end of input
        while (!currentChar.isEmpty()) {
            
            // SKIP WHITESPACE - spaces, tabs, newlines are not meaningful tokens
            if (" \t\n".contains(currentChar)) {
                advance();
            }
            
            // NUMERIC LITERALS - integers and floating point numbers
            // Examples: 42, 3.14, 0.5, 123
            else if (isDigit(currentChar)) {
                tokens.add(makeNumber());  // Delegate to specialized method
            }

            // IDENTIFIERS AND KEYWORDS - multi-character names like "pi"
            else if (Character.isLetter(currentChar.charAt(0))) {
                tokens.add(makeIdentifier());
            }

            
            // MATHEMATICAL OPERATORS - mapped to warrior theme
            // Each operator gets converted to its warrior-themed token type
            else if ("+".equals(currentChar)) {
                tokens.add(new Token(CONSTANTS.TT_PLUS, currentChar));    // "commune"
                advance();
            }
            else if ("-".equals(currentChar)) {
                tokens.add(new Token(CONSTANTS.TT_MINUS, currentChar));   // "banish"
                advance();
            }
            else if ("*".equals(currentChar)) {
                tokens.add(new Token(CONSTANTS.TT_MUL, currentChar));     // "rally"
                advance();
            }
            else if ("/".equals(currentChar)) {
                tokens.add(new Token(CONSTANTS.TT_DIV, currentChar));     // "slash"
                advance();
            }
            else if ("^".equals(currentChar)) {
                tokens.add(new Token(CONSTANTS.TT_POW, currentChar));     // "ascend"
                advance();
            }
            
            // PARENTHESES AND GROUPING - for expression precedence
            else if ("(".equals(currentChar)) {
                tokens.add(new Token(CONSTANTS.TT_LPAREN, currentChar));  // "gather"
                advance();
            }
            else if (")".equals(currentChar)) {
                tokens.add(new Token(CONSTANTS.TT_RPAREN, currentChar));  // "disperse"
                advance();
            }
            
            // COMPARISON OPERATORS - for future language extensions
            else if ("=".equals(currentChar)) {
                advance();
                if ("=".equals(currentChar)) {
                    tokens.add(new Token(CONSTANTS.TT_EE, "=="));      // "evermore"
                    advance();
                } else {
                    tokens.add(new Token(CONSTANTS.TT_EQ, "="));      // "bind"
                }
            }
            else if ("!".equals(currentChar)) {
                advance(); // Move past '!'
                if ("=".equals(currentChar)) {
                    // Found "!="
                    advance(); // Move past '='
                    tokens.add(new Token(CONSTANTS.TT_NE, "!="));
                } else {
                    // Just single "!" (logical not)
                    tokens.add(new Token(CONSTANTS.TT_NOT, "!"));
                }
            }
            else if ("<".equals(currentChar)) {
                advance();      // "lessen"
                if("=".equals(currentChar)) {
                    tokens.add(new Token(CONSTANTS.TT_LE, "<="));      // "atmost"
                    advance();
                } else {
                    // Just single "<"
                    tokens.add(new Token(CONSTANTS.TT_LT, "<"));
                }
            }
            else if (">".equals(currentChar)) {
                advance();      // "heighten"
                if("=".equals(currentChar)) {
                    tokens.add(new Token(CONSTANTS.TT_GE, ">="));      // "atleast"
                    advance();
                } else {
                    // Just single ">"
                    tokens.add(new Token(CONSTANTS.TT_GT, ">"));
                }
            }
            
            // PUNCTUATION AND DELIMITERS - for structured programming
            else if (",".equals(currentChar)) {
                tokens.add(new Token(CONSTANTS.TT_COMMA, currentChar));   // "separate"
                advance();
            }
            else if (":".equals(currentChar)) {
                tokens.add(new Token(CONSTANTS.TT_COLON, currentChar));   // "declare"
                advance();
            }
            else if (";".equals(currentChar)) {
                tokens.add(new Token(CONSTANTS.TT_SEMI, currentChar));    // "conclude"
                advance();
            }
            else if ("{".equals(currentChar)) {
                tokens.add(new Token(CONSTANTS.TT_LBRACE, currentChar));  // "enclose"
                advance();
            }
            else if ("}".equals(currentChar)) {
                tokens.add(new Token(CONSTANTS.TT_RBRACE, currentChar));  // "release"
                advance();
            }
            
            // SINGLE-LINE COMMENTS - everything after # until newline
            // Example: "3 + 5  # This calculates the sum"
            else if ("#".equals(currentChar)) {
                advance(); // Skip the '#' character
                // Skip everything until end of line
                while (!currentChar.isEmpty() && !"\n".equals(currentChar)) {
                    advance();
                }
                // If we stopped at newline, advance past it too
                if ("\n".equals(currentChar)) {
                    advance();
                }
                // Note: Comments are not added as tokens - they're just skipped
            }
            
            // STRING LITERALS - text enclosed in double quotes
            // Examples: "hello", "warrior code", "3.14 is pi"
            else if ("\"".equals(currentChar)) {
                advance(); // Skip opening quote
                StringBuilder strValue = new StringBuilder();
                
                // Collect all characters until closing quote
                while (!currentChar.isEmpty() && !"\"".equals(currentChar)) {
                    strValue.append(currentChar);
                    advance();
                }
                
                if ("\"".equals(currentChar)) {
                    advance(); // Skip closing quote
                    tokens.add(new Token(CONSTANTS.TT_STRING, strValue.toString()));  // "tale"
                } else {
                    // String was not properly closed - this is an error
                    throw new IllegalCharError("Unterminated string literal: " + strValue.toString(), 
                                             "at position " + pos.toString());
                }
            }
            
            // INVALID CHARACTERS - anything not recognized by the language
            // This includes symbols like @, $, %, etc. that aren't part of Jarlang
            else if (!currentChar.isEmpty()) {
                String illegalChar = currentChar;
                advance(); // Move past the invalid character
                throw new IllegalCharError("Illegal character '" + illegalChar + "'", 
                                         "at position " + pos.toString());
            }
        }
        
        // Add EOF (End Of File) token to mark end of input
        // This helps the parser know when to stop processing
        tokens.add(new Token(CONSTANTS.TT_EOF, ""));  // "end"
        return tokens;
    }
    
    /**
     * SPECIALIZED NUMBER TOKENIZATION METHOD
     * 
     * This method handles the complex task of parsing numeric literals.
     * It must distinguish between integers and floating-point numbers
     * and handle edge cases properly.
     * 
     * SUPPORTED NUMBER FORMATS:
     * - Integers: 42, 0, 123456
     * - Floats: 3.14, 0.5, 123.456
     * - Edge cases: .5 (leading decimal), 123. (trailing decimal)
     * 
     * ALGORITHM:
     * 1. Collect consecutive digits
     * 2. If we encounter a dot (.), check if it's the first one
     * 3. Continue collecting digits after the dot
     * 4. Stop at first non-digit, non-dot character
     * 5. Determine if result is INT or FLOAT based on dot count
     * 
     * @return Token representing the parsed number
     */
    private Token makeNumber() {
        StringBuilder numStr = new StringBuilder();
        int dotCount = 0;  // Track decimal points to distinguish int vs float
        
        // Collect digits and at most one decimal point
        while (!currentChar.isEmpty() && (isDigit(currentChar) || ".".equals(currentChar))) {
            if (".".equals(currentChar)) {
                if (dotCount == 1) {
                    // Already found one dot, so this must be end of number
                    // (second dot might be start of next token)
                    break;
                }
                dotCount++;
                numStr.append(".");
            } else {
                // Regular digit
                numStr.append(currentChar);
            }
            advance();
        }
        
        // Determine token type based on whether we found a decimal point
        if (dotCount == 0) {
            return new Token(CONSTANTS.TT_INT, numStr.toString());      // "int"
        } else {
            return new Token(CONSTANTS.TT_FLOAT, numStr.toString());    // "float"
        }
    }

    /**
     * SPECIALIZED IDENTIFIER TOKENIZATION METHOD
     * 
     * This method handles the parsing of identifiers, which are names
     * used for variables, functions, and keywords. Identifiers can consist
     * of letters, digits, and underscores, but must start with a letter.
     * 
     * ALGORITHM:
     * 1. Start with a letter (already confirmed by caller)
     * 2. Collect subsequent letters, digits, or underscores
     * 3. Stop at first character that is not valid for identifiers
     * 4. Return token with type IDENTIFIER and collected name
     * 
     * FUTURE ENHANCEMENT:
     * - Check if the identifier matches any reserved keywords
     *   and return appropriate token type if so.
     * 
     * @return Token representing the parsed identifier
     */
    private Token makeIdentifier() {
        StringBuilder idStr = new StringBuilder();
        
        // Collect letters, digits, and underscores for identifiers
        while (!currentChar.isEmpty() && (Character.isLetterOrDigit(currentChar.charAt(0)) || "_".equals(currentChar))) {
            idStr.append(currentChar);
            advance();
        }
        
        String id = idStr.toString();

        if("pi".equals(id)) {
            // Future enhancement: return specific keyword token type
            // For now, treat all keywords as identifiers
            return new Token(CONSTANTS.TT_PI, String.valueOf(Math.PI));
        }

        if (CONSTANTS.KEYWORDS.containsKey(id)) {
            // Future enhancement: return specific keyword token type
            // For now, treat all keywords as identifiers
            String tokenType = CONSTANTS.KEYWORDS.get(id);
            return new Token(tokenType, id);  // "keyword"
        }
        return new Token(CONSTANTS.TT_IDENTIFIER, id);  // "identifier"
    }
}

//////////////////////////////
/// ABSTRACT SYNTAX TREE (AST) NODE SYSTEM ///
//////////////////////////////

/**
 * ABSTRACT SYNTAX TREE OVERVIEW
 * 
 * After tokenization, the parser builds an Abstract Syntax Tree (AST).
 * The AST represents the hierarchical structure of the program, showing
 * how operators, operands, and expressions relate to each other.
 * 
 * For example, the expression "3 + 4 * 5" becomes:
 *         +
 *        / \
 *       3   *
 *          / \
 *         4   5
 * 
 * This tree structure respects operator precedence (* before +) and
 * makes evaluation straightforward through tree traversal.
 * 
 * AST NODE HIERARCHY:
 * - ASTNode (abstract base): Common interface for all nodes
 *   - NumberNode: Represents numeric literals (3, 4.5, etc.)
 *   - BinOpNode: Represents binary operations (+, -, *, /, etc.)
 *   - (Future: UnaryNode, FunctionNode, VariableNode, etc.)
 * 
 * Each node knows how to evaluate itself, implementing the
 * Interpreter pattern directly in the AST structure.
 */

/**
 * ASTNODE - Abstract base class for all syntax tree nodes
 * 
 * This class defines the common interface that all AST nodes must implement.
 * It uses the Template Method pattern where subclasses provide specific
 * implementations of abstract methods.
 * 
 * Key responsibilities:
 * - Define common interface for all nodes
 * - Provide evaluation capability (interpret pattern)
 * - Enable polymorphic processing of different node types
 * 
 * This approach is much cleaner than Mojo's variant type system
 * and leverages Java's object-oriented features effectively.
 */
abstract class ASTNode {
    /**
     * Evaluate this AST node and return its numeric value
     * This method implements the Interpreter pattern - each node
     * knows how to compute its own value.
     * 
     * @return The numeric result of evaluating this node
     * @throws InterpreterError if evaluation fails (e.g., division by zero)
     */
    //TODO: public abstract double evaluate() throws InterpreterError;
    // Modify ASTNode abstract class:
    public abstract double evaluate(Context context) throws InterpreterError;

// Update all evaluate methods in NumberNode, BinOpNode, etc. to accept Context
    
    /**
     * Get a string describing the type of this node
     * Useful for debugging and error reporting
     * 
     * @return String identifying the node type ("number", "binop", etc.)
     */
    public abstract String getNodeType();
}

/**
 * NUMBERNODE - Represents numeric literals in the AST
 * 
 * This is the simplest type of AST node, representing literal numbers
 * like 42, 3.14, 0, etc. These are the leaf nodes of expression trees.
 * 
 * Examples in Jarlang source:
 * - "42" becomes NumberNode("42")
 * - "3.14" becomes NumberNode("3.14")
 * - "-5" becomes UnaryNode(MINUS, NumberNode("5"))  [future feature]
 * 
 * NumberNodes are created by the parser when it encounters INT or FLOAT
 * tokens from the lexer. They store the value as a string initially
 * and convert to double only when evaluation is needed.
 * 
 * This design allows for:
 * - Lazy evaluation (conversion only when needed)
 * - Preservation of original format for error reporting
 * - Support for different numeric representations
 */
class NumberNode extends ASTNode {
    private String value;  // Stored as string to preserve original format
    
    /**
     * Constructor from token (typical case during parsing)
     * @param token The INT or FLOAT token containing the numeric value
     */
    public NumberNode(Token token) {
        this.value = token.getValue();
    }
    
    /**
     * Constructor from string value (for testing or manual creation)
     * @param value String representation of the number
     */
    public NumberNode(String value) {
        this.value = value;
    }
    
    /**
     * Get the string representation of this number
     * @return Original string value
     */
    public String getValue() { return value; }
    
    /**
     * Evaluate this number node by converting string to double
     * This is where the actual numeric conversion happens.
     * 
     * @return The numeric value of this literal
     * @throws InterpreterError if the string cannot be parsed as a number
     */
    @Override
    public double evaluate(Context context) throws InterpreterError {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new InterpreterError("Invalid number format: " + value);
        }
    }
    
    /**
     * Return node type identifier for debugging/error reporting
     */
    @Override
    public String getNodeType() { return "number"; }
    
    /**
     * String representation for debugging and AST visualization
     */
    @Override
    public String toString() {
        return value;
    }
}

/**
 * BINOPNODE - Represents binary operations in the AST
 * 
 * Binary operations are expressions with two operands and one operator,
 * such as addition, subtraction, multiplication, division, and exponentiation.
 * 
 * Examples in Jarlang:
 * - "3 + 5" becomes BinOpNode(NumberNode("3"), Token("+"), NumberNode("5"))
 * - "2 + 3 * 4" becomes BinOpNode(NumberNode("2"), Token("+"), BinOpNode(...))
 * - "10 ^ 3" becomes BinOpNode(NumberNode("10"), Token("^"), NumberNode("3"))
 * 
 * TREE STRUCTURE:
 * Each BinOpNode has exactly three components:
 * - left: Left operand (any AST node - NumberNode, BinOpNode, etc.)
 * - opToken: The operator token (commune, banish, rally, slash, ascend, etc.)
 * - right: Right operand (any AST node - NumberNode, BinOpNode, etc.)
 * 
 * This creates a recursive tree structure where complex expressions
 * become trees of BinOpNodes with NumberNodes at the leaves.
 * 
 * EVALUATION STRATEGY:
 * To evaluate a BinOpNode:
 * 1. Recursively evaluate the left operand
 * 2. Recursively evaluate the right operand  
 * 3. Apply the operator to the two values
 * 4. Return the result
 * 
 * This recursive evaluation automatically handles operator precedence
 * because the parser builds the tree with correct precedence.
 */
class BinOpNode extends ASTNode {
    private ASTNode left;         // Left operand (any AST node for full expression support)
    private Token opToken;        // Operator token (commune, banish, rally, slash, ascend, etc.)
    private ASTNode right;        // Right operand (any AST node for full expression support)
    
    /**
     * Constructor creates a binary operation node
     * 
     * Now supports any ASTNode types for both operands, enabling
     * complex nested expressions like ((3 + 4) * (5 - 2)) and 10^(2+3).
     * 
     * @param left Left operand (can be any ASTNode)
     * @param opToken Operator token with warrior-themed type
     * @param right Right operand (can be any ASTNode)
     */
    public BinOpNode(ASTNode left, Token opToken, ASTNode right) {
        this.left = left;
        this.opToken = opToken;
        this.right = right;
    }
    
    // Getter methods for accessing components
    public ASTNode getLeft() { return left; }
    public Token getOpToken() { return opToken; }
    public ASTNode getRight() { return right; }
    
    /**
     * Evaluate this binary operation
     * 
     * This method implements the core arithmetic operations of Jarlang.
     * It demonstrates how the warrior-themed tokens map to actual operations.
     * 
     * OPERATION MAPPING:
     * - "commune" (TT_PLUS) → addition (+)
     * - "banish" (TT_MINUS) → subtraction (-)
     * - "rally" (TT_MUL) → multiplication (*)
     * - "slash" (TT_DIV) → division (/)
     * - "ascend" (TT_POW) → exponentiation (^)
     * 
     * @return Result of applying the operation to left and right operands
     * @throws InterpreterError for runtime errors (division by zero, unknown operators)
     */
    @Override
    public double evaluate(Context context) throws InterpreterError {
        // Recursively evaluate both operands with context
        double leftVal = left.evaluate(context);
        double rightVal = right.evaluate(context);
        
        // Apply the appropriate operation based on token type
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
        } else if (CONSTANTS.TT_POW.equals(opType)) {
            return Math.pow(leftVal, rightVal);
        
        // Comparison operators for future extensions
        } else if (CONSTANTS.TT_EE.equals(opType)) {
            return (leftVal == rightVal) ? 1.0 : 0.0;  // "evermore"
        } else if (CONSTANTS.TT_NE.equals(opType)) {
            return (leftVal != rightVal) ? 1.0 : 0.0;  // "never"
        } else if (CONSTANTS.TT_LT.equals(opType)) {
            return (leftVal < rightVal) ? 1.0 : 0.0;   // "lessen"
        } else if (CONSTANTS.TT_GT.equals(opType)) {
            return (leftVal > rightVal) ? 1.0 : 0.0;   // "heighten"
        } else if (CONSTANTS.TT_LE.equals(opType)) {
            return (leftVal <= rightVal) ? 1.0 : 0.0;  // "atmost"
        } else if (CONSTANTS.TT_GE.equals(opType)) {
            return (leftVal >= rightVal) ? 1.0 : 0.0;  // "atleast"

        } else {
            throw new InterpreterError("Unknown operator: " + opType);
        }
    }
    
    /**
     * Return node type identifier
     */
    @Override
    public String getNodeType() { return "binop"; }
    
    /**
     * String representation showing the tree structure
     * Format: " {left}(operator){right}"
     * Example: " {3}(commune){5}" for "3 + 5"
     */
    @Override
    public String toString() {
        return " {" + left.toString() + "}(" + opToken.toString() + "){" + right.toString() + "}";
    }
}

//////////////////////////////////
/// UNARY OPERATION NODE  ///
///  ///////////////////////////////////
//////////////////////////////////
class UnaryOpNode extends ASTNode {
    private Token opToken;    // Operator token (e.g., banish for negation)
    private ASTNode node;     // Operand (any AST node)
    
    /**
     * Constructor creates a unary operation node
     * 
     * @param opToken Operator token (e.g., banish for negation)
     * @param node Operand (can be any ASTNode)
     */
    public UnaryOpNode(Token opToken, ASTNode node) {
        this.opToken = opToken;
        this.node = node;
    }
    
    // Getter methods for accessing components
    public Token getOpToken() { return opToken; }
    public ASTNode getNode() { return node; }
    
    /**
     * Evaluate this unary operation
     * 
     * This method implements unary operations like negation.
     * 
     * OPERATION MAPPING:
     * - "banish" (TT_MINUS) → negation (-)
     * - "commune" (TT_PLUS) → unary plus (+) [no effect]
     * 
     * @return Result of applying the unary operation to the operand
     * @throws InterpreterError for runtime errors (unknown operators)
     */
    @Override
    public double evaluate(Context context) throws InterpreterError {
        double value = node.evaluate(context);  // Pass context to operand
        
        String opType = opToken.getType();
        
        if (CONSTANTS.TT_MINUS.equals(opType)) {
            return -value;
        } else if (CONSTANTS.TT_PLUS.equals(opType)) {
            return value;
        } else if (CONSTANTS.TT_NOT.equals(opType)) {
            return (value == .0) ? 1.0 : 0.0;  // Logical NOT: 1 -> 0, 0 -> 1
        } else {
            throw new InterpreterError("Unknown unary operator: " + opType);
        }
    }
    
    /**
     * Return node type identifier
     */
    @Override
    public String getNodeType() { return "unaryop"; }
    
    /**
     * String representation showing the unary operation
     * Format: "(operator){operand}"
     * Example: "(banish){5}" for "-5"
     */
    @Override
    public String toString() {
        return "(" + opToken.toString() + "){" + node.toString() + "}";
    }
}

/**
 * AST node for variable assignment: wield variableName expression
 */
class AssignmentNode extends ASTNode {
    private String varName;
    private ASTNode value;
    
    public AssignmentNode(String varName, ASTNode value) {
        this.varName = varName;
        this.value = value;
    }
    
    public String getVarName() { return varName; }
    public ASTNode getValue() { return value; }
    
    @Override
    public double evaluate(Context context) throws InterpreterError {
        if (value instanceof StringNode) {
            // Store as string
            StringNode stringNode = (StringNode) value;
            String stringValue = stringNode.evaluateAsString(context);
            context.setVariable(varName, stringValue);
            return 0.0; // Return dummy value for string assignments
        } else {
            // Store as number
            double result = value.evaluate(context);
            context.setVariable(varName, result);
            return result;
        }
    }
    
    @Override
    public String getNodeType() { return "assignment"; }
    
    @Override
    public String toString() {
        return "(wield " + varName + " " + value.toString() + ")";
    }
}

/**
 * AST node for variable lookup: variableName
 */
class VariableNode extends ASTNode {
    private String varName;
    
    public VariableNode(String varName) {
        this.varName = varName;
    }
    
    public String getVarName() { return varName; }
    
    @Override
    public double evaluate(Context context) throws InterpreterError {
        Object value = context.getVariable(varName);
        if (value == null) {
            throw new InterpreterError("Undefined variable: " + varName);
        }
        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                throw new InterpreterError("Variable '" + varName + "' is not a number");
            }
        } else {
            throw new InterpreterError("Variable '" + varName + "' has unsupported type");
        }
        // Future enhancement: support other types (e.g., strings)
    }
    // Add method to get string value
    public String evaluateAsString(Context context) throws InterpreterError {
        Object value = context.getVariable(varName);
        if (value == null) {
            throw new InterpreterError("Undefined variable: " + varName);
        }
        return value.toString();
    }
    @Override
    public String getNodeType() { return "variable"; }
    
    @Override
    public String toString() {
        return varName;
    }
}

/**
 * AST node for string literals: "hello world"
 */
class StringNode extends ASTNode {
    private String value;
    
    public StringNode(Token token) {
        this.value = token.getValue();
    }
    
    public StringNode(String value) {
        this.value = value;
    }
    
    public String getValue() { return value; }
    
    @Override
    public double evaluate(Context context) throws InterpreterError {
        // For now, strings can't be used in numeric contexts
        throw new InterpreterError("Cannot use string '" + value + "' in numeric expression");
    }
    
    // Add method to get string value
    public String evaluateAsString(Context context) {
        return value;
    }
    
    @Override
    public String getNodeType() { return "string"; }
    
    @Override
    public String toString() {
        return "\"" + value + "\"";
    }
}

/**
 * AST node for print statements: chant expression
 */
class ChantNode extends ASTNode {
    private ASTNode expression;
    
    public ChantNode(ASTNode expression) {
        this.expression = expression;
    }
    
    public ASTNode getExpression() { return expression; }
    
    @Override
    public double evaluate(Context context) throws InterpreterError {
        // Check if expression is a string or numeric
        if (expression instanceof StringNode) {
            StringNode stringNode = (StringNode) expression;
            String output = stringNode.evaluateAsString(context);
            System.out.println(output);
        } else if (expression instanceof VariableNode) {
            VariableNode varNode = (VariableNode) expression;
            Object value = context.getVariable(varNode.getVarName());
            if (value instanceof String) {
                System.out.println(value);
            } else {
                System.out.println(value.toString());
            }
        } else {
            // Numeric expression
            double result = expression.evaluate(context);
            System.out.println(result);
        }
        return 0.0; // Print statements don't return meaningful values
    }
    
    @Override
    public String getNodeType() { return "chant"; }
    
    @Override
    public String toString() {
        return "(chant " + expression.toString() + ")";
    }
}

/**
 * AST node for boolean values: true/false results from comparisons
 */
class BooleanNode extends ASTNode {
    private boolean value;
    
    public BooleanNode(boolean value) {
        this.value = value;
    }
    
    public boolean getValue() { return value; }
    
    @Override
    public double evaluate(Context context) throws InterpreterError {
        // Convert boolean to numeric: true = 1.0, false = 0.0
        return value ? 1.0 : 0.0;
    }
    
    @Override
    public String getNodeType() { return "boolean"; }
    
    @Override
    public String toString() {
        return String.valueOf(value);
    }
}

/**
 * AST node for if statements: judge condition expression orjudge expression
 */
class IfNode extends ASTNode {
    private ASTNode condition;    // The condition to test
    private ASTNode thenBranch;   // Expression to execute if condition is true
    private ASTNode elseBranch;   // Expression to execute if condition is false (optional)
    
    // Constructor with else branch
    public IfNode(ASTNode condition, ASTNode thenBranch, ASTNode elseBranch) {
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }
    
    // Constructor without else branch
    public IfNode(ASTNode condition, ASTNode thenBranch) {
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = null;
    }
    
    public ASTNode getCondition() { return condition; }
    public ASTNode getThenBranch() { return thenBranch; }
    public ASTNode getElseBranch() { return elseBranch; }
    public boolean hasElseBranch() { return elseBranch != null; }
    
    @Override
    public double evaluate(Context context) throws InterpreterError {
        // Evaluate condition
        double conditionValue = condition.evaluate(context);
        
        // In Jarlang, 0.0 is false, anything else is true
        if (conditionValue != 0.0) {
            // Condition is true, execute then branch
            return thenBranch.evaluate(context);
        } else if (hasElseBranch()) {
            // Condition is false and we have an else branch
            return elseBranch.evaluate(context);
        } else {
            // Condition is false and no else branch
            return 0.0; // Default return value
        }
    }
    
    @Override
    public String getNodeType() { return "if"; }
    
    @Override
    public String toString() {
        if (hasElseBranch()) {
            return "(judge " + condition.toString() + " " + thenBranch.toString() + 
                   " orjudge " + elseBranch.toString() + ")";
        } else {
            return "(judge " + condition.toString() + " " + thenBranch.toString() + ")";
        }
    }
}

/**
 * AST node for while loops: lest condition expression
 */
class WhileNode extends ASTNode {
    private ASTNode condition;    // The condition to test
    private ASTNode body;         // Expression to execute while condition is true
    
    public WhileNode(ASTNode condition, ASTNode body) {
        this.condition = condition;
        this.body = body;
    }
    
    public ASTNode getCondition() { return condition; }
    public ASTNode getBody() { return body; }
    
    @Override
    public double evaluate(Context context) throws InterpreterError {
        double lastResult = 0.0;
        
        // Keep executing while condition is true (non-zero)
        while (condition.evaluate(context) != 0.0) {
            lastResult = body.evaluate(context);
        }
        
        return lastResult; // Return result of last iteration
    }
    
    @Override
    public String getNodeType() { return "while"; }
    
    @Override
    public String toString() {
        return "(lest " + condition.toString() + " " + body.toString() + ")";
    }
}
/**
 * Unified result type that can hold either numeric or string values
 */
class Result {
    private Object value;
    private ResultType type;
    
    public enum ResultType {
        NUMBER, STRING
    }
    
    // Constructor for numeric results
    public Result(double value) {
        this.value = value;
        this.type = ResultType.NUMBER;
    }
    
    // Constructor for string results
    public Result(String value) {
        this.value = value;
        this.type = ResultType.STRING;
    }
    
    // Getters
    public Object getValue() { return value; }
    public ResultType getType() { return type; }
    
    public boolean isNumber() { return type == ResultType.NUMBER; }
    public boolean isString() { return type == ResultType.STRING; }
    
    public double asNumber() {
        if (isNumber()) {
            return (Double) value;
        }
        throw new RuntimeException("Result is not a number");
    }
    
    public String asString() {
        if (isString()) {
            return (String) value;
        }
        return value.toString(); // Convert number to string
    }
    
    @Override
    public String toString() {
        if (isString()) {
            return "\"" + value + "\"";
        }
        return value.toString();
    }
}

//////////////////////////////
/// CONTEXT MANAGEMENT SYSTEM ///
/// (Future feature for variables, functions, scopes) ///
/// (Not yet integrated into parser/interpreter) ///
/// ////////////////////////////
class Context {
    // Existing field
    private Map<String, Object> variables = new HashMap<>();
    
    // NEW FIELDS TO ADD:
    private String displayName;           // Human-readable context name
    private Context parentContext;        // Reference to parent scope
    private Position parentEntryPosition; // Where this context was created

    
    // CONSTRUCTORS TO ADD:
    // Constructor for root context (no parent)
    public Context(String displayName) {
    // Future context for variables, functions, etc.
        //private Map<String, Double> variables = new HashMap<>();
        this.displayName = displayName;
        this.parentContext = null;
        this.parentEntryPosition = null;
    }
    
    // Constructor for child context (with parent)
    public Context(String displayName, Context parent, Position entryPosition) {
        this.displayName = displayName;
        this.parentContext = parent;
        this.parentEntryPosition = entryPosition.copy(); // Defensive copy
    }
    
    // GETTER METHODS TO ADD:
    public String getDisplayName() { return displayName; }
    public Context getParentContext() { return parentContext; }
    public Position getParentEntryPosition() { return parentEntryPosition; }
    
    // UTILITY METHODS TO ADD:
    public boolean hasParent() { return parentContext != null; }
    public int getDepth() {
        return hasParent() ? parentContext.getDepth() + 1 : 0;
    }

    public void setVariable(String name, double value) {
        variables.put(name, value);
    }

    public void setVariable(String name, String value) {
        variables.put(name, value);
    }

    public void setVariable(String name, Object value) {
        variables.put(name, value);
    }

    // Generic getter
    public Object getVariable(String name) {
        // Check current context first
        if (variables.containsKey(name)) {
            return variables.get(name);
        }
        
        // If not found and we have a parent, check parent context
        if (hasParent()) {
            return parentContext.getVariable(name);
        }
        
        return null;
    }
    
    // Type-specific getters
    public Double getVariableAsDouble(String name) {
        Object value = getVariable(name);
        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return null; // Can't convert string to number
            }
        }
        return null;
    }
    
    public String getVariableAsString(String name) {
        Object value = getVariable(name);
        if (value != null) {
            return value.toString();
        }
        return null;
    }
    
    // Type checking methods
    public boolean isStringVariable(String name) {
        Object value = getVariable(name);
        return value instanceof String;
    }
    
    public boolean isNumberVariable(String name) {
        Object value = getVariable(name);
        return value instanceof Double || value instanceof Integer;
    }
    
    public Map<String, Object> getAllVariables() {
        Map<String, Object> allVars = new HashMap<>(variables);
        if (hasParent()) {
            Map<String, Object> parentVars = parentContext.getAllVariables();
            for (Map.Entry<String, Object> entry : parentVars.entrySet()) {
                allVars.putIfAbsent(entry.getKey(), entry.getValue());
            }
        }
        return allVars;
    }

    public boolean hasVariable(String name) {
        return variables.containsKey(name) || 
            (hasParent() && parentContext.hasVariable(name));
    }

}


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
    // In JarlangParser class, modify the parse() method:
    public ASTNode parse() throws SyntaxError {
        if (currentToken == null) {
            throw new SyntaxError("No tokens to parse", "at token position " + tokIdx);
        }
        return statement();  // Change from expr() to statement()
    }

    // Add new statement parsing method:
    private ASTNode statement() throws SyntaxError {

        // Check for if statement: if condition then expression [else expression]
        if (currentToken != null && "judge".equals(currentToken.getType())) {
            return ifStatement();
        }

        // Check for while statement: lest condition expression
        if (currentToken != null && "lest".equals(currentToken.getType())) {
            return whileStatement();
        }

        // Check for chant statement: chant expression
        if (currentToken != null && "chant".equals(currentToken.getType())) {
            return chantStatement();
        }

        // Check for assignment: wield identifier expression
        if (currentToken != null && "wield".equals(currentToken.getType())) {
            return assignment();
        }

        // NEW: Check for variable reassignment (identifier = expression)
        if (currentToken != null && CONSTANTS.TT_IDENTIFIER.equals(currentToken.getType())) {
            // Look ahead to see if next token is '='
            if (tokIdx + 1 < tokens.size() && 
                CONSTANTS.TT_EQ.equals(tokens.get(tokIdx + 1).getType())) {
                return assignment(); // Handle as reassignment
            }
            // If not followed by '=', fall through to expression parsing
        }
        
        // Otherwise, parse as expression
        return expr();
    }

    // Add if statement parsing method:
    private ASTNode ifStatement() throws SyntaxError {
        // Consume 'judge' keyword
        advance();
        
        // Parse the condition
        ASTNode condition = expr();
        
        // Parse the then branch
        ASTNode thenBranch = statement();
        
        // Check for optional 'orjudge' (else)
        if (currentToken != null && "orjudge".equals(currentToken.getType())) {
            advance(); // Consume 'orjudge'
            ASTNode elseBranch = statement();
            return new IfNode(condition, thenBranch, elseBranch);
        } else {
            // No else branch
            return new IfNode(condition, thenBranch);
        }
    }

    // Add while statement parsing method:
    private ASTNode whileStatement() throws SyntaxError {
        // Consume 'lest' keyword
        advance();
        
        // Parse the condition
        ASTNode condition = expr();
        
        // Parse the body
        ASTNode body = statement();
        
        return new WhileNode(condition, body);
    }

    // Add chant parsing method:
    private ASTNode chantStatement() throws SyntaxError {
        // Consume 'chant' keyword
        advance();
        
        // Parse the expression to print
        ASTNode expression = expr();
        
        return new ChantNode(expression);
    }

    // Add assignment parsing method:
    private ASTNode assignment() throws SyntaxError {
        String varName;

        // Check if it's a 'wield' (new variable) or identifier (reassignment)
        if (currentToken != null && "wield".equals(currentToken.getType())) {
            // New variable declaration: wield varName expression
            advance(); // Consume 'wield'
            
            if (currentToken == null || !CONSTANTS.TT_IDENTIFIER.equals(currentToken.getType())) {
                throw new SyntaxError("Expected variable name after 'wield'", "at token position " + tokIdx);
            }
            
            varName = currentToken.getValue();
            advance(); // Consume identifier
            
        } else if (currentToken != null && CONSTANTS.TT_IDENTIFIER.equals(currentToken.getType())) {
            // Reassignment: varName = expression
            varName = currentToken.getValue();
            advance(); // Consume identifier
            
            // Expect '=' token
            if (currentToken == null || !CONSTANTS.TT_EQ.equals(currentToken.getType())) {
                throw new SyntaxError("Expected '=' after variable name", "at token position " + tokIdx);
            }
            advance(); // Consume '='
            
        } else {
            throw new SyntaxError("Expected 'wield' or variable name", "at token position " + tokIdx);
        }
        // Parse the value expression
        ASTNode value = expr();
        
        return new AssignmentNode(varName, value);
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

    // Update expr() to call comparison instead of term
    private ASTNode expr() throws SyntaxError {
        return comparison();  // Start with comparison level
    }
    // changed expr() to call comparison()
    private ASTNode comparison() throws SyntaxError {
        ASTNode leftNode = addSub();  // Start with higher-precedence term
        
        // Handle zero or more addition/subtraction operations
        while (currentToken != null && 
               (CONSTANTS.TT_EE.equals(currentToken.getType()) ||     // "=="
                CONSTANTS.TT_NE.equals(currentToken.getType()) ||     // "!="
                CONSTANTS.TT_LT.equals(currentToken.getType()) ||     // "<"
                CONSTANTS.TT_GT.equals(currentToken.getType()) ||     // ">"
                CONSTANTS.TT_LE.equals(currentToken.getType()) ||     // "<="
                CONSTANTS.TT_GE.equals(currentToken.getType()))) { // ">="
            
            Token opToken = currentToken;  // Save the operator
            advance();                     // Move past operator

            ASTNode rightNode = addSub();  // Parse right operand

            // Create binary operation node (now supports any ASTNode types)
            leftNode = new BinOpNode(leftNode, opToken, rightNode);
        }
        
        return leftNode;
    }

    //added addSub() method which was original expr() method easier refactoring
    private ASTNode addSub() throws SyntaxError {
        ASTNode leftNode = term();  // Start with higher-precedence term
        
        // Handle zero or more addition/subtraction operations
        while (currentToken != null && 
            (CONSTANTS.TT_PLUS.equals(currentToken.getType()) ||   // "commune"
                CONSTANTS.TT_MINUS.equals(currentToken.getType()))) { // "banish"
            
            Token opToken = currentToken;
            advance();
            
            ASTNode rightNode = term();
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
    // Modify factor() to handle variable lookup:
    private ASTNode factor() throws SyntaxError {
        Token tok = currentToken;

        // CASE 1: Unary operators
        if (tok != null && (CONSTANTS.TT_MINUS.equals(tok.getType()) || 
                        CONSTANTS.TT_PLUS.equals(tok.getType()) ||
                        CONSTANTS.TT_NOT.equals(tok.getType()))) {
            advance();
            ASTNode factorNode = factor();
            return new UnaryOpNode(tok, factorNode);
        }
        
        // CASE 2: Numeric literal
        if (tok != null && (CONSTANTS.TT_INT.equals(tok.getType()) || 
                        CONSTANTS.TT_FLOAT.equals(tok.getType()))) {
            advance();
            return new NumberNode(tok);
        } 

        // CASE 3: String literal (NEW!)
        else if (tok != null && CONSTANTS.TT_STRING.equals(tok.getType())) {
            advance();
            return new StringNode(tok);
        }

        // CASE 4: Pi constant
        else if (tok != null && CONSTANTS.TT_PI.equals(tok.getType())) {
            advance();
            return new NumberNode(tok);
        }

        // CASE 5: Variable lookup (NEW!)
        else if (tok != null && CONSTANTS.TT_IDENTIFIER.equals(tok.getType())) {
            advance();
            return new VariableNode(tok.getValue());
        }

        // CASE 6: Parenthesized expression
        else if (tok != null && CONSTANTS.TT_LPAREN.equals(tok.getType())) {
            advance();
            ASTNode node = expr();
            
            if (currentToken != null && CONSTANTS.TT_RPAREN.equals(currentToken.getType())) {
                advance();
                return node;
            } else {
                throw new SyntaxError("Expected ')'", "at token position " + tokIdx);
            }
        } 
        // CASE 7: Invalid factor
        else {
            throw new SyntaxError("Expected int, float, identifier, or '('", "at token position " + tokIdx);
        }
    }
}

//////////////////////////////
/// INTERPRETER (EVALUATOR) ///
//////////////////////////////

/**
 * JARLANG INTERPRETER - Evaluates Abstract Syntax Trees
 * 
 * The interpreter is the third and final phase of language processing.
 * It takes the AST produced by the parser and evaluates it to compute
 * the final result.
 * 
 * INTERPRETATION STRATEGY: Visitor Pattern + Direct Evaluation
 * This implementation uses a hybrid approach:
 * 1. Each AST node knows how to evaluate itself (evaluate() method)
 * 2. The interpreter coordinates the evaluation process
 * 3. Recursive evaluation naturally handles nested expressions
 * 
 * EVALUATION PROCESS:
 * 1. Start at the root of the AST
 * 2. For NumberNodes: convert string to double and return
 * 3. For BinOpNodes: recursively evaluate left and right, then apply operator
 * 4. Handle runtime errors (division by zero, etc.)
 * 
 * ADVANTAGES OF THIS APPROACH:
 * - Simple and easy to understand
 * - Direct correspondence between AST structure and evaluation
 * - Good error reporting with context
 * - Easily extensible for new node types
 * 
 * FUTURE ENHANCEMENTS:
 * - Support for variables and symbol tables
 * - Function calls and scoping
 * - Control flow (if/else, loops)
 * - Type checking and coercion
 * - Debugging and profiling support
 */
class JarlangInterpreter {
    
    private Context context;  // Current variable/function context (for future use)
    
    /**
     * Interpret a complete AST and return the result
     * 
     * This is the main public interface for the interpreter.
     * It provides a clean abstraction for evaluating parsed expressions.
     * 
     * @param ast The root node of the Abstract Syntax Tree
     * @return The final computed value of the expression
     * @throws InterpreterError if evaluation encounters runtime errors
     */
    // Update the interpret method
    public double interpret(ASTNode ast, Context context) throws InterpreterError {
        this.context = context;
        return visit(ast);
    }
    
    // Keep existing interpret method for backward compatibility
    public double interpret(ASTNode ast) throws InterpreterError {
        Context globalContext = new Context("global");
        return interpret(ast, globalContext);
    }

    /**
     * Visit an AST node and evaluate it
     * 
     * This method serves as the main entry point for evaluation.
     * It delegates to the node's own evaluate() method, which implements
     * the specific evaluation logic for that node type.
     * 
     * @param node The AST node to evaluate
     * @return The numeric result of evaluating the node
     * @throws InterpreterError if evaluation fails
     */
    public double visit(ASTNode node) throws InterpreterError {
        return node.evaluate(context);  // Delegate to node's evaluation method
    }
}

//////////////////////////////
/// UTILITY RUNNER METHODS ///
//////////////////////////////

/**
 * JARLANG RUNNERS - High-level interface for language processing pipeline
 * 
 * This utility class provides convenient methods for running the complete
 * Jarlang language processing pipeline. It encapsulates the three main phases:
 * 1. Lexical Analysis (tokenization)
 * 2. Syntax Analysis (parsing) 
 * 3. Semantic Analysis (interpretation)
 * 
 * These methods are designed to be used by:
 * - The interactive REPL shell
 * - Test frameworks
 * - IDE integrations
 * - Command-line tools
 * 
 * The methods handle error propagation and provide clean interfaces
 * that hide the complexity of coordinating multiple processing phases.
 */
class JarlangRunners {
    
    /**
     * Run the lexer on input text and return tokens
     * 
     * This method provides a simple interface for tokenization.
     * It handles lexer initialization and error propagation.
     * 
     * @param filename Source filename (for error reporting)
     * @param text Input text to tokenize
     * @return List of tokens representing the input
     * @throws IllegalCharError if invalid characters are encountered
     */
    public static List<Token> runLexer(String filename, String text) throws IllegalCharError {
        JarlangLexer lexer = new JarlangLexer(filename, text);
        return lexer.tokenize();
    }
    
    /**
     * ParseResult - Container for parser output and potential errors
     * 
     * Since parsing can fail at multiple stages (lexing or parsing),
     * this class provides a structured way to return either:
     * - A successful AST result
     * - A lexical error (from tokenization)
     * - A syntax error (from parsing)
     * 
     * This approach is cleaner than using multiple exception types
     * and allows callers to handle different error types appropriately.
     */
    public static class ParseResult {
        public ASTNode ast;                // Successfully parsed AST (null if error)
        public IllegalCharError lexError;  // Lexical error (null if no error)
        public SyntaxError parseError;     // Syntax error (null if no error)
        
        /**
         * Constructor for ParseResult
         * Exactly one of ast, lexError, or parseError should be non-null
         */
        public ParseResult(ASTNode ast, IllegalCharError lexError, SyntaxError parseError) {
            this.ast = ast;
            this.lexError = lexError;
            this.parseError = parseError;
        }
    }
    
    /**
     * Run the complete lexing and parsing pipeline
     * 
     * This method coordinates the lexer and parser to convert input text
     * into an Abstract Syntax Tree. It handles errors from both phases
     * and returns a structured result.
     * 
     * PROCESSING STEPS:
     * 1. Create and run lexer to get tokens
     * 2. If lexing succeeds, create and run parser
     * 3. Return either successful AST or appropriate error
     * 
     * @param filename Source filename (for error reporting)
     * @param text Input text to parse
     * @return ParseResult containing either AST or error information
     */
    public static ParseResult runParser(String filename, String text) {
        try {
            // PHASE 1: Lexical Analysis
            List<Token> tokens = runLexer(filename, text);
            
            // PHASE 2: Syntax Analysis
            JarlangParser parser = new JarlangParser(tokens);
            ASTNode ast = parser.parse();
            
            // Success: return AST with no errors
            return new ParseResult(ast, null, null);
            
        } catch (IllegalCharError e) {
            // Lexical error: invalid characters in input
            return new ParseResult(null, e, null);
        } catch (SyntaxError e) {
            // Syntax error: invalid token sequence
            return new ParseResult(null, null, e);
        }
    }
    
    /**
     * Run the interpreter on a parsed AST
     * 
     * This method provides a clean interface for AST evaluation.
     * It handles interpreter initialization and error propagation.
     * 
     * @param ast Abstract Syntax Tree to evaluate
     * @return Computed numeric result
     * @throws InterpreterError if evaluation fails (e.g., division by zero)
     */
    public static double runInterpreter(ASTNode ast, Context context) throws InterpreterError {
        JarlangInterpreter interpreter = new JarlangInterpreter();
        // Future: Pass context to interpreter for variable/function lookup
        return interpreter.interpret(ast, context);
    }
    public static double runInterpreter(ASTNode ast) throws InterpreterError {
        JarlangInterpreter interpreter = new JarlangInterpreter();
        Context context = new Context("<global>"); // Create a global context
        return interpreter.interpret(ast, context);
    }
}

//////////////////////////////
/// TESTING AND DEBUGGING UTILITIES ///
//////////////////////////////

/**
 * JARLANG TEST UTILITIES - Tools for testing and debugging the language implementation
 * 
 * This class provides utility methods for testing individual components
 * of the Jarlang language implementation. These tools are essential for:
 * 
 * DEVELOPMENT AND DEBUGGING:
 * - Verifying lexer behavior on various inputs
 * - Testing parser output and AST structure
 * - Debugging tokenization issues
 * - Educational demonstrations of language internals
 * 
 * INTEGRATION TESTING:
 * - Validating the complete processing pipeline
 * - Regression testing for language changes
 * - Performance benchmarking
 * - Error handling verification
 * 
 * The methods in this class provide detailed output that helps developers
 * understand how the language processes input and where problems occur.
 */
class JarlangTestUtils {
    
    /**
     * Test the lexer with detailed output for debugging
     * 
     * This method provides a comprehensive view of how the lexer processes
     * input text. It's invaluable for debugging tokenization issues and
     * understanding how the lexer handles different input patterns.
     * 
     * OUTPUT INCLUDES:
     * - Original input text
     * - Success/failure status
     * - Complete list of generated tokens
     * - Detailed error information if tokenization fails
     * 
     * USAGE SCENARIOS:
     * - Debugging lexer behavior on edge cases
     * - Verifying that new token types are recognized correctly
     * - Testing error handling for invalid characters
     * - Educational demonstrations of tokenization process
     * 
     * @param text Input text to tokenize and analyze
     * @return Detailed string report of lexer behavior
     */
    public static String testLexer(String text) {
        // Create lexer with test filename
        JarlangLexer lexer = new JarlangLexer("<stdin>", text);
        
        StringBuilder result = new StringBuilder();
        result.append("=== JARLANG LEXER TEST REPORT ===\n");
        result.append("Input text: '").append(text).append("'\n");
        result.append("Text length: ").append(text.length()).append(" characters\n");
        result.append("\n--- TOKENIZATION PROCESS ---\n");
        
        try {
            // Attempt to tokenize the input
            List<Token> tokens = lexer.tokenize();
            
            // SUCCESS: Report successful tokenization
            result.append("✓ Tokenization SUCCESSFUL\n");
            result.append("Generated ").append(tokens.size()).append(" tokens:\n\n");
            
            // Display each token with detailed information
            for (int i = 0; i < tokens.size(); i++) {
                Token token = tokens.get(i);
                result.append(String.format("[%d] %s\n", i, token.toString()));
                
                // Add warrior-themed explanation for operators
                String explanation = getTokenExplanation(token.getType());
                if (!explanation.isEmpty()) {
                    result.append("    → ").append(explanation).append("\n");
                }
            }
            
            result.append("\n--- SUMMARY ---\n");
            result.append("Total tokens: ").append(tokens.size()).append("\n");
            result.append("Status: Ready for parsing\n");
            
        } catch (IllegalCharError e) {
            // FAILURE: Report lexer error with details
            result.append("✗ Tokenization FAILED\n");
            result.append("Error type: IllegalCharError\n");
            result.append("Error details: ").append(e.toString()).append("\n");
            result.append("Position: ").append(e.getPosition()).append("\n");
            result.append("\n--- DEBUGGING HINTS ---\n");
            result.append("• Check for unsupported characters\n");
            result.append("• Verify string literals are properly closed\n");
            result.append("• Ensure all characters are valid Jarlang tokens\n");
        }
        
        return result.toString();
    }
    
    /**
     * Get human-readable explanation for warrior-themed tokens
     * 
     * This helper method provides educational descriptions of the
     * warrior-themed token system, helping users understand the
     * mapping between standard operators and Jarlang's creative names.
     * 
     * @param tokenType Token type from CONSTANTS
     * @return Human-readable explanation of the token's purpose
     */
    private static String getTokenExplanation(String tokenType) {
        switch (tokenType) {
            case "commune":    return "Addition operator (+) - brings numbers together";
            case "banish":     return "Subtraction/Negative operator (-) - removes value";
            case "rally":      return "Multiplication operator (*) - amplifies numbers";
            case "slash":      return "Division operator (/) - splits numbers apart";
            case "gather":     return "Left parenthesis (() - groups expressions";
            case "disperse":   return "Right parenthesis ()) - closes grouping";
            case "ascend":     return "Exponentiation operator (^) - raises to power";
            case "bind":       return "Equality operator (=) - compares for sameness";
            case "differ":     return "Inequality operator (!) - compares for difference";
            case "lessen":     return "Less-than operator (<) - compares magnitude";
            case "heighten":   return "Greater-than operator (>) - compares magnitude";
            case "int":        return "Integer number literal";
            case "float":      return "Floating-point number literal";
            case "tale":       return "String literal - text in quotes";
            case "end":        return "End of file marker";
            default:           return "";
        }
    }
}
