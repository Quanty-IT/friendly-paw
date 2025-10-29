package modules.Animal.views;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import modules.Animal.controllers.AnimalController;
import modules.Animal.models.Animal;
import modules.Animal.views.AnimalView;
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
    private CheckBox castratedCheckBox;

    private Connection conn;
    private BorderPane mainLayout;

    // Variável para guardar o animal que está sendo editado
    private Animal animalToEdit;

    /**
     * Construtor para criação de novo animal.
     * 
     * @param mainLayout Layout principal da aplicação para navegação entre telas
     */
    public AnimalForm(BorderPane mainLayout) {
        // Chamada ao construtor de edição, passando null como animal
        this(mainLayout, null);
    }

    /**
     * Construtor para edição de animal existente ou criação de novo.
     * 
     * @param mainLayout Layout principal da aplicação para navegação entre telas
     * @param animal Animal a ser editado (null para criação de novo animal)
     */
    public AnimalForm(BorderPane mainLayout, Animal animal) {
        this.mainLayout = mainLayout;
        this.animalToEdit = animal;

        nameField = new TextField();
        sexComboBox = new ComboBox<>();
        speciesComboBox = new ComboBox<>();
        breedComboBox = new ComboBox<>();
        sizeComboBox = new ComboBox<>();
        colorComboBox = new ComboBox<>();
        fivComboBox = new ComboBox<>();
        felvComboBox = new ComboBox<>();
        statusComboBox = new ComboBox<>();

        sexComboBox.getItems().addAll("Macho", "Fêmea");
        speciesComboBox.getItems().addAll("Cachorro", "Gato");
        breedComboBox.getItems().addAll(
                "S.R.D", "Shih-tzu", "Yorkshire Terrier", "Spitz Alemão",
                "Buldogue Francês", "Poodle", "Lhasa Apso", "Golden Retriever",
                "Rottweiler", "Labrador Retriever", "Pug", "Pastor Alemão",
                "Border Collie", "Chihuahua de Pelo Longo", "Pastor Belga Malinois",
                "Maltês"
        );
        sizeComboBox.getItems().addAll("Pequeno", "Médio", "Grande");
        colorComboBox.getItems().addAll("Preto", "Branco", "Cinza", "Marrom", "Dourado", "Creme", "Canela", "Malhado");
        fivComboBox.getItems().addAll("Sim", "Não", "Não testado");
        felvComboBox.getItems().addAll("Sim", "Não", "Não testado");
        statusComboBox.getItems().addAll("Quarentena", "Abrigado", "Adotado", "Perdido");

        birthdateField = new DatePicker();
        microchipField = new TextField();
        rgaField = new TextField();
        notesField = new TextArea();
        castratedCheckBox = new CheckBox();

        try {
            this.conn = Database.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Button saveButton = new Button("Salvar");
        saveButton.setOnAction(e -> saveAnimal());

        Button backButton = new Button("Voltar");
        backButton.setOnAction(e -> mainLayout.setCenter(new AnimalView(mainLayout)));

        HBox buttonBox = new HBox(10, saveButton, backButton);

        this.setVgap(10);
        this.setHgap(10);
        this.setPadding(new Insets(20, 20, 20, 20));

        int row = 0;
        this.add(new Label("Nome:"), 0, row); this.add(nameField, 1, row++);
        this.add(new Label("Sexo:"), 0, row); this.add(sexComboBox, 1, row++);
        this.add(new Label("Espécie:"), 0, row); this.add(speciesComboBox, 1, row++);
        this.add(new Label("Raça:"), 0, row); this.add(breedComboBox, 1, row++);
        this.add(new Label("Tamanho:"), 0, row); this.add(sizeComboBox, 1, row++);
        this.add(new Label("Cor:"), 0, row); this.add(colorComboBox, 1, row++);
        this.add(new Label("Nascimento:"), 0, row); this.add(birthdateField, 1, row++);
        this.add(new Label("Microchip:"), 0, row); this.add(microchipField, 1, row++);
        this.add(new Label("RGA:"), 0, row); this.add(rgaField, 1, row++);
        this.add(new Label("Castrado:"), 0, row); this.add(castratedCheckBox, 1, row++);
        this.add(new Label("FIV:"), 0, row); this.add(fivComboBox, 1, row++);
        this.add(new Label("FeLV:"), 0, row); this.add(felvComboBox, 1, row++);
        this.add(new Label("Status:"), 0, row); this.add(statusComboBox, 1, row++);
        this.add(new Label("Observações:"), 0, row); this.add(notesField, 1, row++);
        this.add(saveButton, 1, row);
        this.add(buttonBox, 2, row);

        nameField.setPrefWidth(240);
        sexComboBox.setPrefWidth(240);
        speciesComboBox.setPrefWidth(240);
        breedComboBox.setPrefWidth(240);
        sizeComboBox.setPrefWidth(240);
        colorComboBox.setPrefWidth(240);
        microchipField.setPrefWidth(240);
        rgaField.setPrefWidth(240);
        notesField.setPrefWidth(240);
        notesField.setPrefHeight(100);

        this.setPrefSize(520, 600);

        // Se houver um animal para editar, preenche o formulário com os dados existentes
        if (this.animalToEdit != null) {
            // Pré-preenche todos os campos do formulário com os dados do animal
            nameField.setText(this.animalToEdit.getName());
            sexComboBox.setValue(convertSexToPt(this.animalToEdit.getSex()));
            speciesComboBox.setValue(convertSpeciesToPt(this.animalToEdit.getSpecies()));
            breedComboBox.setValue(convertBreedToPt(this.animalToEdit.getBreed()));
            sizeComboBox.setValue(convertSizeToPt(this.animalToEdit.getSize()));
            colorComboBox.setValue(convertColorToPt(this.animalToEdit.getColor()));
            birthdateField.setValue(this.animalToEdit.getBirthdate());
            microchipField.setText(this.animalToEdit.getMicrochip());
            rgaField.setText(this.animalToEdit.getRga());
            castratedCheckBox.setSelected(this.animalToEdit.getCastrated());
            fivComboBox.setValue(convertYesNoNotTestedToPt(this.animalToEdit.getFiv()));
            felvComboBox.setValue(convertYesNoNotTestedToPt(this.animalToEdit.getFelv()));
            statusComboBox.setValue(convertStatusToPt(this.animalToEdit.getStatus()));
            notesField.setText(this.animalToEdit.getNotes());

            // Altera o texto do botão para indicar que está em modo de edição
            saveButton.setText("Atualizar");
        }
    }

    /**
     * Salva ou atualiza um animal no banco de dados.
     * Verifica se o animal está sendo editado ou criado e chama o método apropriado do controller.
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
        boolean castrated = castratedCheckBox.isSelected();

        // Cria o objeto Animal com os dados do formulário
        // Se estiver editando, preserva o UUID e a data de criação originais
        Animal animal = new Animal(
                this.animalToEdit != null ? this.animalToEdit.getUuid() : null, // Em edição, mantém o UUID original
                name, sex, species, breed, size, color, birthdate,
                microchip, rga, castrated, fiv, felv, status, notes,
                this.animalToEdit != null ? this.animalToEdit.getCreatedAt() : LocalDateTime.now(), // Em edição, mantém a data de criação original
                LocalDateTime.now() // Sempre atualiza a data de modificação
        );

        try {
            if (this.animalToEdit == null) {
                // Modo de criação: adiciona um novo animal ao banco de dados
                AnimalController.addAnimal(conn, animal);
                System.out.println("Animal adicionado com sucesso!");
                clearForm();
            } else {
                // Modo de edição: atualiza os dados do animal existente
                AnimalController.updateAnimal(conn, animal);
                System.out.println("Animal atualizado com sucesso!");
            }
            // Retorna para a tela de lista de animais após salvar
            mainLayout.setCenter(new AnimalView(mainLayout));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Limpa todos os campos do formulário.
     */
    private void clearForm() {
        nameField.clear();
        sexComboBox.getSelectionModel().clearSelection();
        speciesComboBox.getSelectionModel().clearSelection();
        breedComboBox.getSelectionModel().clearSelection();
        sizeComboBox.getSelectionModel().clearSelection();
        colorComboBox.getSelectionModel().clearSelection();
        birthdateField.setValue(null);
        microchipField.clear();
        rgaField.clear();
        fivComboBox.getSelectionModel().clearSelection();
        felvComboBox.getSelectionModel().clearSelection();
        statusComboBox.getSelectionModel().clearSelection();
        notesField.clear();
        castratedCheckBox.setSelected(false);
    }

    // Métodos auxiliares para conversão entre valores em português e inglês
    private String convertSex(String value) {
        return switch (value) {
            case "Macho" -> "male";
            case "Fêmea" -> "female";
            default -> null;
        };
    }

    private String convertSpecies(String value) {
        return switch (value) {
            case "Cachorro" -> "dog";
            case "Gato" -> "cat";
            default -> null;
        };
    }

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

    private String convertSize(String value) {
        return switch (value) {
            case "Pequeno" -> "small";
            case "Médio" -> "medium";
            case "Grande" -> "large";
            default -> null;
        };
    }

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

    private String convertYesNoNotTested(String value) {
        return switch (value) {
            case "Sim" -> "yes";
            case "Não" -> "no";
            case "Não testado" -> "not-tested";
            default -> null;
        };
    }

    private String convertStatus(String value) {
        return switch (value) {
            case "Quarentena" -> "quarantine";
            case "Abrigado" -> "sheltered";
            case "Adotado" -> "adopted";
            case "Perdido" -> "lost";
            default -> null;
        };
    }

    private String convertSexToPt(String value) {
        return switch (value) {
            case "male" -> "Macho";
            case "female" -> "Fêmea";
            default -> null;
        };
    }

    private String convertSpeciesToPt(String value) {
        return switch (value) {
            case "dog" -> "Cachorro";
            case "cat" -> "Gato";
            default -> null;
        };
    }

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

    private String convertSizeToPt(String value) {
        return switch (value) {
            case "small" -> "Pequeno";
            case "medium" -> "Médio";
            case "large" -> "Grande";
            default -> null;
        };
    }

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

    private String convertYesNoNotTestedToPt(String value) {
        return switch (value) {
            case "yes" -> "Sim";
            case "no" -> "Não";
            case "not-tested" -> "Não testado";
            default -> null;
        };
    }

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