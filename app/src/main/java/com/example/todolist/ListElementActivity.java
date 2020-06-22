package com.example.todolist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ListElementActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private EditText title, note, deadline;
    private CheckBox status;
    private Spinner priority, remindTime;
    private Switch remindSwitch;
    private ImageView image;
    private String imageTitle = "";
    private boolean isFileToDelete;
    private Button btnOpenCalendar, btnAddImage, btnSave, btnDelete, btnDeleteImage;
    private static final int Image_Capture_Code = 1;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 2;
    int remindTimeDay, remindTimeMonth, remindTimeYear;

    public List<ToDoElement> listElements;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_element);
        init();

        Intent incomingIntent = getIntent();
        listElements = (List<ToDoElement>) incomingIntent.getSerializableExtra("data");

        final String strIncomingTitle = incomingIntent.getStringExtra("title");
        int index = -1;

        if(!strIncomingTitle.equals("")) {
            for(index=0; index<listElements.size(); index++) {
                if(listElements.get(index).getTitle().equals(strIncomingTitle)) {
                    break;
                }
            }

            title.setText(listElements.get(index).getTitle());
            status.setChecked(listElements.get(index).isStatus());
            note.setText(listElements.get(index).getNote());
            priority.setSelection(listElements.get(index).getPriority()-1);
            deadline.setText(listElements.get(index).getDeadline());
            remindTime.setSelection(1);
            remindSwitch.setChecked(listElements.get(index).isRemindChecked());

            setImageTitle(listElements.get(index).getImageTitle());

            final String imageTitle = listElements.get(index).getImageTitle();
            if(!imageTitle.equals("")) {
                Bitmap imageBitmap = getImageBitmap(imageTitle);
                image.setImageBitmap(imageBitmap);
            } else {
                image.setImageResource(R.drawable.ic_launcher_foreground);
            }

            btnDeleteImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setFileToDelete(true);
                    image.setImageResource(R.drawable.ic_launcher_foreground);
                }
            });
        }

        final int finalIndex = index;

        status.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(status.isChecked()) {
                    status.setText("Zrobione");
                } else {
                    status.setText("Do zrobienia");
                }
            }
        });

        btnOpenCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        btnAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(ListElementActivity.this, new String[] {Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strTitle, strNote, strDeadline, strRemindTime;
                boolean boolStatus, boolRemindSwitchValue, boolTitleExist;
                int intPriorityValue;
                boolTitleExist = false;

                strTitle = title.getText().toString();

                for(ToDoElement element : listElements) {
                    if(element.getTitle().equals(strTitle)) {
                        boolTitleExist = true;
                        break;
                    }
                }

                if(strTitle.isEmpty()) {
                    Toast.makeText(ListElementActivity.this, "Musisz dodać tytuł zadania", Toast.LENGTH_SHORT).show();
                    return;
                }

                strNote = note.getText().toString();
                strRemindTime = remindTime.getSelectedItem().toString();
                boolStatus = status.isChecked();

                boolRemindSwitchValue = remindSwitch.isChecked();

                if(boolRemindSwitchValue && !boolStatus) {
                    Intent intent = new Intent(ListElementActivity.this, ReminderBroadcast.class);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(ListElementActivity.this, 0, intent, 0);
                    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

                    int selectedItemPosition = remindTime.getSelectedItemPosition();
                    int timeBeforeDeadlineInMillis = 0;

                    switch (selectedItemPosition) {
                        case 0:
                            timeBeforeDeadlineInMillis = 15*60*1000;
                            break;
                        case 1:
                            timeBeforeDeadlineInMillis = 30*60*1000;
                            break;
                        case 2:
                            timeBeforeDeadlineInMillis = 60*60*1000;
                            break;
                        case 3:
                            timeBeforeDeadlineInMillis = 3*60*60*1000;
                            break;
                        case 4:
                            timeBeforeDeadlineInMillis = 6*60*60*1000;
                            break;
                        case 5:
                            timeBeforeDeadlineInMillis = 12*60*60*1000;
                            break;
                        case 6:
                            timeBeforeDeadlineInMillis = 24*60*60*1000;
                            break;
                    }

                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.YEAR, remindTimeYear);
                    cal.set(Calendar.MONTH, remindTimeMonth);
                    cal.set(Calendar.DAY_OF_MONTH, remindTimeDay);
                    cal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY));
                    cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE));
                    cal.set(Calendar.SECOND, cal.get(Calendar.SECOND));
                    cal.set(Calendar.MILLISECOND, cal.get(Calendar.MILLISECOND));

                    alarmManager.set(AlarmManager.RTC_WAKEUP, ((cal.getTimeInMillis() - timeBeforeDeadlineInMillis) > 0 ? (cal.getTimeInMillis() - timeBeforeDeadlineInMillis) : 1000),  pendingIntent);
                }

                String tempStrPriority = priority.getSelectedItem().toString();

                intPriorityValue = 0;
                if(tempStrPriority.equals("Bardzo ważne")) {
                    intPriorityValue = 1;
                } else if(tempStrPriority.equals("Ważne")) {
                    intPriorityValue = 2;
                } else if(tempStrPriority.equals("Normalne")) {
                    intPriorityValue = 3;
                } else if(tempStrPriority.equals("Mało ważne")) {
                    intPriorityValue = 4;
                }

                strDeadline = deadline.getText().toString();
                String currentDateStr = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

                boolean isDateCorrect = true;

                try {
                    Date deadlineDate = simpleDateFormat.parse(strDeadline);
                    Date currentDate = simpleDateFormat.parse(currentDateStr);

                    if(currentDate.after(deadlineDate)) {
                        isDateCorrect = false;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if(isDateCorrect) {
                    if(!strIncomingTitle.equals("")) {

                        listElements.get(finalIndex).setTitle(strTitle);
                        listElements.get(finalIndex).setNote(strNote);
                        listElements.get(finalIndex).setDeadline(strDeadline);
                        listElements.get(finalIndex).setPriority(intPriorityValue);
                        listElements.get(finalIndex).setRemindChecked(boolRemindSwitchValue);
                        listElements.get(finalIndex).setRemindTime(strRemindTime);
                        listElements.get(finalIndex).setStatus(boolStatus);
                        listElements.get(finalIndex).setImageTitle(imageTitle);

                        if(isFileToDelete) {
                            listElements.get(finalIndex).setImageTitle("");
                        }

                        if(updateFile(isFileToDelete)) {
                            Toast.makeText(ListElementActivity.this, "Zaktualizowano zadanie", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ListElementActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
                    } else {
                        if(boolTitleExist) {
                            Toast.makeText(ListElementActivity.this, "Masz już zadanie o tej nazwie", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        ToDoElement newElement = new ToDoElement(strTitle, strNote, strRemindTime, boolRemindSwitchValue, boolStatus, intPriorityValue, currentDateStr, strDeadline, imageTitle);
                        addNewElementToListOfElements(newElement);
                        JSONObject jsonObject = generateJsonObjectFromListOfElements();
                        if(addNewElement(jsonObject)) {
                            Toast.makeText(ListElementActivity.this, "Dodano nowy element", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ListElementActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
                    }
                } else {
                    Toast.makeText(ListElementActivity.this, "Nie możesz ustawić daty, która minęła.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(finalIndex >= 0) {
                    listElements.remove(finalIndex);
                    deleteImageFromGallery(imageTitle);
                    updateFile(true);
                    Intent intent = new Intent(ListElementActivity.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    finish();
                }
            }
        });
    }

    public boolean deleteImageFromGallery(String imageTitle) {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pictures/" + imageTitle;
        File pictureFile = new File(path);
        if(pictureFile.exists()) {
            if(pictureFile.delete()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public void setFileToDelete(boolean fileToDelete) {
        isFileToDelete = fileToDelete;
    }

    public void setImageTitle(String imageTitle) {
        this.imageTitle = imageTitle;
    }

    public Bitmap getImageBitmap(String imageTitle) {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pictures/" + imageTitle;
        File pictureFile = new File(path);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(pictureFile.getAbsolutePath(), bmOptions);
        return bitmap;
    }

    public void addNewElementToListOfElements(ToDoElement newElement) {
        listElements.add(newElement);
    }

    public JSONObject generateJsonObjectFromListOfElements() {
        JSONArray jsonArray = new JSONArray();
        for (ToDoElement element : listElements) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("title", element.getTitle());
                jsonObject.put("note", element.getNote());
                jsonObject.put("remindTime", element.getRemindTime());
                jsonObject.put("isRemindTimeOptionChecked", element.isRemindChecked());
                jsonObject.put("status", element.isStatus());
                jsonObject.put("priority", element.getPriority());
                jsonObject.put("creationDate", element.getDateOfCreation());
                jsonObject.put("deadline", element.getDeadline());
                jsonObject.put("imageTitle", element.getImageTitle());
                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        JSONObject elementsObject = new JSONObject();
        try {
            elementsObject.put("Elements", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return elementsObject;
    }

    public boolean addNewElement(JSONObject newJsonElement) {
        MyFileIO myFileIO = new MyFileIO();
        myFileIO.saveListOfElementsToFile(newJsonElement.toString());
        return true;
    }

    public boolean updateFile(boolean deleteFile) {
        if(deleteFile) {
            deleteImageFromGallery(imageTitle);
        }
        MyFileIO myFileIO = new MyFileIO();
        JSONObject jsonObject = generateJsonObjectFromListOfElements();
        myFileIO.saveListOfElementsToFile(jsonObject.toString());
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];

                if (permission.equals(Manifest.permission.CAMERA)) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        takePicture();
                    } else {
                        Toast.makeText(this, "Nie możesz zrobić zdjęcia bez dostępu do kamery.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, Image_Capture_Code);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Image_Capture_Code) {
            if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                image.setImageBitmap(imageBitmap);
                addPictureToGallery(imageBitmap);
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void addPictureToGallery(Bitmap bitmap) {
        deleteImageFromGallery(imageTitle);
        imageTitle  = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + ".jpg";
        String desc = "Photo from " + getString(R.string.app_name);
        MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, imageTitle, desc);
    }

    private void init() {
        title = findViewById(R.id.etElementTitle);
        note = findViewById(R.id.etNote);
        deadline = findViewById(R.id.etDeadline);
        status = findViewById(R.id.cbStatus);
        priority = findViewById(R.id.spinnerPriority);
        remindTime = findViewById(R.id.spinnerRemindTime);
        remindSwitch = findViewById(R.id.switchRemind);
        image = findViewById(R.id.ivImage);
        btnOpenCalendar = findViewById(R.id.btnDate);
        btnAddImage = findViewById(R.id.btnAddImage);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);
        btnDeleteImage = findViewById(R.id.btnDeleteImage);

        isFileToDelete = false;

        listElements = new ArrayList<>();

        List<String> spinnerPriorityArray = new ArrayList<>();
        spinnerPriorityArray.add("Bardzo ważne");
        spinnerPriorityArray.add("Ważne");
        spinnerPriorityArray.add("Normalne");
        spinnerPriorityArray.add("Mało ważne");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, spinnerPriorityArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        priority.setAdapter(adapter);

        List<String> spinnerRemindTimeArray = new ArrayList<>();
        spinnerRemindTimeArray.add("15min");
        spinnerRemindTimeArray.add("30min");
        spinnerRemindTimeArray.add("1h");
        spinnerRemindTimeArray.add("3h");
        spinnerRemindTimeArray.add("6h");
        spinnerRemindTimeArray.add("12h");
        spinnerRemindTimeArray.add("24h");

        adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, spinnerRemindTimeArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        remindTime.setAdapter(adapter);

        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        deadline.setText(currentDate);
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String strDate = dayOfMonth + "-" + (month+1) + "-" + year;
        this.remindTimeDay = dayOfMonth;
        this.remindTimeMonth = month;
        this.remindTimeYear = year;
        deadline.setText(strDate);;
    }
}
