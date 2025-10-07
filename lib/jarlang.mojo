
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
    var pos_start: Optional[Position]  # Optional position tracking
    var pos_end: Optional[Position]    # Optional position tracking

    fn __copyinit__(out self, read other: Token):
        self.type = other.type
        self.value = other.value
        self.pos_start = other.pos_start
        self.pos_end = other.pos_end

    fn __moveinit__(out self, deinit other: Token):
        self.type = other.type
        self.value = other.value
        self.pos_start = other.pos_start  # Add this
        self.pos_end = other.pos_end      # Add this

    fn __init__(out self, type_: String, value: String = ""):
        self.type = type_
        self.value = value
        self.pos_start = None
        self.pos_end = None

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
    var filename: String
    var text: String
    var pos: Position
    var curr: String

    fn __init__(out self, filename: String, text: String):
        self.filename = filename
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
                return List[Token](), IllegalCharError("Illegal character '" + char + "'" + " at position " + self.pos.__str__(), "at position " + self.pos.__str__())

        tokens.append(Token(CONSTANTS.TT_EOF))
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

###################################
### AST NODES FOR JARLANG ###
###################################

struct NumberNode(Copyable, Movable):
    var value: String

    fn __copyinit__(out self, read other: NumberNode):
        self.value = other.value

    fn __moveinit__(out self, deinit other: NumberNode):
        self.value = other.value

    fn __init__(out self, token: Token):
        self.value = token.value

    fn __repr__(mut self) -> String:
        return self.value 

struct BinOpNode(Copyable, Movable):
    var left: NumberNode
    var op_token: Token
    var right: NumberNode

    fn __copyinit__(out self, read other: BinOpNode):
        self.left = other.left.copy()
        self.op_token = other.op_token.copy()
        self.right = other.right.copy()

    fn __moveinit__(out self, deinit other: BinOpNode):
        self.left = other.left.copy()
        self.op_token = other.op_token.copy()
        self.right = other.right.copy()

    fn __init__(out self, left: ASTNode, op_token: Token, right: ASTNode):
        self.left = left.copy()
        self.op_token = op_token.copy()
        self.right = right.copy()

    fn __repr__(mut self) -> String:
        return " {" + self.left.__repr__() + "}(" + self.op_token.__repr__() + "){" + self.right.__repr__() + "}"

    


# Simple AST variant type to hold either NumberNode or BinOpNode
struct ASTNode(Copyable, Movable):
    var node_type: String  # "number" or "binop"
    var number_node: Optional[NumberNode]
    var binop_node: Optional[BinOpNode]

    fn __copyinit__(out self, read other: ASTNode):
        self.node_type = other.node_type
        self.number_node = other.number_node
        self.binop_node = other.binop_node

    fn __moveinit__(out self, deinit other: ASTNode):
        self.node_type = other.node_type
        self.number_node = other.number_node
        self.binop_node = other.binop_node

    fn __init__(out self, number: NumberNode):
        self.node_type = "number"
        self.number_node = number.copy()
        self.binop_node = None

    fn __init__(out self, binop: BinOpNode):
        self.node_type = "binop" 
        self.number_node = None
        self.binop_node = binop.copy()

    fn __repr__(mut self) -> String:
        if self.node_type == "number" and self.number_node:
            return self.number_node.value().__repr__()
        elif self.node_type == "binop" and self.binop_node:
            return self.binop_node.value().__repr__()
        else:
            return "ASTNode(invalid)"

    fn evaluate(self) raises -> Float64:
        """Evaluate the AST node and return the numeric result."""
        if self.node_type == "number" and self.number_node:
            # Convert string to Float64
            return atof(self.number_node.value().value)
        elif self.node_type == "binop" and self.binop_node:
            var binop = self.binop_node.value().copy()
            var left_val = atof(binop.left.value())
            var right_val = atof(binop.right.value())

            # Apply the operator
            if binop.op_token.type == CONSTANTS.TT_PLUS:
                return left_val + right_val
            elif binop.op_token.type == CONSTANTS.TT_MINUS:
                return left_val - right_val
            elif binop.op_token.type == CONSTANTS.TT_MUL:
                return left_val * right_val
            elif binop.op_token.type == CONSTANTS.TT_DIV:
                if right_val != 0:
                    return left_val / right_val
                else:
                    # Division by zero - raise an error
                    raise Error("Division by zero")
            else:
                # Unknown operator
                raise Error("Unknown operator: " + binop.op_token.type)
        else:
            # Invalid node
            raise Error("Invalid AST node")

###################################
###### PARSER FOR JARLANG ######
###################################

struct Parser:
    var tokens: List[Token]
    var tok_idx: Int
    var curr_tok: Optional[Token]

    fn __init__(out self, tokens: List[Token]):
        self.tokens = tokens.copy()
        self.tok_idx = -1
        self.curr_tok = None
        _ = self.advance()
    
    fn advance(mut self) -> Optional[Token]:
        """Advance to the next token."""
        self.tok_idx += 1
        if self.tok_idx < len(self.tokens):
            self.curr_tok = self.tokens[self.tok_idx].copy()
        else:
            self.curr_tok = None
        return self.curr_tok

    fn expect_token(mut self, token_type: String) -> (Optional[Token], Optional[SyntaxError]):
        """Expect a specific token type and advance if found."""
        if self.curr_tok:
            var tok = self.curr_tok.value().copy()
            if tok.type == token_type:
                self.advance()
                return tok.copy(), None
            else:
                return None, SyntaxError("Expected '" + token_type + "' but got '" + tok.type + "'", "at token position " + String(self.tok_idx))
        else:
            return None, SyntaxError("Expected '" + token_type + "' but reached end of input", "at token position " + String(self.tok_idx))
    
    fn parse(mut self) -> (Optional[ASTNode], Optional[SyntaxError]):
        """Parse the list of tokens into an AST."""
        if self.curr_tok == None:
            return None, SyntaxError("No tokens to parse", "at token position " + String(self.tok_idx))

        return self.expr()

    fn expr(mut self) -> (Optional[ASTNode], Optional[SyntaxError]):
        """Parse expression: term ((commune|banish) term)*"""
        var result = self.term()
        var left_node = result[0]
        var error = result[1]
        
        if error:
            return None, error.value().copy()
        
        # Handle binary operations (+ and -)
        while self.curr_tok and (self.curr_tok.value().type == CONSTANTS.TT_PLUS or self.curr_tok.value().type == CONSTANTS.TT_MINUS):
            var op_token = self.curr_tok.value().copy()
            _ = self.advance()
            
            var right_result = self.term()
            var right_node = right_result[0]
            var right_error = right_result[1]
            
            if right_error:
                return None, right_error.value().copy()
            
            if left_node and right_node:
                # Extract NumberNodes from ASTNodes (simplified - only handling number leaves for now)
                if left_node.value().node_type == "number" and right_node.value().node_type == "number":
                    var left_num = left_node.value().number_node.value().copy()
                    var right_num = right_node.value().number_node.value().copy()
                    var binop = BinOpNode(left_num, op_token, right_num)
                    left_node = ASTNode(binop)
                else:
                    return None, SyntaxError("Complex expressions not yet supported", "at token position " + String(self.tok_idx))
            
        return left_node, None

    fn term(mut self) -> (Optional[ASTNode], Optional[SyntaxError]):
        """Parse term: factor ((rally|slash) factor)*"""
        var result = self.factor()
        var left_node = result[0]
        var error = result[1]
        
        if error:
            return None, error.value().copy()
        
        # Handle binary operations (* and /)
        while self.curr_tok and (self.curr_tok.value().type == CONSTANTS.TT_MUL or self.curr_tok.value().type == CONSTANTS.TT_DIV):
            var op_token = self.curr_tok.value().copy()
            _ = self.advance()
            
            var right_result = self.factor()
            var right_node = right_result[0]
            var right_error = right_result[1]
            
            if right_error:
                return None, right_error.value().copy()
            
            if left_node and right_node:
                # Extract NumberNodes from ASTNodes (simplified - only handling number leaves for now)
                if left_node.value().node_type == "number" and right_node.value().node_type == "number":
                    var left_num = left_node.value().number_node.value().copy()
                    var right_num = right_node.value().number_node.value().copy()
                    var binop = BinOpNode(left_num, op_token, right_num)
                    left_node = ASTNode(binop)
                else:
                    return None, SyntaxError("Complex expressions not yet supported", "at token position " + String(self.tok_idx))
            
        return left_node, None

    fn factor(mut self) -> (Optional[ASTNode], Optional[SyntaxError]):
        """Parse factor: int|float|(expr)"""
    
        var tok = self.curr_tok
        
        if tok and (tok.value().type == CONSTANTS.TT_INT or tok.value().type == CONSTANTS.TT_FLOAT):
            _ = self.advance()
            var number_node = NumberNode(tok.value())
            return ASTNode(number_node), None
        elif tok and tok.value().type == CONSTANTS.TT_LPAREN:
            _ = self.advance()
            var result = self.expr()
            var node = result[0]
            var error = result[1]
            
            if error:
                return None, error.value().copy()
            
            if self.curr_tok and self.curr_tok.value().type == CONSTANTS.TT_RPAREN:
                _ = self.advance()
                return node, None
            else:
                return None, SyntaxError("Expected ')')", "at token position " + String(self.tok_idx))
        else:
            return None, SyntaxError("Expected int, float or '('", "at token position " + String(self.tok_idx))
    
struct ParserResult:
    var node: Optional[ASTNode]
    var error: Optional[SyntaxError]

    fn __init__(out self, node: Optional[ASTNode] = None, error: Optional[SyntaxError] = None):
        self.node = node
        self.error = error

    fn register(mut self, res: ParserResult) -> (Optional[ASTNode], Optional[SyntaxError]):
        if res.error:
            self.error = res.error.value().copy()
        return res.node.copy(), res.error
    
    fn success(mut self, node: ASTNode) -> ParserResult:
        self.node = node.copy()
        return self
    
    fn failure(mut self, error: SyntaxError) -> ParserResult:
        self.error = error.copy()
        return self

    fn __repr__(out self) -> String:
        if self.error:
            return "ParserResult(error: " + self.error.value().__repr__() + ")"
        elif self.node:
            return "ParserResult(node: " + self.node.value().__repr__() + ")"
        else:
            return "ParserResult(empty)"
    

####################################
### RUNNER FOR JARLANG ###
####################################

fn run_lexer(filename: String, text: String) raises -> (List[Token], Optional[IllegalCharError]):
    var lexer = Lexer(filename, text)
    var result = lexer.generate_tokens()
    var tokens = result[0].copy()
    var error = result[1]

    if error:
        return List[Token](), error
    
    return tokens.copy(), error

################################
### RUNNER FOR PARSER ###
################################

fn run_parser(filename: String, text: String) raises -> (Optional[ASTNode], Optional[IllegalCharError], Optional[SyntaxError]):
    """Parse text and return the AST along with any errors."""
    var lexer = Lexer(filename, text)
    var lex_result = lexer.generate_tokens()
    var tokens = lex_result[0].copy()
    var lex_error = lex_result[1]

    if lex_error:
        return None, lex_error, None
    
    # Generate AST and check for parser errors
    var parser = Parser(tokens)
    var parse_result = parser.parse()
    var ast = parse_result[0]
    var parse_error = parse_result[1]

    
    
    return ast, None, parse_error


####################################
### SIMPLE TEST FUNCTION ###
####################################

fn test_lexer(text: String) -> String:
    """Simple test function to verify lexer basics work."""
    var lexer = Lexer("<stdin>", text)

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

