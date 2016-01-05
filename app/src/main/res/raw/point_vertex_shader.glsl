precision mediump float;

attribute vec4 a_Position;
attribute vec4 a_Color;
uniform mat4 u_Matrix;
uniform float u_PointSize;

varying vec4 v_Color;

float rgbColor = 256.0;
float alpha = 0.2;

void main() {
    //v_Color = a_Color;
    v_Color = vec4(u_PointSize / rgbColor, ((rgbColor - u_PointSize) * alpha) / rgbColor, (rgbColor - u_PointSize) / rgbColor, 1);
    gl_Position = u_Matrix * a_Position;
    //gl_Position = a_Position;
    gl_PointSize = u_PointSize;
}