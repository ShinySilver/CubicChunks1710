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
    /** Bit mask of cube faces known to be completely opaque. Unknown faces must remain clear. */
    public final byte sealedFaces;

    public GenerationResult(T object) {
        this.object = object;
        this.columnSideEffects = ImmutableList.of();
        this.cubeSideEffects = ImmutableList.of();
        this.cubeOpacity = null;
        this.sealedFaces = 0;
    }

    public GenerationResult(T object, List<Chunk> columnSideEffects, List<Cube> cubeSideEffects) {
        this.object = object;
        this.columnSideEffects = columnSideEffects == null ? ImmutableList.of()
            : ImmutableList.copyOf(columnSideEffects);
        this.cubeSideEffects = cubeSideEffects == null ? ImmutableList.of() : ImmutableList.copyOf(cubeSideEffects);
        this.cubeOpacity = null;
        this.sealedFaces = 0;
    }

    /** Supplies optional generated-cube opacity, indexed by {@code localZ * 16 + localX}. */
    public GenerationResult(T object, short[] cubeOpacity) {
        this(object, null, null, cubeOpacity, (byte) 0);
    }

    public GenerationResult(T object, short[] cubeOpacity, byte sealedFaces) {
        this(object, null, null, cubeOpacity, sealedFaces);
    }

    public GenerationResult(T object, List<Chunk> columnSideEffects, List<Cube> cubeSideEffects, short[] cubeOpacity) {
        this(object, columnSideEffects, cubeSideEffects, cubeOpacity, (byte) 0);
    }

    public GenerationResult(T object, List<Chunk> columnSideEffects, List<Cube> cubeSideEffects, short[] cubeOpacity,
        byte sealedFaces) {
        this.object = object;
        this.columnSideEffects = columnSideEffects == null ? ImmutableList.of()
            : ImmutableList.copyOf(columnSideEffects);
        this.cubeSideEffects = cubeSideEffects == null ? ImmutableList.of() : ImmutableList.copyOf(cubeSideEffects);
        if (cubeOpacity == null || cubeOpacity.length != 16 * 16)
            throw new IllegalArgumentException("expected 256 opacity columns");
        this.cubeOpacity = cubeOpacity.clone();
        this.sealedFaces = (byte) (sealedFaces & 0x3f);
    }
}
