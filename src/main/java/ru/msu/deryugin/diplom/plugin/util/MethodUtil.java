package ru.msu.deryugin.diplom.plugin.util;

import com.intellij.psi.PsiType;
import lombok.experimental.UtilityClass;
import ru.msu.deryugin.diplom.plugin.context.dto.JoinPointContext;
import ru.msu.deryugin.diplom.plugin.context.dto.PointCutContext;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

@UtilityClass
public class MethodUtil {
    /**
     * Распарсить параметры метода из строки - работа со строкой происходит на этапе анализа среза
     *
     * @param methodText
     * @param methodName
     * @return
     */
    public static LinkedList<String> getArgsFromMethod(String methodText, String methodName) {
        LinkedList<String> argList = new LinkedList<>();

        var methodArgsMatcher = Pattern.compile("^(.*)" + methodName + "\\((.*)\\)$");
        var matcher = methodArgsMatcher.matcher(methodText);

        if (matcher.matches()) {
            var argsString = methodText.split(methodName)[1];

            var splitedArguments = argsString.substring(1, argsString.length() - 1);
            splitedArguments= splitedArguments.replace("..", "*");

            List.of(splitedArguments.split(",")).forEach(argTypeAndValue -> {
                argList.add(argTypeAndValue.trim().split(" ")[0].trim());
            });
        }

        return argList;
    }


    /**
     * Psitype может быть null (если тип возвращаемого значения примитивный void).<br/>
     *
     * psiType.getCanonicalTest() возвращает строку формата pkg.name.ClassName, потому необходимо отсеить название
     * пакета
     */
    public static String getReturnType(PsiType psiType) {
        return Optional.ofNullable(psiType)
                .map(it -> it.getCanonicalText())
                .map(returnTypeString -> {
                    var returnTypeParts = returnTypeString.split("\\.");
                    return  returnTypeParts[returnTypeParts.length - 1];
                })
                .orElse("void");
    }

    /**
     * Соответствует ли точка соединения какому-либо из известных срезов.
     *
     * @param aspectPointCutContext контекст среза
     * @param methodJoinPointContext контекст точки соединения
     */
    public static boolean joinPointCorrespondsPointcutContext(PointCutContext aspectPointCutContext, JoinPointContext methodJoinPointContext) {
        if (!Objects.equals(aspectPointCutContext.isAnnotation(), methodJoinPointContext.isAnnotation())) {
            return false;
        }


        return methodJoinPointContext.isAnnotation()
                ? joinPointCorrespondsPointcutContextAnnotationCase(aspectPointCutContext, methodJoinPointContext)
                : joinPointCorrespondsPointcutContextExecutionCase(aspectPointCutContext, methodJoinPointContext);
    }

    private static boolean joinPointCorrespondsPointcutContextExecutionCase(PointCutContext aspectPointCutContext, JoinPointContext methodJoinPointContext) {
        return Objects.equals(aspectPointCutContext.getPkgName(), methodJoinPointContext.getPkgName())
                && Objects.equals(aspectPointCutContext.getClassName(), methodJoinPointContext.getClassName())
                && Objects.equals(aspectPointCutContext.getMethodName(), methodJoinPointContext.getMethodName())
                && (
                    aspectPointCutContext.getReturnType().equals("*") || Objects.equals(aspectPointCutContext.getReturnType(), methodJoinPointContext.getReturnType())
                )
                && (
                    aspectPointCutContext.isAnyArgs() || Objects.equals(aspectPointCutContext.getArgs(), methodJoinPointContext.getArgs())
                );
    }

    private static boolean joinPointCorrespondsPointcutContextAnnotationCase(PointCutContext aspectPointCutContext, JoinPointContext methodJoinPointContext) {
        return Objects.equals(aspectPointCutContext.getPkgName(), methodJoinPointContext.getPkgName())
                && Objects.equals(aspectPointCutContext.getAnnotationName(), methodJoinPointContext.getAnnotationName());
    }
}
