public final class Logger {

    private Logger() {
        throw new AssertionError();
    }

    public static void info(String message) {
        System.out.printf("[INFO] %s%n", message);
    }

    public static void warning(String message) {
        System.out.printf("[WARN] %s%n", message);
    }

    public static void error(String message) {
        System.out.printf("[ERROR] %s%n", message);
    }

}
