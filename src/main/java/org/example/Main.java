package org.example;

import org.example.Physics.World;
import org.example.Window.Window;

public class Main {

    private Window window;
    private World world;

    private void start() {
        this.init();
        this.loop();

        this.window.delete();
    }

    private void init() {
        this.window = new Window();
        this.window.init(1000,1080,"Spoils of War 2");
        this.world = new World();
        this.world.init(window.get_glfw_window());
    }

    private void loop() {
        float dt;
        while (!window.should_close()) {
            this.window.clear();

            dt = this.window.update();

            this.world.update(dt);

            this.world.draw();

            this.window.draw();
        }
    }



    public static void main(String[] args) {
        new Main().start();
    }
}