
import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.Random;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebEngine;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;

/**
 * FXML Controller class
 * mabr3 [TODO](Continue comments tomrrow)
 * This class controls the main chat pane of the application, handling UI
 * interactions, communication with a backend LLM service, and managing chat
 * history.
 */
public class ChatPaneController implements Initializable {

    // FXML Components injected from the FXML file
    @FXML
    private BorderPane MainPane;
    @FXML
    private ScrollPane HistoryPane;
    @FXML
    private Accordion chatHistoryAccordion;
    @FXML
    private TextArea userMessage;
    @FXML
    private ScrollPane chatPane;
    @FXML
    private VBox chatsBox;

    @FXML
    private Pane welcomeDialog;

    @FXML
    private Label welcomeMsgBox;

    @FXML
    private TitledPane noHistory;

    @FXML
    private Button newChatBtn;
    @FXML
    private ImageView sendBtn;
    @FXML
    private ProgressIndicator chatProgress;

    public Accordion widget;

    @FXML
    private HBox menuPane;
    @FXML
    private HBox bottomPane;

    @FXML
    private Button createQuizPrompt;
    @FXML
    private Button explainConceptPrompt;
    @FXML
    private VBox resize_box;

    private Pane welcomePane;

    // Variables for window dragging and resizing
    private double xOffset = 0;
    private double yOffset = 0;
    private boolean isDragging = false;
    private boolean isMaximized = false;
    private double prevWidth, prevHeight, prevX, prevY;

    // Stage reference, typically obtained from Session or passed during initialization
    private Stage stage = Session.getStage();

    private Timeline blinkTimeline; // Not explicitly used in provided snippet, but kept

    private HTMLTextFlow chatWebView; // Custom JavaFX component to embed WebView
    private WebEngine webEngine; // WebEngine for interacting with the HTML/JS content

    private boolean newChat; // Flag to indicate if it's a new chat session
    private ChatState chatState; // Current state of the chat pane

    // Getter for chatState
    public ChatState getChatState() {
        return chatState;
    }

    private volatile boolean stopStreaming = false; // Flag to stop the streaming process
    private volatile boolean isStreaming = false; // Flag to indicate if streaming is active

    private Thread streamingThread; // Thread for handling backend streaming

    // Resource path for the chat UI HTML file
    String ChatUI = getClass().getResource("res/ChatUI.html").toExternalForm();

    /**
     * Initializes the controller class. This method is called automatically
     * after the FXML file has been loaded.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupStage();
        newChat = true; // Initialize newChat flag
    }

    // Enum for different chat states
    public enum ChatState {
        /* State of Chat Pane
        |  NEW_USER for new users with zero history, 
        |  USER_WITH_HISTORY for user with history just logged in,
        |  NEW_CHAT_FOR_USER_WITH_HISTORY for 'new chat' for user with history (pressed new chat button),
        |  CONTINUE_CHAT for continue chat (pressed load chat) or just sent a message
         */
        NEW_USER,
        USER_WITH_HISTORY,
        NEW_CHAT_FOR_USER_WITH_HISTORY,
        CONTINUE_CHAT
    }

    /**
     * Sets the current chat state and performs any associated UI updates or
     * logic.
     *
     * @param state The new ChatState to set.
     */
    private void setChatState(ChatState state) {
        this.chatState = state;
        // Additional UI updates or logic based on state can be added here
    }

    /**
     * Loads custom fonts for the application.
     */
    public void loadFont() {
        Font.loadFont(getClass().getResourceAsStream("/fonts/Inter-Regular.otf"), 10);
    }

    /**
     * Sets up the main stage, including UI initialization, font loading,
     * WebView setup, and initial chat state.
     */
    public void setupStage() {
        initializeUI();
        loadFont();
        initializeWebView();
        initializeChatState();
    }

    /* ******************************
    * UTILITY AND APP LOGIC METHODS
    *******************************/
    /**
     * Adds custom dragging functionality to a given Node, allowing the window
     * to be moved by dragging the node. Includes snap-to-edge functionality.
     *
     * @param node The Node to attach the dragging listener to.
     */
    public void addCustomDragging(Node node) {
        node.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
            isDragging = true;
        });

        node.setOnMouseDragReleased(event -> {
            isDragging = false;
            checkForSnap(stage);
        });

        node.setOnMouseDragged(event -> {
            if (isDragging) {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            }
        });
    }

    /**
     * Initializes the chat state based on whether the user has existing
     * conversations.
     */
    private void initializeChatState() {
        if (Session.getTotal_conversations() < 1) {
            // Fresh chat, fresh user
            setChatState(ChatState.NEW_USER);
        } else {
            // Remove our dummy chat history pane if history exists
            chatHistoryAccordion.getPanes().remove(noHistory);
            // Populate history pane with existing chats
            loadChatsPane();
            // Set state to user with history
            setChatState(ChatState.USER_WITH_HISTORY);
        }
    }

    /**
     * Initializes the UI components and their properties.
     */
    private void initializeUI() {
        addCustomDragging(MainPane); // Enable dragging for the main pane
        MainPane.setLeft(null); // Hide Chat History initially
        // Add keyboard Listeners for shortcuts
        MainPane.setOnKeyPressed(eh -> {
            //Handle shortcut for various features -- we go add more later
        });
        chatsBox.setSpacing(5);
        chatWebView = new HTMLTextFlow(); // Initialize custom WebView component

        // Configure user message TextArea for dynamic height and wrapping
        userMessage.setWrapText(true);
        userMessage.setPrefHeight(100);
        userMessage.textProperty().addListener((observable, oldValue, newValue) -> {
            // Calculate content height based on number of lines
            double contentHeight = userMessage.getText().split("\n").length * 20;
            double newheight = Math.max(contentHeight + 50, 70); // Minimum height of 70, plus padding

            // Adjust TextArea and bottomPane height, ensuring it doesn't exceed MainPane height
            userMessage.setPrefHeight(newheight);
            
            if (newheight > 100 && newheight <= MainPane.getPrefHeight()) {
                bottomPane.setPrefHeight(newheight);
            } else if (newheight < MainPane.getPrefHeight()) {
                userMessage.setPrefHeight(newheight);
            }
        });
        userMessage.setOnKeyPressed(eh -> {
            // Shortcut to send message
            if(eh.getCode() == KeyCode.ENTER && eh.isShiftDown()){
                //Send user message
                processUserInput();
                eh.consume();
            }
        });

        // Bind bottomPane's max height to MainPane's height for responsiveness
        bottomPane.maxHeightProperty().bind(MainPane.heightProperty());

        // Configure chatsBox for welcome dialog display
        chatsBox.setAlignment(Pos.CENTER);
        chatsBox.setFillWidth(false);

        editImageButton(sendBtn); // Apply styling to the send button image

        welcomePane = welcomeDialog; // Store reference to the welcome dialog pane
        welcomeMsgBox.setText("Welcome " + Session.getAuthUser() + "!"); // Set welcome message

        widget = loadWidgets(); // Load and store the widgets pane
    }

    /**
     * Initializes the WebView component, loads the ChatUI.html, and enables
     * JavaScript.
     */
    private void initializeWebView() {
        // Apply styling to the WebView container
        chatWebView.setStyle("-fx-background-color:  linear-gradient(to bottom, #2c3e50, #4ca1af); -fx-background-radius: 10px; -fx-border-radius: 10px;");
        chatWebView.getWebEngine().load(ChatUI); // Load the HTML UI
        webEngine = chatWebView.getWebEngine(); // Get the WebEngine instance
        webEngine.setJavaScriptEnabled(true); // Enable JavaScript
        webEngine.setUserDataDirectory(new File("temp/webview")); // Set user data directory for WebView
        webEngine.setOnAlert(event -> {
            showAlert(AlertType.INFORMATION, "Quark Tutor", event.getData());
        });
    }

    /**
     * Executes a JavaScript function in the WebView.
     *
     * @param functionName The name of the JavaScript function to call.
     * @param markdownMessage The message content to pass to the function.
     * @param senderClass The CSS class for the sender (e.g., "user" or "bot").
     */
    private void executeJavaScript(String functionName, String markdownMessage, String senderClass) {
        Gson gson = new Gson();
        String jsonMessage = gson.toJson(markdownMessage);
        String jsonClass = gson.toJson(senderClass);
        String script = functionName + "(" + jsonMessage + ", " + jsonClass + ");";
        chatWebView.getWebEngine().executeScript(script);
    }

    /**
     * Checks if the window should snap to screen edges (maximize or snap to
     * half).
     *
     * @param stage The current Stage.
     */
    public void checkForSnap(Stage stage) {
        double screenWidth = Screen.getPrimary().getVisualBounds().getWidth();
        double screenHeight = Screen.getPrimary().getVisualBounds().getHeight();
        double x = stage.getX();
        double y = stage.getY();

        if (y <= 0) {
            maximizeWindow(stage, screenWidth, screenHeight);
        } else if (x <= 0) {
            snapWindow(stage, 0, 0, screenWidth / 2.0, screenHeight);
        } else if (x + stage.getWidth() >= screenWidth) {
            snapWindow(stage, screenWidth / 2, 0, screenWidth / 2, screenHeight);
        }
    }

    /**
     * Snaps the window to a specific position and size.
     *
     * @param stage The current Stage.
     * @param x The X coordinate for the window.
     * @param y The Y coordinate for the window.
     * @param screenWidth The width to set the window to.
     * @param screenHeight The height to set the window to.
     */
    public void snapWindow(Stage stage, double x, double y, double screenWidth, double screenHeight) {
        stage.setX(x);
        stage.setY(y);
        stage.setWidth(screenWidth);
        stage.setHeight(screenHeight);
        isMaximized = false;
    }

    /**
     * Restores the window to its previous size and position.
     *
     * @param stage The current Stage.
     */
    public void restoreWindow(Stage stage) {
        stage.setX(prevX);
        stage.setY(prevY);
        stage.setWidth(prevWidth);
        stage.setHeight(prevHeight);
        isMaximized = false;
    }

    /**
     * Maximizes or restores the window.
     *
     * @param stage The current Stage.
     * @param screenWidth The width of the primary screen.
     * @param screenHeight The height of the primary screen.
     */
    public void maximizeWindow(Stage stage, double screenWidth, double screenHeight) {
        if (!isMaximized) {
            prevX = stage.getX();
            prevY = stage.getY();
            prevWidth = stage.getWidth();
            prevHeight = stage.getHeight();

            stage.setX(0);
            stage.setY(0);
            stage.setWidth(screenWidth);
            stage.setHeight(screenHeight);
            isMaximized = true;
        } else {
            restoreWindow(stage);
        }
    }

    /**
     * Toggles the visibility of the chat history pane.
     *
     * @param event The MouseEvent that triggered this action.
     */
    @FXML
    void showChatHistory(MouseEvent event) {
        if (MainPane.getLeft() == null) {
            MainPane.setLeft(HistoryPane);
        } else {
            MainPane.setLeft(null);
        }
    }

    /**
     * Adds a message to the chat UI by executing JavaScript in the WebView.
     *
     * @param markdownMessage The message content in Markdown format. (the text response from wanna server)
     * @param isUser True if the message is from the user, false if from the
     * bot.
     */
    public void addMessage(String markdownMessage, boolean isUser) {
        String senderClass = isUser ? "user" : "bot";
        executeJavaScript("renderMarkdown", markdownMessage, senderClass);
    }

    /**
     * Loads the widgets pane from an FXML file.
     *
     * @return An Accordion containing the widgets.
     */
    public Accordion loadWidgets() {
        try {
            Accordion widget = FXMLLoader.load(getClass().getResource("/res/widgetsPane.fxml"));
            return widget;
        } catch (Exception e) {
            e.printStackTrace();
            return new Accordion(); // Return an empty Accordion on error
        }
    }
    

    /**
     * Processes user input: sends the message to the backend, displays it, and
     * handles the streaming response.
     */
    private void processUserInput() {
        if (!isStreaming) {
            // Check if user message is empty
            if (userMessage.getText().isEmpty()) {
                UtilityMethods.showAlert(AlertType.INFORMATION, "Chat", "Please enter a message!");
                return;
            }

            // Remove welcome dialog if present and set chat box alignment
            if (chatsBox.getAlignment() == Pos.CENTER) {
                chatsBox.getChildren().clear();
                chatsBox.setAlignment(Pos.TOP_LEFT);
                chatsBox.getChildren().add(chatWebView);
                chatsBox.setFillWidth(true);
            }

            String message = userMessage.getText();
            userMessage.clear(); // Clear the input field
            isStreaming = true; // Set streaming flag to true
            addMessage(message, true); // Display user message in the chat UI
            Platform.runLater(() -> chatProgress.setVisible(true)); // Show progress indicator

            // Change send button icon to stop icon and start blinking effect
            Image stopImage = new Image(getClass().getResource("res/images/icons/close_white.png").toExternalForm());
            sendBtn.setFitWidth(109);
            sendBtn.setFitHeight(45);
            Platform.runLater(() -> {
                sendBtn.setImage(stopImage);
                BlinkAnimation.getInstance().startBlink(sendBtn);
            });

            // Start streaming response in a new thread
            streamingThread = new Thread(() -> streamResponseFromOllama(message));
            streamingThread.setDaemon(true); // Allow thread to terminate with application
            streamingThread.start();

        } else {
            // If streaming is active, stop it
            Platform.runLater(() -> chatProgress.setVisible(false)); // Hide progress indicator
            stopStreaming = true; // Set flag to stop streaming
            isStreaming = false; // Set streaming flag to false

            // Change send button icon back to send icon and stop blinking
            Image sendImage = new Image(getClass().getResource("res/images/icons/send-white.png").toExternalForm());
            sendBtn.setFitWidth(109);
            sendBtn.setFitHeight(45);
            Platform.runLater(() -> {
                sendBtn.setImage(sendImage);
                BlinkAnimation.getInstance().stopBlink();
                sendBtn.setVisible(true);
            });
            
        }
    }

    /**
     * Streams the response from the backend LLM API (Ollama).
     *
     * @param prompt The user's message to send to the LLM.
     */
    private void streEamResponseFromOllama(String prompt) {
        try {
            String baseUrl = "http://localhost:8000/llm";

            JSONObject data = new JSONObject();
            data.put("message", prompt);
            // Only include new_chat flag if it's truly a new conversation
            if (Session.isNewChat()) {
                data.put("new_chat", Session.isNewChat());
            }

            data.put("conversation_id", Session.getConversation_id());

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(baseUrl + "/chat"))
                    .header("Authorization", "Bearer " + Session.getAuthToken())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(data.toString()))
                    .build();

            isStreaming = true; // Confirm streaming is active
            final StringBuilder botMessageBuilder = new StringBuilder(); // Buffer for bot's full message

            client.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                    .thenAccept(response -> {
                        String conversation_id = response.headers().firstValue("X-Conversation-ID").orElse(null);
                        Session.setConversation_id(conversation_id); // Update session conversation ID

                        try (InputStream inputStream = response.body(); BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

                            String line;
                            while ((line = reader.readLine()) != null && !stopStreaming) {
                                if (!line.trim().isEmpty()) {
                                    JSONObject obj = new JSONObject(line);
                                    String content = obj.optString("response", ""); // Extract 'response' field
                                    if (!content.isEmpty()) {
                                        botMessageBuilder.append(content); // Append to full message buffer

                                        // Update UI with streamed content
                                        Platform.runLater(() -> {
                                            executeJavaScript("appendToLastMessage", content, "bot");
                                        });
                                    }
                                }
                            }

                            // Finalize and update state after streaming completes or is stopped
                            String finalMessage = botMessageBuilder.toString();
                            Platform.runLater(() -> {
                                chatProgress.setVisible(false); // Hide progress indicator
                                chatWebView.getWebEngine().executeScript("cleanTempBox()"); // Clean up temporary box

                                // Only render the final message if it's not blank
                                if (!finalMessage.isBlank()) {
                                    Image sendImage = new Image(getClass().getResource("res/images/icons/send-white.png").toExternalForm());
                                    sendBtn.setFitWidth(109);
                                    sendBtn.setFitHeight(45);
                                    sendBtn.setImage(sendImage); // Reset send button icon
                                    BlinkAnimation.getInstance().stopBlink(); // Stop blinking
                                    sendBtn.setVisible(true); // Ensure button is visible

                                    executeJavaScript("renderMarkdown", finalMessage, "bot"); // Render final message

                                    // Crucial state update: If it was a new user, transition to CONTINUE_CHAT
                                    if (getChatState() == ChatState.NEW_USER || getChatState() == ChatState.NEW_CHAT_FOR_USER_WITH_HISTORY) {
                                        setChatState(ChatState.CONTINUE_CHAT);
                                    }
                                    isStreaming = false; // Reset streaming flag
                                    stopStreaming = false; // Reset stop streaming flag for next message
                                } else {
                                    // Handle cases where final message is blank (e.g., error or no content)
                                    isStreaming = false;
                                    stopStreaming = false;
                                    // Reset send button icon and stop blink in case of blank message
                                    Image sendImage = new Image(getClass().getResource("res/images/icons/send-white.png").toExternalForm());
                                    sendBtn.setFitWidth(109);
                                    sendBtn.setFitHeight(45);
                                    sendBtn.setImage(sendImage);
                                    BlinkAnimation.getInstance().stopBlink();
                                    sendBtn.setVisible(true);
                                }
                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                            Platform.runLater(() -> {
                                chatWebView.getWebEngine().executeScript("cleanTempBox()");
                                // Ensure UI is reset even on error
                                chatProgress.setVisible(false);
                                isStreaming = false;
                                stopStreaming = false;
                                Image sendImage = new Image(getClass().getResource("res/images/icons/send-white.png").toExternalForm());
                                sendBtn.setFitWidth(109);
                                sendBtn.setFitHeight(45);
                                sendBtn.setImage(sendImage);
                                BlinkAnimation.getInstance().stopBlink();
                                sendBtn.setVisible(true);
                                UtilityMethods.showAlert(AlertType.ERROR, "Chat Error", "Failed to get response from LLM. Please try again.");
                            });
                        }

                    }).exceptionally(ex -> {
                // Handle exceptions from the HttpClient.sendAsync call
                ex.printStackTrace();
                Platform.runLater(() -> {
                    chatWebView.getWebEngine().executeScript("cleanTempBox()");
                    chatProgress.setVisible(false);
                    isStreaming = false;
                    stopStreaming = false;
                    Image sendImage = new Image(getClass().getResource("res/images/icons/send-white.png").toExternalForm());
                    sendBtn.setFitWidth(109);
                    sendBtn.setFitHeight(45);
                    sendBtn.setImage(sendImage);
                    BlinkAnimation.getInstance().stopBlink();
                    sendBtn.setVisible(true);
                    UtilityMethods.showAlert(AlertType.ERROR, "Network Error", "Could not connect to the LLM service. Please check your connection.");
                });
                return null;
            });

        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                chatWebView.getWebEngine().executeScript("cleanTempBox()");
                chatProgress.setVisible(false);
                isStreaming = false;
                stopStreaming = false;
                Image sendImage = new Image(getClass().getResource("res/images/icons/send-white.png").toExternalForm());
                sendBtn.setFitWidth(109);
                sendBtn.setFitHeight(45);
                sendBtn.setImage(sendImage);
                BlinkAnimation.getInstance().stopBlink();
                sendBtn.setVisible(true);
                UtilityMethods.showAlert(AlertType.ERROR, "Application Error", "An unexpected error occurred. Please restart the application.");
            });
        }
    }

    /**
     * Streams the response from the backend LLM API (Ollama).
     *
     * @param prompt The user's message to send to the LLM.
     */
    private void streamResponseFromOllama(String prompt) {
        try {
            String baseUrl = "http://localhost:8000/llm";

            JSONObject data = new JSONObject();
            data.put("message", prompt);
            // Only include new_chat flag if it's truly a new conversation
            if (Session.isNewChat()) {
                data.put("new_chat", Session.isNewChat());
            }

            data.put("conversation_id", Session.getConversation_id());

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(baseUrl + "/chat"))
                    .header("Authorization", "Bearer " + Session.getAuthToken())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(data.toString()))
                    .build();

            isStreaming = true; // Confirm streaming is active
            final StringBuilder botMessageBuilder = new StringBuilder(); // Buffer for bot's full message

            client.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                    .thenAccept(response -> {
                        String conversation_id = response.headers().firstValue("X-Conversation-ID").orElse(null);
                        Session.setConversation_id(conversation_id); // Update session conversation ID

                        try (InputStream inputStream = response.body(); BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

                            String line;
                            while ((line = reader.readLine()) != null && !stopStreaming) {
                                if (!line.trim().isEmpty()) {
                                    try {
                                        JSONObject obj = new JSONObject(line);
                                        // !!! IMPORTANT: Check this key. Is it "response" or something else?
                                        String content = obj.optString("response", "");
                                        if (!content.isEmpty()) {
                                            botMessageBuilder.append(content); // Append to full message buffer

                                            // Update UI with streamed content
                                            Platform.runLater(() -> {
                                                executeJavaScript("appendToLastMessage", content, "bot");
                                            });
                                        }
                                    } catch (org.json.JSONException jsonEx) {
                                        // This means the line is not valid JSON, which is a backend streaming issue.
                                        //mabr3 (Work on this later) [TODO]
                                    }
                                }
                            }

                            // Finalize and update state after streaming completes or is stopped
                            String finalMessage = botMessageBuilder.toString();
                            Platform.runLater(() -> {
                                chatProgress.setVisible(false); // Hide progress indicator
                                chatWebView.getWebEngine().executeScript("cleanTempBox()"); // Clean up temporary box

                                // Only render the final message if it's not blank
                                if (!finalMessage.isBlank()) {
                                    Image sendImage = new Image(getClass().getResource("res/images/icons/send-white.png").toExternalForm());
                                    sendBtn.setFitWidth(109);
                                    sendBtn.setFitHeight(45);
                                    sendBtn.setImage(sendImage); // Reset send button icon
                                    BlinkAnimation.getInstance().stopBlink(); // Stop blinking
                                    sendBtn.setVisible(true); // Ensure button is visible

                                    executeJavaScript("renderMarkdown", finalMessage, "bot"); // Render final message

                                    // Crucial state update: If it was a new user, transition to CONTINUE_CHAT
                                    if (getChatState() == ChatState.NEW_USER || getChatState() == ChatState.NEW_CHAT_FOR_USER_WITH_HISTORY) {
                                        setChatState(ChatState.CONTINUE_CHAT);
                                    }
                                    isStreaming = false; // Reset streaming flag
                                    stopStreaming = false; // Reset stop streaming flag for next message
                                } else {
                                    isStreaming = false;
                                    stopStreaming = false;
                                    // Reset send button icon and stop blink in case of blank message
                                    Image sendImage = new Image(getClass().getResource("res/images/icons/send-white.png").toExternalForm());
                                    sendBtn.setFitWidth(109);
                                    sendBtn.setFitHeight(45);
                                    sendBtn.setImage(sendImage);
                                    BlinkAnimation.getInstance().stopBlink();
                                    sendBtn.setVisible(true);
                                    UtilityMethods.showAlert(AlertType.WARNING, "No Response", "The LLM did not provide a response. Please try again.");
                                }
                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                            Platform.runLater(() -> {
                                chatWebView.getWebEngine().executeScript("cleanTempBox()");
                                // Ensure UI is reset even on error
                                chatProgress.setVisible(false);
                                isStreaming = false;
                                stopStreaming = false;
                                Image sendImage = new Image(getClass().getResource("res/images/icons/send-white.png").toExternalForm());
                                sendBtn.setFitWidth(109);
                                sendBtn.setFitHeight(45);
                                sendBtn.setImage(sendImage);
                                BlinkAnimation.getInstance().stopBlink();
                                sendBtn.setVisible(true);
                                UtilityMethods.showAlert(AlertType.ERROR, "Chat Error", "Failed to get response from LLM. Please try again.");
                            });
                        }

                    }).exceptionally(ex -> {
                // Handle exceptions from the HttpClient.sendAsync call
                ex.printStackTrace();
                Platform.runLater(() -> {
                    chatWebView.getWebEngine().executeScript("cleanTempBox()");
                    chatProgress.setVisible(false);
                    isStreaming = false;
                    stopStreaming = false;
                    Image sendImage = new Image(getClass().getResource("res/images/icons/send-white.png").toExternalForm());
                    sendBtn.setFitWidth(109);
                    sendBtn.setFitHeight(45);
                    sendBtn.setImage(sendImage);
                    BlinkAnimation.getInstance().stopBlink();
                    sendBtn.setVisible(true);
                    UtilityMethods.showAlert(AlertType.ERROR, "Network Error", "Could not connect to the LLM service. Please check your connection.");
                });
                return null;
            });

        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                chatWebView.getWebEngine().executeScript("cleanTempBox()");
                chatProgress.setVisible(false);
                isStreaming = false;
                stopStreaming = false;
                Image sendImage = new Image(getClass().getResource("res/images/icons/send-white.png").toExternalForm());
                sendBtn.setFitWidth(109);
                sendBtn.setFitHeight(45);
                sendBtn.setImage(sendImage);
                BlinkAnimation.getInstance().stopBlink();
                sendBtn.setVisible(true);
                UtilityMethods.showAlert(AlertType.ERROR, "Application Error", "An unexpected error occurred. Please restart the application.");
            });
        }
    }

    /**
     * Creates a TitledPane for a chat history entry.
     *
     * @param chatId The ID of the chat.
     * @param title The title of the chat (e.g., first few words of the user's
     * message).
     * @param description A brief description of the chat (e.g., bot's initial
     * response).
     * @return A TitledPane representing a chat history entry.
     */
    public TitledPane makeChatPane(String chatId, String title, String description) {
        TitledPane pane = new TitledPane();
        pane.setStyle("-fx-font-size: 14px; -fx-font-family: Century; -fx-text-fill: white;");
        pane.setAnimated(true);
        pane.setId(chatId); // Set the ID for later retrieval

        VBox content = new VBox();
        VBox.setVgrow(pane, Priority.ALWAYS);

        Label desc = new Label(description);
        desc.setStyle("-fx-font-size: 16px; -fx-font-family: Century; -fx-text-fill: white;");
        desc.setTextOverrun(OverrunStyle.CENTER_ELLIPSIS);
        desc.setWrapText(true);

        HBox btnWrap = new HBox();
        btnWrap.setSpacing(7);

        // Load Button
        Button loadBtn = new Button("   Load");
        loadBtn.setTooltip(new Tooltip("Load chat"));
        loadBtn.setId("loadbtn");
        loadBtn.setPrefWidth(70.0);
        loadBtn.setPrefHeight(10.0);
        loadBtn.setEffect(new DropShadow());

        loadBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent eh) {
                String selectedChatId = chatHistoryAccordion.getExpandedPane().getId();
                // Clear current chat display
                chatsBox.getChildren().clear();
                chatsBox.setAlignment(Pos.TOP_LEFT);
                chatsBox.setFillWidth(true);
                chatWebView.getWebEngine().executeScript("document.getElementById('chatbox').innerHTML = '';");
                chatsBox.getChildren().add(chatWebView);
                // Set session conversation ID and load chat messages
                Session.setConversation_id(selectedChatId);
                loadChatBox(selectedChatId);
                setChatState(ChatState.CONTINUE_CHAT); // Set state to continue existing conversation
            }
        });

        // Delete Button
        Button deleteBtn = new Button("    Delete");
        deleteBtn.setId("deletebtn");
        deleteBtn.setTooltip(new Tooltip("Delete chat"));
        deleteBtn.setPrefWidth(70.0);
        deleteBtn.setPrefHeight(10.0);
        deleteBtn.setEffect(new DropShadow());

        deleteBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent eh) {
                String selectedChatId = chatHistoryAccordion.getExpandedPane().getId();
                // Check if the chat to be deleted is the current active chat
                if (selectedChatId.equals(Session.getConversation_id())) {
                    Alert prompt = new Alert(Alert.AlertType.CONFIRMATION);
                    prompt.setHeaderText("Permission to delete current chat.");
                    prompt.setContentText("Are you sure you want to delete this chat?");
                    Optional<ButtonType> result = prompt.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        chatsBox.getChildren().clear();
                        chatHistoryAccordion.getPanes().remove(chatHistoryAccordion.getExpandedPane());
                        Session.setConversation_id(null); // Clear session ID
                        deleteChat(selectedChatId); // Delete from database
                        openNewChat(eh); // Open a new chat after deletion
                        setChatState(ChatState.NEW_CHAT_FOR_USER_WITH_HISTORY);
                    }
                } else {
                    // If deleting a non-active chat
                    deleteChat(selectedChatId); // Delete from database
                    chatHistoryAccordion.getPanes().remove(chatHistoryAccordion.getExpandedPane());
                }
            }
        });

        btnWrap.getChildren().addAll(loadBtn, deleteBtn); // Add buttons to wrapper

        content.getChildren().addAll(desc, btnWrap); // Add description and buttons to content VBox

        pane.setText(title);
        pane.setTextOverrun(OverrunStyle.CENTER_ELLIPSIS);
        pane.setExpanded(true); // Initially expanded
        pane.setCollapsible(true);
        pane.setAnimated(true);

        pane.setContent(content); // Set content of the TitledPane

        return pane;
    }

    /**
     * Appends a new chat entry to the history accordion.
     *
     * @param message The user's initial message.
     * @param chatBotMessage The bot's initial response.
     */
    public void appendHistory(String message, String chatBotMessage) {
        String[] message_split = message.split(" ");
        String title = message_split[0]; // Use the first word as title
        TitledPane chatHist = makeChatPane(Session.getConversation_id(), title, chatBotMessage);
        chatHist.setStyle("-fx-background-radius: 50px;");
        chatHistoryAccordion.getPanes().add(chatHist);
    }

    /**
     * Loads previous chat conversations from the backend and populates the
     * history pane.
     */
    public void loadChatsPane() {
        try {
            JSONObject response = ApiFunctions.loadConversations(); // Call API to load conversations
            int total_convos = response.getInt("total");
            if (total_convos > 0) {
                JSONArray conversations = response.getJSONArray("messages");
                for (Object conversation : conversations) {
                    JSONObject chatDesc = (JSONObject) conversation;
                    String chat_id = chatDesc.getString("conversation_id");
                    String header = chatDesc.getString("conversation_header");
                    String description = chatDesc.getString("conversation_desc");
                    chatHistoryAccordion.getPanes().add(makeChatPane(chat_id, header, description));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Load History Error", "Failed to load chat history. Please try again.");
        }
    }

    /**
     * Displays an alert dialog.
     *
     * @param type The type of alert (e.g., INFORMATION, ERROR).
     * @param title The title of the alert dialog.
     * @param msg The message content of the alert.
     */
    private static void showAlert(AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setHeaderText(title);
        alert.setContentText(msg);
        alert.show();
    }

    /**
     * Loads and displays the messages for a specific chat conversation in the
     * chat box.
     *
     * @param chatId The ID of the conversation to load.
     */
    public void loadChatBox(String chatId) {
        chatWebView.getWebEngine().executeScript("document.getElementById('chatbox').innerHTML = '';"); // Clear current chat display
        try {
            JSONObject responseJson = ApiFunctions.loadChatHistory(chatId); // Fetch chat history from API
            JSONObject conversations = responseJson.getJSONObject("conversations");

            JSONArray userMessages = conversations.getJSONArray("userMessages");
            JSONArray botMessages = conversations.getJSONArray("botMessages");

            int maxLength = Math.max(userMessages.length(), botMessages.length());
            for (int i = 0; i < maxLength; i++) {
                if (i < userMessages.length()) {
                    addMessage(userMessages.getString(i), true); // Add user message
                }
                if (i < botMessages.length()) {
                    addMessage(botMessages.getString(i), false); // Add bot message
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            showAlert(AlertType.ERROR, "Load Chat Error", "Failed to load chat messages. Please try again.");
        }
    }

    /**
     * Deletes a chat conversation from the database.
     *
     * @param chatId The ID of the chat to delete.
     */
    private void deleteChat(String chatId) {
        try {
            JSONObject response = ApiFunctions.deleteChatHistory(chatId); // Call API to delete chat
            if (response != null) {
                String msg = response.getString("status");
                if (msg.equals("deleted")) {
                    showAlert(Alert.AlertType.INFORMATION, "Chat", "Chat deleted successfully!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Delete Chat Error", "Failed to delete chat. Please try again.");
        }
    }

    /* **********************\
    | THEME AND UI METHODS   |
    \************************/
    /**
     * Applies hover and press effects to a button.
     *
     * @param btn The Button to apply effects to.
     * @param color1 The color on mouse hover.
     * @param color2 The default color.
     */
    public void editBtn(Button btn, String color1, String color2) {
        btn.setOnMouseEntered(eh -> {
            String style = "-fx-background-color: " + color1 + "; -fx-padding: 10 20; -fx-border-radius: 5; -fx-background-radius: 5;";
            btn.setStyle(style);
        });

        btn.setOnMouseExited(eh -> {
            String style = "-fx-background-color: " + color2 + "; -fx-padding: 10 20; -fx-border-radius: 5; -fx-background-radius: 5;";
            btn.setStyle(style);
        });

        btn.setOnMousePressed(eh -> {
            ScaleTransition sclTrans = new ScaleTransition(Duration.millis(100), btn);
            sclTrans.setToX(0.9);
            sclTrans.setToY(0.9);
            sclTrans.play();
        });

        btn.setOnMouseReleased(eh -> {
            ScaleTransition sclTrans = new ScaleTransition(Duration.millis(100), btn);
            sclTrans.setToX(1.0);
            sclTrans.setToY(1.0);
            sclTrans.play();
        });
    }

    /**
     * Applies a scale transition effect to an ImageView on mouse press.
     *
     * @param img The ImageView to apply the effect to.
     */
    public void editImageButton(ImageView img) {
        ScaleTransition scaleTrans = new ScaleTransition(Duration.millis(200), img);
        scaleTrans.setToX(1.2);
        scaleTrans.setToY(1.2);
        scaleTrans.setCycleCount(2);
        scaleTrans.setAutoReverse(true);

        img.setOnMousePressed(eh -> {
            scaleTrans.play();
        });
    }

    /**
     * Handles the action for starting a new chat. Clears the current chat,
     * resets session variables, and displays the welcome pane.
     *
     * @param event The ActionEvent that triggered this action.
     */
    @FXML
    private void openNewChat(ActionEvent event) {
        if(isStreaming){
            stopStreaming = true;
        }
        newChat = true;
        Session.setNewChat(newChat); // Set session flag for new chat
        Session.setConversation_id(null); // Clear current conversation ID
        BlinkAnimation.getInstance().stopBlink(); // Stop blinking animation

        // Clear chat box and display welcome pane
        chatsBox.getChildren().clear();
        chatsBox.getChildren().add(welcomePane);
        chatsBox.setAlignment(Pos.CENTER);
        chatsBox.setFillWidth(false);
        // Clear the WebView's chatbox content
        chatWebView.getWebEngine().executeScript("document.getElementById('chatbox').innerHTML = '';");

        // Set chat state based on whether user has history
        if (Session.getTotal_conversations() > 0) { // Check if user has any history
            setChatState(ChatState.NEW_CHAT_FOR_USER_WITH_HISTORY);
        } else {
            setChatState(ChatState.NEW_USER);
        }
    }

    /**
     * Handles the action for sending a message. Triggers the processUserInput
     * method.
     *
     * @param event The MouseEvent that triggered this action.
     */
    @FXML
    private void sendMessage(MouseEvent event) {
        processUserInput(); // Send user input to backend server
    }

    /**
     * Toggles the visibility of the widgets pane.
     *
     * @param event The MouseEvent that triggered this action.
     */
    @FXML
    private void showWidgetsPane(MouseEvent event) {
        if (MainPane.getRight() == null) {
            MainPane.setRight(widget);
        } else {
            MainPane.setRight(null);
        }
    }

    /**
     * Placeholder for launching a lab simulation.
     *
     * @param event The ActionEvent that triggered this action.
     */
    @FXML
    private void launchLabSimulation(ActionEvent event) {
        // Implementation for launching lab simulation
    }

    /**
     * Sets the user message input to a predefined prompt for explaining
     * concepts and triggers message processing.
     *
     * @param event The ActionEvent that triggered this action.
     */
    @FXML
    void explainConceptPrompt(ActionEvent event) {
        String prompt = "I need an explanation of a concept. "
                + "Ask me which concept I want to learn about, then provide a clear and concise explanation with examples.";
        userMessage.setText(prompt);
        processUserInput();
    }

    /**
     * Sets the user message input to a predefined prompt for generating quiz
     * questions and triggers message processing.
     *
     * @param event The ActionEvent that triggered this action.
     */
    @FXML
    void quizPrompt(ActionEvent event) {
        String prompt = "Generate a set of quiz questions for a topic. "
                + "Ask me for the topic first, and then provide a mix of multiple choice and short-answer questions";
        userMessage.setText(prompt);
        processUserInput();
    }

    /**
     * Sets the user message input to a randomly selected predefined prompt for
     * lesson planning and triggers message processing.
     *
     * @param event The ActionEvent that triggered this action.
     */
    @FXML
    void lessonPlannerPrompt(ActionEvent event) {
        String promptOne = "I need a lesson plan for teaching the topic I'll specify in my next message.";
        String promptTwo = "Help me design a lesson plan with key concepts and activities for the topic I'll specify in my next message.";
        String promptThree = "Create a structured lesson plan for the topic I'll specify in my next message.";
        ArrayList<String> prompts = new ArrayList<>();
        prompts.add(promptOne);
        prompts.add(promptTwo);
        prompts.add(promptThree);
        Random rand = new Random();
        int index = rand.nextInt(prompts.size()); // Use prompts.size() for upper bound
        String prompt = prompts.get(index);
        userMessage.setText(prompt);
        processUserInput();
    }

    /**
     * Placeholder for plotting a graph.
     *
     * @param event The ActionEvent that triggered this action.
     */
    @FXML
    void plotGraph(ActionEvent event) {
        // Implementation for opening graph pane
    }

    /**
     * Closes the application window.
     *
     * @param event The MouseEvent that triggered this action.
     */
    @FXML
    void closeWindow(MouseEvent event) {
        stage.close();
        System.exit(0); // Ensure the application exits cleanly
    }

    /**
     * Minimizes the application window.
     *
     * @param event The MouseEvent that triggered this action.
     */
    @FXML
    void minimizeWindow(MouseEvent event) {
        stage.setIconified(true);
    }

    /**
     * Resizes or restores the application window, and updates the resize icon.
     *
     * @param event The MouseEvent that triggered this action.
     */
    @FXML
    void resizeWindow(MouseEvent event) {
        double screenWidth = Screen.getPrimary().getVisualBounds().getWidth();
        double screenHeight = Screen.getPrimary().getVisualBounds().getHeight();
        if (!isMaximized) {
            // Save current state before maximizing
            prevX = stage.getX();
            prevY = stage.getY();
            prevWidth = stage.getWidth();
            prevHeight = stage.getHeight();

            // Maximize window
            stage.setX(0);
            stage.setY(0);
            stage.setWidth(screenWidth);
            stage.setHeight(screenHeight);
            isMaximized = true;
            // Change icon to restore icon
            ImageView img = new ImageView(new Image(getClass().getResource("res/images/icons/resize_white.png").toExternalForm()));
            img.setFitHeight(25);
            img.setFitWidth(25);
            resize_box.getChildren().set(0, img);

        } else {
            // Restore window
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

    /**
     * Constructor for ChatPaneController, allowing a Stage to be passed.
     *
     * @param stage The JavaFX Stage associated with this controller.
     */
    public ChatPaneController(Stage stage) {
        this.stage = stage;
    }

    /**
     * Default constructor for ChatPaneController.
     */
    public ChatPaneController() {
    }
}
