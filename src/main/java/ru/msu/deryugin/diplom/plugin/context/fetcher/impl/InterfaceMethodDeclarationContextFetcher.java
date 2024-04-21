package ru.msu.deryugin.diplom.plugin.context.fetcher.impl;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import ru.msu.deryugin.diplom.plugin.context.dto.JoinPointContext;
import ru.msu.deryugin.diplom.plugin.context.fetcher.FetcherSubject;
import ru.msu.deryugin.diplom.plugin.context.fetcher.MethodContextFetcher;

import java.util.Set;

public class InterfaceMethodDeclarationContextFetcher implements MethodContextFetcher {
    @Override
    public void fetchContexts(PsiMethod psiMethod, Set<JoinPointContext> methodJoinPointContextSet, PsiClass classContainingMethod, FetcherSubject fetcherSubject) {
        if (!psiMethod.getContainingClass().isInterface()) {
            return;
        }


    }
}
