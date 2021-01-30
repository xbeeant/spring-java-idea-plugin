package com.xstudio.plugin.idea.sj.getset;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.CollectionListModel;
import com.xstudio.plugin.idea.sj.getset.po.Template;
import com.xstudio.plugin.idea.sj.util.JavaBeansUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * @author xiaobiao
 * @version 2020/9/26
 */
public class GenerateGetterSetter {

    public static void createGetSet(PsiClass psiClass, boolean getter, boolean setter, Template template) {
        List<PsiField> fields = new CollectionListModel<>(psiClass.getFields()).getItems();
        List<PsiMethod> methods = new CollectionListModel<>(psiClass.getMethods()).getItems();
        HashSet<String> methodSet = new HashSet<>();
        for (PsiMethod method : methods) {
            methodSet.add(method.getName());
        }
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(psiClass.getProject());
        String methodText;
        PsiMethod toMethod;
        for (PsiField field : fields) {
            if (!(Objects.requireNonNull(field.getModifierList())).hasModifierProperty(PsiModifier.FINAL)) {
                if (getter) {
                    methodText = buildGet(field, template);
                    toMethod = elementFactory.createMethodFromText(methodText, psiClass);
                    if (!methodSet.contains(toMethod.getName())) {
                        psiClass.add(toMethod);
                    }
                }

                if (setter) {
                    methodText = buildSet(field, template);
                    elementFactory = JavaPsiFacade.getElementFactory(psiClass.getProject());
                    toMethod = elementFactory.createMethodFromText(methodText, psiClass);
                    if (!methodSet.contains(toMethod.getName())) {
                        psiClass.add(toMethod);
                    }
                }
            }
        }
    }

    private static String buildGet(PsiField field, Template template) {
        StringBuilder sb = new StringBuilder();
        String doc = format("get", field, template);
        if (doc != null) {
            sb.append(doc);
        }
        sb.append("public ");
        if ((Objects.requireNonNull(field.getModifierList())).hasModifierProperty("static")) {
            sb.append("static ");
        }
        sb.append(field.getType().getPresentableText() + " ");
        if ("boolean".equals(field.getType().getPresentableText())) {
            sb.append("is");
        } else {
            sb.append("get");
        }
        sb.append(JavaBeansUtil.getFirstCharacterUppercase(field.getName()));
        sb.append("(){\n");
        sb.append(" return this.").append(field.getName()).append(";}\n");
        return sb.toString();
    }

    private static String buildSet(PsiField field, Template template) {
        StringBuilder sb = new StringBuilder();
        String doc = format("set", field, template);
        if (doc != null) {
            sb.append(doc);
        }
        sb.append("public ");
        if (field.getModifierList().hasModifierProperty("static")) {
            sb.append("static ");
        }
        sb.append("void ");
        sb.append("set").append(JavaBeansUtil.getFirstCharacterUppercase(field.getName()));
        sb.append("(").append(field.getType().getPresentableText()).append(" ").append(field.getName()).append("){\n");
        sb.append("this.").append(field.getName()).append(" = ").append(field.getName()).append(";");
        sb.append("}");
        return sb.toString();
    }


    private static String format(String string, PsiField field, Template template) {
        DateFormat date = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat dateTime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date currentDate = new Date();
        String oldContent;
        if (field.getDocComment() == null) {
            oldContent = field.getText().substring(0, field.getText().lastIndexOf("\n") + 1);
        } else {
            oldContent = field.getDocComment().getText();
        }
        oldContent = oldContent.replaceAll("[\n,\r,*,/,\t]", "").trim();
        if ("get".equals(string)) {
            oldContent = template.getGetter().toLowerCase()
                    .replaceAll("\\$\\{field_comment}", oldContent)
                    .replaceAll("\\$\\{user}", System.getProperties().getProperty("user.name"))
                    .replaceAll("\\$\\{date}", date.format(currentDate))
                    .replaceAll("\\$\\{time}", dateTime.format(currentDate))
                    .replaceAll("\\$\\{field_name}", field.getName());
        } else if ("set".equals(string)) {
            oldContent = template.getSetter().toLowerCase()
                    .replaceAll("\\$\\{field_comment}", oldContent)
                    .replaceAll("\\$\\{user}", System.getProperties().getProperty("user.name"))
                    .replaceAll("\\$\\{date}", date.format(currentDate))
                    .replaceAll("\\$\\{time}", dateTime.format(currentDate))
                    .replaceAll("\\$\\{field_name}", field.getName());
        }
        return oldContent;
    }

    public static PsiClass getPsiMethodFromContext(AnActionEvent e) {
        PsiElement elementAt = getPsiElement(e);
        return (elementAt == null) ? null : PsiTreeUtil.getParentOfType(elementAt, PsiClass.class);
    }

    private static PsiElement getPsiElement(AnActionEvent e) {
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (psiFile != null && editor != null) {
            int offset = editor.getCaretModel().getOffset();
            return psiFile.findElementAt(offset);
        }
        e.getPresentation().setEnabled(false);
        return null;
    }
}
