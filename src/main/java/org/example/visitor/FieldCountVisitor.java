package org.example.visitor;

import static org.objectweb.asm.Opcodes.ASM8;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.*;

public class FieldCountVisitor extends ClassVisitor {
    public FieldCountVisitor() {
        super(ASM8);
    }

    private final Map<String, Integer> fieldCounts = new HashMap<>();

    private String className;

    public double avg() {
        int count = fieldCounts.size();
        return (double) fieldCounts.values().stream().reduce(Integer::sum).orElse(count) / count;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        className = name;
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        fieldCounts.merge(className, 1, Integer::sum);
        return null;
    }
}
