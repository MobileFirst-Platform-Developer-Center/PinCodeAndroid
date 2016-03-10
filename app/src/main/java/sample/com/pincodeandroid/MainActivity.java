/**
* Copyright 2016 IBM Corp.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package sample.com.pincodeandroid;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.worklight.wlclient.api.WLClient;
import com.worklight.wlclient.api.WLFailResponse;
import com.worklight.wlclient.api.WLResourceRequest;
import com.worklight.wlclient.api.WLResponse;
import com.worklight.wlclient.api.WLResponseListener;

import java.net.URI;
import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity {

    private TextView resultTxt;

    private BroadcastReceiver errorReceiver, challengeReceiver;

    private final String ACTIVITY_NAME = "MainActivity";

    private MainActivity _this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _this = this;

        WLClient client = WLClient.createInstance(this);
        PinCodeChallengeHandler pinCodeChallengeHandler = new PinCodeChallengeHandler("PinCodeAttempts");
        client.registerChallengeHandler(pinCodeChallengeHandler);


        Button getBalanceBtn = (Button) findViewById(R.id.getBalance);
        resultTxt = (TextView) findViewById(R.id.result);

        getBalanceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                URI adapterPath = null;
                try {
                    adapterPath = new URI("/adapters/ResourceAdapter/balance");
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                assert adapterPath != null;
                WLResourceRequest request = new WLResourceRequest(adapterPath, WLResourceRequest.GET);
                request.send(new WLResponseListener() {
                    @Override
                    public void onSuccess(WLResponse wlResponse) {
                        Log.d("Balance: ", wlResponse.getResponseText());
                        updateTextView("Balance: " + wlResponse.getResponseText());
                    }

                    @Override
                    public void onFailure(WLFailResponse wlFailResponse) {
                        Log.d("Failed to get balance: ", wlFailResponse.getErrorMsg());
                        updateTextView("Failed to get balance.");
                    }
                });
            }
        });

        errorReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(ACTIVITY_NAME, "errorReceiver");
                alertError(intent.getStringExtra("errorMsg"));
            }
        };

        challengeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(ACTIVITY_NAME, "challengeReceiver");
                alertMsg(intent.getStringExtra("msg"));
            }
        };
    }


    public void updateTextView(final String str){
        Runnable run = new Runnable() {
            public void run() {
                resultTxt.setText(str);
            }
        };
        this.runOnUiThread(run);
    }

    public void alertMsg(final String msg) {
        Runnable run = new Runnable() {
            public void run() {
                final Intent intent = new Intent();
                final EditText pinCodeTxt = new EditText(_this);
                pinCodeTxt.setHint("PIN CODE");
                pinCodeTxt.setInputType(InputType.TYPE_CLASS_NUMBER);

                AlertDialog.Builder builder = new AlertDialog.Builder(_this);
                builder.setMessage(msg)
                        .setTitle("Protected");
                builder.setView(pinCodeTxt);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        intent.setAction(Constants.ACTION_CHALLENGE_SUBMIT_ANSWER);
                        intent.putExtra("pinCodeTxt", pinCodeTxt.getText().toString());
                        LocalBroadcastManager.getInstance(_this).sendBroadcast(intent);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        intent.setAction(Constants.ACTION_CHALLENGE_CANCEL);
                        LocalBroadcastManager.getInstance(_this).sendBroadcast(intent);
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
            }
        };

        _this.runOnUiThread(run);

    }


    public void alertError(final String errorMsg) {
        Runnable run = new Runnable() {
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(_this);
                builder.setMessage(errorMsg)
                        .setTitle("Error");
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
            }
        };

        _this.runOnUiThread(run);

    }


    @Override
    protected void onStart() {
        Log.d(ACTIVITY_NAME, "onStart");
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(challengeReceiver, new IntentFilter(Constants.ACTION_CHALLENGE_RECEIVED));
        LocalBroadcastManager.getInstance(this).registerReceiver(errorReceiver, new IntentFilter(Constants.ACTION_CHALLENGE_FAILURE));
    }

    @Override
    protected void onStop() {
        Log.d(ACTIVITY_NAME, "onStop");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(challengeReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(errorReceiver);
        super.onStop();
    }
}
