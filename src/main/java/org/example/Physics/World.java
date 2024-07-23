package org.example.Physics;

import org.example.Object.TextureGenerator;
import org.example.Object.Transform;

// A collection of objects which are all drawn to the screen
public class World {
	private Object[] objects;
	private int scale;

	private TextureGenerator texture_generator = null;

	public World() {
		this.init();
	}

	private Transform temp_transform;
	private short[] temp_image;

	private int width;
	private int height;

	public void init() {
		this.width = 50;
		this.height = 50;
		this.texture_generator = TextureGenerator.get();
		this.temp_transform = new Transform(0,0,0);
		temp_image = new short[width * height * 3];
		temp_image[0] = (short)30000;
		temp_image[4] = (short)30000;
		temp_image[8] = (short)30000;
	}


	public void draw() {
		this.texture_generator.draw(width,height,this.temp_transform,temp_image);
	}

	public void update() {

	}
}
