package main.core;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import main.util.MyUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

public class MyToolWindowFactory implements ToolWindowFactory, DumbAware {
    private Core core;

    @Override
    public void init(@NotNull ToolWindow toolWindow) {
        // Can't replace with method reference due to object replacing rules.
        core = new Core(toolWindow);
        core.disposer = new Disposable() {
            @Override
            public void dispose() {
                core.dispose();
            }
        };
        core.preProjectSetup();
    }

    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        core.start(project);
        core.background.addAncestorListener(new AncestorListener() {
            public void ancestorAdded(AncestorEvent event) {
                core.loadingHasCompleted();
                MyUtils.log(core, "Mounted core.background");
                Tutorial.doStep1(core);
            }

            public void ancestorRemoved(AncestorEvent event) {
                core.loadingHasCompleted();
                MyUtils.log(core, "Unmounted core.background");
                Tutorial.doStep1(core);
            }

            public void ancestorMoved(AncestorEvent event) {
            }
        });

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(core.background, "", false);
        toolWindow.getContentManager().addContent(content);

        content.setDisposer(core.disposer);
        DumbService.getInstance(project).runWhenSmart(core::createFiles);
    }
}