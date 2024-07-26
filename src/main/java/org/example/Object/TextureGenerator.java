package org.example.Object;

import org.example.Components.Transform;
import org.example.Render.Shader;

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

	private Shader shader;
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

	private void init() {
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA);
		glEnable(GL_TEXTURE_2D);

		glPixelStorei(GL_UNPACK_ALIGNMENT,GL_TRUE);

		// Set textures to be pixilated rather than blurred
		glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_S,GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_T,GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_NEAREST);
		glTexEnvf(GL_TEXTURE_ENV,GL_TEXTURE_ENV_MODE,GL_DECAL);

		// Bind texture to slot 0?
		glBindTexture(GL_TEXTURE_2D,0);

		shader = new Shader("assets/shaders/default.glsl");
		shader.compile();

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
			texture_generator.init();
		}
		return texture_generator;
	}

	// Takes width and height of world (pixels that fit on screen), of the object, the object transform and the texture data
	public void generate(int width, int height, short[] texture_data) {
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
	}
}
