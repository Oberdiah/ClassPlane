package main.core;

import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiTreeChangeAdapter;
import com.intellij.psi.PsiTreeChangeEvent;
import com.sun.istack.NotNull;
import main.components.EditorWindow;
import main.util.MyUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Background extends JLayeredPane {
    private final Point topLeftFrom = new Point();
    private final Point bottomLeftFrom = new Point();
    private final Point topRightFrom = new Point();
    private final Point bottomRightFrom = new Point();
    private final Point topLeftTo = new Point();
    private final Point bottomLeftTo = new Point();
    private final Point topRightTo = new Point();
    private final Point bottomRightTo = new Point();
    private final Core core;
    Point myTopLeft = new Point();
    // To listen for manually written changes to the PSI tree and redraw extends arrows accordingly.
    private final PsiTreeChangeAdapter psiTreeChangeAdapter;

    public Background(Core core) {
        this.core = core;
        add(core.getWorld(), 1, 0);
        add(core.overlay, 2, 0);
        core.overlay.setSize(getWidth(), getHeight());

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                core.overlay.setSize(getWidth(), getHeight());
            }
        });

        psiTreeChangeAdapter = new PsiTreeChangeAdapter() {
            @Override
            public void childrenChanged(@NotNull PsiTreeChangeEvent event) {
                super.childrenChanged(event);
                for (var w : core.editorWindowManager.getWindows()) {
                    if (w.myPsiFile == event.getFile()) {
                        w.rebuildExtendsList();
                        repaint();
                    }
                }

            }
        };
        PsiManager.getInstance(core.getProject()).addPsiTreeChangeListener(psiTreeChangeAdapter);

        for (MouseWheelListener listener : getMouseWheelListeners()) {
            removeMouseWheelListener(listener);
        }

        addMouseMotionListener(core.globalListeners.globalMouseMotionListener);
        addMouseListener(core.globalListeners.globalMouseListener);
        addMouseWheelListener(core.globalListeners.globalMouseWheelListener);

        addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                core.editorWindowManager.mouseMovedOnBackground();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                core.editorWindowManager.mouseMovedOnBackground();
            }
        });

        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                core.editorWindowManager.mouseClickedOnBackground();
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });
    }

    public void dispose() {
        PsiManager.getInstance(core.getProject()).removePsiTreeChangeListener(psiTreeChangeAdapter);
    }

    public void setWindowLayer(EditorWindow windowToMove, int layer) {
        core.getWorld().setLayer(windowToMove, layer);
    }

    public EditorWindow addEditorWindow(PsiJavaFile psiFile) {
        EditorWindow window = core.editorWindowManager.createEditor(psiFile);
        core.getWorld().add(window);
        return window;
    }

    private void getPoints(EditorWindow window, boolean from) {
        Point topLeft = from ? topLeftFrom : topLeftTo;
        Point bottomLeft = from ? bottomLeftFrom : bottomLeftTo;
        Point topRight = from ? topRightFrom : topRightTo;
        Point bottomRight = from ? bottomRightFrom : bottomRightTo;


        int topLeftX = window.getX() + core.getWorld().getX();
        int topLeftY = window.getY() + core.getWorld().getY();

        topLeft.setLocation(topLeftX, topLeftY);
        bottomLeft.setLocation(topLeftX, topLeftY + window.getHeight());
        topRight.setLocation(topLeftX + window.getWidth(), topLeftY);
        bottomRight.setLocation(topLeftX + window.getWidth(), topLeftY + window.getHeight());
    }

    private void drawArrowFrom(Graphics2D g, EditorWindow from, EditorWindow to) {
        getPoints(from, true);
        getPoints(to, false);

        var avgY = (Math.min(bottomLeftFrom.y, bottomLeftTo.y) + Math.max(topLeftFrom.y, topLeftTo.y)) / 2;
        var avgX = (Math.min(topRightFrom.x, topRightTo.x) + Math.max(topLeftFrom.x, topLeftTo.x)) / 2;

        if (topLeftFrom.x > topRightTo.x) {
            if (topLeftFrom.y > bottomLeftTo.y) {
                MyUtils.drawArrowLine(g, topLeftFrom.x, topLeftFrom.y, bottomRightTo.x, bottomRightTo.y);
            } else if (topLeftTo.y > bottomLeftFrom.y) {
                MyUtils.drawArrowLine(g, bottomLeftFrom.x, bottomLeftFrom.y, topRightTo.x, topRightTo.y);
            } else {
                MyUtils.drawArrowLine(g, bottomLeftFrom.x, avgY, topRightTo.x, avgY);
            }
        } else if (topLeftTo.x > topRightFrom.x) {
            if (topLeftFrom.y > bottomLeftTo.y) {
                MyUtils.drawArrowLine(g, topRightFrom.x, topRightFrom.y, bottomLeftTo.x, bottomLeftTo.y);
            } else if (topLeftTo.y > bottomLeftFrom.y) {
                MyUtils.drawArrowLine(g, bottomRightFrom.x, bottomRightFrom.y, topLeftTo.x, topLeftTo.y);
            } else {
                MyUtils.drawArrowLine(g, bottomRightFrom.x, avgY, topLeftTo.x, avgY);
            }
        } else {
            if (topLeftFrom.y > bottomLeftTo.y) {
                MyUtils.drawArrowLine(g, avgX, topLeftFrom.y, avgX, bottomLeftTo.y);
            } else {
                MyUtils.drawArrowLine(g, avgX, bottomLeftFrom.y, avgX, topLeftTo.y);
            }
        }
    }

    // NOTE TO SELF. YOU CANNOT PRINT IN HERE. INFINITE RECURSIVE LOOP
    public void paintComponent(Graphics g) {
        var g2 = (Graphics2D) g;

        g.setColor(Colors.background());

        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(Colors.text());

        myTopLeft = getLocationOnScreen();

        for (var window : core.editorWindowManager.getWindows()) {
            for (var window2 : window.extendsList) {
                drawArrowFrom(g2, window, window2);
            }
        }
    }
}
