import com.sun.tools.javac.Main;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.awt.*;
import java.net.URL;


public class MainApplicationScreen extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        //loading an image from a file
        final Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
        final URL imageResource = Main.class.getClassLoader().getResource("resources/icon.png");
        final java.awt.Image image = defaultToolkit.getImage(imageResource);

        //this is new since JDK 9
        final Taskbar taskbar = Taskbar.getTaskbar();

        try {
            //set icon for mac os (and other systems which do support this method)
            taskbar.setIconImage(image);
        } catch (final UnsupportedOperationException e) {
            System.out.println("The os does not support: 'taskbar.setIconImage'");
        } catch (final SecurityException e) {
            System.out.println("There was a security exception for: 'taskbar.setIconImage'");
        }

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
        button.setStyle("-fx-font-size:18");
        button.setOnAction(event -> {
            try {
                //  TODO get info and set info on batch size
                int batchSize = 20;
                System.out.println("Spinning up server");
                PennyServer server = new PennyServer(batchSize);
                server.start();
                System.out.println("Switching to PennyClient view");
                PennyClient client = new PennyClient(stage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        final Button button2 = new Button("Join Game");
        button2.setPrefSize(150,50);
        button2.setStyle("-fx-font-size:18");
        button2.setOnAction(event -> {
            try {
                System.out.println("Switching to PennyClient view");
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
