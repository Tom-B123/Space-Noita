package org.example.Physics;

import org.example.Object.TextureGenerator;
import org.example.Object.Transform;
import org.lwjgl.BufferUtils;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;

// A collection of objects which are all drawn to the screen
public class World {
	private Object[] objects;
	private int scale;

	private TextureGenerator texture_generator = null;


	private Transform temp_transform;
	private short[] temp_image;

	private int width;
	private int height;

	private double world_width = 0;
	private double world_height = 0;

	private long glfw_window;

	public World() {
	}

	public void init(long glfw_window) {
		this.width = 32;
		this.height = 32;

		this.glfw_window = glfw_window;

		update_window_dims();

		this.texture_generator = TextureGenerator.get();
		this.temp_transform = new Transform(0,0,0);
		temp_image = new short[width * height];
		for (int y = 0; y < 32; y++) {
			for (int x = 0; x < 32; x++) {
				set_pixel(x,y,x,(x+y)/2,y,1);
			}
		}
	}

	private void set_pixel(int x, int y, int r, int g, int b,int a) {
		int index = (x + y * this.width);
		short colour = (short)(a + (b << 1) + (g << 6) + (r << 11));
		this.temp_image[index] = colour;
	}


	public void draw() {
		this.texture_generator.draw(world_width,world_height,width,height,this.temp_transform,temp_image);
	}

	private void update_window_dims() {
		IntBuffer w = BufferUtils.createIntBuffer(1);
		IntBuffer h = BufferUtils.createIntBuffer(1);
		glfwGetWindowSize(this.glfw_window, w, h);
		this.world_width = w.get(0);
		this.world_height = h.get(0);
	}

	public void update(float dt) {
		this.temp_transform.x += dt * 0.2;

		update_window_dims();

	}
}
