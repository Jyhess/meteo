# meteo — shared project instructions

Simple and fast Android weather app, with no ads or trackers: current conditions, hourly and 15-day forecasts, city search, favorites, offline mode, fallback between weather providers.

Audience: Android users who want a plain weather forecast; Julien first. Stack: Kotlin, Jetpack Compose (Material 3), Retrofit + Gson, Gradle (Kotlin DSL, AGP), JUnit + MockK; weather data from Open-Meteo (primary) and MET Norway (fallback). Background images generated with a Python script (OpenAI).

## Communication and languages

- Talk to Julien in French.
- Documentation and skills are in English; code, comments and commit messages are in English.
- A question gets an answer, not an action: change files, tickets or remote state only on an explicit request; "Is it possible to…?" is a question: answer, propose, wait.
- Before designing anything non-trivial, use the shared clarification workflow.

## Commands

- `./gradlew :app:testDebugUnitTest` — Unit tests
- `./gradlew :newRelease` — Release APK (bumps VERSION_CODE)
- `./gradlew :newDebug` — Debug APK (bumps VERSION_CODE)
- `python3 scripts/generate_weather_wallpapers.py` — Weather backgrounds

## Documentation and tracking

- Documentation lives in `docs/`; no wiki.
- Tasks are GitHub issues of this repository.
- Time spent is recorded in the work journal (plugin `dev-journal`).

## Writing

- Write what is needed to act, nothing else: no history (git keeps it), no paraphrase of the code, no copy of a ticket (link it); a decision is its choice and its reason in one line.

## Context

- Every tool result stays in the context: ask only for what is needed (file ranges, `head`, `--stat`, `grep`, a tool's own filters).
- One ticket per session. When the context-size hook warns, finish the current work, then ask Julien whether to hand over as it says.

## Vocabulary

- The domain glossary is at `docs/glossary.md`: read it before a task involving domain concepts, and follow its definitions and forbidden synonyms.

## Design

- Before designing or changing code, read `docs/design/principles.md` (errors, robustness by design).

## Architecture

- Components (each package right under `com.meteo.app`) and their allowed dependencies: `docs/architecture.puml`, checked by `app/src/test/java/com/meteo/app/ArchitectureTest.kt` (ArchUnit, with the unit tests: `./gradlew :app:testDebugUnitTest`) and by `.github/scripts/architecture.py` (every package declared). Run both to check a change. Rewrite them from the code: `python3 .github/scripts/architecture.py --write` (components), then `ARCHITECTURE_DIAGRAM=write ./gradlew :app:testDebugUnitTest` (arrows).
- Declare dependencies in the version catalog `gradle/libs.versions.toml`, not as coordinates in the build files.
- The allowed dependencies form no cycle between components: `.github/scripts/architecture.py` fails on one that its architecture file does not admit as an `allowed cycle`.
- Changing these components or their checks is an architecture change, approved by Julien beforehand.

## Provisional values

- A value the task does not give (threshold, duration, limit…) gets a provisional one in a named constant or config entry.
- List them under "Values to validate" in the draft PR, with the label `to acknowledge`, then ask Julien about each: confirm, change, or defer. Confirmed or changed values go into the ticket and the code, and leave the list; a deferred one gets a ticket and stays listed; with none left, drop the label.

## Known limitations

- Anything deliberately left incomplete or simplified is written where it applies (README, doc, comment, or a TODO citing a ticket) and listed at the end of the task and in the PR; their tickets are created with Julien's agreement.

## Verification

- The pre-push hook (`.githooks/`) runs `./gradlew :app:testDebugUnitTest && python3 .github/scripts/architecture.py`; never bypass it.
- When it fails, squash each fix into the commit it corrects: `git commit --fixup <sha>`, then `git -c sequence.editor=: rebase -i --autosquash --autostash origin/<default branch>`.

## Git

- One commit per feature, fix or change; commit messages in English.
- Pull requests: once the task's verification passes, push its branch and open the PR as a draft (`gh pr create --draft`) without asking; in the Claude desktop app, turn on the PR's auto-fix (CI, conflicts, review comments), elsewhere say it is off. Only Julien marks a PR ready (it may then be merged by itself): never `gh pr ready`. Any other push only when asked.
- A task's branch starts from the default branch and stays current with it: `git fetch`, then `git rebase --autostash origin/<default branch>`, when starting or resuming the task and before each push or PR (on the default branch: `git pull --rebase --autostash`). Never branch from another task's branch: work that depends on an unmerged branch or PR starts at once from the default branch, without that code and without asking, and is rebased once it is merged.
- Resolve and show mechanical conflicts (imports, lists, neighbouring lines); on a conflict of substance, `git rebase --abort` and describe it. A pushed branch goes back with `git push --force-with-lease`.
