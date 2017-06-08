package org.cafebabepy.util;

/**
 * Created by yotchang4s on 2017/06/09.
 */
@FunctionalInterface
public interface BinaryConsumer<T1, T2> {
    void accept(T1 t1, T2 t2);
}
