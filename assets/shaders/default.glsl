#type vertex
#version 330 core
layout (location=0) in vec3 a_pos;
layout (location=1) in vec4 a_colour;
layout (location=2) in vec2 a_tex_coords;

uniform mat4 u_projection;
uniform mat4 u_view;
uniform vec2 u_transform;

// Pixel size, x and y dimensions of the object
uniform vec3 u_scale;
// Rotation amount, x and y of shape centre
uniform vec3 u_rotation;

out vec4 f_colour;
out vec2 f_tex_coords;


const float PI = 3.141592654;
const float half_PI = PI/2;

float get_distance(float x, float y) {
    return sqrt(x*x + y*y);
}

float get_angle(float x, float y) {
    if (x >= 0 && y >= 0)  return atan(y/x);
    if (x < 0 && y >= 0)   return half_PI + atan(-x/y);
    if (x < 0 && y < 0)  return PI + atan(y/x);
    return half_PI + PI + atan(-x/y);
}


void main()
{
    f_colour = a_colour;
    f_tex_coords = a_tex_coords;

    float n_angle = get_angle(a_pos.x - u_rotation.y, a_pos.y - u_rotation.z);
    float n_distance = get_distance(a_pos.x - u_rotation.y, a_pos.y - u_rotation.z);

    gl_Position = u_projection * u_view * vec4(u_transform.xy + u_scale.x * u_scale.yz * a_pos.xy,0.0, 1.0);
}

#type fragment
#version 330 core

uniform float u_time;
uniform sampler2D TEX_SAMPLER;

in vec4 f_colour;
in vec2 f_tex_coords;

out vec4 color;

void main()
{
    color = texture(TEX_SAMPLER, f_tex_coords);
}