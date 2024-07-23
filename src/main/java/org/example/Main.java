package org.example;

import org.example.Window.Window;

public class Main {

    private Window window;

    private void start() {
        this.init();
        this.loop();

        this.window.delete();
    }

    private void init() {
        this.window = new Window(1920,1080,"Spoils of War 2");
    }

    private void loop() {
        while (!window.should_close()) {
            window.clear();
            window.update();
            window.draw();
        }
    }



    public static void main(String[] args) {
        new Main().start();
    }
}