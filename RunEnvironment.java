public class RunEnvironment {
    public static void setup() {
        System.setProperty("sun.jnu.encoding", "UTF-8");
        System.setProperty("input.encoding", "UTF-8");
        System.setProperty("output.encoding", "UTF-8");

        suppressFileChooserWarnings();

        try {
            System.setOut(new java.io.PrintStream(System.out, true, "UTF-8"));
            System.setErr(new java.io.PrintStream(System.err, true, "UTF-8"));

        } catch (java.io.UnsupportedEncodingException e) {
        }

    }

    private static void suppressFileChooserWarnings() {
        System.setProperty("sun.awt.noerasebackground", "true");
        System.setProperty("sun.awt.disableMixing", "true");
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        System.setProperty("awt.useSystemAAFontSettings", "on");
    }
}
