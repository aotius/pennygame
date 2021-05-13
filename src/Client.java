import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class Client extends Application {
    private static final int ROWS = 4;
    private static final int COLUMNS = 5;
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private Timeline timeline;
    // The batch size (e.g. - 20, 5)
    private int batchSize;
    // How many coins the user has flipped in their current batch
    private int count = 0;

    @Override
    public void start(Stage stage) throws Exception {
        socket = new Socket("localhost", 1234);
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
        batchSize = inputStream.readInt();

        final VBox vbox = new VBox();

        final HBox hBoxTop = new HBox();
        hBoxTop.setAlignment(Pos.CENTER);
        final HBox hBoxBottom = new HBox();

        final GridPane pennyGrid = new GridPane();
        pennyGrid.setPadding(new Insets(10, 10, 10, 10));
        pennyGrid.setHgap(10);
        pennyGrid.setVgap(10);

        hBoxTop.getChildren().add(pennyGrid);

        final Button button = new Button("Pass Pennies");
        button.setOnAction(event -> {
            if (count != batchSize) {
                return;
            }
            try {
                outputStream.writeInt(6000);
                count = 0;
                pennyGrid.getChildren().forEach(child -> ((Circle) child).setFill(Color.BLACK));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        hBoxTop.getChildren().add(button);

        // TODO bottom half of the UI (clean this up maybe)
        final GridPane scoreGrid = new GridPane();
        scoreGrid.setAlignment(Pos.CENTER);
        for (int i = 0; i < 5; i++) {
            StackPane pane1 = new StackPane();
            StackPane pane2 = new StackPane();
            pane1.setStyle("-fx-border-color: black");
            pane1.setPrefSize(70, 35);
            pane2.setStyle("-fx-border-color: black");
            pane2.setPrefSize(70, 35);
            String player;
            if (i == 4) {
                player = "Client";
            } else {
                player = String.format("Player %d", i + 1);
            }
            Text playerText = new Text(player);
            String score;
            if (i == 0) {
                score = "20";
            } else {
                score = "0";
            }
            Text scoreText = new Text(score);
            pane1.getChildren().add(playerText);
            pane2.getChildren().add(scoreText);
            scoreGrid.add(pane1, i, 0);
            scoreGrid.add(pane2, i, 1);
        }

        hBoxBottom.getChildren().add(scoreGrid);

        vbox.getChildren().add(hBoxTop);
        vbox.getChildren().add(hBoxBottom);

        final ReadThread readThread = new ReadThread(socket, inputStream);
        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(1), action -> {
            final int batches = readThread.getBatches();
            if (count != 0 || batches == 0) {
                return;
            }

            readThread.setBatches(batches - 1);
            initPennies(pennyGrid);
        }));
        readThread.start();
        timeline.play();

        stage.setScene(new Scene(vbox));
        stage.show();
    }

    public void initPennies(GridPane pennyGrid) {
        for (int n = 0; n < ROWS; n++) {
            for (int m = 0; m < COLUMNS; m++) {
                final Circle circle = new Circle(30, Color.BLACK);
                circle.setOnMouseClicked(event -> {
                    if (circle.getFill() == Color.GRAY) {
                        return;
                    }
                    ScaleTransition stHideFront = new ScaleTransition(Duration.millis(500), circle);
                    stHideFront.setFromX(1);
                    stHideFront.setToX(0);
                    ScaleTransition stShowBack = new ScaleTransition(Duration.millis(500), circle);
                    stShowBack.setFromX(0);
                    stShowBack.setToX(1);
                    stHideFront.setOnFinished(t -> {
                        circle.setFill(Color.GRAY);
                        stShowBack.play();
                    });
                    stHideFront.play();
                    count++;
                });
                pennyGrid.add(circle, m, n, 1, 1);
            }
        }
    }

    @Override
    public void stop() {
        try {
            socket.close();
            inputStream.close();
            outputStream.close();
            timeline.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}