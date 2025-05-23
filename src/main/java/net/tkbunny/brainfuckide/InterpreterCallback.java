package net.tkbunny.brainfuckide;

public interface InterpreterCallback {
    void step(int character);
    Byte input();
}
