package principal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class CreacionTablas {

    //PROCESAR LOS ARCHIVOS ALMACENADOS EN LA CARPETA archives
    public static void procesarArchivos(Connection conn) throws Exception {
        File folder = new File(Main.folderPath);
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".csv"));
        System.out.println("Procesando archivos de datos\n");
        if (files != null) {
            for (File file : files) {

                // Leer CSV e verificar si no esta vacio
                List<String[]> rows = readCSV(file);
                if (rows.isEmpty()) {
                    System.out.println("Archivo vacio: " + file.getName());
                    continue;
                }

                //Se selecciona el nombre de la nueva table dependiendo del nombre del archivo
                String tableName = file.getName().replace(".csv", "");
                String[] headers = rows.get(0);// Encabezados
                List<String> columnTypes = inferColumnTypes(rows);

                createTable(conn, tableName, headers, columnTypes);

                if (isTableEmpty(conn, tableName)) {
                    insertData(conn, tableName, headers, rows);
                } else {
                    //Mensaje en caso de que ya existan datos dentro de la tabla.
                    System.out.println("- Tabla " + tableName + " ya contiene datos. Saltando pasos.");
                }
            }
        }

    }

    //Leer datos dentro del archivo CSV (Separar datos en comas)
    private static List<String[]> readCSV(File csvFile) throws Exception {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                rows.add(line.split(","));
            }
        }
        return rows;
    }

    //DETECTAR EL TIPO DE DATOS DE LA TABLA
    private static List<String> inferColumnTypes(List<String[]> rows) {
        List<String> columnTypes = new ArrayList<>();
        int numColumns = rows.get(0).length;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        for (int col = 0; col < numColumns; col++) {
            boolean isInteger = true;
            boolean isFloat = true;
            boolean isDate = true;

            for (int row = 1; row < rows.size(); row++) {
                // Saltar encabezados
                String value = rows.get(row)[col].trim();
                if (value.isEmpty()) continue;

                // Verificar si es Integer, Float, o Date
                if (!value.matches("-?\\d+")) isInteger = false;
                if (!value.matches("-?\\d*\\.\\d+")) isFloat = false;
                try {
                    dateFormat.parse(value);
                } catch (ParseException e) {
                    isDate = false;
                }
            }

            //Devolver resultados
            if (isInteger) columnTypes.add("INT");
            else if (isFloat) columnTypes.add("FLOAT");
            else if (isDate) columnTypes.add("DATE");
            else columnTypes.add("NVARCHAR(255)");
        }

        return columnTypes;
    }

    //Crear tablas
    private static void createTable(Connection conn, String tableName, String[] headers, List<String> columnTypes) throws Exception {
        StringBuilder createQuery = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName + " (");
        for (int i = 0; i < headers.length; i++) {
            createQuery.append(headers[i]).append(" ").append(columnTypes.get(i));
            if (i < headers.length - 1) createQuery.append(", ");
        }
        createQuery.append(");");

        //Mensaje de exito
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createQuery.toString());
            System.out.println("• Tabla creada o ya existente: " + tableName);
        }
    }
    //Insertar datos dentro de la tabla, separando en comas
    private static void insertData(Connection connection, String tableName, String[] headers, List<String[]> rows) throws Exception {
        StringBuilder insertQuery = new StringBuilder("INSERT INTO " + tableName + " (");
        for (int i = 0; i < headers.length; i++) {
            insertQuery.append(headers[i]);
            if (i < headers.length - 1) insertQuery.append(", ");
        }
        insertQuery.append(") VALUES (");
        for (int i = 0; i < headers.length; i++) {
            insertQuery.append("?");
            if (i < headers.length - 1) insertQuery.append(", ");
        }
        insertQuery.append(");");

        try (PreparedStatement pstmt = connection.prepareStatement(insertQuery.toString())) {
            for (int i = 1; i < rows.size(); i++) { // Saltar encabezados
                String[] values = rows.get(i);
                for (int j = 0; j < values.length; j++) {
                    pstmt.setString(j + 1, values[j].trim());
                }
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            System.out.println("Datos insertados en la tabla: " + tableName);
        }
    }
    //Verifica si la tabla se encuentra vacia dentro de la base de datos
    private static boolean isTableEmpty(Connection connection, String tableName) throws Exception {
        String query = "SELECT COUNT(*) FROM " + tableName;
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        }
        return true;
    }
}