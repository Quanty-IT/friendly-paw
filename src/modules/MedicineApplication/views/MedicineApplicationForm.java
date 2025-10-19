package modules.MedicineApplication.views;

import config.Database;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import modules.MedicineApplication.controllers.MedicineApplicationController;
import modules.Medicine.controllers.MedicineController;
import modules.Animal.models.Animal;
import modules.Animal.views.AnimalView;
import modules.Medicine.models.Medicine;
import modules.MedicineApplication.models.MedicineApplication;
import modules.User.models.User;
import utils.Session;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class MedicineApplicationForm extends VBox {

    private final BorderPane mainLayout;
    private final Animal selectedAnimal;
    private final User currentUser;
    private Connection conn;

    private final MedicineController medicineController;
    private final MedicineApplicationController appController = new MedicineApplicationController();

    // Campos do formulário
    private final TextField animalField = new TextField();
    private final ComboBox<Medicine> medicineComboBox = new ComboBox<>();
    private final DatePicker appliedDatePicker = new DatePicker(LocalDate.now());
    private final TextField quantityField = new TextField();
    private final TextField frequencyField = new TextField();
    private final DatePicker nextDatePicker = new DatePicker();
    private final DatePicker endsDatePicker = new DatePicker();
    private final Button saveButton = new Button("Salvar Aplicação");

    public MedicineApplicationForm(BorderPane mainLayout, Animal selectedAnimal) {
        this.mainLayout = mainLayout;
        this.selectedAnimal = selectedAnimal;
        this.currentUser = Session.get(); // Pega o usuário logado da sessão

        try {
            this.conn = Database.getConnection();
        } catch (SQLException e) {
            showErrorAlert("Erro de Conexão", "Não foi possível conectar ao banco de dados.");
        }

        this.medicineController = new MedicineController(this.conn);

        setupUI();
        loadData();
        setupActions();
    }

    private void setupUI() {
        setSpacing(10);
        setPadding(new Insets(20));

        animalField.setText(selectedAnimal.getName());
        animalField.setDisable(true); // O campo do animal não pode ser editado

        quantityField.setPromptText("Ex: 1.5");
        frequencyField.setPromptText("Ex: A cada 8 horas");

        Button backButton = new Button("Voltar");
        backButton.setOnAction(e -> this.mainLayout.setCenter(new AnimalView(this.mainLayout)));

        getChildren().addAll(
                new Label("Animal:"), animalField,
                new Label("Medicamento:"), medicineComboBox,
                new Label("Data da Aplicação:"), appliedDatePicker,
                new Label("Quantidade (Em mmg):"), quantityField,
                new Label("Frequência:"), frequencyField,
                new Label("Próxima Aplicação (opcional):"), nextDatePicker,
                new Label("Fim do Tratamento (opcional):"), endsDatePicker,
                new HBox(10, saveButton, backButton)
        );
    }

    private void loadData() {
        try {
            medicineComboBox.setItems(FXCollections.observableArrayList(medicineController.listAll()));

            // Melhora a exibição do nome do medicamento na ComboBox
            medicineComboBox.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(Medicine item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getName());
                    }
                }
            });
            medicineComboBox.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Medicine item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getName());
                    }
                }
            });

        } catch (SQLException e) {
            showErrorAlert("Falha ao Carregar Dados", "Não foi possível carregar a lista de medicamentos.");
        }
    }

    private void setupActions() {
        saveButton.setOnAction(event -> {
            try {
                Medicine selectedMedicine = medicineComboBox.getValue();

                if (selectedMedicine == null) {
                    showErrorAlert("Dados Incompletos", "Por favor, selecione um medicamento.");
                    return;
                }

                MedicineApplication newApp = new MedicineApplication();
                newApp.setAnimalUuid(selectedAnimal.getId());
                newApp.setMedicineId(selectedMedicine.getId());
                newApp.setUserUuid(currentUser.getId());

                LocalDate localDate = appliedDatePicker.getValue();
                newApp.setAppliedAt(ZonedDateTime.of(localDate, LocalTime.now(), ZoneId.systemDefault()));
                newApp.setQuantity(new BigDecimal(quantityField.getText().replace(',', '.'))); // Aceita vírgula e ponto
                newApp.setFrequency(frequencyField.getText());

                if (nextDatePicker.getValue() != null) {
                    newApp.setNextApplicationAt(ZonedDateTime.of(nextDatePicker.getValue(), LocalTime.MIDNIGHT, ZoneId.systemDefault()));
                }
                if (endsDatePicker.getValue() != null) {
                    newApp.setEndsAt(ZonedDateTime.of(endsDatePicker.getValue(), LocalTime.MIDNIGHT, ZoneId.systemDefault()));
                }

                appController.create(newApp);

                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Aplicação registrada com sucesso!", ButtonType.OK);
                alert.setHeaderText("Sucesso");
                alert.showAndWait();

                // Volta para a tela de animais
                this.mainLayout.setCenter(new AnimalView(this.mainLayout));

            } catch (NumberFormatException e) {
                showErrorAlert("Formato Inválido", "A quantidade deve ser um número válido (ex: 1.5).");
            } catch (Exception e) {
                showErrorAlert("Erro ao Salvar", "Ocorreu um erro ao registrar a aplicação: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}