package world.bentobox.abc.commands;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.math.NumberUtils;
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


    private Long price;

    public AdminCommand(ABC addon, CompositeCommand adminCommand) {
        super(addon, adminCommand,
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
        if (args.isEmpty()) {
            showHelp(this, user);
            return false;
        }
        // First argument must be a price
        if (!NumberUtils.isDigits(args.get(0))) {
            user.sendMessage("abc.commands.admin.error.no-price");
            return false;
        }
        try {
            price = Long.valueOf(args.get(0));
        } catch (Exception e) {
            user.sendMessage("abc.commands.admin.error.not-integer");
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see world.bentobox.bentobox.api.commands.BentoBoxCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)
     */
    @Override
    public boolean execute(User user, String label, List<String> args) {
        // Command is abc [cost] [command]
        // Create QR code elements
        Code qrCode = new Code();
        qrCode.setAmount(price);
        // Create command
        qrCode.setCommand(String.join(" ", args.stream().skip(1).collect(Collectors.toList())));
        qrCode.setId(((ABC)getAddon()).getSettings().getAdminUUID());
        qrCode.setPayTo(((ABC)getAddon()).getSettings().getServerName());

        // Make the map
        MapView map = Bukkit.createMap(this.getWorld());
        map.getRenderers().clear();
        map.setCenterX(64);
        map.setCenterZ(64);
        map.addRenderer(new QRCodeGenerator(getAddon(), qrCode));
        ItemStack item = new ItemStack(Material.FILLED_MAP);
        ItemMeta m = item.getItemMeta();
        MapMeta meta = (MapMeta) m;
        meta.setMapView(map);
        item.setItemMeta(meta);
        m.setDisplayName("Payment QR Code");
        m.setLore(Collections.singletonList("Scan with phone to pay"));
        item.setItemMeta(m);
        user.getInventory().addItem(item);
        // Store the map for future reference
        qrCode.setUniqueId(String.valueOf(map.getId()));
        ((ABC)getAddon()).saveMap(qrCode);
        return true;
    }

}
