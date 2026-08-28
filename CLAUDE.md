# CLAUDE.md

Read [AGENTS.md](AGENTS.md) first — it holds everything about this repository. This file only adds
what is specific to Claude Code.

## Long-running commands

`./gradlew build` takes several minutes whenever it has real work to do — well over the default
`Bash` timeout. Pass an explicit one:

```text
timeout: 900000
```

Put `EXIT=` **in the log**, never on stdout — see the gate shape in `AGENTS.md`. In a backgrounded
run the `echo` is the last process, so the harness reports exit 0 for a build that failed.

## Subagents

A dispatched agent reports back on its own: the harness re-invokes you when it completes.
Polling for it re-sends the whole conversation on every filler turn, and a review wave costs
dozens of them — easily the largest avoidable cost in a session. Dispatch, run the independent
work you already know you need, then stop and let the notification arrive.

If plan mode is what makes you keep the turn alive, that is the signal to ask the question now
rather than to manufacture another `echo waiting`.

An agent's finding is a hypothesis. Reproduce it here before acting on it, and before relaying
it — the reproduction is usually one command, and findings have been wrong in both directions:
false positives, and real defects whose stated cause was not the cause.

## Environment

- The workspace may be reset to an older commit between sessions. Run the staleness check from
  `AGENTS.md` before anything else, and fast-forward if needed.
- The SSH agent is not always loaded. If `git push` fails with `Permission denied (publickey)`, push
  over HTTPS using the `gh` credentials:
  `git push https://github.com/<owner>/testng.git <branch>`. With no remote-tracking ref,
  `--force-with-lease` needs the expected SHA spelled out: `--force-with-lease=<branch>:<sha>`.
