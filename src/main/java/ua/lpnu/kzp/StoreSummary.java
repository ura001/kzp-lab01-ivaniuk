package ua.lpnu.kzp;

/**
 * Незмінний набір підсумкових статистичних показників магазину техніки.
 *
 * @param validCount кількість коректно оброблених товарів
 * @param averagePrice середня ціна товару
 * @param maxWarranty найдовша гарантія в місяцях
 * @param totalStock загальна кількість одиниць товару на складі
 */
public record StoreSummary(
        int validCount,
        double averagePrice,
        int maxWarranty,
        int totalStock) {

    /**
     * Перевіряє коректність підсумкових показників.
     */
    public StoreSummary {
        if (validCount < 0 || averagePrice < 0 || !Double.isFinite(averagePrice)
                || maxWarranty < 0 || totalStock < 0) {
            throw new IllegalArgumentException("Підсумкові показники не можуть бути від'ємними");
        }
    }
}