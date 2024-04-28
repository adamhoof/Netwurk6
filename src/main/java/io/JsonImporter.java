package io;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileReader;

public class JsonImporter {
    public NetworkData importNetworkData(File selectedFile) {
        try (FileReader reader = new FileReader(selectedFile)) {
            Gson gson = new Gson();
            return gson.fromJson(reader, NetworkData.class);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
