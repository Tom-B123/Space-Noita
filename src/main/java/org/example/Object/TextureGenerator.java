package org.example.Object;

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

	public void draw(int width, int height, Transform transform, short[] texture_data) {

		if (width * height * 3 != texture_data.length) {
			throw new Error("Texture generation error: invalid texture dimensions for array size");
		}

		// Takes height and width in world pixels, applies the transform and draws the texture
		glTexImage2D(
				GL_TEXTURE_2D,
				0,
				GL_RGB,
				width,
				height,
				GL_FALSE,
				GL_RGB,
				GL_UNSIGNED_SHORT,
				texture_data
		);

		//ToDo: apply the transform to the vertex data here
		glBegin(GL_QUADS);
		glTexCoord2f(0.0f,0.0f);
		glVertex3f(-1.0f,-1.0f, 0.0f);
		glTexCoord2f(1.0f,0.0f);
		glVertex3f(1.0f,-1.0f, 0.0f);
		glTexCoord2f(1.0f,1.0f);
		glVertex3f(1.0f,1.0f, 0.0f);
		glTexCoord2f(0.0f,1.0f);
		glVertex3f(-1.0f,1.0f, 0.0f);
		glEnd();
	}
}
