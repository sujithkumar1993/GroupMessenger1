package edu.buffalo.cse.cse486586.groupmessenger1;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 * 
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {

    static final String TAG = GroupMessengerActivity.class.getSimpleName();

    static final int SERVER_PORT = 10000;
    public int sequence = 0;
    final String[] AVD_PORTS = {"11108", "11112", "11116", "11120", "11124"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);


        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));



        try {

            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            Log.e(TAG,"******* SERVER SOCKET CREATED *******");
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,serverSocket);
            Log.e(TAG,"******* PINGED SOCKET CREATED *******");

        }
        catch (IOException e)
        {

            Log.e(TAG, "Can't create a ServerSocket");
            return;
        }







        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */
        TextView tv = (TextView) findViewById(R.id.textView1);





        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));
        
        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs.
         */

        final Button sendButton = (Button) findViewById(R.id.button4);

        sendButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final EditText editText = (EditText) findViewById(R.id.editText1);
                String msg = editText.getText().toString();
                editText.setText("");
                TextView tv = (TextView) findViewById(R.id.textView1);
                tv.append(msg+"\n");
                tv.setMovementMethod(new ScrollingMovementMethod());

                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,msg,myPort);
            }});


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }



    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {
        int counter = 0;
        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];
            Log.e(TAG,"*** INSIDE ServerTask METHOD ***");


            /*
             * TODO: Fill in your server code that receives messages and passes them
             * to onProgressUpdate().
             */

            try{
                while (true) {

                    Socket socket = serverSocket.accept();
                    Log.e(TAG, "***ServerTask Method --------Connection accepted by server***");

                    BufferedReader brIncomingMsg = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String incomingString = brIncomingMsg.readLine();
                    String c = String.valueOf(counter);
                    if (incomingString != null) {
                        publishProgress(c,incomingString);
                        Log.e(TAG, "***Publish progress method*****");
                        socket.close();

                    }
                    counter +=1;
                }




            }
            catch (Exception e){

                Log.e(TAG,"Error in ServerTask method ");
            }


            return null;
        }

        protected void onProgressUpdate(String...strings) {
            /*
             * The following code displays what is received in doInBackground().
             */



            Log.e(TAG,"INSIDE OnProgressUpdate Method*****");
            String msgReceived = strings[1].trim();
            Log.e(TAG,"OnProgressUpdate Method ---> String received is "+msgReceived);
            TextView localTextView = (TextView) findViewById(R.id.textView1);
            localTextView.append(msgReceived+ "\t\n");
            localTextView.setMovementMethod(new ScrollingMovementMethod());



            /*
             * The following code creates a file in the AVD's internal storage and stores a file.
             *
             * For more information on file I/O on Android, please take a look at
             * http://developer.android.com/training/basics/data-storage/files.html
             */


            String filename = strings[0];
            String string = msgReceived + "\n";
            FileOutputStream outputStream;

            try {
                outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream.write(string.getBytes());
                outputStream.close();
            } catch (Exception e) {
                Log.e(TAG, "File write failed");
            }



            return;
        }
    }















    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {
            for (int i =0;i <=4;++i){
                try {
                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),Integer.parseInt(AVD_PORTS[i]));

                    PrintWriter textOutput = new PrintWriter(socket.getOutputStream(),true);
                    textOutput.println(msgs[0]);
                    socket.close();
                }
                catch (UnknownHostException e) {
                    Log.e(TAG, "ClientTask UnknownHostException in "+ AVD_PORTS[i]);
                    e.printStackTrace();
                } catch (IOException e) {
                    Log.e(TAG, "ClientTask socket IOException"+AVD_PORTS[i]);
                    e.printStackTrace();
                }
            }
            return null;
        }
    }



}
