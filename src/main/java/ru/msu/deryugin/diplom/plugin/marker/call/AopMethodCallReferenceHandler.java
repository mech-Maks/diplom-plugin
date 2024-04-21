package ru.msu.deryugin.diplom.plugin.marker.call;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;
import ru.msu.deryugin.diplom.plugin.aop.state.AspectStateService;
import ru.msu.deryugin.diplom.plugin.context.dto.JoinPointContext;
import ru.msu.deryugin.diplom.plugin.context.fetcher.FetcherSubject;
import ru.msu.deryugin.diplom.plugin.context.fetcher.MethodContextFetcher;
import ru.msu.deryugin.diplom.plugin.context.fetcher.impl.AnnotationMethodContextFetcher;
import ru.msu.deryugin.diplom.plugin.context.fetcher.impl.ClassHierarchyMethodContextFetcher;
import ru.msu.deryugin.diplom.plugin.marker.AbstractAopReferenceHandler;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static ru.msu.deryugin.diplom.plugin.util.MethodUtil.getReturnType;
import static ru.msu.deryugin.diplom.plugin.util.MethodUtil.joinPointCorrespondsPointcutContext;

public class AopMethodCallReferenceHandler extends AbstractAopReferenceHandler {
    public final List<MethodContextFetcher> contextFetchers;

    public AopMethodCallReferenceHandler() {
        this.contextFetchers = List.of(
                new AnnotationMethodContextFetcher(),
                new ClassHierarchyMethodContextFetcher()
        );
    }

    public void handle(PsiMethodCallExpression psiMethodCallExpression, Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        Set<JoinPointContext> methodJoinPointContextSet = new HashSet<>();
        var psiMethodCalled = psiMethodCallExpression.resolveMethod();
        var classContainingCalledMethod = getClassContainingMethod(psiMethodCallExpression);

        contextFetchers.forEach(fetcher -> fetcher.fetchContexts(psiMethodCalled, methodJoinPointContextSet, classContainingCalledMethod, FetcherSubject.METHOD_CALL));

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