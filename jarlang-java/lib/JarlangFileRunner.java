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
    
    /**
     * Execute a Jarlang source file
     * 
     * @param filepath Path to the .vase or .pot file to execute
     * @return ExecutionResult containing output and any errors
     */
    public static ExecutionResult executeFile(String filepath) {
        StringBuilder output = new StringBuilder();
        Context globalContext = new Context("file:" + filepath);
        
        try {
            // Read file contents
            String content = readFile(filepath);
            
            // Split into statements (each line is a statement for now)
            String[] lines = content.split("\n");
            
            output.append("🏺 Executing Jarlang vase: ").append(filepath).append("\n");
            output.append("=".repeat(50)).append("\n\n");
            
            int lineNumber = 1;
            for (String line : lines) {
                line = line.trim();
                
                // Skip empty lines and comments
                if (line.isEmpty() || line.startsWith("#")) {
                    lineNumber++;
                    continue;
                }
                
                try {
                    // Execute the line
                    Result result = executeLine(line, globalContext);
                    
                    // Only show results for expressions (not statements like chant)
                    if (result.isNumber() && result.asNumber() != 0.0) {
                        output.append("Line ").append(lineNumber).append(" → ").append("number").append(" = ").append(result.asNumber()).append("\n");
                    }
                    
                } catch (Exception e) {
                    output.append("❌ Error on line ").append(lineNumber).append(": ").append(e.getMessage()).append("\n");
                    return new ExecutionResult(output.toString(), false, e.getMessage());
                }
                
                lineNumber++;
            }
            
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
}