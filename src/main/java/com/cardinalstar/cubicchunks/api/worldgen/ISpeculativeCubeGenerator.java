package com.cardinalstar.cubicchunks.api.worldgen;

/** Optional generator capability used to avoid materializing cubes during speculative player-view loading. */
public interface ISpeculativeCubeGenerator {

    SpeculativeCubeHint classifyCube(int cubeX, int cubeY, int cubeZ);

    enum SpeculativeCubeHint {
        /** Classification is unavailable; generate when the reachability frontier arrives. */
        GENERATE,
        /** Known empty air that can transmit reachability without a materialized cube. */
        VIRTUAL_AIR,
        /** Known fully sealed cube; generate only as the first visible blocker. */
        SEALED
    }
}
