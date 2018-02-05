package com.mmall;

import org.junit.Test;

import java.math.BigDecimal;

/**
 * @author fan
 * @date 2018/2/1 12:49
 */
public class test {

    @Test
    public void test1() {
        System.out.println(0.05+0.01);
        System.out.println(1.0-0.42);
        System.out.println(4.032*100);
        System.out.println(123.3/100);
    }

    @Test
    public void test2() {
        BigDecimal b1 = new BigDecimal(0.05);
        BigDecimal b2 = new BigDecimal(0.01);
        System.out.println(b1.add(b2));
    }

    @Test
    public void test3() {
        BigDecimal b1 = new BigDecimal("0.05");
        BigDecimal b2 = new BigDecimal("0.01");
        System.out.println(b1.add(b2));
    }
}
