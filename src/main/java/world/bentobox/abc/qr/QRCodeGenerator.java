package world.bentobox.abc.qr;

import java.awt.Color;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.map.MapView.Scale;
import org.bukkit.map.MinecraftFont;
import org.bukkit.util.Vector;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import world.bentobox.abc.ABC;
import world.bentobox.abc.dos.Code;
import world.bentobox.bentobox.api.addons.Addon;


/**
 * A QR code generator for Minecraft maps
 * @author tastybento
 *
 */
public class QRCodeGenerator extends MapRenderer implements Listener {

    private static final String WEB_SITE = "https://bento.cash/welcome.php";
    private BitMatrix bitMatrix;
    private ABC addon;
    private Code code;
    private Set<UUID> viewingPlayers;
    private Player viewingPlayer;
    private enum MapState {
        GET_CLOSER,
        TOO_MANY,
        JUST_RIGHT
    }
    private MapState state = MapState.GET_CLOSER;

    /**
     * Generate a QR code map renderer.
     * @param addon2 - addon
     * @param code - the QR code to render
     */
    public QRCodeGenerator(Addon addon2, Code code) {
        this.addon = (ABC)addon2;
        this.code = code;
        viewingPlayers = new HashSet<>();
        addon.registerListener(this);
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent e) {
        viewingPlayers.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    private void onPlayerDeath(PlayerDeathEvent e) {
        viewingPlayers.remove(e.getEntity().getUniqueId());
    }

    @Override
    public void render(MapView map, MapCanvas canvas, Player player) {
        if (player.getInventory().getItemInMainHand().getType().equals(Material.FILLED_MAP)) {
            // Check the map's id
            MapMeta meta = (MapMeta) player.getInventory().getItemInMainHand().getItemMeta();
            if (meta.getMapView().getId() == map.getId()) {
                // Map is in player's hand
                renderMap(map, canvas, player);
                return;
            }
        }
        // Map must be in an item frame
        // Check if player is close enough and looking at map
        if (reallyLooking(player, map)) {
            viewingPlayers.add(player.getUniqueId());
        } else {
            viewingPlayers.remove(player.getUniqueId());
        }
        if (viewingPlayers.isEmpty()) {
            if (!state.equals(MapState.GET_CLOSER)) {
                state = MapState.GET_CLOSER;
                renderQR(canvas, WEB_SITE);
                showText(canvas, "Get Closer To Scan", false);
                this.viewingPlayer = null;
            }
            return;
        } else if (viewingPlayers.size() > 1) {
            if (!state.equals(MapState.TOO_MANY)) {
                state = MapState.TOO_MANY;
                renderQR(canvas, WEB_SITE);
                showText(canvas, "One player at a time!", false);
                this.viewingPlayer = null;
            }
            return;
        }
        // Only render once per player otherwise this will run every tick
        if (state != MapState.JUST_RIGHT) {
            state = MapState.JUST_RIGHT;
            // Return the first time
            return;
        }
        renderMap(map, canvas, player);
    }

    private void renderMap(MapView map, MapCanvas canvas, Player player) {
        if (!player.equals(viewingPlayer)) {
            this.viewingPlayer = player;
            // Make a copy of the code and customize it
            Code toCodeCode = new Code(code);
            // convert @p etc to player name in code's command
            if (toCodeCode.getCommand() != null) {
                toCodeCode.setCommand(toCodeCode.getCommand().replace("@p", player.getName()));
            }
            // Insert payer
            toCodeCode.setFbo(player.getUniqueId());
            // Sign code
            toCodeCode.setHash(addon.getCrypto().sign(toCodeCode.toString()));
            // Make the payload
            String data = addon.getGson().toJson(toCodeCode);
            // Put it into a URL
            String toCode = WEB_SITE + "?code=" + Base64.getUrlEncoder().encodeToString(data.getBytes());
            map.setScale(Scale.NORMAL);
            renderQR(canvas, toCode);
            // Put a title on it
            // TODO: customize
            showText(canvas, player.getName() + " scan code", false);
        }
    }

    private void renderQR(MapCanvas canvas, String toCode) {
        // Create the QR code
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            // Map size is 128 x 128 max
            bitMatrix = qrCodeWriter.encode(toCode, BarcodeFormat.QR_CODE, 128, 128);
        } catch (WriterException e) {
            addon.logError("QR code generation error: " + e.getMessage());
            return;
        }
        // Render
        for (int i = 0; i < bitMatrix.getWidth(); i++) {
            for (int j = 0; j < bitMatrix.getHeight(); j++) {
                @SuppressWarnings("deprecation")
                byte color = bitMatrix.get(i, j) ? MapPalette.matchColor(Color.BLACK) : MapPalette.WHITE;
                canvas.setPixel(i, j, color);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void showText(MapCanvas canvas, String title, boolean blank) {
        if (blank) {
            for (int i = 0; i < 255; i++) {
                for (int j = 0; j < 255; j++) {
                    canvas.setPixel(i, j, MapPalette.WHITE);
                }
            }
        }
        int x = (128 - MinecraftFont.Font.getWidth(title)) / 2;
        canvas.drawText(x, 5, MinecraftFont.Font, title);
    }

    // Check if player is really looking at the map
    private boolean reallyLooking(Player p, MapView map) {
        if (p == null) return false;
        // Todo check for this specific map
        return p.getNearbyEntities(1, 1, 1).stream().filter(e -> e.getType().equals(EntityType.ITEM_FRAME)).anyMatch(f -> getLookingAt(p,f));
    }

    private boolean getLookingAt(Player player, Entity frame)
    {
        Location eye = player.getEyeLocation();
        Vector toEntity = frame.getLocation().toVector().subtract(eye.toVector());
        double dot = toEntity.normalize().dot(eye.getDirection());
        return dot > 0.80D;
    }

}
