package world.bentobox.abc.commands;

import java.util.List;

import world.bentobox.abc.ABC;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;

/**
 * User command. By default shows the balance
 * @author tastybento
 *
 */
public class UserCommand extends CompositeCommand {

    public UserCommand(ABC addon, CompositeCommand userCommand) {
        super(addon, userCommand,
                addon.getSettings().getIslandCommand().split(" ")[0],
                addon.getSettings().getIslandCommand().split(" "));
    }

    @Override
    public void setup() {
        setPermission("abc.player");
        setOnlyPlayer(false);
        setParametersHelp("abc.commands.player.help.parameters");
        setDescription("abc.commands.player.help.description");

    }

    @Override
    public boolean canExecute(User user, String label, List<String> args) {
        return true;
    }

    /* (non-Javadoc)
     * @see world.bentobox.bentobox.api.commands.BentoBoxCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)
     */
    @Override
    public boolean execute(User user, String label, List<String> args) {
        return true;
    }

}
