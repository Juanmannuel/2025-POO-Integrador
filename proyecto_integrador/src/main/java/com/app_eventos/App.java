package com.app_eventos;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Carga la vista principal desde el archivo FXML
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/main.fxml"));
        Scene scene = new Scene(root);

        // Aplicar el CSS global
        scene.getStylesheets().add(getClass().getResource("/styles/app.css").toExternalForm());

        // Configuración de la ventana principal
        primaryStage.setTitle("Gestión de Eventos Culturales");
        primaryStage.setWidth(1300);
        primaryStage.setHeight(800);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);

        // Establecer la escena y mostrar
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
