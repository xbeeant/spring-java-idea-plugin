package com.xstudio.plugin.idea.sj.util;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;

/**
 * @author xiaobiao
 * @date 2023/9/21
 */
public class PluginNotifier {
    public static void notify(Project project, String title, String content) {
        NotificationGroupManager.getInstance()
                .getNotificationGroup("SJ Notification Group")
                .createNotification(title, content, NotificationType.INFORMATION)
                .notify(project);
    }
}
