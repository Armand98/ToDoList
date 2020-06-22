package com.example.todolist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private Button btnAddNewElement, btnSaveToTextFile;
    private Spinner sortOrder;
    private static final int SD_PERMISSION_REQUEST_CODE = 3;
    private static final String CHANNEL_ID = "personal_notifications";

    public List<ToDoElement> listElements;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, SD_PERMISSION_REQUEST_CODE);
    }

    public void createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Personal Notifications";
            String description = "Include all the personal notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            notificationChannel.setDescription(description);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private void updateListOfElements() {
        MyFileIO myFileIO = new MyFileIO();
        listElements = myFileIO.getListOfElements();
        if(!listElements.isEmpty()) {
            ListElementAdapter adapter = new ListElementAdapter(this, R.layout.list_item_layout, listElements);
            listView.setAdapter(adapter);
            sendNotificationIfSomeTasksAreOutdatedAndNotDone();
        } else {
            Toast.makeText(MainActivity.this, "Nie masz żadnych zadań", Toast.LENGTH_LONG).show();
        }
    }

    private void refreshListView() {
        ListElementAdapter adapter = new ListElementAdapter(this, R.layout.list_item_layout, listElements);
        listView.setAdapter(adapter);
    }

    private void sortListElementsByTitle(final boolean mode) {
        Collections.sort(listElements, new Comparator<ToDoElement>() {
            @Override
            public int compare(ToDoElement o1, ToDoElement o2) {
                if(mode) {
                    return o1.getTitle().compareTo(o2.getTitle());
                } else {
                    return o2.getTitle().compareTo(o1.getTitle());
                }
            }
        });
    }

    private void sortListElementsByDeadline(final boolean mode) {
        Collections.sort(listElements, new Comparator<ToDoElement>() {
            @Override
            public int compare(ToDoElement o1, ToDoElement o2) {
                if(mode) {
                    return o1.getDeadline().compareTo(o2.getDeadline());
                } else {
                    return o2.getDeadline().compareTo(o1.getDeadline());
                }
            }
        });
    }

    private void sendNotificationIfSomeTasksAreOutdatedAndNotDone() {

        boolean isTaskNotCompleted = false;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        @SuppressLint("SimpleDateFormat") String date = new SimpleDateFormat("dd-MM-yyyy").format(Calendar.getInstance().getTime());

        for (ToDoElement element : listElements) {
            if(!element.isStatus()) {
                try {
                    Date todaysDate = simpleDateFormat.parse(date);
                    Date taskDeadline = simpleDateFormat.parse(element.getDeadline());

                    if(todaysDate.after(taskDeadline)) {
                        isTaskNotCompleted = true;
                        break;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        if(isTaskNotCompleted) {
            Intent intent = new Intent(MainActivity.this, ReminderBroadcast.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, 0);
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, 0,  pendingIntent);
        }
    }

    private void init() {
        listView = findViewById(R.id.listView);
        btnAddNewElement = findViewById(R.id.btnAddNewElement);
        btnSaveToTextFile = findViewById(R.id.btnSaveToTextFile);
        sortOrder = findViewById(R.id.spinnerSortOrder);

        List<String> spinnerSortOrderArray = new ArrayList<>();
        spinnerSortOrderArray.add("Alfabetycznie - A-Z");
        spinnerSortOrderArray.add("Alfabetycznie - Z-A");
        spinnerSortOrderArray.add("Po terminie - najbliższym");
        spinnerSortOrderArray.add("Po terminie - najdalszym");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, spinnerSortOrderArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortOrder.setAdapter(adapter);
        sortOrder.setSelection(2);

        sortOrder.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        sortListElementsByTitle(true);
                        refreshListView();
                        break;

                    case 1:
                        sortListElementsByTitle(false);
                        refreshListView();
                        break;

                    case 2:
                        sortListElementsByDeadline(true);
                        refreshListView();
                        break;

                    case 3:
                        sortListElementsByDeadline(false);
                        refreshListView();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ToDoElement item = (ToDoElement) listView.getItemAtPosition(position);
                String strTitle = item.getTitle();

                Intent intent = new Intent(MainActivity.this, ListElementActivity.class);
                intent.putExtra("data", (Serializable)listElements);
                intent.putExtra("title", strTitle);
                startActivity(intent);
            }
        });

        btnAddNewElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ListElementActivity.class);
                intent.putExtra("data", (Serializable)listElements);
                intent.putExtra("title", "");
                startActivity(intent);
            }
        });

        btnSaveToTextFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyFileIO myFileIO = new MyFileIO();
                if(myFileIO.generateTextFile(listElements)) {
                    Toast.makeText(MainActivity.this, "Zapisano plik tekstowy z zadaniami.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == SD_PERMISSION_REQUEST_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];

                if (permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        init();
                        updateListOfElements();
                    } else {
                        Toast.makeText(this, "Aplikacja nie zadziała bez dostępu do karty SD", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }
}
