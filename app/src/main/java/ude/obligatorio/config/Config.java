package ude.obligatorio.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Config {

    private static final Pattern ENV_PATTERN = Pattern.compile("\\$\\{([A-Za-z_][A-Za-z0-9_]*)(?::-([^}]*))?}");

    private static Properties properties = new Properties();
    private static boolean cargado = false;

    private Config() {}

    public static synchronized void cargar() {
        if (cargado) {
            return;
        }

        try (InputStream is = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (is == null) {
                throw new IllegalStateException("No se encontró config.properties en el classpath");
            }
            properties.load(is);
            cargado = true;
        } catch (IOException e) {
            throw new IllegalStateException("Error al cargar config.properties: " + e.getMessage(), e);
        }
    }

    private static String resolver(String raw) {
        if (raw == null) {
            return null;
        }

        Matcher matcher = ENV_PATTERN.matcher(raw);
        if (!matcher.find()) {
            return raw;
        }

        matcher.reset();
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String nombre = matcher.group(1);
            String defecto = matcher.group(2);

            String valor = System.getProperty(nombre);

            if (valor == null || valor.isBlank()) {
                valor = System.getenv(nombre);
            }

            if (valor == null || valor.isBlank()) {
                if (defecto != null) {
                    valor = defecto;
                } else {
                    throw new IllegalStateException("Variable de entorno requerida no definida: '" + nombre
                            + "' (referenciada desde config.properties)");
                }
            }

            matcher.appendReplacement(sb, Matcher.quoteReplacement(valor));
        }

        matcher.appendTail(sb);
        return sb.toString();
    }

    public static String get(String key) {
        if (!cargado) {
            cargar();
        }
        return resolver(properties.getProperty(key));
    }

    public static String getObligatorio(String key) {
        String value = get(key);
        if (value == null) {
            throw new IllegalStateException("Propiedad requerida '" + key + "' no encontrada en config.properties");
        }
        return value;
    }

    public static int getInt(String key, int defaultValue) {
        String value = get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Valor inválido para '" + key + "': " + value, e);
        }
    }

    public static int getIntObligatorio(String key) {
        return Integer.parseInt(getObligatorio(key).trim());
    }

    public static String get(String key, String defaultValue) {
        String value = get(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value.trim());
    }
}
