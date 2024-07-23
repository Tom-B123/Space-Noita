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
		this.width = 89;
		this.height = 50;
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
		this.texture_generator.draw(width,height,this.temp_transform,temp_image);
	}

	public void update() {

	}
}
