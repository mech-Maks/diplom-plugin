package ru.msu.deryugin.diplom.plugin.aop.state.loader.inst;

import com.intellij.psi.PsiMethod;
import com.intellij.psi.impl.source.PsiJavaFileImpl;
import lombok.extern.slf4j.Slf4j;
import ru.msu.deryugin.diplom.plugin.aop.state.loader.AopReferenceFetcher;
import ru.msu.deryugin.diplom.plugin.dto.PointCutContext;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static ru.msu.deryugin.diplom.plugin.util.MethodUtil.getArgsFromMethod;

@Slf4j
public class AnnotationAopReferenceFetcher implements AopReferenceFetcher {
    @Override
    public void fetchAspectsFromFile(PsiJavaFileImpl psiJavaFile, Map<PointCutContext, PsiMethod> aspectMap) {
        List.of(psiJavaFile.getClasses()[0].getMethods()).forEach(classMethod -> {
            try {
                var methodSignaturePattern = Pattern.compile("^@Pointcut\\(value ?= ?\"@annotation\\((.*)\\)\"\\)$");
                var matcher = methodSignaturePattern.matcher(classMethod.getText().split("\n")[0]);

                if (matcher.matches()) {
                    var pointCutDeclaration = classMethod.getText().substring(matcher.start(), matcher.end());
                    System.out.println(pointCutDeclaration);

                    var annotationPointCut = pointCutDeclaration.split("@annotation\\(")[1].replace(")\")", "");

                    var annotationParts = List.of(annotationPointCut.split("\\."));
                    var annotationName = annotationParts.get(annotationParts.size() - 1);
                    var annotationPkgName = String.join(".", annotationParts.subList(0, annotationParts.size() - 1));

                    var pointCutContext = new PointCutContext()
                            .setPkgName(annotationPkgName)
                            .setAnnotationName(annotationName)
                            .setAnnotation(true)
                            .setPointCutReferenceName(classMethod.getName());

                    aspectMap.put(pointCutContext, classMethod);
                }
            } catch (Exception e) {
                log.error("Error occurred during processing classMethod {}", classMethod);
            }
        });
    }
}
