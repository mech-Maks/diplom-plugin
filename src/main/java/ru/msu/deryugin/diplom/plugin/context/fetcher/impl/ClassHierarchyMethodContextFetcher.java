package ru.msu.deryugin.diplom.plugin.context.fetcher.impl;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;
import ru.msu.deryugin.diplom.plugin.context.dto.JoinPointContext;
import ru.msu.deryugin.diplom.plugin.context.fetcher.FetcherSubject;
import ru.msu.deryugin.diplom.plugin.context.fetcher.MethodContextFetcher;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static ru.msu.deryugin.diplom.plugin.util.MethodUtil.getReturnType;

public class ClassHierarchyMethodContextFetcher implements MethodContextFetcher {
    @Override
    public void fetchContexts(PsiMethod psiMethod, Set<JoinPointContext> methodJoinPointContextSet, PsiClass classContainingMethod, FetcherSubject fetcherSubject) {
        if (fetcherSubject.equals(FetcherSubject.METHOD_CALL) && isNull(classContainingMethod)) {
            return;
        }

        if (fetcherSubject.equals(FetcherSubject.METHOD_DECLARATION)) {
            if (psiMethod.getContainingClass().isInterface()) {
                classContainingMethod = psiMethod.getContainingClass();
            } else {
                return;
            }
        }

        findPointCutContext(classContainingMethod, methodJoinPointContextSet, psiMethod);
    }

    private void findPointCutContext(PsiClass calledMethodPsiClass, final Set<JoinPointContext> pointCutContextSet, PsiMethod calledMethod) {
        Set<PsiClass> calledClassHierarchy = new HashSet<>();
        findClassHierarchy(calledMethodPsiClass, calledClassHierarchy);

        var argList = Arrays.stream(calledMethod.getParameterList().getParameters())
                .map(it -> getReturnType(it.getType()))
                .collect(Collectors.toCollection(LinkedList::new));

        var returnType = getReturnType(calledMethod.getReturnType());

        calledClassHierarchy.forEach(psiClass -> {
            pointCutContextSet.add(new JoinPointContext()
                    .setPkgName(((PsiJavaFile)psiClass.getContainingFile()).getPackageName())
                    .setClassName(psiClass.getName())
                    .setMethodName(calledMethod.getName())
                    .setArgs(argList)
                    .setReturnType(returnType)
            );
        });
    };

    /**
     * Метод рекурсивно находит иерархию классов для предоставленного класса psiClass. Поиск осуществляется по
     * всевозможным родительским классам и интерфейсам, пока не будет достигнут вверх иерархии -
     * класс Object
     */
    private static void findClassHierarchy(PsiClass psiClass, Set<PsiClass> psiClasses) {
        // вверх иерархии
        if (isNull(psiClass) || psiClass.getName().equals("Object")) {
            return;
        }

        psiClasses.add(psiClass);

        List.of(psiClass.getInterfaces()).forEach(psiClassInterface -> {
            findClassHierarchy(psiClassInterface, psiClasses);
        });

        findClassHierarchy(psiClass.getSuperClass(), psiClasses);
    }
}
