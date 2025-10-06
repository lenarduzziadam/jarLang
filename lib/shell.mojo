# Import the lexer functionality
from lib.jarlang import *

## Shell for jarlang language here currently has a simple infinite while loop until 
## user prompts exit with 'q!' shell command try block used for error handling
fn shell_repl():
    """Interactive REPL for Jarlang language."""
    while True:
        try:
            text = input('duel -> ')
            if text == "q!":
                break
            elif text == "!test":
                print("Running lexer tests...")
                print(test_lexer("3 + 5"))
                print(test_lexer("12.34 * 56 - 78 / 9"))
                print(test_lexer("(1 + 2) * 3.5 - 4/ (5 + 6)"))

            ## Help command to show available commands
            elif text == "!help":
                print("Jarlang Commands:")
                print("  q! - quit")
                print("  !test - run built-in tests")
                print("  !tokens - toggle token display mode")
                print("  !help - show this help")
                print("  <expr> - parse and show AST of expression")
            
            ## Default case: parse the input and show AST
            else:
                try:
                    # Parse the input and show AST
                    var result = run_parser("<stdin>", text)
                    var ast = result[0]
                    var lex_error = result[1]
                    var parse_error = result[2]

                    # Check for errors first
                    if lex_error:
                        print("JarKnight Ashamed:", lex_error.value().message)
                    elif parse_error:
                        print("JarKnight Confused:", parse_error.value().message)
                    elif ast:
                        print("AST:", ast.value().__repr__())
                        print("JarKnight: Parsed successfully!")
                    else:
                        print("JarKnight: No AST generated")

                    # Also show tokens for debugging
                    var token_result = run_lexer("<stdin>", text)
                    var tokens = token_result[0].copy()
                    var token_error = token_result[1]
                    if not token_error and len(tokens) > 0:
                        print("Found", len(tokens), "tokens")
                        var token_str = "[{"
                        var idx = 0
                        while idx < len(tokens):
                            var token = tokens[idx].copy()
                            if idx > 0:
                                token_str += "}, {"
                            token_str += token.type + ":" + "'" + token.value + "'"
                            idx += 1
                        token_str += "}]"
                        print("Tokens:", token_str)
                        
                except:
                    print("JarKnight Ashamed: Parser crashed")
                
                    
        except:
            print("Error reading input")
            break