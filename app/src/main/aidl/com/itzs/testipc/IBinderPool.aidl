// IBinderPool.aidl
package com.itzs.testipc;

// Declare any non-default types here with import statements

interface IBinderPool {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);

    /**
     * @param binderCode, 标记每个binder的独一无二的标识；
     * @return 返回对应binderCode的binder
     */
    IBinder queryBinder(int bindCode);
}
