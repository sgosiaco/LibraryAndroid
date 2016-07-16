package comsgosiaco.github.library;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.regex.Pattern;

public class exportEmailDialogFragment extends DialogFragment {
    public interface exportEmailDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog, CharSequence email);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    exportEmailDialogListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (exportEmailDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement addBookDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.email, null))
                .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        TextView emailText = (TextView) getDialog().findViewById(R.id.email);
                        if(isValidEmail(emailText.getText()))
                            mListener.onDialogPositiveClick(exportEmailDialogFragment.this, emailText.getText());
                        else
                            showToast("Invalid Email!");
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogNegativeClick(exportEmailDialogFragment.this);
                    }
                })
                .setTitle("Email");
        // Create the AlertDialog object and return it
        return builder.create();
    }

    private boolean isValidEmail(CharSequence email)
    {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }

    public void showToast(CharSequence text)
    {
        Context context = getActivity().getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
}
