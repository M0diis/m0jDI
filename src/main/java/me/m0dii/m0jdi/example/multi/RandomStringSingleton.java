package me.m0dii.m0jdi.example.multi;

import me.m0dii.m0jdi.annotations.Singleton;

import java.util.Random;

@Singleton
public class RandomStringSingleton {
    private static final Random RANDOM = new Random();

    public String getRandomString() {
        return String.format("RandomString-%d", RANDOM.nextInt(1000));
    }
}
