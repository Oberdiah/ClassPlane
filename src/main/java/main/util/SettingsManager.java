package main.util;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.vfs.VirtualFile;
import main.components.EditorWindow;
import main.components.World;
import main.core.Core;

import java.awt.*;
import java.awt.geom.Point2D;

public class SettingsManager {
    private final String PREFIX = "UMLPlugin ";
    private final Core core;

    public SettingsManager(Core core) {
        this.core = core;
    }
    
    private PropertiesComponent props() {
        return PropertiesComponent.getInstance(core.getProject());
    }

    public String getFilePropertyPath(VirtualFile file) {
        return PREFIX + file.getPath();
    }

    // ### Stored in other.xml
    public void setDoNotShow() {
        PropertiesComponent.getInstance().setValue(PREFIX + "showTutorial", false, true);
    }

    public boolean shouldShowTutorial() {
        return PropertiesComponent.getInstance().getBoolean(PREFIX + "showTutorial", true);
    }

    public void savePan(Point pan) {
        props().setValue(PREFIX + "panning.x", pan.x, -World.SIZE / 2);
        props().setValue(PREFIX + "panning.y", pan.y, -World.SIZE / 2);
    }

    public Point loadPan() {
        Point rv = new Point();
        rv.x = props().getInt(PREFIX + "panning.x", -World.SIZE / 2);
        rv.y = props().getInt(PREFIX + "panning.y", -World.SIZE / 2);
        return rv;
    }

    public int loadScaleFromFile() {
        return props().getInt(PREFIX + "scale", 0);
    }

    public void saveScale(int scale) {
        props().setValue(PREFIX + "scale", scale, 0);
    }

    public boolean locationIsSaved(EditorWindow editorWindow) {
        String path = getFilePropertyPath(editorWindow.getVirtualFile());
        float test = props().getFloat(path + ".y", -1f);
        return test != -1f;
    }

    public Point2D.Double loadLocationFromFile(EditorWindow editorWindow) {
        String path = getFilePropertyPath(editorWindow.getVirtualFile());
        Point2D.Double rv = new Point2D.Double();
        rv.x = props().getFloat(path + ".x", World.SIZE / 2f);
        rv.y = props().getFloat(path + ".y", World.SIZE / 2f);
        return rv;
    }

    /**
     * Saves the location of the provided editorWindow to properties.
     *
     * @param editorWindow The window to save location of.
     */
    public void saveLocationToFile(EditorWindow editorWindow) {
        String path = getFilePropertyPath(editorWindow.getVirtualFile());
        Point2D.Double location = editorWindow.getTrueLocation();
        props().setValue(path + ".x", (float) location.x, World.SIZE / 2f);
        props().setValue(path + ".y", (float) location.y, World.SIZE / 2f);
    }
}
