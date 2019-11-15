package fp.onion;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.nio.file.Path;

public class ScriptBuilder {

    static private StringBuilder script;

    static public String getChromaScript(String image, Color key, double delta) {
        script = new StringBuilder("chromaKey(").append( image ).append(", ")
            .append( (int) (key.getRed() * 255) ).append(", ")
            .append( (int) (key.getGreen() * 255) ).append(", ")
            .append( (int) (key.getBlue() * 255) ).append(", ")
            .append( delta ).append(")");

        return script.toString();
    }

    static public String getTransparencyScript(String image, double intensity) {
        script = new StringBuilder( "transparency(" ).append(image).append(", ").append(intensity).append(")");
        return script.toString();
    }

    static public String getImage( String image ) {
        script = new StringBuilder(image);
        return script.toString();
    }
}
