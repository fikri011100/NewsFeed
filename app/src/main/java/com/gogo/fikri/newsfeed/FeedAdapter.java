package com.gogo.fikri.newsfeed;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Created by fikri on 1/7/17.
 */

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.ViewHolder>{
    private static final String TAG = "Adapter";
    private List<Model> mDataSet;
    private Context context;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final View View;
        private final TextView Name;
        private final ImageView imageView;

        public ViewHolder(View v) {
            super(v);

            View            = v;
            Name            = (TextView) v.findViewById(R.id.title);
            imageView            = (ImageView) v.findViewById(R.id.gbr);

        }
    }

    public FeedAdapter(Context con, List<Model> dataSet) {
        mDataSet = dataSet;
        context = con;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_feed, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        Log.d(TAG, "Element " + position + " set.");

        final Model model = mDataSet.get(position);

        viewHolder.Name.setText(model.getTitle());

        Glide.with(context)
                .load("http://192.168.10.114/realcom/api/assets/feeds_pict/"+model.getImage())
                .into(viewHolder.imageView);

        //======================        ONCLICK HANDLER         =======================
//        viewHolder.View.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(context, Detail.class);
//                intent.putExtra("id", model.getId());
//                intent.putExtra("name", model.getNama());
//                intent.putExtra("desc", model.getDesc());
//                intent.putExtra("qty", model.getQty());
//                context.startActivity(intent);
//            }
//        });

    }

    private int color(int colorRes) {
        return ContextCompat.getColor(context, colorRes);
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }
}
