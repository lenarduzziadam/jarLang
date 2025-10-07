from lib.jarlang import Lexer, Token

fn test_operators():
    """Test basic operator tokenization."""
    print("Testing operators...")
    var lexer = Lexer("+ - * /")
    
    try:
        var result = lexer.generate_tokens()
        var tokens = result[0].copy()  # Get the List[Token] from the tuple
        print("Success! Found", len(tokens), "tokens")
        
        # Manual iteration since Mojo Lists don't have __iter__
        var token_count = len(tokens)
        var idx = 0
        while idx < token_count:
            var token = tokens[idx].copy()
            print("  Token", idx+1, "- Type:", token.type, "Value:", token.value)
            idx += 1
    except e:
        print("Error:", String(e))

fn test_numbers():
    """Test number tokenization."""
    print("Testing numbers...")
    var lexer = Lexer("123 45.6")
    
    try:
        var result = lexer.generate_tokens()
        var tokens = result[0].copy()  # Get the List[Token] from the tuple
        print("Success! Found", len(tokens), "tokens")
        
        # Manual iteration since Mojo Lists don't have __iter__
        var token_count = len(tokens)
        var idx = 0
        while idx < token_count:
            var token = tokens[idx].copy()
            print("  Token", idx+1, "- Type:", token.type, "Value:", token.value)
            idx += 1
    except e:
        print("Error:", String(e))

fn test_illegal_char():
    """Test illegal character handling."""
    print("Testing illegal character...")
    var lexer = Lexer("123 @")
    
    try:
        var result = lexer.generate_tokens()
        var tokens = result[0].copy()  # Get the List[Token] from the tuple
        print("Unexpected success")
    except e:
        print("Caught error correctly:", String(e))

fn main():
    """Run simple tests."""
    print("=== Jarlang Tests ===")
    test_operators()
    test_numbers() 
    test_illegal_char()
    print("=== Tests Complete ===")