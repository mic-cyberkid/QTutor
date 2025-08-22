import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        // Specify the path to your JSON file
        String jsonFile = "src/lightweight/NewDataset.json";

        try (BufferedReader br = new BufferedReader(new FileReader(jsonFile))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            JSONObject dataset = new JSONObject(sb.toString());
            System.out.println(dataset);
        } catch (Exception e) {
            System.out.println("Error parsing JSON: " + e.getMessage());
        }
    }
    
    public JSONObject loadDataset(){
         String jsonFile = "src/lightweight/NewDataset.json";
        try{
            JSONObject dataset = new JSONObject(new StringBuilder(jsonFile));
            return dataset;
            
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
}