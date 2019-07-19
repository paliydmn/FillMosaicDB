package sample;

import java.io.File;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Random;

class RecordedTVDAO {

    //INSERT new Recs
    //*************************************
    public static String insertRecs(File file) throws ClassNotFoundException {
        //Declare a INSERT statement
        //(10000, 10000, 1000, 1, "D:\RecTV\test\360.ts", "NoName", 360 );
        int schedule_id = 1000;
        long start_time = new Timestamp(System.currentTimeMillis()).getTime() / 1000L;
        int timer_id = getRandomNumberInRange(1000, 10000000);

        String updateStmt =
                "INSERT INTO completed_recording\n" +
                        "(schedule_id, timer_id, state, filename, channel_name, name, start_time)\n" +
                        "values (" + schedule_id + "," + timer_id + ", 1 ,'" + file.getAbsolutePath() +
                        "','NoName'" + ",'" + file.getName() + "'," + start_time + ")";

        // System.out.println(updateStmt);
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //Execute INSERT operation
        try {
            DBUtilSQLite.dbExecuteUpdate(updateStmt);
        } catch (SQLException e) {
            System.out.print("Error occurred while INSERT Operation: " + e);
            return e.getMessage();
        }
        return "Success";
    }

    //INSERT new Recs
    //*************************************
    public static String insertRecs(List<File> files) throws ClassNotFoundException {
        //Declare a INSERT statement
        //(10000, 10000, 1000, 1, "D:\RecTV\test\360.ts", "NoName", 360 );
        for (File file : files) {

            if (file.getAbsolutePath().contains("'")) {
                file = new File(file.getAbsolutePath().replace("'", ""));
            }
            int schedule_id = 1000;
            long start_time = new Timestamp(System.currentTimeMillis()).getTime() / 1000L;
            int timer_id = getRandomNumberInRange(1000, 10000000);

            String updateStmt =
                    "INSERT INTO completed_recording\n" +
                            "(schedule_id, timer_id, state, filename, channel_name, name, start_time)\n" +
                            "values (" + schedule_id + "," + timer_id + ", 1 ,'" + file.getAbsolutePath() +
                            "','NoName'" + ",'" + file.getName() + "'," + start_time + ")";
            //Execute INSERT operation
            try {
                DBUtilSQLite.dbExecuteUpdate(updateStmt);
            } catch (SQLException e) {
                System.out.print("Error occurred while INSERT Operation: " + e);
                return e.getMessage();
            }
        }
        return "Success";
    }


    private static int getRandomNumberInRange(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }
}


