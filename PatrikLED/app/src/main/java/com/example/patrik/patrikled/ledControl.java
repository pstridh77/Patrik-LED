package com.example.patrik.patrikled;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;

import android.widget.Button;

import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;
import android.telephony.SmsManager;

import java.io.IOException;
import java.util.Calendar;
import java.util.UUID;



public class ledControl extends AppCompatActivity {
    Button btnOn, btnOff, btnDis, btnAlarm, btnMonitor, btnStopMonitor;
    EditText textBox;
    CheckBox chkBox;

    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    //private boolean continueMonitorBT =false;
    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private  AsyncTask monitorBTThread;
    private boolean sendSMS  = false; //determine if SMS shall be sent nor not




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_led_control);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        Intent newint = getIntent();
        address = newint.getStringExtra(DeviceList.EXTRA_ADDRESS); //receive the address of the bluetooth device
        //view of the ledControl
        setContentView(R.layout.activity_led_control);

        //call the widgtes
        btnOn = (Button)findViewById(R.id.button2);
        btnOff = (Button)findViewById(R.id.button3);
        btnDis = (Button)findViewById(R.id.button4);
        btnAlarm = (Button)findViewById(R.id.button5);
        textBox = (EditText) findViewById(R.id.editText);
        btnMonitor = (Button)findViewById(R.id.button6);
        btnStopMonitor= (Button)findViewById(R.id.button7);
        chkBox= (CheckBox)findViewById(R.id.checkBox);


        new ConnectBT().execute();

        //commands to be sent to bluetooth
        btnOn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                turnOnLed();      //method to turn on
            }
        });

        btnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                turnOffLed();   //method to turn off
            }
        });

        btnDis.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Disconnect(); //close connection
            }
        });

        btnAlarm.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendSMS("Test Message"); //Send sms
            }
        });

        btnMonitor.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                monitorBTThread = new MonitorBT().execute();
                btnStopMonitor.setEnabled(true);
                btnMonitor.setEnabled(false);
                //monitorBT(); //Monitor BT for incomming alarm
            }
        });

        btnStopMonitor.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                monitorBTThread.cancel(true); //Stop Monitor BT for incomming alarm

                btnStopMonitor.setEnabled(false );
                btnMonitor.setEnabled(true);
            }
        });

        chkBox.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                sendSMS= chkBox.isChecked();
            }
        });
    }

    private void Disconnect()
    {
        if (btSocket!=null) //If the btSocket is busy
        {
            try
            {
                if ( monitorBTThread == null ){}
                else{
                    monitorBTThread.cancel(true); //Stop Monitor BT for incomming alarm
                    btnStopMonitor.setEnabled(false );
                    btnMonitor.setEnabled(true);
                }

                textBox.setText(textBox.getText() + "\n" + "Closing BT");
                btSocket.close(); //close connection
            }
            catch (IOException e)
            { msg("Error");}
        }
        finish(); //return to the first layout

    }

    private void turnOffLed()
    {
        if (btSocket!=null)
        {
            try
            {
                textBox.setText(textBox.getText() + "\n" + "Sent: 0");
                btSocket.getOutputStream().write("0".toString().getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

    // fast way to call Toast
    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_device_list, menu);
        return true;
    }

    private void turnOnLed()
    {
        if (btSocket!=null)
        {
            textBox.setText(textBox.getText() + "\n" + "Sent: 1");
            try
            {
                btSocket.getOutputStream().write("1".toString().getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }

        }
    }
    private void sendSMS(String message)
    {

            try {
                //textBox.setText(textBox.getText() + "\n" + "Sending SMS...");
                //TODO Add phonenumber to settings meneu
                String phoneNumber = "0728852028";
                //String message = "Gräsklippar larm: Gunter är borta";

                Calendar c = Calendar.getInstance();
                String dateString = c.getTime().toString();
                message=dateString + ": " + message;
                if (sendSMS==true){
                    Log.d("sendSMS","Sending SMS: " + message);
                    SmsManager sms = SmsManager.getDefault();
                    sms.sendTextMessage(phoneNumber, null, message , null, null);

                }else{

                    Log.d("sendSMS","SMS Sending Inhibit: " + message);
                }


                //textBox.setText(textBox.getText() + "Done!");
            }
            catch (Exception e)
            {
                msg("Error: Unable to send SMS." + e.toString());
            }

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(ledControl.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            }
            else
            {
                msg("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }

    private class MonitorBT extends AsyncTask<Void, String, Boolean>  // Param, Progress, Result Monitor thread
    {
        @Override
        protected Boolean doInBackground(Void... devices) //monitor in background
        {
            boolean alarmRecived = false;
            try {
                byte[] tempData = new byte[1024];
                byte[] data = new byte[2];

                int alarmReading, noOfBytes;

                String alarmMessage = "";
                if (btSocket != null) {
                    try {

                        publishProgress("Start Reading");
                        if (btSocket.getInputStream().available() > 0) { //Check if the stream already has data
                            noOfBytes = btSocket.getInputStream().read(tempData); //Clear stream
                        }
                        while (!isCancelled()) {//Continue monitor until we cancel thread
                            if (btSocket.getInputStream().available() > 0) {
                                //textBox.setText(textBox.getText() + "\n" + "Read BT...");
                                alarmReading = btSocket.getInputStream().read();
                                //Convert to  text
                                //textBox.setText((char)alarmReading + "\n" + textBox.getText());
                                noOfBytes = btSocket.getInputStream().read(data); //Read the CRLF char and throw away
                                switch ((char) alarmReading) {
                                    case 'C': {
                                        alarmMessage = "Gunter in Charger, be calm";
                                        alarmRecived = false;
                                        publishProgress(alarmMessage);
                                        sendSMS(alarmMessage);
                                        break;
                                    }
                                    case 'A': {
                                        alarmMessage = "Alarm! Gunter ist Weggelaufen!";
                                        alarmRecived = true;
                                        publishProgress(alarmMessage);
                                        sendSMS(alarmMessage);
                                        break;
                                    }
                                    case '0': {
                                        alarmMessage = "Led: Off";
                                        //alarmRecived = true;
                                        publishProgress(alarmMessage);
                                        //sendSMS(alarmMessage);
                                        break;
                                    }
                                    case '1': {
                                        alarmMessage = "Led: On";
                                        publishProgress(alarmMessage);
                                        //sendSMS(alarmMessage);
                                        break;
                                    }
                                    case 'T': {//Timestamp recieved
                                        //publishProgress("Read String: " + (char)alarmReading);
                                        StringBuilder charString  = new StringBuilder();
                                        alarmReading = btSocket.getInputStream().read();

                                        while (alarmReading != 13){
                                            charString.append((char)alarmReading);
                                            alarmReading = btSocket.getInputStream().read();
                                        }
                                        publishProgress("T: " + charString);
                                        alarmReading = btSocket.getInputStream().read();//Read the last LF
                                        break;
                                    }
                                    default: {
                                        publishProgress("Unknown message recived from Arduino: " + (char) alarmReading);
                                        if (btSocket.getInputStream().available() > 0) { //Check if the stream already has data
                                            publishProgress("Clearing Stream");
                                            noOfBytes = btSocket.getInputStream().read(tempData); //Clear stream
                                        }
                                    }
                                }
                            }
                        Thread.sleep(1000);
                        }

                        //textBox.setText("Done reading" + "\n" + textBox.getText());

                    } catch (IOException e) {
                        msg("Error in monitoring BT: " + e.toString());
                    }

                }

            } catch (Exception e) {
                msg("Error in monitoring thread: " + e.toString());
            }
            return alarmRecived;
        }

        @Override
        protected void onProgressUpdate(String... message) {
            EditText textBox;
            textBox = (EditText) findViewById(R.id.editText);

            //textBox.setText(message[0]+ "\n" + textBox.getText());
            textBox.setText(message[0]);
        }


    }
}

