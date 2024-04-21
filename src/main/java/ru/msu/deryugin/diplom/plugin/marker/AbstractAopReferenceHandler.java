package ru.msu.deryugin.diplom.plugin.marker;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import ru.msu.deryugin.diplom.plugin.util.AopNavigateIcon;

import java.util.Collection;
import java.util.List;

public abstract class AbstractAopReferenceHandler {
    protected void markAndNavigateToReferences(List<PsiMethod> aspectReferences, PsiElement psiElement, Collection<? super RelatedItemLineMarkerInfo<?>> result) {
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
            result.add(builder.createLineMarkerInfo(psiElement));
        }
    }
}
