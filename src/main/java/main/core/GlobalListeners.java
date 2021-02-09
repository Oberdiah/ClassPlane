package main.core;

import com.intellij.ide.IdeEventQueue;
import com.intellij.openapi.editor.event.EditorMouseEvent;
import com.intellij.openapi.editor.event.EditorMouseListener;
import com.intellij.openapi.editor.event.EditorMouseMotionListener;
import main.util.MyUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.event.*;
import java.util.HashSet;

/**
 * A simple setup that consolidates any events from throughout the application into one function.
 * <p>
 * Any new elements of the application should add these listeners to their JComponents so they can be routed
 * accordingly.
 * <p>
 * While setting up an entire application AWT listener could be done instead,
 * it's a bad idea since a plugin error would cause an IDE-wide failure.
 */
public class GlobalListeners {
    private final Core core;
    private final HashSet<Integer> keysPressed = new HashSet<>();
    public MouseListener globalMouseListener = new MouseListener() {
        @Override
        public void mouseClicked(MouseEvent e) {
            mouseEventFired(e);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            mouseEventFired(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            mouseEventFired(e);
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            mouseEventFired(e);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            mouseEventFired(e);
        }
    };
    public MouseMotionListener globalMouseMotionListener = new MouseMotionListener() {
        @Override
        public void mouseDragged(MouseEvent e) {
            mouseEventFired(e);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            mouseEventFired(e);
        }
    };
    public EditorMouseMotionListener globalEditorMouseMotionListener = new EditorMouseMotionListener() {
        public void mouseDragged(@NotNull EditorMouseEvent e) {
            mouseEventFired(e.getMouseEvent());
        }

        public void mouseMoved(@NotNull EditorMouseEvent e) {
            mouseEventFired(e.getMouseEvent());
        }
    };
    public EditorMouseListener globalEditorMouseListener = new EditorMouseListener() {
        public void mouseClicked(@NotNull EditorMouseEvent e) {
            mouseEventFired(e.getMouseEvent());
        }

        public void mousePressed(@NotNull EditorMouseEvent e) {
            mouseEventFired(e.getMouseEvent());
        }

        public void mouseReleased(@NotNull EditorMouseEvent e) {
            mouseEventFired(e.getMouseEvent());
        }

        public void mouseEntered(@NotNull EditorMouseEvent e) {
            mouseEventFired(e.getMouseEvent());
        }

        public void mouseExited(@NotNull EditorMouseEvent e) {
            mouseEventFired(e.getMouseEvent());
        }
    };
    public MouseWheelListener globalMouseWheelListener;
    public GlobalListeners(Core core) {
        this.core = core;

        /*
         * I couldn't find a nice way to get the keypresses to work in the same way as the mouse movement does,
         * so I simply globally listen for it.
         * This would generally be dangerous, but as I end up only needing to know whether CTRL is pressed anyway,
         * it's unlikely to result in any ill effects.
         *
         * There also doesn't seem to be any way to dispose of this listener but it's a tiny function and
         * unlikely to cause issues even if somehow bound 100s of times.
         */
        IdeEventQueue.getInstance().addPostEventListener(event -> {
            if (event instanceof KeyEvent) {
                keyEventFired((KeyEvent) event);
            }
            return false;
        }, core.disposer);
        globalMouseWheelListener = core.zoomPanHandler::mouseEvent;
    }

    private void keyEventFired(KeyEvent e) {
        MyUtils.log(core, "Key pressed " + e.getKeyChar() + " " + e.getKeyCode());
        if (e.getID() == KeyEvent.KEY_PRESSED) {
            keysPressed.add(e.getKeyCode());
        } else if (e.getID() == KeyEvent.KEY_RELEASED) {
            keysPressed.remove(e.getKeyCode());
        }
    }

    public boolean isKeyPressed(int key) {
        return keysPressed.contains(key);
    }

    private void mouseEventFired(MouseEvent e) {
        core.zoomPanHandler.mouseEvent(e);
    }
}
