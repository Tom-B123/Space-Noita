package org.example.Physics;

import org.example.Components.SpriteRenderer;
import org.example.Object.ParticleUpdate;
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
	private ParticleUpdate particle_update = null;
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
		this.texture_generator.init();
		this.particle_update = ParticleUpdate.get();
		this.particle_update.init();

		this.camera = new Camera(new Vector2f(0.0f,0.0f));
		shader = new Shader("assets/shaders/default.glsl");
		shader.compile();

		int count = 10;

		this.objects = new Object[count];
		for (int i = 0; i < count; i ++) {
			this.objects[i] = new_object(200 * i,200,i * 0.3f,40,100);
		}
	}

	private Object new_object(float x, float y, float angle, int width, int height) {
		Object object = new Object(new Transform(x,y,angle),width,height);
		object.get_component(SpriteRenderer.class).scale((float)width, (float)height);
		object.get_component(SpriteRenderer.class).scale(this.scale,this.scale);
		object.get_component(SpriteRenderer.class).rotate((float)object.get_transform().angle);
		return object;
	}

	public void update(float dt) {
		update_window_dims();
		for (Object object : this.objects) {
			// Consider drawing threads to send and read data to the GPU.
			object.get_component(SpriteRenderer.class).update(dt);
			object.data = particle_update.update(object.data,object.get_width(),object.get_height());
			//object.translate(30 * dt,0 * dt);
			object.rotate(0.5f * dt);
		}
	}

	public void draw() {
		for (Object object : this.objects) {
			// Consider drawing threads to send pixel data to the GPU
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
