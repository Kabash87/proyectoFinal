package principal;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class TablaView extends JFrame {
    private JPanel panel;
    private JLabel textoInicio;
    public static DefaultTableModel modelo;
    public JTable tablaDatos;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                TablaView frame = new TablaView();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public TablaView() {
        setTitle("Tabla con datos: Acceso a datos");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Inicializar el panel principal
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        getContentPane().add(panel);

        textoInicio = new JLabel("Acceso a datos. Datos de la tabla:");
        textoInicio.setFont(new Font("Segoe UI Semibold", Font.BOLD, 22));
        textoInicio.setBorder(new EmptyBorder(10, 10, 10, 10));
        textoInicio.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(textoInicio, BorderLayout.NORTH);

        tablaDatos = new JTable(modelo);

        // Agregar la tabla dentro de un JScrollPane
        JScrollPane scrollPane = new JScrollPane(tablaDatos);
        panel.add(scrollPane, BorderLayout.CENTER);
    }
}
