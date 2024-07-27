// Holds RGB colour data for a pixel
struct Colour {
    // Stores the colour in a more readable format, chars used as channels must be between 0 and 31
    char r;
    char g;
    char b;
    bool a;
};

struct Node {
    struct Node *next;
    struct Node *prev;
    char *val;
};

struct List {
    struct Node *root;
    int length;
};

struct List push(struct List list, char val) {
    struct Node n_node = { NULL, NULL, val };
    if (list.root == NULL) {
        list.root = &n_node;
        list.root -> next = &n_node;
        list.root -> prev = &n_node;
        list.root -> val = &n_node.val;
    }
    else {

    }
    list.length ++;
    return list;
}

char peep(struct List list) {
    if (list.root == NULL) { return '0'; }
    return &list.root -> val;
}

struct Colour get_pixel_colour(short pixel_data) {
    struct Colour out = {(char)(pixel_data & 63488) >> 11, (char)(pixel_data & 1984) >> 6, (char)(pixel_data & 62) >> 1, (bool)(pixel_data & 1) };
    return out;
}
short get_pixel_data(struct Colour pixel_colour) {
    short out = (short)(pixel_colour.a + (pixel_colour.b << 1) + (pixel_colour.g << 6) + (pixel_colour.r << 11));
    return out;
}

char get_tags(struct Colour pixel_colour) {
    char msg[4] = "stop";
    return msg;
}

__kernel void sampleKernel(__global const short *src_pos, __global short *dst_pos,__global const int *world_dims, __global int *step_ptr) {
    int gid = get_global_id(0);
    const int width = world_dims[0];
    const int height = world_dims[1];
    int x = (gid) % width;
    int y = (gid) / width;
    int step = step_ptr[0];


    struct Colour cell_colour = get_pixel_colour(src_pos[gid]);

    struct List tag_list = {NULL, 0};
    tag_list = push(tag_list,'s');

    char c = get_tags(cell_colour);

    if (step == 0) { printf("tags: %c \n",c); }

    dst_pos[gid] = get_pixel_data(cell_colour);
    return;
};
