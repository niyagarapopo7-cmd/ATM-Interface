import java.util.Locale;
import java.util.Scanner;

public final class ConsoleUI {
    public static final String RESET  = "\u001B[0m";
    public static final String BOLD   = "\u001B[1m";
    public static final String RED    = "\u001B[31m";
    public static final String GREEN  = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String CYAN   = "\u001B[36m";
    public static final String GRAY   = "\u001B[90m";
    public static final String WHITE  = "\u001B[97m";

    private static final int WIDTH = 76;

    private ConsoleUI() {}

    public static void clear() {
        System.out.print("\u001B[H\u001B[2J\u001B[3J");
        System.out.flush();
    }

    public static String money(double amount) {
        return String.format(Locale.US, "\u20B9%,.2f", amount);
    }

    private static String fit(String text) {
        if (text.length() > WIDTH - 2) text = text.substring(0, WIDTH - 3) + "…";
        return text;
    }

    public static void boxTop() {
        System.out.println(CYAN + "╔" + "═".repeat(WIDTH) + "╗" + RESET);
    }

    public static void boxBottom() {
        System.out.println(CYAN + "╚" + "═".repeat(WIDTH) + "╝" + RESET);
    }

    public static void boxDivider() {
        System.out.println(CYAN + "╠" + "═".repeat(WIDTH) + "╣" + RESET);
    }

    public static void boxLine(String text, String color) {
        String padded = String.format("%-" + WIDTH + "s", " " + fit(text));
        System.out.println(CYAN + "║" + RESET + color + padded + RESET + CYAN + "║" + RESET);
    }

    public static void boxLine(String text) { boxLine(text, WHITE); }

    public static void boxCenter(String text, String color) {
        text = fit(text);
        int left = (WIDTH - text.length()) / 2;
        String padded = String.format("%-" + WIDTH + "s", " ".repeat(left) + text);
        System.out.println(CYAN + "║" + RESET + color + padded + RESET + CYAN + "║" + RESET);
    }

    /** Clears screen and prints the bank banner + screen title. */
    public static void header(String bankName, String screenTitle) {
        clear();
        boxTop();
        boxCenter("◆  " + bankName.toUpperCase() + "  ATM  ◆", BOLD + YELLOW);
        boxDivider();
        boxCenter(screenTitle, BOLD + WHITE);
        boxBottom();
        System.out.println();
    }

    public static void prompt(String label) {
        System.out.print(CYAN + "  ➤ " + RESET + BOLD + label + RESET + GRAY + " : " + RESET);
    }

    public static void success(String msg) { System.out.println(GREEN + "  ✔ " + msg + RESET); }
    public static void error(String msg)   { System.out.println(RED + "  ✖ " + msg + RESET); }
    public static void info(String msg)    { System.out.println(CYAN + "  ℹ " + msg + RESET); }

    public static void pause(Scanner sc) {
        System.out.print(GRAY + "\n  Press ENTER to continue..." + RESET);
        sc.nextLine();
    }

    public static void loading(String msg) {
        System.out.print(YELLOW + "  " + msg);
        for (int i = 0; i < 3; i++) {
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.out.print(" .");
        }
        System.out.println(RESET);
    }
}