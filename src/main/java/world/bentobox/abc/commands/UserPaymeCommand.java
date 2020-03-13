package world.bentobox.abc.commands;

import java.util.Arrays;
import java.util.List;

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
import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;

/**
 * Command to make pay me QR code maps
 * @author tastybento
 *
 */
public class UserPaymeCommand extends CompositeCommand {

    private Long price;
    private boolean admin;

    public UserPaymeCommand(Addon addon, CompositeCommand parent, boolean admin) {
        super(addon, parent, "payme");
        this.admin = admin;
    }

    @Override
    public void setup() {
        setPermission("abc.payme");
        setOnlyPlayer(true);
        setParametersHelp("abc.commands.user.payme.parameters");
        setDescription("abc.commands.user.payme.description");
    }

    @Override
    public boolean canExecute(User user, String label, List<String> args) {
        if (args.size() != 1) {
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
        // Command is payme [cost]
        // Create QR code elements
        Code qrCode = new Code();
        qrCode.setAmount(price);
        qrCode.setId(user.getUniqueId().toString());
        qrCode.setPayTo(user.getName());

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
        m.setDisplayName("Pay " + user.getName() + " ABC$" + price);
        m.setLore(Arrays.asList("Scan with phone to pay", user.getName(), "ABC$" + price));
        item.setItemMeta(m);
        user.getInventory().addItem(item);
        // Store the map for future reference
        qrCode.setUniqueId(String.valueOf(map.getId()));
        ((ABC)getAddon()).saveMap(qrCode);
        return true;
    }

}
