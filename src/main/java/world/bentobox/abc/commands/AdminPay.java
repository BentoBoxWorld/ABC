package world.bentobox.abc.commands;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.math.NumberUtils;
import org.eclipse.jdt.annotation.Nullable;

import okhttp3.CacheControl;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import world.bentobox.abc.ABC;
import world.bentobox.abc.dos.PayTo;
import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;

/**
 * @author tastybento
 *
 */
public class AdminPay extends CompositeCommand {

    private ABC addon;
    private long amount;
    private @Nullable UUID targetUUID;

    public AdminPay(Addon addon, CompositeCommand parent) {
        super(addon, parent, "pay");
        this.addon = (ABC)addon;
    }

    /* (non-Javadoc)
     * @see world.bentobox.bentobox.api.commands.BentoBoxCommand#setup()
     */
    @Override
    public void setup() {
        setPermission("admin.abc.pay");
        setOnlyPlayer(false);
        setParametersHelp("abc.commands.admin.pay.parameters");
        setDescription("abc.commands.admin.pay.description");
    }

    @Override
    public boolean canExecute(User user, String label, List<String> args) {
        if (args.size() != 3) {
            this.showHelp(this, user);
            return false;
        }
        // Get target player
        targetUUID = getPlayers().getUUID(args.get(0));
        if (targetUUID == null) {
            user.sendMessage("general.errors.unknown-player", TextVariables.NAME, args.get(0));
            return false;
        }
        // Player cannot ban themselves
        if (targetUUID.equals(addon.getSettings().getAdminUUID())) {
            user.sendMessage("abc.commands.admin.pay.cannot-pay-yourself");
            return false;
        }
        // Check amount
        amount = 0;
        if (NumberUtils.isDigits(args.get(1))) {
            try {
                amount = Long.valueOf(args.get(1));
            } catch (Exception e) {
                this.showHelp(this, user);
                return false;

            }
        }
        if (amount <= 0) {
            user.sendMessage("abc.commands.admin.pay.greater-than-zero");
            return false;
        }
        // Password can be anything
        return true;
    }


    /* (non-Javadoc)
     * @see world.bentobox.bentobox.api.commands.BentoBoxCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)
     */
    @Override
    public boolean execute(User user, String label, List<String> args) {
        // Admin pay <player> <amount> <password>
        PayTo payObj = new PayTo();
        payObj.setAmount(amount);
        payObj.setPayFrom(addon.getSettings().getAdminUUID().toString());
        payObj.setPayTo(targetUUID.toString());
        payObj.setPassword(args.get(2));
        String payObjJson = addon.getGson().toJson(payObj);
        payPlayer(user, payObjJson);
        return false;
    }

    private void payPlayer(User user, String payload) {
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
                return;
            }
            // Get response body
            user.sendMessage("abc.commands.admin.pay.success", TextVariables.NUMBER, response.body().string());
        } catch (IOException e) {
            user.sendMessage("abc.commands.admin.pay.error", "[error]", e.getMessage());
        }
    }
}
