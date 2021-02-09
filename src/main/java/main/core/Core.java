package main.core;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.psi.PsiJavaFile;
import com.sun.istack.NotNull;
import main.components.*;
import main.util.MyUtils;
import main.util.SettingsManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Core {
    private final World world = new World();
    public String loadingString = "Waiting for indexing to complete ...";
    public Disposable disposer;
    public ZoomPanHandler zoomPanHandler;
    public GlobalListeners globalListeners;
    public Background background;
    public EditorWindowManager editorWindowManager;
    public Overlay overlay;
    public Layout layout;
    public SettingsManager settingsManager;
    public ToolWindow toolWindow;
    public File log;
    private Project project;
    private boolean isLoaded = false;
    private boolean preLoadComplete = false;


    public Core(ToolWindow toolWindow) {
        this.toolWindow = toolWindow;
    }

    /**
     * Run before a project is necessarily even opened.
     */
    public void preProjectSetup() {
        this.settingsManager = new SettingsManager(this);
        if (settingsManager.shouldShowTutorial()) {
            Tutorial.doTutorial(this);
        }
    }

    /**
     * The functional entry point of the whole program.
     * There are other bootstrapping methods, but this is where things kick off.
     *
     * @param project The currently open project.
     */
    public void start(Project project) {
        if (MyUtils.LOGGING_ENABLED) {
            log = new File(project.getBasePath() + "/ClassPlane_Log.txt");
            MyUtils.log(this, "LOGGING START");
        }
        MyUtils.log(this, "Running start()");
        this.project = project;
        this.editorWindowManager = new EditorWindowManager(this);
        this.overlay = new Overlay(this);
        this.layout = new Layout(this);

        this.zoomPanHandler = new ZoomPanHandler(this);
        this.globalListeners = new GlobalListeners(this);
        VFSHandler.init(project, this);
        this.background = new Background(this);
        editorWindowManager.loadingHasFinished();

        MyUtils.log(this, "FINISHED START");
    }

    @NotNull
    public Project getProject() {
        return project;
    }

    @NotNull
    public World getWorld() {
        return world;
    }

    public void dispose() {
        editorWindowManager.dispose();
        background.dispose();
    }

    public boolean isLoading() {
        return !isLoaded;
    }

    /**
     * Keep everything responsive while loading by amortising over many frames.
     */
    private void rec(List<PsiJavaFile> files, int i) {
        ApplicationManager.getApplication().invokeLater(() -> {
            if (i < files.size()) {
                MyUtils.log(this, "Loaded " + (i + 1) + " out of " + files.size());
                background.addEditorWindow(files.get(i));
                loadingString = "Loaded " + (i + 1) + " out of " + files.size();
                rec(files, i + 1);
            } else {
                preLoadComplete = true;
                loadingHasCompleted();
            }
        });
    }

    public void loadingHasCompleted() {
        MyUtils.log(this, "Hit loading has completed isLoaded:" + isLoaded + " preloadComplete:" + preLoadComplete + " toolWindowVis:" + toolWindow.isVisible());
        if (isLoaded || !preLoadComplete) {
            return;
        }
        if (DumbService.isDumb(project)) {
            MyUtils.log(this, "The project was dumb when it shouldn't have been. Re-requesting smart mode.");
            DumbService.getInstance(project).smartInvokeLater(this::loadingHasCompleted);
            return;
        }
        if (!toolWindow.isVisible()) {
            return;
        }
        MyUtils.log(this, "Got through loadingHasCompleted barriers, starting...");
        MyUtils.log(this, "There are " + editorWindowManager.getWindows().size() + " windows loaded.");
        isLoaded = true;

        for (var editor : editorWindowManager.getWindows()) {
            editor.rebuildExtendsList();
        }
        for (var editor : editorWindowManager.getWindows()) {
            editor.rebuildExtendsMeList();
        }
        editorWindowManager.allToStills();
        if (editorWindowManager.getWindows().size() > 0) {
            if (!settingsManager.locationIsSaved(editorWindowManager.getWindows().get(0))) {
                layout.layAllWindowsOut();
            }
        } else {
            layout.layAllWindowsOut();
        }
        overlay.revalidate();
        background.revalidate();
    }

    /**
     * Run once indexing has finished.
     */
    public void createFiles() {
        MyUtils.log(this, "Indexing has finished. Running createFiles()");

        loadingString = "Loading ...";
        final var files = new ArrayList<PsiJavaFile>();

        for (var psiItem : MyUtils.allPsiItemsIn(this)) {
            MyUtils.log(this, "Found PSI item " + psiItem.getName());
            if (!(psiItem instanceof PsiJavaFile)) continue;
            MyUtils.log(this, "    and it was a valid file");
            var javaFile = (PsiJavaFile) psiItem;
            files.add(javaFile);
        }

        MyUtils.log(this, "There are " + files.size() + " files.");

        if (files.size() > 50) {
            Messages.showDialog("Your project has more than 50 (" + files.size() + ") classes in it, which could have a negative performance impact.",
                    "Performance Warning", new String[]{"OK"}, 0, Messages.getWarningIcon());
        }

        rec(files, 0);
    }
}