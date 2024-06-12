package util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ReadProperties {

    private static ReadProperties instance = null;
    private Properties properties = null;

    private ReadProperties() {
        String configFilePath = "src/config.properties";
        try (FileInputStream propsInput = new FileInputStream(configFilePath)){
            this.properties = new Properties();
            this.properties.load(propsInput);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized ReadProperties getInstance() {
        if (instance == null)
            instance = new ReadProperties();
        return instance;
    }

    public String getValue(String propKey){
        return this.properties.getProperty(propKey);
    }
}
