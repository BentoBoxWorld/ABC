package world.bentobox.abc;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.map.MapView;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;

import okhttp3.OkHttpClient;
import world.bentobox.abc.commands.AdminCommand;
import world.bentobox.abc.commands.UserCommand;
import world.bentobox.abc.crypto.Crypto;
import world.bentobox.abc.dos.Code;
import world.bentobox.abc.dos.Settings;
import world.bentobox.abc.listeners.JoinLeaveListener;
import world.bentobox.abc.qr.QRCodeGenerator;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.configuration.Config;
import world.bentobox.bentobox.database.Database;
import world.bentobox.bentobox.database.json.BentoboxTypeAdapterFactory;

public final class ABC extends Addon {

    private Config<Settings> configObject = new Config<>(this, Settings.class);

    private Mqtt5BlockingClient client;

    private AdminCommand adminCommand;

    private @Nullable Settings settings;

    private Gson gson;

    private Database<Code> database;

    private Crypto crypto;

    // one instance, reuse
    private final OkHttpClient httpClient = new OkHttpClient();

    @Override
    public void onLoad() {
        // Save the default config from config.yml
        saveDefaultConfig();
        // Load settings from config.yml. This will check if there are any issues with it too.
        loadSettings();
        // Crypto
        try {
            crypto = new Crypto(this);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
            logError("Hmm, this server does not seem to support crypto. Disabling addon. " + e.getMessage());
            this.setState(State.DISABLED);
            return;
        }
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
        // Get gson
        getGson();
        // Hook into game modes
        getPlugin().getAddonsManager().getGameModeAddons().stream()
        .forEach(gm -> {
            log("ABC hooking into " + gm.getDescription().getName());
            gm.getAdminCommand().ifPresent(adminCommand -> new AdminCommand(this, adminCommand));
            gm.getPlayerCommand().ifPresent(playerCommand -> new UserCommand(this, playerCommand));
        });
        // Subscribe to topics
        subscribe();
        // Load maps
        loadMaps();
        // Register listeners
        registerListener(new JoinLeaveListener(this));
    }

    private void loadMaps() {
        log("Loading maps");
        database = new Database<>(this, Code.class);
        // Load any maps
        List<Code> maps = database.loadObjects();
        maps.forEach(code -> {
            @SuppressWarnings("deprecation")
            MapView map = Bukkit.getMap(Integer.valueOf(code.getUniqueId()));
            if (map == null) {
                database.deleteID(code.getUniqueId());
            } else {
                map.getRenderers().clear();
                map.setCenterX(64);
                map.setCenterZ(64);
                map.addRenderer(new QRCodeGenerator(this, code));
            }
        });

    }

    private void subscribe() {
        // Server
        client = Mqtt5Client.builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost(settings.getHost())
                .buildBlocking();
        // Announcement server
        Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            client.connect();
            log("Connected to ABC announcement server");
            client.toAsync().subscribeWith()
            .topicFilter("bentobox/abc/" + settings.getAdminUUID().toString())
            .qos(MqttQos.AT_MOST_ONCE)
            .callback(this::process)
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

    /**
     * This is run async!
     * @param cb
     */
    private void process(Mqtt5Publish cb) {
        byte[] payload = cb.getPayloadAsBytes();
        if (payload.length == 0) return;
        String p = new String(payload);
        String json = new String(Base64.getUrlDecoder().decode(p));
        Code code = gson.fromJson(json, Code.class);
        // Run checks on the code
        boolean hashVerified = getCrypto().verify(code.toString(), code.getHash());
        if (hashVerified) {
            // Execute the command
            Bukkit.getScheduler().runTask(getPlugin(), () ->
            Bukkit.getServer().dispatchCommand(getServer().getConsoleSender(), code.getCommand()));
        } else {
            BentoBox.getInstance().logError("Signature verification failed!\n" + code);
            BentoBox.getInstance().logError("This could indicate a hacking attempt.");
        }
    }

    public Gson getGson() {
        if (gson == null) {
            GsonBuilder builder = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().enableComplexMapKeySerialization();
            // Register adapter factory
            builder.registerTypeAdapterFactory(new BentoboxTypeAdapterFactory(BentoBox.getInstance()));
            gson = builder.create();
        }
        return gson;
    }

    public void saveMap(Code code) {
        database.saveObject(code);
    }

    /**
     * @return the crypto
     */
    public Crypto getCrypto() {
        return crypto;
    }

    /**
     * @return the client
     */
    public Mqtt5BlockingClient getClient() {
        return client;
    }

    /**
     * @return the httpClient
     */
    public OkHttpClient getHttpClient() {
        return httpClient;
    }
}
