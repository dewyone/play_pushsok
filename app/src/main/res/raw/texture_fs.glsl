
precision mediump float;

uniform sampler2D u_TextureUnit;        // Texture Unit Number :  (e.g. GLES20.GL_TEXTURE0 --> 0)
varying vec2 v_TextureCoordinates;

void main() {
    gl_FragColor = texture2D(u_TextureUnit, v_TextureCoordinates);
}
