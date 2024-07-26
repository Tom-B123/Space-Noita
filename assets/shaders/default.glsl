#type vertex
#version 330 core
layout (location=0) in vec3 a_pos;
layout (location=1) in vec4 a_colour;
layout (location=2) in vec2 a_tex_coords;

uniform mat4 u_projection;
uniform mat4 u_view;
uniform vec2 u_transform;

out vec4 f_colour;
out vec2 f_tex_coords;

void main()
{
    f_colour = a_colour;
    f_tex_coords = a_tex_coords;
    gl_Position = u_projection * u_view * vec4(a_pos.xy + u_transform,0.0, 1.0);
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