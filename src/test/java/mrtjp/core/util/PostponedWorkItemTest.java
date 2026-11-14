package mrtjp.core.util;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PostponedWorkItemTest {
    @Test
    void readyWorkIsNotBlockedByALaterShorterDelay() throws InterruptedException {
        var ready = new PostponedWorkItem<>("ready", 20);
        Thread.sleep(50);
        var later = new PostponedWorkItem<>("later", 10);
        var queue = new DelayQueue<PostponedWorkItem<String>>();
        queue.add(ready);
        queue.add(later);
        assertSame(ready, queue.poll());
        assertTrue(ready.compareTo(later) < 0);
        assertTrue(later.compareTo(ready) > 0);
    }

    @Test
    void extremeDelaysKeepExpiredAndFutureWorkInOrder() {
        var expired = new PostponedWorkItem<>("expired", Long.MIN_VALUE);
        var future = new PostponedWorkItem<>("future", Long.MAX_VALUE);
        var ready = new PostponedWorkItem<>("ready", 0);
        assertTrue(expired.getDelay(TimeUnit.MILLISECONDS) < 0);
        var queue = new DelayQueue<PostponedWorkItem<String>>();
        queue.add(future);
        queue.add(ready);
        queue.add(expired);
        assertSame(expired, queue.poll());
        assertSame(ready, queue.poll());
        assertNull(queue.poll());
    }
}
