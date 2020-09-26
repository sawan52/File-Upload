package com.example.file_upload;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private RecyclerView recyclerView;
    private Context context;
    private ArrayList<String> items = new ArrayList<String>();
    private ArrayList<String> urls = new ArrayList<String>();

    public MyAdapter(RecyclerView recyclerView, Context context, ArrayList<String> items, ArrayList<String> urls) {
        this.recyclerView = recyclerView;
        this.context = context;
        this.items = items;
        this.urls = urls;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view1 = LayoutInflater.from(context).inflate(R.layout.item, parent, false);
        return new ViewHolder(view1);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        // initialize the individual items
        holder.fileName.setText(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void update(String name, String url) {
        items.add(name);
        urls.add(url);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView fileName;

        public ViewHolder(View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.pdf_file_name);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = recyclerView.getChildLayoutPosition(view); // return the position of selected file
                    Intent intent = new Intent();

                    intent.setType(Intent.ACTION_VIEW);

                    intent.setDataAndType(Uri.parse(urls.get(position)), "application/pdf");

                    context.startActivity(intent);
                }
            });
        }
    }
}
