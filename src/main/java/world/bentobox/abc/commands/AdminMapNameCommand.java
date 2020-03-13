package world.bentobox.abc.commands;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;

import world.bentobox.abc.qr.QRCodeGenerator;
import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;

/**
 * Admin command to make QR codes
 * @author tastybento
 *
 */
public class AdminMapNameCommand extends CompositeCommand {

    private Optional<ItemStack> opMap;

    public AdminMapNameCommand(Addon addon, CompositeCommand parent) {
        super(addon, parent, "name");
    }

    @Override
    public void setup() {
        setPermission("admin.abc.map.name");
        setOnlyPlayer(true);
        setParametersHelp("abc.commands.admin.map.name.parameters");
        setDescription("abc.commands.admin.map.name.description");
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean canExecute(User user, String label, List<String> args) {
        if (args.isEmpty()) {
            showHelp(this, user);
            return false;
        }
        // Check player is looking at a map
        opMap = getMapFromItemFrame(user);
        if (!opMap.map(m -> Bukkit.getMap(m.getDurability()))
                .filter(m -> !m.getRenderers().isEmpty())
                .filter(m -> m.getRenderers().get(0) instanceof QRCodeGenerator).
                isPresent()) {
            user.sendMessage("abc.commands.admin.map.name.look-at-map");
            return false;
        }
        return true;
    }

    private Optional<ItemStack> getMapFromItemFrame(User user) {
        return user.getPlayer().getLineOfSight(null, 10).stream().filter(b -> b.getType().equals(Material.ITEM_FRAME))
                .findFirst()
                .map(b -> (ItemFrame)b.getBlockData())
                .filter(f -> f.getItem() != null && f.getItem().getType().equals(Material.FILLED_MAP))
                .map(f -> f.getItem());
    }

    /* (non-Javadoc)
     * @see world.bentobox.bentobox.api.commands.BentoBoxCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)
     */
    @Override
    public boolean execute(User user, String label, List<String> args) {
        opMap.ifPresent(map -> map.getItemMeta().setDisplayName(String.join(" ", args.stream().skip(1).collect(Collectors.toList()))));
        user.sendMessage("abc.commands.admin.map.name.name-set");
        return true;
    }

}
