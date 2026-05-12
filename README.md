# Meteo - Simple Weather App

Meteo is a simple and fast Android weather app, with no ads or trackers.

## ✨ Features

- **Complete Forecasts**: View current conditions, hourly forecasts (next 12 hours), and daily forecasts (15 days).
- **City Search**: Search for any city worldwide.
- **Favorites Management**: Save preferred locations for quick access.
- **Offline Mode**: Access the latest fetched data even without an internet connection.
- **Resilience (Multi-Source)**: Smart fallback architecture. If the primary provider is unavailable, the app automatically switches to a secondary provider.

## 🚀 Installation

1. Clone the repository:

    ```bash
    git clone https://github.com/your-username/Meteo.git
    ```

2. Open the project in **Android Studio Ladybug** (or newer).
3. Sync the project with Gradle files.
4. Run the app on an emulator or a physical device.

## 📦 Build APK

Build a release APK:

```bash
./gradlew :app:assembleRelease
```

APK path:

- `app/build/outputs/apk/release/app-release.apk`

Optional install with ADB:

```bash
adb install -r app/build/outputs/apk/release/app-release.apk
```

## 🎨 Generate Weather Backgrounds

Weather backgrounds are generated with OpenAI.
You can generate or regenerate images using [scripts/generate_weather_wallpapers.py](scripts/generate_weather_wallpapers.py).

1. Install the Python dependency:

    ```bash
    pip install openai
    ```

2. Export your OpenAI API key:

    ```bash
    export OPENAI_API_KEY="your_api_key"
    ```

3. Generate all missing backgrounds (default output: `app/src/main/res/drawable/`):

    ```bash
    python3 scripts/generate_weather_wallpapers.py
    ```

4. Generate one weather condition only (if missing):

    ```bash
    python3 scripts/generate_weather_wallpapers.py --weather PARTLY_CLOUDY
    ```

5. Force-regenerate one weather condition:

    ```bash
    python3 scripts/generate_weather_wallpapers.py --weather PARTLY_CLOUDY --override
    ```

Notes:

- Run commands from the repository root.
- Existing files are skipped by default unless `--override` is used.
- Use `--dry-run` to print prompts without calling the API.

## 📝 License

This project is licensed under **GPL v3**. See [LICENSE](LICENSE) for details.

### Third Party

Icons are from [Airycons](https://github.com/HaroleDev/Airycons).

---
Built with care to keep weather always accessible.
