package modules.MedicineBrand.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import modules.MedicineBrand.controllers.MedicineBrandController;
import modules.MedicineBrand.models.MedicineBrand;

import java.sql.SQLException;

public class MedicineBrandEditForm extends VBox {

    private TextField nameField;
    private MedicineBrand brand;
    private MedicineBrandController controller;

    public MedicineBrandEditForm(MedicineBrand brand, MedicineBrandController controller) {
        this.brand = brand;
        this.controller = controller;

        this.setSpacing(20);
        this.setPadding(new Insets(30, 40, 30, 40));
        this.setAlignment(Pos.CENTER);
        this.getStyleClass().add("form-bg");

        Label titleLabel = new Label("Editar marca");
        titleLabel.getStyleClass().add("form-title");

        Label nameLabel = new Label("Nome da marca:");
        nameLabel.getStyleClass().add("form-label");

        nameField = new TextField(brand.getName());
        nameField.setPrefWidth(350);
        nameField.getStyleClass().add("form-input");

        Button saveButton = new Button("Salvar alterações");
        saveButton.getStyleClass().add("form-btn-save");
        saveButton.setPrefWidth(150);
        saveButton.setOnAction(e -> updateBrand());

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
        this.getStylesheets().add(getClass().getResource("/modules/MedicineBrand/styles/MedicineBrandEditForm.css").toExternalForm());
    }

    private void updateBrand() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Por favor, digite um nome para a marca!");
            alert.setTitle("Campo obrigatório");
            alert.setHeaderText(null);
            alert.showAndWait();
            return;
        }
        try {
            controller.update(brand.getUuid(), name);
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Marca editada com sucesso!");
            alert.setTitle("Sucesso");
            alert.setHeaderText(null);
            alert.showAndWait();
            closeWindow();
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erro ao editar a marca: " + e.getMessage());
            alert.setTitle("Erro");
            alert.setHeaderText(null);
            alert.showAndWait();
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) this.getScene().getWindow();
        stage.close();
    }
}