package world.bentobox.abc.dos;

import java.util.UUID;

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
    @ConfigEntry(path = "abc.command.island")
    private String islandCommand = "island is skyblock sb";

    @ConfigComment("The ABC admin command.")
    @ConfigComment("To define alias, just separate commands with white space.")
    @ConfigEntry(path = "abc.command.admin")
    private String adminCommand = "bsbadmin bsb skyblockadmin sbadmin sba";


    @ConfigComment("Transaction announcement server host")
    @ConfigEntry(path = "abc.servers")
    private String host = "broker.mqttdashboard.com";

    @ConfigComment("UUID of player to receive payments. Make sure this is accurate")
    @ConfigComment("because all payments are final and cannot be reversed.")
    @ConfigComment("Player must have made an account on mc.tastybento.us to receive funds.")
    @ConfigComment("Format must include dashes, e.g., 5988eecd-1dcd-4080-a843-785b62419")
    @ConfigComment("Google 'Minecraft UUID lookup' if you need to find out your UUID.")
    @ConfigEntry(path = "abc.admin-uuid")
    private UUID adminUUID = UUID.fromString("5988eecd-1dcd-4080-a843-785b62419");

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
    public UUID getAdminUUID() {
        return adminUUID;
    }

    /**
     * @param adminUUID the adminUUID to set
     */
    public void setAdminUUID(UUID adminUUID) {
        this.adminUUID = adminUUID;
    }


}
