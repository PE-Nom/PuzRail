package com.pentech.puzrail.ui;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Switch;

import com.pentech.puzrail.MainActivity;
import com.pentech.puzrail.R;
import com.pentech.puzrail.database.DBAdapter;
import com.pentech.puzrail.database.SettingParameter;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static com.pentech.puzrail.database.SettingParameter.DIFFICULTY_AMATEUR;
import static com.pentech.puzrail.database.SettingParameter.DIFFICULTY_BEGINNER;
import static com.pentech.puzrail.database.SettingParameter.DIFFICULTY_PROFESSIONAL;

/**
 * Created by c0932 on 2017/08/30.
 */

public class SettingParameterDialog {

    private static String TAG = "SettingParameterDialog";
    private AppCompatActivity activity;
    private SettingParameter settingParameter;
    private DBAdapter db;

    public SettingParameterDialog(AppCompatActivity activity,SettingParameter settingParameter,DBAdapter db){
        this.activity = activity;
        this.settingParameter = settingParameter;
        this.db = db;
    }

    public void show(){
        LayoutInflater inflater = (LayoutInflater) this.activity.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View dialogLayout = inflater.inflate(R.layout.level_setting_dialog, (ViewGroup) this.activity.findViewById(R.id.layout_root));
        AlertDialog.Builder builder = new AlertDialog.Builder(this.activity);
        builder.setTitle("レベル設定");
        builder.setView(dialogLayout);

        // radioボタンの初期化
        RadioGroup radioGroup = (RadioGroup) dialogLayout.findViewById(R.id.settingDifficulty);
        int dif = this.settingParameter.getDifficultyMode();
        if(dif == DIFFICULTY_BEGINNER){
            radioGroup.check(R.id.difficulty_beginner);
        }
        else if(dif == DIFFICULTY_AMATEUR){
            radioGroup.check(R.id.difficulty_amateur);
        }
        else{
            radioGroup.check(R.id.difficulty_professional);
        }

        // switchの初期化
        Switch vibrationSwitch = (Switch) dialogLayout.findViewById(R.id.vibration_mode);
        vibrationSwitch.setChecked(this.settingParameter.isVibrate());

        // ok 選択操作
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                int difficulty_mode = 0;
                RadioGroup radioGroup = (RadioGroup) dialogLayout.findViewById(R.id.settingDifficulty);
                Switch vibrationSwitch = (Switch) dialogLayout.findViewById(R.id.vibration_mode);
                if (radioGroup.getCheckedRadioButtonId() == R.id.difficulty_beginner) {
                    difficulty_mode = DIFFICULTY_BEGINNER;
                    Log.d(TAG,"beginner selected");
                }
                else if(radioGroup.getCheckedRadioButtonId() == R.id.difficulty_amateur){
                    difficulty_mode = DIFFICULTY_AMATEUR;
                    Log.d(TAG,"amateur selected");
                }
                else{
                    difficulty_mode = DIFFICULTY_PROFESSIONAL;
                    Log.d(TAG,"professional selected");
                }
                if(vibrationSwitch.isChecked()){
                    Log.d(TAG,"vibration checked");
                }
                else{
                    Log.d(TAG,"vibration no checked");
                }
                boolean vibration = vibrationSwitch.isChecked();
                SettingParameterDialog.this.settingParameter.setDifficultyMode(difficulty_mode);
                SettingParameterDialog.this.settingParameter.setVibrationMode(vibration);
                SettingParameterDialog.this.db.updateDifficultySetting(difficulty_mode);
                SettingParameterDialog.this.db.updateVibrationSetting(vibration);
            }
        });

        // cancel操作
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }
        );
        builder.create().show();

    }
}
