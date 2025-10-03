# Import the lexer functionality
from .jarlang import run

## Shell for jarlang language here currently has a simple infinite while loop until 
## user prompts exit with 'q!' shell command try block used for error handling
fn shell_repl():
    """Interactive REPL for Jarlang language."""
    while True:
        try:
            text = input('duel -> ')
            if text == "q!":
                break
            
            # Run the lexer on the input
            tokens, error = run('<stdin>', text)
            
            if error:
                print("Battle Error:", error.as_string())
            else:
                print("JarKnight tokens:")
                for token in tokens:
                    print("  ", token.__repr__())
                    
        except Exception as e:
            print("Error reading input:", e)
            break