import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.json.JSONObject;

/**
 * FXML Controller class
 *
 * @author pc
 */
public class SignupPaneController implements Initializable {

    @FXML
    private Label loginInfoLabel;
    @FXML
    private TextField username_field;
    @FXML
    private PasswordField password_field;
    @FXML
    private Button backBtn;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    

  
    @FXML
    public void loginUser(ActionEvent event) {
        if(username_field.getText().isEmpty() || password_field.getText().isEmpty()){
            loginInfoLabel.setText("Please fill all fields.");
            return;
        }else{
            String username = username_field.getText();
            String password = password_field.getText();
            
            try{
                JSONObject response = ApiFunctions.registerUser(username, password);
                if(response != null){
                   loginInfoLabel.setStyle("-fx-font-family: Century; -fx-font-size: 14px; -fx-text-fill: #61bf49;");
                   loginInfoLabel.setText("Signed Up!");
                   UtilityMethods.showAlert(Alert.AlertType.INFORMATION, "Quark Tutor", "Successfully signed up. Login to continue");
                    Platform.runLater(() -> {
                        try {
                            Thread.sleep(3000);
                            loadHomePane(event);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(SignupPaneController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    });
                }else{
                    loginInfoLabel.setStyle("-fx-font-family: Century; -fx-font-size: 14px; -fx-text-fill: red;");
                    loginInfoLabel.setText("Username Exists!");
                }
                
                
                
            }catch(IOException e){
                System.out.println("Error : "+e.getMessage());
                loginInfoLabel.setStyle("-fx-font-family: Century; -fx-font-size: 14px; -fx-text-fill: #f54949;");
                loginInfoLabel.setText("An error occured!");
                return;
            }
        
        }

    }
    
    // Back to home page
    public void loadHomePane(ActionEvent event){
        try {
                Parent root = FXMLLoader.load(getClass().getResource("/res/Home.fxml"));
                Scene scene = new Scene(root);
                Node node = (Node)event.getSource();
                Stage stage = (Stage) node.getScene().getWindow();
                stage.setScene(scene);
                stage.setResizable(true);
                stage.show();
            } catch (IOException ex) {
                Logger.getLogger(Launcher.class.getName()).log(Level.SEVERE, null, ex);
            } 

    }
    
    
    public void loadChatPane(ActionEvent event){
        try {
                Parent root = FXMLLoader.load(getClass().getResource("/res/ChatPane.fxml"));
                Scene scene = new Scene(root);
                Node node = (Node)event.getSource();
                Stage stage = (Stage) node.getScene().getWindow();
                stage.setScene(scene);
                stage.setResizable(true);
                stage.show();
            } catch (IOException ex) {
                Logger.getLogger(Launcher.class.getName()).log(Level.SEVERE, null, ex);
            } 

    }
    

    @FXML
    private void backToHome(ActionEvent event) {
        try {
                
                Parent root = FXMLLoader.load(getClass().getResource("/res/Home.fxml"));
                Scene scene = new Scene(root);
                Node node = (Node)event.getSource();
                Stage stage = (Stage) node.getScene().getWindow();
                stage.setScene(scene);
                stage.setResizable(true);
                stage.show();
            } catch (IOException ex) {
                Logger.getLogger(Launcher.class.getName()).log(Level.SEVERE, null, ex);
            } 

    }
    
    
}
