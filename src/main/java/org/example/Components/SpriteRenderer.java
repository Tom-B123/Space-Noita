package org.example.Components;

import org.example.Object.Component;
import org.example.Render.Camera;
import org.example.Render.Shader;
import org.example.Util.Time;
import org.joml.Vector2f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static java.lang.Math.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class SpriteRenderer extends Component {

	// Centre point of the vertex data
	private double[] vertex_origin = { 0.5, 0.5 };
	private double half_diagonal = sqrt(2) / 2;
	private double half_PI = PI / 2;


	private float[] vertex_array = {
		1f,   0f, 0.0f,       		1.0f, 0.0f, 0.0f, 1.0f,     1, 1, // Bottom right 0
		0f, 1f, 0.0f,      		 	0.0f, 1.0f, 0.0f, 1.0f,     0, 0, // Top left     1
		1f, 1f, 0.0f ,  	    	1.0f, 0.0f, 1.0f, 1.0f,     1, 0, // Top right    2
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

	private int position_size;
	private int colour_size;
	private int uv_size;
	private int vertex_size_bytes;

	@Override
	public void init() {
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

		position_size = 3;
		colour_size = 4;
		uv_size = 2;
		vertex_size_bytes = (position_size + colour_size + uv_size) * Float.BYTES;

		glVertexAttribPointer(0,position_size,GL_FLOAT, false ,vertex_size_bytes, 0);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(1,position_size,GL_FLOAT, false ,vertex_size_bytes, position_size * Float.BYTES);
		glEnableVertexAttribArray(1);
		glVertexAttribPointer(2,position_size,GL_FLOAT, false ,vertex_size_bytes, (position_size + colour_size) * Float.BYTES);
		glEnableVertexAttribArray(2);
	}


	private double get_angle(double x, double y) {
		if (x >= 0 && y >= 0)  return atan(y/x);
		if (x < 0 && y >= 0)   return half_PI + atan(-x/y);
		if (x < 0 && y < 0)  return PI + atan(y/x);
		return half_PI + PI + atan(-x/y);
	}

	// Scale the quad around the central point
	public void scale(float scale_x, float scale_y) {
		for (int i = 0; i < 4; i++) {
			vertex_array[i * (position_size + colour_size + uv_size)] -=		vertex_origin[0];
			vertex_array[1 + i * (position_size + colour_size + uv_size)] -=	vertex_origin[1];

			vertex_array[i * (position_size + colour_size + uv_size)] *= 	scale_x;
			vertex_array[1 + i * (position_size + colour_size + uv_size)] *= scale_y;

			vertex_array[i * (position_size + colour_size + uv_size)] +=		vertex_origin[0];
			vertex_array[1 + i * (position_size + colour_size + uv_size)] +=	vertex_origin[1];
		}
	}

	// Rotate the quad around the central point
	public void rotate(float angle) {
		float cos_theta = (float)cos(angle);
		float sin_theta = (float)sin(angle);
		for (int i = 0; i < 4; i++) {
			vertex_array[i * (position_size + colour_size + uv_size)] -=		vertex_origin[0];
			vertex_array[1 + i * (position_size + colour_size + uv_size)] -=	vertex_origin[1];

			float x = vertex_array[i * (position_size + colour_size + uv_size)];
			float y = vertex_array[1 + i * (position_size + colour_size + uv_size)];
			vertex_array[i * (position_size + colour_size + uv_size)] = 	x * cos_theta - y * sin_theta;
			vertex_array[1 + i * (position_size + colour_size + uv_size)] =	x * sin_theta + y * cos_theta;

			vertex_array[i * (position_size + colour_size + uv_size)] +=		vertex_origin[0];
			vertex_array[1 + i * (position_size + colour_size + uv_size)] +=	vertex_origin[1];
		}
	}

	@Override
	public void update(float dt) {
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

		position_size = 3;
		colour_size = 4;
		uv_size = 2;
		vertex_size_bytes = (position_size + colour_size + uv_size) * Float.BYTES;

		glVertexAttribPointer(0,position_size,GL_FLOAT, false ,vertex_size_bytes, 0);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(1,position_size,GL_FLOAT, false ,vertex_size_bytes, position_size * Float.BYTES);
		glEnableVertexAttribArray(1);
		glVertexAttribPointer(2,position_size,GL_FLOAT, false ,vertex_size_bytes, (position_size + colour_size) * Float.BYTES);
		glEnableVertexAttribArray(2);
	}

	public void draw(Shader shader, Camera camera, float cell_size) {
		shader.use();

		shader.upload_texture("TEX_SAMPLER", 0);
		glActiveTexture(GL_TEXTURE0);

		glBindVertexArray(vao);

		shader.upload_mat4f("u_projection",camera.get_projection_matrix());
		shader.upload_mat4f("u_view",camera.get_view_matrix());
		shader.upload_float("u_time", Time.get_time());
		shader.upload_vec2f("u_transform", (float)this.object.get_transform().x, (float)this.object.get_transform().y);
		shader.upload_vec3f("u_scale", cell_size,(float)this.object.get_width(),(float)this.object.get_height());
		shader.upload_vec3f("u_rotation", (float)this.object.get_transform().angle,(float)this.object.get_transform().x, (float)this.object.get_transform().y);

		glDrawElements(GL_TRIANGLES,element_array.length,GL_UNSIGNED_INT,0);

	}
}
