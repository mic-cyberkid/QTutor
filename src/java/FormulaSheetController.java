
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class FormulaSheetController implements Initializable {

    private static Stage stage;

    // variables to enable dragging
    private double xOffset = 0;
    private double yOffset = 0;
    private boolean isDragging = false;
    private boolean isMaximized = false;
    private double prevWidth, prevHeight, prevX, prevY;

    public FormulaSheetController(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private VBox resize_box;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        //Add custom dragging
        resize_box.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
            isDragging = true;

        });

        resize_box.setOnMouseReleased(event -> isDragging = false);

        resize_box.setOnMouseDragged(event -> {
            if (isDragging) {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            }
        });

    }

    @FXML
    void minimizeWindow(MouseEvent event) {
        stage.setIconified(true);
    }

    @FXML
    private void closeWindow(MouseEvent event) {
        stage.close();
    }

    public FormulaSheetController() {
    }

}
