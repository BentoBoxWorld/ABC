package world.bentobox.abc.commands;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;

import world.bentobox.abc.ABC;
import world.bentobox.abc.dos.Code;
import world.bentobox.abc.qr.QRCodeGenerator;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;

/**
 * Admin command to make QR codes
 * @author tastybento
 *
 */
public class AdminCommand extends CompositeCommand {

    public AdminCommand(ABC addon) {
        super(addon,
                addon.getSettings().getAdminCommand().split(" ")[0],
                addon.getSettings().getAdminCommand().split(" "));
    }

    @Override
    public void setup() {
        setPermission("admin.abc");
        setOnlyPlayer(false);
        setParametersHelp("abc.commands.admin.help.parameters");
        setDescription("abc.commands.admin.help.description");

    }

    @Override
    public boolean canExecute(User user, String label, List<String> args) {
        if (!args.isEmpty()) {
            showHelp(this, user);
            return false;
        }
        return true;
    }
    /* (non-Javadoc)
     * @see world.bentobox.bentobox.api.commands.BentoBoxCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)
     */
    @Override
    public boolean execute(User user, String label, List<String> args) {
        // Create QR code elements
        Code qrCode = new Code();
        // TODO
        String nonce = UUID.randomUUID().toString();
        // Make the QR code data
        String data = user.getUniqueId().toString() + ":" + nonce;
        // Make the map
        MapView map = Bukkit.createMap(this.getWorld());
        map.getRenderers().clear();
        map.setCenterX(64);
        map.setCenterZ(64);
        map.addRenderer(new QRCodeGenerator(getAddon(), "Server", data));
        ItemStack item = new ItemStack(Material.FILLED_MAP);
        ItemMeta m = item.getItemMeta();
        MapMeta meta = (MapMeta) m;
        meta.setMapView(map);
        item.setItemMeta(meta);
        m.setDisplayName("Server QR Code");
        m.setLore(Collections.singletonList("Scan with BentoBox App"));
        item.setItemMeta(m);
        user.getInventory().addItem(item);
        return true;
    }

}
