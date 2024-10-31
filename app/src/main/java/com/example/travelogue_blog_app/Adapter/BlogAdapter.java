package com.example.travelogue_blog_app.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelogue_blog_app.Database.BlogDBHelper;
import com.example.travelogue_blog_app.Model.BlogModel;
import com.example.travelogue_blog_app.R;
import com.example.travelogue_blog_app.View.AddUpdateBlogActivity;
import com.example.travelogue_blog_app.View.MainActivity;
import com.example.travelogue_blog_app.View.ViewBlogActivity;

import java.util.ArrayList;

public class BlogAdapter extends RecyclerView.Adapter<BlogAdapter.BlogHolder> {
    private Context context;
    private ArrayList<BlogModel> blogList;

    BlogDBHelper dbHelper;

    public BlogAdapter(Context context, ArrayList<BlogModel> blogList) {
        this.blogList = blogList;
        this.context = context;

        dbHelper=new BlogDBHelper(context);
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
                Intent intent=new Intent(context, ViewBlogActivity.class);
                intent.putExtra("BLOG_ID", id);
                context.startActivity(intent);
            }
        });

        // handle on clicks on more button
        holder.moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreDialog(
                        ""+id,
                        ""+title,
                        ""+content,
                        ""+location,
                        ""+image
                );
            }
        });
    }

    private void showMoreDialog(final String id, final String title, final String content, final String location, final String image) {
        // options to display in the menu
        String [] options= {"Update", "Delete"};

        // dialog
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        // add items to the dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // handle item clicks
                if (which==0){
                    // when update click
                    // start AddUpdateBolgActivity to update the existing record
                    Intent intent = new Intent(context, AddUpdateBlogActivity.class);
                    intent.putExtra("id", id);
                    intent.putExtra("title", title);
                    intent.putExtra("content", content);
                    intent.putExtra("location", location);
                    intent.putExtra("image", image);
                    // say this is a update operation
                    intent.putExtra("isEditMode", true);
                    context.startActivity(intent);
                }
                else if (which==1) {
                    // when delete click
                    dbHelper.deleteBlogById(id);
                    // refresh blogs by calling onResume
                    ((MainActivity) context) .onResume();
                }
            }
        });

        // show the menu
        builder.show();
    }

    @Override
    public int getItemCount() {
        // return blog list item count
        return blogList.size();
    }

    class BlogHolder extends RecyclerView.ViewHolder{
        // views
        private ImageView image;
        private TextView title, content, location;
        private ImageButton moreButton;

        public BlogHolder(@NonNull View itemView) {
            super(itemView);

            // init views
            image=itemView.findViewById(R.id.blogImage);
            title=itemView.findViewById(R.id.titleText);
            content=itemView.findViewById(R.id.contentText);
            location=itemView.findViewById(R.id.locationText);
            moreButton=itemView.findViewById(R.id.moreButton);
        }
    }
}
