package ru.msu.deryugin.diplom.plugin.marker.call;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.util.PsiUtil;
import ru.msu.deryugin.diplom.plugin.aop.state.AspectStateService;
import ru.msu.deryugin.diplom.plugin.context.dto.JoinPointContext;
import ru.msu.deryugin.diplom.plugin.context.fetcher.FetcherSubject;
import ru.msu.deryugin.diplom.plugin.context.fetcher.MethodContextFetcher;
import ru.msu.deryugin.diplom.plugin.context.fetcher.impl.AnnotationMethodContextFetcher;
import ru.msu.deryugin.diplom.plugin.context.fetcher.impl.ClassHierarchyMethodContextFetcher;
import ru.msu.deryugin.diplom.plugin.marker.AbstractAopReferenceHandler;

import java.util.*;

import static ru.msu.deryugin.diplom.plugin.util.MethodUtil.joinPointCorrespondsPointcutContext;

public class AopMethodCallReferenceHandler extends AbstractAopReferenceHandler {
    public final List<MethodContextFetcher> joinPointContextFetchers;

    public AopMethodCallReferenceHandler() {
        this.joinPointContextFetchers = List.of(
                new AnnotationMethodContextFetcher(),
                new ClassHierarchyMethodContextFetcher()
        );
    }

    public void handle(PsiMethodCallExpression psiMethodCallExpression, Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        Set<JoinPointContext> methodJoinPointContextSet = new HashSet<>();
        var psiMethodCalled = psiMethodCallExpression.resolveMethod();
        var classContainingCalledMethod = getClassContainingMethod(psiMethodCallExpression);

        joinPointContextFetchers.forEach(fetcher -> fetcher.fetchContexts(psiMethodCalled, methodJoinPointContextSet, classContainingCalledMethod, FetcherSubject.METHOD_CALL));

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

        markAndNavigateToReferences(aspectReferences, psiMethodCallExpression, result);
    }

    /**
     * Метод может быть вызван в контексте переменной, содержащей данный метод, либо без окнтекста переменной,
     * что означает, что был вызван внутренний метод класса или статический метод
     */
    private static PsiClass getClassContainingMethod(PsiMethodCallExpression psiMethodCallExpression) {
        var methodQualifier = psiMethodCallExpression.getMethodExpression().getQualifierExpression();

        return Optional.ofNullable(methodQualifier)
                .filter(qualifier -> !qualifier.getText().equals("this"))
                .map(PsiExpression::getType)
                .map(PsiUtil::resolveClassInType)
                .orElse(null);
    }
}