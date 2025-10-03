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


@value
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

struct Lexer:
    var text: String     
    var pos: Int            
    var curr: String 
    
    fn __init__(inout self, text: String):
        self.text = text
        self.pos = -1
        self.curr = ""
        self.advance()
    
    fn advance(inout self):
        """Advance the 'pos' pointer and set 'curr' character."""
        self.pos += 1
        if self.pos < len(self.text):
            self.curr = self.text[self.pos]
        else:
            self.curr = ""

    fn generate_tokens(inout self) -> List[Token]:
        """Tokenize the input text into a list of tokens."""
        var tokens = List[Token]()
        
        # Simple approach - let's just store them as we go and return them
        while self.curr != "":
            # Skip whitespace
            if self.curr == ' ' or self.curr == '\t':
                self.advance()
            # Handle numbers (integers and floats)
            elif self.curr in DIGITS:
                var token = self.make_number()
                tokens.append(token)
            # Handle operators and parentheses
            elif self.curr == '+':
                var token = Token(TT_PLUS, self.curr)
                tokens.append(token)
                self.advance()
            elif self.curr == '-':
                var token = Token(TT_MINUS, self.curr)
                tokens.append(token)
                self.advance()
            elif self.curr == '*':
                var token = Token(TT_MUL, self.curr)
                tokens.append(token)
                self.advance()
            elif self.curr == '/':
                var token = Token(TT_DIV, self.curr)
                tokens.append(token)
                self.advance()
            elif self.curr == '(':
                var token = Token(TT_LPAREN, self.curr)
                tokens.append(token)
                self.advance()
            elif self.curr == ')':
                var token = Token(TT_RPAREN, self.curr)
                tokens.append(token)
                self.advance()
            else:
                # Skip unknown characters for now
                self.advance()

        # Append EOF token at the end
        var eof_token = Token(TT_EOF, "")
        tokens.append(eof_token)
        return tokens

    
    fn make_number(inout self) -> Token:
        """Create a number token (integer or float)."""
        var num_str = ""
        var dot_count = 0
        # Collect digits and a single dot for floats
        while self.curr != "" and (self.curr in DIGITS or self.curr == '.'):
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
            return Token(TT_INT, num_str)
        return Token(TT_FLOAT, num_str)
    
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

fn run(fn_name: String, text: String) -> List[Token]:
    """Run the lexer on the input text and return tokens."""
    var lexer = Lexer(text)
    var tokens = lexer.generate_tokens()
    
    return tokens