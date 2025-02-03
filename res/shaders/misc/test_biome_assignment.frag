#version 430

in vec2 outUV;
out vec4 fragColor;

#include "shaders/lib/simplex_noise.glsl"

struct biome_params {
    // How far "inland". 0.2 = coast, -1 = far ocean, 1 = far inland.
    // Continentalness should create a baseline for height and allow the generation of mountains.
    // Mountains can only generate when continentalness >= 0.4
    float continentalness;
    float temperature;
    float humidity;
    float fertility;
};

biome_params generate_biome_params(vec2 pos) {
    // Continentalness should vary slowly in order to create large continents
    float continentalness = simplex_noise(pos * 1.0 + 10000.1);

    // Inland temperature should vary rather quickly in order to create interesting
    // variation
    float raw_temperature = simplex_noise(pos * 1.6 - 10000.1);
    // Ocean temperature should vary rather slowly, and less extremely
    float ocean_temperature = simplex_noise(pos * 0.4 - 20000.1) * 0.6;
    // However, temperature should be influenced by continentalness -- inland regions may
    // have more extreme variations in temperature, while coastal regions will have more
    // uniform temperatures
    float temperature = ocean_temperature + (raw_temperature - ocean_temperature) * (tanh(3.0 * continentalness) / (2.0 * tanh(3.0)) + 0.5);
    // Humidity should vary rather quickly in order to create interesting variation
    float raw_humidity = simplex_noise(pos * 1.6 + 40263.);
    // Humidity should be influenced by continentalness -- the further inland, the drier.
    float humidity = pow(raw_humidity + 1.0, continentalness + 1.0) / pow(2.0, continentalness) - 1.0;
    // Humidity should be influenced by temperature -- the colder, the less humidity possible.
    humidity = (humidity * temperature + humidity + temperature - 1.0) / 2.0

    // Fertility should vary rather quickly in order to create interesting variation
    float fertility = simplex_noise(pos * 2.0);

    return biome_params(continentalness, temperature, humidity, fertility);
}

const vec3 BIOME_COLD_OCEAN = vec3(0, 0, 0.8);
const vec3 BIOME_TEMPERATE_OCEAN = vec3(0, .3, 1);
const vec3 BIOME_TROPICAL_OCEAN = vec3(0, .5, 1);
const vec3 BIOME_CONTINENT = vec3(0, 0.7, 0.3);

vec3 get_biome(biome_params params) {
    float temperature = params.temperature;
    float humidity = params.humidity;
    float continentalness = params.continentalness;

    if(continentalness <= 0.2 || true) {
        // Ocean biome
        if(temperature < -0.3) {
            return BIOME_COLD_OCEAN;
        }
        else if(temperature < 0.3) {
            return BIOME_TEMPERATE_OCEAN;
        }
        else {
            return BIOME_TROPICAL_OCEAN;
        }
    }
    else {
        // Continental biome
        return BIOME_CONTINENT;
    }
}

void main() {
    vec2 pos = outUV.xy * 5.0;
    biome_params p = generate_biome_params(pos);
    vec3 biome_color = get_biome(p);
    if(p.continentalness >= 0.2) {
        biome_color = (biome_color + BIOME_CONTINENT) * 0.5;
    }

    fragColor = vec4(biome_color, 1.0);
}