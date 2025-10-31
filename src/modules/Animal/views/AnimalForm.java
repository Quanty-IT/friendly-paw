package modules.Animal.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import modules.Animal.controllers.AnimalController;
import modules.Animal.models.Animal;
import config.Database;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AnimalForm extends GridPane {

    private TextField nameField;
    private ComboBox<String> sexComboBox;
    private ComboBox<String> speciesComboBox;
    private ComboBox<String> breedComboBox;
    private ComboBox<String> sizeComboBox;
    private ComboBox<String> colorComboBox;
    private DatePicker birthdateField;
    private TextField microchipField;
    private TextField rgaField;
    private ComboBox<String> fivComboBox;
    private ComboBox<String> felvComboBox;
    private ComboBox<String> statusComboBox;
    private TextArea notesField;
    private ComboBox<String> castratedComboBox;

    private Connection conn;
    private final BorderPane mainLayout;
    private final Animal animalToEdit;

    public AnimalForm(BorderPane mainLayout) {
        this(mainLayout, null);
    }

    public AnimalForm(BorderPane mainLayout, Animal animal) {
        this.mainLayout = mainLayout;
        this.animalToEdit = animal;

        try {
            this.conn = Database.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // estilo base
        getStyleClass().add("form-bg");
        setPadding(new Insets(30, 40, 30, 40));
        setHgap(20);
        setVgap(14);
        setAlignment(Pos.TOP_CENTER);
        getStylesheets().add(getClass().getResource("/modules/Animal/styles/AnimalForm.css").toExternalForm());

        // título
        Label title = new Label(animalToEdit == null ? "Cadastrar novo animal" : "Editar animal");
        title.getStyleClass().add("form-title");

        HBox titleBox = new HBox(title);
        titleBox.setAlignment(Pos.CENTER);
        add(titleBox, 0, 0, 4, 1);

        // campos
        nameField = new TextField();
        nameField.setPromptText("Nome do animal");

        sexComboBox = new ComboBox<>();
        sexComboBox.getItems().addAll("Macho", "Fêmea");

        speciesComboBox = new ComboBox<>();
        speciesComboBox.getItems().addAll("Cachorro", "Gato");

        breedComboBox = new ComboBox<>();
        breedComboBox.getItems().addAll(
            "S.R.D",
            "Shih-tzu",
            "Yorkshire Terrier",
            "Spitz Alemão",
            "Buldogue Francês",
            "Poodle",
            "Lhasa Apso",
            "Golden Retriever",
            "Rottweiler",
            "Labrador Retriever",
            "Pug",
            "Pastor Alemão",
            "Border Collie",
            "Chihuahua de Pelo Longo",
            "Pastor Belga Malinois",
            "Maltês"
        );

        sizeComboBox = new ComboBox<>();
        sizeComboBox.getItems().addAll("Pequeno", "Médio", "Grande");

        colorComboBox = new ComboBox<>();
        colorComboBox.getItems().addAll("Preto", "Branco", "Cinza", "Marrom", "Dourado", "Creme", "Canela", "Malhado");

        fivComboBox = new ComboBox<>();
        fivComboBox.getItems().addAll("Sim", "Não", "Não testado");

        felvComboBox = new ComboBox<>();
        felvComboBox.getItems().addAll("Sim", "Não", "Não testado");

        statusComboBox = new ComboBox<>();
        statusComboBox.getItems().addAll("Quarentena", "Abrigado", "Adotado", "Perdido");

        birthdateField = new DatePicker();
        birthdateField.setPromptText("Selecione a data");
        birthdateField.setMinWidth(280);
        birthdateField.setPrefWidth(280);
        birthdateField.setMaxWidth(Double.MAX_VALUE);

        microchipField = new TextField();
        microchipField.setPromptText("Microchip (opcional)");

        rgaField = new TextField();
        rgaField.setPromptText("RGA (opcional)");

        notesField = new TextArea();
        notesField.setPromptText("Observações (opcional)");
        notesField.setPrefRowCount(5);

        castratedComboBox = new ComboBox<>();
        castratedComboBox.getItems().addAll("Sim", "Não");
        castratedComboBox.setPrefWidth(280);
        castratedComboBox.getStyleClass().add("combo-box");

        // larguras padronizadas
        double HALF = 280;
        double FULL = 580;

        nameField.setPrefWidth(FULL);
        notesField.setPrefWidth(FULL);

        sexComboBox.setPrefWidth(HALF);
        speciesComboBox.setPrefWidth(HALF);
        breedComboBox.setPrefWidth(HALF);
        sizeComboBox.setPrefWidth(HALF);
        colorComboBox.setPrefWidth(HALF);
        birthdateField.setPrefWidth(HALF);
        microchipField.setPrefWidth(HALF);
        rgaField.setPrefWidth(HALF);
        fivComboBox.setPrefWidth(HALF);
        felvComboBox.setPrefWidth(HALF);
        statusComboBox.setPrefWidth(HALF);

        // classes de estilo
        addClass(nameField, "form-input");
        addClass(microchipField, "form-input");
        addClass(rgaField, "form-input");
        addClass(notesField, "form-textarea");
        addClass(sexComboBox, "combo-box");
        addClass(speciesComboBox, "combo-box");
        addClass(breedComboBox, "combo-box");
        addClass(sizeComboBox, "combo-box");
        addClass(colorComboBox, "combo-box");
        addClass(fivComboBox, "combo-box");
        addClass(felvComboBox, "combo-box");
        addClass(statusComboBox, "combo-box");
        addClass(birthdateField, "date-picker");

        // linhas
        int r = 1;

        addRowFull("Nome:", nameField, r++, FULL);
        addRowHalf("Sexo:", sexComboBox, "Espécie:", speciesComboBox, r++, HALF);
        addRowHalf("Raça:", breedComboBox, "Porte:", sizeComboBox, r++, HALF);
        addRowHalf("Cor:", colorComboBox, "Nascimento:", birthdateField, r++, HALF);
        addRowHalf("Microchip:", microchipField, "RGA:", rgaField, r++, HALF);
        addRowHalf("FIV:", fivComboBox, "FeLV:", felvComboBox, r++, HALF);
        addRowHalf("Status:", statusComboBox, "Castrado:", castratedComboBox, r++, HALF);
        addRowFull("Observações:", notesField, r++, FULL);

        // botões
        Button backButton = new Button("Voltar");
        backButton.getStyleClass().add("form-btn-cancel");
        backButton.setPrefWidth(150);
        backButton.setOnAction(e -> mainLayout.setCenter(new AnimalView(mainLayout)));

        Button saveButton = new Button(animalToEdit == null ? "Salvar" : "Atualizar");
        saveButton.getStyleClass().add("form-btn-save");
        saveButton.setPrefWidth(150);
        saveButton.setOnAction(e -> saveAnimal());

        HBox buttons = new HBox(15, backButton, saveButton);
        buttons.setAlignment(Pos.CENTER);
        add(buttons, 0, r, 4, 1);

        // modo edição
        if (animalToEdit != null) {
            nameField.setText(animalToEdit.getName());
            sexComboBox.setValue(convertSexToPt(animalToEdit.getSex()));
            speciesComboBox.setValue(convertSpeciesToPt(animalToEdit.getSpecies()));
            breedComboBox.setValue(convertBreedToPt(animalToEdit.getBreed()));
            sizeComboBox.setValue(convertSizeToPt(animalToEdit.getSize()));
            colorComboBox.setValue(convertColorToPt(animalToEdit.getColor()));
            birthdateField.setValue(animalToEdit.getBirthdate());
            microchipField.setText(animalToEdit.getMicrochip());
            rgaField.setText(animalToEdit.getRga());
            castratedComboBox.setValue(animalToEdit.getCastrated() ? "Sim" : "Não");
            fivComboBox.setValue(convertYesNoNotTestedToPt(animalToEdit.getFiv()));
            felvComboBox.setValue(convertYesNoNotTestedToPt(animalToEdit.getFelv()));
            statusComboBox.setValue(convertStatusToPt(animalToEdit.getStatus()));
            notesField.setText(animalToEdit.getNotes());
        }
    }

    // Helpers de layout (mantendo seu padrão)
    private void addRowFull(String labelText, Control field, int row, double width) {
        Label label = new Label(labelText);
        label.getStyleClass().add("form-label");

        HBox wrapper = new HBox(field);
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setPrefWidth(width);
        wrapper.setMaxWidth(width);

        VBox box = new VBox(8, label, wrapper);
        box.setPrefWidth(width);
        box.setMaxWidth(width);
        box.setAlignment(Pos.CENTER_LEFT);

        add(box, 0, row, 4, 1);
    }

    private void addRowHalf(String l1, Control f1, String l2, Control f2, int row, double width) {
        VBox left = buildLabeledBox(l1, f1, width);
        VBox right = buildLabeledBox(l2, f2, width);

        HBox rowBox = new HBox(20, left, right);
        rowBox.setAlignment(Pos.CENTER);

        add(rowBox, 0, row, 4, 1);
    }

    private VBox buildLabeledBox(String labelText, Control field, double width) {
        if (labelText == null || labelText.isBlank()) {
            labelText = " ";
        }

        Label label = new Label(labelText);
        label.getStyleClass().add("form-label");

        HBox wrapper = new HBox(field);
        wrapper.setAlignment(field instanceof CheckBox ? Pos.CENTER_LEFT : Pos.CENTER);
        wrapper.setPrefWidth(width);
        wrapper.setMaxWidth(width);

        VBox box = new VBox(8, label, wrapper);
        box.setPrefWidth(width);
        box.setMaxWidth(width);
        box.setAlignment(Pos.CENTER_LEFT);

        return box;
    }

    private void addClass(Control c, String cls) {
        c.getStyleClass().add(cls);
    }

    // salvar
    private void saveAnimal() {
        String name = nameField.getText();

        String sex = sexComboBox.getValue() != null ? convertSex(sexComboBox.getValue()) : null;
        String species = speciesComboBox.getValue() != null ? convertSpecies(speciesComboBox.getValue()) : null;
        String breed = breedComboBox.getValue() != null ? convertBreed(breedComboBox.getValue()) : null;
        String size = sizeComboBox.getValue() != null ? convertSize(sizeComboBox.getValue()) : null;
        String color = colorComboBox.getValue() != null ? convertColor(colorComboBox.getValue()) : null;

        LocalDate birthdate = birthdateField.getValue();

        String microchip = microchipField.getText();
        String rga = rgaField.getText();

        String fiv = fivComboBox.getValue() != null ? convertYesNoNotTested(fivComboBox.getValue()) : null;
        String felv = felvComboBox.getValue() != null ? convertYesNoNotTested(felvComboBox.getValue()) : null;
        String status = statusComboBox.getValue() != null ? convertStatus(statusComboBox.getValue()) : null;

        String notes = notesField.getText();
        boolean castrated = "Sim".equals(castratedComboBox.getValue());

        Animal animal = new Animal(
            animalToEdit != null ? animalToEdit.getUuid() : null,
            name,
            sex,
            species,
            breed,
            size,
            color,
            birthdate,
            microchip,
            rga,
            castrated,
            fiv,
            felv,
            status,
            notes,
            animalToEdit != null ? animalToEdit.getCreatedAt() : LocalDateTime.now(),
            LocalDateTime.now()
        );

        try {
            if (animalToEdit == null) {
                AnimalController.addAnimal(conn, animal);
            } else {
                AnimalController.updateAnimal(conn, animal);
            }
            mainLayout.setCenter(new AnimalView(mainLayout));
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erro ao salvar animal: " + e.getMessage()).showAndWait();
        }
    }

    // Converters (mantidos)
    private String convertSex(String v) {
        return switch (v) {
            case "Macho" -> "male";
            case "Fêmea" -> "female";
            default -> null;
        };
    }

    private String convertSpecies(String v) {
        return switch (v) {
            case "Cachorro" -> "dog";
            case "Gato" -> "cat";
            default -> null;
        };
    }

    private String convertBreed(String v) {
        return switch (v) {
            case "S.R.D" -> "mixed-breed";
            case "Shih-tzu" -> "shih-tzu";
            case "Yorkshire Terrier" -> "yorkshire-terrier";
            case "Spitz Alemão" -> "german-spitz";
            case "Buldogue Francês" -> "french-bulldog";
            case "Poodle" -> "poodle";
            case "Lhasa Apso" -> "lhasa-apso";
            case "Golden Retriever" -> "golden-retriever";
            case "Rottweiler" -> "rottweiler";
            case "Labrador Retriever" -> "labrador-retriever";
            case "Pug" -> "pug";
            case "Pastor Alemão" -> "german-shepherd";
            case "Border Collie" -> "border-collie";
            case "Chihuahua de Pelo Longo" -> "long-haired-chihuahua";
            case "Pastor Belga Malinois" -> "belgian-malinois";
            case "Maltês" -> "maltese";
            default -> null;
        };
    }

    private String convertSize(String v) {
        return switch (v) {
            case "Pequeno" -> "small";
            case "Médio" -> "medium";
            case "Grande" -> "large";
            default -> null;
        };
    }

    private String convertColor(String v) {
        return switch (v) {
            case "Preto" -> "black";
            case "Branco" -> "white";
            case "Cinza" -> "gray";
            case "Marrom" -> "brown";
            case "Dourado" -> "golden";
            case "Creme" -> "cream";
            case "Canela" -> "tan";
            case "Malhado" -> "speckled";
            default -> null;
        };
    }

    private String convertYesNoNotTested(String v) {
        return switch (v) {
            case "Sim" -> "yes";
            case "Não" -> "no";
            case "Não testado" -> "not-tested";
            default -> null;
        };
    }

    private String convertStatus(String v) {
        return switch (v) {
            case "Quarentena" -> "quarantine";
            case "Abrigado" -> "sheltered";
            case "Adotado" -> "adopted";
            case "Perdido" -> "lost";
            default -> null;
        };
    }

    private String convertSexToPt(String v) {
        return switch (v) {
            case "male" -> "Macho";
            case "female" -> "Fêmea";
            default -> null;
        };
    }

    private String convertSpeciesToPt(String v) {
        return switch (v) {
            case "dog" -> "Cachorro";
            case "cat" -> "Gato";
            default -> null;
        };
    }

    private String convertBreedToPt(String v) {
        return switch (v) {
            case "mixed-breed" -> "S.R.D";
            case "shih-tzu" -> "Shih-tzu";
            case "yorkshire-terrier" -> "Yorkshire Terrier";
            case "german-spitz" -> "Spitz Alemão";
            case "french-bulldog" -> "Buldogue Francês";
            case "poodle" -> "Poodle";
            case "lhasa-apso" -> "Lhasa Apso";
            case "golden-retriever" -> "Golden Retriever";
            case "rottweiler" -> "Rottweiler";
            case "labrador-retriever" -> "Labrador Retriever";
            case "pug" -> "Pug";
            case "german-shepherd" -> "Pastor Alemão";
            case "border-collie" -> "Border Collie";
            case "long-haired-chihuahua" -> "Chihuahua de Pelo Longo";
            case "belgian-malinois" -> "Pastor Belga Malinois";
            case "maltese" -> "Maltês";
            default -> null;
        };
    }

    private String convertSizeToPt(String v) {
        return switch (v) {
            case "small" -> "Pequeno";
            case "medium" -> "Médio";
            case "large" -> "Grande";
            default -> null;
        };
    }

    private String convertColorToPt(String v) {
        return switch (v) {
            case "black" -> "Preto";
            case "white" -> "Branco";
            case "gray" -> "Cinza";
            case "brown" -> "Marrom";
            case "golden" -> "Dourado";
            case "cream" -> "Creme";
            case "tan" -> "Canela";
            case "speckled" -> "Malhado";
            default -> null;
        };
    }

    private String convertYesNoNotTestedToPt(String v) {
        return switch (v) {
            case "yes" -> "Sim";
            case "no" -> "Não";
            case "not-tested" -> "Não testado";
            default -> null;
        };
    }

    private String convertStatusToPt(String v) {
        return switch (v) {
            case "quarantine" -> "Quarentena";
            case "sheltered" -> "Abrigado";
            case "adopted" -> "Adotado";
            case "lost" -> "Perdido";
            default -> null;
        };
    }
}
