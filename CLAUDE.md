# CLAUDE.md

Read [AGENTS.md](AGENTS.md) first — it holds everything about this repository. This file only adds
what is specific to Claude Code.

## Long-running commands

Run the gate with `run_in_background: true`, not with an explicit `timeout`. The `Bash` tool's
documented maximum is 600000 ms and it clamps anything larger silently, so `timeout: 900000` reads
like fifteen minutes and is ten. A gate that overruns is not lost — the tool moves it to the
background anyway and notifies you — but it has held the turn open for the whole ten minutes first,
and the foreground read is cut off mid-build. Backgrounding deliberately costs none of that.

Guard-set runs need no `timeout` at all: the two-class set printed in `AGENTS.md` § *Verification*
finishes in about ten seconds, an order of magnitude inside the default.

A build started with `run_in_background` reports the status of its **last** process, so an `echo`
after `./gradlew` masks a failure as exit 0. The gate shape in `AGENTS.md` already accounts for
that — use it as written and backgrounding is safe.

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
