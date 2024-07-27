package org.example.Object;

import org.example.Components.Transform;
import org.example.Render.Shader;
import org.lwjgl.stb.STBTTBitmap;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Scanner;

import static java.lang.Math.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL12.GL_UNSIGNED_SHORT_5_5_5_1;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class TextureGenerator {

	private static TextureGenerator texture_generator = null;

	private final int vertex_size = 3+2;
	private float[] vertices = new float[4 * vertex_size];

	private int vao,vbo;

	private final int POS_SIZE = 2;
	private final int TEX_SIZE = 2;
	private final int POS_OFFSET = 0;
	private final int TEX_OFFSET = POS_OFFSET + POS_SIZE * Float.BYTES;
	private final int VERTEX_SIZE = 4;

	private TextureGenerator() {

	}

	public void init() {
		glEnable(GL_TEXTURE_2D);

		glPixelStorei(GL_UNPACK_ALIGNMENT,2);

		// Set textures to be pixilated rather than blurred
		glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_S,GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_T,GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_NEAREST);
		glTexEnvf(GL_TEXTURE_ENV,GL_TEXTURE_ENV_MODE,GL_DECAL);

		// Bind texture to slot 0?
		glBindTexture(GL_TEXTURE_2D,0);


		vao = glGenVertexArrays();
		glBindVertexArray(vao);

		vbo = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER,vbo);
		glBufferData(GL_ARRAY_BUFFER, vertices.length * Float.BYTES, GL_DYNAMIC_DRAW);

		//int ebo = glGenBuffers();
		//int[] indices = generate_indices();
		//glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
		//glBufferData(GL_ELEMENT_ARRAY_BUFFER,indices,GL_STATIC_DRAW);

		glVertexAttribPointer(0,POS_SIZE,GL_FLOAT, false, VERTEX_SIZE * Float.BYTES, POS_OFFSET);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(1,TEX_SIZE,GL_FLOAT, false, VERTEX_SIZE * Float.BYTES, TEX_OFFSET);
		glEnableVertexAttribArray(1);
	}

	public static TextureGenerator get() {
		if (texture_generator == null) {
			texture_generator = new TextureGenerator();
		}
		return texture_generator;
	}

	// Takes width and height of world (pixels that fit on screen), of the object, the object transform and the texture data
	public void generate(int width, int height, short[] texture_data) {
		if (width * height != texture_data.length) {
			throw new Error("Texture generation error: invalid texture dimensions for array size");
		}

		//glPixelStorei(GL_UNPACK_ROW_LENGTH,width);
		//glPixelStorei(GL_UNPACK_IMAGE_HEIGHT,height);

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
	}
	public void generate(String file_path) {
		try {
			BufferedImage image = ImageIO.read(new File(file_path));
			short[] texture_data = new short[image.getWidth() * image.getHeight()];

			int ind = 0;

			for (int y = 0; y < image.getHeight(); y++) {
				for (int x = 0; x < image.getWidth(); x++) {
					int colour = image.getRGB(x, y);
					// Bright green is interpreted as a transparent pixel, any non 255 shade of green is not
					int a = colour == -16711936 ? 0 : 1;

					// Get the byte long rgb values
					int r = ((colour >> 16) & 255) / 8;
					int g = ((colour >> 8) & 255) / 8;
					int b = ((colour >> 0) & 255) / 8;

					short out = (short) (a + (b << 1) + (g << 6) + (r << 11));
					texture_data[ind] = out;
					ind++;
				}
			}
			this.generate(image.getWidth(), image.getHeight(), texture_data);
		} catch (IOException e) {throw new Error("failed to read texture: " + file_path); }
	}
}
