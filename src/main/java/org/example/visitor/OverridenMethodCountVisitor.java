package org.example.visitor;

import static org.objectweb.asm.Opcodes.ACC_INTERFACE;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ASM8;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

public class OverridenMethodCountVisitor extends ClassVisitor {
    public OverridenMethodCountVisitor() {
        super(ASM8);
    }

    @Getter
    @Setter
    @EqualsAndHashCode
    @RequiredArgsConstructor
    private static class Method {
        private final String name;
        private final String argumentTypes;
        // private final String[] exceptions;

        @EqualsAndHashCode.Exclude
        private boolean overrideFound = false;
    }

    private final Map<String, String> inheritanceMap = new HashMap<>();

    private final Map<String, String[]> interfacesByClass = new HashMap<>();

    private final Map<String, List<Method>> methodsByClass = new HashMap<>();

    private String className;

    public double avg() {
        var overridenMethodCounts = getOverridenMethodCounts();

        int count = overridenMethodCounts.size();

        int sum = overridenMethodCounts.values().stream().reduce(Integer::sum).orElse(count);

        return sum / (double) count;
    }

    public int max() {
        var overridenMethodCounts = getOverridenMethodCounts();

        return overridenMethodCounts.entrySet().stream()
                .map(Map.Entry::getValue)
                .max(Integer::compare)
                .orElse(0);
    }

    private Map<String, Integer> getOverridenMethodCounts() {
        Map<String, Integer> result = new HashMap<>();

        for (var className : methodsByClass.keySet()) {
            int inheritedOverridenCount = inheritedOverridenMethodCount(className, inheritanceMap.get(className), 0);

            int implementedMethodCount = implementedMethodCount(className);

            result.put(className, inheritedOverridenCount + implementedMethodCount);
        }

        methodsByClass.values().stream()
                .flatMap(list -> list.stream())
                .forEach(method -> method.setOverrideFound(false));

        return result;
    }

    private int implementedMethodCount(String className) {
        String[] interfaces = interfacesByClass.get(className);
        if (interfaces == null || interfaces.length == 0) {
            return 0;
        }

        int result = 0;

        for (var method : methodsByClass.get(className)) {
            if (method.isOverrideFound()) {
                continue;
            }

            for (String interfaceName : interfaces) {
                var interfaceMethods = methodsByClass.get(interfaceName);
                if (interfaceMethods == null) {
                    continue;
                }

                for (var interfaceMethod : interfaceMethods) {
                    if (method.equals(interfaceMethod)) {
                        ++result;
                        method.setOverrideFound(true);
                    }
                }
            }
        }

        return result;
    }

    private int inheritedOverridenMethodCount(String className, String superName, int result) {
        if ("java/lang/Object".equals(superName) || superName == null) {
            return result;
        }

        var superMethods = methodsByClass.get(superName);
        if (superMethods == null) {
            return result;
        }

        for (var method : methodsByClass.get(className)) {
            if (method.isOverrideFound()) {
                continue;
            }

            for (var superMethod : superMethods) {
                if (method.equals(superMethod)) {
                    ++result;
                    method.setOverrideFound(true);
                }
            }
        }

        return inheritedOverridenMethodCount(className, inheritanceMap.get(superName), result);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        className = name;
        methodsByClass.put(className, new ArrayList<>());

        if ((access & ACC_INTERFACE) == ACC_INTERFACE) {
            // isInterface = true;
            return;
        }

        // isInterface = false;

        interfacesByClass.put(className, interfaces);
        inheritanceMap.put(className, superName);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if ("<init>".equals(name) || "<clinit>".equals(name)) {
            return null;
        }

        if ((access & ACC_PRIVATE) == ACC_PRIVATE) {
            return null; // Ignore private methods
        }

        var method = new Method(name, Arrays.toString(Type.getArgumentTypes(desc)));
        methodsByClass.get(className).add(method);

        return null;
    }
}
