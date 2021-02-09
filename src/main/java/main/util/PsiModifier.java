package main.util;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElementFactory;
import main.components.EditorWindow;
import main.core.Core;


public class PsiModifier {
    public static void toggleExtends(EditorWindow from, EditorWindow to, Core core) {
        var clazz = from.getPsiClass();
        if (clazz == null) return;
        var extendsList = clazz.getExtendsList();
        if (extendsList == null) return;

        boolean alreadyExtends = false;
        for (PsiClassType psiClassType : extendsList.getReferencedTypes()) {
            if (to.getPsiClass() == psiClassType.resolve()) {
                alreadyExtends = true;
            }
        }

        from.toEditor();
        if (alreadyExtends) {
            WriteCommandAction.runWriteCommandAction(core.getProject(), () -> {
                extendsList.getReferenceElements()[0].delete();
            });
        } else {
            if (extendsList.getReferencedTypes().length > 0) {
                WriteCommandAction.runWriteCommandAction(core.getProject(), () -> {
                    extendsList.getReferenceElements()[0].delete();
                    PsiElementFactory factory = JavaPsiFacade.getInstance(core.getProject()).getElementFactory();
                    clazz.getExtendsList().add(factory.createClassReferenceElement(to.getPsiClass()));
                });
            } else {
                WriteCommandAction.runWriteCommandAction(core.getProject(), () -> {
                    PsiElementFactory factory = JavaPsiFacade.getInstance(core.getProject()).getElementFactory();
                    clazz.getExtendsList().add(factory.createClassReferenceElement(to.getPsiClass()));
                });
            }
        }
        from.toStill();
    }
}
