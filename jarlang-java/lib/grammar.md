# JARLANG LANGUAGE GRAMMAR SPECIFICATION
# =====================================
# 
# This file defines the complete grammatical structure of the Jarlang
# programming language - a warrior-themed mathematical expression language.
# 
# NOTATION:
# - UPPERCASE: Terminal symbols (tokens from lexer)
# - lowercase: Non-terminal symbols (grammar rules)
# - |: Alternative (or)
# - (): Grouping
# - *: Zero or more repetitions
# - +: One or more repetitions
# - ?: Optional (zero or one)
# 
# PRECEDENCE (highest to lowest):
# 1. Parentheses: gather (...) disperse
# 2. Unary operators: banish (-), commune (+)
# 3. Exponentiation: ascend (^) [RIGHT-ASSOCIATIVE]
# 4. Multiplication/Division: rally (*), slash (/)
# 5. Addition/Subtraction: commune (+), banish (-)

## LEXICAL ELEMENTS (TOKENS)

### Numbers
```
INT    ::= [0-9]+
FLOAT  ::= [0-9]+ '.' [0-9]+
       |   '.' [0-9]+
       |   [0-9]+ '.'
```

### Identifiers and Keywords
```
IDENTIFIER ::= [a-zA-Z][a-zA-Z0-9_]*
PI         ::= 'pi'  # Special constant
```

### Operators (Warrior-Themed)
```
PLUS   ::= '+'  # commune  - brings numbers together
MINUS  ::= '-'  # banish   - removes/negates value  
MUL    ::= '*'  # rally    - amplifies numbers
DIV    ::= '/'  # slash    - divides numbers
POW    ::= '^'  # ascend   - raises to power
```

### Comparison Operators (Future Extensions)
```
EQ     ::= '='  # bind     - equality comparison
NE     ::= '!'  # differ   - inequality comparison  
LT     ::= '<'  # lessen   - less than comparison
GT     ::= '>'  # heighten - greater than comparison
```

### Delimiters
```
LPAREN ::= '('  # gather   - groups expressions
RPAREN ::= ')'  # disperse - closes grouping
LBRACE ::= '{'  # enclose  - block start
RBRACE ::= '}'  # release  - block end
COMMA  ::= ','  # separate - list separator
COLON  ::= ':'  # declare  - declaration separator
SEMI   ::= ';'  # conclude - statement terminator
```

### Literals
```
STRING ::= '"' [^"]* '"'  # tale - text literals
```

### Special Tokens
```
EOF    ::= <end-of-input>  # end - marks input termination
```

## SYNTACTIC GRAMMAR

### Top-Level Structure
```
program ::= expression EOF
```

### Expressions (Operator Precedence)
```
expression ::= term ( (PLUS | MINUS) term )*

term ::= power ( (MUL | DIV) power )*

power ::= factor ( POW factor )*

factor ::= unary
       |   primary

unary ::= (PLUS | MINUS) factor
      |   primary

primary ::= number
        |   constant  
        |   LPAREN expression RPAREN

number ::= INT
       |   FLOAT

constant ::= PI
```

## DETAILED GRAMMAR RULES

### Expression Parsing
```
# Addition and Subtraction (Lowest Precedence)
# Left-associative: a + b + c = (a + b) + c
expression ::= term ( (commune | banish) term )*

# Multiplication and Division  
# Left-associative: a * b * c = (a * b) * c
term ::= power ( (rally | slash) power )*

# Exponentiation (Highest Binary Precedence)
# Right-associative: a ^ b ^ c = a ^ (b ^ c)
power ::= factor ( ascend factor )*

# Factors and Unary Operations
factor ::= (banish | commune) factor    # Unary minus/plus
       |   gather expression disperse   # Parenthesized expressions
       |   INT                          # Integer literals
       |   FLOAT                        # Float literals  
       |   PI                           # Pi constant
```

## OPERATOR ASSOCIATIVITY

### Left-Associative Operators
```
commune (+): a + b + c = (a + b) + c
banish  (-): a - b - c = (a - b) - c  
rally   (*): a * b * c = (a * b) * c
slash   (/): a / b / c = (a / b) / c
```

### Right-Associative Operators
```
ascend  (^): a ^ b ^ c = a ^ (b ^ c)
```

## SEMANTIC RULES

### Numeric Operations
```
commune (addition):       left + right
banish  (subtraction):    left - right
rally   (multiplication): left * right
slash   (division):       left / right (error if right = 0)
ascend  (exponentiation): left ^ right
```

### Unary Operations
```
banish (negation):        -operand
commune (unary plus):     +operand (no effect)
```

### Constants
```
pi: Mathematical constant π (3.141592653589793...)
```

## EXAMPLE EXPRESSIONS

### Basic Arithmetic
```
Input:  3 + 5
Tokens: [int, commune, int]
AST:    BinOpNode(3, +, 5)
Result: 8

Input: 3 + 5.1
Tokens [int, commune, float, end]
AST: BinOpNode(3, +, 5.1)
Result: 8.1
```

### Operator Precedence
```
Input:  2 + (3 * 4)
Tokens: [2, commune, gather, 3, rally, 4, disperse, end]
AST:    BinOpNode(2, +, BinOpNode(3, *, 4))
Result: 14
```

### Right-Associative Exponentiation
```
Input:  2 ^ 3 ^ 2  
Tokens: [2, ascend, 3, ascend, 2]
AST:    BinOpNode(2, ^, BinOpNode(3, ^, 2))
Result: 512 (2^9, not 8^2)
```

### Complex Nested Expression
```
Input:  (2 + 3) * 4 ^ 2
Tokens: [gather, 2, commune, 3, disperse, rally, 4, ascend, 2]
AST:    BinOpNode(BinOpNode(2, +, 3), *, BinOpNode(4, ^, 2))
Result: 80 (5 * 16)
```

### Unary Operations
```
Input:  -(2 + 3)
Tokens: [banish, gather, 2, commune, 3, disperse]
AST:    UnaryOpNode(-, BinOpNode(2, +, 3))
Result: -5
```

### Pi Constant Usage
```
Input:  2 * pi
Tokens: [2, rally, pi]
AST:    BinOpNode(2, *, NumberNode(3.14159...))
Result: 6.283185307179586
```

## ERROR CONDITIONS

### Lexical Errors
```
- Illegal characters: @, $, %, etc.
- Unterminated strings: "hello world
- Invalid number formats: 1.2.3, .
```

### Syntax Errors
```
- Missing operands: 3 +
- Unmatched parentheses: (3 + 5
- Invalid token sequences: 3 ) + 5
- Empty expressions: ()
```

### Runtime Errors
```
- Division by zero: 5 / 0
- Invalid number conversion: (internal error)
- Unknown operators: (should not occur with correct parser)
```

## FUTURE LANGUAGE EXTENSIONS

### Variables and Assignment
```
assignment ::= IDENTIFIER bind expression
variable   ::= IDENTIFIER
```

### Function Definitions (Planned)
```
function_def ::= forge IDENTIFIER LPAREN parameter_list? RPAREN block
parameter_list ::= IDENTIFIER (COMMA IDENTIFIER)*
return_stmt ::= mend expression?
```

### Print Statements (Planned)
```
print_stmt ::= chant expression
```

### Control Flow (Future)
```
if_stmt ::= guard expression block (otherwise block)?
while_stmt ::= persist expression block
```

## IMPLEMENTATION NOTES

### Parser Implementation
- Uses recursive descent parsing
- Each grammar rule corresponds to a parser method
- Left-associativity handled by iterative loops
- Right-associativity handled by recursive calls

### AST Structure
- NumberNode: Leaf nodes for numeric literals
- BinOpNode: Binary operations with left/right operands
- UnaryOpNode: Unary operations with single operand
- More node types planned for future features

### Error Recovery
- Lexer reports position information for all errors
- Parser provides context about expected tokens
- Runtime errors include operation details

---

*This grammar specification serves as the authoritative reference for*
*the Jarlang language syntax and semantics. It should be updated as*
*new language features are added.*