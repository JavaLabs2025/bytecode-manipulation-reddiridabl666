package org.example.visitor;

import org.objectweb.asm.*;

import static org.objectweb.asm.Opcodes.ASM8;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class InheritanceVisitor extends ClassVisitor {
    private final Map<String, List<String>> inheritanceMap = new TreeMap<>();

    public InheritanceVisitor() {
        super(ASM8);
    }

    public int maxDepth() {
        return inheritanceDepthMap().values().stream()
                .max(Integer::compareTo)
                .orElse(1);
    }

    public int avgDepth() {
        var inheritanceDepth = inheritanceDepthMap().entrySet().stream()
                .filter(entry -> entry.getValue() > 1) // filter out childless classes to get something more interesting
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        int count = inheritanceDepth.size();

        return inheritanceDepth.values().stream().reduce(Integer::sum).orElse(count) / count;
    }

    private Map<String, Integer> inheritanceDepthMap() {
        Map<String, Integer> results = new HashMap<>();

        var children = inheritanceMap.get("java/lang/Object");

        for (var child : children) {
            results.put(child, getInheritanceRecursive(child, 1));
        }

        return results;
    }

    private int getInheritanceRecursive(String parent, int result) {
        var children = inheritanceMap.get(parent);
        if (children == null) {
            return result;
        }

        ++result;

        for (var child : children) {
            int childResult = getInheritanceRecursive(child, result);
            if (childResult > result) {
                result = childResult;
            }
        }

        return result;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        if (superName == null) {
            return;
        }

        if (inheritanceMap.get(superName) == null) {
            inheritanceMap.put(superName, new ArrayList<>());
        }
        inheritanceMap.get(superName).add(name);
    }

    @Override
    public void visitSource(String source, String debug) {
    }

    @Override
    public void visitOuterClass(String owner, String name, String desc) {
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        return null;
    }

    @Override
    public void visitAttribute(Attribute attr) {
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        return null;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        return null;
    }

    @Override
    public void visitEnd() {
    }
}
