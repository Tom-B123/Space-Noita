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

float PI() { return 3.141592654f; }
float half_PI() { return 1.570796327f; }

int pos_to_index(struct Pos pos, int width) {
    return (pos.x+pos.y*width);
}

int get_gravity_direction(float src_gravity_angle,int step) {
    // Split 8 directions into n more "pseudo directions"
    // High n = obvious alternation, low n = low degree of extra directions
    const int increments = 4;

    // Step of the simulation within these increments
    int sub_step = step % increments;

    float direction = 8 * src_gravity_angle / 2 / PI();

    // 2 direction options
    int remain_direction = (int)(floor(direction)) % 8;
    int change_direction = (remain_direction + 1) % 8;

    // Fractional component between the remain and change directions
    float decimal = direction - floor(direction);

    int remain_weight = (int)(floor((1-decimal) * increments + 0.5f));

    if (sub_step <= remain_weight) { return remain_direction; }
    return change_direction;
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
            break;
        case 1:
            // Water
            out_tag.is_liquid = true;
            break;
        default:
            break;
    }
    return out_tag;
}

bool is_falling(struct Pos pos, int width, struct Bound *bounds, __global const short *src_pos) {
    struct Tag tag = get_tags(get_pixel_colour(src_pos[pos_to_index(pos,width)]),bounds);
    return tag.is_powder || tag.is_liquid;
}

bool is_empty(struct Pos pos, int width, int height, __global const short *src_pos) {
    struct Colour cell_colour = get_pixel_colour(src_pos[pos_to_index(pos,width)]);
    if (pos.x < 0 || pos.x > width - 1 || pos.y < 0 || pos.y > height - 1) { return false; }
    return !cell_colour.a;
}

int falling_cell_priority(struct Pos pos, int width, float gravity_angle, int step, struct Bound *bounds, __global const short *src_pos) {

    int gravity_direction = get_gravity_direction(gravity_angle,step);

    int offset_x[] = {
         0,  1,  1,  1,  0, -1, -1, -1,
    };

    int offset_y[] = {
        -1, -1,  0,  1,  1,  1,  0, -1,
    };

    // Cell falling from above
    for (int i = 0; i < 5; i++) {
        // Look at cell moving into the position
        pos.x -= offset_x[gravity_direction];
        pos.y -= offset_y[gravity_direction];

        if (is_falling(pos,width,bounds,src_pos)) { return i;}

        pos.x += offset_x[gravity_direction];
        pos.y += offset_y[gravity_direction];

        // Move gravity_direction to the next cell
        gravity_direction = gravity_direction + ((i+1) * (2 * ((i+1)%2) - 1))%8;
        if (gravity_direction < 0) {gravity_direction += 8;}
    }

    return 4;
}

struct Pos update_powder(struct Pos pos, int width, int height, float gravity_angle, int step, struct Bound *bounds, __global const short *src_pos) {

    int initial_x = pos.x;
    int initial_y = pos.y;

    int gravity_direction = get_gravity_direction(gravity_angle,step);

    int offset_x[] = {
         0,  1,  1,  1,  0, -1, -1, -1,
    };

    int offset_y[] = {
        -1, -1,  0,  1,  1,  1,  0, -1,
    };

    for (int i = 0; i < 3; i++) {
        // Look at cell moving into the position
        pos.x += offset_x[gravity_direction];
        pos.y += offset_y[gravity_direction];

        if (is_empty(pos,width,height,src_pos) && falling_cell_priority(pos,width,gravity_angle,step,bounds,src_pos) >= i) { return pos;}

        pos.x -= offset_x[gravity_direction];
        pos.y -= offset_y[gravity_direction];

        // Move gravity_direction to the next cell
        gravity_direction = gravity_direction + ((i+1) * (2 * ((i+1)%2) - 1));
        if (gravity_direction < 0) {gravity_direction += 8;}
    }

    pos.x = initial_x;
    pos.y = initial_y;

    return pos;
}

struct Pos update_liquid(struct Pos pos, int width, int height, float gravity_angle, int step, struct Bound *bounds, __global const short *src_pos) {

    int initial_x = pos.x;
    int initial_y = pos.y;

    int gravity_direction = get_gravity_direction(gravity_angle,step);

    int offset_x[] = {
         0,  1,  1,  1,  0, -1, -1, -1,
    };

    int offset_y[] = {
        -1, -1,  0,  1,  1,  1,  0, -1,
    };

    for (int i = 0; i < 5; i++) {
        // Look at cell moving into the position
        pos.x += offset_x[gravity_direction];
        pos.y += offset_y[gravity_direction];

        if (is_empty(pos,width,height,src_pos) && falling_cell_priority(pos,width,gravity_angle,step,bounds,src_pos) >= i) { return pos;}

        pos.x -= offset_x[gravity_direction];
        pos.y -= offset_y[gravity_direction];

        // Move gravity_direction to the next cell
        gravity_direction = gravity_direction + ((i+1) * (2 * ((i+1)%2) - 1));
        if (gravity_direction < 0) {gravity_direction += 8;}
    }

    pos.x = initial_x;
    pos.y = initial_y;

    return pos;
}

__kernel void sampleKernel(__global const short *src_pos, __global short *dst_pos, __global const float *src_gravity_angle, __global const int *src_world_dims, __global int *src_step) {
    const int gid = get_global_id(0);
    const int width = src_world_dims[0];
    const int height = src_world_dims[1];
    const int x = (gid) % width;
    const int y = (gid) / width;
    const int step = src_step[0];
    const float gravity_angle = src_gravity_angle[0];

    const struct Colour blank = { 0,0,0,0 };

    struct Pos pos = {x,y};

    struct Colour cell_colour = get_pixel_colour(src_pos[gid]);

    // Materials should have a range, eg. 2 in rgb. These colours correspond to unique colours, e.g. metal can be white / red etc.
    struct Bound bounds[] = {
        {29,31,29,31, 0, 2, 0}, // Sand (ID 0)
        { 0, 2, 0, 2,29,31, 1},   // Water (ID 1)
    };

    struct Tag tag = get_tags(cell_colour,&bounds);

    if (tag.is_powder) { pos = update_powder(pos,width,height,gravity_angle,step,&bounds,src_pos); }
    if (tag.is_liquid) { pos = update_liquid(pos,width,height,gravity_angle,step,&bounds,src_pos); }

    int n_id = pos_to_index(pos,width);

    dst_pos[gid] = get_pixel_data(blank);
    dst_pos[n_id] = get_pixel_data(cell_colour);
    return;
};
