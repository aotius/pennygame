import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.concurrent.TimeUnit;

public class MainApplicationScreen extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        final VBox vbox = new VBox();
        final HBox hBoxTop = new HBox();
        hBoxTop.setAlignment(Pos.CENTER);
        final HBox hBoxBottom = new HBox();

        final Button button = new Button("Host Game");
        button.setOnAction(event -> {
            try {
                //  TODO get info and set info on batch size
                int batchSize = 20;
                PennyServer server = new PennyServer(batchSize);
                server.start();
                PennyClient client = new PennyClient(stage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        final Button button2 = new Button("Join Game");
        button2.setOnAction(event -> {
            try {
                PennyClient client = new PennyClient(stage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        hBoxTop.getChildren().add(button);
        hBoxTop.getChildren().add(button2);

        vbox.getChildren().add(hBoxTop);
        vbox.getChildren().add(hBoxBottom);

        stage.setScene(new Scene(vbox));
        stage.setWidth(800);
        stage.setHeight(600);
        stage.setResizable(false);
        stage.setTitle("Agile Penny Game");
        stage.getIcons().add(new Image(getClass().getResourceAsStream( "icon.png" )));
        stage.show();
    }
}
