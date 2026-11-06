# meteo — shared project instructions

Simple and fast Android weather app, with no ads or trackers: current conditions, hourly and 15-day forecasts, city search, favorites, offline mode, fallback between weather providers.

Audience: Android users who want a plain weather forecast; Julien first.. Stack: Kotlin, Jetpack Compose (Material 3), Retrofit + Gson, Gradle (Kotlin DSL, AGP), JUnit + MockK; weather data from Open-Meteo (primary) and MET Norway (fallback). Background images generated with a Python script (OpenAI)..

## Communication and languages

- Talk to Julien in French.
- Documentation and skills are in English; code, comments and commit messages are in English.
- Before designing anything non-trivial, use the shared clarification workflow.

## Commands

- `./gradlew :app:testDebugUnitTest` — Unit tests
- `./gradlew :newRelease` — Release APK (bumps VERSION_CODE)
- `./gradlew :newDebug` — Debug APK (bumps VERSION_CODE)
- `python3 scripts/generate_weather_wallpapers.py` — Weather backgrounds

## Documentation and tracking

- Documentation lives in `docs/`; no wiki.
- Tasks are GitHub issues of this repository.
- Time spent is recorded in the work journal (github.com/Jyhess/dev-journal).

## Writing

- Write what is needed to act, nothing else: no history (git keeps it), no paraphrase of the code, no copy of ticket content; a link rather than a copy; a decision is its choice and its reason in one line.

## Tool results

- Every tool result stays in the context for the rest of the session: ask only for what is needed. Read a file range rather than the whole file once its size is known; trim command outputs (`head`, `--stat`, `--name-only`, `grep` filters, a tool's own field or format options).

## Vocabulary

- The domain glossary is at `docs/glossary.md`. At the start of each task involving domain concepts, read it and follow its definitions and forbidden synonyms.

## Errors and messages

- Lower layers (domain, application, infrastructure) never return display text. Each error is a typed value (enum variant, error class) whose kind is its identifier and whose typed fields carry the context to show (file path, name, limits).
- The interface turns each kind into its translated message, with an exhaustive match where the language has one, so that a kind without a message does not compile.
- A nested cause is a nested error; text coming from outside (operating system, libraries) stays an untranslated payload.
- Lower layers describe their errors in English for logs (`Display`, `toString`…).

## Robustness by design

- A rule the code must follow is guaranteed by its structure (types, compiler, a single source of truth) or by a failing test; never by a comment or an instruction to remember ("remember to…", "every property is copied here").
- Before choosing a design, picture its most likely change (one more property, case or type) and check what it would break. Prefer the design where that omission does not compile or cannot happen; otherwise, a test that finds new cases by itself (reflection, exhaustive `match`) and fails.
- No computed state in a copyable object (`record`, `struct`, value type): the computation lives apart, keyed by the instance.
- A regression test is proven by making it fail: reintroduce the defect it targets, see it fail, then remove the defect.
- A ticket's implementation notes follow the same rules.

## Provisional values

- When a value the task does not give is needed (threshold, duration, limit…), pick a provisional one, keep it in a named constant or a config entry, and go on.
- Before the PR, ask Julien about each one: confirm, change, or defer to a ticket. Confirmed or changed values are written into the ticket; a deferred value gets a ticket and is listed under "Values to validate" in the PR description, and that PR always goes to review.

## Known limitations

- A known limitation is anything deliberately left incomplete or simplified. Write it where it applies: README, documentation, comment or TODO; every TODO cites a ticket.
- At the end of the task and in the PR description, list the limitations, then create their tickets with Julien's agreement; the documentation or comment then cites the ticket.

## Verification

Before each commit: `./gradlew :app:testDebugUnitTest` (pre-commit hook in `.githooks/`). Never bypass it.

## Git

- One commit per feature, fix or change; commit messages in English.
- Push only when asked.
- Every branch starts from the default branch (`origin/<default branch>` after `git fetch`), never from another task's branch. Work that depends on an unmerged branch waits for it or, with Julien's go, starts from the default branch without that code and is rebased once it is merged.
- Keep the branch current with the default branch: `git fetch`, then `git rebase --autostash origin/<default branch>`, when starting or resuming a task on a branch and before each push or PR (on the default branch itself: `git pull --rebase --autostash`). Resolve mechanical conflicts (imports, lists, neighbouring lines) and show them; on a conflict of substance, `git rebase --abort` and describe it. A branch already pushed goes back with `git push --force-with-lease`.
