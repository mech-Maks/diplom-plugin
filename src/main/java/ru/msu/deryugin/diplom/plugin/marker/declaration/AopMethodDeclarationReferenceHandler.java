package ru.msu.deryugin.diplom.plugin.marker.declaration;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.impl.source.PsiMethodImpl;
import ru.msu.deryugin.diplom.plugin.aop.state.AspectStateService;
import ru.msu.deryugin.diplom.plugin.context.dto.JoinPointContext;
import ru.msu.deryugin.diplom.plugin.context.fetcher.FetcherSubject;
import ru.msu.deryugin.diplom.plugin.context.fetcher.impl.ClassHierarchyMethodContextFetcher;
import ru.msu.deryugin.diplom.plugin.marker.AbstractAopReferenceHandler;
import ru.msu.deryugin.diplom.plugin.context.fetcher.MethodContextFetcher;
import ru.msu.deryugin.diplom.plugin.context.fetcher.impl.AnnotationMethodContextFetcher;

import java.util.*;

import static ru.msu.deryugin.diplom.plugin.util.MethodUtil.joinPointCorrespondsPointcutContext;

public class AopMethodDeclarationReferenceHandler extends AbstractAopReferenceHandler {
    private final List<MethodContextFetcher> joinPointContextFetchers;
    public AopMethodDeclarationReferenceHandler() {
        joinPointContextFetchers = List.of(
                new AnnotationMethodContextFetcher(),
                new ClassHierarchyMethodContextFetcher()
        );
    }

    public void handle(PsiMethodImpl psiMethodImpl, Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        Set<JoinPointContext> methodJoinPointContextSet = new HashSet<>();
        joinPointContextFetchers.forEach(fetcher -> fetcher.fetchContexts(psiMethodImpl, methodJoinPointContextSet, null, FetcherSubject.METHOD_DECLARATION));

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

        markAndNavigateToReferences(aspectReferences, psiMethodImpl, result);
    }
}
