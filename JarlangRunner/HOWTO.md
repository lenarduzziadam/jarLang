# JarlangRunner — How to Use Jarlang

Welcome, brave JarKnight! This guide will help you wield the power of Jarlang.

## 1. Starting the Jarlang Shell (REPL)

- Run the shell with:
  ```sh
  java -jar jarlang.jar
  ```
- You'll see a medieval welcome and a prompt. Type commands or run files interactively.

## 2. Running a Jarlang File

- From inside the shell, run a `.vase` file:
  ```
  !run tests/loop_tests.vase
  ```
- You can run any file in the `tests/` directory or your own `.vase` scripts.

## 3. Key Jarlang Commands (Medieval Style)

- `wield x 10` — Declare a variable
- `vow y 42` — Declare a constant
- `sacred z "legend"` — Declare a sacred variable
- `forge fn(a, b)` — Define a function
- `chant "Hello!"` — Print a message
- `summon "stdlib.vase"` — Import a library
- `lest x < 5 { ... }` — While loop
- `endure wield i 0; i < 5; i = i + 1 { ... }` — For loop
- `judge ... orjudge ...` — If/else
- `q!` — Quit the shell

## 4. Example

```vase
wield name "JarKnight"
chant "Hello, " + name
lest i < 3 {
  chant i
  i = i + 1
}
```

## 5. Need Help?
- See the main README for more details and examples.
- Explore the `tests/` folder for sample scripts.

HUZZAH! May your code be valiant and your bugs vanquished!
