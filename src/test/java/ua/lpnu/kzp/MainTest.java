package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Модульні тести основної логіки, CLI та формування звіту.
 */
class MainTest {

    @Test
    void averagePriceAndLocaleFormattingWorkAsExpected() {
        double totalPrice = 35000.50 + 850.00 + 18999.99;
        int validCount = 3;
        double average = totalPrice / validCount;

        assertEquals(18283.4966667, average, 0.0001);
        assertEquals("18283.50", String.format(Locale.ROOT, "%.2f", average));
    }

    @Test
    void storeSummaryRejectsInvalidAggregates() {
        assertThrows(IllegalArgumentException.class,
                () -> new StoreSummary(-1, 100.0, 12, 10));
        assertThrows(IllegalArgumentException.class,
                () -> new StoreSummary(2, -1.0, 12, 10));
        assertThrows(IllegalArgumentException.class,
                () -> new StoreSummary(2, 100.0, -6, 10));
        assertThrows(IllegalArgumentException.class,
                () -> new StoreSummary(2, 100.0, 12, -1));
    }

    @Test
    void cliParserHandlesHelpVersionAndCustomPaths() {
        CliParser parser = new CliParser();

        assertTrue(parser.parse(new String[]{"--help"}).showHelp());
        assertTrue(parser.parse(new String[]{"--version"}).showVersion());

        CliOptions custom = parser.parse(new String[]{
                "--input", "data/input.csv",
                "--output", "out/report.txt"
        });
        assertEquals(Path.of("data/input.csv"), custom.inputPath());
        assertEquals(Path.of("out/report.txt"), custom.outputPath());
    }

    @Test
    void controllerReportsLineNumbersForInvalidRows(@TempDir Path tempDir) throws IOException {
        Path input = tempDir.resolve("input.csv");
        Path output = tempDir.resolve("report.txt");

        Files.write(input, List.of(
                "Ноутбук ASUS;Комп'ютери;35000.50;24;15",
                "Миша бездротова;Периферія;850.00;12;40",
                "Смартфон;Мобільні пристрої;18999.99;12;25",
                "Клавіатура;Периферія;-500.00;12;5",
                "Монітор AOC;Екрани;помилка;36;10"
        ), StandardCharsets.UTF_8);

        StoreController controller = new StoreController();
        int exitCode = controller.run(new CliOptions(input, output, false, false));

        assertEquals(0, exitCode);
        String report = Files.readString(output, StandardCharsets.UTF_8);
        assertTrue(report.contains("Коректних записів: 3"));
        assertTrue(report.contains("Середня ціна: 18283.50 грн"));
        assertTrue(report.contains("Найдовша гарантія: 24 міс."));
        assertTrue(report.contains("Загальна кількість товарів: 80 шт."));
        assertTrue(report.contains("Помилок: 2"));
        assertTrue(report.contains("Рядок 4:"));
        assertTrue(report.contains("Рядок 5:"));
    }
}