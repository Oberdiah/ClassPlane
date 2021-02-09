package main.components;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.ex.FoldingListener;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiJavaFile;
import main.core.Colors;
import main.core.Core;
import main.core.Tutorial;
import main.util.MyUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class EditorWindow extends JComponent {
    public final EditorImpl editor;
    public final VirtualFile myVirtualFile;
    public final PsiJavaFile myPsiFile;
    public final Document myDoc;
    private final Point startDragMouseLocation = new Point();
    private final Point startDragComponentLocation = new Point();
    private final TitleBorder border;
    private final DocumentListener docListener;
    public BufferedImage image;
    public boolean isSelected;
    public List<EditorWindow> extendsList = new ArrayList<>();
    public List<EditorWindow> extendsMeList = new ArrayList<>();
    public boolean processing = false;
    public boolean isDragging = false;
    private boolean isEditor = true;
    private int myVirtualPageHeight;
    // This exclusively exists so that we don't get rounding errors when zooming.
    private final Point2D.Double trueLocation = new Point2D.Double();
    private final Core core;

    public EditorWindow(PsiJavaFile psiFile, Core core) {
        this.core = core;
        myPsiFile = psiFile;
        myDoc = MyUtils.PSIFileToDoc(psiFile, core);
        myVirtualFile = MyUtils.docToVirtualFile(myDoc);
        setLayout(new BorderLayout());
        editor = (EditorImpl) EditorFactory.getInstance().createEditor(
                myDoc,
                core.getProject(),
                myVirtualFile,
                false
        );

        docListener = new DocumentListener() {
            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
                if (!core.toolWindow.isVisible()) return;
                if (!isEditor) {
                    // A cause of many framerate issues.
                    toEditor();
                    toStill();
                }
                Tutorial.doStep5(core);
            }
        };
        myDoc.addDocumentListener(docListener);

        editor.getScrollingModel().addVisibleAreaListener(e -> {
            refresh();
        });
        editor.getFoldingModel().addListener(new FoldingListener() {
            @Override
            public void onFoldProcessingEnd() {
                refresh();
            }
        }, core.disposer);

        editor.getSettings().setShowIntentionBulb(false);
        editor.getSettings().setRightMarginShown(false);
        editor.getScrollPane().getVerticalScrollBar().setEnabled(false);
        editor.getSettings().setRefrainFromScrolling(true);
        editor.getScrollPane().setWheelScrollingEnabled(false);
        editor.getFoldingModel().setFoldingEnabled(false);

        add(editor.getComponent());
        border = new TitleBorder(this, core);
        setBorder(border);

        addListeners();

        refresh();
    }

    @Override
    public String toString() {
        return myPsiFile.getName();
    }

    public void rebuildExtendsList() {
        extendsList.clear();
        var classes = getPsiClass();
        if (classes == null) return;
        if (classes.isInterface()) return; // Don't even bother
        var extendsList = classes.getExtendsList();
        if (extendsList == null) return;
        for (PsiClassType psiClassType : extendsList.getReferencedTypes()) {
            for (var window2 : core.editorWindowManager.getWindows()) {
                var psiClass = psiClassType.resolve();
                if (psiClass == window2.getPsiClass()) {
                    this.extendsList.add(window2);
                }
            }
        }
    }

    public void rebuildExtendsMeList() {
        extendsMeList.clear();
        for (var window : core.editorWindowManager.getWindows()) {
            if (window.extendsList.contains(this)) {
                extendsMeList.add(window);
            }
        }
    }

    public void dispose() {
        myDoc.removeDocumentListener(docListener);
        core.getWorld().remove(this);
        EditorFactory.getInstance().releaseEditor(editor);
    }

    public PsiClass getPsiClass() {
        // We just ignore any files with no classes.
        var classes = myPsiFile.getClasses();
        for (var clazz : classes) {
            return clazz;
        }
        return null;
    }

    public void paintComponent(Graphics g) {
        g.setColor(new Color(40, 40, 60));

        var insets = border.getBorderInsets(null);
        double scalingFactor = core.zoomPanHandler.getCurrentZoom();

        if (image == null) {
            return;
        }

        int width = (int) (image.getWidth() * scalingFactor);
        int height = (int) (image.getHeight() * scalingFactor);

        g.drawImage(image, insets.left, insets.top, width, height, null);

        if (core.zoomPanHandler.getCurrentZoom() != 1.0) {
            if (isSelected) {
                g.setColor(Colors.windowSelectedOverlay());
                g.fillRect(insets.left, insets.top, width, height);
            }
            if (core.editorWindowManager.isHovered(this)) {
                g.setColor(Colors.windowHoveredOverlay());
                g.fillRect(insets.left, insets.top, width, height);
            } else if (core.editorWindowManager.dragHovered == this && core.overlay.isDragging) {
                g.setColor(Colors.windowDraggedOntoOverlay());
                g.fillRect(insets.left, insets.top, width, height);
            }

            if (isSelected) {
                var p = getLocationOnScreen();
                p.translate(getWidth() / 2, getHeight() / 2 + insets.top / 2);
                core.overlay.overlayCircleLocation = p;
                core.overlay.repaint();
            }
        }
    }

    public VirtualFile getVirtualFile() {
        return myVirtualFile;
    }

    public boolean isEditor() {
        return isEditor;
    }

    public void toEditor() {
        MyUtils.log(core, this.myPsiFile.getName() + " - Converting to editor.");
        if (isEditor) {
            MyUtils.log(core, this.myPsiFile.getName() + " - Already editor. Refreshing anyway.");
            refresh();
            return;
        }
        editor.setCaretVisible(true);
        this.add(editor.getComponent());
        isEditor = true;
        refresh();
    }

    public void toStill() {
        MyUtils.log(core, this.myPsiFile.getName() + " - Converting to still");
        editor.getSelectionModel().removeSelection();
        editor.setCaretVisible(false);

        refresh();
        processing = true;
        isEditor = false;
        rebuildImage();
        this.remove(editor.getComponent());
        zoomHasUpdated();
        processing = false;
    }

    private void rebuildImage() {
        MyUtils.log(core, this.myPsiFile.getName() + "- Rebuilding image");
        int width = editor.getComponent().getWidth();
        int height = editor.getComponent().getHeight();
        if (image != null) {
            image.flush();
        }
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = image.createGraphics();
        editor.getComponent().paint(graphics2D);
        graphics2D.dispose();
    }

    public void zoomHasUpdated() {
        double scalingFactor = core.zoomPanHandler.getCurrentZoom();
        if (!isEditor() && image != null) {
            var insets = border.getBorderInsets(null);
            setSize(
                    (int) (image.getWidth() * scalingFactor) + insets.left + insets.right,
                    (int) (image.getHeight() * scalingFactor) + insets.top + insets.bottom
            );
            revalidate();
        }
    }

    private void refresh() {
        if (!isEditor) {
            validate();
            return;
        }

        var preferredSize = getPreferredSize();

        var editorSettings = editor.getSettings();
        var lineHeight = editor.getLineHeight();

        // Calculates the height by undoing the virtual page space that
        // EditorSizeManager.getPreferredHeight() adds to the bottom of files.
        int height = preferredSize.height;
        if (editorSettings.isAdditionalPageAtBottom()) {
            int visibleAreaHeight = editor.getScrollingModel().getVisibleArea().height;
            if (visibleAreaHeight > 0 || myVirtualPageHeight <= 0) {
                myVirtualPageHeight = Math.max(visibleAreaHeight - 2 * lineHeight, lineHeight);
            }

            height -= Math.max(myVirtualPageHeight, 0);
        } else {
            height -= editorSettings.getAdditionalLinesCount() * lineHeight;
        }
        setSize(preferredSize.width, height);
        validate();
    }

    private void mouseMoveEvent(MouseEvent e) {
        if (isDragging) {
            core.editorWindowManager.pullToTop(this);
            int dX = startDragMouseLocation.x - e.getXOnScreen();
            int dY = startDragMouseLocation.y - e.getYOnScreen();
            setLocation(startDragComponentLocation.x - dX, startDragComponentLocation.y - dY);
            core.background.repaint();
            MyUtils.log(core, "Moved editor window: " + getLocation());
        }

        core.editorWindowManager.mouseMovedInWindow(this);
    }

    private void addListeners() {
        final var editorWindow = this;

        addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                editorWindow.mouseMoveEvent(e);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                editorWindow.mouseMoveEvent(e);
            }
        });

        addMouseListener(new MouseListener() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (core.overlay.circleHovered) return;

                if (e.getButton() == 1) {
                    isDragging = true;
                    startDragMouseLocation.setLocation(e.getXOnScreen(), e.getYOnScreen());
                    startDragComponentLocation.setLocation(editorWindow.getLocation());
                    Tutorial.doStep2(core);
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                core.editorWindowManager.mouseClickedInWindow(editorWindow);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == 1) {
                    isDragging = false;
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                core.editorWindowManager.mouseEntered(editorWindow);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                core.editorWindowManager.mouseExited(editorWindow);
            }
        });

        addMouseMotionListener(core.globalListeners.globalMouseMotionListener);
        addMouseListener(core.globalListeners.globalMouseListener);
        addMouseWheelListener(core.globalListeners.globalMouseWheelListener);
        editor.addEditorMouseListener(core.globalListeners.globalEditorMouseListener);
        editor.addEditorMouseMotionListener(core.globalListeners.globalEditorMouseMotionListener);
        editor.getScrollPane().addMouseWheelListener(core.globalListeners.globalMouseWheelListener);
    }

    public Point2D.Double getTrueLocation() {
        return trueLocation;
    }

    public void setLocation(double x, double y) {
        trueLocation.setLocation(x, y);
        super.setLocation((int) x, (int) y);
        core.settingsManager.saveLocationToFile(this);
    }

    @Override
    public void setLocation(@NotNull Point p) {
        trueLocation.setLocation(p);
        super.setLocation(p);
        core.settingsManager.saveLocationToFile(this);
    }

    @Override
    public void setLocation(int x, int y) {
        trueLocation.setLocation(x, y);
        super.setLocation(x, y);
        core.settingsManager.saveLocationToFile(this);
    }
}
