import javafx.application.Application;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.scene.layout.StackPane;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import netscape.javascript.JSObject;
import java.io.*;
import java.nio.file.*;
import java.util.Base64;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class WebViewMermaidApp extends Application {

    @Override
    public void start(Stage stage) {
        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();
        
        String  html = getClass().getResource("res/Mermaid.html").toExternalForm();
        System.out.println("Path:"+html);

        // Create the WebEngine to handle HTML/JS content
        webEngine.load(html);
        webEngine.setJavaScriptEnabled(true);
        webEngine.setUserDataDirectory(new File("C:/temp/webview"));
        webEngine.setOnAlert(event -> {
            System.out.println("JS Alert: " + event.getData());
        });

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
        if (newState == Worker.State.SUCCEEDED) {
            JSObject window = (JSObject) webEngine.executeScript("window");
            window.setMember("javaApp", new JavaBridge(stage));

            // Notify JS the bridge is ready
            webEngine.executeScript("onBridgeReady()");
        }
        });


       webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
       if (newState == Worker.State.SUCCEEDED) {
            JSObject window = (JSObject) webEngine.executeScript("window");
            window.setMember("javaApp", new JavaBridge(stage));

            // Optional: call back into JavaScript to confirm
            webEngine.executeScript("alert('Bridge connected: ' + !!window.javaApp);");
        }
        });

       
        /*
        // Set up a listener for the WebEngine 
        webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldState, Worker.State newState) {
                if (newState == Worker.State.SUCCEEDED) {
                    // Add JavaScript methods to communicate with Java
                    JSObject window = (JSObject) webEngine.executeScript("window");
                    System.out.println("Window:"+window);
                    window.setMember("javaApp", new JavaBridge(stage)); // Expose JavaBridge to JavaScript
                    //webEngine.executeScript(getMermaidScript());
                    JSObject wow = (JSObject) webEngine.executeScript("window");
                    System.out.println("Window:"+wow);
                }
            }
        });
       */
        
        
        // Add the WebView to the layout
        StackPane root = new StackPane();
        root.getChildren().add(webView);
        Scene scene = new Scene(root, 800, 600);
        stage.setTitle("Mermaid Diagram in WebView");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}

// JavaBridge class to interact with JavaScript
class JavaBridge {
    private Stage stage;

    public JavaBridge(Stage stage) {
        this.stage = stage;
    }

    // Method to save the file using FileChooser
    public void saveFile(String data, String filename) {
        try {
            // Let user choose where to save file
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SVG Files", "*.svg"));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Files", "*.png"));
            fileChooser.setInitialFileName(filename);

            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                if (filename.endsWith(".png")) {
                    // Handle PNG Saving
                    saveImageAsPNG(data, file);
                } else if (filename.endsWith(".svg")) {
                    // Handle SVG Saving
                    saveImageAsSVG(data, file);
                }
            }
        } catch (IOException e) {
            showError("Error", "An error occurred while saving the file.");
        }
    }

    // Method to save SVG file
    private void saveImageAsSVG(String svgData, File file) throws IOException {
        Files.write(file.toPath(), svgData.getBytes());
        showInfo("SVG Saved", "SVG file has been saved successfully.");
    }

    // Method to save PNG file
    private void saveImageAsPNG(String base64Data, File file) throws IOException {
        // Decode Base64 and write the data to a file
        byte[] decodedData = Base64.getDecoder().decode(base64Data.split(",")[1]);
        Files.write(file.toPath(), decodedData);
        showInfo("PNG Saved", "PNG file has been saved successfully.");
    }

    // Display error alert
    private void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Display info alert
    private void showInfo(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
