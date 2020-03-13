package world.bentobox.abc.commands;

import java.io.IOException;
import java.util.List;

import org.bukkit.Bukkit;

import okhttp3.CacheControl;
import okhttp3.Request;
import okhttp3.Response;
import world.bentobox.abc.ABC;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
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
        setOnlyPlayer(true);
        setDescription("abc.commands.player.help.description");
        new UserPaymeCommand(getAddon(), this, false);
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
        user.sendMessage("abc.commands.player.checking");
        Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> getBalance(user));
        return true;
    }

    private void getBalance(User user) {
        Request request = new Request.Builder()
                .url("https://bento.cash/balance.php?u=" + user.getUniqueId().toString())
                .addHeader("User-Agent", "ABC Addon")
                .cacheControl(new CacheControl.Builder().noCache().build())
                .build();

        try (Response response = ((ABC)getAddon()).getHttpClient().newCall(request).execute()) {

            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            // Get response body
            user.sendMessage("abc.commands.player.balance", TextVariables.NUMBER, response.body().string());
        } catch (IOException e) {
            user.sendMessage("abc.commands.player.error");
            getAddon().logError(e.getMessage());
        }
    }
}
