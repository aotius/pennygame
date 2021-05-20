import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
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
import java.util.Arrays;

public class PennyClient {
    // Images
    private static final Image GAMEBOARD = new Image(PennyClient.class.getResourceAsStream("resources/gameboard.png"));
    private static final Image IMAGE_FRONT = new Image(PennyClient.class.getResourceAsStream("resources/icon.png"));
    private static final Image IMAGE_BACK = new Image(PennyClient.class.getResourceAsStream("resources/inverseicon.png"));
    private static final Image ARROW = new Image(PennyClient.class.getResourceAsStream("resources/passarrow.png"));

    // Penny grid dimensions (rows are vertical, columns horizontal)
    private static final int ROWS = 4;
    private static final int COLUMNS = 5;

    // Connection
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private ClientReadFromServerThread clientReadFromServerThread;
    private Timeline timeline;

    // The batch size (e.g. - 20, 5)
    private int batchSize;
    // How many coins the user has flipped in their current batch
    private int count = 0;
    // Flag used to set the gameboard
    private boolean readyForNextBatch = true;

    public Socket getSocket() {
        return socket;
    }

    public DataInputStream getInputStream() {
        return inputStream;
    }

    public DataOutputStream getOutputStream() {
        return outputStream;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public PennyClient(Stage stage) throws Exception {
        socket = new Socket("localhost", 1234);
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());

        // Root VBox
        final VBox vBoxRoot = new VBox();
        vBoxRoot.setAlignment(Pos.CENTER);
        vBoxRoot.setPadding(new Insets(10, 10, 10, 10));

        // Headers/Instructions
        final Text header = new Text("Instructions");
        header.setFont(Font.font("arial", FontWeight.BOLD, FontPosture.REGULAR, 20));
        final Text instructions = new Text(String.join("\n", Arrays.asList(
                "Click the pennies in the batch to flip them over",
                "Click the button on the right to pass to the next player",
                "Scoreboard below displays where the pennies are in real-time"
        )));
        vBoxRoot.getChildren().add(header);
        vBoxRoot.getChildren().add(instructions);

        // HBox which will hold the gameboard and arrow to pass pennies to the next player
        final HBox hBoxPenny = new HBox();
        hBoxPenny.setAlignment(Pos.CENTER);
        hBoxPenny.setSpacing(20);

        // HBox which will hold the scoreboard and times (first penny/total time)
        final HBox hBoxBottom = new HBox();
        hBoxBottom.setAlignment(Pos.CENTER);

        // Create the actual board of pennies (A StackPane that contains an image of a board and a GridPane of pennies)
        final StackPane stackPaneGameBoard = new StackPane();
        stackPaneGameBoard.setPadding(new Insets(10,10,10,10));

        final GridPane pennyGrid = new GridPane();
        pennyGrid.setPadding(new Insets(10, 10, 10, 10));
        pennyGrid.setAlignment(Pos.CENTER);
        pennyGrid.setHgap(10);
        pennyGrid.setVgap(10);
        pennyGrid.setPrefSize(450, 370);

        final ImageView gameboard = new ImageView();
        gameboard.setImage(GAMEBOARD);

        stackPaneGameBoard.getChildren().add(gameboard);
        stackPaneGameBoard.getChildren().add(pennyGrid);
        hBoxPenny.getChildren().add(stackPaneGameBoard);

        // Create the scoreboard
        // TODO convert to HBox and make stackpanes VBox
        final GridPane scoreGrid = new GridPane();
        scoreGrid.setAlignment(Pos.CENTER);

        final String[] scoreboardNames = {"Player 1", "Player 2", "Player 3", "Player 4", "Client"};
        final int[] scoreboardScores = {20, 0, 0, 0, 0};
        for (int n = 0; n < 5; n++) {
            final StackPane stackPaneName = new StackPane();
            final StackPane stackPaneScore = new StackPane();

            stackPaneName.setStyle("-fx-border-color: black");
            stackPaneName.setPrefSize(70, 35);
            stackPaneScore.setStyle("-fx-border-color: black");
            stackPaneScore.setPrefSize(70, 35);

            final String name = scoreboardNames[n];
            final int score = scoreboardScores[n];

            stackPaneName.getChildren().add(new Text(name));
            stackPaneScore.getChildren().add(new Text(String.format("%,d", score)));

            scoreGrid.add(stackPaneName, n, 0);
            scoreGrid.add(stackPaneScore, n, 1);
        }

        hBoxBottom.getChildren().add(scoreGrid);

        // The first batch and total time texts
        // TODO cleanup
        final VBox vBoxTimes = new VBox();
        vBoxTimes.setAlignment(Pos.CENTER);
        vBoxTimes.setStyle("-fx-border-color: black");
        vBoxTimes.setPrefSize(200, 70);

        final Label labelFirstBatch = new Label("First Batch: ...");
        final Label labelTotalTime = new Label("Total Time: ...");
        vBoxTimes.getChildren().add(labelFirstBatch);
        vBoxTimes.getChildren().add(labelTotalTime);
        hBoxBottom.getChildren().add(vBoxTimes);

        vBoxRoot.getChildren().add(hBoxPenny);
        vBoxRoot.getChildren().add(hBoxBottom);

        // Create ClientReadFromServerThread and initialize timeline for UI updates
        clientReadFromServerThread = new ClientReadFromServerThread(this);
        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);

        // KeyFrame responsible for adding  pennies to the board
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(0.05), action -> {
            final int batches = clientReadFromServerThread.getBatches();
            if (readyForNextBatch && batches != 0) {
                clientReadFromServerThread.setBatches(batches - 1);
                initPennies(pennyGrid);
            }
        }));

        // KeyFrame responsible for updating the scoreboard
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(1), action -> {
            for (int i = 0; i < clientReadFromServerThread.internalScoreboard.size(); i++) {
                updatePlayerScore(scoreGrid, i, clientReadFromServerThread.internalScoreboard.get(i));
            }
        }));

        // KeyFrame responsible for updating times (i.e. - first batch and total time)
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(1), action -> {
            final String timeElapsedTotal = clientReadFromServerThread.getTimeElapsedTotal();
            if (timeElapsedTotal != null) {
                labelTotalTime.setText(String.format("Total Time: %s", timeElapsedTotal));
                clientReadFromServerThread.setTimeElapsedTotal(null);
            }

            final String timeElapsedFirstBatch = clientReadFromServerThread.getTimeElapsedFirstBatch();
            if (timeElapsedFirstBatch != null) {
                labelFirstBatch.setText(String.format("First Batch: %s", timeElapsedFirstBatch));
                clientReadFromServerThread.setTimeElapsedFirstBatch(null);
            }
        }));

        // Create the button to pass pennies to the next player
        final StackPane passPane = new StackPane();
        final ImageView button = new ImageView();
        button.setImage(ARROW);
        button.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (count < batchSize) {
                return;
            }
            try {
                // TODO magic numbers bad
                outputStream.writeInt(6000);
                count = 0;
                readyForNextBatch = true;
                pennyGrid.getChildren().clear();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        passPane.getChildren().add(button);
        hBoxPenny.getChildren().add(passPane);

        stage.setScene(new Scene(vBoxRoot));
        stage.setWidth(700);
        stage.setHeight(650);
        stage.setResizable(false);
        stage.setTitle("Agile Penny Game");
        stage.getIcons().add(IMAGE_FRONT);
        stage.show();
    }

    public void start() {
        clientReadFromServerThread.start();
        timeline.play();
    }

    public void stop() {
        try {
            socket.close();
            inputStream.close();
            outputStream.close();
            clientReadFromServerThread.stop();
            timeline.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updatePlayerScore(GridPane scoreGrid, int playerIndex, int value) {
        ((Text)((StackPane)scoreGrid.getChildren().get(playerIndex * 2 + 1)).getChildren().get(0)).setText(String.valueOf(value));
    }

    public void initPennies(GridPane pennyGrid) {
        readyForNextBatch = false;
        Logger.info("initPennies - Empty");
        int pennyCount = 0;
        for (int n = 0; n < ROWS; n++) {
            for (int m = 0; m < COLUMNS; m++) {
                if (pennyCount >= batchSize) {
                    return;
                }

                final ImageView penny = new ImageView(IMAGE_FRONT);
                penny.setFitWidth(80);
                penny.setFitHeight(80);
                penny.setOnMouseClicked(event -> {
                    if (penny.getImage() == IMAGE_BACK) {
                        return;
                    }
                    ScaleTransition stHideFront = new ScaleTransition(Duration.millis(250), penny);
                    stHideFront.setFromX(1);
                    stHideFront.setToX(0);
                    stHideFront.setOnFinished(t -> {
                        penny.setImage(IMAGE_BACK);
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
    }

}
