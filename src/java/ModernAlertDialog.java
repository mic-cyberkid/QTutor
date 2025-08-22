import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Modality;

public class ModernAlertDialog extends Dialog<Void> {

    public ModernAlertDialog(String title, String message, String imageUrl) {
        setTitle(title);
        
        // Set the style for the custom dialog (CSS)
        getDialogPane().getStyleClass().add("custom-alert-pane");
        
        // Create the dialog content container (VBox)
        VBox content = new VBox(10);
        content.setStyle("-fx-padding: 20;");
        
        // Create ImageView for the icon or image
        ImageView imageView = new ImageView(new Image(imageUrl));
        imageView.setFitWidth(50); // Set image width
        imageView.setFitHeight(50); // Set image height
        imageView.setStyle("-fx-margin-bottom: 10px;");
        
        // Create the message text
        Text messageText = new Text(message);
        
        // Create the OK button
        Button okButton = new Button("OK");
        okButton.getStyleClass().add("custom-button");
        okButton.setOnAction(e -> close());
        
        // Add image, message, and button to the VBox
        content.getChildren().addAll(imageView, messageText, okButton);
        
        // Set the content into the dialog
        getDialogPane().setContent(content);
        
        // Make the dialog modal
        initModality(Modality.APPLICATION_MODAL);
    }

    public static void main(String[] args) {
        Application.launch(ModernAlertDialogApp.class, args);
    }

    public static class ModernAlertDialogApp extends Application {
        @Override
        public void start(Stage primaryStage) {
            StackPane root = new StackPane();
            Button showAlertButton = new Button("Show Custom Alert");

            showAlertButton.setOnAction(e -> {
                // You can replace the image URL with any valid path or resource
                ModernAlertDialog alert = new ModernAlertDialog("Custom Alert", "This is a modern alert with an image!", "/res/images/rounded/image1.png");
                alert.showAndWait();
            });

            root.getChildren().add(showAlertButton);
            Scene scene = new Scene(root, 400, 300);
            scene.getStylesheets().add(getClass().getResource("/res/AlertStyles.css").toExternalForm());
            primaryStage.setTitle("Custom Modern Alert");
            primaryStage.setScene(scene);
            primaryStage.show();
        }
    }
}