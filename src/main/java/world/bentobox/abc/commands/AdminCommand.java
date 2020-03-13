package world.bentobox.abc.commands;

import java.util.List;

import world.bentobox.abc.ABC;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;

/**
 * Admin command to make QR codes
 * @author tastybento
 *
 */
public class AdminCommand extends CompositeCommand {

    public AdminCommand(ABC addon, CompositeCommand adminCommand) {
        super(addon, adminCommand,
                addon.getSettings().getAdminCommand().split(" ")[0],
                addon.getSettings().getAdminCommand().split(" "));
    }

    @Override
    public void setup() {
        setPermission("admin.abc");
        setOnlyPlayer(false);
        setDescription("abc.commands.admin.help.description");
        new AdminPay(this.getAddon(), this);
        new AdminMapCommand(this.getAddon(), this);
    }

    @Override
    public boolean canExecute(User user, String label, List<String> args) {
        showHelp(this, user);
        return false;
    }

    /* (non-Javadoc)
     * @see world.bentobox.bentobox.api.commands.BentoBoxCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)
     */
    @Override
    public boolean execute(User user, String label, List<String> args) {
        return false;
    }

}
