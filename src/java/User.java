/**
 *
 * @author Eben and Mickey
 */
public class User {
    int userId;
    String username;
    int totalChats;
    String dateCreated;

    public int getTotalChats() {
        return totalChats;
    }

    public void setTotalChats(int totalChats) {
        this.totalChats = totalChats;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }
    

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public User(String username) {
        this.username = username;
    }
    
    
    public User(){}
    
    
}
