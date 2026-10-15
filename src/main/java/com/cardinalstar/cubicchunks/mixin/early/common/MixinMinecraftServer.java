/*
 * This file is part of Cubic Chunks Mod, licensed under the MIT License (MIT).
 * Copyright (c) 2015-2021 OpenCubicChunks
 * Copyright (c) 2015-2021 contributors
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.cardinalstar.cubicchunks.mixin.early.common;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.cardinalstar.cubicchunks.mixin.api.ICubicWorldInternal;
import com.cardinalstar.cubicchunks.server.CubeProviderServer;
import com.cardinalstar.cubicchunks.server.ICubicChunksServer;
import com.cardinalstar.cubicchunks.server.SpawnCubes;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer implements ICubicChunksServer {

    private static final long CUBIC_CHUNKS_IDLE_SAFETY_NS = 2_000_000L;

    @Unique
    private int cubicChunks$buildMinimum;

    @Unique
    private long cubicChunks$tickStartNanos;

    @Unique
    private int cubicChunks$ticksSinceSleep;

    @Unique
    private int cubicChunks$nextIdleDimension;

    @Inject(method = "tick", at = @At("HEAD"))
    private void cubicChunks$recordTickStart(CallbackInfo ci) {
        cubicChunks$tickStartNanos = System.nanoTime();
        cubicChunks$ticksSinceSleep++;
    }

    /**
     * Replaces only time that the server was about to sleep with optional eager loading. Multiple ticks between
     * sleeps mean the server is catching up, so those loops intentionally receive no additional generation work.
     */
    @Redirect(method = "run", at = @At(value = "INVOKE", target = "Ljava/lang/Thread;sleep(J)V"))
    private void cubicChunks$useIdleTime(long requestedSleepMillis) throws InterruptedException {
        if (Boolean.getBoolean("cubicchunks.disableIdleLoading")) {
            cubicChunks$ticksSinceSleep = 0;
            Thread.sleep(requestedSleepMillis);
            return;
        }

        long sleepDeadline = System.nanoTime() + requestedSleepMillis * 1_000_000L;

        if (cubicChunks$ticksSinceSleep == 1) {
            // The requested sleep excludes the tick that just ran. Anchor it to tick start so eager loading replaces
            // idle time instead of lengthening the tick cycle.
            sleepDeadline = Math.min(sleepDeadline, cubicChunks$tickStartNanos + requestedSleepMillis * 1_000_000L);
            cubicChunks$loadCubesUntil(sleepDeadline - CUBIC_CHUNKS_IDLE_SAFETY_NS);
        }

        cubicChunks$ticksSinceSleep = 0;
        long remainingNanos = sleepDeadline - System.nanoTime();
        if (remainingNanos > 0) {
            long millis = remainingNanos / 1_000_000L;
            int nanos = (int) (remainingNanos % 1_000_000L);
            Thread.sleep(millis, nanos);
        }
    }

    @Unique
    private void cubicChunks$loadCubesUntil(long deadlineNanos) {
        if (System.nanoTime() >= deadlineNanos) return;

        Integer[] dimensions = DimensionManager.getIDs(false);
        if (dimensions.length == 0) return;

        int start = Math.floorMod(cubicChunks$nextIdleDimension++, dimensions.length);
        for (int offset = 0; offset < dimensions.length && System.nanoTime() < deadlineNanos; offset++) {
            WorldServer world = DimensionManager.getWorld(dimensions[(start + offset) % dimensions.length]);
            if (world != null && world.getChunkProvider() instanceof CubeProviderServer) {
                ((CubeProviderServer) world.getChunkProvider()).doEagerLoadingUntil(deadlineNanos);
            }
        }
    }

    @Inject(method = "initialWorldChunkLoad", at = @At("HEAD"), cancellable = true)
    private void onInitialSpawnLoad(CallbackInfo ci) {
        World world = DimensionManager.getWorld(0);
        ((ICubicWorldInternal.Server) world).setSpawnArea(new SpawnCubes());
        ((ICubicWorldInternal.Server) world).getSpawnArea()
            .update(world);
        ci.cancel();
    }

    @Override
    public void setBuildMinimum(int minBuildHeight) {
        cubicChunks$buildMinimum = minBuildHeight;
    }

    @Override
    public int getBuildMinimum() {
        return cubicChunks$buildMinimum;
    }
}
