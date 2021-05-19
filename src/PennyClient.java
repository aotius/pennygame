import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class PennyClient {
    private static final int ROWS = 4;
    private static final int COLUMNS = 5;
    private final DataOutputStream outputStream;
    // The batch size (e.g. - 20, 5)
    private final int batchSize;
    // How many coins the user has flipped in their current batch
    private int count = 0;
    private boolean readyForNextBatch = true;

    public PennyClient(Stage stage) throws Exception {
        Socket socket = new Socket("localhost", 1234);
        DataInputStream inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
        batchSize = inputStream.readInt();

        final VBox vbox = new VBox();
        final VBox vTop = new VBox();
        final StackPane pane = new StackPane();
        final StackPane panepane = new StackPane();
        String text = "Click the pennies in the batch to flip them over\nClick the button on the right to pass to the next player\nScoreboard below displays where the pennies are in real-time";
        final Text instructions = new Text();
        instructions.setText(text);
        String text2 = "Instructions";
        final Text header = new Text();
        header.setFont(Font.font("arial", FontWeight.BOLD, FontPosture.REGULAR, 20));
        header.setText(text2);
        pane.getChildren().add(header);
        pane.setPadding(new Insets(10,10,10,10));
        panepane.setPadding(new Insets(10,10,10,10));
        panepane.getChildren().add(instructions);
        pane.setAlignment(Pos.CENTER);
        vTop.getChildren().add(pane);
        vTop.getChildren().add(panepane);

        final HBox hBoxTop = new HBox();
        hBoxTop.setAlignment(Pos.CENTER);
        hBoxTop.setSpacing(20);
        final HBox hBoxBottom = new HBox();
        hBoxBottom.setAlignment(Pos.CENTER);

        final StackPane gameBoard = new StackPane();
        gameBoard.setPadding(new Insets(10,10,10,10));
        final GridPane pennyGrid = new GridPane();
        pennyGrid.setPadding(new Insets(10, 10, 10, 10));
        pennyGrid.setAlignment(Pos.CENTER);
        pennyGrid.setHgap(10);
        pennyGrid.setVgap(10);
        pennyGrid.setPrefSize(450, 370);

        final Image board = new Image(getClass().getResourceAsStream("resources/gameboard.png"));
        final ImageView gameboard = new ImageView();
        gameboard.setImage(board);
        gameBoard.getChildren().add(gameboard);
        gameBoard.getChildren().add(pennyGrid);
        hBoxTop.getChildren().add(gameBoard);

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

        final VBox times = new VBox();
        //final Label labelFirstBatch = new Label("First Batch: ...");
        final StackPane pane3 = new StackPane();
        pane3.setStyle("-fx-border-color: black");
        pane3.setPrefSize(200, 70);
        final Label labelTotalTime = new Label("Total Time: ...");
        //times.getChildren().add(labelFirstBatch);
        pane3.getChildren().add(labelTotalTime);
        times.getChildren().add(pane3);
        hBoxBottom.getChildren().add(times);

        vbox.getChildren().add(vTop);
        vbox.getChildren().add(hBoxTop);
        vbox.getChildren().add(hBoxBottom);

        final ClientReadFromServerThread clientReadFromServerThread = new ClientReadFromServerThread(socket, inputStream);
        Timeline timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(0.05), action -> {
            final int batches = clientReadFromServerThread.getBatches();
            //System.out.printf("count: %d, batches: %d%n", count, batches);
            if (readyForNextBatch && batches != 0) {
                System.out.println("Refreshing board");
                clientReadFromServerThread.setBatches(batches - 1);
                initPennies(pennyGrid);
            }
        }));
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(1), action -> {
            for (int i = 0; i < clientReadFromServerThread.internalScoreboard.size(); i++) {
                updatePlayerScore(scoreGrid, i, clientReadFromServerThread.internalScoreboard.get(i));
            }
        }));
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(1), action -> {
            final String timeElapsed = clientReadFromServerThread.getTimeElapsed();
            if (timeElapsed == null) {
                return;
            }
            labelTotalTime.setText(timeElapsed);
        }));
        clientReadFromServerThread.start();
        timeline.play();

        final StackPane passPane = new StackPane();
        final Image passarrow = new Image(getClass().getResourceAsStream("resources/passarrow.png"));
        final ImageView button = new ImageView();
        button.setImage(passarrow);
        Text buttonText = new Text("Pass Pennies");
        buttonText.setFont(Font.font("Calibri", FontWeight.BOLD, 14));
        button.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (count < batchSize) {
                return;
            }
            try {
                outputStream.writeInt(6000);
                count = 0;
                readyForNextBatch = true;
                pennyGrid.getChildren().clear();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        passPane.getChildren().add(button);
        passPane.getChildren().add(buttonText);
        hBoxTop.getChildren().add(passPane);

        stage.setScene(new Scene(vbox));
        stage.setWidth(700);
        stage.setHeight(650);
        stage.setResizable(false);
        stage.setTitle("Agile Penny Game");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("resources/icon.png")));
        stage.show();
    }

    public void updatePlayerScore(GridPane scoreGrid, int playerIndex, int value) {
        ((Text)((StackPane)scoreGrid.getChildren().get(playerIndex * 2 + 1)).getChildren().get(0)).setText(String.valueOf(value));
    }

    public void initPennies(GridPane pennyGrid) {
        readyForNextBatch = false;

        if (pennyGrid.getChildren().isEmpty()) {
            int pennyCount = 0;
            for (int n = 0; n < ROWS; n++) {
                for (int m = 0; m < COLUMNS; m++) {
                    if (pennyCount >= batchSize) {
                        return;
                    }
                    final Image image = new Image(getClass().getResourceAsStream("resources/icon.png"));
                    final Image back = new Image(getClass().getResourceAsStream("resources/inverseicon.png"));
                    final Circle circle = new Circle(30, Color.GRAY);
                    final ImageView penny = new ImageView();
                    penny.setImage(image);
                    penny.setFitWidth(80);
                    penny.setFitHeight(80);
                    penny.setOnMouseClicked(event -> {
                        if (penny.getImage() == back) {
                            return;
                        }
                        ScaleTransition stHideFront = new ScaleTransition(Duration.millis(250), penny);
                        stHideFront.setFromX(1);
                        stHideFront.setToX(0);
                        stHideFront.setOnFinished(t -> {
                            penny.setImage(back);
                            ScaleTransition stShowBack = new ScaleTransition(Duration.millis(250), penny);
                            stShowBack.setFromX(0);
                            stShowBack.setToX(1);
                            //circle.setFill(Color.GRAY);
                            stShowBack.play();
                        });
                        stHideFront.play();
                        count++;
                    });
                    pennyGrid.add(penny, m, n, 1, 1);
                    pennyCount++;
                }
            }
        } else {
            fillAll(pennyGrid, Color.BLACK);
        }
    }

    // Assumes all children are circles
    private void fillAll(GridPane gridPane, Color color) {
        gridPane.getChildren().forEach(child -> ((Circle) child).setFill(color));
    }

}
