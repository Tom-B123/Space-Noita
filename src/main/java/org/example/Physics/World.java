package org.example.Physics;

import org.example.Components.SpriteRenderer;
import org.example.Components.ParticleUpdate;
import org.example.Object.TextureGenerator;
import org.example.Components.Transform;
import org.example.Object.Object;
import org.example.Render.Camera;
import org.example.Render.Shader;
import org.joml.Vector2f;
import org.lwjgl.BufferUtils;

import java.nio.IntBuffer;
import java.util.Vector;

import static java.lang.Math.PI;
import static java.lang.Math.random;
import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.opengl.GL11.glReadPixels;

// A collection of objects which are all drawn to the screen
public class World {
	public Vector<Object> objects = new Vector<>();
	private float scale;

	private TextureGenerator texture_generator = null;
	private Camera camera;
	private Shader shader;
	private Graph graph;

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

		this.camera = new Camera(new Vector2f(0.0f,0.0f));
		shader = new Shader("assets/shaders/default.glsl");
		shader.compile();

		graph = new Graph(10,2,1920,1080);
		graph.init(this,scale,100.0f,135.0f);

		this.texture_generator.generate(objects.get(0).get_width(),objects.get(0).get_height(),objects.get(0).data);
		for (Object object : this.objects) {
			// Consider drawing threads to send and read data to the GPU.
			object.get_component(SpriteRenderer.class).update(0);
		}
	}

	public void new_object(float x, float y, float angle, int width, int height) {
		Object object = new Object(new Transform(x,y,angle),width,height);
		object.get_component(SpriteRenderer.class).scale((float)width, (float)height);
		object.get_component(SpriteRenderer.class).scale(this.scale,this.scale);
		object.get_component(SpriteRenderer.class).rotate((float)object.get_transform().angle);

		this.objects.add(object);
	}

	public void update(float dt) {
		update_window_dims();
	}

	public void draw() {
		int ind = 0;
		for (Object object : this.objects) {
			// Consider drawing threads to send pixel data to the GPU

			//if (ind == 0) { this.texture_generator.generate("assets/images/default.bmp"); }
			object.get_component(SpriteRenderer.class).draw(shader,camera,scale);
			ind ++;
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
