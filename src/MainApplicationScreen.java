import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.text.Text;
import javafx.scene.control.TextField;


public class MainApplicationScreen extends Application {
    private static final Image TITLE_SCREEN = new Image(MainApplicationScreen.class.getResourceAsStream("resources/titlescreen.png"));
    private static final Image ICON = new Image(MainApplicationScreen.class.getResourceAsStream("resources/icon.png"));
    private PennyServer pennyServer;
    private PennyClient pennyClient;

    @Override
    public void start(Stage stage) {
        final VBox vBoxRoot = new VBox();
        vBoxRoot.setPadding(new Insets(10,10,10,10));
        vBoxRoot.setAlignment(Pos.CENTER);
        vBoxRoot.setSpacing(20);

        final ImageView imageViewLogo = new ImageView(TITLE_SCREEN);
        vBoxRoot.getChildren().add(imageViewLogo);

        final Text textInstructions = new Text("Type the desired batch size below if you are the host");
        final Text textInvalidInput = new Text();
        textInvalidInput.setFont(Font.font("arial", FontWeight.BOLD, FontPosture.REGULAR, 20));
        textInvalidInput.setFill(Color.RED);
        vBoxRoot.getChildren().add(textInstructions);
        vBoxRoot.getChildren().add(textInvalidInput);

        final TextField textFieldBatchSize = new TextField();
        textFieldBatchSize.setMaxWidth(100);
        vBoxRoot.getChildren().add(textFieldBatchSize);

        final Button buttonHostGame = new Button("Host Game");
        buttonHostGame.setPrefSize(150,50);
        buttonHostGame.setStyle("-fx-font-size: 18");
        buttonHostGame.setOnAction(event -> {
            try {
                final int batchSize = Integer.parseInt(textFieldBatchSize.getText());
                if (batchSize < 0 || batchSize > 20) {
                    throw new NumberFormatException();
                }

                // Start the server
                pennyServer = new PennyServer();
                pennyServer.start();

                // Start the client
                pennyClient = new PennyClient(stage);

                // Handshake - Batch size
                pennyClient.getOutputStream().writeInt(batchSize);
                final int serverBatchSize = pennyClient.getInputStream().readInt();
                if (batchSize != serverBatchSize) {
                    // TODO log because if this happens it is bad
                }
                pennyClient.setBatchSize(serverBatchSize);
                pennyClient.start();

                Logger.info(batchSize + "");

            } catch (NumberFormatException e) {
                textInvalidInput.setText("You must enter an integer between 1 and 20 (inclusive)");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        final Button buttonJoinGame = new Button("Join Game");
        buttonJoinGame.setPrefSize(150,50);
        buttonJoinGame.setStyle("-fx-font-size:18");
        buttonJoinGame.setOnAction(event -> {
            try {
                pennyClient = new PennyClient(stage);

                // Handshake - Batch size
                pennyClient.getOutputStream().writeInt(-1);
                final int serverBatchSize = pennyClient.getInputStream().readInt();
                if (serverBatchSize == -1) {
                    // TODO log because if this happen it is bad
                }
                pennyClient.setBatchSize(serverBatchSize);
                pennyClient.start();

                Logger.info(serverBatchSize + "");

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        vBoxRoot.getChildren().add(buttonHostGame);
        vBoxRoot.getChildren().add(buttonJoinGame);

        stage.setScene(new Scene(vBoxRoot));
        stage.setWidth(750);
        stage.setHeight(650);
        stage.setResizable(false);
        stage.setTitle("Agile Penny Game");
        stage.getIcons().add(ICON);
        stage.show();
    }
}
