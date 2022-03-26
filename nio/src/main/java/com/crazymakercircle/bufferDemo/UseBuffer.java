package com.crazymakercircle.bufferDemo;

import java.nio.IntBuffer;

public class UseBuffer {
    static IntBuffer intBuffer = null;

    public static void display() {
        System.out.println("position: " + intBuffer.position());
        System.out.println("limit: " + intBuffer.limit());
        System.out.println("capacity: " + intBuffer.capacity());

        System.out.println();
        System.out.println();
    }

    public static void allocateTest() {
        intBuffer = IntBuffer.allocate(20);

        display();
    }

    public static void putTest() {
        for (int i = 0; i < 5; i++) {
            intBuffer.put(i);
        }

        display();
    }

    public static void flipTest() {
        intBuffer.flip();

        display();
    }

    public static void getTest() {
        for (int i = 0; i < 2; i++) {
            int j = intBuffer.get();
            System.out.println("j: " + j);
        }
        display();
    }

    public static void rewindTest() {
        intBuffer.rewind();

        display();
    }

    public static void markTest() {
        intBuffer.mark();
    }

    public static void reRead() {
        intBuffer.reset();

        while (intBuffer.position() < intBuffer.limit()) {
            System.out.println(intBuffer.get());
        }
    }

    public static void clearTest() {
        intBuffer.clear();

        display();
    }


    public static void main(String[] args) {
        allocateTest();

        putTest();

        flipTest();

        getTest();

        getTest();

        rewindTest();

        System.out.println("=================");
        getTest();
        markTest();
        getTest();
        display();
        reRead();
        display();

        clearTest();
    }
}
