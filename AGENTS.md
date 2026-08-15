# ComfyStory Codex Project Instructions

This project is based on the Swordie v232.2 MapleStory server source.

The goal is to keep the game stable and preserve existing behavior unless a change is explicitly requested. Prefer small, targeted changes over unnecessarily broad rewrites.

## General development rules

- Preserve existing Swordie behavior unless I explicitly ask for a change.
- Prefer the smallest practical change that accomplishes the requested behavior.
- Do not perform unrelated refactors, cleanup, formatting sweeps, renames, dependency upgrades, or architecture changes unless they are required for the requested task.
- Reuse existing project patterns and abstractions instead of inventing new systems when possible.
- Before changing unfamiliar behavior, trace the existing implementation first.
- If a requested change appears likely to require substantial reverse engineering, invasive changes, or broad architectural work, explain the implications before proceeding.
- If a request is impractical or disproportionately risky for the benefit, say so rather than forcing an implementation.
- Do not silently change unrelated gameplay behavior.

## Stability priorities

Stability and broad class playability are important.

Assume players may use any implemented class.

When modifying shared systems, consider their impact across classes, content, and existing game systems.

Avoid regressions to existing functionality that is unrelated to the requested change.

Prefer configuration, scripts, data definitions, or existing hooks over invasive engine changes when they are suitable for the requested change.

## Client directory rules

The project contains three client-related directories.

### `client-original/`

- Pristine, untouched MapleStory v232.2 client.
- Treat this directory as read-only.
- Never modify, delete, rename, or overwrite files here.
- It exists as the known-good baseline for comparison and rebuilding the working client.

### `client/`

- Working MapleStory v232.2 client used by the server and for testing.
- Client-side changes may be made here.
- This directory is intentionally not tracked by Git.
- Large binaries and WZ files may be modified here when required.

### `client-mods/`

- Git-tracked source of truth for custom client-side modifications.
- Represent client-side changes here in reproducible form whenever practical.

For client-side work:

- Never commit the full MapleStory client or full WZ trees to Git.
- Do not add large generated client binaries directly to normal Git history.
- Changes to large files under `client/` are allowed.
- When practical, preserve the corresponding modification under `client-mods/` using source code, configuration, scripts, patch files, transformation tools, documentation, or another reproducible method.
- Prefer tracked source/build inputs when available rather than treating a generated binary as the only copy of a modification.
- A fresh copy of `client-original/` should ideally be reproducible into the customized client using the contents of `client-mods/`.
- If reproducibly preserving a particular client modification would be impractical, explain that rather than committing a huge binary to Git.

## WZ/client handling

The server currently reads WZ data from the working `client/` directory.

- Do not modify `client-original/`.
- avoid modifying giant WZ files unnecessarily.
- Distinguish between server-readable game data and behavior that actually requires a client modification.
- Do not duplicate or modify client data unnecessarily when the same result can be achieved more cleanly elsewhere.
- Preserve client modifications reproducibly under `client-mods/` when practical.

## Git rules

This project preserves the original Swordie commit history, with ComfyStory development continuing on top of it.

The project's private personal Git repository is the only repository intended to receive ComfyStory changes.

- Never push, publish, submit a pull request, or otherwise send ComfyStory changes to the original Swordie repository.
- Do not rewrite, squash, rebase away, or otherwise alter the inherited Swordie history.
- Do not initialize a new unrelated Git history when the inherited history is available.
- Development occurs directly in the live project rather than through a separate development/staging branch.
- Keep changes logically scoped and easy to understand or revert.
- Do not use destructive Git commands unless explicitly requested.
- Before a large or risky change, inspect the current Git state and preserve unrelated user changes.
- Do not commit `client/` or `client-original/`.

Unless explicitly asked to commit, make the requested changes and report what changed without automatically creating a commit.

## Secrets and private configuration

Do not commit:

- passwords
- authentication tokens
- private credentials
- secret keys
- other sensitive authentication material

Avoid exposing secrets in logs, documentation, or generated files that will be committed.

Do not perform unrelated configuration refactors merely to move existing settings unless I ask for them.

## Database rules

The server uses MySQL.

- Preserve existing schema compatibility unless a requested change requires otherwise.
- Avoid destructive schema or player-data operations unless explicitly requested.
- If a code change requires a database migration, make that requirement explicit.
- Do not modify or delete existing player data unnecessarily.
- Before performing an operation with meaningful risk of player-data loss, explain the impact.
- Prefer changes that remain compatible with existing saved characters and accounts when practical.

## Live development environment

This is a small personal server and development is performed directly against the live project.

Do not introduce staging, deployment pipelines, branch workflows, or other production-development infrastructure unless I specifically ask for it.

For changes that could realistically damage saved data or make the server difficult to restore:

- identify the risk before making the change
- prefer an easily reversible implementation
- recommend a backup or Git checkpoint when appropriate

Do not treat ordinary low-risk gameplay changes as requiring production-grade deployment procedures.

## Investigation requests

When I ask you to investigate, audit, inspect, find, trace, or explain something:

- do not modify files unless I explicitly ask you to
- identify the relevant files, classes, scripts, or client data
- explain the current behavior
- identify meaningful risks and dependencies
- distinguish server-side behavior from client-side behavior
- mention relevant existing custom Swordie behavior when discovered

## Implementation requests

When I ask you to make a change:

1. Inspect the existing implementation.
2. Identify an appropriate, reasonably contained solution.
3. Make the requested change.
4. Check for obvious compile, import, reference, or data issues.
5. Run relevant lightweight validation when practical.
6. Do not fix unrelated failures unless they block validation of the requested work.
7. Summarize:
    - what changed
    - which files changed
    - whether server, client, or database behavior was affected
    - how I should test it in game
    - any meaningful caveats

If a build or test failure already existed and is unrelated to the requested work, call that out clearly.

## Communication style

Keep responses concise and practical.

For completed work, prioritize:

- what changed
- where it changed
- how to test it
- meaningful caveats

Do not provide long tutorials unless I ask for them.

When multiple approaches have meaningful tradeoffs, explain them briefly before making a major design choice.
