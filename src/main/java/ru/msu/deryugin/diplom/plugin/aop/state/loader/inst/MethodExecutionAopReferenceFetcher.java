package ru.msu.deryugin.diplom.plugin.aop.state.loader.inst;

import com.intellij.psi.PsiMethod;
import com.intellij.psi.impl.source.PsiJavaFileImpl;
import lombok.extern.slf4j.Slf4j;
import ru.msu.deryugin.diplom.plugin.aop.state.loader.AopReferenceFetcher;
import ru.msu.deryugin.diplom.plugin.context.dto.PointCutContext;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static ru.msu.deryugin.diplom.plugin.util.MethodUtil.getArgsFromMethod;

@Slf4j
public class MethodExecutionAopReferenceFetcher implements AopReferenceFetcher {
    @Override
    public void fetchAspectsFromFile(PsiJavaFileImpl psiJavaFile, Map<PointCutContext, PsiMethod> aspectMap) {
        List.of(psiJavaFile.getClasses()[0].getMethods()).forEach(classMethod -> {
            try {
                var methodSignaturePattern = Pattern.compile("^@Pointcut\\(value ?= ?\"execution\\((.*)\\)\"\\)$");
                var matcher = methodSignaturePattern.matcher(classMethod.getText().split("\n")[0]);

                if (matcher.matches()) {
                    var pointCutDeclarations = classMethod.getText().substring(matcher.start(), matcher.end());
                    System.out.println(pointCutDeclarations);

                    var pointCuts = pointCutDeclarations.split("execution\\(")[1].replace(")\")", "");

                    Arrays.stream(pointCuts.split("\\|\\|")).forEach(pointCut -> {
                        System.out.println(pointCut);
                        var returnType = pointCut.trim().split(" ")[0];
                        var methodNameAndPkg = List.of(pointCut.trim().split(" ")[1].split("\\(")[0].split("\\."));

                        var methodName = methodNameAndPkg.get(methodNameAndPkg.size() - 1);
                        var className = methodNameAndPkg.get(methodNameAndPkg.size() - 2);
                        var pkgName = String.join(".", methodNameAndPkg.subList(0, methodNameAndPkg.size() - 2));

                        var argList = getArgsFromMethod(pointCut, methodName);

                        boolean anyArgs = argList.size() == 1 && argList.get(0).equals("*");

                        var pointCutContext = new PointCutContext()
                                .setPkgName(pkgName)
                                .setClassName(className)
                                .setMethodName(methodName)
                                .setReturnType(returnType)
                                .setArgs(argList)
                                .setAnyArgs(anyArgs)
                                .setPointCutReferenceName(classMethod.getName());

                        aspectMap.put(pointCutContext, classMethod);
                    });
                }
            } catch (Exception e) {
                log.error("Error occurred during processing classMethod {}", classMethod);
            }
        });
    }
}
