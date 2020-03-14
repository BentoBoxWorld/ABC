package world.bentobox.abc.dos;

import world.bentobox.bentobox.api.configuration.ConfigComment;
import world.bentobox.bentobox.api.configuration.ConfigEntry;
import world.bentobox.bentobox.api.configuration.ConfigObject;
import world.bentobox.bentobox.api.configuration.StoreAt;

@StoreAt(filename="config.yml", path="addons/ABC") // Explicitly call out what name this should have.
@ConfigComment("ABC Configuration [version]")
public class Settings implements ConfigObject {
    /* Commands */
    @ConfigComment("Player command for ABC")
    @ConfigComment("To define alias, just separate commands with white space.")
    @ConfigEntry(path = "abc.command.player")
    private String islandCommand = "abc";

    @ConfigComment("The ABC admin command.")
    @ConfigComment("To define alias, just separate commands with white space.")
    @ConfigEntry(path = "abc.command.admin")
    private String adminCommand = "abc";


    @ConfigComment("Transaction announcement server host. Note that this is a public server.")
    @ConfigEntry(path = "abc.servers")
    private String host = "broker.mqttdashboard.com";

    @ConfigComment("UUID of player to receive payments. Make sure this is accurate")
    @ConfigComment("because all payments are final and cannot be reversed.")
    @ConfigComment("Player must have made an account on mc.tastybento.us to receive funds.")
    @ConfigComment("Format must include dashes, e.g., 5988eecd-1dcd-4080-a843-785b62419")
    @ConfigComment("Google 'Minecraft UUID lookup' if you need to find out your UUID.")
    @ConfigEntry(path = "abc.admin-uuid")
    private String adminUUID = "5988eecd-1dcd-4080-a843-785b62419";

    @ConfigComment("Admin password. If you don't want to put this in a config file, leave it blank")
    @ConfigComment("and set it with the abc admin setpassword command on every server restart.")
    @ConfigEntry(path = "abc.admin-password")
    private String adminPassword = "";

    @ConfigComment("Name of your server")
    @ConfigEntry(path = "abc.server-name")
    private String serverName = "A BentoBox Server";

    @ConfigComment("How many xp you will buy for ABC$1 Bento Cash")
    @ConfigComment("when the player runs the cashxp command. The amount is paid from the admin account.")
    @ConfigEntry(path = "abc.exchange-rate")
    private int exchangeRate = 100;

    /**
     * @return the islandCommand
     */
    public String getIslandCommand() {
        return islandCommand;
    }

    /**
     * @param islandCommand the islandCommand to set
     */
    public void setIslandCommand(String islandCommand) {
        this.islandCommand = islandCommand;
    }

    /**
     * @return the adminCommand
     */
    public String getAdminCommand() {
        return adminCommand;
    }

    /**
     * @param adminCommand the adminCommand to set
     */
    public void setAdminCommand(String adminCommand) {
        this.adminCommand = adminCommand;
    }

    /**
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * @param host the host to set
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @return the adminUUID
     */
    public String getAdminUUID() {
        return adminUUID;
    }

    /**
     * @param adminUUID the adminUUID to set
     */
    public void setAdminUUID(String adminUUID) {
        this.adminUUID = adminUUID;
    }

    /**
     * @return the adminPassword
     */
    public String getAdminPassword() {
        return adminPassword;
    }

    /**
     * @param adminPassword the adminPassword to set
     */
    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    /**
     * @return the serverName
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * @param serverName the serverName to set
     */
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    /**
     * @return the exchangeRate
     */
    public int getExchangeRate() {
        return exchangeRate;
    }

    /**
     * @param exchangeRate the exchangeRate to set
     */
    public void setExchangeRate(int exchangeRate) {
        this.exchangeRate = exchangeRate;
    }


}
