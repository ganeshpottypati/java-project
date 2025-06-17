import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;
import java.util.*;

public class JavaEffects extends Application {
    private Connection connection;
    private VBox formArea = new VBox(10);
    private VBox actionArea = new VBox(10);
    private TableView<Map<String, String>> tableView = new TableView<>();
    private Label statusLabel = new Label();
    private ComboBox<String> tableSelector = new ComboBox<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        connectToDatabase();

        // Left: CRUD Buttons
        VBox crudButtons = new VBox(10);
        Button btnCreateTable = new Button("Create Table");
        Button btnInsertData = new Button("Insert Data");
        Button btnViewTable = new Button("View Table");
        Button btnDeleteRow = new Button("Delete Row");
        crudButtons.getChildren().addAll(btnCreateTable, btnInsertData, btnViewTable, btnDeleteRow);

        // Center: Dynamic Action Area
        ScrollPane actionPane = new ScrollPane(actionArea);
        actionPane.setFitToWidth(true);
        actionPane.setPrefHeight(300);

        // Right: Table Display
        tableView.setPlaceholder(new Label("No data to show"));
        tableView.setPrefWidth(600);

        HBox content = new HBox(10, crudButtons, actionPane, tableView);
        content.setPadding(new Insets(10));
        content.setPrefHeight(600);

        VBox root = new VBox(10, content, statusLabel);
        Scene scene = new Scene(root, 1000, 600);
        stage.setTitle("JavaFX MySQL Full CRUD");
        stage.setScene(scene);
        stage.show();

        btnCreateTable.setOnAction(e -> showCreateTableForm());
        btnInsertData.setOnAction(e -> showInsertForm());
        btnViewTable.setOnAction(e -> showTableView());
        btnDeleteRow.setOnAction(e -> showDeleteForm());
    }

    private void connectToDatabase() {
        try {
            // Load MySQL driver (optional in recent JDBC versions)
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/college_details",
                    "root", "ganesh123");
            showSuccess("Connected to MySQL database.");
        } catch (Exception e) {
            showError("MySQL connection failed: " + e.getMessage());
        }
    }

    private void showCreateTableForm() {
        actionArea.getChildren().clear();
        TextField tableNameField = new TextField();
        tableNameField.setPromptText("Enter Table Name");

        VBox columnFields = new VBox(5);
        addColumnRow(columnFields);

        Button addColBtn = new Button("+ Add Column");
        addColBtn.setOnAction(e -> addColumnRow(columnFields));

        Button createBtn = new Button("Create Table");
        createBtn.setOnAction(e -> {
            String tableName = tableNameField.getText().trim();
            if (tableName.isEmpty()) {
                showError("Table name cannot be empty.");
                return;
            }
            if (doesTableExist(tableName)) {
                showError("Table already exists. Use another name.");
                return;
            }

            List<String> columns = new ArrayList<>();
            for (javafx.scene.Node node : columnFields.getChildren()) {
                HBox row = (HBox) node;
                TextField colName = (TextField) row.getChildren().get(0);
                TextField colType = (TextField) row.getChildren().get(1);
                if (!colName.getText().isEmpty() && !colType.getText().isEmpty()) {
                    String type = colType.getText().trim();
                    if (type.equalsIgnoreCase("varchar")) type = "VARCHAR(255)";
                    columns.add(colName.getText() + " " + type);
                }
            }

            if (columns.isEmpty()) {
                showError("Add at least one column.");
                return;
            }

            String query = "CREATE TABLE " + tableName + " (" + String.join(", ", columns) + ")";
            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate(query);
                showSuccess("Table '" + tableName + "' created.");
            } catch (SQLException ex) {
                showError("Creation failed: " + ex.getMessage());
            }
        });

        actionArea.getChildren().addAll(new Label("Create New Table"), tableNameField, columnFields, addColBtn, createBtn);
    }

    private void addColumnRow(VBox columnFields) {
        TextField name = new TextField();
        name.setPromptText("Column Name");
        TextField type = new TextField();
        type.setPromptText("Data Type (e.g., INT, VARCHAR(100))");
        columnFields.getChildren().add(new HBox(10, name, type));
    }

    private boolean doesTableExist(String tableName) {
        try (ResultSet rs = connection.getMetaData().getTables(null, null, tableName, null)) {
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    private void showInsertForm() {
        actionArea.getChildren().clear();
        actionArea.getChildren().add(new Label("Select Table to Insert Data Into:"));

        tableSelector = new ComboBox<>();
        updateTableList();
        actionArea.getChildren().add(tableSelector);

        VBox inputFields = new VBox(10);
        actionArea.getChildren().add(inputFields);

        tableSelector.setOnAction(e -> {
            inputFields.getChildren().clear();
            String selectedTable = tableSelector.getValue();
            if (selectedTable != null) {
                try (Statement stmt = connection.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT * FROM " + selectedTable + " LIMIT 1")) {
                    ResultSetMetaData meta = rs.getMetaData();
                    for (int i = 1; i <= meta.getColumnCount(); i++) {
                        TextField tf = new TextField();
                        tf.setPromptText(meta.getColumnName(i));
                        inputFields.getChildren().add(tf);
                    }
                } catch (SQLException ex) {
                    showError("Failed to get table structure: " + ex.getMessage());
                }
            }
        });

        Button insertBtn = new Button("Insert Row");
        actionArea.getChildren().add(insertBtn);

        insertBtn.setOnAction(e -> {
            String table = tableSelector.getValue();
            if (table == null) {
                showError("Select a table.");
                return;
            }

            List<String> columns = new ArrayList<>();
            List<String> values = new ArrayList<>();
            for (javafx.scene.Node node : inputFields.getChildren()) {
                TextField tf = (TextField) node;
                columns.add(tf.getPromptText());
                values.add("'" + tf.getText().replace("'", "''") + "'");
            }

            String query = "INSERT INTO " + table + " (" + String.join(", ", columns) + ") VALUES (" + String.join(", ", values) + ")";
            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate(query);
                showSuccess("Data inserted into '" + table + "'");
            } catch (SQLException ex) {
                showError("Insertion failed: " + ex.getMessage());
            }
        });
    }

    private void showTableView() {
        actionArea.getChildren().clear();
        actionArea.getChildren().add(new Label("Select Table to View:"));

        tableSelector = new ComboBox<>();
        updateTableList();
        actionArea.getChildren().add(tableSelector);

        tableSelector.setOnAction(e -> {
            String table = tableSelector.getValue();
            if (table == null) return;

            tableView.getColumns().clear();
            tableView.getItems().clear();

            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM " + table)) {

                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();

                for (int i = 1; i <= colCount; i++) {
                    final int index = i;
                    TableColumn<Map<String, String>, String> column = new TableColumn<>(meta.getColumnName(i));
                    String columnName = meta.getColumnName(index);
                    column.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(columnName)));
                    tableView.getColumns().add(column);
                }

                while (rs.next()) {
                    Map<String, String> row = new HashMap<>();
                    for (int i = 1; i <= colCount; i++) {
                        row.put(meta.getColumnName(i), rs.getString(i));
                    }
                    tableView.getItems().add(row);
                }

            } catch (SQLException ex) {
                showError("Could not load data: " + ex.getMessage());
            }
        });
    }

    private void showDeleteForm() {
        actionArea.getChildren().clear();
        actionArea.getChildren().add(new Label("Select Table to Delete From:"));
        tableSelector = new ComboBox<>();
        updateTableList();
        actionArea.getChildren().add(tableSelector);

        TextField conditionField = new TextField();
        conditionField.setPromptText("Enter WHERE condition (e.g., id=1)");
        actionArea.getChildren().add(conditionField);

        Button deleteBtn = new Button("Delete Rows");
        deleteBtn.setOnAction(e -> {
            String table = tableSelector.getValue();
            String condition = conditionField.getText().trim();
            if (table == null || condition.isEmpty()) {
                showError("Please select a table and enter a condition.");
                return;
            }
            String query = "DELETE FROM " + table + " WHERE " + condition;
            try (Statement stmt = connection.createStatement()) {
                int affected = stmt.executeUpdate(query);
                showSuccess(affected + " row(s) deleted from '" + table + "'.");
            } catch (SQLException ex) {
                showError("Delete failed: " + ex.getMessage());
            }
        });
        actionArea.getChildren().add(deleteBtn);
    }

    private void updateTableList() {
        try (ResultSet rs = connection.getMetaData().getTables(null, null, "%", new String[]{"TABLE"})) {
            tableSelector.getItems().clear();
            while (rs.next()) {
                tableSelector.getItems().add(rs.getString("TABLE_NAME"));
            }
        } catch (SQLException e) {
            showError("Failed to list tables: " + e.getMessage());
        }
    }

    private void showError(String msg) {
        statusLabel.setStyle("-fx-text-fill: red;");
        statusLabel.setText(msg);
    }

    private void showSuccess(String msg) {
        statusLabel.setStyle("-fx-text-fill: green;");
        statusLabel.setText(msg);
    }
}
