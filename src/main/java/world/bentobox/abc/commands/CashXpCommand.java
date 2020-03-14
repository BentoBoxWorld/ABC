package world.bentobox.abc.commands;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.entity.Player;

import okhttp3.CacheControl;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import world.bentobox.abc.ABC;
import world.bentobox.abc.dos.PayTo;
import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.commands.ConfirmableCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;

/**
 * Command to make pay bento cash for xp
 * @author tastybento
 *
 */
public class CashXpCommand extends ConfirmableCommand {

    ABC addon;
    int percent;

    public CashXpCommand(Addon addon, CompositeCommand parent) {
        super(addon, parent, "cashxp");
        this.addon = (ABC)addon;
    }

    @Override
    public void setup() {
        setPermission("abc.cashxp");
        setOnlyPlayer(true);
        setParametersHelp("abc.commands.player.cashxp.parameters");
        setDescription("abc.commands.player.cashxp.description");
    }

    @Override
    public boolean canExecute(User user, String label, List<String> args) {
        if (args.size() != 1) {
            showHelp(this, user);
            return false;
        }
        // First argument must be a price
        if (!NumberUtils.isDigits(args.get(0))) {
            user.sendMessage("abc.commands.player.cashxp.choose-percent");
            return false;
        }
        try {
            percent = Integer.valueOf(args.get(0));
        } catch (Exception e) {
            user.sendMessage("abc.commands.player.cashxp.not-integer");
            return false;
        }
        if (percent <= 0) {
            user.sendMessage("abc.commands.player.pay.positive-amount");
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see world.bentobox.bentobox.api.commands.BentoBoxCommand#execute(world.bentobox.bentobox.api.player.player, java.lang.String, java.util.List)
     */
    @Override
    public boolean execute(User user, String label, List<String> args) {
        user.sendMessage("abc.commands.player.cashxp.convert", TextVariables.NUMBER, String.valueOf(percent));
        int rate = addon.getSettings().getExchangeRate();
        long earnings = (long)(getTotalExperience(user.getPlayer()) * ((double)percent / 100) / rate);
        if (earnings <= 0) {
            user.sendMessage("abc.commands.player.cashxp.need-xp");
            return false;
        }
        user.sendMessage("abc.commands.player.cashxp.you-will-make", TextVariables.NUMBER, String.valueOf(earnings));
        user.sendMessage("abc.commands.player.cashxp.rate", TextVariables.NUMBER, String.valueOf(rate));

        PayTo payObj = new PayTo();
        payObj.setAmount(earnings);
        payObj.setPayFrom(addon.getSettings().getAdminUUID().toString());
        payObj.setPayTo(user.getUniqueId().toString());
        payObj.setPassword(addon.getSettings().getAdminPassword());
        String payObjJson = addon.getGson().toJson(payObj);
        this.askConfirmation(user, () -> payPlayer(user, payObjJson));
        return false;
    }

    private void payPlayer(User user, String payload) {
        // Remove xp
        int oldXp = getTotalExperience(user.getPlayer());
        this.setTotalExperience(user.getPlayer(), oldXp - (int)(oldXp * ((double)percent / 100)));
        String url = "https://bento.cash/pay.php";
        RequestBody body = new FormBody.Builder()
                .add("po", Base64.getUrlEncoder().encodeToString(payload.getBytes()))
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .cacheControl(new CacheControl.Builder().noCache().build())
                .build();

        try (Response response = ((ABC)getAddon()).getHttpClient().newCall(request).execute()) {
            if (!response.isSuccessful()) {
                user.sendMessage("abc.commands.admin.pay.error", "[error]", user.getTranslation(response.body().string()));
                this.setTotalExperience(user.getPlayer(), oldXp);
                return;
            }
            // Get response body
            user.sendMessage("abc.commands.player.cashxp.success", TextVariables.NUMBER, response.body().string());

        } catch (IOException e) {
            user.sendMessage("abc.commands.admin.pay.error", "[error]", e.getMessage());
            this.setTotalExperience(user.getPlayer(), oldXp);
        }
    }

    //new Exp Math from 1.8
    private  static  int getExpAtLevel(final int level)
    {
        if (level <= 15)
        {
            return (2*level) + 7;
        }
        if (level <= 30)
        {
            return (5 * level) -38;
        }
        return (9*level)-158;

    }
    public static int getExpAtLevel(final Player player)
    {
        return getExpAtLevel(player.getLevel());
    }

    //This method is required because the bukkit player.getTotalExperience() method, shows exp that has been 'spent'.
    //Without this people would be able to use exp and then still sell it.
    public int getTotalExperience(final Player player)
    {
        int exp = Math.round(getExpAtLevel(player) * player.getExp());
        int currentLevel = player.getLevel();

        while (currentLevel > 0)
        {
            currentLevel--;
            exp += getExpAtLevel(currentLevel);
        }
        if (exp < 0)
        {
            exp = Integer.MAX_VALUE;
        }
        return exp;
    }

    //This method is used to update both the recorded total experience and displayed total experience.
    //We reset both types to prevent issues.
    private void setTotalExperience(final Player player, final int exp)
    {
        if (exp < 0)
        {
            throw new IllegalArgumentException("Experience is negative!");
        }
        player.setExp(0);
        player.setLevel(0);
        player.setTotalExperience(0);

        //This following code is technically redundant now, as bukkit now calculates levels more or less correctly
        //At larger numbers however... player.getExp(3000), only seems to give 2999, putting the below calculations off.
        int amount = exp;
        while (amount > 0)
        {
            final int expToLevel = CashXpCommand.getExpAtLevel(player);
            amount -= expToLevel;
            if (amount >= 0)
            {
                // give until next level
                player.giveExp(expToLevel);
            }
            else
            {
                // give the rest
                amount += expToLevel;
                player.giveExp(amount);
                amount = 0;
            }
        }
    }

}
