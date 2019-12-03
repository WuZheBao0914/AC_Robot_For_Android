package cn.edu.uestc.cssl.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;


import cn.edu.uestc.cssl.activities.MainActivity;
import cn.edu.uestc.cssl.activities.R;
import cn.edu.uestc.cssl.delegates.AcDelegate;

/**
 * Dialog for requesting confirmation before deleting a RobotInfo.
 *
 * Created by Michael Brunson on 1/23/16.
 */
@SuppressWarnings("deprecation")
public class ConfirmDeleteDialogFragment extends DialogFragment {

    /** Bundle key for the name of the Robot being deleted */
    public static final String NAME_KEY = "DELETE_ITEM_NAME_KEY";
    /** Bundle key for the position of the RobotItem being deleted */
    public static final String POSITION_KEY = "DELETE_ITEM_POSITION_KEY";

    private DialogListener mListener;
    private String mItemName;
    private int mPosition;

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        mItemName = args.getString(NAME_KEY, "");
        mPosition = args.getInt(POSITION_KEY, -1);
    }

    // Override the Fragment.onAttach() method to instantiate the DialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof MainActivity){
            AcDelegate fragment = ((MainActivity) activity).getFragments()[MainActivity.FIRST];
            // Instantiate the DialogListener so we can send events to the host
            mListener = (ConfirmDeleteDialogFragment.DialogListener) fragment;
        }else {
            throw new RuntimeException();
        }
    }

    /**
     * Creates the Dialog.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.delete)
                .setMessage("Delete: " + "'" + mItemName + "'" + "?")
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Notify the listener
                        mListener.onConfirmDeleteDialogPositiveClick(mPosition, mItemName);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Notify the listener
                        mListener.onConfirmDeleteDialogNegativeClick();
                        dialog.cancel();
                    }
                });

        return builder.create();
    }

    /**
     * Interface for Objects that need to be notified of the result of the user's action on the Dialog.
     */
    public interface DialogListener {
        void onConfirmDeleteDialogPositiveClick(int position, String name);
        void onConfirmDeleteDialogNegativeClick();
    }
}
