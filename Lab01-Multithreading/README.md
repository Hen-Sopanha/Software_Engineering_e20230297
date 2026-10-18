## Task2 Ex7
### Why is synchronizing only `increment()` still a bug?

A lock only protects code that uses it. If `get()` is not synchronized,
the reader never takes the lock, so Java gives no guarantee that it sees
the latest value written by other threads. It may read a stale copy from
a CPU cache or register, or a value in the middle of an update.

Example: Thread A calls `increment()` and finishes, but Thread B's
unsynchronized `get()` can still return the old count.

**Fix:** synchronize `get()` with the same lock (as `SynchronizedCounter`
and `LockCounter` do), make the field `volatile`, or use `AtomicLong`.
Note that `volatile` alone fixes visibility but not `count++`.

**Why our benchmark doesn't show the bug:** `Bench` reads the counter only
after `join()` has waited for all threads, and `join()` guarantees the
reader sees their writes. The bug would appear if `get()` were called while
other threads were still running.

Timing Results:
- UnsafeCounter,          value=6,509,859,  correct=false,  36 ms
- SynchronizedCounter,    value=8,000,000,  correct=true,   157 ms
- LockCounter,            value=8,000,000,  correct=true,   70 ms
- AtomicCounter,          value=8,000,000,  correct=true,   99 ms
- LongAdderCounter,       value=8,000,000,  correct=true,   10 ms

---

## Task3 Ex7
### Experiment: Change `notifyAll()` to `notify()` and the `while` to an `if`.

**Result:** 20/20 runs crashed with NoSuchElementException in a consumer thread.

### What happens
1. Consumers are usually faster than producers, so the buffer is often empty
   and several consumers are waiting in `wait()`.
2. `notify()` wakes one arbitrary waiter, which is often another consumer,
   even though the buffer is empty.
3. That consumer wakes up. Because of `if`, it does **not** re-check the
   condition and goes straight to `removeFirst()`.
4. `removeFirst()` on an empty `ArrayDeque` throws `NoSuchElementException`,
   and the consumer thread dies.

---

## Task4 Ex2
### Platform threads vs virtual threads

| Executor | Time for 500 downloads |
|---|---|
| Fixed pool, 20 platform threads | 5089 ms |
| Virtual threads (one per task) | 222 ms |

**Why the fixed pool is slow:** a sleeping platform thread stays blocked and
can't run other tasks, so only 20 downloads run at once.
500 tasks / 20 threads = 25 rounds x 200 ms = ~5000 ms.

**Why virtual threads are fast:** they are cheap, so every task gets its own
thread. When one blocks, the JVM frees its carrier thread for another task, so
all 500 downloads wait at the same time and the total is about one download
(~200 ms).

**Conclusion:** virtual threads speed up I/O-bound work (lots of waiting). They
don't help CPU-bound work like the prime count, where the number of cores is
the limit.