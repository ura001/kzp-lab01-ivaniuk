package ua.lpnu.kzp;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Надає метадані збірки, сформовані Maven під час побудови проєкту.
 */
public final class BuildInfo {

    private static final String PROPERTIES_PATH = "/app.properties";

    private BuildInfo() {
        // Службовий клас без екземплярів
    }

    /**
     * Повертає версію проєкту з {@code pom.xml} або {@code unknown}, якщо ресурс недоступний.
     *
     * @return рядок версії збірки
     */
    public static String version() {
        Properties props = new Properties();
        try (InputStream in = BuildInfo.class.getResourceAsStream(PROPERTIES_PATH)) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException ignored) {
            return "unknown";
        }
        return props.getProperty("app.version", "unknown");
    }
}