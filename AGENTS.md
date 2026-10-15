# Repository instructions

- Never add generated build artifacts to Git: no built mod JARs, source JARs,
  ZIP archives, compiled classes, build directories, caches or local game runs.
- Publish installable JARs as GitHub Release assets or temporary Actions artifacts.
- The checked-in Gradle wrapper JAR is build tooling, not a generated mod artifact;
  preserve it so a clean checkout can build without a preinstalled Gradle.
- Keep equivalent functionality, descriptions and translations in `mc1.21.11`,
  `mc26.2` and `mc26.3`, while respecting their different Minecraft and Java interfaces.
- Read the shared mod version from root `version.txt`. Release Please updates it.
- Use Conventional Commits (`fix:`, `feat:`, `chore:`) for automatic release notes.
- Build all targets and run their geometry audits before publishing a change.
- Do not add credentials, tokens or secrets to tracked files.
