package org.example.Object;

import org.example.Components.SpriteRenderer;
import org.example.Components.Transform;

import java.util.Vector;

// A quad with a procedural texture, which is simulated as both a rigid-body and in
// The powder sim.
public class Object {
	private Transform transform;
	private int width;
	private int height;
	public short[] data;

	private Vector<Component> components;

	public Object(Transform transform, int width, int height) {
		this.transform = transform;
		this.width = width;
		this.height = height;
		this.data = new short[width*height];

		this.components = new Vector<>();

		add_component(new SpriteRenderer());
		get_component(SpriteRenderer.class).init();

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (y > 3) { set_pixel(x,y,31,31,0,1);}
			}
		}
	}

	private void set_pixel(int x, int y, int r, int g, int b,int a) {
		int index = (x + y * this.width);
		short colour = (short)(a + (b << 1) + (g << 6) + (r << 11));
		this.data[index] = colour;
	}

	public int get_width() { return this.width; }
	public int get_height() { return this.height; }
	public Transform get_transform() { return this.transform; }

	public void rotate(float angle) { this.transform.angle += angle; }

	public <T extends Component> T get_component(Class<T> component_class) {
		for (Component c : components) {
			if (component_class.isAssignableFrom(c.getClass())) {
				try {
					return component_class.cast(c);
				} catch(ClassCastException e) {
					throw new Error("component casting error");
				}
			}
		}
		return null;
	}

	public <T extends Component> void remove_component(Class<T> component_class) {
		for (int i=0; i<components.size(); i++) {
			Component c = components.get(i);
			if (component_class.isAssignableFrom((c.getClass()))) {
				components.remove(i);
				return;
			}
		}
	}

	public void add_component(Component c) {
		this.components.add(c);
		this.get_component(c.getClass()).object = this;
	}

}
