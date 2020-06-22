package com.example.todolist;

import android.os.Environment;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MyFileIO {

    private static final String FILE_NAME = "data.txt";
    private static final String FOLDER_NAME = "/Download/";

    public List<ToDoElement> getListOfElements() {
        List<ToDoElement> listElements = new ArrayList<>();

        if(isFileCreated()) {
            JSONArray jsonArray = getJsonArrayFromFile();

            for(int i=0; i<jsonArray.length(); i++) {
                try {
                    JSONObject json_data = jsonArray.getJSONObject(i);
                    String title = json_data.getString("title");
                    String note = json_data.getString("note");
                    String remindTime = json_data.getString("remindTime");
                    boolean isRemindTimeOptionChecked = json_data.getBoolean("isRemindTimeOptionChecked");
                    boolean isStatusDone = json_data.getBoolean("status");
                    int priority = json_data.getInt("priority");
                    String creationDate = json_data.getString("creationDate");
                    String deadline = json_data.getString("deadline");
                    String imageTitle = json_data.getString("imageTitle");

                    ToDoElement newElement = new ToDoElement(title, note, remindTime, isRemindTimeOptionChecked, isStatusDone, priority, creationDate, deadline, imageTitle);
                    listElements.add(newElement);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return listElements;
    }

    public boolean generateTextFile(List<ToDoElement> listElements) {
        String outputString;

        StringBuilder stringBuilder = new StringBuilder();

        for (ToDoElement element : listElements) {
            stringBuilder.append("Tytuł: " + element.getTitle() + "\n");
            stringBuilder.append("Notatka: " + element.getNote() + "\n");

            int priority = element.getPriority();
            String strPriority = "";

            if(priority == 1) {
                strPriority = "Bardzo ważne";
            } else if(priority == 2) {
                strPriority = "Ważne";
            } else if(priority == 3) {
                strPriority = "Normalne";
            } else if(priority == 4) {
                strPriority = "Mało ważne";
            }

            stringBuilder.append("Priorytet: " + strPriority + "\n");
            stringBuilder.append("Status: " + (element.isStatus() ? "Zrobione" : "Do zrobienia") + "\n");
            stringBuilder.append("Data utworzenia: " + element.getDeadline() + "\n");
            stringBuilder.append("Termin wykonania: " + element.getDeadline() + "\n");
            stringBuilder.append("\n------------------------------------------------------\n\n");
        }

        outputString = stringBuilder.toString();

        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + FOLDER_NAME;
        File textFile = new File(path, "lista_zadan.txt");

        try {
            FileOutputStream fos = new FileOutputStream(textFile);
            fos.write(outputString.getBytes());
            fos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public JSONObject getJsonObjectFromFile() {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(readJsonStringFromFile());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public JSONArray getJsonArrayFromFile() {
        JSONObject jsonObject = getJsonObjectFromFile();
        JSONArray jsonArray = null;
        try {
            jsonArray = jsonObject.getJSONArray("Elements");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonArray;
    }

    public String readJsonStringFromFile() {
        if(isFileCreated())
        {
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + FOLDER_NAME;
            File textFile = new File(path, FILE_NAME);
            StringBuilder jsonString = new StringBuilder();
            try {
                BufferedReader br = new BufferedReader(new FileReader(textFile));
                String line;
                while ((line = br.readLine()) != null) {
                    jsonString.append(line);
                }
                br.close();
            } catch (IOException e) {

            }
            if(isJSONValid(jsonString.toString())) {
                return jsonString.toString();
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    private boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            try {
                new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

    public void saveListOfElementsToFile(String elementObject) {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + FOLDER_NAME;
        File textFile = new File(path, FILE_NAME);

        try {
            FileOutputStream fos = new FileOutputStream(textFile);
            fos.write(elementObject.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Boolean isFileCreated() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + FOLDER_NAME;
        File file = new File(path, FILE_NAME );
        if (file.exists()) {
            return true;
        } else {
            return false;
        }
    }
}
