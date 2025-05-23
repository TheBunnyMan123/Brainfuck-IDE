package net.tkbunny.brainfuckide.windows;

import net.tkbunny.brainfuckide.App;
import net.tkbunny.brainfuckide.Interpreter;
import net.tkbunny.brainfuckide.InterpreterCallback;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;

public class IDEWindow extends JFrame {
    final int cells = 30000;
    final JTextPane codeArea;
    Interpreter interpreter;
    final DefaultHighlighter highlighter = new DefaultHighlighter();
    final DefaultHighlighter.DefaultHighlightPainter highlighterPainter = new DefaultHighlighter.DefaultHighlightPainter(CURRENT);

    static final Color DARK_BACKGROUND = new Color(38, 38, 38);
    static final Color LIGHT_BACKGROUND = new Color(45, 45, 45);
    static final Color TEXT = new Color(150, 150, 150);
    static final Color LIGHT_TEXT = new Color(170, 170, 170);
    static final Color CURRENT = new Color(255, 200, 80);
    static final MatteBorder THICK_BORDER = BorderFactory.createMatteBorder(5, 5, 5, 5, DARK_BACKGROUND);
    static final Font MONOSPACE_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);
    static final Font SANS_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 14);

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

    private JButton createButton(String name, ActionListener listener) {
        final Color HOVER_BACKGROUND = LIGHT_BACKGROUND.brighter();
        JButton button = new JButton(name);
        button.setBackground(LIGHT_BACKGROUND);
        button.setForeground(TEXT);
        button.setBorder(THICK_BORDER);
        button.addActionListener(listener);
        button.setFocusable(false);
        button.setOpaque(true);
        button.setFont(SANS_FONT);

        button.addChangeListener(e -> {
            if (button.getModel().isPressed()) {
                button.setBackground(TEXT);
                button.setForeground(LIGHT_BACKGROUND);
            } else if (button.getModel().isRollover()) {
                button.setBackground(HOVER_BACKGROUND);
                button.setForeground(TEXT);
            } else {
                button.setBackground(LIGHT_BACKGROUND);
                button.setForeground(TEXT);
            }
        });

        return button;
    }

    private JTextPane createCodeArea(JTextArea outputArea) {
        JTextPane codeArea = new JTextPane();
        codeArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        codeArea.setHighlighter(this.highlighter);
        codeArea.setFont(MONOSPACE_FONT);

        DefaultStyledDocument styledDocument = new DefaultStyledDocument();
        SimpleAttributeSet commentStyle = new SimpleAttributeSet();
        SimpleAttributeSet plusMinusStyle = new SimpleAttributeSet();
        SimpleAttributeSet pointerStyle = new SimpleAttributeSet();
        SimpleAttributeSet loopStyle = new SimpleAttributeSet();
        SimpleAttributeSet ioStyle = new SimpleAttributeSet();

        StyleConstants.setForeground(commentStyle, TEXT);
        StyleConstants.setForeground(plusMinusStyle, new Color(100, 150, 200));
        StyleConstants.setForeground(pointerStyle, new Color(200, 150, 200));
        StyleConstants.setForeground(loopStyle, new Color(245, 142, 85));
        StyleConstants.setForeground(ioStyle, new Color(150, 200, 150));

        codeArea.addStyle("", null);
        codeArea.setStyledDocument(styledDocument);
        codeArea.setOpaque(true);
        codeArea.setBorder(null);
        codeArea.setCaretColor(TEXT);
        codeArea.setBackground(LIGHT_BACKGROUND);

        styledDocument.addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(() -> updateStyledDocument(outputArea, codeArea, plusMinusStyle, ioStyle, pointerStyle, loopStyle, commentStyle, styledDocument));
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(() -> updateStyledDocument(outputArea, codeArea, plusMinusStyle, ioStyle, pointerStyle, loopStyle, commentStyle, styledDocument));
            }

            @Override
            public void changedUpdate(DocumentEvent e) {}
        });

        return codeArea;
    }

    private JTextArea createTextArea(boolean editable) {
        JTextArea area = new JTextArea();
        area.setBackground(LIGHT_BACKGROUND);
        area.setForeground(TEXT);
        area.setCaretColor(TEXT);
        area.setEditable(editable);
        area.setFont(MONOSPACE_FONT);

        return area;
    }

    private JLabel createLabel(String text, Color background) {
        JLabel label = new JLabel(text);
        label.setForeground(LIGHT_TEXT);
        label.setBackground(background);
        label.setOpaque(true);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBorder(null);
        label.setFont(SANS_FONT);

        return label;
    }

    private JLabel createLabel(String text) {
        return createLabel(text, DARK_BACKGROUND);
    }

    public IDEWindow() {
        JPanel panel = new JPanel();
        panel.setBackground(DARK_BACKGROUND);

        JLabel inputLabel = createLabel("Input");
        JTextArea inputArea = createTextArea(true);
        JScrollPane inputScrollPane = new JScrollPane(inputArea);
        inputScrollPane.setBorder(THICK_BORDER);
        JLabel outputLabel = createLabel("Output");
        JTextArea outputArea = createTextArea(false);
        JScrollPane outputScrollPane = new JScrollPane(outputArea);
        outputScrollPane.setBorder(THICK_BORDER);

        JFormattedTextField stepCount = new JFormattedTextField(NumberFormat.getIntegerInstance());
        stepCount.setBackground(LIGHT_BACKGROUND);
        stepCount.setForeground(TEXT);
        stepCount.setCaretColor(TEXT);
        stepCount.setHorizontalAlignment(SwingConstants.CENTER);
        stepCount.setText("1");
        stepCount.setBorder(THICK_BORDER);

        this.codeArea = createCodeArea(outputArea);
        JScrollPane codeScrollPane = new JScrollPane(this.codeArea);
        codeScrollPane.setAlignmentX(0);
        codeScrollPane.setAlignmentY(0);
        codeScrollPane.setBorder(THICK_BORDER);

        interpreter = new Interpreter(cells, this.codeArea.getText());

        JPanel tapePanel = new JPanel();
        tapePanel.setLayout(new GridBagLayout());
        tapePanel.setBackground(DARK_BACKGROUND);
        tapePanel.setBorder(null);
        JLabel[] tapePanelLabels = new JLabel[13];

        String tapeDisplayFormat = "<html><body style='text-align: center;'>%d<br>%0" + String.valueOf(cells).length() + "d</body></html>";
        for (int i = 0; i < tapePanelLabels.length; i++) {
            tapePanelLabels[i] = createLabel(String.format(tapeDisplayFormat, 0, ((((-tapePanelLabels.length/2 + i)) % cells + cells) % cells)), LIGHT_BACKGROUND);
            tapePanelLabels[i].setBorder(new LineBorder(new Color(200, 200, 200)));
            tapePanelLabels[i].setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
            tapePanelLabels[i].setBorder(THICK_BORDER);
            if (i == 13 / 2) {
                tapePanelLabels[i].setForeground(CURRENT);
            } else {
                tapePanelLabels[i].setForeground(new Color(245, 142, 85));
            }
            tapePanel.add(tapePanelLabels[i], newConstraints(1.0/tapePanelLabels.length, 1.0, i, 0, 1, 1));
        }

        InterpreterCallback callback = new InterpreterCallback() {
            @Override
            public void step(int character, int pointer, byte[] tape) {
                highlighter.removeAllHighlights();

                try {
                    highlighter.addHighlight(character, character + 1, highlighterPainter);
                } catch (Exception ignored) {
                    highlighter.removeAllHighlights();
                }

                for (int i = 0; i < tapePanelLabels.length; i++) {
                    int offsetPointer = ((((-tapePanelLabels.length/2 + pointer + i)) % cells + cells) % cells);
                    tapePanelLabels[i].setText(String.format(tapeDisplayFormat, tape[offsetPointer], offsetPointer));
                }
            }

            @Override
            public Byte input() {
                String text = inputArea.getText();

                byte[] bytes = text.getBytes(StandardCharsets.ISO_8859_1);

                if (bytes.length == 0) {
                    return 0;
                } else {
                    inputArea.setText(text.substring(1));
                    return text.getBytes(StandardCharsets.ISO_8859_1)[0];
                }
            }
        };

        JButton runButton = createButton("Run", e -> {
            interpreter = new Interpreter(cells, codeArea.getText());
            String ret = interpreter.run(callback);
            outputArea.setText(ret);
        });
        JButton stepButton = createButton("Step", e -> outputArea.setText(
                outputArea.getText() +
                interpreter.step(
                        Integer.parseInt(
                                stepCount.getText().replace(",", "")
                        ),
                        callback
                )
        ));
        JButton resetButton = createButton("Reset", e -> {
            interpreter = new Interpreter(cells, codeArea.getText());
            outputArea.setText("");
        });

        panel.setLayout(new GridBagLayout());
        panel.add(codeScrollPane, newConstraints(0.8, 0.9, 0, 0, 1, 6));
        panel.add(inputLabel, newConstraints(0.2, 0.05, 1, 0, 2, 1));
        panel.add(inputScrollPane, newConstraints(0.2, 0.35, 1, 1, 2, 1));
        panel.add(outputLabel, newConstraints(0.2, 0.05, 1, 2, 2, 1));
        panel.add(outputScrollPane, newConstraints(0.2, 0.35, 1, 3, 2, 1));
        panel.add(stepCount, newConstraints(0.1, 0.1, 1, 4, 1, 1));
        panel.add(runButton, newConstraints(0.1, 0.1, 1, 5, 1, 1));
        panel.add(stepButton, newConstraints(0.1, 0.1, 2, 4, 1, 1));
        panel.add(resetButton, newConstraints(0.1, 0.1, 2, 5, 1, 1));
        panel.add(tapePanel, newConstraints(1.0, 0.1, 0, 6, 3, 1));

        add(panel);
        setName("Brainfuck IDE");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setBackground(new Color(37, 37, 37));
        setVisible(true);
    }

    private void updateStyledDocument(JTextArea outputArea, JTextPane codeArea, SimpleAttributeSet plusMinusStyle, SimpleAttributeSet ioStyle, SimpleAttributeSet pointerStyle, SimpleAttributeSet loopStyle, SimpleAttributeSet commentStyle, DefaultStyledDocument styledDocument) {
        interpreter = new Interpreter(cells, codeArea.getText());
        outputArea.setText("");
        highlighter.removeAllHighlights();

        applySyntaxHighlighting(codeArea, plusMinusStyle, ioStyle, pointerStyle, loopStyle, commentStyle, styledDocument);
    }

    private static void applySyntaxHighlighting(JTextPane codeArea, SimpleAttributeSet plusMinusStyle, SimpleAttributeSet ioStyle, SimpleAttributeSet pointerStyle, SimpleAttributeSet loopStyle, SimpleAttributeSet commentStyle, DefaultStyledDocument styledDocument) {
        for (int i = 0; i < codeArea.getText().length(); i++) {
            char c = codeArea.getText().charAt(i);
            SimpleAttributeSet styleToApply = switch (c) {
                case '+', '-' -> plusMinusStyle;
                case '.', ',' -> ioStyle;
                case '>', '<' -> pointerStyle;
                case '[', ']' -> loopStyle;
                default -> commentStyle;
            };

            styledDocument.setCharacterAttributes(i, 1, styleToApply, true);
        }
    }

    public static void main(String[] args) {
        // Allow starting directly from IDEWindow in IDEs
        App.main(args);
    }
}
