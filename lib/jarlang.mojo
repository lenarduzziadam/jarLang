import .constants

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
TT_MLSTART    = ":guard/"
TT_MLEND      = "/guard:"


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

##################################
### ERROR HANDLING FOR JARLANG ###
##################################

@value
struct LexError:
    var message: String
    var position: Int
    
    fn __init__(inout self, message: String, position: Int):
        self.message = message
        self.position = position
    
    fn __repr__(self) -> String:
        return "Battle Error at position " + str(self.position) + ": " + self.message


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
                tokens.append(Token(TT_DIV, self.curr))
                self.advance()
            ## Handle parentheses    
            elif self.curr == '(':
                tokens.append(Token(TT_LPAREN, self.curr))
                self.advance()
            elif self.curr == ')':
                tokens.append(Token(TT_RPAREN, self.curr))
                self.advance()
            else:
                raise Exception(f"Illegal character '{self.curr}'")

        # Append EOF token at the end
        tokens.append(Token(TT_EOF, None))
        return tokens

    
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
    



fn run(filename: String, text: String) -> String:
    """Run the lexer on the input text and return a formatted result."""
    var lexer = Lexer(text)
    var result = lexer.generate_tokens()
    var tokens = result[0]
    var error = result[1]

    ## Check for errors
    if error is not None:
        return "ERROR: " + error.__repr__()
    
    ## Format tokens for output
    else:
        var output = "TOKENS: "
        for i in range(len(tokens)):
            output += tokens[i].__repr__()
            if i < len(tokens) - 1:
                output += ", "
        return output
    

