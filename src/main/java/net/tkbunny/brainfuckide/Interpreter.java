package net.tkbunny.brainfuckide;

import java.util.Stack;

public class Interpreter {
    public final int cells;
    private final String code;
    private int character;
    private final Stack<Integer> loops;

    private int pointer;
    private final byte[] tape;

    public Interpreter(int cells, String code) {
        this.cells = cells;
        this.code = code;
        this.character = 0;
        this.loops = new Stack<>();

        this.pointer = 0;
        this.tape = new byte[cells];
    }

    public String step(int count, InterpreterCallback interpreterCallback) {
        StringBuilder ret = new StringBuilder();

        int loopsRan = 0;
        while (count > 0 && character < code.length()) {
            interpreterCallback.step(character);

            switch(((Character) code.charAt(character)).toString()) {
                case ">" -> pointer++;
                case "<" -> pointer--;
                case "+" -> tape[pointer]++;
                case "-" -> tape[pointer]--;
                case "." -> ret.append((char) tape[pointer]);
                case "," -> tape[pointer] = interpreterCallback.input();
                case "[" -> {
                    if (tape[pointer] == 0) {
                        int loopCount = 1;
                        while (loopCount > 0) {
                            character++;
                            if (((Character) code.charAt(character)).toString().equals("[")) {
                                loopCount++;
                            } else if (((Character) code.charAt(character)).toString().equals("]")) {
                                loopCount--;
                            }
                        }
                    } else {
                        if (loopsRan++ > 45000) {
                            character = Integer.MAX_VALUE;
                            return "ABORTED: INFINITE LOOP DETECTED";
                        }

                        this.loops.push(character);
                    }
                }
                case "]" -> {
                    if (tape[pointer] != 0) {
                        if (!this.loops.isEmpty()) {
                            character = this.loops.pop() - 1;
                        }
                    } else if (!this.loops.isEmpty()) {
                        this.loops.pop();
                    }
                }
            }

            pointer = (pointer % cells + cells) % cells;
            count--;
            character++;
        }

        return ret.toString();
    }

    public String run(InterpreterCallback interpreterCallback) {
        StringBuilder builder = new StringBuilder();

        while (character < code.length()) {
            builder.append(step(Integer.MAX_VALUE, interpreterCallback));
        }

        return builder.toString();
    }
}
