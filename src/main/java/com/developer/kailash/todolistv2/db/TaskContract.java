package com.developer.kailash.todolistv2.db;

import android.provider.BaseColumns;

/**
 * Created by Kailash on 7/21/2016.
 */
public class TaskContract {
    public static final String DB_NAME = "com.developer.kailash.todolistv2";
    public static final int DB_VERSION = 1;

    public class TaskEntry implements BaseColumns{
        public static final String TABLE = "tasks";

        public static final String COL_TASK_TITLE = "title";
        public static final String COL_TYPE = "type";
        public static final String COL_PARENT = "parent";
    }
}

