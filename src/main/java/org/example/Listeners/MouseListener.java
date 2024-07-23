package org.example.Listeners;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;


public class MouseListener {
	private static MouseListener instance;
	private double scroll_x, scroll_y;
	private double x_pos, y_pos, last_x, last_y;
	private boolean[] mouse_button_pressed = new boolean[3];
	private boolean is_dragging;

	private MouseListener() {
		this.scroll_x = 0.0;
		this.scroll_y = 0.0;
		this.x_pos = 0.0;
		this.y_pos = 0.0;
		this.last_x = 0.0;
		this.last_y = 0.0;
	}

	public static MouseListener get() {
		if (instance == null) {
			instance = new MouseListener();
		}

		return instance;
	}

	public static void mouse_pos_callback(long window, double x_pos, double y_pos) {
		get().last_x = get().x_pos;
		get().last_y = get().y_pos;
		get().x_pos = x_pos;
		get().y_pos = y_pos;
		get().is_dragging = get().mouse_button_pressed[0] || get().mouse_button_pressed[1] || get().mouse_button_pressed[2];
	}

	public static void mouse_button_callback(long window, int button, int action, int mods) {
		// If the button isn't
		if (button < get().mouse_button_pressed.length) { return; }
		if (action == GLFW_PRESS) { get().mouse_button_pressed[button] = true; }
		else if (action == GLFW_RELEASE) { get().mouse_button_pressed[button] = false; get().is_dragging = false; }
	}

	public static void mouse_scroll_callback(long window, double x_offset, double y_offset) {
		get().scroll_x = x_offset;
		get().scroll_y = y_offset;
	}

	public static void end_frame() {
		get().scroll_x = 0;
		get().scroll_y = 0;
		get().last_x = get().x_pos;
		get().last_y = get().y_pos;
	}

	public static float get_x() { return (float)get().x_pos; }
	public static float get_y() { return (float)get().y_pos; }
	public static float get_dx() { return (float)(get().last_x - get().x_pos); }
	public static float get_dy() { return (float)(get().last_y - get().y_pos); }
	public static float get_scroll_x() { return (float)get().scroll_x; }
	public static float get_scroll_y() { return (float)get().scroll_y; }
	public static boolean is_dragging() { return get().is_dragging; }
	public static boolean mouse_down(int button) { return get().mouse_button_pressed[button]; }
}
