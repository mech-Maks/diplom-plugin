package ru.msu.deryugin.diplom.plugin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.LinkedList;

/**
 * Контекст точки присоединения аспекта в программе
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class JoinPointContext {
    private String pkgName;
    private String className;
    private String methodName;
    private String returnType;
    private LinkedList<String> args;
}
