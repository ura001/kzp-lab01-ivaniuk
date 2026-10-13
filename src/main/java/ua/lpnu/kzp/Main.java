package ua.lpnu.kzp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Консольна програма для обробки даних магазину техніки.
 */
public final class Main {

    private Main() {
    }

    /**
     * Точка входу до програми.
     *
     * @param args аргументи командного рядка
     */
    public static void main(String[] args) {
        String inputPath = "data/input.csv";
        String outputPath = "out/report.txt";

        // 1. Обробка аргументів командного рядка
        for (int i = 0; i < args.length; i++) {
            if ("--help".equals(args[i])) {
                System.out.printf("Використання: java -jar lab01.jar [--help] [--version] [--input <файл>] [--output <файл>]%n");
                return;
            }
            if ("--version".equals(args[i])) {
                System.out.printf("TechStore-Report версія 1.0.0%n");
                return;
            }
            if ("--input".equals(args[i]) && i + 1 < args.length) {
                inputPath = args[++i];
            }
            if ("--output".equals(args[i]) && i + 1 < args.length) {
                outputPath = args[++i];
            }
        }

        // 2. Читання файлу, обробка та запис
        try {
            Path input = Path.of(inputPath);
            Path output = Path.of(outputPath);

            // Читання у фіксованому кодуванні UTF-8
            List<String> lines = Files.readAllLines(input, StandardCharsets.UTF_8);
            String report = processRecords(lines);

            System.out.print(report);

            // Створення каталогу призначення, якщо він не існує
            Path parentDir = output.getParent();
            if (parentDir != null) {
                Files.createDirectories(parentDir);
            }
            Files.writeString(output, report, StandardCharsets.UTF_8);

        } catch (IOException e) {
            System.err.printf("Помилка роботи з файлами: %s%n", e.getMessage());
        }
    }

    /**
     * Перевіряє записи та обчислює показники магазину техніки.
     *
     * @param lines список рядків із CSV-файлу
     * @return форматований текст звіту
     */
    private static String processRecords(List<String> lines) {
        List<String> errors = new ArrayList<>();
        int validCount = 0;
        
        double totalPrice = 0.0;
        int maxWarranty = -1;
        int totalStock = 0;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank()) {
                errors.add(String.format(Locale.ROOT, "Рядок %d: порожній рядок", i + 1));
                continue;
            }

            // -1 дозволяє зберегти порожні поля в кінці рядка
            String[] fields = line.split(";", -1);
            if (fields.length != 5) {
                errors.add(String.format(Locale.ROOT, "Рядок %d: очікується 5 полів, знайдено %d", i + 1, fields.length));
                continue;
            }

            if (fields[0].isBlank() || fields[1].isBlank()) {
                errors.add(String.format(Locale.ROOT, "Рядок %d: порожня назва або категорія", i + 1));
                continue;
            }

            try {
                double price = Double.parseDouble(fields[2]);
                int warranty = Integer.parseInt(fields[3]);
                int stock = Integer.parseInt(fields[4]);

                if (price < 0 || warranty < 0 || stock < 0) {
                    errors.add(String.format(Locale.ROOT, "Рядок %d: від'ємне числове значення", i + 1));
                    continue;
                }

                // Запис успішно пройшов усі перевірки
                validCount++;
                totalPrice += price;
                maxWarranty = Math.max(maxWarranty, warranty);
                totalStock += stock;

            } catch (NumberFormatException e) {
                errors.add(String.format(Locale.ROOT, "Рядок %d: числове поле має помилковий формат", i + 1));
            }
        }

        // Обчислення середньої ціни з уникненням ділення на нуль
        double averagePrice = validCount == 0 ? 0.0 : totalPrice / validCount;

        // 3. Формування звіту з використанням Locale.ROOT та %n
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.ROOT, "Коректних записів: %d%n", validCount));
        sb.append(String.format(Locale.ROOT, "Середня ціна: %.2f грн%n", averagePrice));
        sb.append(String.format(Locale.ROOT, "Найдовша гарантія: %d міс.%n", maxWarranty == -1 ? 0 : maxWarranty));
        sb.append(String.format(Locale.ROOT, "Загальна кількість товарів: %d шт.%n", totalStock));
        sb.append(String.format(Locale.ROOT, "Помилок: %d%n", errors.size()));

        for (String error : errors) {
            sb.append(String.format(Locale.ROOT, "%s%n", error));
        }

        return sb.toString();
    }
}