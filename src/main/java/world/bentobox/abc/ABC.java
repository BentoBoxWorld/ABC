package world.bentobox.abc;

import java.nio.charset.Charset;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.eclipse.jdt.annotation.Nullable;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;

import world.bentobox.abc.commands.AdminCommand;
import world.bentobox.abc.dos.Settings;
import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.configuration.Config;

public final class ABC extends Addon {

    private Config<Settings> configObject = new Config<>(this, Settings.class);

    private Mqtt5BlockingClient client;

    private AdminCommand adminCommand;

    private @Nullable Settings settings;


    @Override
    public void onLoad() {
        // Save the default config from config.yml
        saveDefaultConfig();
        // Load settings from config.yml. This will check if there are any issues with it too.
        loadSettings();
        // Register commands
        adminCommand = new AdminCommand(this);
    }

    private boolean loadSettings() {
        // Load settings again to get worlds
        settings = configObject.loadConfigObject();
        if (settings == null) {
            // Disable
            logError("ABC settings could not load! Addon disabled.");
            setState(State.DISABLED);
            return false;
        }
        // Update to latest version
        configObject.saveConfigObject(settings);
        return true;
    }

    @Override
    public void onEnable() {
        client = Mqtt5Client.builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost(settings.getHost())
                .buildBlocking();
        log("Built and trying to connect");
        Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            client.connect();
            log("Connected");
            log("Subscribing");
            client.toAsync().subscribeWith()
            .topicFilter("bentobox/abc/" + settings.getAdminUUID().toString())
            .qos(MqttQos.AT_MOST_ONCE)
            .callback(cb -> {
                log("Topic : " + cb.getTopic().toString());
                Charset charset = Charset.forName("ISO-8859-1");
                cb.getPayload().ifPresent(p -> log("Content: " + charset.decode(p)));
            })
            .send();
        });

    }

    @Override
    public void onDisable() {
        if (client != null ) client.disconnect();
    }

    public Settings getSettings() {
        return settings;
    }

    /**
     * @return the adminCommand
     */
    public AdminCommand getAdminCommand() {
        return adminCommand;
    }

}
