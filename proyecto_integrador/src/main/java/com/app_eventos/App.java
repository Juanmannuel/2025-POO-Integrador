package com.app_eventos;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
@Override
public void start(Stage primaryStage) throws Exception {
    Parent root = FXMLLoader.load(getClass().getResource("/fxml/main.fxml"));
    Scene scene = new Scene(root);
    scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
    scene.getStylesheets().add(getClass().getResource("/styles/persona.css").toExternalForm());

    
    primaryStage.setTitle("Gestión de Eventos Culturales");

    // Tamaño inicial grande
    primaryStage.setWidth(1300);
    primaryStage.setHeight(800);

    // Permitir maximizar correctamente
    primaryStage.setMinWidth(800);
    primaryStage.setMinHeight(600);

    primaryStage.setScene(scene);
    primaryStage.show();
}

    public static void main(String[] args) {
        launch(args);
    }
}