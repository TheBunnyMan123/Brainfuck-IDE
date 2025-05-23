package net.tkbunny.brainfuckide;

public interface InterpreterCallback {
    void step(int character, int pointer, byte[] tape);
    Byte input();
}
