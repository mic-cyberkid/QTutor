import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import javafx.util.Duration;


public class UtilityMethods {
    
    //method to store image 
    public static void storeImage(String username, String imagePath){
        
        try{
            File file = new File(imagePath);
            byte[] imageBytes = new byte[(int)file.length()];
            FileInputStream fis = new FileInputStream(file);
            fis.read(imageBytes);
            
            
        } catch (Exception e) {
            System.out.println("Error: "+ e);
        }
    }
    
    // method to generate md5 hash to hash password
    public static String getMD5(String input) {
        try {
            // Create a MessageDigest instance for MD5
            MessageDigest md = MessageDigest.getInstance("MD5");

            // Update the MessageDigest with the bytes of the input string
            md.update(input.getBytes());

            // Perform the hash calculation and get the resulting bytes
            byte[] digest = md.digest();

            // Convert the byte array to a hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                // Convert each byte to a two-character hexadecimal representation
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            // Return the final MD5 hash as a string
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            // If MD5 is not available, throw an exception
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }
    
    // Method to show alert dialog
    // Display Warnings and Messages
    public static void showAlert(Alert.AlertType type, String title, String msg){
        Alert alert = new Alert(type);
        alert.setHeaderText(title);
        alert.setContentText(msg);
        
        //Set image
        alert.getDialogPane().setStyle("-fx-font-family: Century; -fx-font-size: 15px; -fx-background-radius: 10; -fx-border-radius: 10;");
        alert.show();
    }
    
    
    // Fade in effect
    public void applyFadeInEffect(Node node) {
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(2), node);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.play();
    }
    
    public void applyBounceEffect(Node node) {
        TranslateTransition transition = new TranslateTransition(Duration.seconds(0.5), node);
        transition.setByY(-10); // Move up 10px
        transition.setCycleCount(4); // Repeat 4 times
        transition.setInterpolator(Interpolator.EASE_OUT); // Bounce effect
        transition.setAutoReverse(true); // Auto reverse after each cycle
        transition.play();
    }
    
    public void applySlideInLeft(Node node) {
        TranslateTransition transition = new TranslateTransition(Duration.seconds(1), node);
        transition.setFromX(-300); // Start off-screen
        transition.setToX(0); // Move to normal position
        transition.play();
}
    
    public void applyRotateEffect(Node node) {
        RotateTransition rotateTransition = new RotateTransition(Duration.seconds(3), node);
        rotateTransition.setByAngle(360); // Rotate 360 degrees
        rotateTransition.setCycleCount(RotateTransition.INDEFINITE); // Infinite rotation
        rotateTransition.setInterpolator(Interpolator.LINEAR);
        rotateTransition.play();
}
    
    
}
