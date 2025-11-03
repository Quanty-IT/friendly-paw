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
    private Connection conn;
    private BorderPane mainLayout;
    private MedicineView medicineView;

    /**
     * Construtor do formulário de cadastro de novo medicamento.
     * 
     * @param mainLayout Layout principal para navegação
     * @param medicineView View de medicamentos para recarregar dados após salvar
     */
    public MedicineForm(BorderPane mainLayout, MedicineView medicineView) {
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

        setupComponents();
    }

    /**
     * Configura todos os componentes do formulário, incluindo campos e botões.
     * Não retorna valor e não lança exceções.
     */
    private void setupComponents() {
        // Título
        Label titleLabel = new Label("Cadastrar novo medicamento");
        titleLabel.getStyleClass().add("form-title");
        
        // Centraliza o título usando um HBox
        HBox titleBox = new HBox(titleLabel);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setPadding(new Insets(0, 0, 10, 0));

        // Nome
        Label nameLabel = new Label("Nome do Medicamento:");
        nameLabel.getStyleClass().add("form-label");

        nameField = new TextField();
        nameField.setPromptText("Digite o nome do medicamento");
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
        brandComboBox.setPromptText("Selecione uma marca");
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
        quantityField.setPromptText("Digite a quantidade (opcional)");
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
        descriptionField.setPromptText("Digite a descrição (opcional)");
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
        saveButton.setOnAction(e -> saveMedicine());

        Button backButton = new Button("Voltar");
        backButton.getStyleClass().add("form-btn-cancel");
        backButton.setPrefWidth(150);
        backButton.setOnAction(e -> goBack());

        HBox buttonBox = new HBox(15, backButton, saveButton);
        buttonBox.setAlignment(Pos.CENTER);

        VBox formContent = new VBox(20, nameBox, brandQuantityRow, descriptionBox);
        formContent.setAlignment(Pos.CENTER);
        formContent.setPadding(new Insets(10, 0, 10, 0));

        this.getChildren().clear();
        this.getChildren().addAll(titleBox, formContent, buttonBox);
        this.getStylesheets().add(getClass().getResource("/modules/Medicine/styles/MedicineForm.css").toExternalForm());
    }

    /**
     * Carrega a lista de marcas de medicamentos do banco de dados e popula o ComboBox.
     * 
     * @throws SQLException Se ocorrer erro na operação do banco de dados (tratado internamente com Alert)
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
     * Salva um novo medicamento no banco de dados.
     * Valida os campos antes de prosseguir.
     * 
     * @throws SQLException Se ocorrer erro na operação do banco de dados (tratado internamente com Alert)
     * @throws NumberFormatException Se a quantidade não for um número válido (tratado internamente com Alert)
     */
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
        java.util.UUID brandUuid = selectedBrand != null ? selectedBrand.getUuid() : null;
        String description = descriptionField.getText().trim();
        // Medicamentos são criados ativos por padrão
        Boolean isActive = true;

        try {
            MedicineController controller = new MedicineController(conn);
            controller.insert(name, brandUuid, quantity, description, isActive);
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Medicamento cadastrado com sucesso!");
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
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erro ao salvar o medicamento: " + e.getMessage());
            alert.setTitle("Erro");
            alert.setHeaderText(null);
            alert.showAndWait();
        }
    }

    /**
     * Volta para a view de lista de medicamentos.
     * Não retorna valor e não lança exceções.
     */
    private void goBack() {
        if (mainLayout != null && medicineView != null) {
            mainLayout.setCenter(medicineView);
        }
    }
}