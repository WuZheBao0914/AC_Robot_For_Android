package cn.edu.uestc.cssl.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.joanzapata.iconify.IconDrawable;

import cn.edu.uestc.ac_ui.icon.AcIcons;
import cn.edu.uestc.cssl.activities.R;
import cn.edu.uestc.cssl.adapter.RobotInfoAdapter;
import cn.edu.uestc.cssl.delegates.SimpleFragment;
import cn.edu.uestc.cssl.dialogs.AddEditRobotDialogFragment;
import cn.edu.uestc.cssl.dialogs.ConfirmDeleteDialogFragment;
import cn.edu.uestc.cssl.entity.RobotInfo;
import cn.edu.uestc.cssl.util.RobotStorage;

/*
 *@author xuyang
 *@createTime 2019/2/17 0:00
 *@description 机器人列表Fragment
 */
@SuppressWarnings("SpellCheckingInspection")
public class RobotListFragment extends SimpleFragment implements AddEditRobotDialogFragment.DialogListener,
        ConfirmDeleteDialogFragment.DialogListener {
    private static final String TAG = "RobotListFragment";
    //机器人列表
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;

    public static RobotListFragment newInstance() {

        Bundle args = new Bundle();

        RobotListFragment fragment = new RobotListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Object setLayout() {
        return R.layout.fragment_robot_list;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {
        setHasOptionsMenu(true);

        mRecyclerView = rootView.findViewById(R.id.robot_recycler_view);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getControlApp());
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // 机器人列表适配器
        mAdapter = new RobotInfoAdapter(getControlApp(), RobotStorage.getRobots());

        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_add_robot, menu);
        menu.findItem(R.id.action_add_robot).setIcon(
                new IconDrawable(getContext(), AcIcons.icon_add)
                        .color(Color.BLACK)
                        .actionBarSize()
        );
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onAddEditDialogPositiveClick(RobotInfo info, int position) {
        if (position >= 0 && position < RobotStorage.getRobots().size()) {
            updateRobot(position, info);
        } else {
            addRobot(info);
        }
    }

    @Override
    public void onAddEditDialogNegativeClick(DialogFragment dialog) {

    }

    @Override
    public void onConfirmDeleteDialogPositiveClick(int position, String name) {
        if (position >= 0 && position < RobotStorage.getRobots().size()) {
            removeRobot(position);
        }
    }

    @Override
    public void onConfirmDeleteDialogNegativeClick() {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_robot:

                mRecyclerView.setVisibility(View.VISIBLE);

                RobotInfo.resolveRobotCount(RobotStorage.getRobots());

                AddEditRobotDialogFragment addRobotDialogFragment = new AddEditRobotDialogFragment();
                addRobotDialogFragment.setArguments(null);
                addRobotDialogFragment.show(getFragmentManager(), "addrobotdialog");

                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Adds a new RobotInfo.
     *
     * @param info The new RobotInfo
     */
    public void addRobot(RobotInfo info) {
        RobotStorage.add(getActivity(), info);

        mAdapter.notifyItemInserted(RobotStorage.getRobots().size() - 1);
    }

    /**
     * Updates the RobotInfo at the specified position.
     *
     * @param position     The position of the RobotInfo to update
     * @param newRobotInfo The updated RobotInfo
     */
    public void updateRobot(int position, RobotInfo newRobotInfo) {

        Log.d(TAG, "updateRobot at position " + position + ": " + newRobotInfo);

        RobotStorage.update(getActivity(), newRobotInfo);
        mAdapter.notifyItemChanged(position);
    }

    /**
     * Removes the RobotInfo at the specified position.
     *
     * @param position The position of the RobotInfo to remove
     */
    public void removeRobot(int position) {
        RobotInfo removed = RobotStorage.remove(getActivity(), position);

        if (removed != null) {
            mAdapter.notifyItemRemoved(position);
        }

        if (RobotStorage.getRobots().size() == 0) {
            mAdapter.notifyDataSetChanged();
        }
    }
}
