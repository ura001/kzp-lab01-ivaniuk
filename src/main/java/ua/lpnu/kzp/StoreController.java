package ua.lpnu.kzp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Контролер обробки даних магазину техніки: координує читання CSV,
 * створення об'єктів {@link Product}, підрахунок {@link StoreSummary} та формування звіту.
 */
public final class StoreController {

    /**
     * Створює екземпляр контролера.
     */
    public StoreController() {
        // Стандартний конструктор
    }

    /**
     * Виконує повний цикл обробки за заданими параметрами командного рядка.
     *
     * @param options параметри запуску
     * @return код завершення ({@code 0} — успіх, {@code 1} — помилка вводу-виводу або файлу)
     */
    public int run(CliOptions options) {
        Objects.requireNonNull(options, "Параметри запуску не можуть бути null");

        if (options.showHelp()) {
            printHelp();
            return 0;
        }

        if (options.showVersion()) {
            System.out.printf(Locale.ROOT, "Версія: %s%n", BuildInfo.version());
            return 0;
        }

        if (!Files.exists(options.inputPath())) {
            System.err.printf(Locale.ROOT, "Помилка: вхідний файл не знайдено: %s%n", options.inputPath());
            return 1;
        }

        List<String> lines;
        try {
            lines = Files.readAllLines(options.inputPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.printf(Locale.ROOT, "Помилка читання файлу: %s%n", e.getMessage());
            return 1;
        }

        List<Product> products = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            String rawLine = lines.get(i);
            if (rawLine.trim().isEmpty()) {
                continue;
            }
            try {
                products.add(Product.fromCsv(rawLine));
            } catch (IllegalArgumentException e) {
                errors.add("Рядок %d: %s".formatted(i + 1, e.getMessage()));
            }
        }

        StoreSummary summary = calculateSummary(products);
        String report = formatReport(summary, errors);
        System.out.print(report);

        try {
            Path parent = options.outputPath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(options.outputPath(), report, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.printf(Locale.ROOT, "Помилка запису звіту: %s%n", e.getMessage());
            return 1;
        }

        return 0;
    }

    /**
     * Обчислює підсумкові показники за списком коректних товарів.
     *
     * @param products список об'єктів {@link Product}
     * @return об'єкт {@link StoreSummary}
     */
    public StoreSummary calculateSummary(List<Product> products) {
        Objects.requireNonNull(products, "Список товарів не може бути null");

        int validCount = products.size();
        if (validCount == 0) {
            return new StoreSummary(0, 0.0, 0, 0);
        }

        double totalPrice = 0.0;
        int maxWarranty = 0;
        int totalStock = 0;

        for (Product product : products) {
            totalPrice += product.getPrice();
            maxWarranty = Math.max(maxWarranty, product.getWarrantyMonths());
            totalStock += product.getStock();
        }

        double averagePrice = totalPrice / validCount;
        return new StoreSummary(validCount, averagePrice, maxWarranty, totalStock);
    }

    private String formatReport(StoreSummary summary, List<String> errors) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.ROOT, "Коректних записів: %d%n", summary.validCount()));
        sb.append(String.format(Locale.ROOT, "Середня ціна: %.2f грн%n", summary.averagePrice()));
        sb.append(String.format(Locale.ROOT, "Найдовша гарантія: %d міс.%n", summary.maxWarranty()));
        sb.append(String.format(Locale.ROOT, "Загальна кількість товарів: %d шт.%n", summary.totalStock()));
        sb.append(String.format(Locale.ROOT, "Помилок: %d%n", errors.size()));
        for (String error : errors) {
            sb.append(String.format(Locale.ROOT, "%s%n", error));
        }
        return sb.toString();
    }

    private void printHelp() {
        System.out.printf(Locale.ROOT,
                "Використання: java -jar app.jar [--help] [--version] [--input <файл>] [--output <файл>]%n");
    }
}