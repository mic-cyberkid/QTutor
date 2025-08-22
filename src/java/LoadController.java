
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.json.JSONObject;

/**
 * FXML Controller class
 *
 * @author pc
 */
public class LoadController implements Initializable {

    private Stage stage = Session.getStage();
    @FXML
    private ProgressBar progressbar;

    @FXML
    private AnchorPane rootPane;

    private double progressTrack = 0;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Check if service is running
        if (activeService()) {
            //progressbar = new ProgressBar(0);
            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent t) {
                    loadPage();
                }
            }));
            timeline.setCycleCount(0);
            timeline.play();
        } else {
            
            startServer();
            Platform.runLater(() -> {
                if (activeService()) {
                    loadPage();
                } 
            });

        }
    }

    public boolean activeService() {
        try {
            JSONObject serverResponse = ApiFunctions.serverStatus();
            String response = serverResponse.getString("status");
            if (response.equals("running")) {
                return true;
            } else {
                return false;
            }

        } catch (Exception e) {
            return false;
        }
    }

    public boolean startService() {

        try {
            // Run server process
            System.out.println("Starting server ...");
            String binfile = System.getProperty("user.dir")+"\\middleware\\ChatbotService.exe";
            System.out.println("Binfile : "+binfile);
            Process process = new ProcessBuilder(binfile.replace("file:/", "")).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            // Normally when we start the server it shows 6 Lines of logging messages so we use that as the check points
            String line;
            int line_count = 0;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                line_count += 1;
                progressTrack += ((1.0 / 6.0));
                System.out.println("Tracker :" + progressTrack);
                Platform.runLater(() -> {
                    progressbar.setProgress(progressTrack);
                });

                if (line.contains("running on http://")) {
                    Platform.runLater(() -> {
                        progressbar.setProgress(1);
                        loadPage();
                    });
                    return true;

                }
            }
            if (line_count >= 6) {

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean startServer() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            startService();
        });
        executor.shutdown();
        return false;
    }

    private void loadPage() {
        try {

            Parent root = FXMLLoader.load(getClass().getResource("/res/Home.fxml"));
            Scene scene = new Scene(root);

            stage.getIcons().add(new Image(getClass().getResource("/res/images/rounded/image1.png").toExternalForm()));
            stage.setScene(scene);
            //stage.close();

            System.out.println("New Stage: " + stage);
            stage.setResizable(true);

            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
