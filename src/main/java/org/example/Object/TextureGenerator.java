package org.example.Object;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_UNSIGNED_SHORT_5_5_5_1;
import static org.lwjgl.opengl.GL12.GL_UNSIGNED_SHORT_5_6_5;
import static org.lwjgl.opengl.GL41.GL_RGB565;

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
	public void draw(double world_width, double world_height, int width, int height, Transform transform, short[] texture_data) {

		if (width * height != texture_data.length) {
			throw new Error("Texture generation error: invalid texture dimensions for array size");
		}

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

		//ToDo: apply the transform to the vertex data here
		glBegin(GL_QUADS);
		glTexCoord2f(0.0f,0.0f);
		glVertex3d((-1.0 + transform.x),(-1.0 + transform.y), 0.0f);
		glTexCoord2f(1.0f,0.0f);
		glVertex3d(1.0 + transform.x,-1.0 + transform.y, 0.0f);
		glTexCoord2f(1.0f,1.0f);
		glVertex3d(1.0 + transform.x,1.0 + transform.y, 0.0f);
		glTexCoord2f(0.0f,1.0f);
		glVertex3d(-1.0 + transform.x,1.0 + transform.y, 0.0f);
		glEnd();
	}
}
