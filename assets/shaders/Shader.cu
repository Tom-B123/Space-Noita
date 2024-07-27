// Holds RGB colour data for a pixel
struct Colour {
    // Stores the colour in a more readable format, chars used as channels must be between 0 and 31
    char r;
    char g;
    char b;
    bool a;
};

// Stores all the relevant data about a pixel and its behaviour
struct Tag {
    int     id;
    int     weight;
    bool    is_powder;
    bool    is_liquid;
    bool    is_gas;
    bool    is_flammable;
};

// Range of colours corresponds to the ID
struct Bound {
    int min_r;
    int max_r;
    int min_g;
    int max_g;
    int min_b;
    int max_b;
    int id;
};

struct Colour get_pixel_colour(short pixel_data) {
    struct Colour out = {(char)(pixel_data & 63488) >> 11, (char)(pixel_data & 1984) >> 6, (char)(pixel_data & 62) >> 1, (bool)(pixel_data & 1) };
    return out;
}
short get_pixel_data(struct Colour pixel_colour) {
    short out = (short)(pixel_colour.a + (pixel_colour.b << 1) + (pixel_colour.g << 6) + (pixel_colour.r << 11));
    return out;
}

int get_material_id(struct Colour pixel_colour, struct Bound *bounds) {
    for (int i = 0; i < 2; i++) {
        if (pixel_colour.r > 0) printf("min: %i, max: %i, pixel: %i \n",bounds[i].min_r,bounds[i].max_r,pixel_colour.r);
        if (
            pixel_colour.r >= bounds[i].min_r && pixel_colour.r <= bounds[i].max_r &&
            pixel_colour.g >= bounds[i].min_g && pixel_colour.g <= bounds[i].max_g &&
            pixel_colour.b >= bounds[i].min_b && pixel_colour.b <= bounds[i].max_b
        ) {
            return i;
        }
    }
    return -1;
}

struct Tag get_tags(struct Colour pixel_colour, struct Bound *bounds) {
    int material_id = get_material_id(pixel_colour,bounds);

    if (material_id > -1) printf("%i \n",material_id);

    struct Tag out_tag = {material_id,0,false,false,false,false};
    switch(material_id) {
        default:
            return out_tag;
    }
}

__kernel void sampleKernel(__global const short *src_pos, __global short *dst_pos,__global const int *world_dims, __global int *step_ptr) {
    const int gid = get_global_id(0);
    const int width = world_dims[0];
    const int height = world_dims[1];
    const int x = (gid) % width;
    const int y = (gid) / width;
    const int step = step_ptr[0];

    struct Colour cell_colour = get_pixel_colour(src_pos[gid]);

    struct Bound bounds[] = {
        {29,31,29,31,0,0,0}, // Sand (ID 0)
        {0,0,0,2,29,31,1},   // Water (ID 1)
    };

    if (cell_colour.r == 0)printf("%i,%i,%i \n",cell_colour.r,cell_colour.g,cell_colour.b);

    struct Tag tag = get_tags(cell_colour,&bounds);

    dst_pos[gid] = get_pixel_data(cell_colour);
    return;
};
