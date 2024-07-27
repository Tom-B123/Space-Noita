// Holds RGB colour data for a pixel
struct Colour {
    // Stores the colour in a more readable format, chars used as channels must be between 0 and 31
    char r;
    char g;
    char b;
    bool a;
};

struct Colour get_pixel_colour(short pixel_data) {
    struct Colour out = {(char)(pixel_data & 63488) >> 11, (char)(pixel_data & 1984) >> 6, (char)(pixel_data & 62) >> 1, (bool)(pixel_data & 1) };
    return out;
}
short get_pixel_data(struct Colour pixel_colour) {
    short out = (short)(pixel_colour.a + (pixel_colour.b << 1) + (pixel_colour.g << 6) + (pixel_colour.r << 11));
    return out;
}

__kernel void sampleKernel(__global const short *src_pos, __global short *dst_pos,__global const int *world_dims, __global int *step_ptr) {
    int gid = get_global_id(0);
    const int width = world_dims[0];
    const int height = world_dims[1];
    int x = (gid) % width;
    int y = (gid) / width;
    int step = step_ptr[0];

    struct Colour cell_colour = get_pixel_colour(src_pos[gid]);

    cell_colour.r = (cell_colour.r - 1 + x + step / 20) % 32;
    cell_colour.g = (cell_colour.b + 2 + y + step / 20) % 32;
    cell_colour.a = !cell_colour.a;

    dst_pos[gid] = get_pixel_data(cell_colour);
    return;
};
