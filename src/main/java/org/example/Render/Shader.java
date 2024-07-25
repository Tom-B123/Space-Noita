package org.example.Render;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;

public class Shader {

	private int shader_program_id;

	private String vertex_source;
	private String fragment_source;
	final private String filepath;

	public Shader(String filepath) {
		this.filepath = filepath;
		try {
			String source = new String(Files.readAllBytes(Paths.get(filepath)));
			// Regex finds [#type][whitespace][word]
			String[] split_source = source.split("(#type)( )+([a-zA-z]+)");

			// Get index at start of #type, then + 6 to get index after "#type "
			int index = source.indexOf("#type") + 6;
			// Get end of line after "#type "
			int eol = source.indexOf("\r\n", index);
			// Get the string between "#type " and the end of line, removing whitespace with trim
			String firstPattern = source.substring(index,eol).trim();

			// Get index after the next "#type " by looking after the line end
			index = source.indexOf("#type", eol) + 6;
			// Get end of line
			eol = source.indexOf("\r\n", index);
			// Set second string
			String secondPattern = source.substring(index,eol).trim();

			if (firstPattern.equals("vertex")) { vertex_source = split_source[1]; }
			else if (firstPattern.equals("fragment")) { fragment_source = split_source[1]; }
			else { throw new IOException("Unexpected [" + firstPattern + "]"); }

			if (secondPattern.equals("vertex")) { vertex_source = split_source[2]; }
			else if (secondPattern.equals("fragment")) { fragment_source = split_source[2]; }
			else { throw new IOException("Unexpected [" + secondPattern + "]"); }

		} catch(IOException e) {
			throw new Error("The shader file: " + filepath + " could not be opened");
		}

	}

	public void compile() {
		GL.createCapabilities();

		int vertex_id,fragment_id;
		vertex_id = glCreateShader(GL_VERTEX_SHADER);

		glShaderSource(vertex_id, vertex_source);
		glCompileShader(vertex_id);

		int success = glGetShaderi(vertex_id, GL_COMPILE_STATUS);
		if (success == GL_FALSE) {
			int len = glGetShaderi(vertex_id, GL_INFO_LOG_LENGTH);
			System.out.println("ERROR: Vertex shader failed to compile from [" + filepath + "]");
			System.out.println("Vertex shader contents were: " + vertex_source);
			System.out.println(glGetShaderInfoLog(vertex_id, len));
			throw new Error("main.java.Render.Shader Compilation failed");
		}

		fragment_id = glCreateShader(GL_FRAGMENT_SHADER);

		glShaderSource(fragment_id,fragment_source);
		glCompileShader(fragment_id);

		success = glGetShaderi(fragment_id, GL_COMPILE_STATUS);
		if (success == GL_FALSE) {
			int len = glGetShaderi(fragment_id, GL_INFO_LOG_LENGTH);
			System.out.println("ERROR: Fragment shader failed to compile from [" + filepath + "]");
			System.out.println("Fragment shader contents were: " + fragment_source);
			System.out.println(glGetShaderInfoLog(fragment_id, len));
			throw new Error("main.java.Render.Shader Compilation failed");
		}

		shader_program_id = glCreateProgram();
		glAttachShader(shader_program_id,vertex_id);
		glAttachShader(shader_program_id,fragment_id);
		glLinkProgram(shader_program_id);

		success = glGetProgrami(shader_program_id, GL_LINK_STATUS);
		if (success == GL_FALSE) {
			int len = glGetProgrami(shader_program_id, GL_INFO_LOG_LENGTH);
			System.out.println("ERROR: main.java.Render.Shader Program [" + filepath + "] failed to link");
			System.out.println(glGetProgramInfoLog(shader_program_id, len));
			throw new Error("main.java.Render.Shader Compilation failed");
		}
	}

	public void use() {
		glUseProgram(shader_program_id);
	}

	public void detach() {
		glUseProgram(0);
	}

	public void uploadMat4f(String var_name, Matrix4f mat4) {
		int var_location = glGetUniformLocation(shader_program_id, var_name);
		FloatBuffer mat_buffer = BufferUtils.createFloatBuffer(16);
		mat4.get(mat_buffer);
		glUniformMatrix4fv(var_location, false, mat_buffer);
	}

	public void upload_float(String var_name, float val) {
		int var_location = glGetUniformLocation(shader_program_id, var_name);
		use();
		glUniform1f(var_location, val);
	}
	public void upload_texture(String var_name, int slot) {
		int var_location = glGetUniformLocation(shader_program_id, var_name);
		use();
		glUniform1i(var_location, slot);
	}
}
