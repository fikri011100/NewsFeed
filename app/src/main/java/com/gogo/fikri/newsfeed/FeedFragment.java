package com.gogo.fikri.newsfeed;

/**
 * Created by fikri on 1/7/17.
 */import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter;
import jp.wasabeef.recyclerview.animators.SlideInDownAnimator;


public class FeedFragment  extends Fragment {

    private enum LayoutManagerType {GRID_LAYOUT_MANAGER,LINEAR_LAYOUT_MANAGER}
    private static final String TAG = "Fragment";
    private static final String KEY_LAYOUT_MANAGER = "layoutManager";
    private static final int SPAN_COUNT = 2;
    protected LayoutManagerType mCurrentLayoutManagerType;
    protected RecyclerView mRecyclerView;
    protected FeedAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    private RelativeLayout loading;
    private RelativeLayout errElement;
    private RelativeLayout empElement;
    SlideInBottomAnimationAdapter animAdapter;

    protected List<Model> mDataset = new ArrayList<Model>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_feed, container, false);
        rootView.setTag(TAG);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        mRecyclerView.setItemAnimator(new SlideInDownAnimator());
        mLayoutManager = new LinearLayoutManager(getActivity());
        mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
        if (savedInstanceState != null) {
            mCurrentLayoutManagerType = (LayoutManagerType) savedInstanceState.getSerializable(KEY_LAYOUT_MANAGER);
        }
        setRecyclerViewLayoutManager(mCurrentLayoutManagerType);
        mAdapter = new FeedAdapter(getContext(), mDataset);
        animAdapter = new SlideInBottomAnimationAdapter(mAdapter);
        animAdapter.setFirstOnly(true);
        animAdapter.setDuration(500);
        animAdapter.setInterpolator(new OvershootInterpolator(.5f));
        mRecyclerView.setAdapter(animAdapter);

        loading = (RelativeLayout) rootView.findViewById(R.id.loadingElement);
        errElement = (RelativeLayout) rootView.findViewById(R.id.errorElement);
        empElement = (RelativeLayout) rootView.findViewById(R.id.emptyElement);

        mSwipeRefreshLayout = (SwipeRefreshLayout)rootView.findViewById(R.id.SwipeRefresh);
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorAccent,
                R.color.colorPrimaryDark);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loading.setVisibility(View.VISIBLE);
                mDataset.clear();
                mAdapter.notifyDataSetChanged();
                getData();
            }
        });

        mDataset.clear();
        getData();

        return rootView;
    }

    public void setRecyclerViewLayoutManager(LayoutManagerType layoutManagerType) {
        int scrollPosition = 0;

        if (mRecyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) mRecyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();
        }

        switch (layoutManagerType) {
            case GRID_LAYOUT_MANAGER:
                mLayoutManager = new GridLayoutManager(getActivity(), SPAN_COUNT);
                mCurrentLayoutManagerType = LayoutManagerType.GRID_LAYOUT_MANAGER;
                break;
            case LINEAR_LAYOUT_MANAGER:
                mLayoutManager = new LinearLayoutManager(getActivity());
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
                break;
            default:
                mLayoutManager = new LinearLayoutManager(getActivity());
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
        }

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.scrollToPosition(scrollPosition);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putSerializable(KEY_LAYOUT_MANAGER, mCurrentLayoutManagerType);
        super.onSaveInstanceState(savedInstanceState);
    }

    public void getData() {
        StringRequest strReq = new StringRequest(Request.Method.POST, "http://192.168.10.114/realcom/api/feeds/get_feed_learn", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("Volley", "Response: " + response.toString());
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject data =  jsonArray.getJSONObject(i);

                        Model model = new Model();
                        model.setTitle(data.getString("feeds_subject"));
                        model.setImage(data.getString("feeds_pict"));
                        mDataset.add(model);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "json error", Toast.LENGTH_LONG).show();
                }

                if (mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
                mAdapter.notifyDataSetChanged();
                empElement.setVisibility(View.GONE);
                loading.setVisibility(View.GONE);
                errElement.setVisibility(View.GONE);

                if(mDataset.size() == 0){
                    empElement.setVisibility(View.VISIBLE);
                    loading.setVisibility(View.GONE);
                    errElement.setVisibility(View.GONE);
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("Volley", "Error: " + error.getMessage());
                Toast.makeText(getActivity(),"not connected", Toast.LENGTH_LONG).show();
                errElement.setVisibility(View.VISIBLE);
                loading.setVisibility(View.GONE);
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                return params;
            }

        };

        AppController.getInstance().addToRequestQueue(strReq, "get");
    }
}