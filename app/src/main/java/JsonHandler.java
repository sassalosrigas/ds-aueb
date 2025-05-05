package main;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsonHandler {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public JsonHandler() {
    }

    public static Store readStoreFromJson(String filePath) throws IOException {
        /*
        Diavazei ena json arxeio kai dhmiourgei ena antikeimeno tupou Store apo auto
         */
        File file = new File(filePath);

        if (!file.exists()) { //elexos an iparxei to path
            throw new FileNotFoundException("JSON file not found: " + filePath);
        }
        if (file.length() == 0) {  //an to arxeio einai keno
            throw new IOException("JSON file is empty: " + filePath);
        }

        return objectMapper.readValue(file, Store.class);
    }



}
