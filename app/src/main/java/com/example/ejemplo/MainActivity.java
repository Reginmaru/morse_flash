package com.example.ejemplo;

import static java.lang.Thread.sleep;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MainActivity extends AppCompatActivity {


    EditText translatorEdit; // crear En donde escribes
    ImageView sendTranslatorInfo; // crear Boton para traducir
    private List<String> morseReply = new ArrayList(); // letras en morse
    private static Map<Character, String> arabicMorse = new HashMap(); // mapa de letras y morse
    int count = 0;

    final char letter[] = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
            'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', ' '}; // letras
    final String morse[] = {".-", "-...", "-.-.", "-..", ".", "..-.", "--.", "....", "..",
            ".---", "-.-", ".-..", "--", "-.", "---", ".--.", "--.-", ".-.", "...", "-",
            "..-", "...-", ".--", "-..-", "-.--", "--..", "|"}; // morse
    //0.10 . 0.2 - 0.10 (inbetween)
    private String[] morseType = {".", "-", "|"};
    private Integer[] morseSleep = {100, 300, 700};
    private Map<String, Integer> sleepTime;
    private CameraManager cameraManager; // el manager de la camara
    private String getCameraID; // para adelante o atras

    private Vibrator vibrator; // vibrate phone
    private char[] letters; // input letters

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Siempre se necesita
        setContentView(R.layout.activity_main); // Agarra el XML

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        createTableOfRecords();

        sleepTime = IntStream.range(0, morseType.length).boxed()
                .collect(Collectors.toMap(i -> morseType[i], i -> morseSleep[i]));

        translatorEdit = (EditText) findViewById(R.id.translator_edit); // en donde escribir
        sendTranslatorInfo = (ImageView) findViewById(R.id.image_view_edit_to_done); // boton de grabar

        createMapArabicToMorse();
        checkCameraWorksAddManager();
        setOnClickForTranslator();
    }

    private void vibrate(int millis){
        vibrator.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE));
    }

    TableLayout records;
    private void createTableOfRecords() {
        records = findViewById(R.id.table_records); // initialize table in code
        records.setBackgroundResource(R.drawable.bubble);

    }

    private void createTableRowRecord() {
        TableRow tr = new TableRow(getApplicationContext()); // Row
        tr.setLayoutParams( // Tell row how to act
                new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
        tr.setOrientation(LinearLayout.HORIZONTAL);
        setOnClickForTableRow(tr); // on click for row
        addTextView(tr);
        records.addView(tr);
    }

    private void setOnClickForTableRow(TableRow tr) {
        tr.setOnClickListener(new View.OnClickListener() { // need to do it this way
            @Override
            public void onClick(View v) {
                translatorEdit.setText(String.valueOf(letters));
                vibrate(100);
                vibrate(100);
            }
        });
    }

    private void addTextView(TableRow tr) {
        TextView tv = new TextView(getApplicationContext()); // text where u cant write
        tv.setText(String.valueOf(letters)); // set text from input
        tv.setLayoutParams( // set weight so it behaves normally
                new TableLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        tr.addView(tv); // add to table row.
    }

    private void createMapArabicToMorse() {
        for (int i = 0; i < letter.length; i++) {
            arabicMorse.put(letter[i], morse[i]); // poner letras y morse en el mapa
        }
    }

    private void checkCameraWorksAddManager() {
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            getCameraID = cameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            Toast.makeText(this, "No Camera?", Toast.LENGTH_SHORT).show();
        }
    }

    private void setOnClickForTranslator() {
        sendTranslatorInfo.setOnClickListener(new View.OnClickListener() { // escuchador de click
            @Override
            public void onClick(View view) { // que hacer cuando haces click
                animate_edit_to_done(sendTranslatorInfo);
                letters = translatorEdit.getText().toString().toUpperCase().toCharArray();
                for (char i : letters) {
                    morseReply.add(arabicMorse.get(i));
                }
                vibrate(200);
                createTableRowRecord(); // Add to table of records
                if(checkTorchWorks())
                    displayTorch(); // I did it with sleep() so you cant click multiple ones at once. Maybe change to thread?
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                morseReply.clear();
                animate_edit_to_done(sendTranslatorInfo);
            }
        });
    }

    private Boolean checkTorchWorks() {
        Boolean check = true;
        try {
            cameraManager.setTorchMode(getCameraID, false);
        } catch (Exception e) {
            check = false;
            Toast.makeText(this, "No Torch?", Toast.LENGTH_SHORT).show();
        }
        return check;
    }

    private void displayTorch() {
        final Boolean[] dontBreak = {true};
        morseReply.forEach(m -> {
            if (dontBreak[0]) {
                try {
                    char[] symbols = m.toCharArray();
                    if (symbols.length == 1) { // space
                        sleep(sleepTime.get(symbols[0]));
                        return;
                    } else {
                        for (char i : m.toCharArray()) {
                            cameraManager.setTorchMode(getCameraID, true);
                            sleep(sleepTime.get(String.valueOf(i)));
                            cameraManager.setTorchMode(getCameraID, false);
                            sleep(sleepTime.get("."));
                        }
                        sleep(2 * sleepTime.get(".")); // end of letter
                    }
                } catch (Exception e) {
                    dontBreak[0] = false;
                }
            }
        });
    }

    public void animate(View view) {
        ImageView v = (ImageView) view;
        Drawable d = v.getDrawable();
        if (d instanceof AnimatedVectorDrawable) {
            AnimatedVectorDrawable avd = (AnimatedVectorDrawable) d;
            avd.start();
        } else if (d instanceof AnimatedVectorDrawableCompat) {
            AnimatedVectorDrawableCompat avd = (AnimatedVectorDrawableCompat) d;
            avd.start();
        }
    }

    Boolean edit = true;
    public void animate_edit_to_done(View view) {
        ImageView v = (ImageView) view;
        AnimatedVectorDrawableCompat animDrawable = AnimatedVectorDrawableCompat.create(this, edit ? R.drawable.edit_to_done : R.drawable.done_to_edit);
        ImageView imageView = findViewById(R.id.image_view_edit_to_done);
        edit = !edit;
        imageView.setImageDrawable(animDrawable);
        animDrawable.start();
    }
}

