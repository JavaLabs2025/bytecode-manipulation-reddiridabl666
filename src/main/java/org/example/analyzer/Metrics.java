package org.example.analyzer;

public record Metrics(
        int maxInheritance,
        int avgInheritance,
        double abc,
        double avgFieldCount,
        double avgOverridenMethodCount) {
}
