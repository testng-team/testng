# CLAUDE.md

Read [AGENTS.md](AGENTS.md) first — it holds everything about this repository. This file only adds
what is specific to Claude Code.

## Long-running commands

The `Bash` tool caps `timeout` at **600000 ms** and clamps anything larger without saying so, so
`timeout: 900000` is not a longer timeout — it is the same ten minutes, written in a way that reads
like fifteen. Passing an explicit timeout is therefore the wrong mechanism for the gate: it cannot
be given more than the cap. Background it instead, which has no cap and re-invokes you when the
build exits:

```text
run_in_background: true
```

A cold `clean build` measured 6m39s here, so ten minutes is a margin rather than a wall — but it is
a margin that shrinks as the suite grows, and the runs that go long are the ones you cannot predict,
such as the first build after a rebase that pulled a batch of commits. Keep an explicit `timeout`
for what it is actually good for: the filtered guard-set runs in `AGENTS.md` § *Verification*, which
finish in tens of seconds.

A build started with `run_in_background` reports the status of its **last** process, so an `echo`
after `./gradlew` masks a failure as exit 0. That is exactly why the gate shape in `AGENTS.md`
writes `EXIT=` into the log rather than to stdout — use it as written and backgrounding is safe.

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
