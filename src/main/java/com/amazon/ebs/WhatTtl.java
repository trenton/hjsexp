package com.amazon.ebs;

/**
 *
 */
public class WhatTtl {
    public static void main(String[] args) {
        String[] properties = {"networkaddress.cache.negative.ttl"};

        for (String property : properties) {
            System.out.println(lookupPropertyAndFormat(property));
        }
    }

    public static String lookupPropertyAndFormat(String property) {
        return property + ": " + System.getProperty(property);
    }
}
