package com.cardinalstar.cubicchunks.api.worldgen;

import java.util.List;

import net.minecraft.world.chunk.Chunk;

import com.cardinalstar.cubicchunks.world.cube.Cube;
import com.google.common.collect.ImmutableList;

public class GenerationResult<T> {

    public final T object;
    public final ImmutableList<Chunk> columnSideEffects;
    public final ImmutableList<Cube> cubeSideEffects;
    public final short[] cubeOpacity;

    public GenerationResult(T object) {
        this.object = object;
        this.columnSideEffects = ImmutableList.of();
        this.cubeSideEffects = ImmutableList.of();
        this.cubeOpacity = null;
    }

    public GenerationResult(T object, List<Chunk> columnSideEffects, List<Cube> cubeSideEffects) {
        this.object = object;
        this.columnSideEffects = columnSideEffects == null ? ImmutableList.of()
            : ImmutableList.copyOf(columnSideEffects);
        this.cubeSideEffects = cubeSideEffects == null ? ImmutableList.of() : ImmutableList.copyOf(cubeSideEffects);
        this.cubeOpacity = null;
    }

    /** Supplies optional generated-cube opacity, indexed by {@code localZ * 16 + localX}. */
    public GenerationResult(T object, short[] cubeOpacity) {
        this(object, null, null, cubeOpacity);
    }

    public GenerationResult(T object, List<Chunk> columnSideEffects, List<Cube> cubeSideEffects, short[] cubeOpacity) {
        this.object = object;
        this.columnSideEffects = columnSideEffects == null ? ImmutableList.of()
            : ImmutableList.copyOf(columnSideEffects);
        this.cubeSideEffects = cubeSideEffects == null ? ImmutableList.of() : ImmutableList.copyOf(cubeSideEffects);
        if (cubeOpacity == null || cubeOpacity.length != 16 * 16)
            throw new IllegalArgumentException("expected 256 opacity columns");
        this.cubeOpacity = cubeOpacity.clone();
    }
}
