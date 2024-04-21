package ru.msu.deryugin.diplom.plugin.aop.state;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.impl.source.PsiJavaFileImpl;
import ru.msu.deryugin.diplom.plugin.aop.state.loader.AopReferenceFetcher;
import ru.msu.deryugin.diplom.plugin.aop.state.loader.inst.AnnotationAopReferenceFetcher;
import ru.msu.deryugin.diplom.plugin.aop.state.loader.inst.MethodExecutionAopReferenceFetcher;
import ru.msu.deryugin.diplom.plugin.context.dto.PointCutContext;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AspectStateService {
    private static boolean isAspectMapConstructed = false;
    private static final Map<PointCutContext, PsiMethod> aspectMap = new HashMap<>();
    private final List<AopReferenceFetcher> aopReferenceFetchers;

    public AspectStateService() {
        aopReferenceFetchers = List.of(
                new MethodExecutionAopReferenceFetcher(),
                new AnnotationAopReferenceFetcher()
        );
    }

    public void loadAopMap(Project project) {
        // Ищем файлы с исходниками
        try {
            var srcFolder = Paths.get(project.getBasePath()).toFile().listFiles(file -> file.getName().equals("src"))[0];
            var virtualFileSystem = LocalFileSystem.getInstance();
            var psiManager = PsiManager.getInstance(project);

            var psiJavaFiles = Files.walk(srcFolder.toPath())
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().contains(".java") && path.getFileName().toString().contains("Aspect"))
                    .map(path -> virtualFileSystem.findFileByIoFile(path.toFile()))
                    .map(virtualFile -> (PsiJavaFileImpl) psiManager.findFile(virtualFile))
                    .toList();


            psiJavaFiles.forEach(psiJavaFile -> {
                aopReferenceFetchers.forEach(loader -> loader.fetchAspectsFromFile(psiJavaFile, aspectMap));
            });

            isAspectMapConstructed = true;
        } catch (Exception e) {
            System.out.println("Error occurred during constructing aspect map");
            System.out.println(e);
        }
    }

    public static Map<PointCutContext, PsiMethod> getAspectMap() {
        if (isAspectMapConstructed) {
            return aspectMap;
        } else {
            return Collections.EMPTY_MAP;
        }
    }
}
