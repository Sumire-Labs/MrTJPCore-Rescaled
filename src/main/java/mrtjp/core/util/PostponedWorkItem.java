/*
 * Copyright (c) 2014.
 * Created by MrTJP.
 * All rights reserved.
 */
package mrtjp.core.util;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * @author Andrey Redko - http://aredko.blogspot.com
 */
public class PostponedWorkItem<T> implements Delayed
{
    private final long origin;
    private final long delay;
    private final T workItem;

    public PostponedWorkItem(final T workItem, final long delay)
    {
        this.origin = System.currentTimeMillis();
        this.workItem = workItem;
        this.delay = delay;
    }

    public T getItem()
    {
        return workItem;
    }

    @Override
    public long getDelay(TimeUnit unit)
    {
        return unit.convert(remainingAt(System.currentTimeMillis()), TimeUnit.MILLISECONDS);
    }

    private long remainingAt(long now)
    {
        long elapsed = now - origin;
        if (elapsed > 0 && delay < Long.MIN_VALUE + elapsed) return Long.MIN_VALUE;
        if (elapsed < 0 && delay > Long.MAX_VALUE + elapsed) return Long.MAX_VALUE;
        return delay - elapsed;
    }

    @Override
    public int compareTo(Delayed delayed)
    {
        if (delayed == this)
            return 0;

        if (delayed instanceof PostponedWorkItem<?>)
        {
            long now = System.currentTimeMillis();
            return Long.compare(remainingAt(now), ((PostponedWorkItem<?>) delayed).remainingAt(now));
        }

        return Long.compare(getDelay(TimeUnit.NANOSECONDS), delayed.getDelay(TimeUnit.NANOSECONDS));
    }
}
