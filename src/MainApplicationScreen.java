import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainApplicationScreen extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        final VBox vbox = new VBox();

        vbox.setPadding(new Insets(10,10,10,10));
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(20);

        final Image titlelogo = new Image(getClass().getResourceAsStream("resources/titlescreen.png"));
        final ImageView title = new ImageView();
        title.setImage(titlelogo);
        vbox.getChildren().add(title);
        final Button button = new Button("Host Game");
        button.setPrefSize(150,50);
        button.setStyle("-fx.font-size:40");
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
        button2.setPrefSize(150,50);
        button2.setStyle("-fx.font-size:40");
        button2.setOnAction(event -> {
            try {
                PennyClient client = new PennyClient(stage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        vbox.getChildren().add(button);
        vbox.getChildren().add(button2);

        stage.setScene(new Scene(vbox));
        stage.setWidth(750);
        stage.setHeight(650);
        stage.setResizable(false);
        stage.setTitle("Agile Penny Game");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("resources/icon.png")));
        stage.show();
    }
}