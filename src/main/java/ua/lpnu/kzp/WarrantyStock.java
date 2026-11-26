package ua.lpnu.kzp;

/**
 * Незмінний носій гарантійного строку та залишку товару на складі.
 *
 * @param warrantyMonths тривалість гарантії в місяцях (не від'ємна)
 * @param stock кількість одиниць на складі (не від'ємна)
 */
public record WarrantyStock(int warrantyMonths, int stock) {

    /**
     * Перевіряє інваріанти компонентів запису.
     */
    public WarrantyStock {
        if (warrantyMonths < 0 || stock < 0) {
            throw new IllegalArgumentException(
                    "Гарантія та залишок на складі не можуть бути від'ємними");
        }
    }
}