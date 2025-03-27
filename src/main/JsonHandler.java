package main;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsonHandler {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public JsonHandler() {
    }


    public static void writeStoreToJson(Store store, String filePath) {
        try {
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            List<Store> stores = readStoresFromJson(filePath);
            boolean storeExists = false;
            for (Store existingStore : stores) {
                if (existingStore.getStoreName().equals(store.getStoreName())) {
                    // Update the existing store's products
                    existingStore.setProducts(store.getProducts());
                    storeExists = true;
                    break;
                }
            }
            if (!storeExists){
                stores.add(store);
            }

            System.out.println(stores.size());
            objectMapper.writeValue(new File(filePath), stores);

            System.out.println("Store data appended successfully.");
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public static List<Store> readStoresFromJson(String filePath) throws IOException {
        File file = new File(filePath);

        if (!file.exists() || file.length() == 0) {
            return new ArrayList<>();
        }

        return objectMapper.readValue(file, objectMapper.getTypeFactory().constructCollectionType(List.class, Store.class));
    }


    public static List<Store> parseStoresFromJsonString(String jsonStores) throws IOException {
        if (jsonStores == null || jsonStores.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return objectMapper.readValue(
                jsonStores,
                objectMapper.getTypeFactory().constructCollectionType(List.class, Store.class)
        );
    }

    // Parses a single Store from JSON string
    public static Store parseStoreFromJsonString(String jsonStore) throws IOException {
        return objectMapper.readValue(jsonStore, Store.class);
    }

    // Converts a List<Store> to JSON string (for sending over network)
    public static String serializeStoresToJsonString(List<Store> stores) throws IOException {
        return objectMapper.writeValueAsString(stores);
    }

    // Converts a single Store to JSON string
    public static String serializeStoreToJsonString(Store store) throws IOException {
        return objectMapper.writeValueAsString(store);
    }

}
