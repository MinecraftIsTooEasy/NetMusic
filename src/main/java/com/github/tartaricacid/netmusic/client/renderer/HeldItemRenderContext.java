package com.github.tartaricacid.netmusic.client.renderer;

import java.util.ArrayDeque;
import java.util.Deque;

public final class HeldItemRenderContext {
    private static final ThreadLocal<Deque<Context>> CONTEXTS = ThreadLocal.withInitial(ArrayDeque::new);

    private HeldItemRenderContext() {}

    public static void push(Context context) {
        CONTEXTS.get().push(context);
    }

    public static void pop() {
        Deque<Context> contexts = CONTEXTS.get();
        if (!contexts.isEmpty()) {
            contexts.pop();
        }
        if (contexts.isEmpty()) {
            CONTEXTS.remove();
        }
    }

    public static Context current() {
        Deque<Context> contexts = CONTEXTS.get();
        return contexts.isEmpty() ? Context.NONE : contexts.peek();
    }

    public enum Context {
        NONE,
        FIRST_PERSON,
        THIRD_PERSON
    }
}
