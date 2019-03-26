package com.example.uart1;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Timer;
import java.util.TimerTask;
import cn.wch.ch34xuartdriver.*;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.hardware.usb.UsbManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.util.Log;

import hex.*;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    //声明定义私有型常量
    private static final String ACTION_USB_PERMISSION = "cn.wch.wchusbdriver.USB_PERMISSION";


    public readThread handlerThread;
    protected final Object ThreadLock = new Object();
    private EditText readText;
    private TextView readText1, readText2, writeText1, writeText2;
    private EditText writeText;
    private Spinner baudSpinner;
    private Spinner stopSpinner;
    private Spinner dataSpinner;
    private Spinner paritySpinner;
    private Spinner flowSpinner;
    private boolean isOpen;
    private Handler handler;
    private int retval;
    private MainActivity activity;

    private Button writeButton, configButton, openButton, clearButton;

    public byte[] writeBuffer;
    public byte[] readBuffer;
    public int actualNumBytes;

    public int numBytes;
    public byte count;
    public int status;
    public byte writeIndex = 0;
    public byte readIndex = 0;

    public int baudRate;
    public byte baudRate_byte;
    public byte stopBit;
    public byte dataBit;
    public byte parity;
    public byte flowControl;

    public boolean isConfiged = false;
    public boolean READ_ENABLE = false;
    public SharedPreferences sharePrefSettings;
    public String act_string;

    public int totalrecv, nowrecv;
    public int totalsend, nowsend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FjLog.d(TAG, "onCreate: ");
        setContentView(R.layout.main);//

        //创建一个CH34xUART对象，并用MyApp.driver指向它
        MyApp.driver = new CH34xUARTDriver(
                (UsbManager) getSystemService(Context.USB_SERVICE), this,
                ACTION_USB_PERMISSION);
        //初始化UI界面
        initUI();

        //若手机系统不支持USB HOST，则显示警告框
        if (!MyApp.driver.UsbFeatureSupported())// 判断系统是否支持USB HOST
        {
            Dialog dialog = new AlertDialog.Builder(MainActivity.this)
                    .setTitle("提示")
                    .setMessage("您的手机不支持USB HOST，请更换其他手机再试！")
                    .setPositiveButton("确认",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface arg0,
                                                    int arg1) {
//                                    System.exit(0);
                                }
                            }).create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        Toast.makeText(MainActivity.this,"恭喜！ 您的手机支持USB HOST", Toast.LENGTH_SHORT).show();

        //若执行到此处，说明手机系统支持USB HOST
        //保持常亮的屏幕的状态
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 保持常亮的屏幕的状态
        //动态分配发送/接收缓冲区
        writeBuffer = new byte[512];
        readBuffer = new byte[512];
        //先默认UART是关闭的
        isOpen = false;
        //先禁止“配置键”，“发送键”
        configButton.setEnabled(false);//true
        writeButton.setEnabled(false);
        activity = this;


        //打开流程主要步骤为ResumeUsbList，UartInit
        //为“open键”设置单击监听器
        openButton.setOnClickListener(new View.OnClickListener() {
            //当单击后
            @Override
            public void onClick(View arg0) {
                //若UART端口未打开
                if (!isOpen) {
                    //枚举并打开CH34x设备，这个函数包含了EnumerateDevice，OpenDevice操作
                    //返回值：0==成功，非0==失败
                    retval = MyApp.driver.ResumeUsbList();
                    //-1==失败，显示提示对话框
                    if (retval == -1)// ResumeUsbList方法用于枚举CH34X设备以及打开相关设备
                    {
                        Toast.makeText(MainActivity.this, "打开设备失败!",
                                Toast.LENGTH_SHORT).show();
                        MyApp.driver.CloseDevice();
                    }
                    //0==成功
                    else if (retval == 0){
                        //对UART进行初始化，若返回false==失败，则显示提示对话框
                        if (!MyApp.driver.UartInit()) {//对串口设备进行初始化操作
                            Toast.makeText(MainActivity.this, "设备初始化失败!",
                                    Toast.LENGTH_SHORT).show();
                            Toast.makeText(MainActivity.this, "打开" +
                                            "设备失败!",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        //若执行到此处，说明初始化UART成功
                        Toast.makeText(MainActivity.this, "打开设备成功!",
                                Toast.LENGTH_SHORT).show();
                        isOpen = true;
                        //将按键标签改为"Close"
                        openButton.setText("Close");
                        //使能“配置键”，“发送键”
                        configButton.setEnabled(true);
                        writeButton.setEnabled(true);
                        //创建并启动读线程来读取串口接收的数据
                        new readThread().start();
                    }
                    //
                    else {

                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setIcon(R.drawable.icon);
                        builder.setTitle("未授权限");
                        builder.setMessage("确认退出吗？");
                        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
//								MainFragmentActivity.this.finish();
                                System.exit(0);
                            }
                        });
                        builder.setNegativeButton("返回", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub

                            }
                        });
                        builder.show();

                    }
                }
                //若UART端口已打开
                else {
                    openButton.setText("Open");
                    configButton.setEnabled(false);
                    writeButton.setEnabled(false);
                    isOpen = false;
                    //阻塞200
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    //关闭UART端口
                    MyApp.driver.CloseDevice();
                    //清空“接收总字节数”
                    totalrecv = 0;
                    nowrecv = 0;
                    totalsend = 0;
                    nowsend = 0;
                }
            }
        });

        //为“配置键”设置单击监听器
        configButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                //保存配置(第8次提交)
//                save("baudRate="+baudRate);
                Toast.makeText(MainActivity.this, "保存成功!",
                        Toast.LENGTH_SHORT).show();

                if (MyApp.driver.SetConfig(baudRate, dataBit, stopBit, parity,//配置串口波特率，函数说明可参照编程手册
                        flowControl)) {
                    Toast.makeText(MainActivity.this, "串口设置成功!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "串口设置失败!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });


//匿名内部类：
//它会隐式的继承一个类或者实现一个接口，也可以说，匿名内部类是一个继承了该类或者实现了该接口的“子类匿名对象”。
//1.不需定义类名，用父类构造器直接创建出匿名内部类的对象来使用，且只能使用一次。
//2.因为创建出来的对象无名，所以在方法的参数列表()中创建然后传入给方法。
//new 父类构造器(参数列表) 或 实现接口() {
//	匿名内部类的类体部分，一般需要重写父类的方法，来实现具体的操作
//}

        //为“发送键”设置单击监听器
        writeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                //将“发送文本框”内字符串分解转换为hex值，并保存到数组to_send中
                byte[] to_send = hex.hexString_to_byteArray(writeText.getText().toString());
                //byte[] to_send = toByteArray(writeText.getText().toString());
//				byte[] to_send = toByteArray2(writeText.getText().toString());
                //发送数据：第一个参数为需要发送的字节数组，第二个参数为需要发送的字节长度，返回实际发送的字节长度
                int retval = MyApp.driver.WriteData(to_send, to_send.length);
                if (retval < 0)
                    Toast.makeText(MainActivity.this, "写失败!",
                            Toast.LENGTH_SHORT).show();
                nowsend = retval;
                totalsend += nowsend;
                writeText1.setText("本次:"+nowsend);
                writeText2.setText("总计"+totalsend);
            }
        });

//谷歌采用了只允许在主线程更新UI，所以作为线程通信桥梁的Handler也就应运而生了。
//Handler是用来结合线程的消息队列来发送、处理“Message对象”和“Runnable对象”的工具。
//每一个Handler实例之后会关联一个线程和该线程的消息队列。
//当你创建一个Handler的时候，它就会自动关联到所在的线程/消息队列，然后它就会陆续把Message/Runnalbe分发到消息队列，并在它们出队的时候处理掉。
        handler = new Handler() {

            //处理消息队列中的消息
            public void handleMessage(Message msg) {
                //因为本程序就只有UART_RX会发送消息，所以就默认为UART_RX发来的消息
                readText.setText(readText.getText() + (String) msg.obj);
                readText1.setText("本次:"+nowrecv);
                readText2.setText("总计:"+totalrecv);
//                Toast.makeText(MainActivity.this,"总计字节数="+totalrecv, Toast.LENGTH_SHORT).show();
//				readText.append((String) msg.obj);
            }
        };
    }

    public void save(String inputText){
        FileOutputStream out = null;
        BufferedWriter writer = null;
        try{
            out = openFileOutput("data",Context.MODE_PRIVATE);
            writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(inputText);
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try {
                if(writer != null){
                    writer.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        FjLog.d(TAG, "onStart: ");
    }

    
    //当Activity被恢复时
    protected void onResume() {
        super.onResume();
        FjLog.d(TAG, "onResume: ");
        //判断UART端口是否已打开，若未打开，则
        if(!MyApp.driver.isConnected()) {
            int retval = MyApp.driver.ResumeUsbPermission();
            if (retval == 0) {

            } else if (retval == -2) {
                Toast.makeText(MainActivity.this, "获取权限失败!",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }



    @Override
    protected void onPause() {
        super.onPause();
        FjLog.d(TAG, "onPause: ");
    }

    @Override
    protected void onStop() {
        super.onStop();
        FjLog.d(TAG, "onStop: ");
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        FjLog.d(TAG, "onDestroy: ");
        //关闭UART端口
        MyApp.driver.CloseDevice();
        MyApp.driver = null;
    }

    //初始化UI界面
    private void initUI() {
//通过调用findViewById来获取布局中的控件元素
//它的完整形式是this.findViewById()
//若所需要的控件不存在本Activity的布局中，那么在获取时需改为：
//ImageView view=(ImageView)view.findViewById(R.id.imageview);
        readText = (EditText) findViewById(R.id.ReadValues);
        writeText = (EditText) findViewById(R.id.WriteValues);
        configButton = (Button) findViewById(R.id.configButton);
        writeButton = (Button) findViewById(R.id.WriteButton);
        openButton = (Button) findViewById(R.id.open_device);
        clearButton = (Button) findViewById(R.id.clearButton);

        readText1 = (TextView) findViewById(R.id.ReadBytes1);
        readText2 = (TextView) findViewById(R.id.ReadBytes2);

        writeText1 = (TextView) findViewById(R.id.WriteBytes1);
        writeText2 = (TextView) findViewById(R.id.WriteBytes2);

        //“波特率”下拉菜单
        baudSpinner = (Spinner) findViewById(R.id.baudRateValue);
        ArrayAdapter<CharSequence> baudAdapter = ArrayAdapter
                .createFromResource(this, R.array.baud_rate,
                        R.layout.my_spinner_textview);
        baudAdapter.setDropDownViewResource(R.layout.my_spinner_textview);
        baudSpinner.setAdapter(baudAdapter);
        baudSpinner.setGravity(0x10);
        baudSpinner.setSelection(9);
        baudRate = 115200;

        //“停止位”下拉菜单
        stopSpinner = (Spinner) findViewById(R.id.stopBitValue);
        ArrayAdapter<CharSequence> stopAdapter = ArrayAdapter
                .createFromResource(this, R.array.stop_bits,
                        R.layout.my_spinner_textview);
        stopAdapter.setDropDownViewResource(R.layout.my_spinner_textview);
        stopSpinner.setAdapter(stopAdapter);
        stopSpinner.setGravity(0x01);
        stopBit = 1;

        //“数据位”下拉菜单
        dataSpinner = (Spinner) findViewById(R.id.dataBitValue);
        ArrayAdapter<CharSequence> dataAdapter = ArrayAdapter
                .createFromResource(this, R.array.data_bits,
                        R.layout.my_spinner_textview);
        dataAdapter.setDropDownViewResource(R.layout.my_spinner_textview);
        dataSpinner.setAdapter(dataAdapter);
        dataSpinner.setGravity(0x11);
        dataSpinner.setSelection(3);
        dataBit = 8;

        //“校验位”下拉菜单
        paritySpinner = (Spinner) findViewById(R.id.parityValue);
        ArrayAdapter<CharSequence> parityAdapter = ArrayAdapter
                .createFromResource(this, R.array.parity,
                        R.layout.my_spinner_textview);
        parityAdapter.setDropDownViewResource(R.layout.my_spinner_textview);
        paritySpinner.setAdapter(parityAdapter);
        paritySpinner.setGravity(0x11);
        parity = 0;

        //“流控”下拉菜单
        flowSpinner = (Spinner) findViewById(R.id.flowControlValue);
        ArrayAdapter<CharSequence> flowAdapter = ArrayAdapter
                .createFromResource(this, R.array.flow_control,
                        R.layout.my_spinner_textview);
        flowAdapter.setDropDownViewResource(R.layout.my_spinner_textview);
        flowSpinner.setAdapter(flowAdapter);
        flowSpinner.setGravity(0x11);
        flowControl = 0;

        //为各个下拉菜单设置监听器
        baudSpinner.setOnItemSelectedListener(new MyOnBaudSelectedListener());
        stopSpinner.setOnItemSelectedListener(new MyOnStopSelectedListener());
        dataSpinner.setOnItemSelectedListener(new MyOnDataSelectedListener());
        paritySpinner
                .setOnItemSelectedListener(new MyOnParitySelectedListener());
        flowSpinner.setOnItemSelectedListener(new MyOnFlowSelectedListener());

        //为“清空键”设置监听器
        clearButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                nowrecv = 0;
                totalrecv = 0;
                readText.setText("");
                readText1.setText("本次:");
                readText2.setText("总计:");
                totalsend = 0;
                nowsend = 0;
                writeText1.setText("本次:");
                writeText2.setText("总计:");
            }
        });
        return;
    }


    //“波特率”下拉菜单的监听器
    public class MyOnBaudSelectedListener implements OnItemSelectedListener {

        //监听到“Item被选择”后的回调处理
        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int position, long id) {
            //1. parent.getItemAtPosition(position) 获得用户当前点选的下拉菜单中的Item
            //2. .toString() 将此Item转换为String
            //3. Integer.parseInt() 将此String转换为Integer
            //4. 赋值给baudRate
            baudRate = Integer.parseInt(parent.getItemAtPosition(position)
                    .toString());
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    //“停止位”下拉菜单的监听器
    public class MyOnStopSelectedListener implements OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int position, long id) {
            stopBit = (byte) Integer.parseInt(parent
                    .getItemAtPosition(position).toString());
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }

    }

    //“数据位”下拉菜单的监听器
    public class MyOnDataSelectedListener implements OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int position, long id) {
            dataBit = (byte) Integer.parseInt(parent
                    .getItemAtPosition(position).toString());
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }

    }

    //“奇偶位”下拉菜单的监听器
    public class MyOnParitySelectedListener implements OnItemSelectedListener {

        //监听到“Item被选择”后的回调处理
        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int position, long id) {

            //获得用户当前点选的下拉菜单中的Item，并转化为String
            String parityString = new String(parent.getItemAtPosition(position)
                    .toString());
            if (parityString.compareTo("None") == 0) {
                parity = 0;
            }

            if (parityString.compareTo("Odd") == 0) {
                parity = 1;
            }

            if (parityString.compareTo("Even") == 0) {
                parity = 2;
            }

            if (parityString.compareTo("Mark") == 0) {
                parity = 3;
            }

            if (parityString.compareTo("Space") == 0) {
                parity = 4;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }

    }

    public class MyOnFlowSelectedListener implements OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int position, long id) {
            String flowString = new String(parent.getItemAtPosition(position)
                    .toString());
            if (flowString.compareTo("None") == 0) {
                flowControl = 0;
            }

            if (flowString.compareTo("CTS/RTS") == 0) {
                flowControl = 1;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }


    //UART_RX线程
    private class readThread extends Thread {

        public void run() {

            //声明定义一块接收缓冲区buffer
            byte[] buffer = new byte[4096];

            //死循环
            while (true) {

                Message msg = Message.obtain();
                //若UART端口未打开，则退出死循环
                if (!isOpen) {
                    break;
                }
                //从UART_RX读取数据到buffer中，并返回读取的字节数
                //注意：这里读到的是一个一个的字节，每个字节代表二进制的hex值
                int length = MyApp.driver.ReadData(buffer, 4096);
                //若读取到数据
                if (length > 0) {
                    //将buffer中接收到的hex字节数组转换为String
					//String recv = toHexString(buffer, length);
					String recv = hex.s8array_to_hex2(buffer, length);
//					String recv = new String(buffer, 0, length);
                    nowrecv = length;
                    //累计总接收字节数，然后转换为String类型
                    totalrecv += length;
 //                   String recv = String.valueOf(totalrecv);
                    //令msg.obj指向recv，然后用handler发送到消息队列中
                    msg.obj = recv;
                    handler.sendMessage(msg);
                }
            }
        }
    }



/*
    //入口：0x1,0x2,0x3,0x4,0x90,0xAB,0xCD,0xEF
    //出口："0102030490ABCDEF"
    private String toHexString(byte[] arg, int length) {
        String result = new String();
        //若传入的不为null
        if (arg != null) {
            //
            for (int i = 0; i < length; i++) {
                result = result
                        + (Integer.toHexString(
                        arg[i] < 0 ? arg[i] + 256 : arg[i]).length() == 1 ? "0"
                        + Integer.toHexString(arg[i] < 0 ? arg[i] + 256
                        : arg[i])
                        : Integer.toHexString(arg[i] < 0 ? arg[i] + 256
                        : arg[i])) + " ";
            }
            return result;
        }
        return "";
    }
*/
/*
    //入口："1234567890ABCDEF"
    //出口：0x12,0x34,0x56,0x78,0x90,0xAB,0xCD,0xEF
    private byte[] toByteArray(String arg) {
        //若入口传入的字符串非空
        if (arg != null) {
            //先去除String中的' '，然后将String转换为char数组
            //动态分配一块空间
            char[] NewArray = new char[1000];
            //将入口传入的字符串分解为字符存入字符型数组array中
            char[] array = arg.toCharArray();
            int length = 0;
            //将字符型数组array中的' '忽略，其余的转存到字符型数组NewArray中
            for (int i = 0; i < array.length; i++) {
                if (array[i] != ' ') {
                    NewArray[length] = array[i];
                    length++;
                }
            }
            //将char数组中的值转成一个实际的十进制数组
            //若length是偶数，则临时变量EvenLength=length
            //若length是奇数，则临时变量EvenLength=length+1
            int EvenLength = (length % 2 == 0) ? length : length + 1;
            //若EvenLength!=0
            if (EvenLength != 0) {
                int[] data = new int[EvenLength];
                data[EvenLength - 1] = 0;
                //将NewArray[]中各个元素，字符转为数值
                for (int i = 0; i < length; i++) {
                    //若NewArray[i]是字符'0'~'9'，则-'0'，获得对应的数值0~9
                    if (NewArray[i] >= '0' && NewArray[i] <= '9') {
                        data[i] = NewArray[i] - '0';
                    }
                    //若NewArray[i]是字符'a'~'f'，则-'a'+10，获得对应的数值10~15
                    else if (NewArray[i] >= 'a' && NewArray[i] <= 'f') {
                        data[i] = NewArray[i] - 'a' + 10;
                    }
                    //若NewArray[i]是字符'A'~'F'，则-'A'+10，获得对应的数值10~15
                    else if (NewArray[i] >= 'A' && NewArray[i] <= 'F') {
                        data[i] = NewArray[i] - 'A' + 10;
                    }
                }
                //将 每个char的值每两个组成一个16进制数据
                //data[i]为hex值的高4位，data[i+1]为hex值的低4位
                //将data[i]，data[i+1]组合为1个hex值
                byte[] byteArray = new byte[EvenLength / 2];
                for (int i = 0; i < EvenLength / 2; i++) {
                    byteArray[i] = (byte) (data[i * 2] * 16 + data[i * 2 + 1]);
                }
                return byteArray;
            }
        }
        return new byte[] {};
    }
*/
/*
    private byte[] toByteArray2(String arg) {
        if (arg != null) {
            //先去除String中的' '，然后将String转换为char数组
            char[] NewArray = new char[1000];
            char[] array = arg.toCharArray();
            int length = 0;
            for (int i = 0; i < array.length; i++) {
                if (array[i] != ' ') {
                    NewArray[length] = array[i];
                    length++;
                }
            }
            NewArray[length] = 0x0D;
            NewArray[length + 1] = 0x0A;
            length += 2;

            byte[] byteArray = new byte[length];
            for (int i = 0; i < length; i++) {
                byteArray[i] = (byte)NewArray[i];
            }
            return byteArray;

        }
        return new byte[] {};
    }
*/

}
