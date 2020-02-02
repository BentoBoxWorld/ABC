package world.bentobox.abc;

import java.nio.charset.Charset;
import java.util.UUID;

import org.bukkit.Bukkit;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;

import world.bentobox.bentobox.api.addons.Addon;

public final class ABC extends Addon {

    final Mqtt5BlockingClient client = Mqtt5Client.builder()
            .identifier(UUID.randomUUID().toString())
            .serverHost("broker.hivemq.com")
            .buildBlocking();


    @Override
    public void onLoad() {
        // Nothing to do
    }

    @Override
    public void onEnable() {
        log("Built and trying to connect");
        Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            client.connect();
            log("Connected");
            log("Subscribing");
            client.toAsync().subscribeWith()
            .topicFilter("test/topic")
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

}
