package lib.tokens;

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
public class Position {
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
