package io.github.gsantner.memetastic.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.gsantner.memetastic.App;
import io.github.gsantner.memetastic.R;
import io.github.gsantner.memetastic.activity.MainActivity;
import io.github.gsantner.memetastic.activity.MemeCreateActivity;
import io.github.gsantner.memetastic.data.MemeData;
import io.github.gsantner.memetastic.service.ImageLoaderTask;
import io.github.gsantner.memetastic.util.ContextUtils;

/**
 * Adapter to show images in a Grid
 */
public class GridRecycleAdapter extends RecyclerView.Adapter<GridRecycleAdapter.ViewHolder> implements ImageLoaderTask.OnImageLoadedListener<GridRecycleAdapter.ViewHolder> {
    private List<MemeData.Image> _imageDataList;
    private int _shortAnimationDuration;
    private Activity _activity;
    private App _app;

    public GridRecycleAdapter(List<MemeData.Image> imageDataList, Activity activity) {
        _imageDataList = imageDataList;
        _shortAnimationDuration = -1;
        _activity = activity;
        _app = (App) (_activity.getApplication());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item__square_image, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // sets up the view of the item at the position in the grid
    @Override
    public void onBindViewHolder(final ViewHolder holder, int pos) {
        final int position = pos;
        holder.imageButtonFav.setVisibility(View.INVISIBLE);
        holder.imageView.setVisibility(View.INVISIBLE);
        ImageLoaderTask<ViewHolder> taskLoadImage = new ImageLoaderTask<>(this, _activity, true, holder);
        taskLoadImage.execute(_imageDataList.get(pos).fullPath);
        holder.imageView.setTag(_imageDataList.get(position));
        holder.imageButtonFav.setTag(_imageDataList.get(position));

        tintFavouriteImage(holder.imageButtonFav, _app.settings.isFavorite(_imageDataList.get(pos).fullPath.toString()));

        holder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                //onImageLongClicked(position, holder.imageButtonFav, _imageDataList);
                return true;
            }
        });
        holder.imageButtonFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MemeData.Image image = (MemeData.Image) v.getTag();
                if (image.isTemplate) {
                    toggleFavorite(holder);
                }
            }
        });

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MemeData.Image image = (MemeData.Image) v.getTag();

                if (image.isTemplate) {
                    Intent intent = new Intent(_activity, MemeCreateActivity.class);
                    intent.putExtra(MemeCreateActivity.EXTRA_IMAGE_PATH, image.fullPath.getAbsolutePath());
                    intent.putExtra(MemeCreateActivity.ASSET_IMAGE, false);
                    _activity.startActivityForResult(intent, MemeCreateActivity.RESULT_MEME_EDITING_FINISHED);
                } else {
                    if (_activity instanceof MainActivity) {
                        ((MainActivity) _activity).openImageViewActivityWithImage(image.fullPath.getAbsolutePath());
                    }
                }
            }
        });
    }

    // gets and returns the count of available items in the grid
    @Override
    public int getItemCount() {
        return _imageDataList.size();
    }

   /* public void onImageLongClicked(final int position, final ImageView iv, final MemeOriginInterface memeObj) {
        Context context = iv.getContext().getApplicationContext();
        String pic = _imageDataList.getFilepath(position);
        if (!pic.contains(context.getString(R.string.app_name))) {
            pic = pic.substring(pic.lastIndexOf("_") + 1);
            pic = pic.substring(0, pic.indexOf("."));
        }
        Snackbar snackbar = Snackbar.make(_activity.findViewById(android.R.id.content), pic, Snackbar.LENGTH_SHORT);
        if (memeObj instanceof MemeOriginAssets) {
            snackbar.setAction(R.string.main__mode__favs, new View.OnClickListener() {
                public void onClick(View v) {
                    toggleFavorite(position, iv, memeObj);
                }
            });
        }
        snackbar.show();
    }*/

    @Override
    public void onImageLoaded(Bitmap bitmap, ViewHolder holder) {
        MemeData.Image dataImage = (MemeData.Image) holder.imageView.getTag();
        Animation animation = AnimationUtils.loadAnimation(_activity, R.anim.fadeinfast);
        holder.imageView.startAnimation(animation);
        if (dataImage.isTemplate) {
            holder.imageButtonFav.startAnimation(animation);
            holder.imageButtonFav.setVisibility(View.VISIBLE);
        }
        holder.imageView.setImageBitmap(bitmap);
        holder.imageView.setVisibility(View.VISIBLE);
    }

    private void toggleFavorite(ViewHolder holder) {
        MemeData.Image dataImage = (MemeData.Image) holder.imageView.getTag();
        if (!dataImage.isTemplate) {
            return;
        }
        if (_app.settings.toggleFavorite(dataImage.fullPath.getAbsolutePath())) {
            tintFavouriteImage(holder.imageButtonFav, true);
        } else {
            tintFavouriteImage(holder.imageButtonFav, false);
        }
        int index = _imageDataList.indexOf(dataImage);
        if (index >= 0) {
            notifyItemChanged(index);
        }
    }

    private void tintFavouriteImage(ImageView iv, boolean isFav) {
        ContextUtils.setDrawableWithColorToImageView(iv,
                isFav ? R.drawable.ic_star_black_32dp : R.drawable.ic_star_border_black_32dp,
                isFav ? R.color.comic_yellow : R.color.comic_blue);
    }


    // contains the data view for the meme and the favorite button to access them
    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item__square_image__image)
        public ImageView imageView;

        @BindView(R.id.item__square_image__image_bottom_end)
        public ImageView imageButtonFav;

        // saves the instance of the data view of the meme and favorite button to access them later
        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            if (_shortAnimationDuration < 0)
                _shortAnimationDuration = imageView.getContext().getResources().getInteger(
                        android.R.integer.config_shortAnimTime);
        }
    }
}
