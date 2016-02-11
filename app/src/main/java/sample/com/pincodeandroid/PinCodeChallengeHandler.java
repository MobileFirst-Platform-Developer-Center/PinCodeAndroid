package sample.com.pincodeandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;

import com.worklight.wlclient.api.challengehandler.WLChallengeHandler;

import org.json.JSONException;
import org.json.JSONObject;

public class PinCodeChallengeHandler extends WLChallengeHandler {

    Activity context;


    public PinCodeChallengeHandler(String securityCheck, Activity context) {
        super(securityCheck);
        this.context = context;
    }

    @Override
    public void handleFailure(JSONObject jsonObject) {
        Log.d("Failure", jsonObject.toString());
        try {
            if (!jsonObject.isNull("failure")) {
                alertError(jsonObject.getString("failure"));
            } else {
                alertError("Unknown error");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void handleChallenge(JSONObject jsonObject) {
        Log.d("Handle Challenge", jsonObject.toString());
        try{
            if (jsonObject.isNull("errorMsg")){
                alertMsg("This data requires a PIN code.\n Remaining attempts: " + jsonObject.getString("remainingAttempts"));
            } else {
                alertMsg(jsonObject.getString("errorMsg") + "\nRemaining attempts: " + jsonObject.getString("remainingAttempts"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void alertMsg(final String msg) {
        Runnable run = new Runnable() {
            public void run() {
                final EditText pinCodeTxt = new EditText(context);
                pinCodeTxt.setHint("PIN CODE");
                pinCodeTxt.setInputType(InputType.TYPE_CLASS_NUMBER);

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(msg)
                        .setTitle("Protected");
                builder.setView(pinCodeTxt);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            submitChallengeAnswer(new JSONObject().put("pin", pinCodeTxt.getText()));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        submitFailure(null);
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        };

        context.runOnUiThread(run);

    }


    public void alertError(final String msg) {
        Runnable run = new Runnable() {
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(msg)
                        .setTitle("Error");
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        };

        context.runOnUiThread(run);

    }




}
