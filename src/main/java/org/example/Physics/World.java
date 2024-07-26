package org.example.Physics;

import org.example.Components.SpriteRenderer;
import org.example.Object.TextureGenerator;
import org.example.Components.Transform;
import org.example.Object.Object;
import org.example.Render.Camera;
import org.example.Render.Shader;
import org.joml.Vector2f;
import org.lwjgl.BufferUtils;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.glReadPixels;
import static org.lwjgl.opengl.GL12.GL_UNSIGNED_SHORT_5_5_5_1;

// A collection of objects which are all drawn to the screen
public class World {
	private Object[] objects;
	private float scale;

	private TextureGenerator texture_generator = null;
	private Camera camera;
	private Shader shader;

	private double world_width = 0;
	private double world_height = 0;

	private long glfw_window;

	public World() {
	}

	public void init(long glfw_window) {
		this.scale = 4;

		this.glfw_window = glfw_window;

		update_window_dims();

		this.texture_generator = TextureGenerator.get();
		this.camera = new Camera(new Vector2f(0.0f,0.0f));
		shader = new Shader("assets/shaders/default.glsl");
		shader.compile();

		int count = 30;

		this.objects = new Object[count];
		for (int i = 0; i < count; i ++) {
			this.objects[i] = new Object(new Transform(3 * i * i,0,i * 0.3f),i,i);
		}
	}


	public void update(float dt) {
		update_window_dims();
		for (Object object : this.objects) {
			object.get_component(SpriteRenderer.class).update(dt);
			object.translate(30 * dt,10 * dt);
			object.rotate(0.2f);
		}
	}

	public void draw() {
		for (Object object : this.objects) {
			this.texture_generator.generate(object.get_width(),object.get_height(),object.data);
			object.get_component(SpriteRenderer.class).draw(shader,camera,scale);
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
