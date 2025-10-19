package modules.Medicine.views;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import modules.MedicineBrand.controllers.MedicineBrandController;
import modules.Medicine.controllers.MedicineController;
import modules.MedicineBrand.models.MedicineBrand;
import config.Database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class MedicineForm extends VBox {

    private TextField nameField;
    private ComboBox<MedicineBrand> brandComboBox;
    private TextField quantityField;
    private TextArea descriptionField;
    private CheckBox isActiveCheckBox;
    private Connection conn;

    public MedicineForm() {
        this.setSpacing(20);
        this.setPadding(new Insets(30, 40, 30, 40));
        this.setAlignment(Pos.CENTER);
        this.getStyleClass().add("form-bg");

        Label titleLabel = new Label("Cadastrar novo medicamento");
        titleLabel.getStyleClass().add("form-title");

        try {
            conn = Database.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        setupComponents();
    }

    private void setupComponents() {
        // Nome
        Label nameLabel = new Label("Nome do Medicamento:");
        nameLabel.getStyleClass().add("form-label");

        nameField = new TextField();
        nameField.setPromptText("Digite o nome do medicamento");
        nameField.setPrefWidth(400);
        nameField.getStyleClass().add("form-input");

        VBox nameBox = new VBox(8, nameLabel, nameField);
        nameBox.setAlignment(Pos.CENTER_LEFT);

        // Marca
        Label brandLabel = new Label("Marca:");
        brandLabel.getStyleClass().add("form-label");

        brandComboBox = new ComboBox<>();
        brandComboBox.setPromptText("Selecione uma marca");
        brandComboBox.setPrefWidth(400);
        brandComboBox.getStyleClass().add("combo-box");
        loadBrands();

        VBox brandBox = new VBox(8, brandLabel, brandComboBox);
        brandBox.setAlignment(Pos.CENTER_LEFT);

        // Quantidade
        Label quantityLabel = new Label("Quantidade:");
        quantityLabel.getStyleClass().add("form-label");

        quantityField = new TextField();
        quantityField.setPromptText("Digite a quantidade");
        quantityField.setPrefWidth(400);
        quantityField.getStyleClass().add("form-input");

        VBox quantityBox = new VBox(8, quantityLabel, quantityField);
        quantityBox.setAlignment(Pos.CENTER_LEFT);

        // Descrição
        Label descriptionLabel = new Label("Descrição:");
        descriptionLabel.getStyleClass().add("form-label");

        descriptionField = new TextArea();
        descriptionField.setPromptText("Digite a descrição (opcional)");
        descriptionField.setPrefSize(400, 100);
        descriptionField.setWrapText(true);
        descriptionField.getStyleClass().add("text-area");

        VBox descriptionBox = new VBox(8, descriptionLabel, descriptionField);
        descriptionBox.setAlignment(Pos.CENTER_LEFT);

        // Ativo
        Label activeLabel = new Label("Medicamento ativo:");
        activeLabel.getStyleClass().add("form-label");

        isActiveCheckBox = new CheckBox("Sim");
        isActiveCheckBox.setSelected(true);
        isActiveCheckBox.getStyleClass().add("check-box");

        HBox activeBox = new HBox(15, activeLabel, isActiveCheckBox);
        activeBox.setAlignment(Pos.CENTER_LEFT);

        // Botões
        Button saveButton = new Button("Salvar");
        saveButton.getStyleClass().add("form-btn-save");
        saveButton.setPrefWidth(150);
        saveButton.setOnAction(e -> saveMedicine());

        Button cancelButton = new Button("Cancelar");
        cancelButton.getStyleClass().add("form-btn-cancel");
        cancelButton.setPrefWidth(150);
        cancelButton.setOnAction(e -> closeWindow());

        HBox buttonBox = new HBox(15, cancelButton, saveButton);
        buttonBox.setAlignment(Pos.CENTER);

        VBox formContent = new VBox(15, nameBox, brandBox, quantityBox, descriptionBox, activeBox);
        formContent.setAlignment(Pos.CENTER_LEFT);
        formContent.setPadding(new Insets(10, 0, 10, 0));

        Label titleLabel = new Label("Cadastrar novo medicamento");
        titleLabel.getStyleClass().add("form-title");

        this.getChildren().clear();
        this.getChildren().addAll(titleLabel, formContent, buttonBox);
        this.getStylesheets().add(getClass().getResource("/modules/Medicine/styles/MedicineForm.css").toExternalForm());
    }

    private void loadBrands() {
        try {
            MedicineBrandController brandController = new MedicineBrandController(conn);
            List<MedicineBrand> brands = brandController.listAll();
            ObservableList<MedicineBrand> brandsList = FXCollections.observableArrayList(brands);
            brandComboBox.setItems(brandsList);

            brandComboBox.setCellFactory(param -> new ListCell<MedicineBrand>() {
                @Override
                protected void updateItem(MedicineBrand item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getName());
                    }
                }
            });

            brandComboBox.setButtonCell(new ListCell<MedicineBrand>() {
                @Override
                protected void updateItem(MedicineBrand item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getName());
                    }
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erro ao carregar marcas: " + e.getMessage());
            alert.setTitle("Erro");
            alert.setHeaderText(null);
            alert.showAndWait();
        }
    }

    private void saveMedicine() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Por favor, digite o nome do medicamento.");
            alert.setTitle("Campo obrigatório");
            alert.setHeaderText(null);
            alert.showAndWait();
            return;
        }

        Integer quantity = null;
        try {
            if (!quantityField.getText().trim().isEmpty()) {
                quantity = Integer.parseInt(quantityField.getText().trim());
            }
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Quantidade deve ser um número válido!");
            alert.setTitle("Erro de validação");
            alert.setHeaderText(null);
            alert.showAndWait();
            return;
        }

        MedicineBrand selectedBrand = brandComboBox.getValue();
        Integer brandId = selectedBrand != null ? selectedBrand.getId() : null;
        String description = descriptionField.getText().trim();
        Boolean isActive = isActiveCheckBox.isSelected();

        try {
            MedicineController controller = new MedicineController(conn);
            controller.insert(name, brandId, quantity, description, isActive);
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Medicamento cadastrado com sucesso!");
            alert.setTitle("Sucesso");
            alert.setHeaderText(null);
            alert.showAndWait();
            closeWindow();
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erro ao salvar o medicamento: " + e.getMessage());
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