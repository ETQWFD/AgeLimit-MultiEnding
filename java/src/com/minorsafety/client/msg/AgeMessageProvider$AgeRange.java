/*
 * Decompiled with CFR 0.152.
 */
package com.minorsafety.client.msg;

private record AgeMessageProvider.AgeRange(long min, long max, String message) {
    boolean contains(int age) {
        return (long)age >= this.min && (long)age <= this.max;
    }
}
