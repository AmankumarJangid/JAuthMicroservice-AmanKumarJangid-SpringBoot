//package com.gamegrind.dev.AuthApplication;
//
//public class ConsoleColorCapabitities {
//    static void main() throws InterruptedException {
//        System.out.println("\n============================================================");
//        System.out.println(" 🛠️  ULTIMATE JAVA TERMINAL UI STYLE & COLOR TESTER");
//        System.out.println("============================================================\n");
//
//        // -------------------------------------------------------------------------
//        // 1. BASE TEXT FORMATTING STYLES
//        // -------------------------------------------------------------------------
//        System.out.println("🔹 [1/4] TEXT RENDERING STYLES");
//        System.out.println("-----------------------------------");
//
//        String[][] styles = {
//                {"Normal", "0"},
//                {"Bold / Bright", "1"},
//                {"Dim / Faint", "2"},
//                {"Italic", "3"},
//                {"Underline", "4"},
//                {"Blink (Slow)", "5"},
//                {"Invert Colors", "7"},
//                {"Hidden / Strike", "9"}
//        };
//
//        for (String[] style : styles) {
//            System.out.printf("%-20s -> \u001b[%smSample Text\u001b[0m  (Code: \\u001b[%sm)%n",
//                    style[0], style[1], style[1]);
//        }
//        System.out.println();
//
//        // -------------------------------------------------------------------------
//        // 2. STANDARD 16-COLOR MATRIX (Foreground & Background)
//        // -------------------------------------------------------------------------
//        System.out.println("🔹 [2/4] STANDARD 16-COLOR MATRIX");
//        System.out.println("-----------------------------------");
//
//        String[] colorNames = {"Black", "Red", "Green", "Yellow", "Blue", "Magenta", "Cyan", "White"};
//
//        System.out.printf("%-12s | %-22s | %-20s%n", "Color Name", "Foreground (Text)", "Background Block");
//        System.out.println("------------------------------------------------------------");
//
//        for (int i = 0; i < colorNames.length; i++) {
//            String fgStandard = "3" + i;
//            String fgBright = "9" + i;
//            String bgStandard = "4" + i;
//            String bgBright = "10" + i;
//
//            // Generate strings containing live colors paired with code representations
//            String fgSample = String.format("\u001b[%smText\u001b[0m / \u001b[%smBright\u001b[0m", fgStandard, fgBright);
//            String bgSample = String.format("\u001b[%sm  \u001b[0m \u001b[%sm  \u001b[0m", bgStandard, bgBright);
//
//            System.out.printf("%-12s | %-33s | %s%n", colorNames[i], fgSample, bgSample);
//        }
//        System.out.println();
//
//        // -------------------------------------------------------------------------
//        // 3. 256-EXTENDED COLOR PALETTE GRID
//        // -------------------------------------------------------------------------
//        System.out.println("🔹 [3/4] 256-COLOR EXTENDED PALETTE (XTerm)");
//        System.out.println("-----------------------------------");
//        System.out.println("System Colors (0-15):");
//        for (int i = 0; i < 16; i++) {
//            System.out.printf("\u001b[48;5;%dm %-3d\u001b[0m", i, i);
//            if ((i + 1) % 8 == 0) System.out.println();
//        }
//
//        System.out.println("\nColor Cube Loops (16-231):");
//        for (int i = 16; i < 232; i++) {
//            System.out.printf("\u001b[48;5;%dm %-3d\u001b[0m", i, i);
//            if ((i - 15) % 36 == 0) {
//                System.out.println();
//            } else if ((i - 15) % 6 == 0) {
//                System.out.print("  "); // Visual spacer layout block
//            }
//        }
//
//        System.out.println("\n\nGrayscale Ramp (232-255):");
//        for (int i = 232; i < 256; i++) {
//            System.out.printf("\u001b[48;5;%dm %-3d\u001b[0m", i, i);
//        }
//        System.out.println("\n\n");
//
//        // -------------------------------------------------------------------------
//        // 4. TRUECOLOR 24-BIT RGB SPECTRUM GRADIENT
//        // -------------------------------------------------------------------------
//        System.out.println("🔹 [4/4] 24-BIT TRUECOLOR (RGB GRADIENT RAMP)");
//        System.out.println("-----------------------------------");
//
//        double steps = 400.0;
//        for (int i = 0; i < steps; i++) {
//            // Trigonometric / wave interpolation math for smooth red-green-blue shading transition
//            int r = (int) Math.clamp((1 - Math.abs(i - 0) / (steps / 3.0)) * 255, 0, 255);
//            int g = (int) Math.clamp((1 - Math.abs(i - steps / 3.0) / (steps / 3.0)) * 255, 0, 255);
//            int b = (int) Math.clamp((1 - Math.abs(i - 2 * steps / 3.0) / (steps / 3.0)) * 255, 0, 255);
//
//            System.out.printf("\u001b[48;2;%d;%d;%dm \u001b[0m", r, g, b);
//        }
//
//        System.out.println("\n\n\u001b[1;32m✔ Java Matrix Test Completed Successfully!\u001b[0m\n");
//
//
//        String[] spinner = {"⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"};
//        String cyan = "\u001b[36m";
//        String dim = "\u001b[2m";
//        String reset = "\u001b[0m";
//
//        System.out.println("🤖 " + cyan + "Claude Agent activated." + reset);
//
//        // Hide terminal cursor
//        System.out.print("\u001b[?25l");
//
//        for (int i = 0; i < 40; i++) {
//            // \r pulls the cursor back to start of line, avoiding printing new lines
//            System.out.print("\r" + cyan + spinner[i % spinner.length] + " " + reset + "Thinking... " + dim + "Analyzing AST tree (step " + i + "/40)" + reset);
//            System.out.flush();
//            Thread.sleep(80); // Fast heartbeat
//        }
//
//        // Clean up the line and show complete state
//        System.out.print("\r\u001b[2K"); // Erase the spinner line completely
//        System.out.println("✨ \u001b[32mAnalysis complete! Found 0 errors.\u001b[0m\n");
//
//        // Restore terminal cursor
//        System.out.print("\u001b[?25h");
//
//    }
//}
