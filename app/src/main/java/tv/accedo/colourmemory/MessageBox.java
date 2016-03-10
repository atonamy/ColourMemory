package tv.accedo.colourmemory;

import android.support.v7.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Handler;

public class MessageBox {

    public static void Show(Context context, String title, String message, Integer icon, final Runnable feedback){

        AlertDialog msgbox = new AlertDialog.Builder(context).create();
        msgbox.setCancelable(true); // This blocks the 'BACK' button
        msgbox.setMessage(message);
        msgbox.setTitle(title);
        if(icon != null)
            msgbox.setIcon(icon);
        msgbox.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                // TODO Auto-generated method stub
                if(feedback != null)
                    (new Handler()).post(feedback);
            }

        });
        msgbox.setButton(AlertDialog.BUTTON_POSITIVE, "OK",  new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        msgbox.show();

    }

    public static void Ask(Context context, String title, String message, Integer icon,
                           String button_ok, String button_cancel, final Runnable ok, final Runnable cancel){

        AlertDialog msgbox = new AlertDialog.Builder(context).create();
        msgbox.setCancelable(false); // This blocks the 'BACK' button
        msgbox.setMessage(message);
        msgbox.setTitle(title);
        if(icon != null)
            msgbox.setIcon(icon);

        msgbox.setButton(AlertDialog.BUTTON_NEGATIVE, button_cancel,  new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if(cancel != null)
                    (new Handler()).post(cancel);
                dialog.dismiss();
            }
        });

        msgbox.setButton(AlertDialog.BUTTON_POSITIVE, button_ok,  new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if(ok != null)
                    (new Handler()).post(ok);
                dialog.dismiss();
            }
        });

        msgbox.show();

    }

}

