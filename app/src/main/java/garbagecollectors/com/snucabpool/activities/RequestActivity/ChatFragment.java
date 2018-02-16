package garbagecollectors.com.snucabpool.activities.RequestActivity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import garbagecollectors.com.snucabpool.R;
import garbagecollectors.com.snucabpool.activities.BaseActivity;
import garbagecollectors.com.snucabpool.adapters.UserAdapter;

public class ChatFragment extends Fragment
{
    RecyclerView recycle;

    public static UserAdapter recycleAdapter;

    public ChatFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        recycle = (RecyclerView) view.findViewById(R.id.recycle_users);

        recycleAdapter = new UserAdapter(BaseActivity.getChatList(), getContext());

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(),1);

        recycle.setLayoutManager(layoutManager);
        recycle.setItemAnimator( new DefaultItemAnimator());
        recycle.setAdapter(recycleAdapter);

        return view;
    }

    public static void refreshRecycler()
    {
        if(recycleAdapter != null)
            recycleAdapter.notifyDataSetChanged();
    }
}