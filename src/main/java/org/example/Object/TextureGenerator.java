package org.example.Object;

import static java.lang.Math.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_UNSIGNED_SHORT_5_5_5_1;

public class TextureGenerator {

	private static TextureGenerator texture_generator = null;

	private TextureGenerator() {

	}

	private void init() {
		glEnable(GL_TEXTURE_2D);

		glPixelStorei(GL_UNPACK_ALIGNMENT,GL_TRUE);

		// Set textures to be pixilated rather than blurred
		glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_S,GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_T,GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_NEAREST);
		glTexEnvf(GL_TEXTURE_ENV,GL_TEXTURE_ENV_MODE,GL_DECAL);

		// Bind texture to slot 0?
		glBindTexture(GL_TEXTURE_2D,0);
	}

	public static TextureGenerator get() {
		if (texture_generator == null) {
			texture_generator = new TextureGenerator();
			texture_generator.init();
		}
		return texture_generator;
	}

	// Takes width and height of world (pixels that fit on screen), of the object, the object transform and the texture data
	public void draw(double world_width, double world_height, double world_scale, int width, int height, Transform transform, short[] texture_data) {

		if (width * height != texture_data.length) {
			throw new Error("Texture generation error: invalid texture dimensions for array size");
		}

		double sprite_width = world_width / width;
		double sprite_height = world_height / height;

		double small_angle = atan((double)height / (double)width);
		double big_angle = (PI * 0.5d) - small_angle;

		double rotation = small_angle + (double)transform.angle;
		double magnitude = sqrt(world_width*world_width + world_height*world_height);

		// Takes height and width in world pixels, applies the transform and draws the texture
		glTexImage2D(
				GL_TEXTURE_2D,
				0,
				GL_RGB5_A1,
				width,
				height,
				GL_FALSE,
				GL_RGBA,
				GL_UNSIGNED_SHORT_5_5_5_1,
				texture_data
		);

		glBegin(GL_QUADS);

		// Draw the vertices of the quad
		System.out.println("Quad: small [" + small_angle + "] big : [" + big_angle + "]");
		for (int i = 0; i < 4; i++) {
			glTexCoord2f((float)(((i+1)/2)%2),(float)(i/2));
			glVertex3d(
					transform.x / world_scale + (cos(rotation) * height * world_scale / magnitude),
					transform.y / world_scale + (sin(rotation) * width * world_scale / magnitude),
					0.0f
			);

			System.out.println(rotation);
			switch (i%2) {
				case 0:
					rotation += 2 * big_angle;
					break;
				case 1:
					rotation += 2 * small_angle;
					break;
				default:
					break;
			}
		}
		System.out.println(rotation+small_angle);
		glEnd();
	}
}
