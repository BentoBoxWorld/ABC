package world.bentobox.abc.qr;

import java.awt.Color;
import java.util.Base64;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.map.MapView.Scale;
import org.bukkit.map.MinecraftFont;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import world.bentobox.abc.ABC;
import world.bentobox.abc.dos.Code;
import world.bentobox.bentobox.api.addons.Addon;


/**
 * A QR code generator for Minecraft maps
 * @author tastybento
 *
 */
public class QRCodeGenerator extends MapRenderer {

    private static final String WEB_SITE = "https://www.wasteofplastic.com/welcome.php?code=";
    private BitMatrix bitMatrix;
    private ABC addon;
    private Code code;
    private Player viewingPlayer;

    /**
     * Generate a QR code map renderer.
     * @param addon2 - addon
     * @param code - the QR code to render
     */
    public QRCodeGenerator(Addon addon2, Code code) {
        this.addon = (ABC)addon2;
        this.code = code;
    }

    @Override
    public void render(MapView map, MapCanvas canvas, Player player) {
        // Only render once per player otherwise this will run every tick
        if (viewingPlayer == null || !viewingPlayer.equals(player)) {
            this.viewingPlayer = player;
            // Make a copy of the code and customize it
            Code toCodeCode = new Code(code);
            // convert @p etc to player name in code's command
            toCodeCode.setCommand(toCodeCode.getCommand().replace("@p", player.getName()));
            // Insert payer
            toCodeCode.setFbo(player.getUniqueId());
            // Sign code
            toCodeCode.setHash(addon.getCrypto().sign(toCodeCode.toString()));
            // Make the payload
            String data = addon.getGson().toJson(toCodeCode);
            // Put it into a URL
            String toCode = WEB_SITE + Base64.getUrlEncoder().encodeToString(data.getBytes());
            // Create the QR code
            try {
                QRCodeWriter qrCodeWriter = new QRCodeWriter();
                // Map size is 128 x 128 max
                bitMatrix = qrCodeWriter.encode(toCode, BarcodeFormat.QR_CODE, 128, 128);
            } catch (WriterException e) {
                addon.logError("QR code generation error: " + e.getMessage());
                return;
            }
            map.setScale(Scale.NORMAL);
            // Render
            for (int i = 0; i < bitMatrix.getWidth(); i++) {
                for (int j = 0; j < bitMatrix.getHeight(); j++) {
                    @SuppressWarnings("deprecation")
                    byte color = bitMatrix.get(i, j) ? MapPalette.matchColor(Color.BLACK) : MapPalette.WHITE;
                    canvas.setPixel(i, j, color);
                }
            }
            // Put a title on it
            // TODO: customize
            String title = "Scan QR code";
            int x = (128 - MinecraftFont.Font.getWidth(title)) / 2;
            canvas.drawText(x, 5, MinecraftFont.Font, title);
        }
    }

}
