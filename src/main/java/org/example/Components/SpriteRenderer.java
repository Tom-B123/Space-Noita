package org.example.Components;

import org.example.Object.Component;
import org.example.Render.Camera;
import org.example.Render.Shader;
import org.example.Util.Time;
import org.joml.Vector2f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class SpriteRenderer extends Component {


	private float[] vertex_array = {
		100f,   0f, 0.0f,       	1.0f, 0.0f, 0.0f, 1.0f,     1, 1, // Bottom right 0
		0f, 100f, 0.0f,      	 	0.0f, 1.0f, 0.0f, 1.0f,     0, 0, // Top left     1
		100f, 100f, 0.0f ,      	1.0f, 0.0f, 1.0f, 1.0f,     1, 0, // Top right    2
		0f,   0f, 0.0f,       		1.0f, 1.0f, 0.0f, 1.0f,     0, 1  // Bottom left  3
	};

	private int[] element_array = {
			/*
					x        x


					x        x
			 */
			2, 1, 0, // Top right triangle
			0, 1, 3 // bottom left triangle
	};

	private int vao, vbo, ebo;

	private FloatBuffer vertex_buffer;

	@Override
	public void init() {
		System.out.println("inited sprite renderer");
		vao = glGenVertexArrays();
		glBindVertexArray(vao);

		vertex_buffer = BufferUtils.createFloatBuffer(vertex_array.length);
		vertex_buffer.put(vertex_array).flip();

		vbo = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER,vbo);
		glBufferData(GL_ARRAY_BUFFER,vertex_buffer,GL_DYNAMIC_DRAW);

		IntBuffer element_buffer = BufferUtils.createIntBuffer(element_array.length);
		element_buffer.put(element_array).flip();

		ebo = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER,ebo);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER,element_buffer,GL_DYNAMIC_DRAW);

		int position_size = 3;
		int colour_size = 4;
		int uv_size = 2;
		int vertex_size_bytes = (position_size + colour_size + uv_size) * Float.BYTES;

		glVertexAttribPointer(0,position_size,GL_FLOAT, false ,vertex_size_bytes, 0);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(1,position_size,GL_FLOAT, false ,vertex_size_bytes, position_size * Float.BYTES);
		glEnableVertexAttribArray(1);
		glVertexAttribPointer(2,position_size,GL_FLOAT, false ,vertex_size_bytes, (position_size + colour_size) * Float.BYTES);
		glEnableVertexAttribArray(2);
	}

	@Override
	public void update(float dt) {
		vertex_array[1] += 10;
	}

	public void draw(Shader shader, Camera camera) {
		shader.use();

		shader.upload_texture("TEX_SAMPLER", 0);
		glActiveTexture(GL_TEXTURE0);

		glBindVertexArray(vao);
		//vertex_buffer.put(vertex_array).flip();

		shader.upload_mat4f("u_projection",camera.get_projection_matrix());
		shader.upload_mat4f("u_view",camera.get_view_matrix());
		shader.upload_float("u_time", Time.get_time());
		shader.upload_vec2f("u_transform", (float)this.object.get_transform().x, (float)this.object.get_transform().y);

		glDrawElements(GL_TRIANGLES,element_array.length,GL_UNSIGNED_INT,0);

	}
}
