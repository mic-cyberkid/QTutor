import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.Consumer;

import org.json.JSONObject;

public class StreamingFetch implements Runnable {
    private final String message;
    private final boolean isNewChat;
    private final String conversationId;
    private final Consumer<String> onChunk;
    private final Runnable onComplete;

    public StreamingFetch(String message, boolean isNewChat, String conversationId,
                          Consumer<String> onChunk, Runnable onComplete) {
        this.message = message;
        this.isNewChat = isNewChat;
        this.conversationId = conversationId;
        this.onChunk = onChunk;
        this.onComplete = onComplete;
    }

    @Override
    public void run() {
        try {
            HttpClient client = HttpClient.newHttpClient();

            JSONObject body = new JSONObject();
            body.put("message", message);
            body.put("new_chat", isNewChat);
            body.put("conversation_id", conversationId);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8000/llm/chat")) // Adjust to your actual endpoint
                    .header("Authorization", "Bearer " + Session.getAuthToken())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofLines())
                    .thenAccept(response -> {
                        response.body().forEach(chunk -> {
                            if (chunk.trim().equals("[DONE]")) {
                                onComplete.run();
                            } else {
                                
                                onChunk.accept(chunk);
                            }
                        });
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
