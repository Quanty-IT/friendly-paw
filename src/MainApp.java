import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import modules.views.AnimalListView;
import modules.views.LoginForm;
import modules.models.User;
import modules.views.MedicineView;

public class MainApp extends Application {

    private BorderPane mainLayout;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Pata Amiga");

        // Cria o layout principal para gerenciar as telas
        this.mainLayout = new BorderPane();

        // O conteúdo inicial do mainLayout será a tela de login
        LoginForm login = new LoginForm((User u) -> {
            // Após o login, o conteúdo do mainLayout muda para a lista de animais
            AnimalListView animalView = new AnimalListView(this.mainLayout);
            MedicineView mdV = new MedicineView();

            this.mainLayout.setCenter(mdV);
            stage.sizeToScene();
        });
        this.mainLayout.setCenter(login);

        // A cena da aplicação agora é o mainLayout, que gerencia todas as views
        Scene scene = new Scene(this.mainLayout, 420, 220);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
