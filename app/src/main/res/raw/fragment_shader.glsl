precision mediump float;

uniform vec3 uLightPos;
uniform vec4 uColor;

varying vec3 vPosition;
varying vec3 vNormal;

const float cAmbient = 0.3;
const float cDiffuse = 0.5;
const vec4 cSpecularColor = vec4(1.0, 1.0, 1.0, 1.0);

void main() {
	vec3 mLightDir = normalize(normalize(uLightPos - vPosition));
	vec3 mNormal = normalize(normalize(vNormal));
	float mDiffuse = max(dot(mNormal, mLightDir), 0.0);
	float mSpecular = 0.0;
	if (mDiffuse > 0.0) {
		vec3 mViewDir = normalize(normalize(-vPosition));
		vec3 mHalfDir = normalize(normalize(mLightDir + mViewDir));
		mSpecular = pow(max(dot(mHalfDir, mNormal), 0.0), 16.0);
	}
	gl_FragColor = uColor * (cAmbient + cDiffuse * mDiffuse) + cSpecularColor * mSpecular;
}
