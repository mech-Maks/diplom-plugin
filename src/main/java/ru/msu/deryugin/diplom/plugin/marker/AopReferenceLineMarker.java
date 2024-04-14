package ru.msu.deryugin.diplom.plugin.marker;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiReferenceExpressionImpl;
import com.intellij.psi.util.PsiUtil;
import ru.msu.deryugin.diplom.plugin.aop.state.AspectStateService;
import ru.msu.deryugin.diplom.plugin.dto.JoinPointContext;
import ru.msu.deryugin.diplom.plugin.util.SimpleIcons;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static ru.msu.deryugin.diplom.plugin.util.MethodUtil.getReturnType;
import static ru.msu.deryugin.diplom.plugin.util.MethodUtil.joinPointCorrespondsPointcutContext;

public class AopReferenceLineMarker extends RelatedItemLineMarkerProvider {
    @Override
    protected void collectNavigationMarkers(PsiElement element, Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        // Inspection element must be method call
        if (!(element instanceof PsiMethodCallExpression)) {
            return;
        }

        var psiMethodCallExpression = (PsiMethodCallExpression) element;
        var psiMethodCalled = psiMethodCallExpression.resolveMethod();

        var classContainingCalledMethod = getClassContainingMethod(psiMethodCallExpression);

        Set<JoinPointContext> methodJoinPointContextSet = new HashSet<>();
        findPointCutContext(classContainingCalledMethod, methodJoinPointContextSet, psiMethodCalled);


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

            NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder.create(SimpleIcons.FILE)
                            .setTargets(
                                    aspectReferences.stream().map(PsiMethod::getNameIdentifier).toList()
                            )
                            .setTooltipText(toolTipTextBuilder.toString());
            result.add(builder.createLineMarkerInfo(element));
        }
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

    /**
     * Метод может быть вызван в контексте переменной, содержащей данный метод, либо без окнтекста переменной,
     * что означает, что был вызван внутренний метод класса или статический метод
     */
    private static PsiClass getClassContainingMethod(PsiMethodCallExpression psiMethodCallExpression) {
        var methodQualifier = psiMethodCallExpression.getMethodExpression().getQualifierExpression();

        return isNull(methodQualifier)
                ? psiMethodCallExpression.resolveMethod().getContainingClass()
                : PsiUtil.resolveClassInType(methodQualifier.getType());
    }
}
