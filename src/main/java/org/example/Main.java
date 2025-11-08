package org.example;

import org.example.analyzer.JarAnalyzer;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.jar.JarFile;

public class Main {
    private static final String DEFAULT_JAR = "src/main/resources/sample.jar";

    public static void main(String[] args) throws IOException {
        String jar = DEFAULT_JAR;
        String outFile = null;

        if (args.length > 0) {
            jar = args[0];
        }

        if (args.length > 1) {
            outFile = args[1];
        }

        try (JarFile sampleJar = new JarFile(jar)) {
            var metrics = new JarAnalyzer().analyze(sampleJar);

            if (outFile == null) {
                System.out.println("Max inheritance: " + metrics.maxInheritance());
                System.out.println("Avg inheritance: " + metrics.avgInheritance());
                System.out.printf("ABC metric: %.1f\n", metrics.abc());
                System.out.printf("Average field count: %.1f\n", metrics.avgFieldCount());
                System.out.printf("Average overloaded method count: %.1f\n", metrics.avgOverridenMethodCount());

                return;
            }

            ObjectMapper mapper = new ObjectMapper();
            OutputStream outputStream = new FileOutputStream(outFile);
            mapper.writeValue(outputStream, metrics);
        }
    }
}
