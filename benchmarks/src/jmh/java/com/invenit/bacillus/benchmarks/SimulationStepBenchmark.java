package com.invenit.bacillus.benchmarks;

import com.invenit.bacillus.ServiceContext;
import com.invenit.bacillus.model.Field;
import com.invenit.bacillus.stage.LookUpStep;
import com.invenit.bacillus.stage.ToxinStep;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;

/**
 * End-to-end per-step cost over a populated field, for the two steps that
 * only mutate an Organic's own direction/energy (LookUpStep, ToxinStep) and
 * so can safely reuse one Field across every invocation without the result
 * drifting.
 *
 * ConsumeStep/ProduceStep/a full Environment.doTic are intentionally left
 * out here: they add or drain minerals, so repeated invocations against a
 * shared Field would measure a field whose occupancy keeps changing rather
 * than a steady state. Benchmarking those correctly needs a fresh Field per
 * invocation (JMH's Level.Invocation setup) - left as a follow-up since that
 * setup cost has to be kept from swamping the measurement itself.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 3, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
public class SimulationStepBenchmark {

    @Param({"100", "500", "1000"})
    public int populationSize;

    private final LookUpStep lookUpStep = new LookUpStep(ServiceContext.INSTANCE.getRandomService());
    private final ToxinStep toxinStep = new ToxinStep();
    private Field field;

    @Setup(Level.Trial)
    public void setUp() {
        field = BenchmarkFixtures.populatedField(populationSize, 7L);
    }

    @Benchmark
    public void lookUpStep() {
        lookUpStep.execute(field);
    }

    @Benchmark
    public void toxinStep() {
        toxinStep.execute(field);
    }
}
