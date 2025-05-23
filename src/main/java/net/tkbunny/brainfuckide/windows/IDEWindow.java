package net.tkbunny.brainfuckide.windows;

import net.tkbunny.brainfuckide.Interpreter;
import net.tkbunny.brainfuckide.InterpreterCallback;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultHighlighter;
import java.awt.*;
import java.nio.charset.StandardCharsets;

public class IDEWindow extends JFrame {
    static Interpreter interpreter;
    static DefaultHighlighter highlighter = new DefaultHighlighter();
    static DefaultHighlighter.DefaultHighlightPainter highlighterPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);

    private GridBagConstraints newConstraints(double weightX, double weightY, int gridX, int gridY, int gridWidth, int gridHeight) {
        GridBagConstraints constraints = new GridBagConstraints();

        constraints.weightx = weightX;
        constraints.weighty = weightY;

        constraints.gridx = gridX;
        constraints.gridy = gridY;
        constraints.gridwidth = gridWidth;
        constraints.gridheight = gridHeight;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.fill = GridBagConstraints.BOTH;

        return constraints;
    }

    public IDEWindow() {
        JPanel panel = new JPanel();

        JTextArea codeArea = new JTextArea();
        codeArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        codeArea.setLineWrap(true);
        codeArea.setWrapStyleWord(false);
        codeArea.setText("Test");
        codeArea.setHighlighter(highlighter);
        JScrollPane codeScrollPane = new JScrollPane(codeArea);
        codeScrollPane.setAlignmentX(0);
        codeScrollPane.setAlignmentY(0);

        codeArea.setText("""
        Hello World with brainfuck

        ++++++++[>+++++++++<-]>. H
        <++++++[>+++++<-]>-. e
        +++++++.. l (x2)
        +++. o
        <+++++++++++++++++[>----<-]>+. (comma)
        <+++[>----<-]>. (space)
        <+++++++++++[>+++++<-]>. W
        <++++[>++++++<-]>. o
        +++. r
        ------. l
        --------. d
        +<+++++++++++++++++[>----<-]>. !
        """);

        interpreter = new Interpreter(30000, codeArea.getText());

        JLabel inputLabel = new JLabel("Input");
        inputLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JTextArea inputArea = new JTextArea();
        JScrollPane inputScrollPane = new JScrollPane(inputArea);
        JLabel outputLabel = new JLabel("Output");
        outputLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JTextArea outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane outputScrollPane = new JScrollPane(outputArea);
        JButton runButton = new JButton("Run");
        JButton stepButton = new JButton("Step");
        JButton resetButton = new JButton("Reset");

        InterpreterCallback callback = new InterpreterCallback() {
            @Override
            public void step(int character) {
                highlighter.removeAllHighlights();
                try {
                    highlighter.addHighlight(character, character + 1, highlighterPainter);
                } catch (Exception ignored) {
                    highlighter.removeAllHighlights();
                }
            }

            @Override
            public Byte input() {
                String text = inputArea.getText();
                inputArea.setText(text.replaceFirst(".", ""));

                byte[] bytes = text.getBytes(StandardCharsets.ISO_8859_1);

                if (bytes.length == 0) {
                    return 0;
                } else {
                    return text.getBytes(StandardCharsets.ISO_8859_1)[0];
                }
            }
        };

        runButton.addActionListener(e -> {
            interpreter = new Interpreter(30000, codeArea.getText());
            String ret = interpreter.run(callback);
            outputArea.setText(ret);
        });
        stepButton.addActionListener(e -> outputArea.setText(outputArea.getText() + interpreter.step(1, callback)));
        resetButton.addActionListener(e -> {
           interpreter = new Interpreter(30000, codeArea.getText());
           outputArea.setText("");
        });

        codeArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                interpreter = new Interpreter(30000, codeArea.getText());
                outputArea.setText("");
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                interpreter = new Interpreter(30000, codeArea.getText());
                outputArea.setText("");
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                interpreter = new Interpreter(30000, codeArea.getText());
                outputArea.setText("");
            }
        });

        JMenuBar menuBar = new JMenuBar();

        panel.setLayout(new GridBagLayout());
        panel.add(codeScrollPane, newConstraints(0.6, 1.0, 0, 0, 1, 6));
        panel.add(inputLabel, newConstraints(0.4, 0.05, 1, 0, 2, 1));
        panel.add(inputScrollPane, newConstraints(0.4, 0.55, 1, 1, 2, 1));
        panel.add(outputLabel, newConstraints(0.4, 0.05, 1, 2, 2, 1));
        panel.add(outputScrollPane, newConstraints(0.4, 0.25, 1, 3, 2, 1));
        panel.add(runButton, newConstraints(0.2, 0.1, 1, 4, 1, 2));
        panel.add(stepButton, newConstraints(0.2, 0.1, 2, 4, 1, 1));
        panel.add(resetButton, newConstraints(0.2, 0.1, 2, 5, 1, 1));

        add(panel);
        setJMenuBar(menuBar);
        setName("Brainfuck IDE");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    public static void main(String[] args) {
        new IDEWindow();
    }
}
