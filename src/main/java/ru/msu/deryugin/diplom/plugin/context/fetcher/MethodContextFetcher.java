package ru.msu.deryugin.diplom.plugin.context.fetcher;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import ru.msu.deryugin.diplom.plugin.context.dto.JoinPointContext;

import java.util.Set;

public interface MethodContextFetcher {
    void fetchContexts(PsiMethod psiMethod,
                       Set<JoinPointContext> methodJoinPointContextSet,
                       PsiClass classContainingMethod,
                       FetcherSubject fetcherSubject);
}
