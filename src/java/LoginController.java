import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.sql.ResultSet;
import java.util.Optional;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.StageStyle;
import org.json.JSONObject;


public class LoginController implements Initializable {
    
   
    private Stage  stage = Session.getStage();

    public LoginController(){}

    @FXML
    private Label loginInfoLabel;

    @FXML
    private TextField password_field;

    @FXML
    private TextField username_field;
    
    @FXML
    private Button backBtn;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("Stage:"+stage);
    }
    
    @FXML
    public void loginUser(ActionEvent event) {
        if(username_field.getText().isEmpty() || password_field.getText().isEmpty()){
            loginInfoLabel.setText("Please fill all fields.");
            
        }else{
            String username = username_field.getText();
            String password = password_field.getText();
            
            try{
                    JSONObject response = ApiFunctions.loginUser(username, password);
                    if(response != null){
                        // Set session
                        Session.setAuthUser(response.getString("username"));
                        Session.setAuthToken(response.getString("access_token"));
                        loginInfoLabel.setStyle("-fx-text-fill: lime; -fx-font-family: Century; -fx-font-size: 14px;");
                        loginInfoLabel.setText("Login Successful!");
                        // Get all conversations and set total conversations for user session
                        JSONObject conversations = ApiFunctions.loadConversations();
                        if(conversations != null){
                            Session.setTotal_conversations(conversations.getInt("total"));
                            
                        }
                        
                        loadChatPane(event);
                        return;
                    }else{
                        loginInfoLabel.setStyle("-fx-text-fill: red; -fx-font-family: Century; -fx-font-size: 14px;");
                        loginInfoLabel.setText("Wrong Details!");
                    }
                
            }catch(IOException e){
                e.printStackTrace();
                loginInfoLabel.setStyle("-fx-text-fill: red; -fx-font-family: Century; -fx-font-size: 14px;");
                loginInfoLabel.setText("An error occured during login!");
              
            }

        }

    }
    
    //Method to load chat pane
    public void loadChatPane(ActionEvent event){
        
        try {    
                Parent root = FXMLLoader.load(getClass().getResource("/res/ChatPane.fxml"));
                Scene scene = new Scene(root);
                stage.getIcons().add(new Image(getClass().getResource("/res/images/rounded/image1.png").toExternalForm()));
                stage.setScene(scene);
                stage.setResizable(true);
                stage.show();
            } catch (IOException ex) {
                ex.printStackTrace();
            } 

    }
    
    

    //Method to load home pane
    @FXML
    private void backToHome(ActionEvent event) {
        try {
                
                Parent root = FXMLLoader.load(getClass().getResource("/res/Home.fxml"));
                Scene scene = new Scene(root);
                System.out.println(stage.getScene().getRoot().getChildrenUnmodifiable());
                
                Node node = (Node)event.getSource();
                Stage stage = (Stage) node.getScene().getWindow();
                stage.setScene(scene);
                stage.setResizable(true);
                stage.show();
            } catch (IOException ex) {
                ex.printStackTrace();
            } 

    }
    
    
  
    
     //Method to load home pane
    public void openPasswordResetPane(MouseEvent event) {
        // check if user has input username
        
            try {
               
                Parent root = FXMLLoader.load(getClass().getResource("/res/PasswordReset.fxml"));
                Scene scene = new Scene(root);
                System.out.println(stage.getScene().getRoot().getChildrenUnmodifiable());
                
                Node node = (Node)event.getSource();
                Stage stage = new Stage();
                stage.initStyle(StageStyle.TRANSPARENT);
                stage.setScene(scene);
                stage.setResizable(true);
                stage.show();
            } catch (IOException ex) {
                ex.printStackTrace();
            } 
        
        

    }

   


}
