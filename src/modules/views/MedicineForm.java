package modules.views;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import modules.controllers.MedicineBrandController;
import modules.controllers.MedicineController;
import modules.models.Medicine;
import modules.models.MedicineBrand;
import config.Database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class MedicineForm extends GridPane {

    private TextField nameField;
    private ComboBox<MedicineBrand> brandComboBox;
    private TextField quantityField;
    private TextArea descriptionField;
    private CheckBox isActiveCheckBox;
    private Connection conn;
    private Medicine medicineToEdit;

    public MedicineForm() {
        this(null);
    }

    public MedicineForm(Medicine medicine) {
        this.medicineToEdit = medicine;
        this.setVgap(10);
        this.setHgap(10);
        this.setPadding(new Insets(20, 20, 20, 20));

        try {
            conn = Database.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        setupComponents();
        if (medicineToEdit != null) {
            populateFields();
        }
    }

    private void setupComponents() {
        Label nameLabel = new Label("Nome do Medicamento:");
        nameField = new TextField();
        nameField.setPrefWidth(240);

        Label brandLabel = new Label("Marca:");
        brandComboBox = new ComboBox<>();
        brandComboBox.setPrefWidth(240);
        loadBrands();

        Label quantityLabel = new Label("Quantidade:");
        quantityField = new TextField();
        quantityField.setPrefWidth(240);

        Label descriptionLabel = new Label("Descrição:");
        descriptionField = new TextArea();
        descriptionField.setPrefSize(240, 80);

        Label activeLabel = new Label("Ativo:");
        isActiveCheckBox = new CheckBox();
        isActiveCheckBox.setSelected(true);

        Button saveButton = new Button("Salvar");
        saveButton.setOnAction(e -> saveMedicine());

        Button cancelButton = new Button("Cancelar");
        cancelButton.setOnAction(e -> closeWindow());

        this.add(nameLabel, 0, 0);
        this.add(nameField, 1, 0);
        this.add(brandLabel, 0, 1);
        this.add(brandComboBox, 1, 1);
        this.add(quantityLabel, 0, 2);
        this.add(quantityField, 1, 2);
        this.add(descriptionLabel, 0, 3);
        this.add(descriptionField, 1, 3);
        this.add(activeLabel, 0, 4);
        this.add(isActiveCheckBox, 1, 4);
        this.add(saveButton, 1, 5);
        this.add(cancelButton, 0, 5);
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
            new Alert(Alert.AlertType.ERROR, "Erro ao carregar marcas: " + e.getMessage()).showAndWait();
        }
    }

    private void populateFields() {
        if (medicineToEdit != null) {
            nameField.setText(medicineToEdit.getName());
            quantityField.setText(medicineToEdit.getQuantity() != null ? medicineToEdit.getQuantity().toString() : "");
            descriptionField.setText(medicineToEdit.getDescription());
            isActiveCheckBox.setSelected(medicineToEdit.getIsActive());

            if (medicineToEdit.getBrandId() != null) {
                brandComboBox.getItems().forEach(brand -> {
                    if (brand.getId().equals(medicineToEdit.getBrandId())) {
                        brandComboBox.setValue(brand);
                    }
                });
            }
        }
    }

    private void saveMedicine() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Digite um nome!").showAndWait();
            return;
        }

        Integer quantity = null;
        try {
            if (!quantityField.getText().trim().isEmpty()) {
                quantity = Integer.parseInt(quantityField.getText().trim());
            }
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.WARNING, "Quantidade deve ser um número válido!").showAndWait();
            return;
        }

        MedicineBrand selectedBrand = brandComboBox.getValue();
        Integer brandId = selectedBrand != null ? selectedBrand.getId() : null;
        String description = descriptionField.getText().trim();
        Boolean isActive = isActiveCheckBox.isSelected();

        try {
            MedicineController controller = new MedicineController(conn);
            if (medicineToEdit == null) {
                controller.insert(name, brandId, quantity, description, isActive);
                new Alert(Alert.AlertType.INFORMATION, "Medicamento cadastrado com sucesso!").showAndWait();
            } else {
                controller.update(medicineToEdit.getId(), name, brandId, quantity, description, isActive);
                new Alert(Alert.AlertType.INFORMATION, "Medicamento editado com sucesso!").showAndWait();
            }
            closeWindow();
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erro ao salvar o medicamento: " + e.getMessage()).showAndWait();
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) this.getScene().getWindow();
        stage.close();
    }
}