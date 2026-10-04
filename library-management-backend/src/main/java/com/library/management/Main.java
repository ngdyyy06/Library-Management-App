package com.library.management;

import com.library.management.config.HibernateUtil;
import com.library.management.repository.UserRepository;
import com.library.management.service.AuthService;
import com.library.management.service.PasswordService;
import com.library.management.view.LoginView;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {

        UserRepository userRepository =
                new UserRepository();

        PasswordService passwordService =
                new PasswordService();

        AuthService authService =
                new AuthService(
                        userRepository,
                        passwordService
                );

        LoginView loginView =
                new LoginView(authService);

        loginView.show(stage);
    }

    @Override
    public void stop() {
        HibernateUtil.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}