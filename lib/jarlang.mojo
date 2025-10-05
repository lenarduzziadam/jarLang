
# imports tokens and other constants
from lib.CONSTANTS import *

##############################
### ERROR HANDLING FOR JARLANG ###
##################################

# Define error types as an enum
@fieldwise_init
struct ErrorType:
    alias ILLEGAL_CHAR = "IllegalCharacter"
    alias SYNTAX_ERROR = "SyntaxError" 
    alias UNEXPECTED_EOF = "UnexpectedEOF"

struct IllegalCharError(Copyable, Movable):
    var message: String
    var position: String

    fn __copyinit__(out self, read other: IllegalCharError):
        self.message = other.message
        self.position = other.position

    fn __moveinit__(out self, deinit other: IllegalCharError):
        self.message = other.message
        self.position = other.position

    fn __init__(out self, message: String, position: String):
        self.message = message
        self.position = position

    fn __repr__(mut self) -> String:
        return "IllegalCharError: " + self.message + " " + self.position


struct SyntaxError(Copyable, Movable):
    var message: String
    var position: String

    fn __copyinit__(out self, read other: SyntaxError):
        self.message = other.message
        self.position = other.position

    fn __moveinit__(out self, deinit other: SyntaxError):
        self.message = other.message
        self.position = other.position

    fn __init__(out self, message: String, position: String):
        self.message = message
        self.position = position

    fn __repr__(mut self) -> String:
        return "SyntaxError: " + self.message + " " + self.position

##################################
### TOKENIZING FOR JARLANG ###
##################################

struct Token(Copyable, Movable):
    var type: String
    var value: String

    fn __copyinit__(out self, read other: Token):
        self.type = other.type
        self.value = other.value

    fn __moveinit__(out self, deinit other: Token):
        self.type = other.type
        self.value = other.value

    fn __init__(out self, type_: String, value: String = ""):
        self.type = type_
        self.value = value

    fn __repr__(mut self) -> String:
        if self.value != "":
            return self.type + ":" + self.value
        return self.type

################################
### POSITION TRACKING FOR JARLANG ###
################################

struct Position(Copyable, Movable):
    var idx: Int
    var line: Int
    var column: Int

    fn __copyinit__(out self, read other: Position):
        self.idx = other.idx
        self.line = other.line
        self.column = other.column

    fn __moveinit__(out self, deinit other: Position):
        self.idx = other.idx
        self.line = other.line
        self.column = other.column

    fn __init__(out self, idx: Int, line: Int, column: Int):
        self.idx = idx
        self.line = line
        self.column = column

    fn advance(mut self, current_char: Optional[String] = None):
        """Advance the position by one character."""
        self.idx += 1
        self.column += 1

        if current_char == "\n":
            self.line += 1
            self.column = 0
    
    fn copy(mut self) -> Position:
        """Create a copy of the current position."""
        return Position(self.idx, self.line, self.column)
    
    fn __str__(self) -> String:
        """String representation of position."""
        return "line " + String(self.line) + ", column " + String(self.column) + " (idx: " + String(self.idx) + ")"

################################
### LEXER FOR JARLANG LANGUAGE ###
################################

struct Lexer:
    var text: String
    var pos: Position
    var curr: String

    fn __init__(out self, text: String):
        self.text = text
        self.pos = Position(-1, 0, -1)
        self.curr = ""
        self.advance()
    
    fn advance(mut self):
        """Advance the 'pos' pointer and set 'curr' character."""
        self.pos.advance()
        if self.pos.idx < len(self.text):
            self.curr = String(self.text[self.pos.idx])  # Convert StringSlice to String
        else:
            self.curr = ""

    fn is_digit(self, c: String) -> Bool:
        """Check if character is a digit."""
        return c in CONSTANTS.DIGITS

    fn generate_tokens(mut self) raises -> (List[Token], Optional[IllegalCharError]):
        """Tokenize the input text into a list of tokens."""
        var tokens = List[Token]()
        while self.curr != "":
            # Skip whitespace
            if self.curr == " " or self.curr == "\t" or self.curr == "\n":
                self.advance()

            # Handle numbers (integers and floats)
            elif self.is_digit(self.curr):
                tokens.append(self.make_number())

            # Handle operators and parentheses
            elif self.curr == "+":
                tokens.append(Token(CONSTANTS.TT_PLUS, self.curr))
                self.advance()
            elif self.curr == "-":
                tokens.append(Token(CONSTANTS.TT_MINUS, self.curr))
                self.advance()
            elif self.curr == "*":
                tokens.append(Token(CONSTANTS.TT_MUL, self.curr))
                self.advance()
            elif self.curr == "/":
                tokens.append(Token(CONSTANTS.TT_DIV, self.curr))
                self.advance()
            elif self.curr == "(":
                tokens.append(Token(CONSTANTS.TT_LPAREN, self.curr))
                self.advance()
            elif self.curr == ")":
                tokens.append(Token(CONSTANTS.TT_RPAREN, self.curr))
                self.advance()
            # Handle exponentiation
            elif self.curr == "^":
                tokens.append(Token(CONSTANTS.TT_POW, self.curr))
                self.advance()

            # Handle equality and relational operators
            elif self.curr == "=":
                tokens.append(Token(CONSTANTS.TT_EQ, self.curr))
                self.advance()
            elif self.curr == "!":
                tokens.append(Token(CONSTANTS.TT_NE, self.curr))
                self.advance()
            elif self.curr == "<":
                tokens.append(Token(CONSTANTS.TT_LT, self.curr))
                self.advance()
            elif self.curr == ">":
                tokens.append(Token(CONSTANTS.TT_GT, self.curr))
                self.advance()

            # Handle mathematical and logical operators
            elif self.curr == ",":
                tokens.append(Token(CONSTANTS.TT_COMMA, self.curr))
                self.advance()
            elif self.curr == ":":
                tokens.append(Token(CONSTANTS.TT_COLON, self.curr))
                self.advance()
            elif self.curr == ";":
                tokens.append(Token(CONSTANTS.TT_SEMI, self.curr))
                self.advance()
            elif self.curr == "{":
                tokens.append(Token(CONSTANTS.TT_LBRACE, self.curr))
                self.advance()
            elif self.curr == "}":
                tokens.append(Token(CONSTANTS.TT_RBRACE, self.curr))
                self.advance()
            
            # Handle single line comments
            elif self.curr == "#":
                self.advance()  # Skip the '#' character
                # Skip the rest of the line as a comment
                while self.curr != "" and self.curr != "\n":
                    self.advance()
                # If we stopped because we hit a newline, advance past it
                if self.curr == "\n":
                    self.advance()

            # TODO`: Handle multi-line comments (e.g., :guard/ {comment_text} /guard:)
        
            elif self.curr == "\"":
                # Handle string literals
                self.advance()  # Skip the opening quote
                var str_val = String("")
                while self.curr != "" and self.curr != "\"":
                    str_val += self.curr
                    self.advance()
                if self.curr == "\"":
                    self.advance()  # Skip the closing quote
                    tokens.append(Token(CONSTANTS.TT_STRING, str_val))
                else:
                    return List[Token](), IllegalCharError("Unterminated string literal at position " + self.pos.__str__() + ": " + str_val, "at position " + self.pos.__str__())

            # Handle illegal characters
            elif self.curr != "":
                char = self.curr
                self.advance()
                return List[Token](), IllegalCharError("Illegal character '" + char + "'" + " at position " + self.pos.__str__(), "at position " + self.pos.__str__())

            else:
                char = self.curr
                self.advance()
                return List[Token](), IllegalCharError("Illegal character '" + char + "'" + self.pos.__str__(), "at position " + self.pos.__str__())


        return tokens.copy(), None

    fn make_number(mut self) -> Token:
        """Create a number token (integer or float)."""
        var num_str = String("")
        var dot_count = 0
        
        # Collect digits and a single dot for floats
        while self.curr != "" and (self.is_digit(self.curr) or self.curr == "."):
            if self.curr == ".":
                if dot_count == 1:
                    break
                dot_count += 1
                num_str += "."
            else:
                num_str += self.curr
            self.advance()
        
        # Return appropriate token type
        if dot_count == 0:
            return Token(CONSTANTS.TT_INT, num_str)
        else:
            return Token(CONSTANTS.TT_FLOAT, num_str)


####################################
### RUNNER FOR JARLANG ###
####################################

fn run_lexer(text: String) raises -> (List[Token], Optional[IllegalCharError]):
    var lexer = Lexer(text)
    var result = lexer.generate_tokens()
    var tokens = result[0].copy()
    var error = result[1]

    return tokens.copy(), error


####################################
### SIMPLE TEST FUNCTION ###
####################################

fn test_lexer(text: String) -> String:
    """Simple test function to verify lexer basics work."""
    var lexer = Lexer(text)
    
    # Test that lexer initializes properly
    var result = "Lexer initialized with text: '" + lexer.text + "'\n"
    result += "Current character: '" + lexer.curr + "'\n"
    result += "Position: " + lexer.pos.__str__() + "\n"
    
    # Test advance functionality
    result += "Advancing through characters of text:\n"

    # Loop through the text length to ensure we cover all characters
    for _ in range(len(text) + 1):
        if lexer.curr != "":
            result += "  Pos " + lexer.pos.__str__() + ": '" + lexer.curr + "'\n"
            lexer.advance()
        else:
            result += "  Reached end of text\n"
            break
    
    return result

