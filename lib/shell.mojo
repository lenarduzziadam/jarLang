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
                print("  !help - show this help")
                print("  <expr> - directly tokenize expression")
            
            ## Default case: just echo input for now
            else:
                # Test the lexer with the input
                var lexer = Lexer(text)
                var result = lexer.generate_tokens()
                var tokens = result[0].copy()
                var error = result[1]

                # Check for errors first
                if error:
                    print("JarKnight Ashamed:", error.value().message)
                else:
                    if len(tokens) == 0:
                        print("No tokens found")
                    else:
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

                        # Print the original input for now as a placeholder
                        print("JarKnight:", text)
                
                    
        except:
            print("Error reading input")
            break