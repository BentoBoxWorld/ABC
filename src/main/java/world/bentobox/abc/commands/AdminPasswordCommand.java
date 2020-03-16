package world.bentobox.abc.commands;

import java.util.List;

import world.bentobox.abc.ABC;
import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;

/**
 * Admin command to set the Bento Cash admin password for payments
 * @author tastybento
 *
 */
public class AdminPasswordCommand extends CompositeCommand {

    public AdminPasswordCommand(Addon addon, CompositeCommand adminCommand) {
        super(addon, adminCommand, "password");
    }

    @Override
    public void setup() {
        setPermission("admin.abc.password");
        setOnlyPlayer(false);
        setDescription("abc.commands.admin.password.description");
    }

    @Override
    public boolean canExecute(User user, String label, List<String> args) {
        if (args.size() != 1) {
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
        ((ABC)getAddon()).getSettings().setAdminPassword(args.get(0));
        user.sendMessage("abc.commands.admin.password.success");
        return true;
    }

}
