
import java.awt.Desktop;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.image.ImageView;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.json.JSONException;
import org.json.JSONObject;

public class WidgetsPaneController implements Initializable {

    @FXML
    private Label infoLabel;

    @FXML
    private PasswordField new_password;

    @FXML
    private PasswordField old_password;

    @FXML
    private Label prof_username;

    @FXML
    private Label total_chat;

    @FXML
    private ImageView profilePic;

    public Stage mainStage;

    User user;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        String username = Session.getAuthUser();
        prof_username.setText(username);
        total_chat.setText(String.valueOf(Session.getTotal_conversations()));

    }

    // Logout method
    @FXML
    public void logout(ActionEvent event) {
        loadPane(event, "res/Home.fxml");

    }

    //Load fxml pane
    public void loadPane(ActionEvent event, String page) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(page));
            Scene scene = new Scene(root);
            Node node = (Node) event.getSource();
            mainStage = (Stage) node.getScene().getWindow();
            mainStage.close();
            mainStage.getIcons().add(new Image(getClass().getResource("res/images/rounded/image1.png").toExternalForm()));
            mainStage.setScene(scene);
            mainStage.setResizable(true);
            mainStage.show();
        } catch (IOException ex) {
            Logger.getLogger(Launcher.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    //Load UnitsConverter
    public void loadFxmlPane(String page) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(page));
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.getIcons().add(new Image(getClass().getResource("res/images/rounded/image1.png").toExternalForm()));
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.setResizable(true);
            stage.setScene(scene);
            stage.show();

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    //Load UnitsConverter
    public void loadConverterPane(String page) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(page));
            Stage stage = new Stage();
            stage.getIcons().add(new Image(getClass().getResource("res/images/rounded/image1.png").toExternalForm()));
            new ConverterController(stage);
            //loader.setController(new ConverterController(stage));
            stage.setResizable(true);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    //Load Formular sheet
    public void loadFormulaPane(String page) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(page));
            Stage fstage = new Stage();
            fstage.initStyle(StageStyle.UNDECORATED);
            fstage.getIcons().add(new Image(getClass().getResource("res/images/rounded/image1.png").toExternalForm()));
            fstage.setResizable(true);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            fstage.setScene(scene);
            new FormulaSheetController(fstage);
            fstage.show();

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    // TODO : Allow dyanamic user profile picture
    // method to get user image and display
    public void setupProfile(String username) {
        try {
            String query = "select image from user_profiles where username = ?";

            // Get user image from server via API
            //mabr3 [We go work on this later
            /*
            while(result.next()){
                byte[] imageBytes = result.getBytes("image");
                ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
                Image image = new Image(bis);
                profilePic.setImage(image);
            }*/
        } catch (Exception e) {
            System.out.println("Error : " + e);
        }
    }

    // Method to update password
    @FXML
    public void updatePassword(ActionEvent event) throws Exception {
        if (old_password.getText().isEmpty() || new_password.getText().isEmpty()) {
            infoLabel.setText("Fill all fields!");
            infoLabel.setStyle("-fx-text-fill: red;");
            return;

        }
        JSONObject response = new JSONObject();
        try {

            response = ApiFunctions.changePassword(old_password.getText(), new_password.getText());
            if (response != null) {
                String detail = response.getString("msg");
                infoLabel.setStyle("-fx-text-fill: lime;");
                infoLabel.setText(detail);
            }

        } catch (IOException ex) {
            infoLabel.setText("An Error Occurred!");
            infoLabel.setStyle("-fx-text-fill: red;");

        } catch (JSONException e) {
            infoLabel.setStyle("-fx-text-fill: red;");
            String detail = response.getString("detail");
            infoLabel.setText(detail);
        }

    }

    // Clear all chats
    @FXML
    void clearAllChats(ActionEvent event) {

    }

    // Launch game pane
    void launchGames(ActionEvent event) {

    }

    // Launch units conveter
    @FXML
    void launchUnitsConverter(ActionEvent event) {
        //Run units Converter on different thread
        Platform.runLater(() -> {
            loadConverterPane("res/Convert.fxml");
        });

    }

    // Launch formula sheet
    @FXML
    void openFormulaSheet(ActionEvent event) {
        Platform.runLater(() -> {
            loadFormulaPane("res/FormulaSheet.fxml");
        });
    }

    private void minimizeWindow(ActionEvent event) {
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
    }

    public void launchLab() {
        try {
            URI uri = new File("src/res/phet_labs/UI.html").toURI();
            Desktop.getDesktop().browse(uri);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void launchLabSimulations(ActionEvent event) {
        Platform.runLater(() -> {
            launchLab();
        });
    }

}
