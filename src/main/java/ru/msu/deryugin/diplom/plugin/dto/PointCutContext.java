package ru.msu.deryugin.diplom.plugin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.LinkedList;

/**
 * Контекст среза при объявлении аспектов
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class PointCutContext {
    private String pkgName;
    private String className;
    private String methodName;
    private String returnType;
    private LinkedList<String> args;
    private boolean anyArgs;
    /**
     * Поле необходимо для отслеживания аспектов: имеющих одинаковый контекст, но отличающихся
     * логикой (в частности названием аспекта, идентифицирующим логику)
     */
    private String pointCutReferenceName;
}
