package ru.msu.deryugin.diplom.plugin.aop.startup;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;
import ru.msu.deryugin.diplom.plugin.aop.state.AspectStateService;

public class AopProjectLoaderStartUpActivity implements StartupActivity.DumbAware {
    @Override
    public void runActivity(@NotNull Project project) {
        AspectStateService stateService = new AspectStateService();
        stateService.loadAopMap(project);

        System.out.println("Aop tree loaded");
    }
}
