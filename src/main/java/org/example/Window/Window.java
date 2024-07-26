package org.example.Window;

import org.example.Listeners.KeyListener;
import org.example.Listeners.MouseListener;
import org.example.Util.Time;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.Objects;

import org.lwjgl.BufferUtils.*;

import static java.awt.SystemColor.window;
import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
	private long glfw_window;
	public int width, height;
	public String title;

	private float r,g,b,a;

	private float begin_time = Time.get_time();
	private float end_time;
	private float dt = -1.0f;

	private static Window window = null;

	private Window() {

	}

	public static Window get() {
		if (window == null) {
			window = new Window();
		}
		return window;
	}

	public void init(int width, int height, String title) {
		this.width = width;
		this.height = height;
		this.title = title;

		this.r = 0;
		this.g = 0;
		this.b = 0;
		this.a = 1;

		GLFWErrorCallback.createPrint(System.err).set();

		// Initialize GLFW. Most GLFW functions will not work before doing this.
		if ( !glfwInit() ) {throw new IllegalStateException("Unable to initialize GLFW"); }

		// Configure GLFW
		glfwDefaultWindowHints(); // optional, the current window hints are already the default
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

		// Create the window
		glfw_window = glfwCreateWindow(this.width, this.height, this.title, NULL, NULL);
		if ( glfw_window == NULL ) { throw new RuntimeException("Failed to create the GLFW window"); }

		// Set up a key callback. It will be called every time a key is pressed, repeated or released.
		glfwSetKeyCallback(glfw_window, (window, key, scancode, action, mods) -> {
			if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE ) {
				glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
			}
		});

		// Get the thread stack and push a new frame
		try ( MemoryStack stack = stackPush() ) {
			IntBuffer pWidth = stack.mallocInt(1); // int*
			IntBuffer pHeight = stack.mallocInt(1); // int*

			// Get the window size passed to glfwCreateWindow
			glfwGetWindowSize(glfw_window, pWidth, pHeight);

			// Get the resolution of the primary monitor
			GLFWVidMode video_mode = glfwGetVideoMode(glfwGetPrimaryMonitor());

			if (video_mode == null) { throw new Error("Video mode error"); }

			// Center the window
			glfwSetWindowPos(
					glfw_window,
					(video_mode.width() - pWidth.get(0)) / 2,
					(video_mode.height() - pHeight.get(0)) / 2
			);
		} // the stack frame is popped automatically

		// Make the OpenGL context current
		glfwMakeContextCurrent(glfw_window);
		// Enable v-sync
		glfwSwapInterval(1);

		glfwSetCursorPosCallback(glfw_window, MouseListener::mouse_pos_callback);
		glfwSetMouseButtonCallback(glfw_window, MouseListener::mouse_button_callback);
		glfwSetScrollCallback(glfw_window, MouseListener::mouse_scroll_callback);
		glfwSetKeyCallback(glfw_window, KeyListener::key_callback);

		// Make the window visible
		glfwShowWindow(glfw_window);
		GL.createCapabilities();

		glEnable(GL_DEPTH_TEST);
		glEnable(GL_ALPHA_TEST);
		glAlphaFunc(GL_GREATER,0.5f);
	}

	public long get_glfw_window() { return glfw_window; }

	public float update() {
		glfwPollEvents();

		end_time = Time.get_time();
		dt = end_time - begin_time;
		begin_time = end_time;
		return dt;
	}

	public boolean should_close() {
		return glfwWindowShouldClose(this.glfw_window) ||
				KeyListener.is_key_pressed(GLFW_KEY_ESCAPE);
	}

	public void clear() {
		glClearColor(this.r,this.g,this.b,this.a);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the frame buffer
	}
	public void draw() {
		glfwSwapBuffers(this.glfw_window);
	}
	public void delete() {
		glfwFreeCallbacks(this.glfw_window);
		glfwDestroyWindow(this.glfw_window);
		// Terminate GLFW and free the error callback
		glfwTerminate();
		Objects.requireNonNull(glfwSetErrorCallback(null)).free();
	}
}
