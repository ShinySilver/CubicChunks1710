package com.cardinalstar.cubicchunks.debug;

import java.util.concurrent.atomic.AtomicLong;

/** Low-overhead, opt-in counters for diagnosing integrated client world streaming. */
public final class CCProfiler {

    private static final AtomicLong cubePackets = new AtomicLong();
    private static final AtomicLong cubeRawBytes = new AtomicLong();
    private static final AtomicLong cubeCompressedBytes = new AtomicLong();
    private static final AtomicLong cubeEncodeNanos = new AtomicLong();
    private static final AtomicLong cubeCompressNanos = new AtomicLong();
    private static final AtomicLong cubeDecodeNanos = new AtomicLong();
    private static final AtomicLong cubeInstallNanos = new AtomicLong();
    private static final AtomicLong renderInvalidations = new AtomicLong();
    private static final AtomicLong syncFlushes = new AtomicLong();
    private static final AtomicLong syncFlushNanos = new AtomicLong();
    private static final AtomicLong eagerPasses = new AtomicLong();
    private static final AtomicLong eagerActiveNanos = new AtomicLong();
    private static final AtomicLong eagerRequests = new AtomicLong();
    private static final AtomicLong eagerLoadAttempts = new AtomicLong();
    private static final AtomicLong classifiedAir = new AtomicLong();
    private static final AtomicLong classifiedSealed = new AtomicLong();
    private static final AtomicLong classifiedGenerate = new AtomicLong();
    private static final AtomicLong litCubes = new AtomicLong();
    private static final AtomicLong eagerQueueColumns = new AtomicLong();
    private static final AtomicLong peakEagerQueueColumns = new AtomicLong();
    private static final AtomicLong renderBuilds = new AtomicLong();
    private static final AtomicLong renderBuildNanos = new AtomicLong();
    private static volatile boolean active;
    private static volatile long startedNanos;

    private CCProfiler() {}

    public static synchronized void start() {
        reset();
        startedNanos = System.nanoTime();
        active = true;
    }

    public static synchronized Snapshot stop() {
        Snapshot result = snapshot();
        active = false;
        return result;
    }

    public static boolean isActive() {
        return active;
    }

    public static void recordCubeEncode(long nanos, int bytes) {
        if (!active) return;
        cubePackets.incrementAndGet();
        cubeEncodeNanos.addAndGet(nanos);
        cubeRawBytes.addAndGet(bytes);
    }

    public static void recordCubeCompress(long nanos, int bytes) {
        if (!active) return;
        cubeCompressNanos.addAndGet(nanos);
        cubeCompressedBytes.addAndGet(bytes);
    }

    public static void recordCubeDecode(long nanos) {
        if (active) cubeDecodeNanos.addAndGet(nanos);
    }

    public static void recordCubeInstall(long nanos) {
        if (!active) return;
        cubeInstallNanos.addAndGet(nanos);
        renderInvalidations.incrementAndGet();
    }

    public static void recordSyncFlush(long nanos) {
        if (!active) return;
        syncFlushes.incrementAndGet();
        syncFlushNanos.addAndGet(nanos);
    }

    public static void recordEagerPass(long nanos, int requests, int queueColumns, int observedQueueColumns) {
        if (!active) return;
        eagerPasses.incrementAndGet();
        eagerActiveNanos.addAndGet(nanos);
        eagerRequests.addAndGet(requests);
        eagerQueueColumns.set(queueColumns);
        long peak;
        while (observedQueueColumns > (peak = peakEagerQueueColumns.get())
            && !peakEagerQueueColumns.compareAndSet(peak, observedQueueColumns)) {}
    }

    public static void recordEagerLoadAttempt() {
        if (active) eagerLoadAttempts.incrementAndGet();
    }

    public static void recordClassification(Enum<?> hint) {
        if (!active) return;
        switch (hint.name()) {
            case "VIRTUAL_AIR" -> classifiedAir.incrementAndGet();
            case "SEALED" -> classifiedSealed.incrementAndGet();
            case "GENERATE" -> classifiedGenerate.incrementAndGet();
        }
    }

    public static void recordLitCube() {
        if (active) litCubes.incrementAndGet();
    }

    public static void recordRenderBuild(long nanos) {
        if (!active) return;
        renderBuilds.incrementAndGet();
        renderBuildNanos.addAndGet(nanos);
    }

    public static Snapshot snapshot() {
        long elapsed = active ? System.nanoTime() - startedNanos : 0L;
        return new Snapshot(
            active,
            elapsed,
            cubePackets.get(),
            cubeRawBytes.get(),
            cubeCompressedBytes.get(),
            cubeEncodeNanos.get(),
            cubeCompressNanos.get(),
            cubeDecodeNanos.get(),
            cubeInstallNanos.get(),
            renderInvalidations.get(),
            syncFlushes.get(),
            syncFlushNanos.get(),
            eagerPasses.get(),
            eagerActiveNanos.get(),
            eagerRequests.get(),
            eagerLoadAttempts.get(),
            classifiedAir.get(),
            classifiedSealed.get(),
            classifiedGenerate.get(),
            litCubes.get(),
            eagerQueueColumns.get(),
            peakEagerQueueColumns.get(),
            renderBuilds.get(),
            renderBuildNanos.get());
    }

    private static void reset() {
        cubePackets.set(0L);
        cubeRawBytes.set(0L);
        cubeCompressedBytes.set(0L);
        cubeEncodeNanos.set(0L);
        cubeCompressNanos.set(0L);
        cubeDecodeNanos.set(0L);
        cubeInstallNanos.set(0L);
        renderInvalidations.set(0L);
        syncFlushes.set(0L);
        syncFlushNanos.set(0L);
        eagerPasses.set(0L);
        eagerActiveNanos.set(0L);
        eagerRequests.set(0L);
        eagerLoadAttempts.set(0L);
        classifiedAir.set(0L);
        classifiedSealed.set(0L);
        classifiedGenerate.set(0L);
        litCubes.set(0L);
        eagerQueueColumns.set(0L);
        peakEagerQueueColumns.set(0L);
        renderBuilds.set(0L);
        renderBuildNanos.set(0L);
    }

    public static final class Snapshot {

        public final boolean active;
        public final long elapsedNanos;
        public final long cubePackets, rawBytes, compressedBytes;
        public final long encodeNanos, compressNanos, decodeNanos, installNanos;
        public final long renderInvalidations, syncFlushes, syncFlushNanos;
        public final long eagerPasses, eagerActiveNanos, eagerRequests, eagerLoadAttempts;
        public final long classifiedAir, classifiedSealed, classifiedGenerate, litCubes;
        public final long eagerQueueColumns, peakEagerQueueColumns, renderBuilds, renderBuildNanos;

        Snapshot(boolean active, long elapsedNanos, long cubePackets, long rawBytes, long compressedBytes,
            long encodeNanos, long compressNanos, long decodeNanos, long installNanos, long renderInvalidations,
            long syncFlushes, long syncFlushNanos, long eagerPasses, long eagerActiveNanos, long eagerRequests,
            long eagerLoadAttempts, long classifiedAir, long classifiedSealed, long classifiedGenerate, long litCubes,
            long eagerQueueColumns, long peakEagerQueueColumns, long renderBuilds, long renderBuildNanos) {
            this.active = active;
            this.elapsedNanos = elapsedNanos;
            this.cubePackets = cubePackets;
            this.rawBytes = rawBytes;
            this.compressedBytes = compressedBytes;
            this.encodeNanos = encodeNanos;
            this.compressNanos = compressNanos;
            this.decodeNanos = decodeNanos;
            this.installNanos = installNanos;
            this.renderInvalidations = renderInvalidations;
            this.syncFlushes = syncFlushes;
            this.syncFlushNanos = syncFlushNanos;
            this.eagerPasses = eagerPasses;
            this.eagerActiveNanos = eagerActiveNanos;
            this.eagerRequests = eagerRequests;
            this.eagerLoadAttempts = eagerLoadAttempts;
            this.classifiedAir = classifiedAir;
            this.classifiedSealed = classifiedSealed;
            this.classifiedGenerate = classifiedGenerate;
            this.litCubes = litCubes;
            this.eagerQueueColumns = eagerQueueColumns;
            this.peakEagerQueueColumns = peakEagerQueueColumns;
            this.renderBuilds = renderBuilds;
            this.renderBuildNanos = renderBuildNanos;
        }
    }
}
