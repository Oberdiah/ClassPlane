package main.core;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.UIBundle;
import main.util.MyUtils;

public class Tutorial {
    private static final int numSteps = 6;
    private static int tutorialStep = -1;

    private static String title() {
        return "Tutorial Step " + (tutorialStep - 1) + "/" + numSteps;
    }

    private static void free(Core core) {
        // If we're still in the pre-initialisation phase
        if (core.zoomPanHandler == null) return;
        core.zoomPanHandler.dragMouseStartLocation = null;
        core.zoomPanHandler.dragComponentStartLocation = null;
        core.overlay.isDragging = false;
        for (var w : core.editorWindowManager.getWindows()) {
            w.isDragging = false;
        }
    }

    public static void doStep0(Core core) {
        if (tutorialStep != 0) return;
        tutorialStep++;
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            if (core.toolWindow.isVisible()) {
                doStep1(core);
            } else {
                MyUtils.sleep(1000);
                ApplicationManager.getApplication().invokeLater(() -> {
                    Messages.showDialog("To get started, click the 'ClassPlane' button on the right-hand side of the editor.",
                            title(), new String[]{"Continue"}, 0, Messages.getInformationIcon());
                });
            }
        });
    }

    public static void doStep1(Core core) {
        if (tutorialStep != 1) return;
        tutorialStep++;
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            MyUtils.sleep(1000);
            free(core);
            ApplicationManager.getApplication().invokeLater(() -> {
                Messages.showDialog("Here are your projects' classes.\nTry dragging them around by their header bar.",
                        title(), new String[]{"Continue"}, 0, Messages.getInformationIcon());
            });
        });
    }

    public static void doStep2(Core core) {
        if (tutorialStep != 2) return;
        tutorialStep++;
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            MyUtils.sleep(1000);
            free(core);
            ApplicationManager.getApplication().invokeLater(() -> {
                Messages.showDialog("You can pan around the pane using Middle Click and dragging.\nScrolling will pan you up and down.",
                        title(), new String[]{"Continue"}, 0, Messages.getInformationIcon());
            });
        });
    }

    public static void doStep3(Core core) {
        if (tutorialStep != 3) return;
        tutorialStep++;
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            MyUtils.sleep(1000);
            free(core);
            ApplicationManager.getApplication().invokeLater(() -> {
                Messages.showDialog("Zoom in and out by holding down CTRL and scrolling.",
                        title(), new String[]{"Continue"}, 0, Messages.getInformationIcon());
            });
        });
    }

    public static void doStep4(Core core) {
        if (tutorialStep != 4) return;
        tutorialStep++;
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            MyUtils.sleep(1000);
            free(core);
            ApplicationManager.getApplication().invokeLater(() -> {
                Messages.showDialog("If you zoom fully in you can edit classes as you normally would.\n" +
                                "You can only edit a class while your mouse is hovered over it.\n" +
                                "Note that not all editor functionality is available, as ClassPlane is still in alpha.\n",
                        title(), new String[]{"Continue"}, 0, Messages.getInformationIcon());
            });
        });
    }

    public static void doStep5(Core core) {
        if (tutorialStep != 5) return;
        tutorialStep++;
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            MyUtils.sleep(1000);
            free(core);
            ApplicationManager.getApplication().invokeLater(() -> {
                Messages.showDialog("When zoomed out, click on a class to select it or on the background to deselect everything.\n" +
                                "On a selected class you can click and drag the arrow icon to another class then release.\n" +
                                "This will create a 'Extends' relationship between the two classes.\n" +
                                "Repeating the procedure will remove the relationship.",
                        title(), new String[]{"Continue"}, 0, Messages.getInformationIcon());
            });
        });
    }

    public static void doStep6(Core core) {
        if (tutorialStep != 6) return;
        tutorialStep++;
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            MyUtils.sleep(1000);
            free(core);
            ApplicationManager.getApplication().invokeLater(() -> {
                Messages.showDialog("That should be everything!\n" +
                                "I hope you enjoy using ClassPane.",
                        title(), new String[]{"Done"}, 0, Messages.getInformationIcon());
            });
        });
    }

    public static void doTutorial(Core core) {
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            MyUtils.sleep(1000);
            ApplicationManager.getApplication().invokeLater(() -> {
                free(core);
                final int doTutorial = Messages.showCheckboxMessageDialog(
                        "Welcome to ClassPlane!\nDo you wish to do the tutorial?",
                        "Tutorial",
                        new String[]{"No", "Do The Tutorial"},
                        UIBundle.message("dialog.options.do.not.show"),
                        false, 1, 1, Messages.getQuestionIcon(),
                        (exitCode, cb) -> {
                            if (cb.isSelected()) {
                                core.settingsManager.setDoNotShow();
                            }
                            return exitCode == DialogWrapper.OK_EXIT_CODE ? exitCode : DialogWrapper.CANCEL_EXIT_CODE;
                        }
                );
                if (doTutorial == 1) {
                    tutorialStep = 0;
                }
                ApplicationManager.getApplication().invokeLater(() -> {
                    doStep0(core);
                });
            });
        });
    }
}
