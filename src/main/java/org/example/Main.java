package org.example;

import org.example.visitor.ABCVisitor;
import org.example.visitor.InheritanceVisitor;
import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Main {
    private static final String DEFAULT_JAR = "src/main/resources/sample.jar";

    public static void main(String[] args) throws IOException {
        String jar = DEFAULT_JAR;
        if (args.length > 0) {
            jar = args[0];
        }

        try (JarFile sampleJar = new JarFile(jar)) {
            Enumeration<JarEntry> enumeration = sampleJar.entries();

            InheritanceVisitor inheritance = new InheritanceVisitor();
            ABCVisitor abc = new ABCVisitor();

            while (enumeration.hasMoreElements()) {
                JarEntry entry = enumeration.nextElement();

                if (entry.getName().endsWith(".class")) {

                    ClassReader cr = new ClassReader(sampleJar.getInputStream(entry));

                    cr.accept(inheritance, 0);
                    cr.accept(abc, 0);
                }
            }

            System.out.println("Max inheritance: " + inheritance.maxDepth());
            System.out.println("Avg inheritance: " + inheritance.avgDepth());
            System.out.printf("ABC metric: %.1f\n", abc.getMetric());
        }
    }
}
