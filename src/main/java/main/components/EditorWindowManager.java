package main.components;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiJavaFile;
import main.core.Core;
import main.util.MyUtils;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EditorWindowManager {
    private final ArrayList<EditorWindow> editorWindows = new ArrayList<>();
    public EditorWindow dragHovered = null; // If we're dragging, hovering doesn't work. DragHovered fixes that.
    // When zoomed out, currentEditor is null, but hoveredOver continues to update as normal.
    private EditorWindow currentEditor = null; // The current IntelliJ editor
    private EditorWindow selectedEditor = null; // The currently selected editor (Green outline)
    private EditorWindow hoveredOver = null; // The one we're currently hovering over

    public List<EditorWindow> getWindows() {
        return Collections.unmodifiableList(editorWindows);
    }

    private final Core core;
    public EditorWindowManager(Core core) {
        this.core = core;
    }

    public Point2D.Double averageWindowLocation() {
        if (editorWindows.size() == 0) {
            return new Point2D.Double(World.SIZE / 2f, World.SIZE / 2f);
        }
        Point2D.Double outP = new Point2D.Double();

        for (var w : editorWindows) {
            outP.x += w.getTrueLocation().x;
            outP.y += w.getTrueLocation().y;
        }
        outP.x /= editorWindows.size();
        outP.y /= editorWindows.size();
        return outP;
    }

    public void changeSelectionTo(EditorWindow window) {
        mouseClickedInWindow(window);
    }

    public void dispose() {
        for (var window : editorWindows) {
            window.dispose();
        }
        editorWindows.clear();
    }

    public EditorWindow createEditor(PsiJavaFile psiFile) {
        EditorWindow editorWindow = new EditorWindow(psiFile, core);

        if (core.settingsManager.locationIsSaved(editorWindow)) {
            Point2D.Double p = core.settingsManager.loadLocationFromFile(editorWindow);
            editorWindow.setLocation(p.x, p.y);
        }

        editorWindow.setLocation(World.SIZE / 2f, World.SIZE / 2f);
        editorWindows.add(editorWindow);
        return editorWindow;
    }

    public void delete(EditorWindow window) {
        ApplicationManager.getApplication().runWriteAction(() -> {
            editorWindows.remove(window);
            window.dispose();
            selectedEditor = null;
            hoveredOver = null;
            allToStills();
            core.background.repaint();
        });
    }

    public boolean canPerformActions() {
        return editorWindows.stream().noneMatch((window) -> window.processing);
    }

    public void loadingHasFinished() {
        naturalOrdering();
    }

    public EditorWindow getSelectedEditor() {
        return selectedEditor;
    }

    public boolean isHovered(EditorWindow window) {
        return hoveredOver == window;
    }

    public void updateCurrentWithHovered() {
        if (hoveredOver == null) return;
        mouseMovedInWindow(hoveredOver);
    }

    public void mouseClickedOnBackground() {
        if (selectedEditor == null) return;

        selectedEditor.isSelected = false;
        selectedEditor.repaint();
        selectedEditor = null;
    }

    public void mouseClickedInWindow(EditorWindow window) {
        if (selectedEditor == window) return;
        if (selectedEditor != null) {
            selectedEditor.isSelected = false;
            selectedEditor.repaint();
        }

        selectedEditor = window;
        window.isSelected = true;
        window.repaint();
        pullToTop(selectedEditor);
    }

    public void mouseExited(EditorWindow window) {
        if (dragHovered == window) {
            window.repaint();
            dragHovered = null;
        }
    }

    public void mouseEntered(EditorWindow window) {
        if (dragHovered != window) {
            if (dragHovered != null) {
                dragHovered.repaint();
            }
            dragHovered = window;
            window.repaint();
        }
    }

    /**
     * @param window The window the mouse moved within.
     */
    public void mouseMovedInWindow(EditorWindow window) {
        if (hoveredOver != window) {
            if (hoveredOver != null) {
                hoveredOver.repaint();
            }
            hoveredOver = window;
            window.repaint();
        }
        if (core.zoomPanHandler.getCurrentZoom() != 1.0) return;

        if (window != currentEditor) {
            if (currentEditor != null) {
                currentEditor.toStill();
            }

            currentEditor = window;
            currentEditor.toEditor();

            pullToTop(currentEditor);
        }
    }

    public void allToStills() {
        MyUtils.log(core, "All to stills...");
        for (var window : editorWindows) {
            if (window.isEditor()) {
                window.toStill();
            }
        }
        currentEditor = null;
    }

    public void mouseMovedOnBackground() {
        if (hoveredOver != null) {
            hoveredOver.repaint();
        }
        hoveredOver = null;
        if (currentEditor != null && currentEditor.isEditor()) {
            currentEditor.toStill();
            currentEditor = null;
        }
    }

    public void pullToTop(EditorWindow window) {
        // No need if we're already at the top of the stack.
        if (editorWindows.get(editorWindows.size() - 1) == window) return;

        editorWindows.remove(window);
        editorWindows.add(window);
        naturalOrdering();
    }

    private void naturalOrdering() {
        int layer = 0;
        for (EditorWindow window : getWindows()) {
            layer++;
            core.background.setWindowLayer(window, layer);
        }
    }
}
