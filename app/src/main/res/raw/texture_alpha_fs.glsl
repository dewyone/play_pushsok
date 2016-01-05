
precision mediump float;

uniform sampler2D u_TextureUnit;        // Texture Unit Number :  (e.g. GLES20.GL_TEXTURE0 --> 0)
varying vec2 v_TextureCoordinates;

uniform float u_ColorAlpha;
vec4 baseColor;
float alphaValue = 0.5;

void main() {
    baseColor = texture2D(u_TextureUnit, v_TextureCoordinates);
    baseColor.a = baseColor.a * u_ColorAlpha;
    gl_FragColor = baseColor;
}
