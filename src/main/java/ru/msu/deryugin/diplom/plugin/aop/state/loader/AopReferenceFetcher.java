package ru.msu.deryugin.diplom.plugin.aop.state.loader;

import com.intellij.psi.PsiMethod;
import com.intellij.psi.impl.source.PsiJavaFileImpl;
import ru.msu.deryugin.diplom.plugin.context.dto.PointCutContext;

import java.util.Map;

public interface AopReferenceFetcher {
    void fetchAspectsFromFile(PsiJavaFileImpl psiJavaFile, Map<PointCutContext, PsiMethod> aspectMap);
}
