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
        castratedComboBox.setValue("Não");

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

    /**
     * Adiciona uma linha completa no formulário com label e campo de largura total.
     * 
     * @param labelText Texto do label
     * @param field Campo de controle a ser adicionado
     * @param row Número da linha no GridPane
     * @param width Largura total do campo
     */
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

    /**
     * Adiciona uma linha no formulário com dois campos lado a lado.
     * 
     * @param l1 Texto do label do primeiro campo
     * @param f1 Primeiro campo de controle
     * @param l2 Texto do label do segundo campo
     * @param f2 Segundo campo de controle
     * @param row Número da linha no GridPane
     * @param width Largura de cada campo (metade da largura total)
     */
    private void addRowHalf(String l1, Control f1, String l2, Control f2, int row, double width) {
        VBox left = buildLabeledBox(l1, f1, width);
        VBox right = buildLabeledBox(l2, f2, width);

        HBox rowBox = new HBox(20, left, right);
        rowBox.setAlignment(Pos.CENTER);

        add(rowBox, 0, row, 4, 1);
    }

    /**
     * Cria um VBox com label e campo de controle para uso em layouts de formulário.
     * 
     * @param labelText Texto do label
     * @param field Campo de controle
     * @param width Largura do campo
     * @return VBox configurado com label e campo
     */
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

    /**
     * Adiciona uma classe CSS a um controle.
     * 
     * @param c Controle ao qual adicionar a classe
     * @param cls Nome da classe CSS
     */
    private void addClass(Control c, String cls) {
        c.getStyleClass().add(cls);
    }

    /**
     * Salva ou atualiza um animal no banco de dados.
     * Valida os dados do formulário e cria ou atualiza o animal conforme o modo (criação/edição).
     * 
     * @throws SQLException Se ocorrer erro na operação do banco de dados (tratado internamente com Alert)
     */
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

    /**
     * Converte o valor de sexo do português para inglês.
     * 
     * @param value Valor em português ("Macho" ou "Fêmea")
     * @return Valor em inglês ("male" ou "female"), ou null se o valor não for reconhecido
     */
    private String convertSex(String value) {
        return switch (value) {
            case "Macho" -> "male";
            case "Fêmea" -> "female";
            default -> null;
        };
    }

    /**
     * Converte o valor de espécie do português para inglês.
     * 
     * @param value Valor em português ("Cachorro" ou "Gato")
     * @return Valor em inglês ("dog" ou "cat"), ou null se o valor não for reconhecido
     */
    private String convertSpecies(String value) {
        return switch (value) {
            case "Cachorro" -> "dog";
            case "Gato" -> "cat";
            default -> null;
        };
    }

    /**
     * Converte o valor de raça do português para inglês.
     * 
     * @param value Valor em português (nome da raça)
     * @return Valor em inglês (identificador da raça), ou null se o valor não for reconhecido
     */
    private String convertBreed(String value) {
        return switch (value) {
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

    /**
     * Converte o valor de porte do português para inglês.
     * 
     * @param value Valor em português ("Pequeno", "Médio" ou "Grande")
     * @return Valor em inglês ("small", "medium" ou "large"), ou null se o valor não for reconhecido
     */
    private String convertSize(String value) {
        return switch (value) {
            case "Pequeno" -> "small";
            case "Médio" -> "medium";
            case "Grande" -> "large";
            default -> null;
        };
    }

    /**
     * Converte o valor de cor do português para inglês.
     * 
     * @param value Valor em português (nome da cor)
     * @return Valor em inglês (identificador da cor), ou null se o valor não for reconhecido
     */
    private String convertColor(String value) {
        return switch (value) {
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

    /**
     * Converte o valor de teste do português para inglês.
     * 
     * @param value Valor em português ("Sim", "Não" ou "Não testado")
     * @return Valor em inglês ("yes", "no" ou "not-tested"), ou null se o valor não for reconhecido
     */
    private String convertYesNoNotTested(String value) {
        return switch (value) {
            case "Sim" -> "yes";
            case "Não" -> "no";
            case "Não testado" -> "not-tested";
            default -> null;
        };
    }

    /**
     * Converte o valor de status do português para inglês.
     * 
     * @param value Valor em português ("Quarentena", "Abrigado", "Adotado" ou "Perdido")
     * @return Valor em inglês ("quarantine", "sheltered", "adopted" ou "lost"), ou null se o valor não for reconhecido
     */
    private String convertStatus(String value) {
        return switch (value) {
            case "Quarentena" -> "quarantine";
            case "Abrigado" -> "sheltered";
            case "Adotado" -> "adopted";
            case "Perdido" -> "lost";
            default -> null;
        };
    }

    /**
     * Converte o valor de sexo do inglês para português.
     * 
     * @param value Valor em inglês ("male" ou "female")
     * @return Valor em português ("Macho" ou "Fêmea"), ou null se o valor não for reconhecido
     */
    private String convertSexToPt(String value) {
        return switch (value) {
            case "male" -> "Macho";
            case "female" -> "Fêmea";
            default -> null;
        };
    }

    /**
     * Converte o valor de espécie do inglês para português.
     * 
     * @param value Valor em inglês ("dog" ou "cat")
     * @return Valor em português ("Cachorro" ou "Gato"), ou null se o valor não for reconhecido
     */
    private String convertSpeciesToPt(String value) {
        return switch (value) {
            case "dog" -> "Cachorro";
            case "cat" -> "Gato";
            default -> null;
        };
    }

    /**
     * Converte o valor de raça do inglês para português.
     * 
     * @param value Valor em inglês (identificador da raça)
     * @return Valor em português (nome da raça), ou null se o valor não for reconhecido
     */
    private String convertBreedToPt(String value) {
        return switch (value) {
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

    /**
     * Converte o valor de porte do inglês para português.
     * 
     * @param value Valor em inglês ("small", "medium" ou "large")
     * @return Valor em português ("Pequeno", "Médio" ou "Grande"), ou null se o valor não for reconhecido
     */
    private String convertSizeToPt(String value) {
        return switch (value) {
            case "small" -> "Pequeno";
            case "medium" -> "Médio";
            case "large" -> "Grande";
            default -> null;
        };
    }

    /**
     * Converte o valor de cor do inglês para português.
     * 
     * @param value Valor em inglês (identificador da cor)
     * @return Valor em português (nome da cor), ou null se o valor não for reconhecido
     */
    private String convertColorToPt(String value) {
        return switch (value) {
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

    /**
     * Converte o valor de teste do inglês para português.
     * 
     * @param value Valor em inglês ("yes", "no" ou "not-tested")
     * @return Valor em português ("Sim", "Não" ou "Não testado"), ou null se o valor não for reconhecido
     */
    private String convertYesNoNotTestedToPt(String value) {
        return switch (value) {
            case "yes" -> "Sim";
            case "no" -> "Não";
            case "not-tested" -> "Não testado";
            default -> null;
        };
    }

    /**
     * Converte o valor de status do inglês para português.
     * 
     * @param value Valor em inglês ("quarantine", "sheltered", "adopted" ou "lost")
     * @return Valor em português ("Quarentena", "Abrigado", "Adotado" ou "Perdido"), ou null se o valor não for reconhecido
     */
    private String convertStatusToPt(String value) {
        return switch (value) {
            case "quarantine" -> "Quarentena";
            case "sheltered" -> "Abrigado";
            case "adopted" -> "Adotado";
            case "lost" -> "Perdido";
            default -> null;
        };
    }
}
