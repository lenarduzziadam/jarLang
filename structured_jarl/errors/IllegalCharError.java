package lib.errors;

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
public class IllegalCharError extends Exception {
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