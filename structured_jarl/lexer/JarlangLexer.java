package lib.lexer;
import java.util.ArrayList;
import java.util.List;

import lib.tokens.*;
import lib.errors.*;

import lib.CONSTANTS;

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
                tokens.add(new Token(CONSTANTS.TT_EQ, currentChar));      // "bind"
                advance();
            }
            else if ("!".equals(currentChar)) {
                tokens.add(new Token(CONSTANTS.TT_NE, currentChar));      // "differ"
                advance();
            }
            else if ("<".equals(currentChar)) {
                tokens.add(new Token(CONSTANTS.TT_LT, currentChar));      // "lessen"
                advance();
            }
            else if (">".equals(currentChar)) {
                tokens.add(new Token(CONSTANTS.TT_GT, currentChar));      // "heighten"
                advance();
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

        if (CONSTANTS.KEYWORDS.contains(id)) {
            // Future enhancement: return specific keyword token type
            // For now, treat all keywords as identifiers
            return new Token(CONSTANTS.TT_KEYWORD, id);  // "keyword"
        }
        return new Token(CONSTANTS.TT_IDENTIFIER, id);  // "identifier"
    }
}