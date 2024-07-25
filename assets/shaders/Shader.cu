// Holds RGB colour data for a pixel
struct Colour {
    short r;
    short g;
    short b;
};

// Holds a position and a velocity to update both in 1 function
struct PosVel {
    int x;
    int y;
    int vel_magnitude;
    int vel_angle;
};

int get_weight(int material) {
    const int material_count = 3;

    // If the cell is invalid or an invalid material ID, return -1
    if (material < 0 || material > material_count) { return -1;}

    const int weight[material_count] = {
        30, // sand
        80, // silver
        10  // water
    };

    return weight[material];
}

// Returns PI and PI/2 for calculations
float PI() { return 3.14159f; }
float half_PI() { return 1.570795f; }

// Returns the constant acceleration values
float get_y_accel() { return -1.2f; }
float get_x_accel() { return 0.0f; }

// Convert from cartesian to 1D index
int pos_to_index(int x, int y, int width) {
    return 3 * (x + y * width);
}
int pos_to_vel_index(int x, int y, int width) {
    return (x + y * width);
}

// Get the cell colour
struct Colour get_cell(int x, int y, int width, int height, __global const short *a) {
    if (y < 0 || y > height -1 || x < 0 || x > width -1) {
        struct Colour out = {32767, 32767, 32767 };
        return out;
    }
    int index = pos_to_index(x,y,width);

    struct Colour n_colour = { a[index], a[index + 1], a[index + 2] };
    return n_colour;
}

int material_type(int x, int y, int width, int height, __global const short *src_pos) {
    int combined_rgb;
    const int r_set = 127 * 256 * 256;
    const int g_set = 127 * 256;
    const int b_set = 127;

    if (x < 0 || x > width - 1 || y < 0 || y > height - 1) { return -2; }

    struct Colour colour = get_cell(x,y,width,height,src_pos);
    combined_rgb = (int)(colour.b / 256) + (int)(colour.g / 256) * 256 + (int)(colour.r / 256) * 256 * 256;
    // -1 = empty, -2 = invalid
    switch (combined_rgb) {
        // Order: Powders, liquids, gasses, solids
        case g_set + r_set:
            // Sand
            return 0;
        case g_set + r_set + b_set:
            // Silver
            return 1;
        case b_set:
            // Water
            return 2;
        default:
            return -1;
    }
}

// Cell contents
bool is_empty(int x, int y, int width, int height, __global const short *src_pos) {
    if (x < 0 || x > width - 1 || y < 0 || y > height - 1) { return false; }
    return material_type(x,y,width,height,src_pos) == -1;
}
bool is_sand(int x, int y, int width, int height, __global const short *src_pos) {
    return material_type(x,y,width,height,src_pos) == 0;
}
bool is_silver(int x, int y, int width, int height, __global const short *src_pos) {
    return material_type(x,y,width,height,src_pos) == 1;
}
bool is_water(int x, int y, int width, int height, __global const short *src_pos) {
    return material_type(x,y,width,height,src_pos) == 2;
}
bool is_steam(int x, int y, int width, int height, __global const short *src_pos) {
    return false;//material_type(x,y,width,height,src_pos) == 2;
}

// Cell types
bool is_powder(int x, int y, int width, int height, __global const short *src_pos) {
    int t = material_type(x,y,width,height,src_pos);
    return t >= 0 && t <= 1;
}
bool is_liquid(int x, int y, int width, int height, __global const short *src_pos) {
    int t = material_type(x,y,width,height,src_pos);
    return t >= 2 && t <= 2;
}
bool is_gas(int x, int y, int width, int height, __global const short *src_pos) {
    int t = material_type(x,y,width,height,src_pos);
    return t >= 2 && t <= 2;
}

// Cell properties
bool is_falling(int x, int y, int width, int height, __global const short *a) {
    return is_liquid(x,y,width,height,a) || is_powder(x,y,width,height,a);
}

// Take the target position and return the highest priority cell (0->4, sand only uses 0->2)
int falling_cell_priority(int x, int y, int width, int height, __global const short *a) {
    if (is_falling(x,y+1,width,height,a))                                        { return 0; }
    if (is_falling(x-1,y+1,width,height,a))                                      { return 1; }
    if (is_falling(x+1,y+1,width,height,a))                                      { return 2; }
    if (is_liquid(x-1,y,width,height,a))                                         { return 3; }
    if (is_liquid(x+1,y,width,height,a))                                         { return 4; }
    return 4;
}

int rising_cell_priority(int x, int y, int width, int height, __global const short *a) {
    if (is_gas(x,y-1,width,height,a))                                            { return 0; }
    if (is_gas(x-1,y-1,width,height,a))                                          { return 1; }
    if (is_gas(x+1,y-1,width,height,a))                                          { return 2; }
    if (is_gas(x-1,y,width,height,a))                                            { return 3; }
    if (is_gas(x+1,y,width,height,a))                                            { return 4; }
    return 4;
}

// Process: cells have a flowing priority:
// 1 0 2
// 3 X 4
// 1 0 2
// Iterate through each check in this order.
// For priority 2, check no priority 1 cell is present (no cell is above the empty space)
// For priority 3, check no priority 2 or 1 cell is present.
// If any higher priority exists, do not move and they will move in


// Updating "static" particles, i.e. liquids or powers that are in a pool / stack, not free falling
struct PosVel update_powder(struct PosVel pos_vel, int width, int height, __global const short *src_pos) {
    // P0, always goes through
    if (is_empty(pos_vel.x,pos_vel.y-1,width,height,src_pos)) {
        pos_vel.y -= 1;
        return pos_vel;
    }
    // P1
    if (is_empty(pos_vel.x+1,pos_vel.y-1,width,height,src_pos) && falling_cell_priority(pos_vel.x+1,pos_vel.y-1,width,height,src_pos) >= 1) {
        pos_vel.x += 1;
        pos_vel.y -= 1;
        return pos_vel;
    }
    // P2
    if (is_empty(pos_vel.x-1,pos_vel.y-1,width,height,src_pos) && falling_cell_priority(pos_vel.x-1,pos_vel.y-1,width,height,src_pos) >= 2) {
        pos_vel.x -= 1;
        pos_vel.y -= 1;
        return pos_vel;
    }
    return pos_vel;
}

struct PosVel update_liquid(struct PosVel pos_vel, int width, int height, int step, __global const short *src_pos) {


    // P0, always goes through
    if (is_empty(pos_vel.x,pos_vel.y-1,width,height,src_pos)) {
        pos_vel.y -= 1;
    }
    // P1
    else if (is_empty(pos_vel.x+1,pos_vel.y-1,width,height,src_pos) && falling_cell_priority(pos_vel.x+1,pos_vel.y-1,width,height,src_pos) >= 1) {
        pos_vel.x += 1;
        pos_vel.y -= 1;
    }
    // P2
    else if (is_empty(pos_vel.x-1,pos_vel.y-1,width,height,src_pos) && falling_cell_priority(pos_vel.x-1,pos_vel.y-1,width,height,src_pos) >= 2) {
        pos_vel.x -= 1;
        pos_vel.y -= 1;
    }
    else {
        if (step % 2 == 0) {
            //P3
            if (is_empty(pos_vel.x+1,pos_vel.y,width,height,src_pos) && falling_cell_priority(pos_vel.x+1,pos_vel.y,width,height,src_pos) >= 3) {
                pos_vel.x += 1;
            }
            // P4
            else if (is_empty(pos_vel.x-1,pos_vel.y,width,height,src_pos) && falling_cell_priority(pos_vel.x-1,pos_vel.y,width,height,src_pos) >= 4) {
                pos_vel.x -= 1;
            }
        }
        else {
            //P4
            if (is_empty(pos_vel.x-1,pos_vel.y,width,height,src_pos) && falling_cell_priority(pos_vel.x-1,pos_vel.y,width,height,src_pos) >= 4) {
                pos_vel.x -= 1;
            }
            // P3
            else if (is_empty(pos_vel.x+1,pos_vel.y,width,height,src_pos) && falling_cell_priority(pos_vel.x+1,pos_vel.y,width,height,src_pos) >= 3) {
                pos_vel.x += 1;
            }
        }
    }
    if (pos_vel.x < 0) { pos_vel.x = 0; }
    if (pos_vel.x > width - 1) { pos_vel.x = width - 1; }
    if (pos_vel.y < 0) { pos_vel.y = 0; }
    if (pos_vel.y > height - 1) { pos_vel.y = height - 1; }

    return pos_vel;
}

int update_gas(int x, int y, int width, int height, __global const short *a) {
    // P0, always goes through
    if (is_empty(x,y+1,width,height,a)) {
        return pos_to_index(x,y+1,width);
    }
    // P1
    if (is_empty(x+1,y+1,width,height,a) && rising_cell_priority(x+1,y+1,width,height,a) >= 1) {
        return pos_to_index(x+1,y+1,width);
    }
    // P2
    if (is_empty(x-1,y+1,width,height,a) && rising_cell_priority(x-1,y+1,width,height,a) >= 2) {
        return pos_to_index(x-1,y+1,width);
    }
    // P3
    if (is_empty(x+1,y,width,height,a) && rising_cell_priority(x+1,y,width,height,a) >= 3) {
        return pos_to_index(x+1,y,width);
    }
    // P4
    if (is_empty(x-1,y,width,height,a) && rising_cell_priority(x-1,y,width,height,a) >= 4) {
        return pos_to_index(x-1,y,width);
    }
    return pos_to_index(x,y,width);
}

// Swap cell the cell above / below, depending on material weight
// Pseudo bubble sort to move any column from an unsorted mixture to a sorted one, lightest at the top and heaviest at the bottom
struct PosVel update_weight(struct PosVel pos_vel, const int width,const int height, int step, __global const short *src_pos) {

    // Plan: iteratively look above and below until a "stable" cell is found, ie two cells that don't want to swap.
    // Compare weight difference between self and above / below cells, greater difference = swap direction

    // Worst case = stack of differently weighted cells from very bottom to very top
    int weights[5];

    // Get cell weight
    weights[2] = get_weight(material_type(pos_vel.x,pos_vel.y,width,height,src_pos));
    // Get above and below weight
    weights[3] = get_weight(material_type(pos_vel.x,pos_vel.y+1,width,height,src_pos));
    weights[1] = get_weight(material_type(pos_vel.x,pos_vel.y-1,width,height,src_pos));

    // If above and below are invalid or the cell are already in order, return out
    if (weights[3] == -1 && weights[1] == -1 || (weights[1] >= weights[2] && weights[3] <= weights[2])) { return pos_vel; }

    // Direction to above unless below is the greater distance
    int direction = 0;
    int other_direction = 0;

    // If the rise priority is positive (the cell should rise) and more significant than the sink priority, direction to rising
    if (weights[3] - weights[2] > 0 && weights[3] - weights[2] > weights[2] - weights[1]) { direction = 1;}

    // If the sink priority is positive (the cell should sink) and more significant than the rise priority, direction to sinking
    if (weights[2] - weights[1] > 0 && weights[2] - weights[1] > weights[3] - weights[2]) { direction = -1;}

    // Confine the direction to not point out of bounds
    if (pos_vel.y + direction < 0 || pos_vel.y + direction > height - 1) { direction = 0; }


    // Add in the weight for the other cell's movement
    weights[2+direction+direction] = get_weight(material_type(pos_vel.x,pos_vel.y+direction+direction,width,height,src_pos));

    // If the rise priority is positive (the cell should rise) and more significant than the sink priority, direction to rising
    if (weights[3 + direction] - weights[2 + direction] > 0 && weights[3 + direction] - weights[2 + direction] > weights[2 + direction] - weights[1 + direction]) { other_direction = 1;}

    // If the sink priority is positive (the cell should sink) and more significant than the rise priority, direction to sinking
    if (weights[2 + direction] - weights[1 + direction] > 0 && weights[2 + direction] - weights[1 + direction] > weights[3 + direction] - weights[2 + direction]) { other_direction = -1;}

    // Confine the direction to not point out of bounds
    if (pos_vel.y + other_direction < 0 || pos_vel.y + other_direction > height - 1) { other_direction = 0; }


    // Ensure the other has the opposite direction to you

    // If directions are opposite, move that way
    if (direction == 1 && other_direction == -1) {
        // Ensure the weight difference is correct before swapping
        pos_vel.y += 1;
    }
    if (direction == -1 && other_direction == 1) {
        // Ensure the weight difference is correct before swapping
        pos_vel.y -= 1;
    }
    return pos_vel;
}


// Updating "dynamic" particles, i.e. liquids or powers that are free falling

// Get the angle back from a given x,y velocity
float get_angle(float x, float y) {
    if (x >= 0 && y >= 0)  return atan(y/x);
    if (x < 0 && y >= 0)   return half_PI() + atan(-x/y);
    if (x < 0 && y < 0)  return PI() + atan(y/x);
    return half_PI() + PI() + atan(-x/y);
}

// Order angles 0->256 such that 192->0 and 64->255 to get a sensible priority order
int get_angle_priority(int angle) {
    // Rotate 90 degrees cw so 0 & 256 = furthest (most upwards)
    angle = (angle + 64)%256;

    // Normalise cw and ccw from 128 as distance from 128
    int distance_to = abs(128 - angle);

    distance_to *= 2;
    if (angle < 128) { distance_to -= 1; }

    return 256 - distance_to;
}

// Check in a circle around the cell to see if it has any other cells that will enter next frame.
// Return the lowest cell priority for entering the cell
int velocity_cell_priority(int x, int y, int width, int height, __global const short *src_pos, __global const short *src_vel) {
    // Radius is 16 as this is the maximum moved in 1 tick
    float search_radius = 16;
    float dist_squared = search_radius * search_radius;
    // Priority must be < 256
    int lowest_priority = 1000;
    // Iterate through each y of the circle
    for (int dx = -search_radius; dx <= search_radius; dx++) {
        for (int dy = - search_radius; dy <= search_radius; dy++) {
            if (dx*dx + dy*dy < dist_squared) {
                if (is_falling(x,y,width,height,src_pos)) {
                    int index = pos_to_index(dx + x,dy + y,width);
                    int vel_angle = src_vel[index / 3] & 255;
                    int vel_magnitude = src_vel[index / 3] & 65280 >> 8;

                    // Check if the cell will intersect x,y
                    // If it will, get its priority

                    float rad_angle = PI() * (float)vel_angle / 128.0f;
                    float frac_magnitude = vel_magnitude / 16.0f;

                    float x_vel = cos(rad_angle) * frac_magnitude;
                    float y_vel = sin(rad_angle) * frac_magnitude;

                    if ((int)(dx + x_vel) == x && (int)(dy + y_vel) == y) {
                        // A cell should only move to this cell if it has the lowest priority
                        int n_priority = get_angle_priority(vel_angle);
                        if (n_priority < lowest_priority) { lowest_priority = n_priority; }
                    }
                }
            }
        }
    }
    return lowest_priority;
}


// Update the velocity of the PosVel by the global acceleration factor
struct PosVel accelerate(struct PosVel pos_vel) {
    float rad_angle = PI() * (float)pos_vel.vel_angle / 128.0f;
    float frac_magnitude = pos_vel.vel_magnitude / 16.0f;
    float x_vel = cos(rad_angle) * frac_magnitude + get_x_accel();
    float y_vel = sin(rad_angle) * frac_magnitude + get_y_accel();

    rad_angle = get_angle(x_vel,y_vel);
    int byte_angle = (int)(128 * rad_angle / PI()) % 256;
    int byte_magnitude = (int)(16 * sqrt(x_vel*x_vel + y_vel * y_vel));

    if (byte_magnitude >= 256) { byte_magnitude = 255; }
    if (byte_angle >= 256) { byte_angle = 255; }

    pos_vel.vel_magnitude = byte_magnitude;
    pos_vel.vel_angle = byte_angle;

    return pos_vel;
}

// Return true if particle is near a static particle
bool near_static(struct PosVel pos_vel, int width, int height, __global const short *src_pos, __global const short *src_vel) {
    int tx, ty;

    // Search the 3x3 radius for a falling cell with 0 velocity
    for (int dx = -1; dx <= 1; dx++) {
        for (int dy = -1; dy <= 1; dy++) {
            // Ignore own cell
            if (dx == 0 && dy == 0) { continue; }
            tx = dx + pos_vel.x;
            ty = dy + pos_vel.y;

            // Ignore invalid cells outside of world bounds
            if (tx < 0 || tx >= width || ty < 0 || ty >= height) { continue; }

            int index = pos_to_index(tx,ty,width);
            bool is_static = src_vel[index/3] == 0;
            if (!is_empty(tx,ty,width,height,src_pos) && is_static) {
                return true;
            }
        }
    }
    return false;
}

// Update the PosVel by checking which locations in the attempted line of movement can be accessed
// Hitting edge: set velocity to 0, both to 0 if its the bottom edge
// Hitting a static cell: if 0 velocity in a 3x3 around the cell, set velocity to 0
struct PosVel linear_check (struct PosVel pos_vel, int width, int height, __global const short *src_pos, __global const short *src_vel, __global short *dst_pos){
    // Get current position and target position by adding the vx and vy to the x and y
    // If target is invalid, confine it to the world bounds
    // Iterate across the line from source to target, checking for static or dynamic collisions

    // pos_vel = the start position (x1,y1)

    float rad_angle = PI() * (float)pos_vel.vel_angle / 128.0f;
    float frac_magnitude = pos_vel.vel_magnitude / 16.0f;
    float x_vel = cos(rad_angle) * frac_magnitude + get_x_accel();
    float y_vel = sin(rad_angle) * frac_magnitude + get_y_accel();

    // Get the target position (x2,y2)
    int target_x = pos_vel.x + x_vel;
    int target_y = pos_vel.y + y_vel;


    float dx = (target_x - pos_vel.x);
    float dy = (target_y - pos_vel.y);

    int line_step = abs((int)dx);
    if (abs((int)dy) > abs((int)dx)) { line_step = abs((int)dy); }

    // Amount to step each tick
    dx = dx / (float)line_step;
    dy = dy / (float)line_step;

    float x = (float)pos_vel.x;
    float y = (float)pos_vel.y;

    bool is_static = false;
    int i = 0;

    // Linear walk through each cell from source to target
    while (i <= line_step+1 && !is_static) {

        pos_vel.x = (int)x;
        pos_vel.y = (int)y;

        int index = pos_to_index(pos_vel.x,pos_vel.y,width);
        //dst_pos[index] = 30000;
        // Stop the line when static cell is found
        if (pos_vel.y <= 0 || pos_vel.y >= height - 1 || pos_vel.x <= 0 || pos_vel.x >= width -1 || near_static(pos_vel,width,height,src_pos,src_vel)) {
            pos_vel.vel_angle = 0;
            pos_vel.vel_magnitude = 0;
            is_static = true;
        }
        x += dx;
        y += dy;

        i++;
    }

    rad_angle = get_angle(x_vel,y_vel);

    return pos_vel;
}

// Move to the next free cell from the starting position in the given direction
struct PosVel update_velocity(struct PosVel pos_vel, int width, int height, __global const short *src_pos, __global const short *src_vel, __global short *dst_pos) {

    if (!near_static(pos_vel,width,height,src_pos,src_vel) && pos_vel.y > 0) {
        pos_vel = accelerate(pos_vel);
        pos_vel = linear_check(pos_vel,width,height,src_pos,src_vel,dst_pos);
    }

    return pos_vel;
}

__kernel void sampleKernel(__global const short *src_pos, __global const short *src_vel, __global short *dst_pos, __global short *dst_vel, __global const int *world_dims, __global int *step_ptr) {
    int gid = get_global_id(0);
    const int width = world_dims[0];
    const int height = world_dims[1];
    int x = (gid) % width;
    int y = (gid) / width;
    int step = step_ptr[0];


    // Clear the whole screen to remove junk data
    if (step == 0) {
        dst_pos[3 * gid] = 0;
        dst_pos[3 * gid + 1] = 0;
        dst_pos[3 * gid + 2] = 0;
        dst_vel[gid] = 0;
        return;
    }

    // Assign the RGB to cell_colour
    struct Colour cell_colour = get_cell(x,y,width,height,src_pos);

    // Default output value is blank
    // Mutate the cell_colour in the update step
    if (!is_empty(x,y,width,height,src_pos)) {
        short velocity = src_vel[gid];
		int vel_magnitude = ((velocity & 65280) >> 8);
		int vel_angle = (velocity & 255);

        struct PosVel pos_vel = {x,y,vel_magnitude,vel_angle};

        int new_index = pos_to_index(x,y,width);

        dst_vel[gid] = 0;

        // Move the cell
        dst_pos[3 * gid] = 0;
        dst_pos[3 * gid + 1] = 0;
        dst_pos[3 * gid + 2] = 0;

        //pos_vel = update_velocity(pos_vel,width,height,src_pos,src_vel,dst_pos);

        if (true || near_static(pos_vel,width,height,src_pos,src_vel)) {
            // Allow heavier particles to sink in lighter particles
            if (is_falling(pos_vel.x,pos_vel.y,width,height,src_pos)) { pos_vel = update_weight(pos_vel, width, height, step, src_pos); }

            // Fall and flow due to gravity
            if (is_powder(pos_vel.x,pos_vel.y,width,height,src_pos)) { pos_vel = update_powder(pos_vel,width,height,src_pos); }
            if (is_liquid(pos_vel.x,pos_vel.y,width,height,src_pos)) { pos_vel = update_liquid(pos_vel,width,height,step,src_pos); }

            if (pos_vel.x == -1) { return;}
        }

        velocity = pos_vel.vel_angle + (pos_vel.vel_magnitude << 8);

        new_index = pos_to_index(pos_vel.x,pos_vel.y,width);

        if (new_index == -1) { return; }


        dst_pos[new_index]     = cell_colour.r;
        dst_pos[new_index + 1] = cell_colour.g;
        dst_pos[new_index + 2] = cell_colour.b;
        dst_vel[new_index / 3] = velocity;

    }
};
