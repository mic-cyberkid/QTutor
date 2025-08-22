

import java.util.ArrayList;

/**
 *
 * @author Eben and Mickey
 * 
 */
public class Chat {
    // My Assumption : Size of user messages == size of chatbot messages.
    //Why ? -> Its a give and Take something going on between user and chatbot.
    String chatID;
    String title;
    String desc;

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
    ArrayList<String> UserMessages;
    ArrayList<String> ChatBotMessages;

    public Chat(String chatID, String title, String description) {
        this.chatID = chatID;
        this.title = title;
        this.desc = description;
    }
    
    

    public String getChatID() {
        return chatID;
    }

    public void setChatID(String chatID) {
        this.chatID = chatID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<String> getUserMessages() {
        return UserMessages;
    }

    public void setUserMessages(ArrayList<String> UserMessages) {
        this.UserMessages = UserMessages;
    }

    public ArrayList<String> getChatBotMessages() {
        return ChatBotMessages;
    }

    public void setChatBotMessages(ArrayList<String> ChatBotMessages) {
        this.ChatBotMessages = ChatBotMessages;
    }
    
    
    //
    
    public Chat(){
        
    }
}
