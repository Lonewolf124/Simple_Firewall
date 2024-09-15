import java.io.*;
import java.nio.file.*;
import java.util.*;

public class firewall_project {
    List<String> blockedEntries = new ArrayList<>();
    private static final String HOSTS_FILE_PATH =
            System.getProperty("os.name").toLowerCase().contains("win") ?
                    "C:\\Windows\\System32\\drivers\\etc\\hosts" : "/etc/hosts";

    public static void main(String[] args) {
        firewall_project firewall = new firewall_project();
        firewall.backupHostsFile();
        firewall.start();
    }

    public void backupHostsFile() {
        try {
            Path source = Paths.get(HOSTS_FILE_PATH);
            Path backup = Paths.get(HOSTS_FILE_PATH + ".bak");
            Files.copy(source, backup, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Backup of hosts file created.");
        } catch (IOException e) {
            System.err.println("Error creating backup: " + e.getMessage());
        }
    }

    public void start() {
        Scanner sc = new Scanner(System.in);
        boolean options = true;

        while (options) {
            System.out.println("Firewall - IP & Domain Blocking");
            System.out.println("1. Block an IP Address");
            System.out.println("2. Unblock an IP Address");
            System.out.println("3. Block a Domain");
            System.out.println("4. Unblock a Domain");
            System.out.println("5. View Blocked Entries");
            System.out.println("6. Clear All Blocked Entries");
            System.out.println("7. Exit");
            System.out.print("Choose an option: ");
            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    blockIp();
                    break;

                case 2:
                    unblockIp();
                    break;

                case 3:
                    blockDomain();
                    break;

                case 4:
                    unblockDomain();
                    break;

                case 5:
                    viewBlockedEntries();
                    break;

                case 6:
                    clearAllBlockedEntries();
                    break;

                case 7:
                    options = false;
                    System.out.println("Exiting Firewall...");
                    break;

                default:
                    System.out.println("Invalid option! Please try again.");
            }
        }

        saveBlockedEntries();
    }

    public static boolean isValidIPAddress(String ip) {
        String ipPattern =
                "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        return ip.matches(ipPattern);
    }

    public void blockIp() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter IP (IPv4) address to block: ");
        String ipAddr = sc.nextLine();

        if (isValidIPAddress(ipAddr)) {
            if (!blockedEntries.contains(ipAddr)) {
                blockedEntries.add(ipAddr);
                addIpToHostsFile(ipAddr);
                flushDNSCache(); // Flush DNS cache to apply changes
                System.out.println(ipAddr + " has been blocked.");
            } else {
                System.out.println(ipAddr + " is already blocked.");
            }
        } else {
            System.out.println("Invalid IP address format.");
        }
    }

    public void unblockIp() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter IP (IPv4) address to unblock: ");
        String ipAddr = sc.nextLine();

        if (blockedEntries.remove(ipAddr)) {
            removeEntryFromHostsFile(ipAddr);
            flushDNSCache(); // Flush DNS cache to apply changes
            System.out.println(ipAddr + " has been unblocked.");
        } else {
            System.out.println(ipAddr + " was not found in the block list.");
        }
    }

    public void blockDomain() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter domain to block: ");
        String domain = sc.nextLine();

        if (!blockedEntries.contains(domain)) {
            blockedEntries.add(domain);
            addDomainToHostsFile(domain);
            flushDNSCache();
            System.out.println(domain + " has been blocked.");
        } else {
            System.out.println(domain + " is already blocked.");
        }
    }

    public void unblockDomain() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter domain to unblock: ");
        String domain = sc.nextLine();

        if (blockedEntries.remove(domain)) {
            removeEntryFromHostsFile(domain);
            flushDNSCache();
            System.out.println(domain + " has been unblocked.");
        } else {
            System.out.println(domain + " was not found in the block list.");
        }
    }

    public void viewBlockedEntries() {
        System.out.println("Blocked Entries:");
        for (String entry : blockedEntries) {
            System.out.println(entry);
        }
    }

    public void clearAllBlockedEntries() {
        for (String entry : blockedEntries) {
            removeEntryFromHostsFile(entry);
        }
        blockedEntries.clear();
        saveBlockedEntries();
        System.out.println("All blocked entries have been removed.");
    }

    public void addIpToHostsFile(String ip) {
        try {
            String entry = "127.0.0.1 " + ip + "\n";
            List<String> lines = Files.readAllLines(Paths.get(HOSTS_FILE_PATH));
            if (!lines.contains(entry)) {
                Files.write(Paths.get(HOSTS_FILE_PATH), entry.getBytes(), StandardOpenOption.APPEND);
                System.out.println(ip + " added to hosts file.");
            } else {
                System.out.println(ip + " already exists in the hosts file.");
            }
        } catch (IOException e) {
            System.err.println("Error modifying hosts file: " + e.getMessage());
        }
    }

    public void addDomainToHostsFile(String domain) {
        try {
            String entry = "127.0.0.1 " + domain + "\n";
            List<String> lines = Files.readAllLines(Paths.get(HOSTS_FILE_PATH));
            if (!lines.contains(entry)) {
                Files.write(Paths.get(HOSTS_FILE_PATH), entry.getBytes(), StandardOpenOption.APPEND);
                System.out.println(domain + " added to hosts file.");
            } else {
                System.out.println(domain + " already exists in the hosts file.");
            }
        } catch (IOException e) {
            System.err.println("Error modifying hosts file: " + e.getMessage());
        }
    }

    public void removeEntryFromHostsFile(String entry) {
        try {
            Path path = Paths.get(HOSTS_FILE_PATH);
            List<String> lines = Files.readAllLines(path);
            lines.removeIf(line -> line.contains(entry));
            Files.write(path, lines);
            System.out.println(entry + " removed from hosts file.");
        } catch (IOException e) {
            System.err.println("Error modifying hosts file: " + e.getMessage());
        }
    }

    public void saveBlockedEntries() {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("blocked_entries.txt"))) {
            for (String entry : blockedEntries) {
                writer.write(entry);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving blocked entries: " + e.getMessage());
        }
    }

    public void flushDNSCache() {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                ProcessBuilder processBuilder = new ProcessBuilder("ipconfig", "/flushdns");
                Process process = processBuilder.start();
                process.waitFor();
                System.out.println("DNS cache flushed.");
            } else {
                System.out.println("DNS cache flush is not supported on this operating system.");
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error flushing DNS cache: " + e.getMessage());
        }
    }

}

