package fp.onion;

import javafx.scene.control.TextFormatter;
import javafx.util.StringConverter;

import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

//credit where credit is due:
//https://stackoverflow.com/questions/45977390/how-to-force-a-double-input-in-a-textfield-in-javafx
public class DoubleTextFilter {

    static private Pattern validEditingState = Pattern.compile("-?(([1-9][0-9]*)|0)?(\\.[0-9]*)?");
    static public UnaryOperator<TextFormatter.Change> filter= c -> {
        String text = c.getControlNewText();
        if (validEditingState.matcher(text).matches()) {
            return c;
        } else {
            return null;
        }
    };
    static public StringConverter<Double> converter= new StringConverter<Double>() {

        @Override
        public Double fromString(String s) {
            if (s.isEmpty() || "-".equals(s) || ".".equals(s) || "-.".equals(s)) {
                return 0.0;
            } else {
                return Double.valueOf(s);
            }
        }

        @Override
        public String toString(Double d) {
            return d.toString();
        }
    };
}