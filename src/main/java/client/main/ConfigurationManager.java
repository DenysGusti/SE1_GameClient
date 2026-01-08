package client.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

public class ConfigurationManager {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationManager.class);

    private final Properties properties = new Properties();

    public void loadProperties(String propertiesPath) throws IOException {
        if (propertiesPath == null)
            throw new IllegalArgumentException("propertiesPath is null");

        try (InputStream inputStream = getClass().getResourceAsStream(propertiesPath)) {
            if (inputStream == null)
                throw new FileNotFoundException("Properties file not found: " + propertiesPath);

            properties.load(inputStream);
        }
    }

    public String getString(String key) {
        if (key == null)
            throw new IllegalArgumentException("key is null");

        return properties.getProperty(key);
    }

    public boolean getBoolean(String key) {
        if (key == null)
            throw new IllegalArgumentException("key is null");

        String keyString = Objects.requireNonNull(getString(key), "getString(key) is null");
        return Boolean.parseBoolean(keyString);
    }
}
