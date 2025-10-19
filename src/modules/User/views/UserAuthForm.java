package modules.User.views;

import config.Database;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import modules.User.controllers.UserAuthController;
import modules.User.models.User;
import utils.Session;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;

public class UserAuthForm extends GridPane {

    private final TextField emailField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final Button loginButton = new Button("Entrar");
    private Connection conn;

    private final Consumer<User> onSuccess;

    public UserAuthForm(Consumer<User> onSuccess) {
        this.onSuccess = onSuccess;

        try {
            this.conn = Database.getConnection();
        } catch (SQLException e) {
            showError("Falha na conexão com o banco:\n" + e.getMessage());
            loginButton.setDisable(true);
        }

        setVgap(10);
        setHgap(10);
        setPadding(new Insets(20));

        add(new Label("E-mail:"), 0, 0);
        add(emailField, 1, 0);

        add(new Label("Senha:"), 0, 1);
        add(passwordField, 1, 1);

        add(loginButton, 1, 2);

        emailField.setPromptText("email");
        passwordField.setPromptText("senha");

        loginButton.setOnAction(e -> doLogin());
        passwordField.setOnAction(e -> doLogin());

        emailField.setPrefWidth(260);
        passwordField.setPrefWidth(260);
    }

    private void doLogin() {
        if (conn == null) {
            showError("Sem conexão com o banco.");
            return;
        }
        String email = emailField.getText() != null ? emailField.getText().trim() : "";
        String password = passwordField.getText() != null ? passwordField.getText() : "";

        if (email.isEmpty() || password.isEmpty()) {
            showError("Preencha e-mail e senha.");
            return;
        }

        try {
            User u = UserAuthController.login(conn, email, password);
            if (u == null) {
                showError("Credenciais inválidas.");
                return;
            }
            Session.set(u);
            if (onSuccess != null) onSuccess.accept(u);
        } catch (SQLException ex) {
            showError("Erro ao autenticar:\n" + ex.getMessage());
        }
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText("Erro");
        a.showAndWait();
    }
}
