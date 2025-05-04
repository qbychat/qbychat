package org.cubewhy.qbychat.util

import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class SlidingWindowManager(
    private val windowSize: Long = 64
) {
    private var baseSeq: Long = 0
    private val receivedSeqs = ConcurrentSkipListSet<Long>()
    private val lock = ReentrantLock()

    fun accept(sequence: Long): Boolean = lock.withLock {
        if (sequence < baseSeq) {
            return false
        }

        if (sequence >= baseSeq + windowSize) {
            return false
        }

        if (receivedSeqs.contains(sequence)) {
            return false
        }

        receivedSeqs.add(sequence)
        slideWindow()

        return true
    }

    private fun slideWindow() {
        while (receivedSeqs.contains(baseSeq)) {
            receivedSeqs.remove(baseSeq)
            baseSeq++
        }
    }
}