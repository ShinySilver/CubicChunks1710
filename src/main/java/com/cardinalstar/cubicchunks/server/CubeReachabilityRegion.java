package com.cardinalstar.cubicchunks.server;

/**
 * Allocation-free reachability propagation inside a 4x4x4 cube region. Bit indices are {@code x | z << 2 | y << 4}.
 *
 * <p>
 * A sealed-face bit is conservative: clear can mean either open or unknown. Consequently this class may admit excess
 * work, but it must never reject a path unless one of the two touching cube faces is known sealed.
 */
public final class CubeReachabilityRegion {

    public static final long NEG_X_EDGE = 0x1111111111111111L;
    public static final long POS_X_EDGE = 0x8888888888888888L;
    public static final long NEG_Z_EDGE = 0x000F000F000F000FL;
    public static final long POS_Z_EDGE = 0xF000F000F000F000L;
    public static final long NEG_Y_EDGE = 0x000000000000FFFFL;
    public static final long POS_Y_EDGE = 0xFFFF000000000000L;

    private CubeReachabilityRegion() {}

    /** Returns the fixed-point reachable set within one region. Safe to run on an immutable off-thread snapshot. */
    public static long close(long seeds, long candidates, long sealedNegX, long sealedPosX, long sealedNegY,
        long sealedPosY, long sealedNegZ, long sealedPosZ) {
        long reachable = seeds & candidates;
        long previous;
        do {
            previous = reachable;
            long next = reachable;
            next |= ((reachable & ~sealedPosX & ~POS_X_EDGE) << 1) & ~sealedNegX;
            next |= ((reachable & ~sealedNegX & ~NEG_X_EDGE) >>> 1) & ~sealedPosX;
            next |= ((reachable & ~sealedPosZ & ~POS_Z_EDGE) << 4) & ~sealedNegZ;
            next |= ((reachable & ~sealedNegZ & ~NEG_Z_EDGE) >>> 4) & ~sealedPosZ;
            next |= ((reachable & ~sealedPosY & ~POS_Y_EDGE) << 16) & ~sealedNegY;
            next |= ((reachable & ~sealedNegY & ~NEG_Y_EDGE) >>> 16) & ~sealedPosY;
            reachable = next & candidates;
        } while (reachable != previous);
        return reachable;
    }

    public static long seedsAcrossNegX(long reachable, long sealedNegX) {
        return (reachable & NEG_X_EDGE & ~sealedNegX) << 3;
    }

    public static long seedsAcrossPosX(long reachable, long sealedPosX) {
        return (reachable & POS_X_EDGE & ~sealedPosX) >>> 3;
    }

    public static long seedsAcrossNegZ(long reachable, long sealedNegZ) {
        return (reachable & NEG_Z_EDGE & ~sealedNegZ) << 12;
    }

    public static long seedsAcrossPosZ(long reachable, long sealedPosZ) {
        return (reachable & POS_Z_EDGE & ~sealedPosZ) >>> 12;
    }

    public static long seedsAcrossNegY(long reachable, long sealedNegY) {
        return (reachable & NEG_Y_EDGE & ~sealedNegY) << 48;
    }

    public static long seedsAcrossPosY(long reachable, long sealedPosY) {
        return (reachable & POS_Y_EDGE & ~sealedPosY) >>> 48;
    }
}
