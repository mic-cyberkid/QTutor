
import javafx.concurrent.Task;
import org.json.JSONObject;

/**
 *
 * @author pc
 */
public class ApiTask extends Task<JSONObject>{

    String prompt;

    public ApiTask(String prompt) {
        this.prompt = prompt;
    }
    
    @Override
    protected JSONObject call() throws Exception {
        JSONObject response = ApiFunctions.explainConversion(prompt);
        System.out.println("Response: "+ response);
        return response;
    }
    
}
