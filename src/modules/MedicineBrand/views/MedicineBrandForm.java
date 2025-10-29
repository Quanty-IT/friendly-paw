package modules.MedicineBrand.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import modules.MedicineBrand.controllers.MedicineBrandController;
import modules.MedicineBrand.models.MedicineBrand;
import config.Database;

import java.sql.Connection;
import java.sql.SQLException;

public class MedicineBrandForm extends VBox {

    private TextField nameField;
    private Connection conn;

    /**
     * Construtor do formulário de cadastro de nova marca de medicamento.
     */
    public MedicineBrandForm() {
        this.setSpacing(20);
        this.setPadding(new Insets(30, 40, 30, 40));
        this.setAlignment(Pos.CENTER);
        this.getStyleClass().add("form-bg");

        Label titleLabel = new Label("Cadastrar nova marca");
        titleLabel.getStyleClass().add("form-title");

        Label nameLabel = new Label("Nome da marca:");
        nameLabel.getStyleClass().add("form-label");

        nameField = new TextField();
        nameField.setPromptText("Digite o nome da marca");
        nameField.setPrefWidth(350);
        nameField.getStyleClass().add("form-input");

        try {
            conn = Database.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Button saveButton = new Button("Salvar");
        saveButton.getStyleClass().add("form-btn-save");
        saveButton.setPrefWidth(150);
        saveButton.setOnAction(e -> saveBrand());

        Button cancelButton = new Button("Cancelar");
        cancelButton.getStyleClass().add("form-btn-cancel");
        cancelButton.setPrefWidth(150);
        cancelButton.setOnAction(e -> closeWindow());

        HBox buttonBox = new HBox(15, cancelButton, saveButton);
        buttonBox.setAlignment(Pos.CENTER);

        VBox formContent = new VBox(10, nameLabel, nameField);
        formContent.setAlignment(Pos.CENTER_LEFT);
        formContent.setPadding(new Insets(10, 0, 10, 0));

        this.getChildren().addAll(titleLabel, formContent, buttonBox);
        this.getStylesheets().add(getClass().getResource("/modules/MedicineBrand/styles/MedicineBrandForm.css").toExternalForm());
    }

    /**
     * Salva uma nova marca de medicamento no banco de dados.
     * Valida se o nome foi preenchido antes de prosseguir.
     */
    private void saveBrand() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Por favor, digite o nome da marca.");
            alert.setTitle("Campo obrigatório");
            alert.setHeaderText(null);
            alert.showAndWait();
            return;
        }
        try {
            MedicineBrandController controller = new MedicineBrandController(conn);
            controller.insert(name);
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Marca cadastrada com sucesso!");
            alert.setTitle("Sucesso");
            alert.setHeaderText(null);
            alert.showAndWait();
            closeWindow();
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erro ao salvar a marca: " + e.getMessage());
            alert.setTitle("Erro");
            alert.setHeaderText(null);
            alert.showAndWait();
        }
    }

    /**
     * Fecha a janela do formulário de cadastro.
     */
    private void closeWindow() {
        Stage stage = (Stage) this.getScene().getWindow();
        stage.close();
    }
}