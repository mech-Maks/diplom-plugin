package ru.msu.deryugin.diplom.plugin.context.fetcher.impl;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import ru.msu.deryugin.diplom.plugin.context.dto.JoinPointContext;
import ru.msu.deryugin.diplom.plugin.context.fetcher.FetcherSubject;
import ru.msu.deryugin.diplom.plugin.context.fetcher.MethodContextFetcher;

import java.util.Arrays;
import java.util.Set;

import static java.util.Objects.isNull;

public class AnnotationMethodContextFetcher implements MethodContextFetcher {
    @Override
    public void fetchContexts(PsiMethod psiMethod, Set<JoinPointContext> methodJoinPointContextSet, PsiClass classContainingMethod, FetcherSubject fetcherSubject) {
        if (fetcherSubject.equals(FetcherSubject.METHOD_CALL) && isNull(classContainingMethod)) {
            return;
        }

        var annotations = Arrays.asList(psiMethod.getAnnotations());

        annotations.forEach(annotation -> {
            var annotationParts = Arrays.asList(annotation.getQualifiedName().split("\\."));
            var annotationName = annotationParts.get(annotationParts.size() - 1);
            var annotationPkgName = String.join(".", annotationParts.subList(0, annotationParts.size() - 1));

            methodJoinPointContextSet.add(new JoinPointContext()
                    .setAnnotation(true)
                    .setAnnotationName(annotationName)
                    .setPkgName(annotationPkgName));
        });
    }
}
