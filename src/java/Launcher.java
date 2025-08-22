
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Launcher extends Application {

    // variables to enable dragging
    private double xOffset = 0;
    private double yOffset = 0;
    private boolean isDragging = false;

    @Override
    public void start(Stage stage) {
        System.out.println("Path " + System.getProperty("user.dir"));

        try {
            //Initialize stage
            stage.getIcons().add(new Image(getClass().getResource("res/images/rounded/image1.png").toExternalForm()));
            stage.setResizable(false);

            stage.initStyle(StageStyle.TRANSPARENT);
            Session.setStage(stage);

            Parent root = FXMLLoader.load(getClass().getResource("/res/Load.fxml"));
            Scene scene = new Scene(root, 643, 384, Color.TRANSPARENT);

            //Add custom dragging
            root.setOnMousePressed(event -> {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
                isDragging = true;

            });

            root.setOnMouseReleased(event -> isDragging = false);

            root.setOnMouseDragged(event -> {
                if (isDragging) {
                    stage.setX(event.getScreenX() - xOffset);
                    stage.setY(event.getScreenY() - yOffset);
                }
            });

            stage.setScene(scene);

            stage.show();

        } catch (IOException ex) {
            Logger.getLogger(Launcher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void minimizeWindow(ActionEvent event) {
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
    }

    public static void main(String args[]) {

        launch(args);
    }

}
