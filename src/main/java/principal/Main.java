package principal;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.sql.*;
import java.text.DecimalFormat;

    //Made by Diego Hernandez
public class Main  extends JFrame {
    //Conexion SQLite, carpeta de archivo csv, decimal format a 2 decimales
    public static String url = "jdbc:sqlite:C:/sqlite/database/company_database.db";
    public static String folderPath = "src/archives";
    public static DecimalFormat df = new DecimalFormat("#.##");

    public static void main(String[] args) {
        // Ruta de la base de datos SQLite (Generalmente pondremos en el disco local)
        File databaseDir = new File("C:/sqlite/database");

        if (!databaseDir.exists()) {
            boolean dirCreated = databaseDir.mkdirs();
            if (dirCreated) {
                System.out.println("Se ha creado una nueva carpeta database");
            } else {
                System.out.println("No se pudo crear la carpeta database, revisar codigo.");
            }
        }

        //CONEXION A BASE DE DATOS
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                System.out.println("\nConexion establecida a la base de datos SQLite.");
                File folder = new File(folderPath);
                if (folder.exists()) {
                    System.out.println("Carpeta de archivos csv encontrada");
                    System.out.println("Iniciando procesos...");
                    System.out.println("----------------------------------------------------");
                    CreacionTablas.procesarArchivos(conn);
                    System.out.println("----------------------------------------------------");

                    // Ejecutar la consulta y obtener resultados
                    try (Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery(query)) {
                        showResults(rs);
                    }
                } else {
                    System.out.println("Carpeta de archivos no existe: " + folderPath);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error de conexion: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //Se muestran los resultados dentro de la tabla
    private static void showResults(ResultSet rs) throws SQLException {
        TablaView.modelo = new DefaultTableModel(new Object[]{"project_id", "project_salary_costs", "budget", "cost_fraction"}, 0);
        while (rs.next()) {
            int projectId = rs.getInt("project_id");
            double salaryCosts = rs.getDouble("project_salary_costs");
            double budget = rs.getDouble("budget");
            double costFraction = rs.getDouble("cost_fraction");
            TablaView.modelo.addRow(new Object[]{projectId, df.format(salaryCosts), df.format(budget), df.format(costFraction)});
        }
         TablaView tablaFrame = new TablaView();
         tablaFrame.setVisible(true);

    }

    //Consulta a la base SQL de datos
    public static String query = """
                SELECT p.project_id, SUM(er.salary / 1900 * ep.hours_worked) AS project_salary_costs,
                p.budget, (SUM(er.salary / 1900 * ep.hours_worked) / p.budget) * 100 AS cost_fraction
                FROM projects p JOIN employee_projects ep ON p.project_id = ep.project_id
                JOIN employees_realistic er ON ep.employee_id = er.employee_id
                GROUP BY p.project_id, p.budget;
            """;
}