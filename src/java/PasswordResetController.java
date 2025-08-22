
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.regex.Pattern;
import javafx.scene.Node;
import javafx.stage.Stage;

public class PasswordResetController {

    // These fx:id names MUST match the ones in your PasswordReset.fxml
    @FXML private PasswordField new_password;
    @FXML private PasswordField confirm_password;
    @FXML private CheckBox trustCheckBox;
    @FXML private Button resetButton;
    @FXML private Label feedbackLabel;
    @FXML private Button backBtn;


    @FXML
    public void initialize() {
        // Hook up handlers (you can also set these in the FXML)
        
        resetButton.setOnAction(this::onResetClicked);
        backBtn.setOnAction(e -> onBackClicked(e));
        feedbackLabel.setText("");
    }

    private void onBackClicked(ActionEvent e) {
        Node node = (Node) e.getSource();
        Stage stage = (Stage) node.getScene().getWindow();
        stage.close();
        // Implement navigation as needed
        // Example: close the window, or go back to previous scene
    }

    private void onResetClicked(ActionEvent event) {
        // 1) Ask for username using TextInputDialog
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Enter Username");
        dialog.setHeaderText("Please enter your username");
        dialog.setContentText("Username:");

        Optional<String> maybeUsername = dialog.showAndWait();
        if (maybeUsername.isEmpty()) {
            setFeedback("Username entry cancelled.", false);
            return;
        }

        String username = maybeUsername.get().trim();
        if (username.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Invalid input", "Username cannot be empty.");
            return;
        }

        // 2) Local validations for password fields and checkbox
        String p1 = new_password.getText();
        String p2 = confirm_password.getText();

        if (p1 == null || p1.isEmpty() || p2 == null || p2.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Invalid input", "Password fields cannot be empty.");
            return;
        }

        if (!p1.equals(p2)) {
            showAlert(Alert.AlertType.ERROR, "Mismatch", "Passwords do not match.");
            return;
        }

        if (!trustCheckBox.isSelected()) {
            showAlert(Alert.AlertType.WARNING, "Confirmation required", "Please confirm the checkbox before resetting password.");
            return;
        }

        if (!isPasswordStrong(p1)) {
            showAlert(Alert.AlertType.ERROR, "Weak password",
                    "Password must be at least 8 characters and include an uppercase letter, "
                            + "a lowercase letter, a digit and a special character.");
            return;
        }

        // 3) All checks passed — perform the POST in a background Task so UI remains responsive
        resetButton.setDisable(true);
        setFeedback("Resetting password...", true);

        Task<JSONObject> task = new Task<>() {
            @Override
            protected JSONObject call() throws Exception {
                // Call the method that performs the POST and returns JSON.
                // Replace this with your existing updatePasswordManually(...) method if you already have one.
                return ApiFunctions.updatePasswordManually(username, p1);
            }
        };

        task.setOnSucceeded(t -> {
            JSONObject resp = task.getValue();
            resetButton.setDisable(false);
            handleResponse(resp);
        });

        task.setOnFailed(t -> {
            resetButton.setDisable(false);
            Throwable ex = task.getException();
            setFeedback("Error: " + (ex == null ? "unknown" : ex.getMessage()), false);
            showAlert(Alert.AlertType.ERROR, "Network / Server Error", ex == null ? "Unknown error" : ex.getMessage());
        });

        new Thread(task, "pwd-reset-thread").start();
    }

    private void handleResponse(JSONObject resp) {
        if (resp == null) {
            setFeedback("Empty response from server.", false);
            return;
        }

        boolean success = resp.optBoolean("success", false);
        String message = resp.optString("message", resp.toString());

        if (success) {
            setFeedback("Password reset successful.", true);
            showAlert(Alert.AlertType.INFORMATION, "Success", message.isEmpty() ? "Password updated." : message);
            // Optionally clear fields
            new_password.clear();
            confirm_password.clear();
        } else {
            setFeedback("Failed: " + message, false);
            showAlert(Alert.AlertType.ERROR, "Failed", message.isEmpty() ? "Unknown error" : message);
        }
    }

    // Helper to update feedback label safely on UI thread
    private void setFeedback(String text, boolean positive) {
        Platform.runLater(() -> {
            feedbackLabel.setText(text);
            // Optionally tweak style classes for positive/negative feedback
            feedbackLabel.getStyleClass().removeAll("positive", "negative");
            feedbackLabel.getStyleClass().add(positive ? "positive" : "negative");
        });
    }

    private void showAlert(Alert.AlertType type, String title, String body) {
        Platform.runLater(() -> {
            Alert a = new Alert(type);
            a.setTitle(title);
            a.setHeaderText(null);
            a.setContentText(body);
            a.showAndWait();
        });
    }

    // Basic strong password check
    private boolean isPasswordStrong(String pw) {
        if (pw == null) return false;
        // At least 8 chars, at least one lowercase, uppercase, digit, special char
        Pattern p = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$");
        return p.matcher(pw).matches();
    }

    
    
}
