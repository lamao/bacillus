package com.invenit.bacillus.benchmarks;

import com.invenit.bacillus.Settings;
import com.invenit.bacillus.model.Field;
import com.invenit.bacillus.model.Point;
import com.invenit.bacillus.model.Something;
import com.invenit.bacillus.model.Substance;
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
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

/**
 * Isolates the cost of Field.iterateRadial's ring-walk itself (issue #28):
 * a lambda that mirrors the "read a cell, compare its body substance"
 * shape every real caller (DecideStep, ConsumeStep, ProduceStep, ToxinStep)
 * uses, at the two range values Settings actually configures.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 3, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
public class FieldIterateRadialBenchmark {

    @Param({"100", "500", "1000"})
    public int populationSize;

    private Field field;
    private Point anchor;

    @Setup(Level.Trial)
    public void setUp() {
        field = BenchmarkFixtures.populatedField(populationSize, 42L);
        anchor = new Point(Settings.GridWidth / 2, Settings.GridHeight / 2);
    }

    @Benchmark
    public void rangeUsedByVisionAndProduction(Blackhole blackhole) {
        field.iterateRadial(anchor, Settings.VisionRange, (x, y) -> {
            blackhole.consume(matchesGreen(field.get(x, y)));
            return true;
        });
    }

    @Benchmark
    public void rangeUsedByConsumingAndToxin(Blackhole blackhole) {
        field.iterateRadial(anchor, Settings.ToxinRange, (x, y) -> {
            blackhole.consume(matchesGreen(field.get(x, y)));
            return true;
        });
    }

    private static boolean matchesGreen(Something something) {
        return something != null && something.getBody() == Substance.Green;
    }
}
