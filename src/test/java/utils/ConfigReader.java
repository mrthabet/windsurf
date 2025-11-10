package utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {
    private static final Properties PROPS = new Properties();

    static {
        try (InputStream input = ConfigReader.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                // Allow running with only system properties or env vars
            } else {
                PROPS.load(input);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }
    }

    public static String get(String key) {
        String value = System.getProperty(key);
        if (value == null) {
            value = PROPS.getProperty(key);
        }
        if (value == null) {
            value = System.getenv(key.replace('.', '_').toUpperCase());
        }
        if (value == null) {
            throw new IllegalArgumentException("Missing config key: " + key);
        }
        return value;
    }
}
