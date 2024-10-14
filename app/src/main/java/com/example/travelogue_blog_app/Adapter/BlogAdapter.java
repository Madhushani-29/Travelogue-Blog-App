package com.example.travelogue_blog_app.Adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelogue_blog_app.Model.BlogModel;
import com.example.travelogue_blog_app.R;

import java.util.ArrayList;

public class BlogAdapter extends RecyclerView.Adapter<BlogAdapter.BlogHolder> {
    private Context context;
    private ArrayList<BlogModel> blogList;

    public BlogAdapter(ArrayList<BlogModel> blogList, Context context) {
        this.blogList = blogList;
        this.context = context;
    }

    @NonNull
    @Override
    public BlogHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.blog_card, parent, false);
        return new BlogHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BlogHolder holder, int position) {
        BlogModel model=blogList.get(position);

        // get data
        String id=model.getId();
        String title=model.getTitle();
        String content=model.getContent();
        String location=model.getLocation();
        String image=model.getImage();

        // set data to view
        holder.title.setText(title);
        holder.content.setText(content);
        holder.location.setText(location);
        holder.image.setImageURI(Uri.parse(image));

        // handle on clicks on blog card
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        // handle on clicks on more button
        holder.moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    @Override
    public int getItemCount() {
        // return blog list item count
        return blogList.size();
    }

    class BlogHolder extends RecyclerView.ViewHolder{
        // views
        ImageView image;
        TextView title, content, location;
        ImageButton moreButton;

        public BlogHolder(@NonNull View itemView) {
            super(itemView);

            // init views
            image=itemView.findViewById(R.id.blogImage);
            title=itemView.findViewById(R.id.blogTitle);
            content=itemView.findViewById(R.id.blogContent);
            location=itemView.findViewById(R.id.blogLocation);
            moreButton=itemView.findViewById(R.id.moreButton);
        }
    }
}
