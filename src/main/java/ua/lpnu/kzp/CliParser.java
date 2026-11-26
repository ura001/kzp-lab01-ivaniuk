package ua.lpnu.kzp;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Розбирає аргументи командного рядка у структуру {@link CliOptions}.
 */
public final class CliParser {

    private static final Path DEFAULT_INPUT = Path.of("data", "input.csv");
    private static final Path DEFAULT_OUTPUT = Path.of("out", "report.txt");

    /**
     * Створює новий парсер параметрів командного рядка.
     */
    public CliParser() {
        // Стандартний конструктор
    }

    /**
     * Розбирає масив аргументів командного рядка.
     *
     * @param args аргументи {@code main}
     * @return об'єкт {@link CliOptions}
     * @throws IllegalArgumentException якщо передано невідомий параметр або відсутнє значення
     */
    public CliOptions parse(String[] args) {
        Objects.requireNonNull(args, "Масив аргументів не може бути null");

        Path input = DEFAULT_INPUT;
        Path output = DEFAULT_OUTPUT;
        boolean help = false;
        boolean version = false;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "--help" -> help = true;
                case "--version" -> version = true;
                case "--input" -> {
                    if (i + 1 >= args.length) {
                        throw new IllegalArgumentException("Після --input очікується шлях до файлу");
                    }
                    input = Path.of(args[++i]);
                }
                case "--output" -> {
                    if (i + 1 >= args.length) {
                        throw new IllegalArgumentException("Після --output очікується шлях до файлу");
                    }
                    output = Path.of(args[++i]);
                }
                default -> throw new IllegalArgumentException("Невідомий аргумент: " + arg);
            }
        }

        return new CliOptions(input, output, help, version);
    }
}