package sample.com.pincodeandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.WindowManager;
import android.widget.EditText;

import com.worklight.wlclient.api.WLAuthorizationManager;
import com.worklight.wlclient.api.challengehandler.WLChallengeHandler;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by liorbu on 04/02/16.
 */
public class PinCodeChallengeHandler extends WLChallengeHandler{

    Activity context;
    private static PinCodeChallengeHandler _this;


    public PinCodeChallengeHandler(String realm, Activity context) {
        super(realm);
        this.context = context;
        _this = this;
    }

    @Override
    public void handleSuccess(JSONObject jsonObject) {
        Log.d("Success", jsonObject.toString());
    }

    @Override
    public void handleFailure(JSONObject jsonObject) {
        Log.d("Failure", jsonObject.toString());
    }


    @Override
    public void handleChallenge(JSONObject jsonObject) {
        Log.d("Handle", jsonObject.toString());
        alertMsg("This data requires a PIN code.");


    }

    public void alertMsg(final String msg) {
        Runnable run = new Runnable() {
            public void run() {
                final EditText pinCode = new EditText(context);
                pinCode.setHint("PIN CODE");
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(msg)
                        .setTitle("Protected");
                builder.setView(pinCode);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String json = "{ \"pin\" : 1234 }";
                        JSONObject pc = null;
                        try {
                            pc = new JSONObject(json);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        _this.submitChallengeAnswer(pc);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        };

        context.runOnUiThread(run);

    }


}
