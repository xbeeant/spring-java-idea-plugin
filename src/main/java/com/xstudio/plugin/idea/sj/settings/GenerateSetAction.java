package com.xstudio.plugin.idea.sj.settings;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.xstudio.plugin.idea.sj.settings.po.Template;

/**
 * @author xiaobiao
 * @version 2020/9/26
 */
public class GenerateSetAction extends AnAction {
    private Template template;

    @Override
    public void actionPerformed(AnActionEvent e) {
        template = TemplatePersistentConfiguration.getInstance().getTemplate();
        PsiClass psiClass = GenerateGetterSetter.getPsiMethodFromContext(e);
        final PsiFile file = psiClass.getContainingFile();
        Project project = psiClass.getProject();
        WriteCommandAction.writeCommandAction(project, file)
                .withGlobalUndo()
                .run(() -> GenerateGetterSetter.createGetSet(psiClass, false, true, template));
    }
}
