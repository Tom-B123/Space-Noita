// Holds RGB colour data for a pixel
struct Colour {
    // Stores the colour in a more readable format, chars used as channels must be between 0 and 31
    short r;
    short g;
    short b;
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

struct Pos {
    int x;
    int y;
};

int pos_to_index(struct Pos pos, int width) {
    return (pos.x+pos.y*width);
}

struct Colour get_pixel_colour(short pixel_data) {
    int i_pixel_data = (int)pixel_data;
    struct Colour out = {(short)(i_pixel_data & 63488) >> 11, (short)(i_pixel_data & 1984) >> 6, (short)(i_pixel_data & 62) >> 1, (bool)(pixel_data & 1) };

    if (out.r < 0) { out.r += 32; }

    return out;
}
short get_pixel_data(struct Colour pixel_colour) {
    short out = (short)(pixel_colour.a + (pixel_colour.b << 1) + (pixel_colour.g << 6) + (pixel_colour.r << 11));
    return out;
}

int get_material_id(struct Colour pixel_colour, struct Bound *bounds) {
    for (int i = 0; i < 2; i++) {
        //if (pixel_colour.r > 0) printf("min: %i, max: %i, pixel: %i \n",bounds[i].min_r,bounds[i].max_r,pixel_colour.r);
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

    struct Tag out_tag = {material_id,0,false,false,false,false};
    switch(material_id) {
        case 0:
            // Sand
            out_tag.is_powder = true;
        default:
            break;
    }
    return out_tag;
}

bool is_falling(struct Pos pos, int width, struct Bound *bounds, __global const short *src_pos) {
    struct Tag tag = get_tags(get_pixel_colour(src_pos[pos_to_index(pos,width)]),bounds);
    return tag.is_powder || tag.is_liquid;
}

bool is_empty(struct Pos pos, int width, __global const short *src_pos) {
    struct Colour cell_colour = get_pixel_colour(src_pos[pos_to_index(pos,width)]);
    if (pos.x < 0 || pos.x > width - 1 || pos.y < 0) { return false; }
    return !cell_colour.a;
}

int falling_cell_priority(struct Pos pos, int width, struct Bound *bounds, __global const short *src_pos) {

    // Cell falling from above
    pos.y += 1;
    if (is_falling(pos,width,bounds,src_pos))           { return 0;}

    // Cell falling from right
    pos.x -= 1;
    if (is_falling(pos,width,bounds,src_pos))           { return 1;}

    // Cell falling from left
    pos.x+=2;
    if (is_falling(pos,width,bounds,src_pos))           { return 2;}

    // Cell flowing from right
    pos.y -= 1;
    pos.x -= 1;
    if (is_falling(pos,width,bounds,src_pos))           { return 3;}

    // Cell flowing from left
    pos.x += 2;
    if (is_falling(pos,width,bounds,src_pos))           { return 4;}

    return 4;
}

struct Pos update_powder(struct Pos pos, int width, int height, struct Bound *bounds, __global const short *src_pos) {
    // Check beneath
    pos.y -= 1;
    if (is_empty(pos,width,src_pos)) {
        return pos;
    }

    // Check right
    pos.x += 1;
    if (is_empty(pos,width,src_pos) && falling_cell_priority(pos,width,bounds,src_pos) >= 1) {
        return pos;
    }

    // Check left
    pos.x -= 2;
    if (is_empty(pos,width,src_pos) && falling_cell_priority(pos,width,bounds,src_pos) >= 1) {
        return pos;
    }
    pos.x += 1;
    pos.y += 1;
    return pos;
}

__kernel void sampleKernel(__global const short *src_pos, __global short *dst_pos, __global const float *src_gravity_angle, __global const int *src_world_dims, __global int *src_step) {
    const int gid = get_global_id(0);
    const int width = src_world_dims[0];
    const int height = src_world_dims[1];
    const int x = (gid) % width;
    const int y = (gid) / width;
    const int step = src_step[0];

    const struct Colour blank = { 0,0,0,0 };

    struct Pos pos = {x,y};

    struct Colour cell_colour = get_pixel_colour(src_pos[gid]);

    // Materials should have a range, eg. 2 in rgb. These colours correspond to unique colours, e.g. metal can be white / red etc.
    struct Bound bounds[] = {
        {29,31,29,31,0,0,0}, // Sand (ID 0)
        {0,0,0,2,29,31,1},   // Water (ID 1)
    };

    struct Tag tag = get_tags(cell_colour,&bounds);

    if (tag.is_powder) { pos = update_powder(pos,width,height,&bounds,src_pos); }

    int n_id = pos_to_index(pos,width);

    dst_pos[gid] = get_pixel_data(blank);
    dst_pos[n_id] = get_pixel_data(cell_colour);
    return;
};
