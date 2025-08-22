import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.Consumer;
import javafx.concurrent.Task;
import org.json.JSONObject;

/**
 * Handles all API communication for the chat application.
 * All methods return a JavaFX Task to be run on a background thread.
 */
public class ChatService {

    private static final String BASE_URL = "http://127.0.0.1:8000/llm";
    private final HttpClient client = HttpClient.newHttpClient();

    /**
     * A task to stream a response from the chatbot.
     *
     * @param prompt The user's message.
     * @param onChunkReceived A callback to handle each piece of the response as it arrives.
     * @param onComplete A callback to run when the stream is finished.
     * @param onError A callback to handle any errors.
     * @return A Task that can be run on a background thread.
     */
    public Task<Void> streamChatResponse(String prompt, Consumer<String> onChunkReceived, Consumer<String> onConversationIdReceived, Runnable onComplete, Consumer<Exception> onError) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                JSONObject data = new JSONObject();
                data.put("message", prompt);
                if (Session.isNewChat()) {
                    data.put("new_chat", true);
                }
                data.put("conversation_id", Session.getConversation_id());

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI(BASE_URL + "/chat"))
                        .header("Authorization", "Bearer " + Session.getAuthToken())
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(data.toString()))
                        .build();

                try {
                    HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

                    String conversationId = response.headers().firstValue("X-Conversation-ID").orElse(null);
                    if (conversationId != null) {
                        onConversationIdReceived.accept(conversationId);
                    }

                    try (InputStream inputStream = response.body(); BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (isCancelled()) {
                                break;
                            }
                            if (!line.trim().isEmpty()) {
                                JSONObject obj = new JSONObject(line);
                                String content = obj.optString("response", "");
                                if (!content.isEmpty()) {
                                    onChunkReceived.accept(content);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    onError.accept(e);
                    throw e; // Re-throw to fail the task
                }
                return null;
            }

            @Override
            protected void succeeded() {
                onComplete.run();
            }

            @Override
            protected void failed() {
                onError.accept((Exception) getException());
            }
        };
    }

    /**
     * A task to fetch all conversation summaries for the user.
     *
     * @return A Task that returns a JSONObject with the conversation data.
     */
    public Task<JSONObject> loadConversations() {
        return new Task<>() {
            @Override
            protected JSONObject call() throws Exception {
                return ApiFunctions.loadConversations();
            }
        };
    }

    /**
     * A task to fetch the full message history for a specific conversation.
     *
     * @param conversationId The ID of the conversation to load.
     * @return A Task that returns a JSONObject with the chat history.
     */
    public Task<JSONObject> loadChatHistory(String conversationId) {
        return new Task<>() {
            @Override
            protected JSONObject call() throws Exception {
                return ApiFunctions.loadChatHistory(conversationId);
            }
        };
    }

    /**
     * A task to delete a conversation.
     *
     * @param conversationId The ID of the conversation to delete.
     * @return A Task that returns the server's response.
     */
    public Task<JSONObject> deleteChatHistory(String conversationId) {
        return new Task<>() {
            @Override
            protected JSONObject call() throws Exception {
                return ApiFunctions.deleteChatHistory(conversationId);
            }
        };
    }
    
    /**
     * A task to get an explanation for a unit conversion.
     *
     * @param fromUnit The unit to convert from.
     * @param toUnit The unit to convert to.
     * @return A Task that returns the explanation string.
     */
    public Task<String> explainConversion(String fromUnit, String toUnit) {
        return new Task<>() {
            @Override
            protected String call() throws Exception {
                String prompt = "Briefly describe how to convert " + fromUnit + " to " + toUnit + " provide plain text no markdowns";
                JSONObject response = ApiFunctions.explainConversion(prompt);
                if (response != null) {
                    return response.getString("response");
                } else {
                    throw new Exception("Failed to get explanation from API.");
                }
            }
        };
    }
}
