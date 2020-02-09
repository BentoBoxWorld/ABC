package world.bentobox.abc.qr;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
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
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import world.bentobox.abc.ABC;
import world.bentobox.bentobox.api.addons.Addon;


/**
 * A QR code generator for Minecraft maps
 * @author tastybento
 *
 */
public class QRCodeGenerator extends MapRenderer {

    private static final String WEB_SITE = "https://www.wasteofplastic.com/welcome.php?code=";
    private boolean isRendered;
    private BitMatrix bitMatrix;
    private ABC addon;
    private String value;
    private Player viewingPlayer;

    /**
     * Generate a QR code map renderer. Text is encoded into a QR code in a "prefix:value" format
     * @param addon2 - addon
     * @param prefix - what should go before the colon
     * @param value - what goes after the colon
     */
    public QRCodeGenerator(Addon addon2, String value) {
        this.addon = (ABC)addon2;
        this.value = value;
    }

    @Override
    public void render(MapView map, MapCanvas canvas, Player player) {

        // Only render once otherwise this will run every tick
        if (viewingPlayer == null || !viewingPlayer.equals(player)) {
            this.viewingPlayer = player;
            this.isRendered = true;
            // convert @p etc to player name
            String toCode = value.replace("@p", player.getName());
            toCode = WEB_SITE + Base64.getUrlEncoder().encodeToString(toCode.getBytes());
            try {
                QRCodeWriter qrCodeWriter = new QRCodeWriter();
                // Map size is 128 x 128 max
                bitMatrix = qrCodeWriter.encode(toCode, BarcodeFormat.QR_CODE, 128, 128);
                // Write the code to the file system. Debug only.
                File qr = new File(addon.getDataFolder(), "qr.png");
                MatrixToImageWriter.writeToPath(bitMatrix, "PNG", qr.toPath());
            } catch (IOException | WriterException e) {
                addon.logError("QR code generation error: " + e.getMessage());
                return;
            }
            map.setScale(Scale.NORMAL);
            for (int i = 0; i < bitMatrix.getWidth(); i++) {
                for (int j = 0; j < bitMatrix.getHeight(); j++) {
                    @SuppressWarnings("deprecation")
                    byte color = bitMatrix.get(i, j) ? MapPalette.matchColor(Color.BLACK) : MapPalette.WHITE;
                    canvas.setPixel(i, j, color);
                }
            }
            // Put a title on it
            // TODO: localize
            String title = "Scan QR code";
            int x = (128 - MinecraftFont.Font.getWidth(title)) / 2;
            canvas.drawText(x, 5, MinecraftFont.Font, title);
        }
    }

}
