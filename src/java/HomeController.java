import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * FXML Controller class
 *
 * @author pc
 */
public class HomeController implements Initializable {

    private Stage stage = Session.getStage();

     // variables to enable dragging
    private double xOffset = 0;
    private double yOffset = 0;
    private boolean isDragging = false;
    private boolean isMaximized = false;
    private double prevWidth, prevHeight, prevX, prevY;
    
 
    
    @FXML
    private VBox dynamicBox;
    @FXML
    private VBox homePage;
    
    @FXML
    private VBox resize_box;
    

    /**
     * Initializes the controller.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
         //Add custom dragging
            homePage.setOnMousePressed(event -> {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
                isDragging = true;
                
                
            });
            
            homePage.setOnMouseReleased(event -> isDragging = false);
            
            homePage.setOnMouseDragged(event -> {
                if(isDragging){
                    stage.setX(event.getScreenX() - xOffset);
                    stage.setY(event.getScreenY() - yOffset);
                }
            });
            
            
        
        
        
    }    
    
 

    
    private void switchPaneWithAnimation(VBox newPane, boolean slideLeft) {
        if (dynamicBox.getChildren().isEmpty()) {
            dynamicBox.getChildren().add(newPane);
            return;
        }

        VBox currentPane = (VBox) dynamicBox.getChildren().get(0);

        double width = dynamicBox.getWidth();
        double direction = slideLeft ? -1 : 1;

        // Slide out current pane
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), currentPane);
        slideOut.setFromX(0);
        slideOut.setToX(direction * width);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), currentPane);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        // Slide in new pane
        newPane.setTranslateX(-direction * width);  // opposite direction
        newPane.setOpacity(0.0);
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), newPane);
        slideIn.setFromX(-direction * width);
        slideIn.setToX(0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), newPane);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        slideOut.setOnFinished(e -> {
            dynamicBox.getChildren().clear();
            dynamicBox.getChildren().add(newPane);
            slideIn.play();
            fadeIn.play();
        });

        slideOut.play();
        fadeOut.play();
    }

    @FXML
    private void loginPage(ActionEvent event) {
        try {
            VBox loginPane = FXMLLoader.load(getClass().getResource("/res/Login.fxml"));
            switchPaneWithAnimation(loginPane, false); // slide left
        } catch (IOException iox) {
            iox.printStackTrace();
        }
    }

    @FXML
    private void signupPage(ActionEvent event) {
        try {
            VBox signUpPane = FXMLLoader.load(getClass().getResource("/res/signupPane.fxml"));
            switchPaneWithAnimation(signUpPane, true); // slide left
        } catch (IOException iox) {
            iox.printStackTrace();
        }
    }

    
    @FXML
    private void goBack(ActionEvent event) {
        try {
            VBox homePane = FXMLLoader.load(getClass().getResource("/res/Home.fxml"));
            switchPaneWithAnimation(homePage, true); // slide right
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
   
    
    @FXML
    private void minimizeWindow(ActionEvent event){
        Stage stage = (Stage)((javafx.scene.Node) event.getSource()).getScene().getWindow();
    }

    
    @FXML
    private void exitApp(ActionEvent event) {
        System.exit(0);
    }
    
    
    
    
    @FXML
    void closeWindow(MouseEvent event) {
        stage.close();
        System.exit(0);

    }

    @FXML
    void minimizeWindow(MouseEvent event) {
        stage.setIconified(true);
    }
    @FXML
    void resizeWindow(MouseEvent event) {
        //Work on changing icons for resize
        double screenWidth = Screen.getPrimary().getVisualBounds().getWidth();
        double screenHeight = Screen.getPrimary().getVisualBounds().getHeight();
        if(!isMaximized){
            prevX = stage.getX();
            prevY = stage.getY();
            prevWidth = stage.getWidth();
            prevHeight = stage.getHeight();
            
            stage.setX(0);
            stage.setY(0);
            stage.setWidth(screenWidth);
            stage.setHeight(screenHeight);
            isMaximized = true;
            ImageView img = new ImageView(new Image(getClass().getResource("res/images/icons/resize_white.png").toExternalForm()));
            img.setFitHeight(25);
            img.setFitWidth(25);
            resize_box.getChildren().set(0, img);
            
        }else{
            ImageView img = new ImageView(new Image(getClass().getResource("res/images/icons/maximize_white.png").toExternalForm()));
            img.setFitHeight(25);
            img.setFitWidth(25);
            resize_box.getChildren().set(0, img);
            stage.setX(prevX);
            stage.setY(prevY);
            stage.setWidth(prevWidth);
            stage.setHeight(prevHeight);
            isMaximized = false;
        }
        

    }
    
}
