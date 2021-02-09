package main.components;

import main.core.Core;
import main.core.Tutorial;
import main.util.MyUtils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;

public class ZoomPanHandler {
    private final Point2D.Double safetyPoint = new Point2D.Double(0, 0);
    public Point dragMouseStartLocation = null;
    public Point dragComponentStartLocation = null;
    // 1.0 is zoomed so text is normal size.
    // 0.5 is zoomed out.
    private float currentZoom = 1.0f;
    private int zoomLevel;
    private Core core;

    public float getCurrentZoom() {
        return currentZoom;
    }

    public ZoomPanHandler(Core core) {
        this.core = core;
        MyUtils.log(core, "Loading Zoom manager... ");
        core.getWorld().setLocation(core.settingsManager.loadPan());
        MyUtils.log(core, "Loaded pan " + core.getWorld().getLocation());
        zoomLevel = core.settingsManager.loadScaleFromFile();
        MyUtils.log(core, "Loaded scale " + zoomLevel);
        updateZoom();
    }

    private void updateZoom() {
        currentZoom = (float) Math.pow(0.5, zoomLevel);
    }

    public void mouseEvent(MouseEvent e) {
        int eventID = e.getID();

        if (eventID == MouseEvent.MOUSE_PRESSED) {
            if (e.getButton() == 2) {
                dragMouseStartLocation = e.getLocationOnScreen();
                dragComponentStartLocation = core.getWorld().getLocation();
            }
        } else if (eventID == MouseEvent.MOUSE_RELEASED) {
            if (e.getButton() == 2) {
                dragMouseStartLocation = null;
                dragComponentStartLocation = null;
            }
        } else if (eventID == MouseEvent.MOUSE_DRAGGED) {
            if (dragComponentStartLocation != null && dragMouseStartLocation != null) {
                int dX = dragMouseStartLocation.x - e.getXOnScreen();
                int dY = dragMouseStartLocation.y - e.getYOnScreen();
                Point loc = new Point(dragComponentStartLocation.x - dX, dragComponentStartLocation.y - dY);
                core.getWorld().setLocation(loc);
                core.settingsManager.savePan(loc);
                Tutorial.doStep3(core);
                MyUtils.log(core, "Just panned: New world location: " + core.getWorld().getLocation());
            }
        } else if (eventID == MouseEvent.MOUSE_WHEEL) {
            if (((MouseWheelEvent) e).getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
                scrolled(((MouseWheelEvent) e).getWheelRotation());
            }

        }
        core.overlay.mouseMoved(e);
    }

    public void scrolled(int distance) {
        if (core.globalListeners.isKeyPressed(KeyEvent.VK_CONTROL)) {
            zoomIn(distance);
        } else {
            int x = core.getWorld().getLocation().x;
            int y = core.getWorld().getLocation().y;
            core.getWorld().setLocation(new Point(x, y - 60 * distance));
        }
    }

    public Point2D.Double getSafetyPoint() {
        return safetyPoint;
    }

    private void zoomIn(int distance) {
        MyUtils.log(core, "Zoom " + distance + " detected... isLoading:" + core.isLoading() + " canPerformActions:" + core.editorWindowManager.canPerformActions());
        if (!core.editorWindowManager.canPerformActions()) return;
        if (core.isLoading()) return;

        float oldZoom = currentZoom;
        zoomLevel += distance;
        if (zoomLevel < 0) zoomLevel = 0;
        if (zoomLevel > 7) zoomLevel = 7;
        core.settingsManager.saveScale(zoomLevel);
        updateZoom();
        if (oldZoom == currentZoom) return;
        Tutorial.doStep4(core);

        Point zoomLocation = core.getWorld().getLocation();
        zoomLocation.x *= -1;
        zoomLocation.y *= -1;

        Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
        zoomLocation.x += mouseLocation.x - core.background.getLocationOnScreen().x;
        zoomLocation.y += mouseLocation.y - core.background.getLocationOnScreen().y;

        core.editorWindowManager.allToStills();

        Point2D.Double newSafetyPointLocation = calculateNewLoc(safetyPoint, zoomLocation, oldZoom);
        safetyPoint.setLocation(newSafetyPointLocation);

        for (var editorWindow : core.editorWindowManager.getWindows()) {
            Point2D.Double newWindowLocation = calculateNewLoc(editorWindow.getTrueLocation(), zoomLocation, oldZoom);
            editorWindow.setLocation(newWindowLocation.x, newWindowLocation.y);
            editorWindow.zoomHasUpdated();
            MyUtils.log(core, "Zoom: Set editor window to " + newWindowLocation);
        }
        MyUtils.log(core, "World location: " + core.getWorld().getLocation());
        core.getWorld().invalidate();
        core.getWorld().setLocation(core.getWorld().getLocation());
        core.background.repaint();
        core.editorWindowManager.updateCurrentWithHovered();
    }

    private Point2D.Double calculateNewLoc(Point2D.Double locIn, Point zoomLocation, float oldZoom) {
        Point2D.Double diff = new Point2D.Double();
        diff.x = locIn.x - zoomLocation.x;
        diff.y = locIn.y - zoomLocation.y;
        diff.x *= currentZoom / oldZoom;
        diff.y *= currentZoom / oldZoom;
        return new Point2D.Double(diff.x + zoomLocation.x, diff.y + zoomLocation.y);
    }
}
