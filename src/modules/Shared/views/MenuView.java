package modules.Shared.views;

import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.layout.BorderPane;
import modules.Animal.views.AnimalView;
import modules.Medicine.views.MedicineView;

public class MenuView extends VBox {

    private BorderPane mainLayout;

    /**
     * Construtor da view do menu principal.
     * 
     * @param mainLayout Layout principal da aplicação para navegação entre telas
     * @param stage Stage da aplicação (não utilizado atualmente)
     */
    public MenuView(BorderPane mainLayout, Stage stage) {
        this.mainLayout = mainLayout;

        // Botões de navegação principal
        Button btnAnimais = new Button("Animais");
        Button btnMedicamentos = new Button("Medicamentos");

        // Navega para a view de animais ao clicar no botão
        btnAnimais.setOnAction(e -> {
            AnimalView animalView = new AnimalView(this.mainLayout);
            this.mainLayout.setCenter(animalView);
        });

        // Navega para a view de medicamentos ao clicar no botão
        btnMedicamentos.setOnAction(e -> {
            MedicineView medicineView = new MedicineView(this.mainLayout);
            this.mainLayout.setCenter(medicineView);
        });

        // Adiciona os botões ao layout
        this.getChildren().addAll(btnAnimais, btnMedicamentos);
    }
}
