package com.developer.kailash.todolistv2;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.gesture.GestureOverlayView;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.developer.kailash.todolistv2.db.TaskContract;
import com.developer.kailash.todolistv2.db.TaskDbHelper;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private TaskDbHelper mHelper;
    private ListView mTaskListView;
    private ArrayAdapter<String> mAdapter;
    boolean listEmpty;

    ArrayList<String> taskList;

    Toolbar toolbar;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    android.support.v4.app.FragmentTransaction fragmentTransaction;
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton addButton = (FloatingActionButton)findViewById(R.id.fab_add_task);
        FloatingActionButton speechButton = (FloatingActionButton)findViewById(R.id.fab_speech_recognizer);
        
        addButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(200);
                Toast.makeText(MainActivity.this, "Add Task", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        
        speechButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(200);
                Toast.makeText(MainActivity.this, "Task Listener", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.drawer_open,R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);

        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.main_container, new HomeFragment());
        fragmentTransaction.commit();

        navigationView = (NavigationView)findViewById(R.id.nav);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.home:
                        fragmentTransaction = getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.main_container, new HomeFragment());
                        fragmentTransaction.commit();
                        getSupportActionBar().setTitle("Tasker");
                        item.setChecked(true);
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.settings:
//                        Intent intent = new Intent(MainActivity.this,SettingsActivity.class);
//                        MainActivity.this.startActivity(intent);
                        fragmentTransaction = getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.main_container, new SettingsFragment());
                        fragmentTransaction.commit();
                        getSupportActionBar().setTitle("Settings");
                        item.setChecked(true);
                        drawerLayout.closeDrawers();
                        break;
                }
                return true;
            }
        });

        mHelper = new TaskDbHelper(this);
        mTaskListView = (ListView) findViewById(R.id.list_todo);
        updateUI();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        updateUI();
        if(actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        switch (item.getItemId()) {
            case R.id.action_finish_all_tasks:
                if(listEmpty){
                    Toast.makeText(MainActivity.this, "No Tasks to Finish", Toast.LENGTH_SHORT).show();
                }
                else {
                    deleteAllAlert(item.getActionView());
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**Task Handling*/
    public void createTask(String task){
        SQLiteDatabase db = mHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskEntry.COL_TASK_TITLE, task);
        db.insertWithOnConflict(TaskContract.TaskEntry.TABLE,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE);
        updateUI();
        db.close();
    }

    public void updateUI(){
        taskList = new ArrayList<>();
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.query(TaskContract.TaskEntry.TABLE,
                new String[]{TaskContract.TaskEntry._ID, TaskContract.TaskEntry.COL_TASK_TITLE},
                null, null, null, null, null);
        while(cursor.moveToNext()){
            int idx = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_TITLE);
            taskList.add(cursor.getString(idx));
        }

        TextView emptyList = (TextView)findViewById(R.id.home_fragment);
        if(mAdapter == null){
            mAdapter = new ArrayAdapter<>(this,
                    R.layout.item_todo,
                    R.id.task_title,
                    taskList);
            mTaskListView.setAdapter(mAdapter);
        }
        else{
            mAdapter.clear();
            mAdapter.addAll(taskList);
            if(!taskList.isEmpty()){
                emptyList.setVisibility(View.GONE);
                listEmpty = false;
            }
            else if(taskList.isEmpty()){
                emptyList.setVisibility(View.VISIBLE);
                listEmpty = true;
            }
            mAdapter.notifyDataSetChanged();
        }
        cursor.close();
        db.close();
    }

    public void editTask(View view){
        View parent = (View)view.getParent();
        TextView taskTextView = (TextView)parent.findViewById(R.id.task_title);
        final String currentTask = String.valueOf(taskTextView.getText());
        final EditText taskEditText = new EditText(this);
        taskEditText.setTextColor(Color.WHITE);
        taskEditText.setText(currentTask);
        AlertDialog dialog = new AlertDialog.Builder(this,R.style.AlertDialogStyle)
                .setTitle("Edit Task")
                .setView(taskEditText)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newTask = taskEditText.getText().toString();
                        updateTask(currentTask,newTask);
                        updateUI();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

    public void updateTask(String currentTask, String newTask){
        SQLiteDatabase db = mHelper.getWritableDatabase();
        String selection = TaskContract.TaskEntry.COL_TASK_TITLE + " LIKE ?";
        String args[] = {currentTask};
        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskEntry.COL_TASK_TITLE,newTask);
        db.update(TaskContract.TaskEntry.TABLE, values, selection, args);
        db.close();
    }

    public void addTask(View view){
        final EditText taskEditText = new EditText(this);
        taskEditText.setTextColor(Color.WHITE);
        AlertDialog dialog = new AlertDialog.Builder(this,R.style.AlertDialogStyle)
                .setTitle("Add a new task")
                .setMessage("What do you want to do next?")
                .setView(taskEditText)
                .setPositiveButton("Add",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String task = String.valueOf(taskEditText.getText());
                        createTask(task);
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

    public void deleteTask(View view){
        View parent = (View)view.getParent();
        TextView taskTextView = (TextView)parent.findViewById(R.id.task_title);
        String task = String.valueOf(taskTextView.getText());
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.delete(TaskContract.TaskEntry.TABLE,
                TaskContract.TaskEntry.COL_TASK_TITLE + " = ?",
                new String[]{task});
        db.close();
        if(SettingsFragment.vibratorOn) {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(500);
        }
        Toast.makeText(MainActivity.this, "Finished", Toast.LENGTH_SHORT).show();
        updateUI();
    }

    public void deleteAllAlert(View view){
        AlertDialog dialog = new AlertDialog.Builder(this,R.style.AlertDialogStyle)
                .setTitle("Finish All Tasks")
                .setMessage("Are you sure you want to finish all tasks?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteAllTasks();
                        if (SettingsFragment.vibratorOn) {
                            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            vibrator.vibrate(500);
                        }
                        Toast.makeText(MainActivity.this, "All Tasks Finished", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

    public void deleteAllTasks(){
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.delete(TaskContract.TaskEntry.TABLE, null, null);
        updateUI();
        db.close();
    }

    /**Speech Recognition*/
    public void onSpeechButtonClick(View view){
        if(view.getId() == R.id.fab_speech_recognizer){
            promptSpeechInput();
        }
    }

    public void promptSpeechInput(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Add a Task");

        try {
            startActivityForResult(intent,100);
        }
        catch (ActivityNotFoundException a){
            Toast.makeText(MainActivity.this, "Sorry, your device does not support speech", Toast.LENGTH_LONG).show();
        }
    }

    public void onActivityResult(int request_code, int result_code, Intent intent){
        super.onActivityResult(request_code, result_code, intent);
        switch (request_code){
            case 100:
                if (result_code == RESULT_OK && intent != null){
                    ArrayList<String> result = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String task = String.valueOf(result.get(0));
                    task = task.substring(0,1).toUpperCase() + task.substring(1);
                    createTask(task);
                }
                break;
        }
    }
}