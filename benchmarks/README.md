# Benchmarks

JMH microbenchmarks for the simulation hot paths flagged in
[#27](https://github.com/lamao/bacillus/issues/27). No profiler/benchmark
harness existed before this module - perf claims were static-analysis or
profiler-line-attribution guesses (see the discussion on
[#28](https://github.com/lamao/bacillus/issues/28)/#29 about why per-line
profiler numbers are unreliable once a hot function is `inline`d).

## Running

```bash
./gradlew benchmarks:jmh
```

Run a subset with `-PjmhInclude=<regex>` (matches benchmark class/method
name), e.g.:

```bash
./gradlew benchmarks:jmh -PjmhInclude=FieldIterateRadialBenchmark
```

Results print to the console and are also written under
`benchmarks/build/results/jmh/`.

## What's covered

- `FieldIterateRadialBenchmark` - the raw cost of `Field.iterateRadial`'s
  ring-walk at the two range values `Settings` actually configures
  (`VisionRange`/`ProductionRange` = 1, `ConsumingRange`/`ToxinRange` = 2),
  across populations of 100/500/1000 organics.
- `SimulationStepBenchmark` - end-to-end cost of `LookUpStep` and
  `ToxinStep` over a populated field. These two don't add/remove field
  entries, so a single fixture can be reused across every invocation
  without the result drifting.

## What's not covered yet

`ConsumeStep`, `ProduceStep`, and a full `Environment.doTic` all mutate the
field (drain or add minerals) as they run, so repeatedly invoking them
against one shared `Field` would benchmark a field whose occupancy keeps
changing rather than a steady state. Doing this correctly needs a fresh
`Field` per invocation (JMH's `Level.Invocation` setup), which risks the
per-invocation rebuild cost swamping the measurement for a small field -
left as a follow-up rather than shipping a misleading number.

## Fixtures

`BenchmarkFixtures` scatters organics and minerals with randomized
substances/sizes across a `Settings.GridWidth x GridHeight` field (a fixed
seed, so runs are reproducible). Benchmark sources are Java rather than
Kotlin: `@Benchmark` classes need JMH's annotation processor, which the
`me.champeau.jmh` Gradle plugin wires up for a plain `javac` source set with
no extra configuration - doing the same from Kotlin needs `kapt` wired
through that plugin by hand, which wasn't worth the setup cost for this
harness.
