import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class HTMLTextFlow extends Region {
    private final WebView webView;
     private WebEngine webEngine;

    // Constructor
    public HTMLTextFlow() {
        // Create WebView to display HTML content
        webView = new WebView();
       
       webEngine = webView.getEngine();
        String initialContent = """
        <!DOCTYPE html>
        <html>
        <head>
        <style>
          body { font-family: Times New Roman; background-color: #2c3e50 ; margin: 3px; padding: 3px; }
          .message { max-width: 80%%; padding: px; border-radius: 10px; margin: 5px; }
          .user { background-color: #e0e0e0; color: black; align-self: flex-end; text-align: right; margin: 10px; padding: 10px; }
          .bot { background-color: #e0e0e0; color: black; align-self: flex-start; text-align: left; margin: 5px; padding: 10px; }
          .typing { font-weight: bold; animation: blink 1s infinite; }
          @keyframes blink { 0%% { opacity: 1; } 50%% { opacity: 0; } 100%% { opacity: 1; } }
          #chatbox { display: flex; flex-direction: column; }
        </style>
        </head>
        <body>
          <div id="chatbox"></div>
        </body>
        </html>
        """;
        webEngine.loadContent(initialContent);
        //Hide chat history
        
        //webEngine.setUserStyleSheetLocation(getClass().getResource("styles.css").toExternalForm());
        webEngine.executeScript("document.body.style.overflow = 'hidden';");
        webEngine.executeScript("document.body.style.wordWrap = 'break-word';");
        webEngine.executeScript("document.body.style.whtieSpace = 'normal';");
        webEngine.executeScript("document.body.style.margin = '0px';");
        webEngine.executeScript("document.body.style.padding = '0px';");
        VBox.setVgrow(this, Priority.ALWAYS);

        // Clip the region to create rounded corners
        
        Rectangle clip = new Rectangle();
        clip.setArcWidth(20);  // Adjust the arc width for roundness
        clip.setArcHeight(20); // Adjust the arc height for roundness
        setClip(clip); // Apply the clipping
        clip.widthProperty().bind(widthProperty());
        clip.heightProperty().bind(heightProperty());

        // Add WebView to this custom component
        getChildren().add(webView);
        webView.heightProperty().addListener((observable, oldValue, newValue) -> requestLayout());
    }

    // Set HTML content to WebView
    public void setHtmlContent(String htmlContent) {
        webView.getEngine().loadContent(htmlContent);
    }
    
    public WebEngine getWebEngine() {
        return webView.getEngine();
    }

    // Set a fixed height for the component
    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        double width = getWidth();
        double height = getHeight();

        // Adjust WebView size to match the fixed dimensions of the component
        webView.setPrefSize(width, height);
    }

    @Override
    protected double computePrefWidth(double height) {
        return Region.USE_COMPUTED_SIZE; // You can customize this width as per your needs
    }

    @Override
    protected double computePrefHeight(double width) {
        return Region.USE_COMPUTED_SIZE; // You can customize this height as per your needs
    }
}
