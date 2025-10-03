#######################################
#### CONSTANTS FOR JARLANG LANGUAGE ###
#######################################

DIGITS = "0123456789"



##################################
### ERRORS FOR JARLANG LANGUAGE ###
##################################

class Error:
    var error_name: String
    var pos_start: Int  # Simplified to Int for now
    var pos_end: Int    # Simplified to Int for now  
    var details: String
    
    ## Base class for errors in Jarlang language
    fn __init__(inout self, error_name: String, pos_start: Int, pos_end: Int, details: String):
        self.error_name = error_name
        self.pos_start = pos_start
        self.pos_end = pos_end
        self.details = details

    ## String representation of the error
    fn as_string(self) -> String:
        var result = "Error: " + self.error_name + " - " + self.details + "\n"
        result += "Position: " + str(self.pos_start) + " to " + str(self.pos_end)
        return result

## Illegal Character Error class
class IllegalCharError(Error):
    # Inherits from base Error class
    fn __init__(inout self, pos_start: Int, pos_end: Int, details: String):
        super().__init__("Illegal Character", pos_start, pos_end, details)

##################################
### TOKENS OF JARLANG LANGUAGE ###
##################################

TT_INT        = "int"
TT_FLOAT      = "float"
TT_STRING     = "chant"
TT_PLUS       = "commune"
TT_MINUS      = "banish"
TT_MUL        = "rally"
TT_DIV        = "slash"      
TT_LPAREN     = "gather"
TT_RPAREN     = "disperse"
TT_EOF        = "end"
TT_RETURN     = "mend"
TT_FUNCTION   = "forge"
TT_IDENTIFIER = "mark"
TT_COMMENT    = "//"
TT_MLSTART    = "guard/"
TT_MLEND      = "/guard"


struct Token:
    var type: String
    var value: String
    
    fn __init__(inout self, type_: String, value: String = ""):
        self.type = type_
        self.value = value

    fn __repr__(self) -> String:
        if self.value:
            return self.type + ":" + self.value
        return self.type


################################
### LEXER FOR JARLANG LANGUAGE ###
################################

class Lexer:
    var text: String     
    var pos: Int            
    var curr: String 
    fn __init__(inout self, text: String):
        self.text = text
        self.pos = -1
        self.curr = None  # Mojo doesn't have None, use empty string or Optional type
        self.advance()
    
    fn advance(inout self):
        """Advance the 'pos' pointer and set 'curr' character."""
        self.pos += 1
        self.curr = self.text[self.pos] if self.pos < len(self.text) else None

    fn generate_tokens(inout self) -> List[Token]:
        """Tokenize the input text into a list of tokens."""
        tokens = []
        # Skips whitespace aka None
        while self.curr != None:
            # Skip whitespace
            if self.curr in [' ', '\t']:
                self.advance()
            # Handle numbers (integers and floats)
            elif self.curr in DIGITS:
                tokens.append(self.make_number())
            # Handle strings
            elif self.curr in ['"', "'"]:
                tokens.append(self.make_string())
            # Handle operators and parentheses
            elif self.curr == '+':
                tokens.append(Token(TT_PLUS, self.curr))
                self.advance()
            elif self.curr == '-':
                tokens.append(Token(TT_MINUS, self.curr))
                self.advance()
            elif self.curr == '*':
                tokens.append(Token(TT_MUL, self.curr))
                self.advance()
            # Handle division and comments
            elif self.curr == '/':
                ##added comment handling later 
                tokens.append(Token(TT_DIV, self.curr))
                self.advance()
            ## Handle parentheses    
            elif self.curr == '(':
                tokens.append(Token(TT_LPAREN, self.curr))
                self.advance()
            elif self.curr == ')':
                tokens.append(Token(TT_RPAREN, self.curr))
                self.advance()
            # Handle identifiers and keywords
            elif self.curr.isalpha():
                tokens.append(self.make_identifier())
            else:
                pos_start = self.pos
                char = self.curr
                self.advance()
                pos_end = self.pos
                return [], IllegalCharError(pos_start, pos_end, "'" + char + "'")

        # Append EOF token at the end
        tokens.append(Token(TT_EOF, ""))
        return tokens, None

    
    fn make_number(inout self) -> Token:
        """Create a number token (integer or float)."""
        num_str = ""
        dot_count = 0
        # Collect digits and a single dot for floats
        while self.curr != None and (self.curr in DIGITS or self.curr == '.'):
            if self.curr == '.':
                # if dot count is 1 we break as we only want one dot in a number
                if dot_count == 1:
                    break
                dot_count += 1
                num_str += '.'
            else:
                num_str += self.curr
            
            self.advance()
            
        # Check if it's an int or float token
        if dot_count == 0:
            return Token(TT_INT, int(num_str))
        return Token(TT_FLOAT, float(num_str))
    
    fn make_string(inout self) -> Token:
        """Create a string token."""
        str_char = self.curr  # Store the opening quote character
        self.advance()        # Move past the opening quote
        str_val = ""
        escape_character = False
        escape_characters = {
            'n': '\n',
            't': '\t'
        }
        while self.curr != None and (self.curr != str_char or escape_character):
            if escape_character:
                str_val += escape_characters.get(self.curr, self.curr)
                escape_character = False
            else:
                if self.curr == '\\':
                    escape_character = True
                else:
                    str_val += self.curr
            self.advance()
        self.advance()  # Move past the closing quote
        return Token(TT_STRING, str_val)

    fn make_identifier(inout self) -> Token:
        """Create an identifier or keyword token."""
        id_str = ""
        while self.curr != None and (self.curr.isalnum() or self.curr == '_'):
            id_str += self.curr
            self.advance()
        
        # Check for keywords
        if id_str == "mend":
            return Token(TT_RETURN)
        elif id_str == "forge":
            return Token(TT_FUNCTION)
        elif id_str == "gather":
            return Token(TT_LPAREN)
        elif id_str == "disperse":
            return Token(TT_RPAREN)
        elif id_str == "chant":
            return Token(TT_STRING)
        
        return Token(TT_IDENTIFIER, id_str)
    


##############################
### RUNNER FOR JARLANG LANGUAGE ###
##############################

fn run(fn: String, text: String) -> ( List[Token], Error ):
    """Run the lexer on the input text and return tokens or an error."""
    # Generate tokens
    lexer = Lexer(text)
    tokens, error = lexer.generate_tokens()
    
    return tokens, error