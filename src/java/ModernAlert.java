import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.StageStyle;

public class ModernAlert {

    public static void show(String title, String message, AlertType type) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.initStyle(StageStyle.UNDECORATED); // No OS chrome
        dialog.setTitle(title);

        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 12; " +
            "-fx-border-radius: 12; " +
            "-fx-border-color: #ddd;"
        );

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setMinHeight(Region.USE_PREF_SIZE);

        HBox buttonBox = new HBox(10);
        buttonBox.setStyle("-fx-alignment: center-right;");

        Button okButton = new Button("OK");
        okButton.setStyle(
            "-fx-background-color: " + type.getColor() + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 6 14;"
        );
        okButton.setOnAction(e -> dialog.close());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        buttonBox.getChildren().addAll(spacer, okButton);

        container.getChildren().addAll(titleLabel, messageLabel, buttonBox);

        dialog.getDialogPane().setContent(container);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        Node closeButton = dialog.getDialogPane().lookupButton(ButtonType.CLOSE);
        closeButton.setVisible(false); // hide default close

        dialog.show();
    }

    public enum AlertType {
        SUCCESS("#4CAF50"), ERROR("#F44336"), INFO("#2196F3"), WARNING("#FFC107");
        private final String color;
        AlertType(String color) { this.color = color; }
        public String getColor() { return color; }
    }
}
