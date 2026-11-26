package ua.lpnu.kzp;

import java.util.Locale;

/**
 * Точка входу консольного застосунку магазину техніки (Варіант 10).
 * Делегує розбір аргументів {@link CliParser} та виконання обробки {@link StoreController}.
 */
public final class Main {

    private Main() {
        // Заборона створення екземплярів службового класу
    }

    /**
     * Запускає програму та завершує її з відповідним кодом стану.
     *
     * @param args аргументи командного рядка
     */
    public static void main(String[] args) {
        CliParser parser = new CliParser();
        StoreController controller = new StoreController();

        int exitCode;
        try {
            CliOptions options = parser.parse(args);
            exitCode = controller.run(options);
        } catch (IllegalArgumentException e) {
            System.err.printf(Locale.ROOT, "Помилка параметрів: %s%n", e.getMessage());
            exitCode = 2;
        }

        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }
}