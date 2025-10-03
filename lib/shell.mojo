
## Shell for jarlang language here currently has a simple infinite while loop until 
## user prompts exit with 'q!' shell command try block used for error handling

fn shell_repl():
    """Interactive REPL for Jarlang language."""
    while True:
        try:
            text = input('duel -> ')
            if text == "q!":
                break
            # TODO: Parse and execute Jarlang code here
            print("Echo:", text)
        except:
            print("Error reading input")
            break