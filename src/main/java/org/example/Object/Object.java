package org.example.Object;

// A quad with a procedural texture, which is simulated as both a rigid-body and in
// The powder sim.
public class Object {
	private Transform transform;
	private int width;
	private int height;
	private int scale;

	public Object(Transform transform, int width, int height, int scale) {
		this.transform = transform;
		this.width = width;
		this.height = height;
		this.scale = scale;
	}

}
