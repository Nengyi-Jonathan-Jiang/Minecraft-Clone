To calculate the properties for any point
$$u=\frac{\sum_b w_bu_b}{\sum_bw_b}$$
where $u$ is the combined value of the property in 
question, $w_b$ is the weight of each biome, and 
$u_b$ is the value of property $u$ for biome $b$.
This removes discontinuities at biome borders as 
long as the weight functions $w_b$ have a falloff.

<hr/>

There’s lots of different components you can add to your model with varying levels of difficulty. From easiest to hardest, you could add
- altitude: higher altitude means lower temperatures and lower humidity. this will give you snow-capped mountains.
- ocean proximity: being closer to the sea brings the temperature closer to the average temperature of the planet, and increases the humidity. this will give you coastal forests and pangaea-like interior deserts (like Mongolia).
- atmospheric circulation cells: convolve the humidity map with a decaying half-exponential, adjusting the width and direction of the kernel according to a cosine function modeling the global winds. Also scale the humidity itself by the same cosine function. this will give you latitude deserts like the Sahara and interior Australia as well as most rainforests.
- negative rain shadows: like the previous approach but you integrate the altitude windward of you. this will give you deserts like the Chilean desert.
- positive rain shadows: take the directional derivative of the altitude and multiply it with the convolution map you got from the atmospheric circulation cells step. this will give you forests like the Pacific Northwest.
- ocean currents: do a fluid simulation of the ocean portions of the planet to determine the temperature of the water and blur it over onto the nearby landmasses.