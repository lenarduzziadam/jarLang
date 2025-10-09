package lib.errors;

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