
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Session {
    private static String authUser;
    private static String authToken;
    private static String conversation_id;
    private static int total_conversations;
    private static boolean newChat;

    private static Stage stage;

    public static Stage getStage() {
        return stage;
    }

    public static void setStage(Stage app_stage) {
        Session.stage = app_stage;
        Session.stage.initStyle(StageStyle.TRANSPARENT);
        System.out.println("Session Stage : "+ stage.getStyle());
    }
    
    public static boolean isNewChat() {
        return conversation_id == null;
    }

    public static void setNewChat(boolean newChat) {
        Session.newChat = newChat;
    }

    public static int getTotal_conversations() {
        return total_conversations;
    }

    public static void setTotal_conversations(int total_conversations) {
        Session.total_conversations = total_conversations;
    }

    public static String getAuthToken() {
        return authToken;
    }

    public static void setAuthToken(String authToken) {
        Session.authToken = authToken;
    }

    public static String getConversation_id() {
        return conversation_id;
    }

    public static void setConversation_id(String conversation_id) {
        Session.conversation_id = conversation_id;
    }

    public static String getAuthUser() {
        return authUser;
    }

    public static void setAuthUser(String authUser) {
        Session.authUser = authUser;
    }

    public Session() {
        if(conversation_id == null){
            newChat = true;
        }
    }
    
    
}
