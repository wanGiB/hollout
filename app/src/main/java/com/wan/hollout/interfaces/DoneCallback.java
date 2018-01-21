package com.wan.hollout.interfaces;

/**
 * @author Wan Clem
 */

public interface DoneCallback<T> {

    void done(T result, Exception e);

}
