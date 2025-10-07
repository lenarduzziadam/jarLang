package lib;

import java.util.Scanner;
import java.util.List;

/**
 * Interactive REPL Shell for Jarlang language
 * Translated from Mojo shell.mojo implementation
 */
public class JarlangShell {
    private Scanner scanner;
    // TODO: Uncomment when we implement these classes
    // private JarlangLexer lexer;
    // private JarlangParser parser;
    // private JarlangInterpreter interpreter;
    private boolean showTokens = true; // Debug mode toggle

    public JarlangShell() {
        this.scanner = new Scanner(System.in);
        // Initialize components (will create these classes next)
        // this.lexer = new JarlangLexer();
        // this.parser = new JarlangParser();
        // this.interpreter = new JarlangInterpreter();
    }

    /**
     * Main REPL loop - equivalent to shell_repl() in Mojo
     */
    public void runREPL() {
        System.out.println("Jarlang REPL - Type 'q!' to quit or '!help' for help");
        
        while (true) {
            try {
                System.out.print("duel -> ");
                String input = scanner.nextLine().trim();
                
                // Handle special commands
                if (input.equals("q!")) {
                    System.out.println("Farewell, brave warrior!");
                    break;
                }
                
                if (input.equals("!help")) {
                    showHelp();
                    continue;
                }
                
                if (input.equals("!test")) {
                    runBuiltinTests();
                    continue;
                }
                
                if (input.equals("!tokens")) {
                    showTokens = !showTokens;
                    System.out.println("Token display " + (showTokens ? "enabled" : "disabled"));
                    continue;
                }
                
                // Process expression
                if (!input.isEmpty()) {
                    processExpression(input);
                }
                
            } catch (Exception e) {
                System.err.println("Error reading input: " + e.getMessage());
            }
        }
        
        scanner.close();
    }

    /**
     * Process a Jarlang expression - equivalent to the default case in Mojo
     */
    private void processExpression(String input) {
        try {
            // Tokenize
            List<Token> tokens = tokenize(input);
            
            if (showTokens && !tokens.isEmpty()) {
                System.out.println("Found " + tokens.size() + " tokens");
                System.out.println("Tokens: " + formatTokens(tokens));
            }

            // Parse
            ASTNode ast = parse(tokens);
            if (ast != null) {
                System.out.println("AST: " + ast.toString());
                
                // Evaluate/Interpret
                double result = interpret(ast);
                System.out.println("Result: " + formatResult(result));
                System.out.println("JarKnight: Successfully evaluated!");
            }
            
        } catch (LexerException e) {
            System.err.println("JarKnight Ashamed: " + e.getMessage());
        } catch (ParserException e) {
            System.err.println("JarKnight Confused: " + e.getMessage());
        } catch (InterpreterException e) {
            System.err.println("JarKnight Failed: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("JarKnight Crashed: " + e.getMessage());
        }
    }

    /**
     * Tokenize input string
     */
    private List<Token> tokenize(String input) throws LexerException {
        try {
            return JarlangRunners.runLexer("<stdin>", input);
        } catch (IllegalCharError e) {
            throw new LexerException(e.toString());
        }
    }

    /**
     * Parse tokens into AST
     */
    private ASTNode parse(List<Token> tokens) throws ParserException {
        try {
            JarlangParser parser = new JarlangParser(tokens);
            return parser.parse();
        } catch (SyntaxError e) {
            throw new ParserException(e.toString());
        }
    }

    /**
     * Interpret/evaluate AST
     */
    private double interpret(ASTNode ast) throws InterpreterException {
        try {
            return JarlangRunners.runInterpreter(ast);
        } catch (InterpreterError e) {
            throw new InterpreterException(e.toString());
        }
    }

    /**
     * Format tokens for display - equivalent to token_str building in Mojo
     */
    private String formatTokens(List<Token> tokens) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < tokens.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append("{").append(tokens.get(i).toString()).append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Format numeric result (handle int vs float display)
     */
    private String formatResult(double result) {
        // If result is a whole number, display as integer
        if (result == Math.floor(result) && !Double.isInfinite(result)) {
            return String.valueOf((long) result);
        } else {
            return String.valueOf(result);
        }
    }

    /**
     * Show help message
     */
    private void showHelp() {
        System.out.println("Jarlang Commands:");
        System.out.println("  q! - quit");
        System.out.println("  !test - run built-in tests");
        System.out.println("  !tokens - toggle token display mode");
        System.out.println("  !help - show this help");
        System.out.println("  <expr> - parse and evaluate expression");
        System.out.println();
        System.out.println("Warrior operators:");
        System.out.println("  commune (+) - addition");
        System.out.println("  banish (-) - subtraction");
        System.out.println("  rally (*) - multiplication");
        System.out.println("  slash (/) - division");
        System.out.println("  gather (() - left parenthesis");
        System.out.println("  disperse ()) - right parenthesis");
    }

    /**
     * Run built-in tests
     */
    private void runBuiltinTests() {
        System.out.println("Running lexer tests...");
        String[] testExpressions = {
            "3 + 5",
            "12.34 * 56 - 78 / 9",
            "(1 + 2) * 3.5 - 4 / (5 + 6)"
        };
        
        for (String expr : testExpressions) {
            System.out.println("Testing: " + expr);
            try {
                processExpression(expr);
            } catch (Exception e) {
                System.err.println("Test failed: " + e.getMessage());
            }
            System.out.println();
        }
    }

}

// Exception classes for different components
class LexerException extends Exception {
    public LexerException(String message) { super(message); }
}

class ParserException extends Exception {
    public ParserException(String message) { super(message); }
}

class InterpreterException extends Exception {
    public InterpreterException(String message) { super(message); }
}

