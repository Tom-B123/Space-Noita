package org.example;

import org.example.Physics.World;
import org.example.Util.Time;
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
        this.window = Window.get();
        this.window.init(1920,1080,"Spoils of War 2");
        this.world = new World();
        this.world.init(window.get_glfw_window());
    }

    private int step = 0;

    float start;
    float window_update;
    float world_update;
    float world_draw;
    float window_draw;

    private void loop() {
        float dt;
        while (!window.should_close()) {
            this.window.clear();

            start = Time.get_time();

            dt = this.window.update();

            step++;

            window_update = Time.get_time();

            this.world.update(dt);

            world_update = Time.get_time();

            this.world.draw();

            world_draw = Time.get_time();

            this.window.draw();

            window_draw = Time.get_time();

            if (step % 10 == 9) { System.out.println((window_update - start) + "," + (world_update - start) + "," + (world_draw - start) + "," + (window_draw - start)); }
        }
    }



    public static void main(String[] args) {
        new Main().start();
    }
}