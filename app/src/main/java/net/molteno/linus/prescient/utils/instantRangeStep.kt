package net.molteno.linus.prescient.utils

import kotlinx.datetime.Instant
import kotlin.time.Duration

fun ClosedRange<Instant>.steppedBy(step: Duration): Iterable<Instant> = object: Iterable<Instant> {
    override fun iterator(): Iterator<Instant> = object: Iterator<Instant> {
        private var next = this@steppedBy.start
        private var finalElement = this@steppedBy.endInclusive
        private var hasNext = next < (this@steppedBy.endInclusive)

        override fun hasNext(): Boolean = hasNext


        override fun next(): Instant {
            val value = next

            if (value == finalElement) {
                hasNext = false
            } else {
                next = next.plus(step)
            }

            return value
        }
    }
}