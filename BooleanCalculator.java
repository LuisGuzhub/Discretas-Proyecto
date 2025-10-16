import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

/**
 * Operadores soportados (ASCII):
 *   ~ o !  -> NOT
 *   &      -> AND
 *   |      -> OR
 *   ^      -> XOR
 *   -> o > -> IMPLIES
 *   <-> o = -> EQUIV (bicondicional)
 * Constantes: 1/0, true/false, T/F
 */
public class BooleanCalculator extends JFrame {
    private JTextField expressionField;
    private JTextField expression2Field;
    private JTextArea resultArea;
    private JTable truthTable;
    private DefaultTableModel tableModel;

    // Indices de columnas (se setean al construir el modelo)
    private int expr1Col = -1;
    private int expr2Col = -1;
    private int eqCol    = -1;

    // Filas donde las expresiones difieren (solo en modo equivalencia)
    private final Set<Integer> diffRows = new HashSet<>();

    // Colores
    private static final Color COL_EXPR1_BG = new Color(225, 239, 255); // azul muy claro
    private static final Color COL_EXPR2_BG = new Color(224, 247, 224); // verde muy claro
    private static final Color DIFF_EXPR_BG = new Color(255, 249, 196); // amarillo muy claro
    private static final Color DIFF_EQ_BG   = new Color(255, 235, 238); // rojo muy claro
    private static final Font  BOLD_FONT    = new Font("Consolas", Font.BOLD, 12);
    private static final Font  PLAIN_FONT   = new Font("Consolas", Font.PLAIN, 12);

    public BooleanCalculator() {
        initializeGUI();
    }

    private void initializeGUI() {
        setTitle("Calculadora de Algebra Booleana");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(245, 245, 250));

        JPanel inputPanel = createInputPanel();
        JPanel buttonPanel = createButtonPanel();
        JPanel resultPanel = createResultPanel();

        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        mainPanel.add(resultPanel, BorderLayout.SOUTH);

        add(mainPanel);
        pack();
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(980, 700));
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(15, 15, 15, 15)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel titleLabel = new JLabel("Calculadora de Algebra Booleana");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(51, 51, 51));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        JLabel instructionLabel = new JLabel("<html><i>Use: A,B,C...  ~ ! (NOT)  & (AND)  | (OR)  ^ (XOR)  -> or > (IMPLIES)  <-> or = (EQUIV)</i></html>");
        instructionLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        instructionLabel.setForeground(new Color(102, 102, 102));
        gbc.gridy = 1;
        panel.add(instructionLabel, gbc);

        gbc.gridwidth = 1; gbc.gridy = 2;
        JLabel label1 = new JLabel("Expresion 1:");
        label1.setFont(new Font("Arial", Font.BOLD, 12));
        gbc.gridx = 0; gbc.anchor = GridBagConstraints.WEST;
        panel.add(label1, gbc);

        expressionField = new JTextField(36);
        expressionField.setFont(new Font("Consolas", Font.PLAIN, 14));
        expressionField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
                new EmptyBorder(5, 8, 5, 8)
        ));
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(expressionField, gbc);

        gbc.gridy = 3;
        JLabel label2 = new JLabel("Expresion 2 (opcional):");
        label2.setFont(new Font("Arial", Font.BOLD, 12));
        gbc.gridx = 0; gbc.anchor = GridBagConstraints.WEST;
        panel.add(label2, gbc);

        expression2Field = new JTextField(36);
        expression2Field.setFont(new Font("Consolas", Font.PLAIN, 14));
        expression2Field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
                new EmptyBorder(5, 8, 5, 8)
        ));
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(expression2Field, gbc);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panel.setBackground(new Color(245, 245, 250));

        JButton calculateButton   = createStyledButton("Calcular Tabla de Verdad", new Color(70, 130, 180));
        JButton equivalenceButton = createStyledButton("Verificar Equivalencia",  new Color(60, 150, 100));
        JButton simplifyAlgButton = createStyledButton("Simplificar (Algebra)",   new Color(124, 98, 180));
        JButton clearButton       = createStyledButton("Limpiar",                 new Color(200, 80, 80));

        calculateButton.addActionListener(e -> calculateTruthTable());
        equivalenceButton.addActionListener(e -> checkEquivalence());
        simplifyAlgButton.addActionListener(e -> simplifyAlgebra());
        clearButton.addActionListener(e -> clearAll());

        panel.add(calculateButton);
        panel.add(equivalenceButton);
        panel.add(simplifyAlgButton);
        panel.add(clearButton);

        return panel;
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent evt) { button.setBackground(color.brighter()); }
            @Override public void mouseExited (MouseEvent evt) { button.setBackground(color); }
        });
        return button;
    }

    private JPanel createResultPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(245, 245, 250));

        resultArea = new JTextArea(12, 98);
        resultArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        resultArea.setEditable(false);
        resultArea.setBackground(Color.WHITE);
        resultArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane1 = new JScrollPane(resultArea);
        scrollPane1.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
                "Analisis / Resultados",
                0, 0, new Font("Arial", Font.BOLD, 12), new Color(51, 51, 51)
        ));

        tableModel = new DefaultTableModel();
        truthTable = new JTable(tableModel);
        truthTable.setFont(new Font("Consolas", Font.PLAIN, 12));
        truthTable.setRowHeight(25);
        truthTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        truthTable.getTableHeader().setBackground(new Color(70, 130, 180));
        truthTable.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scrollPane2 = new JScrollPane(truthTable);
        scrollPane2.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
                "Tabla de Verdad",
                0, 0, new Font("Arial", Font.BOLD, 12), new Color(51, 51, 51)
        ));
        scrollPane2.setPreferredSize(new Dimension(0, 300));

        panel.add(scrollPane1, BorderLayout.NORTH);
        panel.add(scrollPane2, BorderLayout.CENTER);
        return panel;
    }

    private void applyRenderers() {
        TruthTableCellRenderer cellRenderer = new TruthTableCellRenderer(expr1Col, expr2Col, eqCol, diffRows);
        truthTable.setDefaultRenderer(Object.class, cellRenderer);

        TableCellRenderer defaultHeader = truthTable.getTableHeader().getDefaultRenderer();
        truthTable.getColumnModel().getColumns().asIterator().forEachRemaining(col -> {
            int idx = truthTable.getColumnModel().getColumnIndex(col.getHeaderValue().toString());
            truthTable.getColumnModel().getColumn(idx).setHeaderRenderer(new HeaderRenderer(defaultHeader, idx, expr1Col, expr2Col));
        });

        for (int c = 0; c < truthTable.getColumnCount(); c++) {
            truthTable.getColumnModel().getColumn(c).setPreferredWidth(120);
        }
    }

    // ========= CALCULAR TABLA =========
    private void calculateTruthTable() {
        String expression = expressionField.getText().trim();
        if (expression.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor ingrese una expresion", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            diffRows.clear();
            BooleanExpression boolExpr = new BooleanExpression(expression);
            Set<Character> variables = boolExpr.getVariables();
            if (variables.isEmpty()) {
                resultArea.setText("No se encontraron variables en la expresion");
                tableModel.setRowCount(0); tableModel.setColumnCount(0);
                return;
            }

            List<Character> varList = new ArrayList<>(variables);
            Collections.sort(varList);
            int numVars = varList.size();
            int numRows = 1 << numVars;

            String[] columnNames = new String[numVars + 1];
            for (int i = 0; i < numVars; i++) columnNames[i] = String.valueOf(varList.get(i));
            columnNames[numVars] = normalizeForHeader(expression);
            tableModel.setColumnIdentifiers(columnNames);
            tableModel.setRowCount(0);

            expr1Col = numVars;
            expr2Col = -1;
            eqCol    = -1;

            int trueCount = 0;
            for (int mask = 0; mask < numRows; mask++) {
                Object[] row = new Object[numVars + 1];
                Map<Character, Boolean> assignment = new HashMap<>();
                for (int j = 0; j < numVars; j++) {
                    boolean value = ((mask >> (numVars - 1 - j)) & 1) == 1;
                    row[j] = value ? "V" : "F";
                    assignment.put(varList.get(j), value);
                }
                boolean result = boolExpr.evaluate(assignment);
                row[numVars] = result ? "V" : "F";
                if (result) trueCount++;
                tableModel.addRow(row);
            }

            applyRenderers();

            StringBuilder analysis = new StringBuilder();
            analysis.append("ANALISIS DE LA EXPRESION: ").append(expression).append("\n");
            analysis.append("===============================================\n\n");
            analysis.append("Variables: ").append(varList).append("\n");
            analysis.append("Filas: ").append(numRows).append("\n");
            analysis.append("Verdaderos: ").append(trueCount).append("\n\n");

            if (trueCount == numRows)      analysis.append("TIPO: TAUTOLOGIA\n\n");
            else if (trueCount == 0)       analysis.append("TIPO: CONTRADICCION\n\n");
            else                            analysis.append("TIPO: CONTINGENCIA\n\n");

            resultArea.setText(analysis.toString());
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Error al procesar la expresion: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    // ========= EQUIVALENCIA =========
    private void checkEquivalence() {
        String expr1 = expressionField.getText().trim();
        String expr2 = expression2Field.getText().trim();
        if (expr1.isEmpty() || expr2.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese ambas expresiones para verificar equivalencia", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            diffRows.clear();

            BooleanExpression e1 = new BooleanExpression(expr1);
            BooleanExpression e2 = new BooleanExpression(expr2);

            Set<Character> allVariables = new HashSet<>();
            allVariables.addAll(e1.getVariables());
            allVariables.addAll(e2.getVariables());
            if (allVariables.isEmpty()) {
                resultArea.setText("No se encontraron variables en las expresiones");
                tableModel.setRowCount(0); tableModel.setColumnCount(0);
                return;
            }

            List<Character> varList = new ArrayList<>(allVariables);
            Collections.sort(varList);
            int numVars = varList.size();
            int numRows = 1 << numVars;

            String[] columnNames = new String[numVars + 3];
            for (int i = 0; i < numVars; i++) columnNames[i] = String.valueOf(varList.get(i));
            columnNames[numVars]     = normalizeForHeader(expr1);
            columnNames[numVars + 1] = normalizeForHeader(expr2);
            columnNames[numVars + 2] = "Equivalente";
            tableModel.setColumnIdentifiers(columnNames);
            tableModel.setRowCount(0);

            expr1Col = numVars;
            expr2Col = numVars + 1;
            eqCol    = numVars + 2;

            boolean areEquivalent = true; int eqRows = 0;
            for (int mask = 0; mask < numRows; mask++) {
                Object[] row = new Object[numVars + 3];
                Map<Character, Boolean> assignment = new HashMap<>();
                for (int j = 0; j < numVars; j++) {
                    boolean value = ((mask >> (numVars - 1 - j)) & 1) == 1;
                    row[j] = value ? "V" : "F";
                    assignment.put(varList.get(j), value);
                }
                boolean r1 = e1.evaluate(assignment);
                boolean r2 = e2.evaluate(assignment);
                boolean eq = (r1 == r2);
                row[numVars]     = r1 ? "V" : "F";
                row[numVars + 1] = r2 ? "V" : "F";
                row[numVars + 2] = eq ? "SI" : "NO";
                if (eq) eqRows++; else { areEquivalent = false; diffRows.add(mask); }
                tableModel.addRow(row);
            }

            applyRenderers();

            StringBuilder analysis = new StringBuilder();
            analysis.append("VERIFICACION DE EQUIVALENCIA LOGICA\n");
            analysis.append("===============================================\n\n");
            analysis.append("Expresion 1: ").append(expr1).append("\n");
            analysis.append("Expresion 2: ").append(expr2).append("\n\n");
            analysis.append("Variables: ").append(varList).append("\n");
            analysis.append("Filas equivalentes: ").append(eqRows).append(" de ").append(numRows).append("\n\n");
            if (areEquivalent) analysis.append("RESULTADO: SON LOGICAMENTE EQUIVALENTES\n");
            else               analysis.append("RESULTADO: NO SON EQUIVALENTES\n");
            resultArea.setText(analysis.toString());
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Error al verificar equivalencia: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    // ========= SIMPLIFICACION ALGEBRAICA =========
    private void simplifyAlgebra() {
        String expr = expressionField.getText().trim();
        if (expr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese una expresion para simplificar", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            AlgebraicSimplifier.SimplifyResult r = AlgebraicSimplifier.simplifyPretty(expr);

            StringBuilder sb = new StringBuilder();
            sb.append("SIMPLIFICACION ALGEBRAICA (Suma de Productos)\n");
            sb.append("===============================================\n\n");
            for (String line : r.steps) sb.append(line).append("\n");
            sb.append("\nResultado final: ").append(r.result).append("\n");

            resultArea.setText(sb.toString());
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Error en simplificacion: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void clearAll() {
        expressionField.setText("");
        expression2Field.setText("");
        resultArea.setText("");
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);
        diffRows.clear();
    }

    private static String normalizeForHeader(String s){
        return s.replace("->", ">").replace("<->", "=")
                .replace("AND", "&").replace("and", "&")
                .replace("OR", "|").replace("or", "|")
                .replace("XOR", "^").replace("xor", "^")
                .replace("NOT", "~").replace("not", "~");
    }

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) { UIManager.setLookAndFeel(info.getClassName()); break; }
            }
        } catch (Exception ex) {
            try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
            catch (Exception ignore) { }
        }
        SwingUtilities.invokeLater(() -> new BooleanCalculator().setVisible(true));
    }

    // =======================
    // Renderers personalizados
    // =======================
    private static class TruthTableCellRenderer extends DefaultTableCellRenderer {
        private final int expr1Col;
        private final int expr2Col;
        private final int eqCol;
        private final Set<Integer> diffRows;

        TruthTableCellRenderer(int expr1Col, int expr2Col, int eqCol, Set<Integer> diffRows) {
            this.expr1Col = expr1Col;
            this.expr2Col = expr2Col;
            this.eqCol    = eqCol;
            this.diffRows = diffRows;
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            // Reset
            setFont(PLAIN_FONT);
            setBackground(Color.WHITE);
            setForeground(Color.BLACK);

            boolean isExpr1 = (column == expr1Col && expr1Col >= 0);
            boolean isExpr2 = (column == expr2Col && expr2Col >= 0);
            boolean isEq    = (column == eqCol    && eqCol    >= 0);

            if (isExpr1) setBackground(COL_EXPR1_BG);
            if (isExpr2) setBackground(COL_EXPR2_BG);
            if (isExpr1 || isExpr2) setFont(BOLD_FONT);

            if (diffRows.contains(row)) {
                if (isExpr1 || isExpr2) setBackground(DIFF_EXPR_BG);
                if (isEq) { setBackground(DIFF_EQ_BG); setFont(BOLD_FONT); }
            }
            setHorizontalAlignment(SwingConstants.CENTER);
            return this;
        }
    }

    private static class HeaderRenderer extends DefaultTableCellRenderer {
        private final TableCellRenderer fallback;
        private final int myIndex;
        private final int expr1Col;
        private final int expr2Col;

        HeaderRenderer(TableCellRenderer fallback, int myIndex, int expr1Col, int expr2Col) {
            this.fallback = fallback;
            this.myIndex = myIndex;
            this.expr1Col = expr1Col;
            this.expr2Col = expr2Col;
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(new Font("Arial", Font.BOLD, 12));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            c.setForeground(Color.WHITE);
            if (myIndex == expr1Col) c.setBackground(new Color(60, 120, 200));
            else if (myIndex == expr2Col) c.setBackground(new Color(70, 160, 90));
            else c.setBackground(new Color(70, 130, 180));
            return c;
        }
    }
}

/* =======================
   Parser / Evaluador (para tabla y equivalencia)
   ======================= */
class BooleanExpression {
    private final String expr; // normalized
    private final Set<Character> variables;

    public BooleanExpression(String expression) {
        this.expr = preprocessExpression(expression);
        this.variables = extractVariables(this.expr);
        if (this.expr.isEmpty()) throw new RuntimeException("Expresion vacia");
        checkParenthesesBalance(this.expr);
    }

    private static String preprocessExpression(String expr) {
        if (expr == null) return "";
        String e = expr.replaceAll("\\s+", "");
        // normalize longer tokens first
        e = e.replace("<->", "=").replace("<=>", "=").replace("BICONDITIONAL", "=").replace("biconditional", "=").replace("IFF", "=").replace("iff", "=");
        e = e.replace("->", ">").replace("IMPLIES", ">").replace("implies", ">");
        e = e.replace("AND", "&").replace("and", "&");
        e = e.replace("OR", "|").replace("or", "|");
        e = e.replace("XOR", "^").replace("xor", "^");
        e = e.replace("NOT", "~").replace("not", "~").replace("!", "~");
        // constants
        e = e.replace("true", "1").replace("TRUE", "1").replace("T", "1");
        e = e.replace("false", "0").replace("FALSE", "0").replace("F", "0");
        return e;
    }

    private static void checkParenthesesBalance(String e){
        int bal = 0;
        for(char ch : e.toCharArray()){
            if(ch=='(') bal++;
            else if(ch==')') bal--;
            if(bal<0) throw new RuntimeException("Parentesis desbalanceados");
        }
        if(bal!=0) throw new RuntimeException("Parentesis desbalanceados");
    }

    private static boolean isVariable(char c){
        return Character.isLetter(c);
    }

    private static Set<Character> extractVariables(String e){
        Set<Character> vars = new HashSet<>();
        for(char c : e.toCharArray()) if(isVariable(c)) vars.add(c);
        return vars;
    }

    public Set<Character> getVariables() { return new HashSet<>(variables); }

    public boolean evaluate(Map<Character, Boolean> assignment) {
        return eval(stripOuter(expr), assignment);
    }

    private static String stripOuter(String s){
        String e = s;
        while(e.length()>=2 && e.charAt(0)=='(' && e.charAt(e.length()-1)==')' && matchingOuterParens(e)){
            e = e.substring(1, e.length()-1);
        }
        return e;
    }
    private static boolean matchingOuterParens(String e){
        int bal=0; for(int i=0;i<e.length();i++){ char c=e.charAt(i); if(c=='(') bal++; else if(c==')'){ bal--; if(bal==0 && i!=e.length()-1) return false; } }
        return true;
    }

    // precedence: lowest (=, >, |, ^, &) then unary ~
    private static boolean eval(String expr, Map<Character, Boolean> asg){
        String e = stripOuter(expr);
        if(e.isEmpty()) throw new RuntimeException("Sintaxis invalida (vacio)");

        int pos;
        if((pos = findMainOp(e, '=')) >= 0) return applyEquiv(e, pos, asg);
        if((pos = findMainOp(e, '>')) >= 0)  return applyImpl (e, pos, asg);
        if((pos = findMainOp(e, '|')) >= 0)  return applyBin  (e, pos, asg, '|');
        if((pos = findMainOp(e, '^')) >= 0)  return applyBin  (e, pos, asg, '^');
        if((pos = findMainOp(e, '&')) >= 0)  return applyBin  (e, pos, asg, '&');

        if(e.charAt(0) == '~') return !eval(e.substring(1), asg);

        if(e.equals("1")) return true;
        if(e.equals("0")) return false;
        if(e.length()==1 && Character.isLetter(e.charAt(0))) return asg.getOrDefault(e.charAt(0), false);

        throw new RuntimeException("Sintaxis invalida cerca de: " + e);
    }

    private static int findMainOp(String e, char target){
        int bal=0;
        for(int i=e.length()-1;i>=0;i--){
            char c = e.charAt(i);
            if(c==')') bal++;
            else if(c=='(') bal--;
            else if(bal==0 && c==target) { if(i>0) return i; }
        }
        return -1;
    }

    private static boolean applyBin(String e, int pos, Map<Character, Boolean> asg, char op){
        String L = e.substring(0, pos);
        String R = e.substring(pos+1);
        boolean lv = eval(L, asg);
        boolean rv = eval(R, asg);
        switch(op){
            case '&': return (lv & rv);
            case '|': return (lv | rv);
            case '^': return (lv ^ rv);
            default: throw new IllegalStateException();
        }
    }

    private static boolean applyImpl(String e, int pos, Map<Character, Boolean> asg){
        String L = e.substring(0, pos);
        String R = e.substring(pos+1);
        return (!eval(L, asg)) | eval(R, asg);
    }
    private static boolean applyEquiv(String e, int pos, Map<Character, Boolean> asg){
        String L = e.substring(0, pos);
        String R = e.substring(pos+1);
        return eval(L, asg) == eval(R, asg);
    }
}

/* =========================================================
   Simplificador ALGEBRAICO con pasos y LEYES (ASCII)
   ========================================================= */
class AlgebraicSimplifier {

    // ===== AST =====
    interface Node { }
    static class Var implements Node { final String name; Var(String n){name=n;} }
    static class Not implements Node { final Node x; Not(Node x){this.x=x;} }
    static class And implements Node { final Node l,r; And(Node l, Node r){this.l=l; this.r=r;} }
    static class Or  implements Node { final Node l,r; Or (Node l, Node r){this.l=l; this.r=r;} }
    static class Const implements Node { final boolean v; Const(boolean v){this.v=v;} }

    static class SimplifyResult { final List<String> steps; final String result; SimplifyResult(List<String> s, String r){steps=s; result=r;} }

    // ====== API principal ======
    static AlgebraicSimplifier.SimplifyResult simplifyPretty(String raw) {
        List<String> steps = new ArrayList<>();
        steps.add("Expresion inicial: " + raw);

        String normalized = preprocess(raw);
        Node ast = parse(normalized);

        // Empujar negaciones (De Morgan) – solo si cambia
        Node nnf = toNNF(ast);
        if (!toString(ast).equals(toString(nnf))) {
            steps.add("-> [De Morgan / Doble negacion] " + toString(nnf));
        }
        ast = nnf;

        // Expansion (Distributiva) con registro
        StepWriter w = new StepWriter(steps);
        List<Term> dnf = expandWithLoggedDistribution(ast, w);

        // 1) Eliminar contradicciones + duplicados
        List<Term> cleaned = new ArrayList<>();
        for (Term t : dnf) {
            if (t.contradictory) {
                steps.add("-> [Complemento] se elimina termino contradictorio: " + termToString(t));
            } else {
                cleaned.add(t);
            }
        }
        dnf = unique(cleaned);
        steps.add("-> [Idempotencia] " + termsToString(dnf));

        // 2) Reglas adicionales hasta punto fijo:
        boolean changed;
        do {
            changed = false;

            // 2.1) Consenso / Complemento: X·Y + X·Y' = X
            List<Term> combined = combineOppositeLiterals(dnf, steps);
            if (!sameSet(dnf, combined)) {
                dnf = unique(combined);
                steps.add("-> [Resultado parcial] " + termsToString(dnf));
                changed = true;
                continue;
            }

            // 2.2) Absorcion variante: X + X'·Y = X + Y (si hay termino unitario X)
            List<Term> dropOpp = dropOppositeUsingUnit(dnf, steps);
            if (!sameSet(dnf, dropOpp)) {
                dnf = unique(dropOpp);
                steps.add("-> [Resultado parcial] " + termsToString(dnf));
                changed = true;
                continue;
            }

            // 2.3) Absorcion clasica: S + S·X = S (subconjunto)
            List<Term> absorbed = applyAbsorption(dnf, steps);
            if (!sameSet(dnf, absorbed)) {
                dnf = unique(absorbed);
                steps.add("-> [Resultado parcial] " + termsToString(dnf));
                changed = true;
            }

        } while (changed);

        String res = dnf.isEmpty() ? "0" : termsToString(dnf);
        return new SimplifyResult(steps, res);
    }

    /* --------------------------
       Impresion de distributiva
       -------------------------- */
    static class StepWriter {
        final List<String> out;
        StepWriter(List<String> out){ this.out = out; }
        void dist(Node left, Node right, List<Term> before, List<Term> after) {
            if (!termsToString(before).equals(termsToString(after))) {
                out.add("-> [Distributiva] " + termsToString(after));
            }
        }
    }

    /* ---------------------------------------
       Preprocesado / Parser / NNF
       --------------------------------------- */
    static String preprocess(String expr){
        String e = expr.replaceAll("\\s+","");
        e = e.replace("<->","=").replace("<=>","=").replace("IFF","=").replace("iff","=").replace("BICONDITIONAL","=").replace("biconditional","=");
        e = e.replace("->",">").replace("IMPLIES",">").replace("implies",">");
        e = e.replace("AND","&").replace("and","&");
        e = e.replace("OR","|").replace("or","|");
        e = e.replace("XOR","^").replace("xor","^");
        e = e.replace("NOT","~").replace("not","~").replace("!","~");
        e = e.replace("TRUE","1").replace("true","1").replace("T","1");
        e = e.replace("FALSE","0").replace("false","0").replace("F","0");
        return e;
    }

    // Parser que acepta =, >, ^ y los reescribe a ~,&,|
    static Node parse(String s){
        s = stripOuter(s);
        if (s.equals("1")) return new Const(true);
        if (s.equals("0")) return new Const(false);

        int pos;
        // menor precedencia primero
        if ((pos=findMainOp(s,'='))>=0) {
            Node L = parse(s.substring(0,pos));
            Node R = parse(s.substring(pos+1));
            // A=B -> (A&B) | (~A&~B)
            return new Or(new And(L, R), new And(new Not(L), new Not(R)));
        }
        if ((pos=findMainOp(s,'>'))>=0) {
            Node L = parse(s.substring(0,pos));
            Node R = parse(s.substring(pos+1));
            // A>B -> ~A | B
            return new Or(new Not(L), R);
        }
        if ((pos=findMainOp(s,'|'))>=0) return new Or(parse(s.substring(0,pos)), parse(s.substring(pos+1)));
        if ((pos=findMainOp(s,'^'))>=0) {
            Node L = parse(s.substring(0,pos));
            Node R = parse(s.substring(pos+1));
            // A^B -> (A&~B) | (~A&B)
            return new Or(new And(L, new Not(R)), new And(new Not(L), R));
        }
        if ((pos=findMainOp(s,'&'))>=0) return new And(parse(s.substring(0,pos)), parse(s.substring(pos+1)));
        if (s.startsWith("~")) return new Not(parse(s.substring(1)));
        if (s.length()==1 && Character.isLetter(s.charAt(0))) return new Var(String.valueOf(s.charAt(0)));

        throw new RuntimeException("Sintaxis invalida: " + s);
    }

    private static String stripOuter(String s){
        String e = s;
        while(e.length()>=2 && e.charAt(0)=='(' && e.charAt(e.length()-1)==')' && matchingOuterParens(e)){
            e = e.substring(1, e.length()-1);
        }
        return e;
    }
    private static boolean matchingOuterParens(String e){
        int bal=0; for(int i=0;i<e.length();i++){ char c=e.charAt(i); if(c=='(') bal++; else if(c==')'){ bal--; if(bal==0 && i!=e.length()-1) return false; } }
        return true;
    }
    private static int findMainOp(String e, char target){
        int bal=0;
        for (int i=e.length()-1;i>=0;i--){
            char c=e.charAt(i);
            if (c==')') bal++;
            else if (c=='(') bal--;
            else if (bal==0 && c==target) return i;
        }
        return -1;
    }

    static Node toNNF(Node n){
        if (n instanceof Not) {
            Node x = ((Not)n).x;
            if (x instanceof Const) return new Const(!((Const)x).v);
            if (x instanceof Var)   return n;
            if (x instanceof Not)   return toNNF(((Not)x).x); // ~~X
            if (x instanceof And)   return new Or (toNNF(new Not(((And)x).l)), toNNF(new Not(((And)x).r))); // De Morgan
            if (x instanceof Or )   return new And(toNNF(new Not(((Or )x).l)), toNNF(new Not(((Or )x).r)));
        }
        if (n instanceof And) return new And(toNNF(((And)n).l), toNNF(((And)n).r));
        if (n instanceof Or ) return new Or (toNNF(((Or )n).l), toNNF(((Or )n).r));
        return n;
    }

    /* ---------------------------------------
       DNF + registro de pasos Distributiva
       --------------------------------------- */
    static class Term {
        final Map<String, Boolean> lits = new LinkedHashMap<>();
        boolean contradictory = false;
        Term add(String v, boolean val){
            Boolean ex = lits.get(v);
            if (ex == null) lits.put(v,val);
            else if (ex != val) contradictory = true;
            return this;
        }
        Term copy(){ Term t=new Term(); t.lits.putAll(lits); t.contradictory=contradictory; return t; }
    }

    private static List<Term> expandWithLoggedDistribution(Node n, StepWriter w){
        List<Term> out = expand(n, w);
        return unique(out);
    }

    private static List<Term> expand(Node n, StepWriter w){
        if (n instanceof Const){
            if (((Const)n).v) return singletonTrue();
            else return new ArrayList<>();
        }
        if (n instanceof Var){
            Term t = new Term().add(((Var)n).name, true);
            return new ArrayList<>(Collections.singletonList(t));
        }
        if (n instanceof Not && ((Not)n).x instanceof Var){
            Term t = new Term().add(((Var)((Not)n).x).name, false);
            return new ArrayList<>(Collections.singletonList(t));
        }
        if (n instanceof Or){
            List<Term> L = expand(((Or)n).l, w);
            List<Term> R = expand(((Or)n).r, w);
            List<Term> before = joinCopy(L,R);
            List<Term> out = new ArrayList<>(L); out.addAll(R);
            w.dist(((Or)n).l, ((Or)n).r, before, out);
            return out;
        }
        if (n instanceof And){
            List<Term> L = expand(((And)n).l, w);
            List<Term> R = expand(((And)n).r, w);
            List<Term> before = joinCopy(L,R);
            List<Term> out = new ArrayList<>();
            for (Term a : L) for (Term b : R){
                Term t = a.copy();
                for (Map.Entry<String,Boolean> e : b.lits.entrySet()) t.add(e.getKey(), e.getValue());
                out.add(t);
            }
            w.dist(((And)n).l, ((And)n).r, before, out);
            return out;
        }
        throw new RuntimeException("No soportado en DNF: " + toString(n));
    }

    private static List<Term> joinCopy(List<Term> A, List<Term> B){
        List<Term> x = new ArrayList<>();
        x.addAll(A); x.addAll(B);
        return x;
    }

    private static List<Term> singletonTrue(){
        return new ArrayList<>(Collections.singletonList(new Term())); // termino vacio = 1
    }

    private static List<Term> unique(List<Term> terms){
        List<Term> out = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (Term t : terms){
            if (t.contradictory) continue;
            String key = termKey(t);
            if (!seen.contains(key)){ out.add(t); seen.add(key); }
        }
        return out;
    }

    /* ====== Reglas de reduccion adicionales ====== */

    // Consenso: X·Y + X·Y' = X  (combina dos terminos que difieren en un solo literal)
    private static List<Term> combineOppositeLiterals(List<Term> terms, List<String> steps){
        List<Term> out = new ArrayList<>(terms);
        boolean changed = true;
        while (changed){
            changed = false;
            outer:
            for (int i=0;i<out.size();i++){
                for (int j=i+1;j<out.size();j++){
                    Term A = out.get(i), B = out.get(j);
                    if (A.lits.size() != B.lits.size()) continue;
                    String diffVar = null;
                    boolean ok = true;
                    for (String k : A.lits.keySet()){
                        Boolean av = A.lits.get(k);
                        Boolean bv = B.lits.get(k);
                        if (bv == null){ ok=false; break; }
                        if (!Objects.equals(av, bv)){
                            if (diffVar != null){ ok=false; break; }
                            diffVar = k;
                        }
                    }
                    if (ok && diffVar != null){
                        Term C = A.copy();
                        C.lits.remove(diffVar); // X·Y + X·Y' => X (quitamos Y)
                        steps.add("-> [Consenso] " + termToString(A) + " + " + termToString(B) + " => " + termToString(C));
                        out.remove(j); out.remove(i);
                        out.add(C);
                        changed = true;
                        break outer;
                    }
                }
            }
        }
        return out;
    }

    // Absorcion variante: X + X'·Y = X + Y  (si hay termino unitario X)
    private static List<Term> dropOppositeUsingUnit(List<Term> terms, List<String> steps){
        List<Term> out = new ArrayList<>(terms);
        boolean changed = true;
        while (changed){
            changed = false;
            outer:
            for (Term unit : new ArrayList<>(out)){
                if (unit.lits.size() != 1) continue;
                String v = unit.lits.keySet().iterator().next();
                boolean val = unit.lits.get(v);
                for (int i=0;i<out.size();i++){
                    Term t = out.get(i);
                    if (t == unit) continue;
                    Boolean other = t.lits.get(v);
                    if (other != null && other != val){
                        Term reduced = t.copy();
                        reduced.lits.remove(v);
                        steps.add("-> [Absorcion] " + termToString(unit) + " + " + termToString(t) + " => " + termToString(unit) + " + " + termToString(reduced));
                        out.remove(i);
                        out.add(reduced);
                        changed = true;
                        break outer;
                    }
                }
            }
        }
        return out;
    }

    // Absorcion clasica: si A subseteq B, entonces A + B = A
    private static List<Term> applyAbsorption(List<Term> terms, List<String> steps){
        List<Term> out = new ArrayList<>(terms);
        boolean changed = true;
        while (changed){
            changed = false;
            outer:
            for (int i=0;i<out.size();i++){
                for (int j=0;j<out.size();j++){
                    if (i==j) continue;
                    Term A = out.get(i), B = out.get(j);
                    if (isSubset(A,B)) {
                        steps.add("-> [Absorcion] " + termToString(B) + " absorbido por " + termToString(A));
                        out.remove(j);
                        changed = true;
                        break outer;
                    }
                }
            }
        }
        out.sort(Comparator.comparingInt(t->t.lits.size()));
        return out;
    }

    private static boolean isSubset(Term a, Term b){
        for (Map.Entry<String,Boolean> e : a.lits.entrySet()){
            Boolean bv = b.lits.get(e.getKey());
            if (bv == null || !Objects.equals(bv, e.getValue())) return false;
        }
        return true;
    }

    private static boolean sameSet(List<Term> a, List<Term> b){
        return new HashSet<>(toKeys(a)).equals(new HashSet<>(toKeys(b)));
    }
    private static List<String> toKeys(List<Term> t){
        List<String> k = new ArrayList<>();
        for (Term x : t) k.add(termKey(x));
        return k;
    }

    /* ---------------------------------------
       Impresion
       --------------------------------------- */
    private static String termKey(Term t){
        List<String> parts = new ArrayList<>();
        for (Map.Entry<String,Boolean> e : t.lits.entrySet()){
            parts.add((e.getValue() ? "" : "~") + e.getKey());
        }
        Collections.sort(parts);
        return String.join("&", parts);
    }

    static String termToString(Term t){
        if (t.lits.isEmpty()) return "1";
        List<String> lits = new ArrayList<>();
        for (Map.Entry<String,Boolean> e : t.lits.entrySet()){
            lits.add((e.getValue() ? "" : "~") + e.getKey());
        }
        Collections.sort(lits);
        return "(" + String.join(" & ", lits) + ")";
    }

    static String termsToString(List<Term> terms){
        if (terms.isEmpty()) return "0";
        List<String> parts = new ArrayList<>();
        for (Term t : terms) parts.add(termToString(t));
        Collections.sort(parts);
        return String.join(" | ", parts);
    }

    static String toString(Node n){
        if (n instanceof Const) return ((Const)n).v ? "1" : "0";
        if (n instanceof Var)   return ((Var)n).name;
        if (n instanceof Not)   return "~" + toString(((Not)n).x);
        if (n instanceof And)   return "(" + toString(((And)n).l) + " & " + toString(((And)n).r) + ")";
        if (n instanceof Or)    return "(" + toString(((Or )n).l) + " | " + toString(((Or )n).r) + ")";
        return "?";
    }
}
