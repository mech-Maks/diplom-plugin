package ru.msu.deryugin.diplom.plugin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.LinkedList;

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
    private String pointCutReferenceName;
}
