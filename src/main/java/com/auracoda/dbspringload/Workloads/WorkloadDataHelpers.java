package com.auracoda.dbspringload.Workloads;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    public static byte[] CreateRandomBytes(int max) {
        final byte[] myData = new byte[max];
        for (int nextByte = 0; nextByte < max; nextByte++) {
            myData[nextByte] = (byte) RandomInt(0, 127);
        }
        return myData;
    }

    public static String CreateRandomJSON(int objCount) {
        return "{\"myField\":true,\"myName\":\"mark zlamal\", \"myValue\": 12345, \"moreDefs\": {\"type\":\"basic\",\"quantity\": 100}}";
    }

    // Example: "2025-03-24 14:30:00"
    public static Timestamp StringToTimestamp(String dateString) throws ParseException {
        // Define the format that matches the date string
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // Parse the date string into a Date object
        java.util.Date parsedDate = dateFormat.parse(dateString);
        // Convert the Date object into a Timestamp
        Timestamp timestamp = new Timestamp(parsedDate.getTime());

        return timestamp;
    }
}
