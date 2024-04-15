package ru.msu.deryugin.diplom.plugin.marker.declaration;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.impl.source.PsiMethodImpl;
import ru.msu.deryugin.diplom.plugin.aop.state.AspectStateService;
import ru.msu.deryugin.diplom.plugin.dto.JoinPointContext;
import ru.msu.deryugin.diplom.plugin.util.AopNavigateIcon;

import java.util.*;

import static ru.msu.deryugin.diplom.plugin.util.MethodUtil.joinPointCorrespondsPointcutContext;

public class AopMethodDeclarationReferenceHandler {

    public void handle(PsiMethodImpl psiMethodImpl, Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        var annotations = Arrays.asList(psiMethodImpl.getAnnotations());

        Set<JoinPointContext> methodJoinPointContextSet = new HashSet<>();
        annotations.forEach(annotation -> {
            var annotationParts = Arrays.asList(annotation.getQualifiedName().split("\\."));
            var annotationName = annotationParts.get(annotationParts.size() - 1);
            var annotationPkgName = String.join(".", annotationParts.subList(0, annotationParts.size() - 1));

            methodJoinPointContextSet.add(new JoinPointContext()
                    .setAnnotation(true)
                    .setAnnotationName(annotationName)
                    .setPkgName(annotationPkgName));
        });

        var aspectMap = AspectStateService.getAspectMap();

        List<PsiMethod> aspectReferences = new ArrayList<>();
        aspectMap.forEach((aspectPointCutContext, psiMethod) -> {
            // проверить все контексты рассматриваемого метода и найти или не найти пересечения с рассматриваемым контекстом среза аспекта
            methodJoinPointContextSet.forEach(methodJoinPointContext -> {
                if (joinPointCorrespondsPointcutContext(aspectPointCutContext, methodJoinPointContext)) {
                    aspectReferences.add(psiMethod);
                }
            });
        });

        if (!aspectReferences.isEmpty()) {
            StringBuilder toolTipTextBuilder = new StringBuilder("Related Aop advices:");

            aspectReferences.forEach(ref -> {
                toolTipTextBuilder.append("\n" + ref.getContainingClass().getName() + "::" + ref.getName());
            });

            NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder.create(AopNavigateIcon.FILE)
                    .setTargets(
                            aspectReferences.stream().map(PsiMethod::getNameIdentifier).toList()
                    )
                    .setTooltipText(toolTipTextBuilder.toString());
            result.add(builder.createLineMarkerInfo(psiMethodImpl));
        }
    }
}
