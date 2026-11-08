# meteo — shared project instructions

Simple and fast Android weather app, with no ads or trackers: current conditions, hourly and 15-day forecasts, city search, favorites, offline mode, fallback between weather providers.

Audience: Android users who want a plain weather forecast; Julien first. Stack: Kotlin, Jetpack Compose (Material 3), Retrofit + Gson, Gradle (Kotlin DSL, AGP), JUnit + MockK; weather data from Open-Meteo (primary) and MET Norway (fallback). Background images generated with a Python script (OpenAI).

## Communication and languages

- Talk to Julien in French.
- Documentation and skills are in English; code, comments and commit messages are in English.

## Commands

- `./gradlew :app:testDebugUnitTest` — Unit tests
- `./gradlew :newRelease` — Release APK (bumps VERSION_CODE)
- `./gradlew :newDebug` — Debug APK (bumps VERSION_CODE)
- `python3 scripts/generate_weather_wallpapers.py` — Weather backgrounds

## Documentation and tracking

- Documentation lives in `docs/`; no wiki.
- Tasks are GitHub issues of this repository.
- Time spent is recorded in the work journal (plugin `dev-journal`).

## Vocabulary

- The domain glossary is at `docs/glossary.md`: read it before a task involving domain concepts, and follow its definitions and forbidden synonyms.

## Design

- Before designing or changing code, read `docs/design/principles.md` (errors, robustness by design).

## Architecture

- Components (each package right under `com.meteo.app`) and their allowed dependencies: `docs/architecture.puml`, checked by `app/src/test/java/com/meteo/app/ArchitectureTest.kt` (ArchUnit, with the unit tests: `./gradlew :app:testDebugUnitTest`) and by `.github/scripts/architecture.py` (every package declared). Run both to check a change. Rewrite them from the code: `python3 .github/scripts/architecture.py --write` (components), then `ARCHITECTURE_DIAGRAM=write ./gradlew :app:testDebugUnitTest` (arrows).
- Declare dependencies in the version catalog `gradle/libs.versions.toml`, not as coordinates in the build files.
- The allowed dependencies form no cycle between components: `.github/scripts/architecture.py` fails on one that its architecture file does not admit as an `allowed cycle`.
- Changing these components or their checks is an architecture change, approved by Julien beforehand.

## Verification

- The pre-push hook (`.githooks/`) runs `./gradlew :app:testDebugUnitTest && python3 .github/scripts/architecture.py`; never bypass it.
- When it fails, squash each fix into the commit it corrects: `git commit --fixup <sha>`, then `git -c sequence.editor=: rebase -i --autosquash --autostash origin/<default branch>`.

## Shared rules

- Shared rules: `~/.dev-kit/rules/shared-rules.md`; Claude Code gets them from the core plugin, other agents read the file at session start; a section of this file that contradicts them wins.
