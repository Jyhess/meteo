#!/usr/bin/env python3
"""Generate weather-themed wallpapers with OpenAI.

The script builds one prompt per weather condition from a fixed base prompt
plus a weather-specific variation. Existing files are kept by default and only
regenerated when --override is passed.
"""

from __future__ import annotations

import argparse
import base64
import re
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable

from openai import OpenAI


@dataclass(frozen=True)
class WeatherSpec:
    key: str
    description: str
    visual_hint: str


WEATHER_SPECS: tuple[WeatherSpec, ...] = (
    WeatherSpec("CLEAR", "Clear sky", "bright open blue sky, clean sunlight, airy atmosphere, soft sun glow"),
    WeatherSpec("MOSTLY_CLEAR", "Mostly clear", "mostly clear sky with a few soft clouds, bright daylight, gentle warmth"),
    WeatherSpec("PARTLY_CLOUDY", "Partly cloudy", "partly cloudy sky, balanced sun and clouds, soft contrast, calm daylight"),
    WeatherSpec("OVERCAST", "Overcast", "dense overcast sky, thick cloud blanket, muted light, quiet and cinematic mood"),
    WeatherSpec("FOG", "Fog", "thick fog layers, low visibility, diffused light, soft gradients, hushed atmosphere"),
    WeatherSpec("RIME_FOG", "Rime fog", "freezing fog, pale icy haze, delicate frost crystals, cold muted daylight"),
    WeatherSpec("LIGHT_DRIZZLE", "Light drizzle", "light drizzle, fine raindrops on clean glass, wet atmosphere, gray daylight, subtle moisture"),
    WeatherSpec("MODERATE_DRIZZLE", "Moderate drizzle", "moderate drizzle, more visible raindrops on clean glass, soft gray sky"),
    WeatherSpec("DENSE_DRIZZLE", "Dense drizzle", "dense drizzle, frequent droplets, textured wet glass, subdued and atmospheric"),
    WeatherSpec("LIGHT_FREEZING_DRIZZLE", "Light freezing drizzle", "light freezing drizzle, icy moisture on glass, cold gray light, delicate frost"),
    WeatherSpec("DENSE_FREEZING_DRIZZLE", "Dense freezing drizzle", "dense freezing drizzle, icy wet surface, fine frost accumulation, cold cinematic realism"),
    WeatherSpec("LIGHT_RAIN", "Light rain", "light rain, realistic raindrops on clean glass, soft gray daylight"),
    WeatherSpec("MODERATE_RAIN", "Moderate rain", "moderate rain, heavier raindrops on clean glass, moody gray sky"),
    WeatherSpec("HEAVY_RAIN", "Heavy rain", "heavy rain, dense raindrops on clean glass, dramatic storm mood"),
    WeatherSpec("LIGHT_FREEZING_RAIN", "Light freezing rain", "light freezing rain, slick icy droplets, cold sheen on the glass, pale winter light"),
    WeatherSpec("HEAVY_FREEZING_RAIN", "Heavy freezing rain", "heavy freezing rain, icy rain coating, thick cold droplets, intense winter atmosphere"),
    WeatherSpec("SLIGHT_SNOWFALL", "Light snowfall", "light snowfall, soft snowflakes in the air, gentle white ambiance, quiet winter mood"),
    WeatherSpec("MODERATE_SNOWFALL", "Moderate snowfall", "moderate snowfall, visible flakes drifting across the scene, muted sky, serene winter mood"),
    WeatherSpec("HEAVY_SNOWFALL", "Heavy snowfall", "heavy snowfall, dense falling snow, reduced visibility, immersive winter atmosphere"),
    WeatherSpec("SNOW_GRAINS", "Snow grains", "tiny snow grains, granular wintry texture, pale cold daylight, minimal composition"),
    WeatherSpec("RAIN_SHOWERS_SLIGHT", "Slight rain showers", "brief light showers, intermittent raindrops, sky breaks between clouds, calm mood"),
    WeatherSpec("RAIN_SHOWERS_MODERATE", "Moderate rain showers", "intermittent moderate showers, raindrops and moving cloud layers, dynamic but calm"),
    WeatherSpec("RAIN_SHOWERS_VIOLENT", "Violent rain showers", "violent rain showers, intense falling rain, storm energy, dramatic cinematic lighting"),
    WeatherSpec("SNOW_SHOWERS_SLIGHT", "Slight snow showers", "light snow showers, intermittent flakes, soft drifting movement, tranquil winter scene"),
    WeatherSpec("SNOW_SHOWERS_HEAVY", "Heavy snow showers", "heavy snow showers, dense gusts of snow, swirling flakes, dramatic whiteout mood"),
    WeatherSpec("THUNDERSTORM", "Thunderstorm", "dark thunderstorm sky, distant lightning glow, dramatic clouds, tense cinematic atmosphere"),
    WeatherSpec("THUNDERSTORM_HAIL", "Thunderstorm with hail", "thunderstorm with hail, stormy clouds, icy pellets, lightning-lit dramatic sky"),
    WeatherSpec("VARIABLE", "Variable weather", "variable weather with layered clouds and subtle breaks of light, dynamic but balanced sky"),
    WeatherSpec("UNKNOWN", "Unknown weather", "neutral atmospheric sky, abstract but weather-inspired, calm and flexible composition"),
)

COMMON_PROMPT = (
    "Looking through a pristine, transparent clean glass surface at the sky, realistic atmospheric details, "
    "soft cinematic daylight, elegant minimal composition, space for UI, vertical composition 20:9 for mobile, "
    "ultra detailed, no people, no buildings, no text, no window frame, no dirt, no stains, no smudges, no grime"
)

DEFAULT_MODEL = "gpt-image-1"
DEFAULT_SIZE = "auto"
#DEFAULT_SIZE = "1600x3600"


def slugify(value: str) -> str:
    value = value.strip().lower()
    value = re.sub(r"[^a-z0-9]+", "_", value)
    return value.strip("_") or "weather"


def build_prompt(spec: WeatherSpec) -> str:
    return f"{COMMON_PROMPT}, {spec.visual_hint}."


def ensure_parent(path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)


def iter_specs(selected: Iterable[str] | None) -> tuple[WeatherSpec, ...]:
    if not selected:
        return WEATHER_SPECS

    selected_keys = {value.upper() for value in selected}
    return tuple(spec for spec in WEATHER_SPECS if spec.key in selected_keys)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Generate weather wallpapers with OpenAI.")
    parser.add_argument(
        "--output-dir",
        default="app/src/main/res/drawable/",
        help="Directory where generated images and metadata are stored. (default: app/src/main/res/drawable/)",
    )
    parser.add_argument(
        "--model",
        default=DEFAULT_MODEL,
        help=f"OpenAI image model to use. (default: {DEFAULT_MODEL})",
    )
    parser.add_argument(
        "--size",
        default=DEFAULT_SIZE,
        help=f"Image size to request from OpenAI. (default: {DEFAULT_SIZE})",
    )
    parser.add_argument(
        "--quality",
        default="high",
        help="Image quality passed to the API. (default: high)",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Print prompts without calling the API.",
    )
    parser.add_argument(
        "--override",
        action="store_true",
        help="Regenerate images even when the target file already exists.",
    )
    parser.add_argument(
        "--weather",
        action="append",
        help="Limit generation to one or more condition keys, for example --weather CLEAR --weather RAIN_SHOWERS_MODERATE. (default: all conditions)",
    )
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    output_dir = Path(args.output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)

    specs = iter_specs(args.weather)

    client = None if args.dry_run else OpenAI()

    for spec in specs:
        filename = f"bg_{slugify(spec.key)}.png"
        image_path = output_dir / filename
        prompt = build_prompt(spec)

        if image_path.exists() and not args.override:
            print(f"SKIP {spec.key}: already exists -> {image_path}")
            continue

        print(f"PROMPT {spec.key}: {prompt}")

        if args.dry_run:
            continue

        result = client.images.generate(
            model=args.model,
            prompt=prompt,
            size=args.size,
            quality=args.quality,
        )

        image_b64 = result.data[0].b64_json
        if not image_b64:
            raise RuntimeError(f"OpenAI returned no image for {spec.key}")

        ensure_parent(image_path)
        image_path.write_bytes(base64.b64decode(image_b64))
        print(f"DONE {spec.key}: saved -> {image_path}")

    return 0


if __name__ == "__main__":
    raise SystemExit(main())