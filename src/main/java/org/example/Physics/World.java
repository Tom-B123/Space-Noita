package org.example.Physics;

import org.example.Object.TextureGenerator;
import org.example.Object.Transform;
import org.example.Object.Object;
import org.lwjgl.BufferUtils;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;

// A collection of objects which are all drawn to the screen
public class World {
	private Object[] objects;
	private double scale;

	private TextureGenerator texture_generator = null;

	private double world_width = 0;
	private double world_height = 0;

	private long glfw_window;

	public World() {
	}

	public void init(long glfw_window) {
		this.scale = 4d;

		this.glfw_window = glfw_window;

		update_window_dims();

		this.texture_generator = TextureGenerator.get();
		this.objects = new Object[] {
				new Object(new Transform(-1,0,0),30,50),
				new Object(new Transform(0,0,0),30,50),
				new Object(new Transform(1,0,0),30,50),
		};
	}


	public void update(float dt) {
		update_window_dims();
		for (Object object : this.objects) {
			//object.rotate(0.01f);
		}
	}

	public void draw() {
		for (Object object : this.objects) {
			this.texture_generator.draw(world_width,world_height,this.scale,object.get_width(),object.get_height(),object.get_transform(),object.data);
		}
	}

	private void update_window_dims() {
		IntBuffer w = BufferUtils.createIntBuffer(1);
		IntBuffer h = BufferUtils.createIntBuffer(1);
		glfwGetWindowSize(this.glfw_window, w, h);
		this.world_width = w.get(0);
		this.world_height = h.get(0);
	}
}
