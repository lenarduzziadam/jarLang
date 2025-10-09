package lib.tokens;

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
public class Token {
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
