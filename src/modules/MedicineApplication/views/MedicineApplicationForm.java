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
    private final ComboBox<MedicineApplication.Frequency> frequencyComboBox = new ComboBox<>();
    private final DatePicker nextDatePicker = new DatePicker();
    private final DatePicker endsDatePicker = new DatePicker();
    private final CheckBox googleCalendarCheckBox = new CheckBox("Agendar no Google Calendar");
    private final Button saveButton = new Button("Salvar Aplicação");

    /**
     * Construtor do formulário de aplicação de medicamentos.
     * 
     * @param mainLayout Layout principal da aplicação
     * @param selectedAnimal Animal selecionado para aplicação
     */
    public MedicineApplicationForm(BorderPane mainLayout, Animal selectedAnimal) {
        this.mainLayout = mainLayout;
        this.selectedAnimal = selectedAnimal;
        // Obtém o usuário logado da sessão
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

    /**
     * Configura a interface do usuário do formulário.
     */
    private void setupUI() {
        setSpacing(10);
        setPadding(new Insets(20));

        animalField.setText(selectedAnimal.getName());
        // O campo do animal não pode ser editado, apenas exibido
        animalField.setDisable(true);

        quantityField.setPromptText("Ex: 1.5");
        
        // Configura o ComboBox de frequência com todos os valores disponíveis
        frequencyComboBox.getItems().addAll(MedicineApplication.Frequency.values());
        frequencyComboBox.setPromptText("Selecione a frequência");
        // Define valor padrão como "Não se repete"
        frequencyComboBox.setValue(MedicineApplication.Frequency.DOES_NOT_REPEAT);
        
        // Configura a exibição do enum na ComboBox para mostrar nomes em português
        frequencyComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(MedicineApplication.Frequency item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getDisplayName());
                }
            }
        });
        frequencyComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(MedicineApplication.Frequency item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getDisplayName());
                }
            }
        });
        
        // Configura os eventos dos campos para habilitar/desabilitar campos relacionados
        frequencyComboBox.setOnAction(e -> {
            MedicineApplication.Frequency selectedFrequency = frequencyComboBox.getValue();
            boolean isRecurring = selectedFrequency != null && selectedFrequency.isRecurring();
            nextDatePicker.setDisable(!isRecurring);
            endsDatePicker.setDisable(!isRecurring);
        });
        
        googleCalendarCheckBox.setOnAction(e -> {
            boolean useGoogleCalendar = googleCalendarCheckBox.isSelected();
            frequencyComboBox.setDisable(!useGoogleCalendar);
            if (!useGoogleCalendar) {
                frequencyComboBox.setValue(MedicineApplication.Frequency.DOES_NOT_REPEAT);
                nextDatePicker.setDisable(true);
                endsDatePicker.setDisable(true);
            }
        });

        Button backButton = new Button("Voltar");
        backButton.setOnAction(e -> this.mainLayout.setCenter(new AnimalView(this.mainLayout)));

        getChildren().addAll(
                new Label("Animal:"), animalField,
                new Label("Medicamento:"), medicineComboBox,
                new Label("Data da Aplicação:"), appliedDatePicker,
                new Label("Quantidade (Em mmg):"), quantityField,
                googleCalendarCheckBox,
                new Label("Frequência:"), frequencyComboBox,
                new Label("Próxima Aplicação (opcional):"), nextDatePicker,
                new Label("Fim do Tratamento (opcional):"), endsDatePicker,
                new HBox(10, saveButton, backButton)
        );
    }

    /**
     * Carrega os dados necessários para o formulário.
     */
    private void loadData() {
        try {
            medicineComboBox.setItems(FXCollections.observableArrayList(medicineController.listAll()));

            // Configura a exibição do nome do medicamento na ComboBox
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

    /**
     * Configura as ações dos botões e eventos do formulário.
     */
    private void setupActions() {
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
                // Converte vírgula para ponto para aceitar formato brasileiro de números
                newApp.setQuantity(new BigDecimal(quantityField.getText().replace(',', '.')));
                
                MedicineApplication.Frequency frequency = frequencyComboBox.getValue();
                // Define frequência padrão se nenhuma foi selecionada
                newApp.setFrequency(frequency != null ? frequency : MedicineApplication.Frequency.DOES_NOT_REPEAT);

                if (nextDatePicker.getValue() != null) {
                    newApp.setNextApplicationAt(ZonedDateTime.of(nextDatePicker.getValue(), LocalTime.MIDNIGHT, ZoneId.systemDefault()));
                }
                if (endsDatePicker.getValue() != null) {
                    newApp.setEndsAt(ZonedDateTime.of(endsDatePicker.getValue(), LocalTime.MIDNIGHT, ZoneId.systemDefault()));
                }

                // Cria evento no Google Calendar se o usuário solicitou
                if (googleCalendarCheckBox.isSelected()) {
                    try {
                        // Usa a próxima aplicação como início, ou a aplicação atual se não houver próxima
                        ZonedDateTime startDateTime = newApp.getNextApplicationAt() != null ? 
                            newApp.getNextApplicationAt() : 
                            newApp.getAppliedAt();
                            
                        // Cria o evento recorrente no Google Calendar
                        String eventUuid = modules.MedicineApplication.services.GoogleCalendarService.createMedicineApplicationEvent(
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

                // Retorna para a tela de lista de animais após salvar
                this.mainLayout.setCenter(new AnimalView(this.mainLayout));

            } catch (NumberFormatException e) {
                showErrorAlert("Formato Inválido", "A quantidade deve ser um número válido (ex: 1.5).");
            } catch (Exception e) {
                showErrorAlert("Erro ao Salvar", "Ocorreu um erro ao registrar a aplicação: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Exibe um alerta de erro na interface.
     * 
     * @param title Título do alerta
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