package org.example.Components;

import org.example.Object.Component;
import org.jocl.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.lang.Math.PI;
import static org.jocl.CL.*;

public class ParticleUpdate extends Component {

	private static String program_source;

	private cl_context context;
	private cl_command_queue command_queue;
	private cl_kernel kernel;
	private cl_program program;
	private long[] global_work_size;
	private long[] local_work_size;
	private cl_mem[] mem_objects;

	private long n;

	// Data to be read and written to
	private Pointer data_pointer;
	private Pointer gravity_pointer;
	private Pointer step_pointer;
	private Pointer dims_pointer;

	private int step = 0;

	private void make_buffer(int object, Pointer source) {
		free_buffer(object);
		mem_objects[object] = clCreateBuffer(context,
				CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_short * n, source, null
		);
	}

	// Assign the mem_object buffers and bind them to the kernel arguments
	private void bind_argument(int object) {
		// RGB5_1A source
		// Bind the kernel's arguments
		clSetKernelArg(kernel, object,
				Sizeof.cl_mem, Pointer.to(mem_objects[object])
		);
	}

	private void free_buffer(int object) {
		if (mem_objects[object] == null) { return; }

		clReleaseMemObject(mem_objects[object]);
	}

	public void init() {
		this.n = (long)this.object.get_width() * this.object.get_height();

		System.out.println("N has size: " + n + " (" + object.get_width() + "x" + object.get_height() +")");

		// Get the shader source code
		String filepath = "assets/Shaders/shader.cu";

		try {
			String source = new String(Files.readAllBytes(Paths.get(filepath)));

			program_source = source;
		} catch(IOException e) {
			throw new Error("The shader file: " + filepath + " could not be opened");
		}

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
		program = clCreateProgramWithSource(context,
				1, new String[] {program_source}, null, null
		);

		// Build program
		clBuildProgram(program,0,null,null,null,null);

		// Create the kernel
		kernel = clCreateKernel(program, "sampleKernel", null);

		// Arguments are: object data source, object data output, window size, simulation step
		mem_objects = new cl_mem[5];

		data_pointer = Pointer.to(this.object.data);
		gravity_pointer = Pointer.to(new float[]{ (float)this.object.get_transform().angle + (float)PI});
		dims_pointer = Pointer.to(new int[]{this.object.get_width(),this.object.get_height()});
		step_pointer = Pointer.to(new int[]{step});

		// Bad to have constant sized read buffer
		mem_objects[1] = clCreateBuffer(
				context, CL_MEM_READ_WRITE,
				Sizeof.cl_short * n, null, null
		);

		make_buffer(0,data_pointer);
		make_buffer(2,gravity_pointer);
		make_buffer(3,dims_pointer);
		make_buffer(4,step_pointer);

		bind_argument(0);
		bind_argument(1);
		bind_argument(2);
		bind_argument(3);
		bind_argument(4);

		global_work_size = new long[]{n};
		local_work_size = new long[]{1};
	}

	public void refresh_pixels() {
		data_pointer = Pointer.to(this.object.data);
		make_buffer(0,data_pointer);
		bind_argument(0);
	}

	// Pass in data, dimensions and simulation step to the GPU
	// Define the global work size (number of pixels)
	@Override
	public void update(float dt) {


		// Prepare kernel

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



		// Create the program
		//cl_program program = clCreateProgramWithSource(context,
				//1, new String[] {program_source}, null, null
		//);

		// Build program
		//clBuildProgram(program,0,null,null,null,null);

		// Create the kernel
		kernel = clCreateKernel(program, "sampleKernel", null);

		data_pointer = Pointer.to(this.object.data);
		gravity_pointer = Pointer.to(new float[]{ (float)this.object.get_transform().angle + (float)PI});
		dims_pointer = Pointer.to(new int[]{this.object.get_width(), this.object.get_height()});
		step_pointer = Pointer.to(new int[]{step});


		// Point to kernel arguments

		make_buffer(0,data_pointer);


		mem_objects[1] = clCreateBuffer(
				context, CL_MEM_READ_WRITE,
				Sizeof.cl_short * n, null, null
		);

		make_buffer(2,gravity_pointer);
		make_buffer(3,dims_pointer);
		make_buffer(4,step_pointer);

		bind_argument(0);
		bind_argument(1);
		bind_argument(2);
		bind_argument(3);
		bind_argument(4);

		step++;
	}
}
