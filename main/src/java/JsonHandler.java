import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;

public class JsonHandler {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Read Store object from JSON file
    public static Store readStoreFromJson(String filePath) {
        try {
            return objectMapper.readValue(new File(filePath), Store.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Write Store object to JSON file
    public static void writeStoreToJson(Store store, String filePath) {
        try {
            objectMapper.writeValue(new File(filePath), store);
            System.out.println("Store data saved successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
