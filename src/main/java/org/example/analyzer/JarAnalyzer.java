package org.example.analyzer;

import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.example.visitor.ABCVisitor;
import org.example.visitor.FieldCountVisitor;
import org.example.visitor.InheritanceVisitor;
import org.example.visitor.OverridenMethodCountVisitor;
import org.objectweb.asm.ClassReader;

public class JarAnalyzer {
    public Metrics analyze(JarFile jar) throws IOException {
        Enumeration<JarEntry> enumeration = jar.entries();

        InheritanceVisitor inheritance = new InheritanceVisitor();
        ABCVisitor abc = new ABCVisitor();
        FieldCountVisitor fieldCount = new FieldCountVisitor();
        OverridenMethodCountVisitor overridenMethodsCount = new OverridenMethodCountVisitor();

        while (enumeration.hasMoreElements()) {
            JarEntry entry = enumeration.nextElement();

            if (entry.getName().endsWith(".class")) {

                ClassReader cr = new ClassReader(jar.getInputStream(entry));

                cr.accept(inheritance, 0);
                cr.accept(abc, 0);
                cr.accept(fieldCount, 0);
                cr.accept(overridenMethodsCount, 0);
            }
        }

        return new Metrics(
                inheritance.maxDepth(),
                inheritance.avgDepth(),
                abc.getMetric(),
                fieldCount.avg(),
                overridenMethodsCount.avg());
    }
}
