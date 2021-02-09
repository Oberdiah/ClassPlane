package main.util;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.ProjectScope;
import main.core.Core;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

public class MyUtils {
    public static final boolean LOGGING_ENABLED = false;

    public static Point2D.Double toWorldSpace(Point2D point, Core core) {
        var offset = core.getWorld().getLocation();
        var safetyPoint = core.zoomPanHandler.getSafetyPoint();
        return new Point2D.Double(
                (point.getX() * core.zoomPanHandler.getCurrentZoom() + offset.x + safetyPoint.x),
                (point.getY() * core.zoomPanHandler.getCurrentZoom() + offset.y + safetyPoint.y)
        );
    }

    public static void log(Core core, String text) {
        if (!LOGGING_ENABLED) return;
        try {
            FileUtil.writeToFile(core.log, text + "\n", true);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static Collection<VirtualFile> allFilesIn(Core core) {
        var result = new HashSet<VirtualFile>();
        var projectScope = ProjectScope.getAllScope(core.getProject());
        ProjectRootManager.getInstance(core.getProject()).getFileIndex().iterateContent(fileOrDir -> {
            if (projectScope.contains(fileOrDir)) result.add(fileOrDir);
            return true;
        });
        return result;
    }

    public static Collection<PsiFileSystemItem> allPsiItemsIn(Core core) {
        var psiManager = PsiManager.getInstance(core.getProject());
        var result = new HashSet<PsiFileSystemItem>();
        for (var item : MyUtils.allFilesIn(core)) {
            if (!item.isDirectory()) {
                result.add(psiManager.findFile(item));
            }
        }
        return result;
    }

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static PsiFile virtualFileToPSIFile(VirtualFile file, Core core) {
        return PsiManager.getInstance(core.getProject()).findFile(file);
    }

    public static Document PSIFileToDoc(PsiFile psiFile, Core core) {
        return PsiDocumentManager.getInstance(core.getProject()).getDocument(psiFile);
    }

    public static void show(Object s) {
        var notification = new Notification("", "", s.toString(), NotificationType.INFORMATION, null);
        ApplicationManager.getApplication().getMessageBus().syncPublisher(Notifications.TOPIC).notify(notification);
    }

    public static VirtualFile docToVirtualFile(Document doc) {
        return FileDocumentManager.getInstance().getFile(doc);
    }

    // Taken from https://stackoverflow.com/a/46211880/5569498
    public static BufferedImage scaleNearest(BufferedImage before, double scale) {
        final int interpolation = AffineTransformOp.TYPE_NEAREST_NEIGHBOR;
        return scale(before, scale, interpolation);
    }

    public static void drawString(Graphics g, String str, int x, int y) {
        FontMetrics fm = g.getFontMetrics();
        Rectangle2D rect = fm.getStringBounds(str, g);

        g.setColor(Color.WHITE);
        g.fillRect(x,
                y,
                (int) rect.getWidth(),
                (int) rect.getHeight());

        g.setColor(Color.BLACK);
        g.drawString(str, x, y + fm.getAscent());
    }

    // Taken from https://stackoverflow.com/a/46211880/5569498
    private static BufferedImage scale(final BufferedImage before, final double scale, final int type) {
        int w = before.getWidth();
        int h = before.getHeight();
        int w2 = (int) (w * scale);
        int h2 = (int) (h * scale);
        BufferedImage after = new BufferedImage(w2, h2, before.getType());
        AffineTransform scaleInstance = AffineTransform.getScaleInstance(scale, scale);
        AffineTransformOp scaleOp = new AffineTransformOp(scaleInstance, type);
        scaleOp.filter(before, after);
        return after;
    }

    public static void setHintsToQuality(Graphics g) {
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    public static void setHintsToSpeed(Graphics g) {
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    public static void drawCircle(Graphics g, int x, int y, int rad) {
        g.drawOval(x - rad / 2, y - rad / 2, rad, rad);
    }

    public static void fillCircle(Graphics g, int x, int y, int rad) {
        g.fillOval(x - rad / 2, y - rad / 2, rad, rad);
    }

    // Taken from https://stackoverflow.com/a/27461352/5569498
    public static void drawArrowLine(Graphics g, int x1, int y1, int x2, int y2) {
        int d = 10;
        int h = 5;

        int dx = x2 - x1, dy = y2 - y1;
        if (dx == 0 && dy == 0) return;
        double D = Math.sqrt(dx * dx + dy * dy);
        double xm = D - d, xn = xm, ym = h, yn = -h, x;
        double sin = dy / D, cos = dx / D;

        x = xm * cos - ym * sin + x1;
        ym = xm * sin + ym * cos + y1;
        xm = x;

        x = xn * cos - yn * sin + x1;
        yn = xn * sin + yn * cos + y1;
        xn = x;

        int[] xpoints = {x2, (int) xm, (int) xn};
        int[] ypoints = {y2, (int) ym, (int) yn};

        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(3));
        g.drawLine(x1, y1, (int) ((xm + xn) / 2), (int) ((ym + yn) / 2));
        g.drawPolygon(xpoints, ypoints, 3);
    }
}