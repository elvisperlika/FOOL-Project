package svm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A code line with instruction and breakpoint if possible.
 */
interface CodeLine {

    static CodeLine simpleLine(String instruction) {
        return new CodeLine() {
            @Override
            public Optional<Boolean> hasBreakpoint() {
                return Optional.empty();
            }

            @Override
            public void switchBreakpoint() {
            }

            @Override
            public String getInstruction() {
                return instruction;
            }
        };
    }

    static CodeLine lineWithBreakpoint(String instruction) {
        return new CodeLine() {
            private boolean breakpoint;

            @Override
            public Optional<Boolean> hasBreakpoint() {
                return Optional.of(this.breakpoint);
            }

            @Override
            public void switchBreakpoint() {
                this.breakpoint = !this.breakpoint;
            }

            @Override
            public String getInstruction() {
                return instruction;
            }
        };
    }

    Optional<Boolean> hasBreakpoint();

    void switchBreakpoint();

    String getInstruction();
}

public class ExecuteVisualVM {

    public static final int MEMSIZE = 10000;
    public static final int CODESIZE = 10000;
    public static final int BASE_WINDOW_SIZE = 800;
    private static final int BASE_FONT_SIZE = 16;
    private static final Font FONT = new Font(Font.MONOSPACED, Font.PLAIN, ScreenUtils.getFontSize(BASE_FONT_SIZE));
    private final List<CodeLine> codeLines = new ArrayList<>();
    private final JFrame frame;
    private final JPanel mainPanel;
    private final JPanel buttonPanel;
    private final JList<CodeLine> asmList;
    private final JList<String> stackList, heapList;
    private final JButton backStep;
    private final JButton backToBreakPoint;
    private final JButton reset;
    private final JButton nextStep;
    private final JButton play;
    private final JPanel registerPanel;
    private final JSplitPane memPanel;
    private final JLabel tmLabel, raLabel, fpLabel, ipLabel, spLabel, hpLabel;
    private final JScrollPane asmScroll, stackScroll, heapScroll, outputScroll;
    private final JTextArea outputText;
    private final int codeLineCount;
    private final int[] code;
    private final int[] sourceMap;
    private final List<String> source;
    private int[] memory;
    private int ip = 0;
    private int sp = MEMSIZE; // punta al top dello stack
    private int tm;
    private int hp = 0;
    private int ra;
    private int fp = MEMSIZE;
    private String keyboardCommand = "";
    private int debugLineCode = 0;

    public ExecuteVisualVM(int[] code, int[] sourceMap, List<String> source) {
        boolean printArgumentLineNumber = false;
        this.code = code;
        this.sourceMap = sourceMap;
        this.source = source;
        this.memory = new int[MEMSIZE];

        this.frame = new JFrame("FOOL Virtual Machine");
        this.mainPanel = new JPanel();

        this.buttonPanel = new JPanel();
        this.buttonPanel.setLayout(new BoxLayout(this.buttonPanel, BoxLayout.Y_AXIS));
        this.play = new JButton("PLAY");
        this.play.setFont(FONT);
        this.play.addActionListener(e -> this.playButtonHandler());
        this.reset = new JButton("RESET");
        this.reset.setFont(FONT);
        this.reset.addActionListener(e -> this.resetButtonHandler());
        this.backToBreakPoint = new JButton("BACK TO BREAK POINT");
        this.backToBreakPoint.setFont(FONT);
        this.backToBreakPoint.addActionListener(e -> this.backToBreakPointButtonHandler());
        this.backStep = new JButton("BACK STEP");
        this.backStep.setFont(FONT);
        this.backStep.addActionListener(e -> this.backStepButtonHandler());
        this.nextStep = new JButton("STEP");
        this.nextStep.setFont(FONT);
        this.nextStep.addActionListener(e -> this.stepButtonHandler());
        this.buttonPanel.add(this.play);
        this.buttonPanel.add(this.nextStep);
        this.buttonPanel.add(this.reset);
        this.buttonPanel.add(this.backToBreakPoint);
        this.buttonPanel.add(this.backStep);

        this.registerPanel = new JPanel();
        this.tmLabel = new JLabel();
        this.tmLabel.setFont(FONT);
        this.raLabel = new JLabel();
        this.raLabel.setFont(FONT);
        this.fpLabel = new JLabel();
        this.fpLabel.setFont(FONT);
        this.ipLabel = new JLabel();
        this.ipLabel.setFont(FONT);
        this.spLabel = new JLabel();
        this.spLabel.setFont(FONT);
        this.hpLabel = new JLabel();
        this.hpLabel.setFont(FONT);
        this.registerPanel.setLayout(new BoxLayout(this.registerPanel, BoxLayout.Y_AXIS));
        this.registerPanel.add(this.tmLabel);
        this.registerPanel.add(this.raLabel);
        this.registerPanel.add(this.fpLabel);
        this.registerPanel.add(this.ipLabel);
        this.registerPanel.add(this.spLabel);
        this.registerPanel.add(this.hpLabel);

        this.mainPanel.setLayout(new BorderLayout());
        this.asmList = new JList<>();

        int realIp = 0;
        for (var line : this.source) {

            // line blank should not be printed
            if (line.isBlank()) {
                codeLines.add(CodeLine.simpleLine("\n"));
                continue;
            }

            // label for function definition is not ad instruction in code[]
            // => setting same address of first function instruction
            if (line.contains(":")) {
                //    commandLines.add(String.format("%5d: %s", realIp, line));
                codeLines.add(CodeLine.simpleLine("       " + line));
                continue;
            }

            var macro = line.split(" ");
            if (macro.length > 1) {
                if (printArgumentLineNumber) {
                    codeLines.add(CodeLine.lineWithBreakpoint(String.format(
                            "%5d: %s   | %5d: %s",
                            realIp++,
                            macro[0],
                            realIp++,
                            macro[1]
                    )));
                } else {
                    codeLines.add(CodeLine.lineWithBreakpoint(String.format(
                            "%5d: %s",
                            realIp++,
                            line
                    )));
                    realIp++;
                }
            } else {
                codeLines.add(CodeLine.lineWithBreakpoint(String.format(
                        "%5d: %s",
                        realIp++,
                        line
                )));
            }
        }

        this.asmList.setListData(new Vector<>(codeLines));
        this.asmList.setCellRenderer(new CodeLineCellRenderer());
        this.codeLineCount = this.source.size();

        this.asmList.setFont(FONT);
        removeListenersFrom(this.asmList);

        this.asmList.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                codeLines.get(asmList.locationToIndex(e.getPoint())).switchBreakpoint();
                asmList.repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });

        this.asmScroll = new JScrollPane(
                this.asmList,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        final int baseFontSize = 16;
        final int suggestedFontSize = ScreenUtils.getFontSize(baseFontSize);
        final var borderFont = new Font(Font.MONOSPACED, Font.PLAIN, suggestedFontSize);

        final var asmScrollBorder = BorderFactory.createTitledBorder("CODE");
        asmScrollBorder.setTitleFont(borderFont);
        this.asmScroll.setBorder(asmScrollBorder);
        this.mainPanel.add(this.asmScroll, BorderLayout.EAST);

        this.stackList = new JList<>();
        removeListenersFrom(this.stackList);
        this.heapList = new JList<>();
        removeListenersFrom(this.heapList);

        final Font fontBold = new Font(Font.MONOSPACED, Font.BOLD, suggestedFontSize);
        this.stackList.setFont(fontBold);
        this.heapList.setFont(fontBold);
        this.stackScroll = new JScrollPane(
                this.stackList,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        final var stackScrollBorder = BorderFactory.createTitledBorder("STACK");
        stackScrollBorder.setTitleFont(borderFont);
        this.stackScroll.setBorder(stackScrollBorder);
        this.heapScroll = new JScrollPane(
                this.heapList,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        final var heapScrollBorder = BorderFactory.createTitledBorder("HEAP");
        heapScrollBorder.setTitleFont(borderFont);
        this.heapScroll.setBorder(heapScrollBorder);

        this.memPanel = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                this.stackScroll,
                this.heapScroll
        );
        this.mainPanel.add(this.memPanel, BorderLayout.CENTER);

        this.outputText = new JTextArea();
        this.outputText.setFont(FONT);
        this.outputText.setRows(7);
        this.outputText.setEditable(false);
        this.outputScroll = new JScrollPane(
                this.outputText,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );

        setMem();
        this.frame.getContentPane().setLayout(new BorderLayout());
        this.frame.add(mainPanel, BorderLayout.CENTER);
        this.frame.add(buttonPanel, BorderLayout.EAST);
        this.frame.add(registerPanel, BorderLayout.WEST);
        this.frame.add(outputScroll, BorderLayout.SOUTH);

        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.outputText.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                ExecuteVisualVM.this.keyboardCommand += e.getKeyChar();
                ExecuteVisualVM.this.checkKeyboardCommand();
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
            }
        });

        this.update();
        this.frame.setPreferredSize(ScreenUtils.getMinimumSize(BASE_WINDOW_SIZE, BASE_WINDOW_SIZE));
        this.frame.pack();
        this.frame.setLocation(ScreenUtils.getScreenCenter(this.frame));

        this.stackScroll.getVerticalScrollBar()
                .setValue(this.stackScroll.getVerticalScrollBar().getMaximum());
        this.memPanel.setDividerLocation(0.5);

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                e.printStackTrace();
                System.exit(1);
            }
        });
    }

    private void reset() {
        this.memory = new int[MEMSIZE];
        this.tm = 0;
        this.ra = 0;
        this.fp = MEMSIZE;
        this.ip = 0;
        this.sp = MEMSIZE;
        this.hp = 0;
        this.nextStep.setEnabled(true);
        this.play.setEnabled(true);
        this.outputText.setText("");
    }

    private void resetButtonHandler() {
        this.debugLineCode = 0;
        this.reset();
        this.update();
    }

    private void backToBreakPointButtonHandler() {
        this.reset();
        int nearlestBreakpoint = 0;
        int tempBreakpoint = 0;
        while (this.step()) {
            tempBreakpoint++;
            if (lineHasBreakpoint()) {
                nearlestBreakpoint = tempBreakpoint;
            }

            if (tempBreakpoint + 1 == this.debugLineCode) {
                break;
            }
        }
        this.debugLineCode = nearlestBreakpoint + 1;
        this.backStepButtonHandler();

    }

    private void backStepButtonHandler() {
        this.reset();
        if (this.debugLineCode < 2) {
            this.debugLineCode = 0;
            this.update();
        } else {
            this.debugLineCode--;
            int tempBreakpoint = 0;
            while (this.step()) {
                tempBreakpoint++;
                if (tempBreakpoint == this.debugLineCode) {
                    this.update();
                    return;
                }
            }
            this.nextStep.setEnabled(false);
            this.play.setEnabled(false);
            ip--;
            this.update();
        }
    }

    private <E> void removeListenersFrom(JList<E> list) {
        for (MouseListener m : list.getMouseListeners()) {
            list.removeMouseListener(m);
        }
        for (MouseMotionListener m : list.getMouseMotionListeners()) {
            list.removeMouseMotionListener(m);
        }
    }

    private void checkKeyboardCommand() {
        if (this.keyboardCommand.endsWith(" ")) {
            this.stepButtonHandler();
        } else if (this.keyboardCommand.endsWith("\n")) {
            this.playButtonHandler();
        } else if (this.keyboardCommand.endsWith("fra")) {
            this.play.setEnabled(false);
        } else if (this.keyboardCommand.endsWith("tranqui")) {
            this.play.setEnabled(true);
        } else {
            return;
        }
        this.keyboardCommand = "";
    }

    private void setMem() {
        // Codice per non visualizzare 0 in memoria
//        this.stackList.setListData(new Vector<>(
//                IntStream.range(0, MEMSIZE).mapToObj(x -> String.format("%5d: %s", x, x <= hp || x >= sp ? this.memory[x] : ""))
//                        .collect(Collectors.toList())));
//        this.heapList.setListData(new Vector<>(
//                IntStream.range(0, MEMSIZE).mapToObj(x -> String.format("%5d: %s", x, x <= hp || x >= sp ? this.memory[x] : ""))
//                        .collect(Collectors.toList())));
        final var mem = IntStream.range(0, MEMSIZE)
                .mapToObj(x -> String.format("%5d: %s", x, this.memory[x]))
                .collect(Collectors.toCollection(ArrayList::new));
        mem.add(String.valueOf(MEMSIZE));

        var memory = new Vector<>(mem);

        this.stackList.setListData(memory);
        this.stackList.clearSelection();
        this.stackList.setSelectedIndex(this.sp);
        this.stackScroll.getVerticalScrollBar()
                .setValue(computeScrollDestination(
                        this.stackScroll.getVerticalScrollBar(),
                        this.sp
                ));

        this.heapList.setListData(memory);
        this.heapList.clearSelection();
        this.heapList.setSelectedIndex(this.hp);
        this.heapScroll.getVerticalScrollBar()
                .setValue(computeScrollDestination(
                        this.heapScroll.getVerticalScrollBar(),
                        this.hp
                ));
    }

    private int computeScrollDestination(JScrollBar scroll, int pointer) {
        return Math.max(
                pointer * (scroll.getMaximum() / MEMSIZE) - scroll.getHeight() / 2,
                0
        );
    }

    private void update() {
        this.raLabel.setText("RA: " + this.ra);
        this.fpLabel.setText("FP: " + this.fp);
        this.tmLabel.setText("TM: " + this.tm);
        this.ipLabel.setText("IP: " + this.ip);
        this.hpLabel.setText("HP: " + this.hp);
        this.spLabel.setText("SP: " + this.sp);
        this.asmList.clearSelection();
        this.asmList.setSelectedIndex(this.sourceMap[this.ip]);
        final JScrollBar s = this.asmScroll.getVerticalScrollBar();
        int dest = this.sourceMap[this.ip] * s.getMaximum() / this.codeLineCount -
                s.getHeight() / 2;
        s.setValue(Math.max(dest, 0));
        setMem();
        var condToDisableButton = this.ip != 0;
        this.reset.setEnabled(condToDisableButton);
        this.backStep.setEnabled(condToDisableButton);
        this.backToBreakPoint.setEnabled(condToDisableButton);
    }

    public void cpu() {
        this.frame.setVisible(true);
    }

    private void playButtonHandler() {
        while (this.step()) {
            debugLineCode++;
            if (lineHasBreakpoint()) {
                this.update();
                return;
            }
        }
        this.nextStep.setEnabled(false);
        this.play.setEnabled(false);
        ip--;
        this.update();
    }

    private boolean lineHasBreakpoint() {
        return this.ip < this.codeLineCount && this.codeLines.get(this.sourceMap[this.ip]).hasBreakpoint().orElse(false);
    }

//    private int getBreakPointCount() {
//        return (int) this.codeLines.stream()
//                .map(CodeLine::hasBreakpoint)
//                .filter(opt -> opt.orElse(false))
//                .count();
//    }

    private void stepButtonHandler() {
        boolean play = this.step();
        if (!play) {
            this.nextStep.setEnabled(false);
            this.play.setEnabled(false);
        } else {
            this.debugLineCode++;
            this.update();
        }
    }

    private boolean step() {
        int bytecode = fetch();
        int v1, v2;
        int address;
        switch (bytecode) {
            case SVMParser.PUSH:
                v1 = fetch();
                push(v1);
                break;
            case SVMParser.POP:
                pop();
                break;
            case SVMParser.ADD:
                v1 = pop();
                v2 = pop();
                push(v2 + v1);
                break;
            case SVMParser.SUB:
                v1 = pop();
                v2 = pop();
                push(v2 - v1);
                break;
            case SVMParser.MULT:
                v1 = pop();
                v2 = pop();
                push(v2 * v1);
                break;
            case SVMParser.DIV:
                v1 = pop();
                v2 = pop();
                push(v2 / v1);
                break;
            case SVMParser.STOREW:
                address = pop();
                memory[address] = pop();
                break;
            case SVMParser.LOADW:
                push(memory[pop()]);
                break;
            case SVMParser.BRANCH:
                ip = fetch();
                break;
            case SVMParser.BRANCHEQ:
                address = fetch();
                v1 = pop();
                v2 = pop();
                ip = v2 == v1 ? address : ip;
                break;
            case SVMParser.BRANCHLESSEQ:
                address = fetch();
                v1 = pop();
                v2 = pop();
                ip = v2 <= v1 ? address : ip;
                break;
            case SVMParser.JS:
                address = pop();
                ra = ip;
                ip = address;
                break;
            case SVMParser.LOADRA:
                push(ra);
                break;
            case SVMParser.STORERA:
                ra = pop();
                break;
            case SVMParser.LOADTM:
                push(tm);
                break;
            case SVMParser.STORETM:
                tm = pop();
                break;
            case SVMParser.LOADFP:
                push(fp);
                break;
            case SVMParser.STOREFP:
                fp = pop();
                break;
            case SVMParser.COPYFP:
                fp = sp;
                break;
            case SVMParser.LOADHP:
                push(hp);
                break;
            case SVMParser.STOREHP:
                hp = pop();
                break;
            case SVMParser.PRINT:
                final String output =
                        sp == MEMSIZE ? "EMPTY STACK" : Integer.toString(memory[sp]);
                System.out.println(output);
                this.outputText.append(output + "\n");
                break;
            case SVMParser.HALT:
                return false;
        }
        if (this.sp <= this.hp) {
            System.out.println("Segmentation fault");
            this.outputText.append("Segmentation fault\n");
            return false;
        }
        return true;
    }

    private int pop() {
        return memory[sp++];
    }

    private void push(int v) {
        memory[--sp] = v;
    }

    private int fetch() {
        return code[ip++];
    }

}

class BreakpointIconStyle implements Icon {

    private final int dim;

    public BreakpointIconStyle(int dimension) {
        // We use the scaled dimension from ScreenUtils
        this.dim = dimension;
    }

    @Override
    public void paintIcon(Component component, Graphics g, int x, int y) {
        AbstractButton button = (AbstractButton) component;
        ButtonModel model = button.getModel();
        Graphics2D g2d = (Graphics2D) g.create();

        // Enable Anti-aliasing for smooth circles
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int y_offset = (component.getHeight() - dim) / 2;
        int x_offset = 2;

        if (model.isSelected()) {
            // --- DRAW ACTIVE BREAKPOINT (Red Circle) ---
            g2d.setColor(new Color(200, 0, 0)); // Deep Red
            g2d.fillOval(x_offset, y_offset, dim, dim);

            // Subtle highlight for a 3D effect
            g2d.setColor(new Color(255, 100, 100));
            g2d.drawOval(x_offset, y_offset, dim, dim);
        } else {
            // --- DRAW INACTIVE STATE ---
            if (model.isRollover()) {
                // Ghost breakpoint on hover
                g2d.setColor(new Color(200, 0, 0, 50));
                g2d.fillOval(x_offset, y_offset, dim, dim);
            }
            // Outline always visible or subtle
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.drawOval(x_offset, y_offset, dim, dim);
        }

        g2d.dispose();
    }

    @Override
    public int getIconWidth() {
        return dim + 4; // Padding
    }

    @Override
    public int getIconHeight() {
        return dim;
    }
}

class CodeLineCellRenderer extends JPanel implements ListCellRenderer<CodeLine> {
    private final JLabel label;
    private final JCheckBox checkBox;

    public CodeLineCellRenderer() {
        setLayout(new BorderLayout());
        setOpaque(true);

        checkBox = new JCheckBox();
        checkBox.setOpaque(false); // Allows JPanel background to show through

        // Use a base size of 20px and scale it proportionally
        final int scaledDim = ScreenUtils.getScaledComponentSize(16);
        final var icon = new BreakpointIconStyle(scaledDim);
        checkBox.setIcon(icon);

        label = new JLabel();

        add(checkBox, BorderLayout.WEST);
        add(label, BorderLayout.CENTER);
    }

    @Override
    public Component getListCellRendererComponent(
            JList<? extends CodeLine> list,
            CodeLine value,
            int index,
            boolean isSelected,
            boolean cellHasFocus
    ) {
        label.setText(value.getInstruction());
        label.setFont(list.getFont());

        boolean noBreakpoint = value.hasBreakpoint().isEmpty();
        if (noBreakpoint) {
            checkBox.setVisible(false);
        } else {
            checkBox.setVisible(true);
            checkBox.setSelected(value.hasBreakpoint().get());
        }

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            label.setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            label.setForeground(noBreakpoint ? Color.BLUE : list.getForeground());
        }

        return this;
    }
}