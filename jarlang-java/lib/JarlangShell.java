package lib;

import java.util.Scanner;
import java.util.List;
import java.util.Map;

/**
 * Interactive REPL Shell for Jarlang language
 * Translated from Mojo shell.mojo implementation
 */
public class JarlangShell {
    private Scanner scanner;
    private Context globalContext;
    private boolean showTokens = true; // Debug mode toggle

    // ANSI Color codes for token formatting
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";      // Operators
    private static final String GREEN = "\u001B[32m";    // Numbers
    private static final String BLUE = "\u001B[34m";     // Keywords/Identifiers
    private static final String YELLOW = "\u001B[33m";   // Punctuation
    private static final String CYAN = "\u001B[36m";     // Strings
    private static final String MAGENTA = "\u001B[35m";  // Special tokens
    private static final String PURPLE = "\u001B[35m";  // Additional color example
    private static final String LIGHTGREEN = "\u001B[92m";  
    private static final String PINK = "\u001B[35m";  

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
        scanner = new Scanner(System.in);
        globalContext = new Context("global");
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

                // In your shell's main loop, add this test command:
                if (input.equals("!context")) {
                    Context testContext = new Context("test");
                    testContext.setVariable("x", 42.0);
                    System.out.println("Stored x = " + testContext.getVariable("x"));
                    System.out.println("Context: " + testContext.getDisplayName());
                    continue;
                }

                // Test context hierarchy
                if (input.equals("!hierarchy")) {
                    Context root = new Context("root");
                    Context child = new Context("child", root, new Position(1, 1, 1));
                    Context grandchild = new Context("grandchild", child, new Position(2, 1, 5));
                    
                    System.out.println("Hierarchy depth:");
                    System.out.println("  Root: " + root.getDepth());
                    System.out.println("  Child: " + child.getDepth());
                    System.out.println("  Grandchild: " + grandchild.getDepth());
                    continue;
                }

                // Test specific token types
                if (input.equals("!tokentest")) {
                    String[] testInputs = {
                        "pi", "3.14159", "gather", "disperse", 
                        "commune", "banish", "rally", "slash", "ascend",
                        "42 + pi * 2"
                    };
                    
                    for (String test : testInputs) {
                        System.out.println("Input: '" + test + "'");
                        try {
                            List<Token> tokens = tokenize(test);
                            for (Token token : tokens) {
                                System.out.println("  " + getTokenColor(token) + token.toString() + RESET);
                            }
                        } catch (Exception e) {
                            System.err.println("  Error: " + e.getMessage());
                        }
                        System.out.println();
                    }
                    continue;
                }


                if (input.equals("!vars")) {
                    if (globalContext != null) {
                        System.out.println("=== Current Variables ===");
                        Map<String, Object> allVars = globalContext.getAllVariables();
                        if (allVars.isEmpty()) {
                            System.out.println("  No variables wielded yet.");
                        } else {
                            for (Map.Entry<String, Object> entry : allVars.entrySet()) {
                                Object value = entry.getValue();
                                String type = value instanceof String ? "string" : "number";
                                if (value instanceof String) {
                                    System.out.println("  " + entry.getKey() + " = \"" + value + "\" (" + type + ")");
                                } else {
                                    System.out.println("  " + entry.getKey() + " = " + value + " (" + type + ")");
                                }
                            }
                        }
                    } else {
                        System.out.println("No global context available.");
                    }
                    continue;
                }

                // Handle file execution
                if (input.startsWith("!run ")) {
                    handleFileCommand(input);
                    continue;
                }

                // Handle help command
                if (input.equals("!helpRun")) {
                    displayHelp();
                    continue;
                }

                // Your existing commands:
                if (input.equals("!vars")) {
                    // ... existing vars code
                    continue;
                }

                if (input.startsWith("!tokens ")) {
                    // ... existing tokens code  
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

                // Evaluate/Interpret - now returns Result object
                Result result = interpret(ast);
                
                /// Display result based on type
                if (result.isString()) {
                    System.out.println("Result: " + result.asString());
                } else {
                    System.out.println("Result: " + formatResult(result.asNumber()));
                }
                
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
            JarlangParser parser = new JarlangParser(tokens, "<stdin>");
            return parser.parse();
        } catch (SyntaxError e) {
            throw new ParserException(e.toString());
        }
    }

    /**
     * Interpret/evaluate AST
     */
    /**
     * Interpret/evaluate AST with unified result handling
     */
    private Result interpret(ASTNode ast) throws InterpreterException {
        try {
            // Check if this is a string variable lookup
            if (ast instanceof VariableNode) {
                VariableNode varNode = (VariableNode) ast;
                Object value = globalContext.getVariable(varNode.getVarName());
                
                if (value == null) {
                    throw new InterpreterException("Undefined variable: " + varNode.getVarName());
                }
                
                if (value instanceof String) {
                    return new Result((String) value);
                } else {
                    return new Result((Double) value);
                }
            }
            
            // For all other expressions, evaluate as number
            double numResult = JarlangRunners.runInterpreter(ast, globalContext);
            return new Result(numResult);
            
        } catch (InterpreterError e) {
            throw new InterpreterException(e.toString());
        }
    }

    private String getTokenColor(Token token) {
        String type = token.getType();
        
        // Numbers
        if ("int".equals(type) || "float".equals(type)) {
            return GREEN;
        }
        // Operators
        else if ("commune".equals(type) || "banish".equals(type) || 
                "rally".equals(type) || "slash".equals(type) || "ascend".equals(type)) {
            return RED;
        }
        // Punctuation
        else if ("gather".equals(type) || "disperse".equals(type) || 
                "separate".equals(type) || "conclude".equals(type)) {
            return YELLOW;
        }
        // Keywords/Constants/Equals/Identifiers
        else if ("Wheel O' Fate".equals(type) || "mark".equals(type) || "word".equals(type) || "equals".equals(type)) {
            return BLUE;
        }
        // Strings
        else if ("tale".equals(type)) {
            return CYAN;
        }
        // Special
        else if ("end".equals(type)) {
            return MAGENTA;
        }
        // Purple
        else if( "chant".equals(type) || "forge".equals(type) || "mend".equals(type)) {
            return PURPLE;
        }
        // Orange
        else if("differ".equals(type) || 
                "lessen".equals(type) || "heighten".equals(type)) {
            return PINK;
        }
        // Default (no color)
        else {
            return LIGHTGREEN;
        }
    }
    /**
     * Format tokens for display - equivalent to token_str building in Mojo
     */
    private String formatTokens(List<Token> tokens) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < tokens.size(); i++) {
            if (i > 0) sb.append(", ");
            String color = getTokenColor(tokens.get(i));
            sb.append("{")
                .append(color)
                .append(tokens.get(i).toString())
                .append(RESET)
                .append("}");
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

    /**
     * Execute a Jarlang source file
     */
    private void executeFile(String filepath) {
        // Validate file extension
        if (!filepath.endsWith(".vase") && !filepath.endsWith(".pot")) {
            System.err.println("❌ Invalid file type. Jarlang files must have .vase or .pot extension");
            return;
        }
        
        // Check if file exists
        java.io.File file = new java.io.File(filepath);
        if (!file.exists()) {
            System.err.println("❌ File not found: " + filepath);
            return;
        }
        
        // Execute the file using your JarlangFileRunner
        JarlangFileRunner.ExecutionResult result = JarlangFileRunner.executeFile(filepath);
        
        // Display results
        System.out.print(result.output);
        
        if (!result.success) {
            System.err.println("❌ Execution failed: " + result.errorMessage);
        }
    }

    /**
     * Handle file execution commands
     */
    private void handleFileCommand(String input) {
        String[] parts = input.split("\\s+");
        
        if (parts.length < 2) {
            System.out.println("Usage: !run <filename.vase> or !run <filename.pot>");
            return;
        }
        
        String filename = parts[1];
        executeFile(filename);
    }

    /**
     * Display help information
     */
    private void displayHelp() {
        System.out.println("=== Jarlang REPL Commands ===");
        System.out.println("  Expression     - Evaluate Jarlang expression");
        System.out.println("  !run <file>    - Execute a .vase or .pot file");
        System.out.println("  !vars          - Show all variables");
        System.out.println("  !scope         - Show context hierarchy");
        System.out.println("  !tokens <expr> - Show tokenization of expression");
        System.out.println("  !help          - Show this help");
        System.out.println("  q!             - Quit the REPL");
        System.out.println();
        System.out.println("=== File Extensions ===");
        System.out.println("  .vase          - Jarlang source files");
        System.out.println("  .pot           - Alternative Jarlang extension");
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

