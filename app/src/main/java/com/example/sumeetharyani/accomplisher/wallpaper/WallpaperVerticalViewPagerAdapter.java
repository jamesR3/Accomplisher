package com.example.sumeetharyani.accomplisher.wallpaper;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.sumeetharyani.accomplisher.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;

public class WallpaperVerticalViewPagerAdapter extends PagerAdapter {


    private final Context mContext;
    private final LayoutInflater mLayoutInflater;
    private ArrayList<String> uploads;

    public WallpaperVerticalViewPagerAdapter(Context context) {
        mContext = context;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        getUserUploads();

    }

    private void getUserUploads() {
        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference mDatabaseReference = mFirebaseDatabase.getReference();
        DatabaseReference ref1 = mDatabaseReference.child("usersUploads");
        ref1.addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (user != null) {
                    dataSnapshot = dataSnapshot.child(user.getUid());
                    if (dataSnapshot.exists()) {
                        uploads = new ArrayList<>();
                        for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                            UploadDetails uploadDetails = dsp.getValue(UploadDetails.class);
                            uploads.add(uploadDetails.url);

                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }

        });
    }

    @Override
    public int getCount() {
        return 35;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        View itemView = mLayoutInflater.inflate(R.layout.wallpaper_content, container, false);
        ImageView imageView = itemView.findViewById(R.id.imageView);
        Button wallpaperButton = itemView.findViewById(R.id.btn_wallpaper);
        final StorageReference pathReference = getPathReference(position);
        setWallpaper(wallpaperButton, pathReference);
        loadImage(imageView, pathReference);
        container.addView(itemView);
        return itemView;
    }

    private void loadImage(ImageView imageView, StorageReference pathReference) {
        Glide.with(mContext)
                .load(pathReference)
                .apply(new RequestOptions()
                        .placeholder(R.drawable.ic_img))
                .into(imageView);
    }

    private void setWallpaper(Button wallpaperButton, final StorageReference pathReference) {
        wallpaperButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Glide.with(mContext)
                        .asBitmap()
                        .load(pathReference)
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                try {
                                    WallpaperManager.getInstance(mContext).setBitmap(resource);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

            }
        });
    }

    private StorageReference getPathReference(int position) {
        //default wallpaper upto 20.
        if (position <= 19) {
            return FirebaseStorage.getInstance().getReferenceFromUrl("gs://accomplisher-7cdf5.appspot.com/IMG_" + (position + 1) + ".png");
        } else if (uploads != null && position - 20 < uploads.size()) {
            return FirebaseStorage.getInstance().getReferenceFromUrl(uploads.get(position - 20));
        }
        return null;
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }
}
