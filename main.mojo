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

fn main():
    print("Welcome to Jarlang!")
    print("Type 'q!' to quit")
    shell_repl()