#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D u_texture;
uniform vec2 resolution;
varying vec2 v_texCoords;

void main() {
  // normalize UV
  vec2 uv = v_texCoords;

  // barrel distortion
  vec2 c = uv * 2.0 - 1.0;
  float r2 = dot(c, c);
  c *= (1.2 + 0.2 * r2);
  uv = c * 0.5 + 0.5;

  // chromatic aberration
  vec2 dir = normalize(c);
  float m = r2 * 0.006;
  float r = texture2D(u_texture, uv + dir * m).r;
  float g = texture2D(u_texture, uv).g;
  float b = texture2D(u_texture, uv - dir * m).b;
  vec3 color = vec3(r, g, b);

  gl_FragColor = vec4(color, 1.0);
}

