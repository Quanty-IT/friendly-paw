package modules.Medicine.views;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import modules.MedicineBrand.controllers.MedicineBrandController;
import modules.Medicine.controllers.MedicineController;
import modules.Medicine.models.Medicine;
import modules.Medicine.views.MedicineView;
import modules.MedicineBrand.models.MedicineBrand;
import config.Database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class MedicineEditForm extends VBox {

    private TextField nameField;
    private ComboBox<MedicineBrand> brandComboBox;
    private TextField quantityField;
    private TextArea descriptionField;
    private Medicine medicine;
    private MedicineController controller;
    private Connection conn;
    private BorderPane mainLayout;
    private MedicineView medicineView;

    /**
     * Construtor do formulário de edição de medicamento.
     * 
     * @param medicine Medicamento a ser editado
     * @param controller Controller responsável pelas operações de medicamento
     * @param mainLayout Layout principal para navegação
     * @param medicineView View de medicamentos para recarregar dados após salvar
     */
    public MedicineEditForm(Medicine medicine, MedicineController controller, BorderPane mainLayout, MedicineView medicineView) {
        this.medicine = medicine;
        this.controller = controller;
        this.mainLayout = mainLayout;
        this.medicineView = medicineView;

        this.setSpacing(20);
        this.setPadding(new Insets(30, 40, 30, 40));
        this.setAlignment(Pos.CENTER);
        this.getStyleClass().add("form-bg");

        try {
            conn = Database.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Label titleLabel = new Label("Editar medicamento");
        titleLabel.getStyleClass().add("form-title");
        
        // Centraliza o título usando um HBox
        HBox titleBox = new HBox(titleLabel);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setPadding(new Insets(0, 0, 10, 0));

        setupComponents();
        populateFields();

        this.getChildren().add(0, titleBox);
    }

    /**
     * Configura todos os componentes do formulário, incluindo campos e botões.
     */
    private void setupComponents() {
        // Nome
        Label nameLabel = new Label("Nome do Medicamento:");
        nameLabel.getStyleClass().add("form-label");

        nameField = new TextField();
        // Largura igual a Marca (280) + Quantidade (280) + espaçamento (20) = 580
        nameField.setPrefWidth(580);
        nameField.setMaxWidth(580);
        nameField.getStyleClass().add("form-input");

        // Container fixo com largura do input - label à esquerda, input centralizado
        HBox nameInputWrapper = new HBox(nameField);
        nameInputWrapper.setAlignment(Pos.CENTER);
        nameInputWrapper.setPrefWidth(580);
        nameInputWrapper.setMaxWidth(580);
        
        VBox nameBox = new VBox(8, nameLabel, nameInputWrapper);
        nameBox.setPrefWidth(580);
        nameBox.setMaxWidth(580);
        nameBox.setAlignment(Pos.CENTER_LEFT);

        // Marca
        Label brandLabel = new Label("Marca:");
        brandLabel.getStyleClass().add("form-label");

        brandComboBox = new ComboBox<>();
        brandComboBox.setPrefWidth(280);
        brandComboBox.setMaxWidth(280);
        brandComboBox.getStyleClass().add("combo-box");
        loadBrands();

        // Container fixo com largura do input - label à esquerda, input centralizado
        HBox brandInputWrapper = new HBox(brandComboBox);
        brandInputWrapper.setAlignment(Pos.CENTER);
        brandInputWrapper.setPrefWidth(280);
        brandInputWrapper.setMaxWidth(280);
        
        VBox brandBox = new VBox(8, brandLabel, brandInputWrapper);
        brandBox.setPrefWidth(280);
        brandBox.setMaxWidth(280);
        brandBox.setAlignment(Pos.CENTER_LEFT);

        // Quantidade
        Label quantityLabel = new Label("Quantidade:");
        quantityLabel.getStyleClass().add("form-label");

        quantityField = new TextField();
        quantityField.setPrefWidth(280);
        quantityField.setMaxWidth(280);
        quantityField.getStyleClass().add("form-input");

        // Container fixo com largura do input - label à esquerda, input centralizado
        HBox quantityInputWrapper = new HBox(quantityField);
        quantityInputWrapper.setAlignment(Pos.CENTER);
        quantityInputWrapper.setPrefWidth(280);
        quantityInputWrapper.setMaxWidth(280);
        
        VBox quantityBox = new VBox(8, quantityLabel, quantityInputWrapper);
        quantityBox.setPrefWidth(280);
        quantityBox.setMaxWidth(280);
        quantityBox.setAlignment(Pos.CENTER_LEFT);

        // Marca e Quantidade na mesma linha
        HBox brandQuantityRow = new HBox(20, brandBox, quantityBox);
        brandQuantityRow.setAlignment(Pos.CENTER);

        // Descrição
        Label descriptionLabel = new Label("Descrição:");
        descriptionLabel.getStyleClass().add("form-label");

        descriptionField = new TextArea();
        // Largura igual a Marca (280) + Quantidade (280) + espaçamento (20) = 580
        descriptionField.setPrefWidth(580);
        descriptionField.setMaxWidth(580);
        descriptionField.setPrefHeight(120);
        descriptionField.setWrapText(true);
        descriptionField.getStyleClass().add("form-textarea");

        // Container fixo com largura do input - label à esquerda, input centralizado
        HBox descriptionInputWrapper = new HBox(descriptionField);
        descriptionInputWrapper.setAlignment(Pos.CENTER);
        descriptionInputWrapper.setPrefWidth(580);
        descriptionInputWrapper.setMaxWidth(580);
        
        VBox descriptionBox = new VBox(8, descriptionLabel, descriptionInputWrapper);
        descriptionBox.setPrefWidth(580);
        descriptionBox.setMaxWidth(580);
        descriptionBox.setAlignment(Pos.CENTER_LEFT);

        // Botões
        Button saveButton = new Button("Salvar");
        saveButton.getStyleClass().add("form-btn-save");
        saveButton.setPrefWidth(150);
        saveButton.setOnAction(e -> updateMedicine());

        Button backButton = new Button("Voltar");
        backButton.getStyleClass().add("form-btn-cancel");
        backButton.setPrefWidth(150);
        backButton.setOnAction(e -> goBack());

        HBox buttonBox = new HBox(15, backButton, saveButton);
        buttonBox.setAlignment(Pos.CENTER);

        VBox formContent = new VBox(20, nameBox, brandQuantityRow, descriptionBox);
        formContent.setAlignment(Pos.CENTER);
        formContent.setPadding(new Insets(10, 0, 10, 0));

        this.getChildren().addAll(formContent, buttonBox);
        this.getStylesheets().add(getClass().getResource("/modules/Medicine/styles/MedicineEditForm.css").toExternalForm());
    }

    /**
     * Carrega a lista de marcas de medicamentos do banco de dados e popula o ComboBox.
     */
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

    /**
     * Pré-preenche os campos do formulário com os dados do medicamento sendo editado.
     */
    private void populateFields() {
        if (medicine != null) {
            nameField.setText(medicine.getName());
            // Se quantity for -1, deixar em branco; caso contrário, mostrar o valor
            if (medicine.getQuantity() != null && medicine.getQuantity() != -1) {
                quantityField.setText(medicine.getQuantity().toString());
            } else {
                quantityField.setText("");
            }
            descriptionField.setText(medicine.getDescription() != null ? medicine.getDescription() : "");

            if (medicine.getBrandUuid() != null) {
                brandComboBox.getItems().forEach(brand -> {
                    if (brand.getUuid().equals(medicine.getBrandUuid())) {
                        brandComboBox.setValue(brand);
                    }
                });
            }
        }
    }

    /**
     * Atualiza os dados do medicamento no banco de dados.
     * Valida os campos antes de prosseguir.
     */
    private void updateMedicine() {
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
        java.util.UUID brandUuid = selectedBrand != null ? selectedBrand.getUuid() : null;
        String description = descriptionField.getText().trim();
        // O status ativo/inativo só pode ser alterado através do botão na tabela
        Boolean isActive = medicine.getIsActive();

        try {
            controller.update(medicine.getUuid(), name, brandUuid, quantity, description, isActive);
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Medicamento editado com sucesso!");
            alert.setTitle("Sucesso");
            alert.setHeaderText(null);
            alert.showAndWait();
            // Recarrega os dados e volta para a view
            if (medicineView != null) {
                medicineView.loadData();
            }
            goBack();
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erro ao editar o medicamento: " + e.getMessage());
            alert.setTitle("Erro");
            alert.setHeaderText(null);
            alert.showAndWait();
        }
    }

    /**
     * Volta para a view de lista de medicamentos.
     */
    private void goBack() {
        if (mainLayout != null && medicineView != null) {
            mainLayout.setCenter(medicineView);
        }
    }
}