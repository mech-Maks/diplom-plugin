package ru.msu.deryugin.diplom.plugin.startup;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;
import ru.msu.deryugin.diplom.plugin.aop.state.AspectStateService;

public class AopTreeProjectStartUpActivity implements StartupActivity.DumbAware {
    @Override
    public void runActivity(@NotNull Project project) {
        AspectStateService.loadTree(project);

        System.out.println("Aop tree loaded");
    }
}
