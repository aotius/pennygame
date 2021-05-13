import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Client extends Application {
    private static final int ROWS = 4;
    private static final int COLUMNS = 5;
    private int count = 0;

    @Override
    public void start(Stage stage) {
        final VBox vbox = new VBox();

        final HBox hBoxTop = new HBox();
        final HBox hBoxBottom = new HBox();

        final GridPane pennyGrid = new GridPane();
        pennyGrid.setPadding(new Insets(10, 10, 10, 10));
        pennyGrid.setHgap(10);
        pennyGrid.setVgap(10);

        for (int n = 0; n < ROWS; n++) {
            for  (int m = 0; m < COLUMNS; m++) {
                final Circle circle = new Circle(30, Color.BLACK);
                // TODO button click
                circle.setOnMouseClicked(event -> {
                    System.out.println(count);
                    if (circle.getFill() == Color.GRAY) {
                        return;
                    }
                    circle.setFill(Color.GRAY);
                    count++;
                });
                pennyGrid.add(circle, m, n, 1, 1);
            }
        }
        hBoxTop.getChildren().add(pennyGrid);

        final Button button = new Button("Pass Pennies");
        button.setOnAction(event -> {
            if (count != 20) {

            }
        });
        hBoxTop.getChildren().add(button);

        // TODO bottom half of the UI (clean this up maybe)
        final GridPane scoreGrid = new GridPane();
        Text p1 = new Text("Player 1");
        Text p2 = new Text("Player 2");
        Text p3 = new Text("Player 3");
        Text p4 = new Text("Player 4");
        Text p5 = new Text("Client");
        Text p1score = new Text("20");
        Text p2score = new Text("0");
        Text p3score = new Text("0");
        Text p4score = new Text("0");
        Text p5score = new Text("0");
        scoreGrid.add(p1, 0, 0);
        scoreGrid.add(p2, 1, 0);
        scoreGrid.add(p3, 2, 0);
        scoreGrid.add(p4, 3, 0);
        scoreGrid.add(p5, 4, 0);
        scoreGrid.add(p1score, 0, 1);
        scoreGrid.add(p2score, 1, 1);
        scoreGrid.add(p3score, 2, 1);
        scoreGrid.add(p4score, 3, 1);
        scoreGrid.add(p5score, 4, 1);

        hBoxBottom.getChildren().add(scoreGrid);

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
