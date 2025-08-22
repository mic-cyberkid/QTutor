import org.json.JSONObject;
import org.json.JSONArray;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.web.WebEngine;

public class ApiFunctions {

    private static final String BASE_URL = "http://127.0.0.1:8000/llm"; // Set your base URL
    private static String authToken = ""; // Store the authentication token
    private static boolean newChat = true;
    private static String conversation_id = "";

    // Load chat history by ID
    public static JSONObject serverStatus() throws IOException {
        String url = "http://127.0.0.1:8000";

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONObject responseJson = new JSONObject(response.toString());
            return responseJson;
        } else {
                System.out.println("Error connecting to server ...");
           
        }
        return null;
    }

    
    // Function to register user
    public static JSONObject registerUser(String username, String password) throws IOException {
        String url = BASE_URL + "/register";
        String params = "username=" + username + "&password=" + password;
        
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setDoOutput(true);
        
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = params.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        // Handle variious cases (User already exist, Standard error )
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONObject responseJson = new JSONObject(response.toString());
            return responseJson;
        } else {
            return null;
            
        }
    }
    

    // Function to login user
    public static JSONObject loginUser(String username, String password) throws IOException {
        String url = BASE_URL + "/token";
        String params = "username=" + username + "&password=" + password;

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = params.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONObject responseJson = new JSONObject(response.toString());
            authToken = responseJson.getString("access_token"); // Store the auth token
            String usernameResponse = responseJson.getString("username");

            // Redirect to the ChatPane (implement as needed)
            return responseJson;
            
        } else {
                return null;
                //showAlert(Alert.AlertType.ERROR, "Chat History", "Error Logging In.");
             
        }
        
    } 
    

    // Function to change passw0rd 
    public static JSONObject changePassword(String old_password,String new_password) throws IOException {
        String url = BASE_URL + "/update-password";
        JSONObject body = new JSONObject();
        body.put("old_password", old_password);
        body.put("new_password", old_password);


        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + authToken);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = body.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONObject responseJson = new JSONObject(response.toString());
            
            return responseJson;
        } else if (responseCode == 401 || responseCode == 403) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            JSONObject responseJson = new JSONObject(content.toString());
            return responseJson;
        } else if (responseCode == 400 || responseCode == 404) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            JSONObject responseJson = new JSONObject(content.toString());
            return responseJson;
        } else if (responseCode == 500 || responseCode == 502 || responseCode == 503 || responseCode == 504) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            JSONObject responseJson = new JSONObject(content.toString());
            return responseJson;
        } else {
            
            System.out.println("Unexpected response: " + connection.getResponseMessage());
        }
        return null;
    }
    
    
    
    // Function to send message
    public static JSONObject sendMessage(String message, boolean newChat, String conversation_id) throws IOException {
        String url = BASE_URL + "/chat";
        JSONObject body = new JSONObject();
        body.put("message", message);

        if (newChat) {
            body.put("new_chat", true);
        } else {
            body.put("conversation_id", conversation_id); // Replace with actual conversation ID
        }

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + authToken);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = body.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONObject responseJson = new JSONObject(response.toString());
            String assistantResponse = responseJson.getString("response");
            return responseJson;
        } else {
            System.out.println("Error sending message.");
            return null;
            //showAlert(Alert.AlertType.ERROR, "Chat", "Error sending message.");
    
        }
    }
    
    
    //Stream message
    public void sendMessageWithStreaming(String message, boolean newChat, String conversationId, WebEngine engine) throws IOException, InterruptedException {
    String url = BASE_URL + "/chat";

    // Build the request body
    JSONObject body = new JSONObject();
    body.put("message", message);
    body.put("new_chat", newChat);
    body.put("conversation_id", conversationId);

    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Bearer " + authToken)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
            .build();

    // Listen for chunks of text
    client.sendAsync(request, HttpResponse.BodyHandlers.ofLines())
            .thenAccept(response -> {
                response.body().forEach(line -> {
                    // Incrementally render each chunk of the response
                    Platform.runLater(() -> {
                        engine.executeScript("appendToLastMessage(" + JSONObject.quote(line) + ")");
                    });
                });
            });
}

    //reset password
    public static JSONObject updatePasswordManually(String username, String newPassword) {
        try {
            URL url = new URL("http://localhost:8000/manual-update-password"); // Change if needed
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Create JSON payload
            JSONObject json = new JSONObject();
            json.put("username", username);
            json.put("new_password", newPassword);

            // Send request
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = json.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Read response
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }

            return new JSONObject(response.toString());

        } catch (IOException e) {
            // Handle error (like 401 or 404) with error stream
            try (InputStream errorStream = ((HttpURLConnection) new URL("http://localhost:8000/manual-update-password").openConnection()).getErrorStream()) {
                if (errorStream != null) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream, "utf-8"));
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorResponse.append(line.trim());
                    }
                    return new JSONObject().put("error", errorResponse.toString());
                }
            } catch (Exception ex) {
                return new JSONObject().put("error", "Connection or unknown error");
            }

            return new JSONObject().put("error", e.getMessage());
        }
    }
    
    // Method to get conversion explanatiion
     public static JSONObject explainConversion(String message) throws IOException {
        String url = BASE_URL + "/conversion";
        JSONObject body = new JSONObject();
        body.put("message", message);

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = body.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONObject responseJson = new JSONObject(response.toString());
            return responseJson;
        } else {
            System.out.println("Error sending message.");
           
            UtilityMethods.showAlert(Alert.AlertType.ERROR, "Chat", "Error sending message.");
            return null;
    
        }
    }


    // Load conversations
    public static JSONObject loadConversations() throws IOException {
        String url = BASE_URL + "/conversations";
        
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Bearer " + authToken);

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONObject responseJson = new JSONObject(response.toString());
            JSONArray messages = responseJson.getJSONArray("messages");
            
            for (int i = 0; i < messages.length(); i++) {
                JSONObject conversation = messages.getJSONObject(i);
            }
            return responseJson;
        } else {
            System.out.println("Error getting converstions");
            return null;
        }
    }

    // Load chat history by ID
    public static JSONObject loadChatHistory(String conversationId) throws IOException {
        String url = BASE_URL + "/conversations/" + conversationId;

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Bearer " + authToken);

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONObject responseJson = new JSONObject(response.toString());
            return responseJson;
        } else {
                System.out.println("Error loading chat history");
           
        }
        return null;
    }

    // Load chat history by ID
    public static JSONObject deleteChatHistory(String conversationId) throws IOException {
        String url = BASE_URL + "/conversations/" + conversationId;

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("DELETE");
        connection.setRequestProperty("Authorization", "Bearer " + authToken);

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONObject responseJson = new JSONObject(response.toString());
            return responseJson;
        } else {
                System.out.println("Error loading chat history");
           
        }
        return null;
    }

}
