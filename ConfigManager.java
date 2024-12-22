import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ConfigManager {
    private static final Properties properties = new Properties();

    static {
        String configFilePath = System.getProperty("config.file", "file/config.properties");
        try (FileInputStream input = new FileInputStream(configFilePath)) {
            properties.load(input);
        } catch (IOException e) {
            System.err.println("Erreur : Impossible de charger le fichier conf ("
                    + configFilePath + ") : " + e.getMessage());
        }
    }

    public static String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public static int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(properties.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static long getLong(String key, long defaultValue) {
        try {
            return Long.parseLong(properties.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static void configureCacheCleaner() {
        String unit = ConfigManager.get("cache.expiration.unit", "MIN");
        long duration = ConfigManager.getLong("cache.expiration.duration", 30);
    
        TimeUnit timeUnit;
        switch (unit.toUpperCase()) {
            case "SEC":
                timeUnit = TimeUnit.SECONDS;
                break;
            case "MIN":
                timeUnit = TimeUnit.MINUTES;
                break;
            case "HEURE":
                timeUnit = TimeUnit.HOURS;
                break;
            default:
                System.err.println("Unité inconnue : " + unit + ". Utilisation de MINUTES par défaut.");
                timeUnit = TimeUnit.MINUTES;
        }
    
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("Nettoyage automatique du cache terminé.");
            CacheManager.clearCache();
        }, duration, duration, timeUnit);
    }
    
    public static void checkConf() {
        StringBuilder errors = new StringBuilder();
        
        String proxyIp = get("proxy.ip", null);
        if (proxyIp == null || proxyIp.isEmpty()) {
            errors.append("- L'adresse IP du proxy (proxy.ip) est manquante ou vide.\n");
        }
        
        int proxyPort = getInt("proxy.port", -1);
        if (proxyPort <= 0 || proxyPort > 65535) {
            errors.append("- Le port du proxy (proxy.port) doit être entre 1 et 65535.\n");
        }
        
        int maxMemory = getInt("cache.max.memory", -1);
        if (maxMemory <= 0) {
            errors.append("- La taille maximale de la mémoire cache (cache.max.memory) doit être positive.\n");
        }
        
        long maxMemoryItem = getLong("cache.max.memory.item", -1);
        if (maxMemoryItem <= 0) {
            errors.append("- La taille maximale d'un fichier dans le cache (cache.max.memory.item) doit être positive.\n");
        }
        
        int ecoutePort = getInt("ecoute.port", -1);
        if (ecoutePort <= 0 || ecoutePort > 65535) {
            errors.append("- Le port à écouter (ecoute.port) doit être entre 1 et 65535.\n");
        }
        
        if (errors.length() > 0) {
            System.err.println("Erreurs détectées dans la configuration :\n" + errors);
        } else {
            System.out.println("Configuration vérifiée avec succès !");
        }
    }
    
}
