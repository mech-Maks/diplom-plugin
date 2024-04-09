package ru.msu.deryugin.diplom.plugin.marker;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiReferenceExpressionImpl;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.PsiNavigateUtil;
import ru.msu.deryugin.diplom.plugin.aop.state.AspectStateService;
import ru.msu.deryugin.diplom.plugin.dto.PointCutContext;
import ru.msu.deryugin.diplom.plugin.util.SimpleIcons;

import java.util.*;
import java.util.stream.Collectors;

import static ru.msu.deryugin.diplom.plugin.util.MethodUtil.getReturnType;

public class SimpleLineMarkerProvider extends RelatedItemLineMarkerProvider {
    @Override
    protected void collectNavigationMarkers(PsiElement element, Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        // Inspection element must be method call
        if (!(element instanceof PsiMethodCallExpression)) {
            return;
        }


        var psiMethodCallExpression = (PsiMethodCallExpression) element;
        var psiMethodCalled = psiMethodCallExpression.resolveMethod();

//        var classContainingCalledMethod = psiMethodCallExpression.resolveMethod().getContainingClass();
        var classContainingCalledMethod = PsiUtil.resolveClassInType(((PsiReferenceExpressionImpl) psiMethodCallExpression.getFirstChild().getFirstChild()).getType());

        Set<PointCutContext> methodPointCutContextSet = new HashSet<>();
        findPointCutContext(classContainingCalledMethod, methodPointCutContextSet, psiMethodCallExpression.resolveMethod().getName(), psiMethodCalled);


        var aspectMap = AspectStateService.getAspectMap();

        List<PsiMethod> aspectReferences = new ArrayList<>();
        aspectMap.forEach((aspectPointCutContext, psiMethod) -> {
            // проверить все контексты рассматриваемого метода и найти или не найти пересечения с рассматриваемым контекстом среза аспекта
            methodPointCutContextSet.forEach(methodPointCutContext -> {
                if (contextsCorrespond(aspectPointCutContext, methodPointCutContext)) {
                    aspectReferences.add(psiMethod);
                }
            });
        });

        System.out.println(psiMethodCallExpression);

        if (!aspectReferences.isEmpty()) {
            NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder.create(SimpleIcons.FILE)
                            .setTargets(aspectReferences)
                            .setTooltipText("Navigate to related AOP advices");
            result.add(builder.createLineMarkerInfo(element));
        }
    }

    private void findPointCutContext(PsiClass psiClass, final Set<PointCutContext> pointCutContextSet, String observingMethodName, PsiMethod calledMethod) {
        // верх иерархии
        if (psiClass.getName().equals("Object")) {
            return;
        }

        var psiMethods = List.of(psiClass.getMethods()).stream()
                .filter(method -> method.getName().equals(observingMethodName))
                .toList();

        psiMethods.forEach(psiMethod -> {
            if (!(psiMethod instanceof PsiMethod)) {
                return;
            }

            var argList = Arrays.stream(psiMethod.getParameterList().getParameters())
                    .map(it -> getReturnType(it.getType()))
                    .collect(Collectors.toCollection(LinkedList::new));

            pointCutContextSet.add(new PointCutContext()
                    .setPkgName(((PsiJavaFile)psiClass.getContainingFile()).getPackageName())
                    .setClassName(psiClass.getName())
                    .setMethodName(psiMethod.getName())
                    .setReturnType(getReturnType(psiMethod.getReturnType()))
                    .setArgs(argList)
            );
        });

        List.of(psiClass.getInterfaces()).forEach(psiClassInterface -> {
            findPointCutContext(psiClassInterface, pointCutContextSet, observingMethodName, calledMethod);
        });

        findPointCutContext(psiClass.getSuperClass(), pointCutContextSet, observingMethodName, calledMethod);
    };

    private static boolean contextsCorrespond(PointCutContext aspectPointCutContext, PointCutContext methodPointCutContext) {
        return Objects.equals(aspectPointCutContext.getPkgName(), methodPointCutContext.getPkgName())
                && Objects.equals(aspectPointCutContext.getClassName(), methodPointCutContext.getClassName())
                && Objects.equals(aspectPointCutContext.getMethodName(), methodPointCutContext.getMethodName())
                && (
                aspectPointCutContext.getReturnType().equals("*") || Objects.equals(aspectPointCutContext.getReturnType(), methodPointCutContext.getReturnType())
        )
                && (
                aspectPointCutContext.isAnyArgs() || Objects.equals(aspectPointCutContext.getArgs(), methodPointCutContext.getArgs())
        );
    }
}
