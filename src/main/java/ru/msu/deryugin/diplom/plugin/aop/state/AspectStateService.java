package ru.msu.deryugin.diplom.plugin.aop.state;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.impl.source.PsiJavaFileImpl;
import lombok.extern.slf4j.Slf4j;
import ru.msu.deryugin.diplom.plugin.dto.PointCutContext;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

import static ru.msu.deryugin.diplom.plugin.util.MethodUtil.getArgsFromMethod;

@Slf4j
public class AspectStateService {
    private static boolean isAspectMapConstructed = false;
    private static Map<PointCutContext, PsiMethod> aspectMap = new HashMap<>();

    public static synchronized void loadTree(Project project) {
        // Ищем файлы с исходниками
        try {
            var srcFolder = Paths.get(project.getBasePath()).toFile().listFiles(file -> file.getName().equals("src"))[0];


            var srcFiles = Files.walk(srcFolder.toPath())
                    .filter(Files::isRegularFile)
                    .filter(file -> file.getFileName().toString().contains(".java"))
                    .filter(file -> file.getFileName().toString().contains("Aspect"))
                    .toList();


            srcFiles.forEach(javaFile -> {
                var vfJavaFile = LocalFileSystem.getInstance().findFileByIoFile(javaFile.toFile());
                var psiJavaFile = (PsiJavaFileImpl) PsiManager.getInstance(project).findFile(vfJavaFile);

                List.of(psiJavaFile.getClasses()[0].getMethods()).forEach(classMethod -> {
                    try {
                        var methodSignaturePattern = Pattern.compile("^@Pointcut\\(value ?= ?\"execution\\((.*)\\)\"\\)$");
                        var matcher = methodSignaturePattern.matcher(classMethod.getText().split("\n")[0]);

                        if (matcher.matches()) {
                            var pointCutDeclarations = classMethod.getText().substring(matcher.start(), matcher.end());
                            System.out.println(pointCutDeclarations);

                            var pointCuts = pointCutDeclarations.split("execution\\(")[1].replace(")\")", "");

                            var aopHandlerMethodDeclaration = classMethod.getText().split("\n")[1];
                            Arrays.stream(pointCuts.split("\\|\\|")).forEach(pointCut -> {
                                System.out.println(pointCut);
                                var returnType = pointCut.trim().split(" ")[0];
                                var methodNameAndPkg = List.of(pointCut.trim().split(" ")[1].split("\\(")[0].split("\\."));

                                var methodName = methodNameAndPkg.get(methodNameAndPkg.size() - 1);
                                var className = methodNameAndPkg.get(methodNameAndPkg.size() - 2);
                                var pkgName = String.join(".", methodNameAndPkg.subList(0, methodNameAndPkg.size() - 2));

                                var argList = getArgsFromMethod(pointCut, methodName);

                                boolean anyArgs = argList.size() == 1 && argList.get(0).equals("*");

                                var pointCutContext = new PointCutContext(pkgName, className, methodName, returnType, argList, anyArgs, classMethod.getName());
                                aspectMap.put(pointCutContext, classMethod);
                            });
                        }

                        System.out.println(classMethod);
                    } catch (Exception e) {
                        log.error("Error occurred during processing classMethod {}", classMethod);
                    }
                });

                isAspectMapConstructed = true;
            });
        } catch (Exception e) {
            log.error("Error occurred during constructing aspect map", e);
            return;
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
