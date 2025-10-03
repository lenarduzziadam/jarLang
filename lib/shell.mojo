# Import the lexer functionality
from lib.jarlang import test_lexer

## Shell for jarlang language here currently has a simple infinite while loop until 
## user prompts exit with 'q!' shell command try block used for error handling
fn shell_repl():
    """Interactive REPL for Jarlang language."""
    while True:
        try:
            text = input('duel -> ')
            if text == "q!":
                break
            elif text == "test!":
                print("Running lexer tests...")
                print(test_lexer("3 + 5"))
                print(test_lexer("12.34 * 56 - 78 / 9"))
                print(test_lexer("(1 + 2) * 3.5 - 4/ (5 + 6)"))
            else:
                # Test the lexer with the input
                var result = test_lexer(text)
                print("JarKnight sparring:")
                print(result)
                    
        except:
            print("Error reading input")
            break