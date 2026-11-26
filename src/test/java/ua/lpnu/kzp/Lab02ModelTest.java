package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class Lab02ModelTest {

    @Test
    void productConstructorStoresFieldsAndAllowsZeroBoundaries() {
        // Arrange & Act
        Product product = new Product("Ноутбук ASUS", "Комп'ютери", 0.0, 0, 0);

        // Assert
        assertEquals("Ноутбук ASUS", product.getName());
        assertEquals("Комп'ютери", product.getCategory());
        assertEquals(0.0, product.getPrice(), 0.0001);
        assertEquals(0, product.getWarrantyMonths());
        assertEquals(0, product.getStock());
        assertEquals(new WarrantyStock(0, 0), product.getWarrantyStock());
    }

    @Test
    void productConstructorRejectsInvalidValues() {
        assertThrows(NullPointerException.class,
                () -> new Product(null, "Екрани", 1000.0, 12, 5));
        assertThrows(IllegalArgumentException.class,
                () -> new Product("   ", "Екрани", 1000.0, 12, 5));
        assertThrows(NullPointerException.class,
                () -> new Product("Монітор", null, 1000.0, 12, 5));
        assertThrows(IllegalArgumentException.class,
                () -> new Product("Монітор", "", 1000.0, 12, 5));
        assertThrows(IllegalArgumentException.class,
                () -> new Product("Монітор", "Екрани", -1.0, 12, 5));
        assertThrows(IllegalArgumentException.class,
                () -> new Product("Монітор", "Екрани", Double.NaN, 12, 5));
        assertThrows(IllegalArgumentException.class,
                () -> new Product("Монітор", "Екрани", Double.POSITIVE_INFINITY, 12, 5));
        assertThrows(IllegalArgumentException.class,
                () -> new Product("Монітор", "Екрани", 1000.0, -3, 5));
        assertThrows(IllegalArgumentException.class,
                () -> new Product("Монітор", "Екрани", 1000.0, 12, -2));
    }

    @Test
    void fromCsvParsesValidLineAndRejectsMalformedLines() {
        // Arrange
        String validLine = "  Смартфон  ;  Мобільні пристрої  ;  18999.99  ;  24  ;  10  ";

        // Act
        Product product = Product.fromCsv(validLine);

        // Assert
        assertEquals("Смартфон", product.getName());
        assertEquals("Мобільні пристрої", product.getCategory());
        assertEquals(18999.99, product.getPrice(), 0.0001);
        assertEquals(24, product.getWarrantyMonths());
        assertEquals(10, product.getStock());

        assertThrows(IllegalArgumentException.class,
                () -> Product.fromCsv("Лише;Чотири;Поля;100"));
        assertThrows(IllegalArgumentException.class,
                () -> Product.fromCsv("Товар;Категорія;не_число;12;5"));
        assertThrows(IllegalArgumentException.class,
                () -> Product.fromCsv("Товар;Категорія;-50.0;12;5"));
    }

    @Test
    void toStringUsesDotDecimalSeparatorRegardlessOfLocale() {
        Product product = new Product("Миша", "Периферія", 1250.5, 12, 8);
        String text = product.toString();

        assertTrue(text.contains("1250.50"));
        assertFalse(text.contains("1250,50"));
    }

    @Test
    void recordsSupportValueEqualityAndRejectNegativeValues() {
        WarrantyStock ws1 = new WarrantyStock(24, 15);
        WarrantyStock ws2 = new WarrantyStock(24, 15);
        assertEquals(ws1, ws2);
        assertEquals(ws1.hashCode(), ws2.hashCode());
        assertThrows(IllegalArgumentException.class, () -> new WarrantyStock(-1, 5));
        assertThrows(IllegalArgumentException.class, () -> new WarrantyStock(12, -1));

        StoreSummary s1 = new StoreSummary(2, 1500.0, 24, 20);
        StoreSummary s2 = new StoreSummary(2, 1500.0, 24, 20);
        assertEquals(s1, s2);
        assertThrows(IllegalArgumentException.class, () -> new StoreSummary(-1, 0.0, 0, 0));
    }

    @Test
    void cliParserParsesFlagsAndRejectsUnknownOption() {
        CliParser parser = new CliParser();

        CliOptions parsed = parser.parse(new String[]{"--input", "a.csv", "--output", "b.txt"});
        assertEquals(Path.of("a.csv"), parsed.inputPath());
        assertEquals(Path.of("b.txt"), parsed.outputPath());
        assertFalse(parsed.showHelp());
        assertFalse(parsed.showVersion());

        assertTrue(parser.parse(new String[]{"--help"}).showHelp());
        assertTrue(parser.parse(new String[]{"--version"}).showVersion());
        assertThrows(IllegalArgumentException.class, () -> parser.parse(new String[]{"--unknown"}));
        assertThrows(IllegalArgumentException.class, () -> parser.parse(new String[]{"--input"}));
    }

    @Test
    void controllerProcessesValidLinesSkipsInvalidAndMatchesExpectedReport(@TempDir Path tempDir)
            throws IOException {
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
        assertTrue(Files.exists(output));

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