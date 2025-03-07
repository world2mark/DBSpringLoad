package com.auracoda.dbspringload.Workloads;

import java.util.Random;

public class WorkloadDataHelpers {

    private static Random myRandom = new Random();

    public static void CreateRandom(long mySeed) {
        myRandom = new Random(mySeed);
    }

    public static String RandomAlphanumericString(int length) {
        String alphanumericCharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuv";

        StringBuffer randomString = new StringBuffer(length);

        for (int i = 0; i < length; i++) {
            int randomIndex = myRandom.nextInt(alphanumericCharacters.length());
            char randomChar = alphanumericCharacters.charAt(randomIndex);
            randomString.append(randomChar);
        }

        return randomString.toString();
    }

    public static double RandomDouble(double min, double max) {
        return min + Math.random() * (max - min);
    }

    public static int RandomInt(int min, int max) {
        return myRandom.nextInt(1 + max - min) + min;
    }

}
