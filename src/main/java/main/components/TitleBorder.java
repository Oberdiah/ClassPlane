package main.components;

import com.intellij.util.ui.JBUI;
import main.core.Colors;
import main.core.Core;

import javax.swing.border.Border;
import java.awt.*;

public class TitleBorder implements Border {
    private final EditorWindow window;

    private final Core core;
    public TitleBorder(EditorWindow window, Core core) {
        this.core = core;
        this.window = window;
    }

    public int getDragHandleHeight() {
        return (int) (20 * core.zoomPanHandler.getCurrentZoom());
    }

    public int getCoreBorderWidth() {
        return (int) (5 * core.zoomPanHandler.getCurrentZoom());
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        if (window.isSelected) {
            g.setColor(Colors.borderSelected());
        } else {
            g.setColor(Colors.borderUnselected());
        }
        Stroke oldStroke = ((Graphics2D) g).getStroke();
        ((Graphics2D) g).setStroke(new BasicStroke(getCoreBorderWidth() * 2));
        g.drawRect(x, y + getDragHandleHeight(), width - 1, height - 1 - getDragHandleHeight());
        ((Graphics2D) g).setStroke(oldStroke);

        if (window.isEditor()) {
            g.setColor(Colors.dragHandleEditor());
        } else {
            g.setColor(Colors.dragHandle());
        }
        g.fillRect(x, y, width, getDragHandleHeight());
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return JBUI.insets(
                getDragHandleHeight() + getCoreBorderWidth(),
                getCoreBorderWidth(),
                getCoreBorderWidth(),
                getCoreBorderWidth()
        );
    }

    @Override
    public boolean isBorderOpaque() {
        return false;
    }
}
