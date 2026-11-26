package ua.lpnu.kzp;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Незмінний набір розібраних параметрів запуску програми.
 *
 * @param inputPath шлях до вхідного CSV-файлу
 * @param outputPath шлях до вихідного файлу звіту
 * @param showHelp чи потрібно вивести довідку
 * @param showVersion чи потрібно вивести версію проєкту
 */
public record CliOptions(
        Path inputPath,
        Path outputPath,
        boolean showHelp,
        boolean showVersion) {

    /**
     * Перевіряє, що шляхи не є {@code null}.
     */
    public CliOptions {
        Objects.requireNonNull(inputPath, "Шлях до вхідного файлу не може бути null");
        Objects.requireNonNull(outputPath, "Шлях до вихідного файлу не може бути null");
    }
}