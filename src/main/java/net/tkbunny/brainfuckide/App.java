package net.tkbunny.brainfuckide;

import net.tkbunny.brainfuckide.windows.IDEWindow;

public class App {
    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        new IDEWindow();
    }
}
