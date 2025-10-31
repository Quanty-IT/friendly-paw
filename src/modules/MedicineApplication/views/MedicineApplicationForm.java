package modules.MedicineApplication.views;

import config.Database;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
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
    private final ComboBox<MedicineApplication.Frequency> frequencyComboBox = new ComboBox<>();
    private final DatePicker nextDatePicker = new DatePicker();
    private final DatePicker endsDatePicker = new DatePicker();
    private final ComboBox<String> googleCalendarCombo = new ComboBox<>();
    private final Button saveButton = new Button("Salvar");
    private final Button backButton = new Button("Voltar");

    public MedicineApplicationForm(BorderPane mainLayout, Animal selectedAnimal) {
        this.mainLayout = mainLayout;
        this.selectedAnimal = selectedAnimal;
        this.currentUser = Session.get();

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

    // =========================
    // UI
    // =========================
    private void setupUI() {
        // estilo base (igual AnimalForm/MedicineForm)
        getStyleClass().add("form-bg");
        setPadding(new Insets(30, 40, 30, 40));
        setSpacing(18);
        setAlignment(Pos.TOP_CENTER);
        getStylesheets().add(getClass().getResource("/modules/MedicineApplication/styles/MedicineApplicationForm.css").toExternalForm());

        // Título
        Label title = new Label("Aplicar medicamento");
        title.getStyleClass().add("form-title");
        HBox titleBox = new HBox(title);
        titleBox.setAlignment(Pos.CENTER);

        // Larguras padrão
        double HALF = 280, FULL = 580; double THIRD = 180;

        // Animal (read-only)
        animalField.setText(selectedAnimal.getName());
        animalField.setEditable(false);
        animalField.setPrefWidth(FULL);
        animalField.getStyleClass().add("form-input-readonly");

        // Medicamento
        medicineComboBox.setPromptText("Selecione um medicamento");
        medicineComboBox.setPrefWidth(HALF);
        medicineComboBox.getStyleClass().add("combo-box");

        // Data de aplicação
        appliedDatePicker.setPrefWidth(HALF);
        appliedDatePicker.getStyleClass().add("date-picker");

        // Quantidade
        quantityField.setPromptText("Ex: 1.5");
        quantityField.setPrefWidth(THIRD);
        quantityField.getStyleClass().add("form-input");

        // Google Calendar (Combo "Sim/Não")
        googleCalendarCombo.getItems().addAll("Sim", "Não");
        googleCalendarCombo.setPrefWidth(THIRD);
        googleCalendarCombo.getStyleClass().add("combo-box");
        googleCalendarCombo.setValue("Não");

        // Frequência
        frequencyComboBox.setPrefWidth(THIRD);
        frequencyComboBox.getStyleClass().add("combo-box");
        frequencyComboBox.getItems().addAll(MedicineApplication.Frequency.values());
        frequencyComboBox.setPromptText("Selecione a frequência");
        frequencyComboBox.setValue(MedicineApplication.Frequency.DOES_NOT_REPEAT);
        frequencyComboBox.setDisable(true);
        frequencyComboBox.setCellFactory(param -> new ListCell<>() {
            @Override protected void updateItem(MedicineApplication.Frequency item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getDisplayName());
            }
        });
        frequencyComboBox.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(MedicineApplication.Frequency item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getDisplayName());
            }
        });

        // Próxima aplicação / Fim do tratamento
        nextDatePicker.setDisable(true);
        endsDatePicker.setDisable(true);
        nextDatePicker.setPrefWidth(HALF);
        endsDatePicker.setPrefWidth(HALF);
        nextDatePicker.getStyleClass().add("date-picker");
        endsDatePicker.getStyleClass().add("date-picker");

        // Botões
        backButton.getStyleClass().add("form-btn-cancel");
        backButton.setPrefWidth(150);
        saveButton.getStyleClass().add("form-btn-save");
        saveButton.setPrefWidth(150);

        // Layout (helpers iguais ao AnimalForm: blocos com label acima + wrapper centralizado)
        VBox form = new VBox(20);
        form.setAlignment(Pos.CENTER);

        form.getChildren().setAll(
                titleBox,
                rowFull("Animal:", animalField, FULL),
                rowHalf("Medicamento:", medicineComboBox, "Data da aplicação:", appliedDatePicker, HALF),

                // 👇 NOVA LINHA COM 3 COLUNAS
                rowThird(
                        "Quantidade (em mg):",        quantityField,
                        "Agendar no Google Calendar:", googleCalendarCombo,
                        "Frequência:",                 frequencyComboBox,
                        THIRD
                ),

                // 👇 LINHA ABAIXO COM 2 COLUNAS
                rowHalf("Próxima aplicação (opcional):", nextDatePicker, "Fim do tratamento (opcional):", endsDatePicker, HALF)
        );

        HBox buttons = new HBox(15, backButton, saveButton);
        buttons.setAlignment(Pos.CENTER);

        getChildren().setAll(form, buttons);
    }

    // Helpers visuais
    private VBox rowFull(String labelText, Control field, double width) {
        Label label = new Label(labelText);
        label.getStyleClass().add("form-label");

        HBox wrapper = new HBox(field);
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setPrefWidth(width);
        wrapper.setMaxWidth(width);

        VBox box = new VBox(8, label, wrapper);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPrefWidth(width);
        box.setMaxWidth(width);

        // Checkbox alinhado à esquerda se aparecer em full
        if (field instanceof CheckBox cb) {
            wrapper.setAlignment(Pos.CENTER_LEFT);
            cb.setMinWidth(24);
        }
        return box;
    }

    private HBox rowHalf(String l1, Control f1, String l2, Control f2, double width) {
        VBox left = labeledBox(l1, f1, width);
        VBox right = labeledBox(l2, f2, width);
        HBox row = new HBox(20, left, right);
        row.setAlignment(Pos.CENTER);
        return row;
    }

    private VBox labeledBox(String labelText, Control field, double width) {
        Label label = new Label(labelText);
        label.getStyleClass().add("form-label");

        HBox wrapper = new HBox(field);
        wrapper.setAlignment(field instanceof CheckBox ? Pos.CENTER_LEFT : Pos.CENTER);
        wrapper.setPrefWidth(width);
        wrapper.setMaxWidth(width);

        // larguras padrão para consistência
        if (!(field instanceof DatePicker) && !(field instanceof ComboBox) && !(field instanceof CheckBox)) {
            field.setPrefWidth(width);
        }

        VBox box = new VBox(8, label, wrapper);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPrefWidth(width);
        box.setMaxWidth(width);
        return box;
    }

    private HBox rowThird(String l1, Control f1, String l2, Control f2, String l3, Control f3, double widthEach) {
        VBox c1 = labeledBox(l1, f1, widthEach);
        VBox c2 = labeledBox(l2, f2, widthEach);
        VBox c3 = labeledBox(l3, f3, widthEach);

        HBox row = new HBox(20, c1, c2, c3);
        row.setAlignment(Pos.CENTER);
        return row;
    }


    // =========================
    // Dados e ações
    // =========================
    private void loadData() {
        try {
            medicineComboBox.setItems(FXCollections.observableArrayList(medicineController.listAll()));
            medicineComboBox.setCellFactory(param -> new ListCell<>() {
                @Override protected void updateItem(Medicine item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getName());
                }
            });
            medicineComboBox.setButtonCell(new ListCell<>() {
                @Override protected void updateItem(Medicine item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getName());
                }
            });
        } catch (SQLException e) {
            showErrorAlert("Falha ao Carregar Dados", "Não foi possível carregar a lista de medicamentos.");
        }
    }

    private void setupActions() {
        // toggle de recorrência conforme Google Calendar
        frequencyComboBox.setOnAction(e -> {
            boolean useGC = "Sim".equals(googleCalendarCombo.getValue());
            if (!useGC) {
                // se não for para usar o GC, mantém tudo travado
                nextDatePicker.setDisable(true);
                endsDatePicker.setDisable(true);
                return;
            }
            var f = frequencyComboBox.getValue();
            boolean isRecurring = f != null && f.isRecurring();
            nextDatePicker.setDisable(!isRecurring);
            endsDatePicker.setDisable(!isRecurring);
        });

        googleCalendarCombo.setOnAction(e -> {
            boolean useGC = "Sim".equals(googleCalendarCombo.getValue());
            frequencyComboBox.setDisable(!useGC);

            if (!useGC) {
                frequencyComboBox.setValue(MedicineApplication.Frequency.DOES_NOT_REPEAT);
                nextDatePicker.setValue(null);
                endsDatePicker.setValue(null);
                nextDatePicker.setDisable(true);
                endsDatePicker.setDisable(true);
            } else {
                boolean isRecurring = frequencyComboBox.getValue() != null && frequencyComboBox.getValue().isRecurring();
                nextDatePicker.setDisable(!isRecurring);
                endsDatePicker.setDisable(!isRecurring);
            }
        });

        backButton.setOnAction(e -> this.mainLayout.setCenter(new AnimalView(this.mainLayout)));

        saveButton.setOnAction(event -> {
            try {
                Medicine selectedMedicine = medicineComboBox.getValue();
                if (selectedMedicine == null) {
                    showErrorAlert("Dados Incompletos", "Por favor, selecione um medicamento.");
                    return;
                }

                MedicineApplication newApp = new MedicineApplication();
                newApp.setAnimalUuid(selectedAnimal.getUuid());
                newApp.setMedicineUuid(selectedMedicine.getUuid());
                newApp.setUserUuid(currentUser.getUuid());

                LocalDate localDate = appliedDatePicker.getValue();
                newApp.setAppliedAt(ZonedDateTime.of(localDate, LocalTime.now(), ZoneId.systemDefault()));

                String qtyText = quantityField.getText() == null ? "" : quantityField.getText().trim();
                if (qtyText.isEmpty()) {
                    showErrorAlert("Dados Incompletos", "Informe a quantidade (ex: 1.5).");
                    return;
                }
                newApp.setQuantity(new BigDecimal(qtyText.replace(',', '.')));

                MedicineApplication.Frequency frequency = frequencyComboBox.getValue();
                newApp.setFrequency(frequency != null ? frequency : MedicineApplication.Frequency.DOES_NOT_REPEAT);

                if (nextDatePicker.getValue() != null) {
                    newApp.setNextApplicationAt(ZonedDateTime.of(nextDatePicker.getValue(), LocalTime.MIDNIGHT, ZoneId.systemDefault()));
                }
                if (endsDatePicker.getValue() != null) {
                    newApp.setEndsAt(ZonedDateTime.of(endsDatePicker.getValue(), LocalTime.MIDNIGHT, ZoneId.systemDefault()));
                }

                if ("Sim".equals(googleCalendarCombo.getValue())) {
                    try {
                        ZonedDateTime startDateTime =
                                newApp.getNextApplicationAt() != null ? newApp.getNextApplicationAt() : newApp.getAppliedAt();

                        modules.MedicineApplication.services.GoogleCalendarService.createMedicineApplicationEvent(
                                selectedAnimal.getName(),
                                selectedMedicine.getName(),
                                newApp.getQuantity().toString(),
                                startDateTime,
                                frequency,
                                newApp.getEndsAt()
                        );
                    } catch (Exception calendarException) {
                        showErrorAlert("Erro no Google Calendar",
                                "Não foi possível criar o evento no Google Calendar: " + calendarException.getMessage());
                        calendarException.printStackTrace();
                    }
                }

                appController.create(newApp);

                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Aplicação registrada com sucesso!", ButtonType.OK);
                alert.setHeaderText("Sucesso");
                alert.showAndWait();

                this.mainLayout.setCenter(new AnimalView(this.mainLayout));

            } catch (NumberFormatException e) {
                showErrorAlert("Formato Inválido", "A quantidade deve ser um número válido (ex: 1.5).");
            } catch (Exception e) {
                showErrorAlert("Erro ao Salvar", "Ocorreu um erro ao registrar a aplicação: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    // =========================
    // Alerts
    // =========================
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
