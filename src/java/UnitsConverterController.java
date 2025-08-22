import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;
import javax.script.ScriptEngineManager;
import javax.script.ScriptEngine;


public class UnitsConverterController {
    @FXML private ComboBox<String> categoryBox;
    @FXML private ComboBox<String> fromUnitBox;
    @FXML private ComboBox<String> toUnitBox;
    @FXML private TextField inputField;
    @FXML private Label resultLabel;

    private final Map<String, String[]> unitCategories = new HashMap<>();
    private final Map<String, Double> conversionFactors = new HashMap<>();

    @FXML
    public void initialize() {
        
        
        // Define unit categories
        unitCategories.put("Length", new String[]{"Meters", "Kilometers", "Feet", "Miles"});
        unitCategories.put("Mass", new String[]{"Grams", "Kilograms", "Pounds"});
        unitCategories.put("Temperature", new String[]{"Celsius", "Fahrenheit", "Kelvin"});

        // Define conversion factors (relative to meters, grams, etc.)
        conversionFactors.put("Meters-Kilometers", 0.001);
        conversionFactors.put("Meters-Feet", 3.28084);
        conversionFactors.put("Meters-Miles", 0.000621371);
        conversionFactors.put("Grams-Kilograms", 0.001);
        conversionFactors.put("Grams-Pounds", 0.00220462);
        conversionFactors.put("Kilograms-Grams", 1000.0);
        conversionFactors.put("Kilograms-Pounds", 2.20462);
        conversionFactors.put("Pounds-Kilograms", 0.453592);

        // Populate category selection
        categoryBox.getItems().addAll(unitCategories.keySet());
    }

    @FXML
    private void updateUnits() {
        fromUnitBox.getItems().clear();
        toUnitBox.getItems().clear();

        String selectedCategory = categoryBox.getValue();
        if (selectedCategory != null) {
            fromUnitBox.getItems().addAll(unitCategories.get(selectedCategory));
            toUnitBox.getItems().addAll(unitCategories.get(selectedCategory));
        }
    }

    @FXML
    private void convert() {
        String fromUnit = fromUnitBox.getValue();
        String toUnit = toUnitBox.getValue();
        String inputText = inputField.getText();

        if (fromUnit == null || toUnit == null || inputText.isEmpty()) {
            resultLabel.setText("Select units and enter a value!");
            return;
        }

        try {
            double inputValue = Double.parseDouble(inputText);
            double result = performConversion(fromUnit, toUnit, inputValue);
            resultLabel.setText(String.format("%.2f %s", result, toUnit));
        } catch (NumberFormatException e) {
            resultLabel.setText("Invalid input!");
        }
    }

    private double performConversion(String from, String to, double value) {
        if (from.equals(to)) return value; // Same unit, no conversion

        String key = from + "-" + to;
        if (conversionFactors.containsKey(key)) {
            return value * conversionFactors.get(key);
        }

        // Temperature conversions
        if (from.equals("Celsius") && to.equals("Fahrenheit")) return (value * 9/5) + 32;
        if (from.equals("Fahrenheit") && to.equals("Celsius")) return (value - 32) * 5/9;
        if (from.equals("Celsius") && to.equals("Kelvin")) return value + 273.15;
        if (from.equals("Kelvin") && to.equals("Celsius")) return value - 273.15;
        if (from.equals("Fahrenheit") && to.equals("Kelvin")) return (value - 32) * 5/9 + 273.15;
        if (from.equals("Kelvin") && to.equals("Fahrenheit")) return (value - 273.15) * 9/5 + 32;

        return 0.0; // No conversion rule found
    }

    @FXML
    private void swapUnits() {
        String temp = fromUnitBox.getValue();
        fromUnitBox.setValue(toUnitBox.getValue());
        toUnitBox.setValue(temp);
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
    
    
     public static double convert(double iv, String fromUnit, String toUnit, JSONObject dataset) {
        try {
            JSONObject selectedConversion = dataset.getJSONObject(fromUnit).getJSONObject(toUnit);
            String baseConv = selectedConversion.getString("toBase");
            String toConv = selectedConversion.getString("fromBase");
            
            // Iv to base unit
            double baseValue = (double) evalExpression(baseConv.replace("iv", String.valueOf(iv)));
            
            // Base value -> result unit
            double toValue = (double) evalExpression(toConv.replace("iv", String.valueOf(iv)));
            return toValue;
        } catch (Exception ex) {
            Logger.getLogger(UnitsConverterController.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
    }

    private static Object evalExpression(String expression) throws Exception {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("js");
        return engine.eval(expression);
    }
}
