package tv.accedo.colourmemory;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

/**
 * Created by archie on 2/3/16.
 */
public class FlipImageView3D {

    private ImageView flipView;
    private int backSide;
    private int frontSide;
    private boolean animating;
    private int animationDuration;
    private boolean flipped;
    private Runnable flipDone;

    public FlipImageView3D(ImageView flip_view, int back_side_drawable_id, int front_side_drawable_id) {
        flipView = flip_view;
        backSide = back_side_drawable_id;
        frontSide = front_side_drawable_id;
        animating = false;
        animationDuration = 700;
        flipDone = null;
        flipped = false;
    }

    public void setAnimationDuration(int duration) {
        animationDuration = duration;
    }
    public int getAnimationDuration() {
        return animationDuration;
    }

    public void setOnFlipListener(Runnable listener) {
        flipDone = listener;
    }

    public boolean isFlipped() {
        return flipped;
    }

    public ImageView getView() {
        return flipView;
    }

    public boolean isAnimating() { return animating; }

    public void flip() {
        if(animating)
            return;
        animating = true;
        ObjectAnimator animation = ObjectAnimator.ofFloat(flipView, "rotationY", 0.0f, 90f);
        setAnimation(animation, true,  new AccelerateInterpolator());
        animation.start();
        animation.addListener(flipFront);
    }

    protected void setAnimation(ObjectAnimator animation, boolean start, TimeInterpolator interpolator) {

        if(flipped && start)
            flipView.setImageDrawable(ContextCompat.getDrawable(flipView.getContext(), frontSide));
        else if(flipped && !start)
            flipView.setImageDrawable(ContextCompat.getDrawable(flipView.getContext(), backSide));
        else if(!flipped && start)
            flipView.setImageDrawable(ContextCompat.getDrawable(flipView.getContext(), backSide));
         else
            flipView.setImageDrawable(ContextCompat.getDrawable(flipView.getContext(), frontSide));
        animation.setDuration(animationDuration / 2);
        animation.setInterpolator(interpolator);
    }

    private Animator.AnimatorListener flipFront = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animator) {

        }

        @Override
        public void onAnimationEnd(Animator animator) {
            ObjectAnimator animation = ObjectAnimator.ofFloat(flipView, "rotationY", 90f, 0f);
            setAnimation(animation, false, new DecelerateInterpolator());
            animation.start();
            animation.addListener(flipBack);
        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    };

    private Animator.AnimatorListener flipBack = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animator) {

        }

        @Override
        public void onAnimationEnd(Animator animator) {
            animating = false;
            flipped = (flipped) ? false : true;
            if(flipDone != null)
                (new Handler()).post(flipDone);
        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    };


}
