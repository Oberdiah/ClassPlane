package main.core;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import main.util.MyUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class VFSHandler {
    public static void init(Project project, Core core) {
        project.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
            @Override
            public void after(@NotNull List<? extends VFileEvent> events) {
                for (var event : events) {
                    if (event.getFile() == null) continue;
                    if (!ProjectFileIndex.getInstance(core.getProject()).isInSource(event.getFile())) continue;

                    PsiFile file = MyUtils.virtualFileToPSIFile(event.getFile(), core);
                    if (file instanceof PsiJavaFile) {
                        PsiJavaFile javaFile = (PsiJavaFile) file;
                        if (event instanceof VFileCreateEvent) {
                            var windLoc = core.editorWindowManager.averageWindowLocation();
                            var window = core.background.addEditorWindow(javaFile);
                            core.editorWindowManager.changeSelectionTo(window);
                            window.setLocation(windLoc.x, windLoc.y);
                            window.toStill();
                        }
                    }
                }
            }

            @Override
            public void before(@NotNull List<? extends VFileEvent> events) {
                for (var event : events) {
                    if (event instanceof VFileDeleteEvent) {
                        for (var w : new ArrayList<>(core.editorWindowManager.getWindows())) {
                            if (w.myVirtualFile.equals(event.getFile())) {
                                core.editorWindowManager.delete(w);
                            }
                        }
                    }
                }
            }
        });
    }
}
