package com.wan.hollout.callbacks;

/**
 * @author Wan Clem
 */

public interface DoneCallback<T> {

    void done(T result, Exception e);

}
