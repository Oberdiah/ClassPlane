package main.components;

import main.core.Core;
import main.util.MyUtils;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;

public class Layout {
    public final List<Box> rootBoxes = new ArrayList<>();
    protected final Set<EditorWindow> alreadyProcessed = new HashSet<>();
    public double initialZoomLevel = 0;

    public double getSpacing() {
        return 100 * core.zoomPanHandler.getCurrentZoom();
    }

    private final Core core;
    public Layout(Core core) {
        this.core = core;
    }

    public void layAllWindowsOut() {
        initialZoomLevel = core.zoomPanHandler.getCurrentZoom();

        for (var editor : core.editorWindowManager.getWindows()) {
            // We only process directly those which aren't under anything
            if (!editor.extendsList.isEmpty()) continue;

            rootBoxes.add(new Box(editor));
        }

        double xMid = -core.getWorld().getLocation().x + 500;
        double yMid = -core.getWorld().getLocation().y + 500;

        for (Box box : rootBoxes) {
            // Just so the boxes have a notion of width and height before the next stage.
            box.setPosition(new Point2D.Double(xMid, yMid));
        }

        double W_H_RATIO = 2.0;
        double numAttemptsScalar = 20;

        MyUtils.log(core, "Laying windows out ... " + rootBoxes.size());

        List<Box> toAddToPlane = new ArrayList<>(rootBoxes);
        List<Box> alreadyOnPlane = new ArrayList<>();
        toAddToPlane.sort(Comparator.comparingInt((t) -> t.windowsIn.size()));
        double radius = 0;
        while (toAddToPlane.size() > 0) {
            // Pick a random angle
            double angle = Math.random() * Math.PI * 2;
            var boxRectA = new Rectangle2D.Double();
            var boxRectB = new Rectangle2D.Double();
            for (var box : new ArrayList<>(toAddToPlane)) {
                // Find possible location for box.
                double newRadius = radius;// + Math.sqrt(box.width*box.width/4 + box.height*box.height/4)
                double x = newRadius * Math.cos(angle) * W_H_RATIO + xMid;
                double y = newRadius * Math.sin(angle) + yMid;

                boxRectA.setRect(x, y, box.width, box.height);
                boolean collided = false;
                for (var collideBox : alreadyOnPlane) {
                    boxRectB.setRect(
                            collideBox.position.getX() - getSpacing(),
                            collideBox.position.getY() - getSpacing(),
                            collideBox.width + getSpacing() * 2,
                            collideBox.height + getSpacing() * 2
                    );
                    if (boxRectA.intersects(boxRectB)) {
                        collided = true;
                    }
                }

                if (!collided) {
                    MyUtils.log(core, "Set window position to " + x + " " + y);
                    box.setPosition(new Point2D.Double(x, y));
                    alreadyOnPlane.add(box);
                    toAddToPlane.remove(box);
                    break;
                }
            }
            radius += getSpacing() / numAttemptsScalar;
        }
    }

    public class Box {
        public EditorWindow parent;
        public List<Box> children = new ArrayList<>();
        public List<EditorWindow> windowsIn = new ArrayList<>();

        public double width = 0;
        public double height = 0;
        public Point2D position;

        public Box(EditorWindow parent) {
            initialise(parent, this);
        }

        private Box(EditorWindow parent, Box root) {
            initialise(parent, root);
        }

        private void initialise(EditorWindow parent, Box root) {
            windowsIn.add(parent);
            this.parent = parent;
            for (var child : parent.extendsMeList) {
                if (alreadyProcessed.contains(child)) continue;
                alreadyProcessed.add(child);
                Box childBox = new Box(child, root);
                children.add(childBox);
                windowsIn.addAll(childBox.windowsIn);
            }
        }

        public void setPosition(Point2D position) {
            this.position = position;
            if (children.isEmpty()) {
                parent.setLocation(position.getX(), position.getY());
                width = parent.getWidth();
                height = parent.getHeight();
            } else {
                double x = position.getX();
                double y = position.getY() + parent.getHeight() + getSpacing();
                double maxChildHeight = 0;
                for (var child : children) {
                    child.setPosition(new Point2D.Double(x, y));
                    x += child.width + getSpacing();
                    if (maxChildHeight < child.height) maxChildHeight = child.height;
                }
                width = x - position.getX() - getSpacing();

                if (parent.getWidth() > width) {
                    width = parent.getWidth();
                }

                height = maxChildHeight + parent.getHeight() + getSpacing();

                double newYSpacing = width / 20;
                if (newYSpacing > getSpacing()) {
                    double newY = position.getY() + parent.getHeight() + newYSpacing;
                    double newX = position.getX();
                    for (var child : children) {
                        child.setPosition(new Point2D.Double(newX, newY));
                        newX += child.width + getSpacing();
                    }

                    height = maxChildHeight + parent.getHeight() + newYSpacing;
                }

                parent.setLocation(width / 2 - parent.getWidth() / 2.0 + position.getX(), position.getY());
            }
        }
    }
}
