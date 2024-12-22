import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class ProxyServer {
    private int port;
    private String ipAddress;

    public ProxyServer(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public void getStat() {
        System.out.println("Statistiques du serveur :");
    
        System.out.println("Adresse IP du serveur proxy : " + ConfigManager.get("proxy.ip", "localhost"));
        System.out.println("Port du serveur proxy : " + ConfigManager.getInt("proxy.port", 9000));
    
        System.out.println("Durée d'expiration du cache (unités) : " + ConfigManager.get("cache.expiration.unit", "SEC"));
        System.out.println("Durée d'expiration du cache (valeur) : " + ConfigManager.getLong("cache.expiration.duration", 60));
        System.out.println("Répertoire du cache : " + ConfigManager.get("cache.directory", "cache"));
        System.out.println("Répertoire de mapping du cache : " + ConfigManager.get("cache.mapping", "file/cacheMapping.txt"));
        System.out.println("Mémoire cache dédiée (en nombre d'éléments) : " + ConfigManager.getInt("cache.max.memory", 100));
        System.out.println("Mémoire occupée par un fichier pour le cache (en octets) : " + ConfigManager.getLong("cache.max.memory.item", 1048576));
    
        System.out.println("Port à écouter : " + ConfigManager.getInt("ecoute.port", 80));
    }    

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port, 50, InetAddress.getByName(ipAddress))) {
            System.out.println("Serveur proxy IP: " + ipAddress + ", Port: " + port);

            new Thread(this::commandes).start();

            ConfigManager.configureCacheCleaner();
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("client connecté: " + clientSocket.getInetAddress());

                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("serveur non initialisé: " + e.getMessage());
        }
    }

    private void commandes() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("> ");
            String command = scanner.nextLine().trim();
            String[] commandParts = command.split(" ", 2);

            switch (commandParts[0].toLowerCase()) {
                case "stop":
                    System.out.println("arrêt du serveur");
                    System.exit(0);
                    break;

                case "stat":
                    getStat();
                    break;
                    
                case "pat":
                    System.out.println("La cavalerie est là !");
                    break;
                    
                case "version":
                    System.out.println("version 1.0.0 Pat&Fe proxyCache");
                    break;

                case "fenitra":
                    System.out.println("Je suis une scout !");
                    break;

                case "clear":
                    CacheManager.clearCache();
                    System.out.println("cache a été vidé en totalité");
                    break;

                case "ls":
                    CacheManager.listCache();
                    break;

                case "lsurl":
                    CacheManager.listUrlsFromMappingFile();
                    break;
    
                case "checkconf":
                    try {
                        ConfigManager.checkConf();
                    } catch (Exception e) {
                        System.err.println("Erreur lors de la vérification de la configuration : " + e.getMessage());
                    }
                    break;
        
                case "delete":
                    if (commandParts.length < 2) {
                        System.out.println("veuillez spécifier l'URL à supprimer du cache.");
                    } else {
                        String urlToDelete = commandParts[1];
                        CacheManager.deleteAllFromUrl(urlToDelete);
                    }
                    break;
                default:
                    System.out.println("commande inconnue");
                    break;
            }
        }
    }
}
