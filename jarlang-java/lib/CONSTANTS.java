package lib;

import java.util.Map;
import java.util.Set;


/**
 * Constants for Jarlang Programming Language
 * Warrior-themed token definitions and other constants
 * Translated from constants.mojo
 */
public class CONSTANTS {

    //////////////////////////////
    /// KEYWORDS FOR JARLANG  ///
    //////////////////////////////
    /// Set of reserved keywords in Jarlang
    /// These cannot be used as identifiers
    /// Future enhancement: map keywords to specific token types
    /// For now, all keywords are treated as TT_KEYWORD tokens
    /// Keywords include control flow, function definitions, etc.
    /// Examples: "judge", "orjudge", "lest", "endure", "march", "mend", "forge", "wield", "vow", "sacred"
    /// These correspond to if, else, while, for, return, function, let, const, final, print in traditional languages
    public static final String TT_WIELD = "wield";   // Variable assignment
    public static final String TT_VOW = "vow";       // Constant declaration
    public static final String TT_SACRED = "sacred"; // Final constant
    public static final String TT_CHANT = "chant";   // Print statement
    public static final String TT_JUDGE = "judge"; // If statement
    public static final String TT_ORJUDGE = "orjudge"; // Else statement
    public static final String TT_LEST = "lest"; // While loop
    public static final String TT_ENDURE = "endure"; // For loop
    public static final String TT_MARCH = "march"; // Function definition
    public static final String TT_MEND = "mend"; // Return statement
    public static final String TT_FORGE = "forge"; // Function definition
    public static final String TT_PIERCE = "pierce"; // Keyword token type
    public static final String TT_EE = "evermore"; // == (equality operator)
    public static final String TT_LE = "atmost"; // <= (less than or equal to) operator
    public static final String TT_GE = "atleast"; // >= (greater than or equal to) operator
    public static final String TT_NE = "notever"; // != (inequality operator)

    // Import type for Jarlang tokens
    public static final String TT_IMPORT = "summon"; // import statement

    //function calls idk if ill use these
    public static final String TT_BLESS = "bless";   // Function call
    public static final String TT_CURSE = "curse";   // Function call
    public static final String TT_HEAL = "heal";     // Function call
    //////////////////////////////////////////////

    public static final Map<String, String> KEYWORDS = Map.ofEntries(
        Map.entry("judge", TT_JUDGE),       // "judge" -> "judge"
        Map.entry("orjudge", TT_ORJUDGE),   // "orjudge" -> "orjudge"  
        Map.entry("lest", TT_LEST),         // "lest" -> "lest"
        Map.entry("endure", TT_ENDURE),     // "endure" -> "endure"
        Map.entry("march", TT_MARCH),       // "march" -> "march"
        Map.entry("mend", TT_MEND),         // "mend" -> "mend"
        Map.entry("forge", TT_FORGE),       // "forge" -> "forge"
        Map.entry("wield", TT_WIELD),       // "wield" -> "wield"
        Map.entry("vow", TT_VOW),           // "vow" -> "vow"
        Map.entry("sacred", TT_SACRED),     // "sacred" -> "sacred"
        Map.entry("chant", TT_CHANT),        // "chant" -> "chant"
        Map.entry("summon", TT_IMPORT)
    );

    

    //////////////////////////////
    /// CONSTANTS FOR JARLANG  ///
    //////////////////////////////
    
    public static final String DIGITS = "0123456789";
    
    //////////////////////////////////
    /// TOKENS OF JARLANG LANGUAGE ///
    //////////////////////////////////
    
    
    
    // Mathematical tokens for expressions
    public static final String TT_INT        = "int";
    public static final String TT_FLOAT      = "float";
    public static final String TT_NEWLINE    = "newline";
    public static final String TT_WHITESPACE = "whitespace";
    public static final String TT_PI         = "Wheel O' Fate"; // π symbol
    public static final String TT_PLUS       = "commune";
    public static final String TT_MINUS      = "banish";
    public static final String TT_MUL        = "rally";
    public static final String TT_DIV        = "slash";
    public static final String TT_LPAREN     = "gather";
    public static final String TT_RPAREN     = "disperse";
    public static final String TT_POW        = "ascend";
    public static final String TT_EQ         = "bind";
    public static final String TT_NOT         = "differ";
    public static final String TT_LT         = "lessen";
    public static final String TT_GT         = "heighten";
    
    // Additional operators (mathematical or logical)
    public static final String TT_COMMA      = "separate";
    public static final String TT_COLON      = "declare";
    public static final String TT_SEMI       = "conclude";
    public static final String TT_LBRACE     = "enclose";
    public static final String TT_RBRACE     = "release";
    
    // Non-mathematical tokens for comments, functions, etc.
    public static final String TT_KEYWORD    = "pierce";
    public static final String TT_PRINT      = "chant";
    public static final String TT_STRING     = "tale";
    public static final String TT_EOF        = "end";
    public static final String TT_RETURN     = "mend";
    public static final String TT_FUNCTION   = "forge";
    public static final String TT_IDENTIFIER = "mark";
    public static final String TT_COMMENT    = "insight";
    public static final String TT_MLSTART    = ":guard/";
    public static final String TT_MLEND      = "/guard:";
}
