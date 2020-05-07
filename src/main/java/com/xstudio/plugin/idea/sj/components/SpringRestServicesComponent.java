package com.xstudio.plugin.idea.sj.components;

import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.impl.source.PsiClassImpl;
import com.intellij.psi.impl.source.tree.java.PsiLiteralExpressionImpl;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBList;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.xstudio.plugin.idea.sj.spring.Annotations;
import com.xstudio.plugin.idea.sj.spring.Mapping;
import com.xstudio.plugin.idea.sj.spring.RequestPath;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author xiaobiao
 * @version 2020/4/29
 */
public class SpringRestServicesComponent implements ProjectComponent {

    protected final Project project;
    private JPanel content = new JPanel();

    public SpringRestServicesComponent(Project project) {
        this.project = project;
    }

    @Override
    public void projectOpened() {
        ModuleManager moduleManager = ModuleManager.getInstance(this.project);
        Module[] modules = moduleManager.getSortedModules();
        List<RequestPath> requestPaths = new ArrayList<>();

        // 获取 request list
        for (Module module : modules) {
            GlobalSearchScope moduleScope = GlobalSearchScope.moduleScope(module);
            // search all spring @RestController annotation in module
            Collection<PsiAnnotation> psiAnnotations = JavaAnnotationIndex.getInstance().get("RestController", this.project, moduleScope);
            for (PsiAnnotation psiAnnotation : psiAnnotations) {
                PsiElement psiElement = psiAnnotation.getParent();
                PsiElement parent = psiElement.getParent();
                // 获取 class 的requestMapping路径
                String parentRequestMapping = null;
                for (Mapping mapping : Annotations.getClasz()) {
                    PsiAnnotation requestMappingAnnotation = ((PsiClassImpl) parent).getAnnotation(mapping.getQualifiedName());
                    parentRequestMapping = getPath(requestMappingAnnotation, mapping);
                    if (null != parentRequestMapping) {
                        break;
                    }
                }

                if (null == parentRequestMapping) {
                    parentRequestMapping = "";
                }

                // 获取各方法的mapping
                PsiMethod[] psiMethods = ((PsiClassImpl) parent).getMethods();
                for (PsiMethod psiMethod : psiMethods) {
                    List<Mapping> methods = Annotations.getMethods();
                    for (Mapping mapping : methods) {
                        PsiAnnotation annotation = psiMethod.getAnnotation(mapping.getQualifiedName());
                        String path = getPath(annotation, mapping);
                        if (null != path) {
                            requestPaths.add(new RequestPath(parentRequestMapping, path, mapping.getMethod(), psiMethod));
                        }
                    }
                }
            }
        }
        // 创建右侧的tool window
        ToolWindowManager instance = ToolWindowManager.getInstance(project);
        ToolWindow toolWindow = instance.registerToolWindow("Request List", true, ToolWindowAnchor.RIGHT);
        toolWindow.setIcon(IconLoader.getIcon("/icons/plus.png"));
        // 初始化右侧的tool window
        initialToolWindow(toolWindow, requestPaths);
    }


    private void initialToolWindow(ToolWindow toolWindow, List<RequestPath> requests) {
        RestListForm restListForm = new RestListForm();
        JBList<RequestPath> jbList = restListForm.getJbList();
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        // 填充list
        DefaultListModel<RequestPath> listModel = new DefaultListModel<>();
        for (RequestPath request : requests) {
            listModel.addElement(request);
        }

        // 搜索框
        restListForm.getRequestSearch().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String value = restListForm.getRequestSearch().getText();
                listModel.clear();
                for (RequestPath item : requests) {
                    if (item.getPath().contains(value)) {
                        listModel.addElement(item);
                    }
                }
            }
        });

        // 自定义list渲染器
        jbList.setCellRenderer(new ListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel jLabel = new JLabel(((RequestPath) value).getPath());
                if (cellHasFocus || isSelected) {
                    jLabel.setBackground(JBColor.LIGHT_GRAY);
                    jLabel.setOpaque(true);
                }
                return jLabel;
            }
        });
        jbList.setModel(listModel);
        // 选中监听器
        // 点击请求 跳转到对应的方法中
        jbList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    JList<RequestPath> item = (JList<RequestPath>) e.getSource();
                    RequestPath selectedValue = item.getSelectedValue();
                    if (null != selectedValue) {
                        NavigationUtil.activateFileWithPsiElement(selectedValue.getPsiMethod(), true);
                    }
                    item.clearSelection();
                }
            }
        });
        // 创建面板
        Content content = contentFactory.createContent(restListForm.getPanel(), "", false);
        toolWindow.getContentManager().addContent(content);
    }

    private String getPath(PsiAnnotation psiAnnotation, Mapping mapping) {
        if (null != psiAnnotation) {
            PsiAnnotationMemberValue value = psiAnnotation.findDeclaredAttributeValue("value");
            if (value instanceof PsiLiteralExpressionImpl) {
                Object path = ((PsiLiteralExpressionImpl) value).getValue();
                return String.valueOf(path);
            }
        }
        return null;
    }

    @Override
    public void projectClosed() {

    }

    @Override
    public void initComponent() {

    }
}
