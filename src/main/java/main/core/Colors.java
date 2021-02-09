package main.core;

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class Colors {
    public static @NotNull EditorColorsScheme getColorScheme() {
        return EditorColorsManager.getInstance().getGlobalScheme();
    }

    public static Color background() {
        return getColorScheme().getColor(EditorColors.GUTTER_BACKGROUND);
    }

    public static Color text() {
        return Colors.getColorScheme().getAttributes(DefaultLanguageHighlighterColors.CLASS_NAME).getForegroundColor();
    }

    public static Color circleGettingDragged() {
        return new Color(83, 83, 83, 255);
    }

    public static Color circle() {
        return new Color(158, 227, 164, 255);
    }

    public static Color circleHovered() {
        return new Color(88, 184, 98, 255);
    }

    public static Color arrowIcon() {
        return Color.WHITE;
    }

    public static Color arrow() {
        return JBColor.BLACK;
    }

    public static Color dragHandle() {
        return new Color(150, 150, 150, 255);
    }

    public static Color dragHandleEditor() {
        return new Color(255, 200, 0);
    }

    public static Color borderUnselected() {
        return new Color(120, 120, 120);
    }

    public static Color borderSelected() {
        return new Color(33, 142, 130);
    }

    public static Color windowHoveredOverlay() {
        return new Color(0, 0, 0, 30);
    }

    public static Color windowSelectedOverlay() {
        return new Color(33, 142, 130, 100);
    }

    public static Color windowDraggedOntoOverlay() {
        return new Color(200, 100, 0, 30);
    }
}
