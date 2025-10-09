package lib.errors;

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