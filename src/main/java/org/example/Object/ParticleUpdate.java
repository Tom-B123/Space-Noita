package org.example.Object;

import org.jocl.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.jocl.CL.*;

public class ParticleUpdate {

	private static String program_source;

	private cl_context context;
	private cl_command_queue command_queue;
	private cl_kernel kernel;
	private long[] global_work_size;
	private long[] local_work_size;
	private cl_mem[] mem_objects;

	private long n;

	// Data to be read and written to
	private Pointer data_pointer;
	private Pointer step_pointer;
	private Pointer dims_pointer;

	private int step = 0;

	private int[] step_array;

	private static ParticleUpdate particle_update = null;

	public static ParticleUpdate get() {
		if (particle_update == null) { particle_update = new ParticleUpdate(); }
		return particle_update;
	}

	// Assign the mem_object buffers and bind them to the kernel arguments
	private void bind_arguments() {
		// RGB5_1A source
		mem_objects[0] = clCreateBuffer(context,
				CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_short * n, data_pointer, null
		);
		// RGB5_1A destination
		mem_objects[1] = clCreateBuffer(context,
				CL_MEM_READ_WRITE,
				Sizeof.cl_short * n, null, null
		);
		//
		mem_objects[2] = clCreateBuffer(context,
				CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_short * n, dims_pointer, null
		);
		// RGB5_1A source
		mem_objects[3] = clCreateBuffer(context,
				CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_short * n, step_pointer, null
		);

		// Bind the kernel's arguments
		clSetKernelArg(kernel, 0,
				Sizeof.cl_mem, Pointer.to(mem_objects[0])
		);
		clSetKernelArg(kernel, 1,
				Sizeof.cl_mem, Pointer.to(mem_objects[1])
		);
		clSetKernelArg(kernel, 2,
				Sizeof.cl_mem, Pointer.to(mem_objects[2])
		);
		clSetKernelArg(kernel, 3,
				Sizeof.cl_mem, Pointer.to(mem_objects[3])
		);
	}

	public void init() {
		// Get the shader source code
		String filepath = "assets/Shaders/shader.cu";

		try {
			String source = new String(Files.readAllBytes(Paths.get(filepath)));

			program_source = source;
		} catch(IOException e) {
			throw new Error("The shader file: " + filepath + " could not be opened");
		}

		// Universal step across every object's update
		step_array = new int[] {step};

		final int platform_index = 0;
		final long device_type = CL_DEVICE_TYPE_ALL;
		final int device_index = 0;

		CL.setExceptionsEnabled(true);

		int[] num_platforms_array = new int[1];
		clGetPlatformIDs(0,null,num_platforms_array);
		int num_platforms = num_platforms_array[0];

		cl_platform_id[] platforms = new cl_platform_id[num_platforms];
		clGetPlatformIDs(platforms.length, platforms, null);
		cl_platform_id platform = platforms[platform_index];

		int[] num_devices_array = new int[1];
		clGetDeviceIDs(platform,device_type,0,null,num_devices_array);
		int num_devices = num_devices_array[0];

		cl_device_id[] devices = new cl_device_id[num_devices];
		clGetDeviceIDs(platform,device_type,num_devices,devices,null);
		cl_device_id device = devices[device_index];

		cl_context_properties context_properties = new cl_context_properties();
		context_properties.addProperty(CL_CONTEXT_PLATFORM, platform);

		context = clCreateContext(
				context_properties, 1, new cl_device_id[]{device},
				null,null,null
		);

		command_queue = clCreateCommandQueue(context,device,0,null);

		// Create the program
		cl_program program = clCreateProgramWithSource(context,
				1, new String[] {program_source}, null, null
		);

		// Build program
		clBuildProgram(program,0,null,null,null,null);

		// Create the kernel
		kernel = clCreateKernel(program, "sampleKernel", null);

		// Arguments are: object data source, object data output, window size, simulation step
		mem_objects = new cl_mem[4];

		n = 1;

		data_pointer = Pointer.to(new int[]{0});
		dims_pointer = Pointer.to(new int[]{0});
		step_pointer = Pointer.to(new int[]{0});

		bind_arguments();

		local_work_size = new long[]{1};
	}


	// Pass in data, dimensions and simulation step to the GPU
	// Define the global work size (number of pixels)
	public short[] update(short[] data, int width, int height) {
		// Prepare kernel
		n = (long)width * (long)height;

		global_work_size = new long[]{n};

		data_pointer = Pointer.to(data);
		dims_pointer = Pointer.to(new int[]{width, height});
		step_pointer = Pointer.to(new int[]{step});

		// Point to kernel arguments
		//bind_arguments();

		// Add data to the kernel
		clEnqueueNDRangeKernel(
				command_queue,kernel,1,null,
				global_work_size,local_work_size,0,null,null
		);

		// Read the processed data back from the kernel
		clEnqueueReadBuffer(
				command_queue,mem_objects[1],CL_TRUE,0,
				n * Sizeof.cl_short, data_pointer, 0, null, null
		);

		step++;

		return data;
	}
}
