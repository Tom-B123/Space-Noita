#type vertex
#version 330 core
layout (location=0) in vec3 a_pos;
layout (location=1) in vec4 a_colour;

uniform mat4 u_projection;
uniform mat4 u_view;
uniform float u_time;

out vec4 f_colour;

void main()
{
    f_colour = a_colour;
    gl_Position = u_projection * u_view * vec4(a_pos, 1.0);
}

#type fragment
#version 330 core

in vec4 f_colour;

out vec4 color;

void main()
{
    color = f_colour;
}