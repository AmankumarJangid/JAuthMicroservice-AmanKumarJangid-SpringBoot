package com.gamegrind.dev.AuthApplication;

public class MainTestingUnit {
    private enum TEST{ CHECK , NOT_CHECKED};
    static void main() {
        TEST test = TEST.CHECK;
        System.out.println( test.toString());
    }
}
