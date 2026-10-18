#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D InSampler;
uniform sampler2D DepthSampler;

layout(location = 0) in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(location = 0) out vec4 fragColor;

void main() {
    vec2 oneTexel = 1.0 / InSize;

    // Center samples
    vec4 centerCol = texture(InSampler, texCoord);
    float centerDepth = texture(DepthSampler, texCoord).r;

    // Sky / far void bypass: if depth is at or beyond far plane, keep as deep dark sky
    if (centerDepth >= 0.99995) {
        fragColor = vec4(0.012, 0.015, 0.022, 1.0);
        return;
    }

    // 8-neighborhood Depth sampling for Sobel edge filter
    float d00 = texture(DepthSampler, texCoord + vec2(-oneTexel.x, -oneTexel.y)).r;
    float d01 = texture(DepthSampler, texCoord + vec2( 0.0,        -oneTexel.y)).r;
    float d02 = texture(DepthSampler, texCoord + vec2( oneTexel.x, -oneTexel.y)).r;
    float d10 = texture(DepthSampler, texCoord + vec2(-oneTexel.x,  0.0)).r;
    float d12 = texture(DepthSampler, texCoord + vec2( oneTexel.x,  0.0)).r;
    float d20 = texture(DepthSampler, texCoord + vec2(-oneTexel.x,  oneTexel.y)).r;
    float d21 = texture(DepthSampler, texCoord + vec2( 0.0,         oneTexel.y)).r;
    float d22 = texture(DepthSampler, texCoord + vec2( oneTexel.x,  oneTexel.y)).r;

    // Geometric Sobel gradient
    float depthGx = (d02 + 2.0 * d12 + d22) - (d00 + 2.0 * d10 + d20);
    float depthGy = (d20 + 2.0 * d21 + d22) - (d00 + 2.0 * d01 + d02);
    float depthGrad = sqrt(depthGx * depthGx + depthGy * depthGy);

    // Adaptive distance factor for crisp outlines near and far
    float distFactor = max(1.0 - centerDepth, 0.0001);
    float depthEdge = depthGrad / distFactor;
    float geometricOutline = smoothstep(0.04, 0.18, depthEdge);

    // 8-neighborhood Luminance sampling for internal block details & entity features
    float l00 = dot(texture(InSampler, texCoord + vec2(-oneTexel.x, -oneTexel.y)).rgb, vec3(0.299, 0.587, 0.114));
    float l01 = dot(texture(InSampler, texCoord + vec2( 0.0,        -oneTexel.y)).rgb, vec3(0.299, 0.587, 0.114));
    float l02 = dot(texture(InSampler, texCoord + vec2( oneTexel.x, -oneTexel.y)).rgb, vec3(0.299, 0.587, 0.114));
    float l10 = dot(texture(InSampler, texCoord + vec2(-oneTexel.x,  0.0)).rgb, vec3(0.299, 0.587, 0.114));
    float l12 = dot(texture(InSampler, texCoord + vec2( oneTexel.x,  0.0)).rgb, vec3(0.299, 0.587, 0.114));
    float l20 = dot(texture(InSampler, texCoord + vec2(-oneTexel.x,  oneTexel.y)).rgb, vec3(0.299, 0.587, 0.114));
    float l21 = dot(texture(InSampler, texCoord + vec2( 0.0,         oneTexel.y)).rgb, vec3(0.299, 0.587, 0.114));
    float l22 = dot(texture(InSampler, texCoord + vec2( oneTexel.x,  oneTexel.y)).rgb, vec3(0.299, 0.587, 0.114));

    float lumGx = (l02 + 2.0 * l12 + l22) - (l00 + 2.0 * l10 + l20);
    float lumGy = (l20 + 2.0 * l21 + l22) - (l00 + 2.0 * l01 + l02);
    float lumGrad = sqrt(lumGx * lumGx + lumGy * lumGy);
    float featureOutline = smoothstep(0.12, 0.38, lumGrad);

    // Combine geometric edges and surface feature lines
    float totalOutline = clamp(geometricOutline * 1.5 + featureOutline * 0.85, 0.0, 1.0);

    // Base world is pitch dark (deep obsidian void with faint silhouette hint)
    float centerLum = dot(centerCol.rgb, vec3(0.299, 0.587, 0.114));
    vec3 pitchVoid = vec3(0.012, 0.015, 0.022) + vec3(centerLum * 0.035);

    // Astral Monochrome: stark luminescent silver/white outlines
    vec3 starkSilverWhite = vec3(0.93, 0.96, 1.0);

    // Subtle bloom glow around the wireframe
    vec3 outlineGlow = vec3(0.72, 0.84, 1.0) * pow(totalOutline, 2.0) * 0.4;

    vec3 finalColor = pitchVoid + (starkSilverWhite * totalOutline) + outlineGlow;

    // Subtle high-tech scanline & void noise grain
    float noise = fract(sin(dot(texCoord * InSize, vec2(12.9898, 78.233))) * 43758.5453);
    finalColor += (noise - 0.5) * 0.015;

    fragColor = vec4(finalColor, 1.0);
}
