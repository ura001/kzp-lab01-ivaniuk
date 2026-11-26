package ua.lpnu.kzp;

import java.util.Locale;
import java.util.Objects;

/**
 * Описує незмінний запис про товар у магазині техніки (Варіант 10).
 */
public final class Product {

    private final String name;
    private final String category;
    private final double price;
    private final WarrantyStock warrantyStock;

    /**
     * Створює коректний об'єкт товару після перевірки всіх інваріантів.
     *
     * @param name назва товару
     * @param category категорія товару
     * @param price ціна товару в гривнях
     * @param warrantyMonths гарантія в місяцях
     * @param stock залишок на складі в штуках
     */
    public Product(String name, String category, double price, int warrantyMonths, int stock) {
        this.name = Objects.requireNonNull(name, "Назва товару не може бути null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Назва товару не може бути порожньою");
        }

        this.category = Objects.requireNonNull(category, "Категорія не може бути null");
        if (category.isBlank()) {
            throw new IllegalArgumentException("Категорія не може бути порожньою");
        }

        if (price < 0 || !Double.isFinite(price)) {
            throw new IllegalArgumentException("Ціна має бути скінченною і не від'ємною");
        }

        this.price = price;
        this.warrantyStock = new WarrantyStock(warrantyMonths, stock);
    }

    /**
     * Створює екземпляр товару з CSV-рядка формату:
     * {@code назва;категорія;ціна;гарантія;залишок}.
     *
     * @param line рядок CSV
     * @return створений об'єкт {@link Product}
     * @throws IllegalArgumentException якщо структура рядка, типи або інваріанти некоректні
     */
    public static Product fromCsv(String line) {
        Objects.requireNonNull(line, "Рядок не може бути null");
        String[] fields = line.split(";", -1);
        if (fields.length != 5) {
            throw new IllegalArgumentException("Очікується 5 полів, отримано " + fields.length);
        }

        try {
            String parsedName = fields[0].trim();
            String parsedCategory = fields[1].trim();
            double parsedPrice = Double.parseDouble(fields[2].trim());
            int parsedWarranty = Integer.parseInt(fields[3].trim());
            int parsedStock = Integer.parseInt(fields[4].trim());

            return new Product(parsedName, parsedCategory, parsedPrice, parsedWarranty, parsedStock);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Числове поле має помилковий формат", exception);
        }
    }

    /** Повертає назву товару. */
    public String getName() {
        return name;
    }

    /** Повертає категорію товару. */
    public String getCategory() {
        return category;
    }

    /** Повертає ціну товару. */
    public double getPrice() {
        return price;
    }

    /** Повертає тривалість гарантії в місяцях. */
    public int getWarrantyMonths() {
        return warrantyStock.warrantyMonths();
    }

    /** Повертає кількість одиниць товару на складі. */
    public int getStock() {
        return warrantyStock.stock();
    }

    /** Повертає пару гарантії та залишку у вигляді record. */
    public WarrantyStock getWarrantyStock() {
        return warrantyStock;
    }

    /** Повертає форматоване текстове подання товару. */
    @Override
    public String toString() {
        return String.format(Locale.ROOT,
                "%s (%s): %.2f грн, гарантія %d міс., запас %d шт.",
                name, category, price, getWarrantyMonths(), getStock());
    }
}