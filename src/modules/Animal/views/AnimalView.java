package modules.Animal.views;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import modules.Animal.controllers.AnimalController;
import modules.Animal.models.Animal;
import modules.Shared.views.MenuView;
import modules.Attachment.views.AttachmentView;
import modules.MedicineApplication.views.MedicineApplicationForm;
import config.Database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class AnimalView extends VBox {

    private TableView<Animal> tableView;
    private ObservableList<Animal> animalList;
    private Connection conn;

    // Referência ao layout principal para navegação entre telas
    private final BorderPane mainLayout;

     /**
      * Construtor da view de lista de animais.
      * 
      * @param mainLayout Layout principal da aplicação para navegação entre telas
      */
    public AnimalView(BorderPane mainLayout) {
        this.mainLayout = mainLayout;

        try {
            this.conn = Database.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        tableView = new TableView<>();
        animalList = FXCollections.observableArrayList();

        // Botão para cadastrar um novo animal
        Button addButton = new Button("Cadastrar");
        addButton.setOnAction(e -> {
            // Navega para o formulário de cadastro de animais
            this.mainLayout.setCenter(new AnimalForm(this.mainLayout));
        });

        // Botão para retornar ao menu principal
        Button backButton = new Button("Voltar");
        backButton.setOnAction(e -> {
            this.mainLayout.setCenter(new MenuView(this.mainLayout, null));
        });

        // Container para agrupar os botões horizontalmente
        HBox buttonBox = new HBox(10, addButton, backButton);
        buttonBox.setPadding(new Insets(10, 10, 10, 10));
        TableColumn<Animal, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(convertStatusToPt(item));
                }
            }
        });

        TableColumn<Animal, String> nameColumn = new TableColumn<>("Nome");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Animal, String> speciesColumn = new TableColumn<>("Espécie");
        speciesColumn.setCellValueFactory(new PropertyValueFactory<>("species"));

        TableColumn<Animal, String> breedColumn = new TableColumn<>("Raça");
        breedColumn.setCellValueFactory(new PropertyValueFactory<>("breed"));

        TableColumn<Animal, String> sizeColumn = new TableColumn<>("Porte");
        sizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));

        TableColumn<Animal, Boolean> castratedColumn = new TableColumn<>("Castrado");
        castratedColumn.setCellValueFactory(new PropertyValueFactory<>("castrated"));

        TableColumn<Animal, String> fivColumn = new TableColumn<>("Fiv");
        fivColumn.setCellValueFactory(new PropertyValueFactory<>("fiv"));

        TableColumn<Animal, String> felvColumn = new TableColumn<>("Felv");
        felvColumn.setCellValueFactory(new PropertyValueFactory<>("felv"));

        // Coluna de ações com botões para editar, deletar, anexos e aplicação de medicamentos
        TableColumn<Animal, Void> actionColumn = new TableColumn<>("Ações");
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("✏️");
            private final Button deleteButton = new Button("❌");
            private final Button attachmentsButton = new Button("Anexos");
            private final Button applyMedicineButton = new Button("💊");
            private final HBox pane = new HBox(5, editButton, deleteButton, attachmentsButton, applyMedicineButton);

            {
                // Botão para editar o animal selecionado
                editButton.setOnAction(event -> {
                    Animal animal = getTableView().getItems().get(getIndex());
                    if (animal != null) {
                        AnimalView.this.mainLayout.setCenter(new AnimalForm(AnimalView.this.mainLayout, animal));
                    } else {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Nenhum animal selecionado");
                        alert.setHeaderText(null);
                        alert.setContentText("Por favor, selecione um animal na tabela para editar.");
                        alert.showAndWait();
                    }
                });

                // Botão para deletar o animal selecionado (com confirmação)
                deleteButton.setOnAction(event -> {
                    Animal animal = getTableView().getItems().get(getIndex());
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Tem certeza que deseja deletar este animal?", ButtonType.YES, ButtonType.NO);
                    alert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.YES) {
                            try {
                                AnimalController.deleteAnimal(conn, animal.getUuid());
                                getTableView().getItems().remove(animal);
                                System.out.println("Animal deletado com sucesso!");
                            } catch (SQLException e) {
                                System.out.println("Erro ao deletar animal.");
                                e.printStackTrace();
                            }
                        }
                    });
                });

                // Botão para acessar os anexos do animal
                attachmentsButton.setOnAction(event -> {
                    Animal animal = getTableView().getItems().get(getIndex());
                    if (animal != null) {
                        AnimalView.this.mainLayout.setCenter(new AttachmentView(AnimalView.this.mainLayout, animal.getUuid()));
                    } else {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Nenhum animal selecionado");
                        alert.setHeaderText(null);
                        alert.setContentText("Por favor, selecione um animal para ver os anexos.");
                        alert.showAndWait();
                    }
                });

                // Botão para registrar aplicação de medicamento no animal
                applyMedicineButton.setOnAction(event -> {
                    Animal animal = getTableView().getItems().get(getIndex());
                    if (animal != null) {
                        // Navega para o formulário de aplicação de medicamentos
                        AnimalView.this.mainLayout.setCenter(new MedicineApplicationForm(AnimalView.this.mainLayout, animal));
                    }
                });
            }


            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });

        tableView.getColumns().addAll(statusColumn, nameColumn, speciesColumn, breedColumn, sizeColumn, castratedColumn, fivColumn, felvColumn, actionColumn);
        // Adiciona os botões e a tabela ao layout principal
        this.getChildren().addAll(buttonBox, tableView);
        loadAnimals();
    }

    /**
     * Carrega a lista de animais do banco de dados e atualiza a tabela.
     */
    private void loadAnimals() {
        try {
            List<Animal> animals = AnimalController.getAllAnimals(conn);
            animalList.clear();
            animalList.addAll(animals);
            tableView.setItems(animalList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Converte o sexo do animal de inglês para português.
     * @param value O valor do sexo em inglês ("male" ou "female").
     * @return O valor em português ("Macho" ou "Fêmea").
     */
    private String convertSexToPt(String value) {
        return switch (value) {
            case "male" -> "Macho";
            case "female" -> "Fêmea";
            default -> null;
        };
    }

    /**
     * Converte a espécie do animal de inglês para português.
     * @param value O valor da espécie em inglês ("dog" ou "cat").
     * @return O valor em português ("Cachorro" ou "Gato").
     */
    private String convertSpeciesToPt(String value) {
        return switch (value) {
            case "dog" -> "Cachorro";
            case "cat" -> "Gato";
            default -> null;
        };
    }

    /**
     * Converte a raça do animal de inglês para português.
     * @param value O valor da raça em inglês.
     * @return O valor em português.
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
     * Converte o porte do animal de inglês para português.
     * @param value O valor do porte em inglês ("small", "medium" ou "large").
     * @return O valor em português ("Pequeno", "Médio" ou "Grande").
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
     * Converte a cor do animal de inglês para português.
     * @param value O valor da cor em inglês.
     * @return O valor em português.
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
     * Converte o status de teste de inglês para português.
     * @param value O valor do status em inglês ("yes", "no" ou "not-tested").
     * @return O valor em português ("Sim", "Não" ou "Não testado").
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
     * Converte o status do animal de inglês para português.
     * @param value O valor do status em inglês.
     * @return O valor em português.
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