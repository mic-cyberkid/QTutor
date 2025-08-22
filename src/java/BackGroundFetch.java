import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.json.JSONArray;
import org.json.JSONObject;


/**
 *
 * @author Prince
 */
public class BackGroundFetch implements Callable<String>{

    String message;
    boolean newChat;
    String conversation_id;
    String response;
    
    
    
    public BackGroundFetch(String message, boolean newChat, String conversation_id){
        this.message = message;
        this.newChat = newChat;
        this.conversation_id = conversation_id;
        
    }
    
    
     //Display Warnings and Messages
    private void showAlert(Alert.AlertType type, String title, String msg){
        Alert alert = new Alert(type);
        alert.setHeaderText(title);
        alert.setContentText(msg);
        alert.show();
    }
    
    public String getConversation_id() {
        return conversation_id;
    }
    
    
    public String getResponse(){
        return this.response;
    }

    @Override
    public String call(){
        try {
            JSONObject resp = ApiFunctions.sendMessage(message, Session.isNewChat(), Session.getConversation_id());
            if(resp != null){
                response = resp.getString("response");
                conversation_id = resp.getString("conversation_id");
                Session.setConversation_id(conversation_id);
                return response;
            }else{
                return "Unable to Process Message";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "IO Error";
        }
        
        
    }
}
