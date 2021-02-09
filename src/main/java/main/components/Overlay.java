package main.components;

import main.core.Colors;
import main.core.Core;
import main.core.Tutorial;
import main.util.MyUtils;
import main.util.PsiModifier;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class Overlay extends JComponent {
    // Relative to the screen
    public final int diam = 50;
    private final Point mouseLoc = new Point();
    public Point overlayCircleLocation = null;
    public boolean circleHovered = false;
    public boolean isDragging = false;

    private final Core core;
    public Overlay(Core core) {
        this.core = core;
        setOpaque(false);
        setFocusable(false);
    }

    public void mouseMoved(MouseEvent e) {
        int eventID = e.getID();
        if (overlayCircleLocation == null || core.editorWindowManager.getSelectedEditor() == null) return;

        if (eventID == MouseEvent.MOUSE_MOVED) {
            circleHovered = e.getLocationOnScreen().distance(overlayCircleLocation) < diam / 2f;
        } else if (eventID == MouseEvent.MOUSE_PRESSED) {
            if (e.getButton() == 1 && circleHovered) {
                isDragging = true;
                mouseLoc.setLocation(overlayCircleLocation);
            }
        } else if (eventID == MouseEvent.MOUSE_RELEASED) {
            if (e.getButton() == 1) {
                mouseLoc.setLocation(overlayCircleLocation);
                core.background.repaint();

                if (isDragging &&
                        core.editorWindowManager.dragHovered != null &&
                        core.editorWindowManager.getSelectedEditor() != null &&
                        core.editorWindowManager.dragHovered != core.editorWindowManager.getSelectedEditor()) {
                    PsiModifier.toggleExtends(core.editorWindowManager.getSelectedEditor(), core.editorWindowManager.dragHovered, core);
                    Tutorial.doStep6(core);
                }
                isDragging = false;
            }
        } else if (eventID == MouseEvent.MOUSE_DRAGGED) {
            if (isDragging) {
                mouseLoc.setLocation(e.getLocationOnScreen());
                repaint();
            }
        }
    }

    private Point getLocalLocation(Point p) {
        return new Point(p.x - getLocationOnScreen().x, p.y - getLocationOnScreen().y);
    }

    @Override
    protected void paintComponent(Graphics g) {
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (core.isLoading()) {
            g.setColor(Colors.background().darker());
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Colors.text());
            g.drawString(core.loadingString, getWidth() / 2, getHeight() / 2);
            setOpaque(true);
            setFocusable(true);
        } else {
            setOpaque(false);
            setFocusable(false);
        }


        if (core.zoomPanHandler.getCurrentZoom() == 1.0) overlayCircleLocation = null;
        if (overlayCircleLocation != null && core.editorWindowManager.getSelectedEditor() != null) {
            int packing = 10;
            int arrowOffset = (diam - packing * 2) / 2;

            var p = getLocalLocation(overlayCircleLocation);
            if (circleHovered) {
                if (isDragging) {
                    g.setColor(Colors.circleGettingDragged());
                } else {
                    g.setColor(Colors.circle());
                }
            } else {
                g.setColor(Colors.circleHovered());
            }
            MyUtils.fillCircle(g, p.x, p.y, diam);
            if (!isDragging) {
                g.setColor(Colors.arrowIcon());
                MyUtils.drawArrowLine(g, p.x - arrowOffset, p.y + arrowOffset, p.x + arrowOffset, p.y - arrowOffset);
            }
        }

        if (isDragging && overlayCircleLocation != null) {
            var p = getLocalLocation(overlayCircleLocation);
            var q = getLocalLocation(mouseLoc);
            g.setColor(Colors.arrow());
            MyUtils.drawArrowLine(g, p.x, p.y, q.x, q.y);
        }

        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    @Override
    public boolean contains(int x, int y) {
        return false;
    }
}
