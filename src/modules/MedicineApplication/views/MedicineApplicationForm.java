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

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javafx.util.StringConverter;

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

    // Locale/formatter pt-BR
    private static final Locale PT_BR = new Locale("pt", "BR");
    private static final DateTimeFormatter BR_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Construtor do formulário de aplicação de medicamento.
     * 
     * @param mainLayout Layout principal para navegação
     * @param selectedAnimal Animal selecionado
     * @throws SQLException Se ocorrer erro na operação do banco de dados
     * @throws IOException Se ocorrer erro na operação do Google Calendar
     */
    public MedicineApplicationForm(BorderPane mainLayout, Animal selectedAnimal) {
        this.mainLayout = mainLayout;
        this.selectedAnimal = selectedAnimal;
        this.currentUser = Session.get();

        // Garante datas e nomes em pt-BR (mês/dia no popup do calendário)
        Locale.setDefault(PT_BR);

        try {
            this.conn = Database.getConnection();
        } catch (SQLException e) {
            showErrorAlert("Erro de Conexão", "Não foi possível conectar ao banco de dados.");
        }

        this.medicineController = new MedicineController(this.conn);

        setupUI();
        localizeDatePicker(appliedDatePicker);
        localizeDatePicker(nextDatePicker);
        localizeDatePicker(endsDatePicker);
        // >>> Regras de datas
        setupDateConstraints();
        loadData();
        setupActions();
    }

    /**
     * Configura a interface do formulário.
     */
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
        appliedDatePicker.setEditable(false); // opcional: evitar digitação livre

        // Quantidade
        quantityField.setPromptText("Digite a quantidade");
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
        nextDatePicker.setEditable(false); // opcional
        endsDatePicker.setEditable(false); // opcional

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
                        "Quantidade:",        quantityField,
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

    // Converte/mostra datas no formato BR e define placeholder
    private void localizeDatePicker(DatePicker dp) {
        dp.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                return date != null ? BR_DATE.format(date) : "";
            }
            @Override
            public LocalDate fromString(String str) {
                if (str == null || str.trim().isEmpty()) return null;
                return LocalDate.parse(str, BR_DATE);
            }
        });
        dp.setPromptText("dd/MM/aaaa");
    }

    // >>> Regras de data para os DatePickers
    private void setupDateConstraints() {
        final LocalDate today = LocalDate.now();

        // 1) Data da aplicação: só hoje e passadas (de hoje pra trás)
        appliedDatePicker.setDayCellFactory(dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) return;
                setDisable(date.isAfter(today)); // desabilita futuro
            }
        });

        // 2) Próxima aplicação: apenas datas após o dia atual
        nextDatePicker.setDayCellFactory(dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) return;
                setDisable(!date.isAfter(today)); // precisa ser > hoje
            }
        });

        // 3) Fim do tratamento: apenas datas após a "próxima aplicação"
        //    (se próxima não estiver setada, considere hoje como base)
        endsDatePicker.setDayCellFactory(dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) return;
                LocalDate base = nextDatePicker.getValue() != null ? nextDatePicker.getValue() : today;
                setDisable(!date.isAfter(base)); // precisa ser > próxima aplicação
            }
        });

        // Quando mudar a "próxima aplicação", refaça o filtro do "fim"
        nextDatePicker.valueProperty().addListener((obs, oldV, newV) -> {
            // se já havia um fim escolhido inválido com a nova base, limpe
            if (endsDatePicker.getValue() != null && newV != null && !endsDatePicker.getValue().isAfter(newV)) {
                endsDatePicker.setValue(null);
            }
            // reconstroi a fábrica para refletir a nova base
            endsDatePicker.setDayCellFactory(dp -> new DateCell() {
                @Override
                public void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    if (empty || date == null) return;
                    LocalDate base = newV != null ? newV : today;
                    setDisable(!date.isAfter(base));
                }
            });
        });
    }

    /**
     * Cria uma linha completa no formulário com label e campo de largura total.
     *
     * @param labelText Texto do label
     * @param field Campo de controle
     * @param width Largura total do campo
     * @return VBox configurado com label e campo
     */
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

        if (field instanceof CheckBox cb) {
            wrapper.setAlignment(Pos.CENTER_LEFT);
            cb.setMinWidth(24);
        }
        return box;
    }

    /**
     * Cria uma linha no formulário com dois campos lado a lado.
     *
     * @param l1 Texto do label do primeiro campo
     * @param f1 Primeiro campo de controle
     * @param l2 Texto do label do segundo campo
     * @param f2 Segundo campo de controle
     * @param width Largura de cada campo (metade da largura total)
     * @return HBox configurado com os dois campos lado a lado
     */
    private HBox rowHalf(String l1, Control f1, String l2, Control f2, double width) {
        VBox left = labeledBox(l1, f1, width);
        VBox right = labeledBox(l2, f2, width);
        HBox row = new HBox(20, left, right);
        row.setAlignment(Pos.CENTER);
        return row;
    }

    /**
     * Cria um VBox com label e campo de controle para uso em layouts de formulário.
     *
     * @param labelText Texto do label
     * @param field Campo de controle
     * @param width Largura do campo
     * @return VBox configurado com label e campo
     */
    private VBox labeledBox(String labelText, Control field, double width) {
        Label label = new Label(labelText);
        label.getStyleClass().add("form-label");

        HBox wrapper = new HBox(field);
        wrapper.setAlignment(field instanceof CheckBox ? Pos.CENTER_LEFT : Pos.CENTER);
        wrapper.setPrefWidth(width);
        wrapper.setMaxWidth(width);

        if (!(field instanceof DatePicker) && !(field instanceof ComboBox) && !(field instanceof CheckBox)) {
            field.setPrefWidth(width);
        }

        VBox box = new VBox(8, label, wrapper);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPrefWidth(width);
        box.setMaxWidth(width);
        return box;
    }

    /**
     * Cria uma linha no formulário com três campos lado a lado.
     *
     * @param l1 Texto do label do primeiro campo
     * @param f1 Primeiro campo de controle
     * @param l2 Texto do label do segundo campo
     * @param f2 Segundo campo de controle
     * @param l3 Texto do label do terceiro campo
     * @param f3 Terceiro campo de controle
     * @param widthEach Largura de cada campo
     * @return HBox configurado com os três campos lado a lado
     */
    private HBox rowThird(String l1, Control f1, String l2, Control f2, String l3, Control f3, double widthEach) {
        VBox c1 = labeledBox(l1, f1, widthEach);
        VBox c2 = labeledBox(l2, f2, widthEach);
        VBox c3 = labeledBox(l3, f3, widthEach);

        HBox row = new HBox(20, c1, c2, c3);
        row.setAlignment(Pos.CENTER);
        return row;
    }

    /**
     * Carrega a lista de medicamentos do banco de dados e popula o ComboBox.
     *
     * @throws SQLException Se ocorrer erro na operação do banco de dados (tratado internamente com Alert)
     */
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

    /**
     * Configura os event handlers dos botões e campos do formulário.
     * Inclui lógica para habilitar/desabilitar campos baseado na seleção de Google Calendar e frequência.
     */
    private void setupActions() {
        // toggle de recorrência conforme Google Calendar
        frequencyComboBox.setOnAction(e -> {
            boolean useGC = "Sim".equals(googleCalendarCombo.getValue());
            if (!useGC) {
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

        backButton.setOnAction(e ->
            // volta para a listagem (novo fluxo)
            this.mainLayout.setCenter(new MedicineApplicationView(this.mainLayout, selectedAnimal))
        );

        saveButton.setOnAction(event -> {
            try {
                Medicine selectedMedicine = medicineComboBox.getValue();
                if (selectedMedicine == null) {
                    showErrorAlert("Dados Incompletos", "Por favor, selecione um medicamento.");
                    return;
                }

                // >>> Validações de datas (reforço das regras)
                LocalDate today = LocalDate.now();
                LocalDate applied = appliedDatePicker.getValue();
                LocalDate next = nextDatePicker.getValue();
                LocalDate ends = endsDatePicker.getValue();

                if (applied == null || applied.isAfter(today)) {
                    showErrorAlert("Data inválida", "A data da aplicação deve ser hoje ou anterior.");
                    return;
                }
                if (next != null && !next.isAfter(today)) {
                    showErrorAlert("Data inválida", "A próxima aplicação deve ser após o dia de hoje.");
                    return;
                }
                if (ends != null) {
                    LocalDate base = (next != null) ? next : today;
                    if (!ends.isAfter(base)) {
                        showErrorAlert("Data inválida", "O fim do tratamento deve ser após a data da próxima aplicação.");
                        return;
                    }
                }

                MedicineApplication newApp = new MedicineApplication();
                newApp.setAnimalUuid(selectedAnimal.getUuid());
                newApp.setMedicineUuid(selectedMedicine.getUuid());
                newApp.setUserUuid(currentUser.getUuid());

                LocalDate localDate = applied;
                newApp.setAppliedAt(ZonedDateTime.of(localDate, LocalTime.now(), ZoneId.systemDefault()));

                String qtyText = quantityField.getText() == null ? "" : quantityField.getText().trim();
                if (qtyText.isEmpty()) {
                    showErrorAlert("Dados Incompletos", "Informe a quantidade.");
                    return;
                }
                newApp.setQuantity(Integer.parseInt(qtyText));

                MedicineApplication.Frequency frequency = frequencyComboBox.getValue();
                newApp.setFrequency(frequency != null ? frequency : MedicineApplication.Frequency.DOES_NOT_REPEAT);

                if (nextDatePicker.getValue() != null) {
                    newApp.setNextApplicationAt(ZonedDateTime.of(nextDatePicker.getValue(), LocalTime.MIDNIGHT, ZoneId.systemDefault()));
                }
                if (endsDatePicker.getValue() != null) {
                    newApp.setEndsAt(ZonedDateTime.of(endsDatePicker.getValue(), LocalTime.MIDNIGHT, ZoneId.systemDefault()));
                }

                // ➕ Cria evento no Google Calendar e salva o ID no model
                if ("Sim".equals(googleCalendarCombo.getValue())) {
                    try {
                        ZonedDateTime startDateTime =
                                newApp.getNextApplicationAt() != null ? newApp.getNextApplicationAt() : newApp.getAppliedAt();

                        String googleCalendarId = modules.MedicineApplication.services.GoogleCalendarService.createMedicineApplicationEvent(
                                selectedAnimal.getName(),
                                selectedMedicine.getName(),
                                newApp.getQuantity().toString(),
                                startDateTime,
                                newApp.getFrequency(),
                                newApp.getEndsAt()
                        );

                        newApp.setGoogleCalendarGoogleCalendarId(googleCalendarId);

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

                // ➕ volta para a listagem das aplicações do animal
                this.mainLayout.setCenter(new MedicineApplicationView(this.mainLayout, selectedAnimal));

            } catch (NumberFormatException e) {
                showErrorAlert("Formato Inválido", "A quantidade deve ser um número inteiro válido.");
            } catch (Exception e) {
                showErrorAlert("Erro ao Salvar", "Ocorreu um erro ao registrar a aplicação: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Exibe um Alert de erro com título e mensagem personalizados.
     *
     * @param title Título do alerta (exibido no header)
     * @param message Mensagem de erro a ser exibida
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
