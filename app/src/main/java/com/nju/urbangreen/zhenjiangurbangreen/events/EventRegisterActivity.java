package com.nju.urbangreen.zhenjiangurbangreen.events;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.nju.urbangreen.zhenjiangurbangreen.ugo.UgoListActivity;
import com.nju.urbangreen.zhenjiangurbangreen.R;
import com.nju.urbangreen.zhenjiangurbangreen.attachments.AttachmentListActivity;
import com.nju.urbangreen.zhenjiangurbangreen.util.ActivityCollector;
import com.nju.urbangreen.zhenjiangurbangreen.util.UrbanGreenDB;
import com.nju.urbangreen.zhenjiangurbangreen.widget.DropdownEditText;
import com.nju.urbangreen.zhenjiangurbangreen.widget.TitleBarLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EventRegisterActivity extends AppCompatActivity {

    private int state;

    @BindView(R.id.btn_event_register_add_ugo)
    AppCompatButton btnEventRegisterAddUgo;
    @BindView(R.id.edit_event_register_code)
    public TextView etCode;
    @BindView(R.id.edit_event_register_name)
    public EditText etName;
    @BindView(R.id.edit_event_register_location)
    public EditText etLocation;
    @BindView(R.id.edit_event_register_damage_degree)
    public EditText etDamageDegree;
    @BindView(R.id.edit_event_register_lost_fee)
    public EditText etLostFee;
    @BindView(R.id.edit_event_register_compensation)
    public EditText etCompensation;
    @BindView(R.id.edit_event_register_relevant_person)
    public EditText etRelevantPerson;
    @BindView(R.id.edit_event_register_relevant_license_plate)
    public EditText etRelevantLicensePlate;
    @BindView(R.id.edit_event_register_relevant_contact)
    public EditText etRelevantContact;
    @BindView(R.id.edit_event_register_relevant_company)
    public EditText etRelevantCompany;
    @BindView(R.id.edit_event_register_relevant_address)
    public EditText etRelevantAddress;
    @BindView(R.id.edit_event_register_description)
    public EditText etDescription;
    @BindView(R.id.edit_event_register_reason)
    public EditText etReason;
    @BindView(R.id.edit_event_register_relevant_description)
    public EditText etRelevantDescription;

    @BindView(R.id.edit_event_register_time)
    public EditText etDateSelect;//日期选择编辑框
    private DatePickerDialog dtpckDateSelect;//日期选择dialog
    @BindView(R.id.ly_events_register_title_bar)
    public TitleBarLayout titleBarLayout;//标题栏
    @BindView(R.id.droplist_event_register_type)
    public DropdownEditText dropdownEditText;//事件类型选择的可编辑下拉列表，（已封装，可复用）
    @BindView(R.id.btn_event_register_add_files)
    public Button btnAddAttachment;
    private OneEvent oneEvent;//如果是从详情按钮启动的本活动，则需要获取一些事件的信息

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);
        setContentView(R.layout.activity_event_register);
        initViews();

    }

    public void initViews() {

        Intent intent = getIntent();
        oneEvent = (OneEvent) intent.getSerializableExtra("event");
        ButterKnife.bind(this);
        titleBarLayout.setTitleText("事件登记");
        titleBarLayout.setBtnBackClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //隐藏软件盘
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm.isActive()) {
                    imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
                }
                processBack();
            }
        });
        titleBarLayout.setBtnSelfDefBkg(R.drawable.ic_btn_self_def_up);
        titleBarLayout.setBtnSelfDefClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                state = 1;
            }
        });

        //初始化可编辑下拉框
        ArrayList<String> dropdownList = new ArrayList<>();
        dropdownList.addAll(Arrays.asList(getResources().getStringArray(R.array.eventTypeDropList)));
        dropdownEditText.setDropdownList(dropdownList);

        //初始化日期选择框
        Calendar calendar = Calendar.getInstance();
        int year, month, day;
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1;
        day = calendar.get(Calendar.DAY_OF_MONTH);

        dtpckDateSelect = new DatePickerDialog(EventRegisterActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        etDateSelect.setText(year + "-" + (month + 1) + "-" + dayOfMonth);
                    }
                }, year, month, day);

        etDateSelect.setText(year + "-" + (month + 1) + "-" + day);
        etDateSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dtpckDateSelect.show();
            }
        });
        if (oneEvent != null) {
            addDataToViews(oneEvent);
        }
        //初始化添加附件按钮
        btnAddAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EventRegisterActivity.this, AttachmentListActivity.class);
                startActivity(intent);
            }
        });
        //添加相关养护对象按钮点击事件
        btnEventRegisterAddUgo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EventRegisterActivity.this,UgoListActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        processBack();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
        Log.i("注册活动", "onDestroy");
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    //如果用户在登记了一些信息后没有上传就返回了，需要将其数据保存到数据库中
    public void saveTempViewData() {
        OneEvent tempEvent = new OneEvent();
        tempEvent.setCode(etCode.getText().toString());
        tempEvent.setName(etName.getText().toString());
        tempEvent.setType(dropdownEditText.getText());
        tempEvent.setLocation(etLocation.getText().toString());
        tempEvent.setDate_time(etDateSelect.getText().toString());
        tempEvent.setDamageDegree(etDamageDegree.getText().toString());

        tempEvent.setLostFee(etLostFee.getText().toString());
        tempEvent.setCompensation(etCompensation.getText().toString());
        tempEvent.setRelevantPerson(etRelevantPerson.getText().toString());
        tempEvent.setRelevantLicensePlate(etRelevantLicensePlate.getText().toString());
        tempEvent.setRelevantContact(etRelevantContact.getText().toString());
        tempEvent.setRelevantCompany(etRelevantCompany.getText().toString());
        tempEvent.setRelevantAddress(etRelevantAddress.getText().toString());
        tempEvent.setRelevantDescription(etRelevantDescription.getText().toString());
        tempEvent.setDescription(etDescription.getText().toString());
        tempEvent.setReason(etReason.getText().toString());
        tempEvent.setRegistrar("xk");
        tempEvent.setState(state);
        UrbanGreenDB urbanGreenDB = UrbanGreenDB.getInstance(EventRegisterActivity.this);
        urbanGreenDB.saveEvent(tempEvent);
    }

    private void addDataToViews(OneEvent oneEvent) {
        etCode.setText(oneEvent.getCode());
        etName.setText(oneEvent.getName());
        dropdownEditText.setText(oneEvent.getType());
        etLocation.setText(oneEvent.getLocation());
        etDateSelect.setText(oneEvent.getDate_time().toString());
        etDamageDegree.setText(oneEvent.getDamageDegree());
        etLostFee.setText(oneEvent.getLostFee());
        etCompensation.setText(oneEvent.getCompensation());
        etRelevantPerson.setText(oneEvent.getRelevantPerson());
        etRelevantLicensePlate.setText(oneEvent.getRelevantLicensePlate());
        etRelevantContact.setText(oneEvent.getRelevantContact());
        etRelevantCompany.setText(oneEvent.getRelevantCompany());
        etRelevantAddress.setText(oneEvent.getRelevantAddress());
        etDescription.setText(oneEvent.getDescription());
        etReason.setText(oneEvent.getReason());
        etRelevantDescription.setText(oneEvent.getRelevantDescription());
    }

    /**
     * 处理一些退出登记界面的事情
     */
    private void processBack() {
        //如果用户没有填写编号，就会弹框提示一下
        if (etCode.getText().toString().equals("")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("温馨提示");
            builder.setMessage("您没有填写事件编号，返回后相关信息不会保存");
            builder.setCancelable(false);
            builder.setPositiveButton("仍然退出", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
            builder.setNegativeButton("继续编辑", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            builder.show();
        } else {
            if (oneEvent == null) {//oneEvent为null，说明不是从详情按钮过来的
                Toast.makeText(this, "事件登记成功~", Toast.LENGTH_SHORT).show();
                saveTempViewData();
            }
            finish();
        }
    }
}
