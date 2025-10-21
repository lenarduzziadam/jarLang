package lib;

import java.util.*;
import java.nio.file.*;
import java.io.IOException;

/**
 * JARLANG FILE RUNNER - Executes Jarlang scripts from files
 * 
 * This class handles reading and executing Jarlang source files with
 * the warrior-themed .vase or .pot extensions. It provides both single-file
 * execution and batch processing capabilities.
 */
public class JarlangFileRunner {
    // static set to track already imported files (canonical normalized paths)
    private static final Set<String> importedFiles = new HashSet<>();
    
    /**
     * Execute a Jarlang source file
     * 
     * @param filepath Path to the .vase or .pot file to execute
     * @return ExecutionResult containing output and any errors
     */
    public static ExecutionResult executeFile(String filepath) {
        StringBuilder output = new StringBuilder();
        Context globalContext = new Context("file:" + filepath);
        importedFiles.clear();
        

        try {
            // Read file contents
            String content = readFile(filepath);

            // Tokenize whole file and parse into a single AST (can be a BlockNode)
            List<Token> tokens = JarlangRunners.runLexer(filepath, content);
            JarlangParser parser = new JarlangParser(tokens);
            ASTNode ast = parser.parse();

            output.append("🏺 Executing Jarlang vase: ").append(filepath).append("\n");
            output.append("=".repeat(50)).append("\n\n");

            // Interpret the entire AST in the single global context
            double result = JarlangRunners.runInterpreter(ast, globalContext);

            output.append("Result: ").append(result).append("\n");
            output.append("\n🗡️ Execution completed successfully!\n");
            return new ExecutionResult(output.toString(), true, null);

        } catch (Exception e) {
            output.append("❌ Failed to execute file: ").append(e.getMessage()).append("\n");
            return new ExecutionResult(output.toString(), false, e.getMessage());
        }
    }
    
    /**
     * Execute a single line of Jarlang code
     */
    private static Result executeLine(String line, Context context) throws Exception {
        // Tokenize
        List<Token> tokens = JarlangRunners.runLexer("<file>", line);
        
        // Parse
        JarlangParser parser = new JarlangParser(tokens);
        ASTNode ast = parser.parse();
        
        // Check if this is a string variable lookup
        if (ast instanceof VariableNode) {
            VariableNode varNode = (VariableNode) ast;
            Object value = context.getVariable(varNode.getVarName());
            
            if (value == null) {
                throw new InterpreterError("Undefined variable: " + varNode.getVarName());
            }
            
            if (value instanceof String) {
                return new Result((String) value);
            } else {
                return new Result((Double) value);
            }
        }
        
        // For all other expressions, evaluate as number
        double numResult = JarlangRunners.runInterpreter(ast, context);
        return new Result(numResult);
    }
    
    /**
     * Read file contents into a string
     */
    private static String readFile(String filepath) throws Exception {
        try {
            Path path = Paths.get(filepath);
            return Files.readString(path, java.nio.charset.StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new Exception("Could not read file: " + filepath + " - " + e.getMessage());
        }
    }
    
    /**
     * Container for file execution results
     */
    public static class ExecutionResult {
        public final String output;
        public final boolean success;
        public final String errorMessage;
        
        public ExecutionResult(String output, boolean success, String errorMessage) {
            this.output = output;
            this.success = success;
            this.errorMessage = errorMessage;
        }
    }

    /**
     * Execute a Jarlang source file into the provided context.
     * Resolves relative paths relative to the importing file (if the context was created from a file),
     * otherwise relative to the current working directory. Duplicate imports are ignored.
     */
    public static void runFileIntoContext(String filepath, Context context) throws Exception {
        // Resolve the requested path
        Path requested = Paths.get(filepath);
        Path resolved;

        if (!requested.isAbsolute()) {
            // Try to use the context's filename as base if available
            String display = (context != null ? context.getDisplayName() : null);
            Path baseDir = Paths.get(System.getProperty("user.dir")); // fallback

            if (display != null && display.startsWith("file:")) {
                String fileAnchor = display.substring("file:".length());
                Path baseFile = Paths.get(fileAnchor);
                if (Files.exists(baseFile)) {
                    Path parent = baseFile.getParent();
                    if (parent != null) baseDir = parent;
                }
            }

            resolved = baseDir.resolve(requested).toAbsolutePath().normalize();
        } else {
            resolved = requested.toAbsolutePath().normalize();
        }

        String canonical = resolved.toString();

        // Avoid duplicate import and cycles
        if (importedFiles.contains(canonical)) {
            return;
        }

        if (!Files.exists(resolved)) {
            throw new Exception("Import file not found: " + canonical);
        }

        // Mark as imported to avoid cycles
        importedFiles.add(canonical);

        // Read file, parse and execute in provided context
        String content = readFile(canonical);
        List<Token> tokens = JarlangRunners.runLexer(canonical, content);
        JarlangParser parser = new JarlangParser(tokens);
        ASTNode ast = parser.parse();

        // Interpret in the caller's context
        JarlangRunners.runInterpreter(ast, context);
    }
}