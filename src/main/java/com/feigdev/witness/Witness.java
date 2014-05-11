package com.feigdev.witness;

import java.util.concurrent.*;

/**
 * The witness is what
 */
public class Witness {
    private static ConcurrentMap<Class<?>, ConcurrentMap<Reporter, String>> events
            = new ConcurrentHashMap<Class<?>, ConcurrentMap<Reporter, String>>();

    private static BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
    private static ExecutorService executorService = new ThreadPoolExecutor(1, 10, 30, TimeUnit.SECONDS, queue);

    public static void register(Class<?> event, Reporter reporter) {
        if (null == event || null == reporter)
            return;

        events.putIfAbsent(event.getClass(), new ConcurrentHashMap<Reporter, String>());
        events.get(event.getClass()).putIfAbsent(reporter, "");
    }

    public static void remove(Class<?> event, Reporter reporter) {
        if (null == event || null == reporter)
            return;

        if (!events.containsKey(event.getClass()))
            return;

        events.get(event.getClass()).remove(reporter);
    }

    public static void notify(final Class<?> event) {
        if (null == event)
            return;

        if (!events.containsKey(event.getClass()))
            return;

        for (final Reporter m : events.get(event.getClass()).keySet()) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    m.notifyEvent(event);
                }
            });
        }
    }
}
