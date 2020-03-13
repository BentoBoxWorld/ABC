package world.bentobox.abc.listeners;

import java.util.Base64;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;

import world.bentobox.abc.ABC;
import world.bentobox.abc.dos.Code;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;

/**
 * Listens for new players and quitters
 * @author tastybento
 *
 */
public class JoinLeaveListener implements Listener {

    ABC addon;

    public JoinLeaveListener(ABC addon) {
        this.addon = addon;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Bukkit.getScheduler().runTaskAsynchronously(addon.getPlugin(), () -> {
            addon.getClient().toAsync().subscribeWith()
            .topicFilter("bentobox/abc/" + e.getPlayer().getUniqueId())
            .qos(MqttQos.AT_MOST_ONCE)
            .callback(cb -> process(e.getPlayer().getUniqueId(), cb))
            .send();
        });
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        Bukkit.getScheduler().runTaskAsynchronously(addon.getPlugin(), () -> {
            addon.getClient().toAsync().unsubscribeWith()
            .topicFilter("bentobox/abc/" + e.getPlayer().getUniqueId())
            .send();
        });
    }

    /**
     * This is run async!
     * @param uuid
     * @param cb
     */
    private void process(UUID uuid, Mqtt5Publish cb) {
        Player pl = Bukkit.getPlayer(uuid);
        if (pl == null) return;
        byte[] payload = cb.getPayloadAsBytes();
        if (payload.length == 0) return;
        String p = new String(payload);
        String json = new String(Base64.getUrlDecoder().decode(p));
        Code code = addon.getGson().fromJson(json, Code.class);
        // Verify player
        if (uuid.toString().equals(code.getId())) {
            String name = addon.getPlayers().getName(code.getPaidBy());
            if (name.isEmpty()) name = "Unknown";
            User.getInstance(pl).sendMessage("abc.payment.you-got-money", TextVariables.NUMBER, String.valueOf(code.getAmount()), TextVariables.NAME, name);
            pl.playSound(pl.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 2F);
        } else {
            addon.logError("A payment notification for " + pl.getName() + " (" + pl.getUniqueId() + ") was received but the security check failed.");
        }
    }
}
