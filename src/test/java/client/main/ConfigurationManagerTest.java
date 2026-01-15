package client.main;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ConfigurationManagerTest {
    private ConfigurationManager configManager;

    @BeforeEach
    public void setUp() {
        configManager = new ConfigurationManager();
    }

    @Test
    public void LoadProperties_ValidPath_LoadsSuccessfully() throws IOException {
        configManager.loadProperties("/test.properties");
        assertThat(configManager.getString("test.key"), is("hello"));
    }

    @Test
    public void LoadProperties_NullPath_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> configManager.loadProperties(null));
    }

    @Test
    public void LoadProperties_NonExistentFile_ThrowsFileNotFoundException() {
        assertThrows(FileNotFoundException.class, () -> configManager.loadProperties("/missing.properties"));
    }

    @Test
    public void GetString_ExistingKey_ReturnsValue() throws IOException {
        configManager.loadProperties("/test.properties");
        assertThat(configManager.getString("test.key"), is("hello"));
    }

    @Test
    public void GetString_NonExistentKey_ReturnsNull() {
        assertThat(configManager.getString("missing.key"), nullValue());
    }

    @Test
    public void GetString_NullKey_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> configManager.getString(null));
    }

    @Test
    public void GetBoolean_ValidTrue_ReturnsTrue() throws IOException {
        configManager.loadProperties("/test.properties");
        assertThat(configManager.getBoolean("config.true"), is(true));
    }

    @Test
    public void GetBoolean_ValidFalse_ReturnsFalse() throws IOException {
        configManager.loadProperties("/test.properties");
        assertThat(configManager.getBoolean("config.false"), is(false));
    }

    @Test
    public void GetBoolean_InvalidString_ReturnsFalse() throws IOException {
        configManager.loadProperties("/test.properties");
        assertThat(configManager.getBoolean("test.key"), is(false));
    }

    @Test
    public void GetBoolean_NullKey_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> configManager.getBoolean(null));
    }

    @Test
    public void GetBoolean_MissingKey_ThrowsNullPointerException() throws IOException {
        configManager.loadProperties("/test.properties");
        assertThrows(NullPointerException.class, () -> configManager.getBoolean("missing.boolean"));
    }
}