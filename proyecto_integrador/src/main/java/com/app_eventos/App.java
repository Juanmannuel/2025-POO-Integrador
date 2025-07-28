package com.app_eventos;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Confirmar ruta
        System.out.println(getClass().getResource("/fxml/abm/abmEventoResources/abm_evento.fxml"));

        Parent root = FXMLLoader.load(getClass().getResource("/fxml/abm/abmEventoResources/abm_evento.fxml"));
        Scene scene = new Scene(root);
        primaryStage.setTitle("ABM Evento");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

