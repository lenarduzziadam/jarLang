import lib.JarlangShell;

/**
 * Main entry point for Jarlang programming language
 * Usage: java Main
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("=== Jarlang Programming Language ===");
        System.out.println("A warrior-themed programming language");
        System.out.println("Version: 1.0.0-alpha");
        System.out.println();
        
        // Create and run the shell
        JarlangShell shell = new JarlangShell();
        shell.runREPL();
    }
}