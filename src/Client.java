import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class Client extends Application {

    @Override
    public void start(Stage stage) {
        final VBox vbox = new VBox();

        final HBox hBoxTop = new HBox();
        final HBox hBoxBottom = new HBox();

        // TODO populate hbox
        Button button1 = new Button("Button 1");
        Button button2 = new Button("Button 2");
        Button button3 = new Button("Button 3");
        Button button4 = new Button("Button 4");
        Button button5 = new Button("Button 5");
        Button button6 = new Button("Button 6");

        GridPane gridPane = new GridPane();

//        gridPane.add(button1, 0, 0, 1, 1);
//        gridPane.add(button2, 1, 0, 1, 1);
//        gridPane.add(button3, 2, 0, 1, 1);
//        gridPane.add(button4, 0, 1, 1, 1);
//        gridPane.add(button5, 1, 1, 1, 1);
//        gridPane.add(button6, 2, 1, 1, 1);

        for (int n = 0; n < 5; n++) {
            for  (int m = 0; m < 4; m++) {
                System.out.printf("n: %d, m: %d%n", m, n);
                
                gridPane.add(new Button("Button"), m, n, 1, 1);
            }
        }
        hBoxTop.getChildren().add(gridPane);

        vbox.getChildren().add(hBoxTop);
        vbox.getChildren().add(hBoxBottom);

        stage.setScene(new Scene(vbox));
        stage.show();
    }

    @Override
    public void stop() {
        // TODO client cleanup
    }

}
