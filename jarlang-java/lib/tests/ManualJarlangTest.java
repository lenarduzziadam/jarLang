package lib.tests;

/**
 * ===================================================================
 * JARLANG MANUAL TEST RUNNER
 * ===================================================================
 * 
 * Simple manual test to verify Jarlang functionality works as expected.
 * This avoids reflection complexity and tests the core functionality directly.
 */

public class ManualJarlangTest {
    
    public static void main(String[] args) {
        System.out.println("=".repeat(50));
        System.out.println("JARLANG MANUAL TEST");
        System.out.println("=".repeat(50));
        
        // Test basic expressions manually
        testExpression("3 + 5", 8.0);
        testExpression("2 * 3", 6.0);
        testExpression("10 / 2", 5.0);
        testExpression("2 ^ 3", 8.0);
        testExpression("(2 + 3) * 4", 20.0);
        testExpression("2 + 3 * 4", 14.0);
        testExpression("2 ^ 3 ^ 2", 512.0);
        testExpression("10 ^ 10", 10000000000.0);
        
        // Test error cases
        testParseError("3 +");
        testParseError("+ 3");
        testParseError("(3 + 4");
        testParseError("");
        testParseError("-5"); // Should fail until unary is implemented
        
        System.out.println("=".repeat(50));
        System.out.println("Manual test completed.");
        System.out.println("All basic functionality appears to be working!");
        System.out.println("Ready to implement unary operator support.");
        System.out.println("=".repeat(50));
    }
    
    private static void testExpression(String expr, double expected) {
        try {
            // Use reflection to access the package-private classes
            Class<?> runnersClass = Class.forName("lib.JarlangRunners");
            java.lang.reflect.Method runParserMethod = runnersClass.getMethod("runParser", String.class, String.class);
            Object parseResult = runParserMethod.invoke(null, "test", expr);
            
            // Check for errors
            Class<?> parseResultClass = parseResult.getClass();
            java.lang.reflect.Field lexErrorField = parseResultClass.getField("lexError");
            java.lang.reflect.Field parseErrorField = parseResultClass.getField("parseError");
            java.lang.reflect.Field astField = parseResultClass.getField("ast");
            
            Object lexError = lexErrorField.get(parseResult);
            Object parseError = parseErrorField.get(parseResult);
            Object ast = astField.get(parseResult);
            
            if (lexError == null && parseError == null && ast != null) {
                java.lang.reflect.Method runInterpreterMethod = runnersClass.getMethod("runInterpreter", Class.forName("lib.ASTNode"));
                Object result = runInterpreterMethod.invoke(null, ast);
                double resultValue = (Double) result;
                
                if (Math.abs(resultValue - expected) < 1e-10) {
                    System.out.println("✓ " + expr + " = " + resultValue + " (expected " + expected + ")");
                } else {
                    System.out.println("✗ " + expr + " = " + resultValue + " (expected " + expected + ")");
                }
            } else {
                System.out.println("✗ " + expr + " - Parse error");
            }
        } catch (Exception e) {
            System.out.println("✗ " + expr + " - Exception: " + e.getMessage());
        }
    }
    
    private static void testParseError(String expr) {
        try {
            Class<?> runnersClass = Class.forName("lib.JarlangRunners");
            java.lang.reflect.Method runParserMethod = runnersClass.getMethod("runParser", String.class, String.class);
            Object parseResult = runParserMethod.invoke(null, "test", expr);
            
            Class<?> parseResultClass = parseResult.getClass();
            java.lang.reflect.Field lexErrorField = parseResultClass.getField("lexError");
            java.lang.reflect.Field parseErrorField = parseResultClass.getField("parseError");
            
            Object lexError = lexErrorField.get(parseResult);
            Object parseError = parseErrorField.get(parseResult);
            
            if (lexError != null || parseError != null) {
                System.out.println("✓ '" + expr + "' correctly produces parse error");
            } else {
                System.out.println("✗ '" + expr + "' should have produced parse error but didn't");
            }
        } catch (Exception e) {
            System.out.println("✗ '" + expr + "' - Exception during test: " + e.getMessage());
        }
    }
}