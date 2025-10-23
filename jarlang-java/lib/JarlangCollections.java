package lib;

import java.util.*;

/**
 * JarlangCollections
 * ------------------
 * A small, self-contained collection of builtins for the Jarlang runtime.
 *
 * Purpose:
 * - Provide an easy-to-use array type (`JarlangArray`) backed by Java's
 *   List<Object> so Jarlang programs can store heterogeneous values.
 * - Expose a compact set of builtin functions (registered into a
 *   `Context`) that operate on arrays and produce values the interpreter
 *   can store or print.
 *
 * Integration points:
 * - The builtins implement `BuiltinFunction` and are stored in the
 *   `Context` with `ctx.setVariable("array_new", (Object)ARRAY_NEW)`.
 * - `FunctionCallNode` should detect `obj instanceof BuiltinFunction`
 *   and call `call(evaluatedArgs, context)` to execute them.
 * - Builtins return `Object` (Double for numbers, String for strings,
 *   `JarlangArray` for arrays). The interpreter may choose how to handle
 *   non-numeric results (store them in variables, return them to caller
 *   after a refactor to a typed Result, or place in a temp `_last` var).
 */
public class JarlangCollections {

    /**
     * JarlangArray
     * -------------
     * Lightweight wrapper around a Java List<Object> to represent arrays
     * inside the runtime. Elements are stored as plain Java objects and may
     * be Double (numbers), String, nested JarlangArray, or any other
     * runtime object the interpreter stores in `Context`.
     *
     * Behavior notes:
     * - Indexing uses 0-based ints. The builtins expose boundary checks and
     *   throw `InterpreterError` on invalid usage.
     * - `toString()` is implemented so `chant` or `array_to_string` prints
     *   a readable representation like `[1, 2, "x"]`.
     */
    public static class JarlangArray {
        private final List<Object> items = new ArrayList<>();

        public JarlangArray() {}
        public JarlangArray(List<Object> initial) { items.addAll(initial); }

        public int len() { return items.size(); }
        public Object get(int idx) { return items.get(idx); }
        public void set(int idx, Object val) { items.set(idx, val); }
        public void push(Object val) { items.add(val); }
        public Object pop() { return items.remove(items.size()-1); }
        public List<Object> asList() { return Collections.unmodifiableList(items); }
        @Override
        public String toString() { return items.toString(); }
    }

    /**
     * BuiltinFunction
     * ---------------
     * Simple functional interface for builtins. Implementations receive a
     * list of already-evaluated Java objects and the current `Context` and
     * return an `Object` which can represent a Double, String, JarlangArray,
     * or any other runtime object.
     *
     * Note: InterpreterError is used to signal runtime problems (wrong arity,
     * type mismatch, out-of-bounds, etc.). Call sites (FunctionCallNode)
     * should translate the returned Object into the interpreter's expected
     * return convention (e.g. a Double for numeric return values).
     */
    public interface BuiltinFunction {
        // args: list of evaluated Objects (Double or String or JarlangArray etc.)
        // context: current Context for lookups if they need it; return Object
        // (Double or String or JarlangArray)
        Object call(List<Object> args, Context context) throws InterpreterError;
    }

    // ------------------------------------------------------------------
    // Builtin implementations
    // ------------------------------------------------------------------
    // Each builtin performs argument validation and returns either a
    // Double (wrapped as a java.lang.Double), a String, or a JarlangArray.
    // If an error occurs the builtin throws InterpreterError with a clear
    // message which will be shown to the user.
    // ------------------------------------------------------------------

    /**
     * array_new(...elements)
     * Create a new array and optionally populate it with provided elements.
     * Example: array_new(1, 2, "x")
     */
    public static final BuiltinFunction ARRAY_NEW = (args, ctx) -> {
        JarlangArray arr = new JarlangArray();
        // Optionally accept initial elements: array_new(1,2,"x")
        for (Object a : args) arr.push(a);
        return arr;
    };

    /**
     * array_len(array)
     * Returns the length of the array as a Double.
     */
    public static final BuiltinFunction ARRAY_LEN = (args, ctx) -> {
        if (args.size() != 1) throw new InterpreterError("array_len expects 1 argument");
        Object a = args.get(0);
        if (!(a instanceof JarlangArray)) throw new InterpreterError("array_len expects an array");
        return (double)((JarlangArray)a).len();
    };

    /**
     * array_get(array, index)
     * Return the element at `index`. Index must be a number (Double).
     * Returns the raw stored object (Double, String, JarlangArray, ...).
     */
    public static final BuiltinFunction ARRAY_GET = (args, ctx) -> {
        if (args.size() != 2) throw new InterpreterError("array_get expects 2 arguments");
        Object arr = args.get(0);
        Object idx = args.get(1);
        if (!(arr instanceof JarlangArray)) throw new InterpreterError("array_get first arg must be array");
        if (!(idx instanceof Double)) throw new InterpreterError("array_get index must be number");
        int i = (int)((Double)idx).doubleValue();
        JarlangArray a = (JarlangArray)arr;
        if (i < 0 || i >= a.len()) throw new InterpreterError("array_get index out of bounds");
        return a.get(i);
    };

    /**
     * array_set(array, index, value)
     * Set the value at `index`. Returns 0.0 (numeric zero) for compatibility.
     */
    public static final BuiltinFunction ARRAY_SET = (args, ctx) -> {
        if (args.size() != 3) throw new InterpreterError("array_set expects 3 arguments");
        Object arr = args.get(0);
        Object idx = args.get(1);
        Object val = args.get(2);
        if (!(arr instanceof JarlangArray)) throw new InterpreterError("array_set first arg must be array");
        if (!(idx instanceof Double)) throw new InterpreterError("array_set index must be number");
        int i = (int)((Double)idx).doubleValue();
        JarlangArray a = (JarlangArray)arr;
        if (i < 0 || i >= a.len()) throw new InterpreterError("array_set index out of bounds");
        a.set(i, val);
        return 0.0;
    };

    /**
     * array_push(array, value...)
     * Push one or more values to the end of the array and return new length.
     */
    public static final BuiltinFunction ARRAY_PUSH = (args, ctx) -> {
        if (args.size() < 2) throw new InterpreterError("array_push expects at least 2 args (array, value...)");
        Object arr = args.get(0);
        if (!(arr instanceof JarlangArray)) throw new InterpreterError("array_push first arg must be array");
        JarlangArray a = (JarlangArray)arr;
        for (int i = 1; i < args.size(); i++) a.push(args.get(i));
        return (double)a.len();
    };

    /**
     * array_pop(array)
     * Remove and return the last element of the array.
     */
    public static final BuiltinFunction ARRAY_POP = (args, ctx) -> {
        if (args.size() != 1) throw new InterpreterError("array_pop expects 1 argument (array)");
        Object arr = args.get(0);
        if (!(arr instanceof JarlangArray)) throw new InterpreterError("array_pop first arg must be array");
        JarlangArray a = (JarlangArray)arr;
        if (a.len() == 0) throw new InterpreterError("array_pop from empty array");
        return a.pop();
    };

    /**
     * array_to_string(array)
     * Return a readable string representation of the array.
     */
    public static final BuiltinFunction ARRAY_TO_STRING = (args, ctx) -> {
        if (args.size() != 1) throw new InterpreterError("array_to_string expects 1 argument");
        Object a = args.get(0);
        if (!(a instanceof JarlangArray)) throw new InterpreterError("array_to_string expects an array");
        return ((JarlangArray)a).toString();
    };

    /**
     * registerAllBuiltins(Context ctx)
     * -------------------------------
     * Convenient helper to populate a Context with the collection builtins.
     * Call this from your runner when creating the global context so
     * programs can call `array_new`, `array_get`, etc.
     *
     * Example:
     *   Context global = new Context("global");
     *   JarlangCollections.registerAllBuiltins(global);
     */
    public static void registerAllBuiltins(Context ctx) {
        ctx.setVariable("array_new", (Object)ARRAY_NEW);
        ctx.setVariable("array_len", (Object)ARRAY_LEN);
        ctx.setVariable("array_get", (Object)ARRAY_GET);
        ctx.setVariable("array_set", (Object)ARRAY_SET);
        ctx.setVariable("array_push", (Object)ARRAY_PUSH);
        ctx.setVariable("array_pop", (Object)ARRAY_POP);
        ctx.setVariable("array_to_string", (Object)ARRAY_TO_STRING);
    }

}