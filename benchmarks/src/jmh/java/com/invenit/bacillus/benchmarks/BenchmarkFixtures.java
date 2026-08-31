package com.invenit.bacillus.benchmarks;

import com.invenit.bacillus.Settings;
import com.invenit.bacillus.model.DNA;
import com.invenit.bacillus.model.Field;
import com.invenit.bacillus.model.Mineral;
import com.invenit.bacillus.model.Organic;
import com.invenit.bacillus.model.Point;
import com.invenit.bacillus.model.Substance;
import com.invenit.bacillus.model.matrix.Action;
import com.invenit.bacillus.model.matrix.Comparator;
import com.invenit.bacillus.model.matrix.DecisionMatrix;
import com.invenit.bacillus.model.matrix.Instruction;
import com.invenit.bacillus.model.matrix.Sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Builds Field snapshots for benchmarking, at a size/density representative
 * of a populated simulation (Settings.GridWidth x GridHeight, a mix of
 * organics and minerals spread across random positions).
 */
final class BenchmarkFixtures {

    private BenchmarkFixtures() {
    }

    static Field populatedField(int organicCount, long seed) {
        Field field = new Field(Settings.GridWidth, Settings.GridHeight);
        Random random = new Random(seed);

        placeOrganics(field, random, organicCount);
        placeMinerals(field, random, organicCount);

        return field;
    }

    private static void placeOrganics(Field field, Random random, int count) {
        int placed = 0;
        int attempts = 0;
        int maxAttempts = count * 50;
        while (placed < count && attempts < maxAttempts) {
            attempts++;
            Point position = randomPoint(random);
            if (!field.isFree(position)) {
                continue;
            }

            DNA dna = new DNA(
                    randomSubstance(random),
                    randomSubstance(random),
                    randomSubstance(random),
                    randomSubstance(random),
                    random.nextBoolean(),
                    inertDecisionMatrix()
            );
            field.add(new Organic(position, 50 + random.nextInt(200), Point.Companion.getZero(), dna));
            placed++;
        }
    }

    private static void placeMinerals(Field field, Random random, int count) {
        int placed = 0;
        int attempts = 0;
        int maxAttempts = count * 50;
        while (placed < count && attempts < maxAttempts) {
            attempts++;
            Point position = randomPoint(random);
            if (!field.isFree(position)) {
                continue;
            }

            field.add(new Mineral(position, 1 + random.nextInt(200), randomSubstance(random)));
            placed++;
        }
    }

    private static Point randomPoint(Random random) {
        return new Point(random.nextInt(Settings.GridWidth), random.nextInt(Settings.GridHeight));
    }

    private static Substance randomSubstance(Random random) {
        Substance[] substances = Substance.values();
        return substances[random.nextInt(substances.length)];
    }

    // DNA.decisionMatrix has no @JvmOverloads default, and DecisionMatrix's
    // own DecisionMatrix.Companion.default() can't be called from Java
    // (`default` is a Java keyword) - build an equivalent inert matrix
    // directly instead. Content doesn't matter: no Step reads it yet.
    private static DecisionMatrix inertDecisionMatrix() {
        List<Instruction> instructions = new ArrayList<>(DecisionMatrix.SIZE);
        for (int i = 0; i < DecisionMatrix.SIZE; i++) {
            instructions.add(new Instruction(
                    new Action(Action.Category.Rest, null),
                    Sensor.EnergyRatio,
                    Comparator.GreaterThanOrEqual,
                    0.0,
                    0
            ));
        }
        return new DecisionMatrix(instructions);
    }
}
