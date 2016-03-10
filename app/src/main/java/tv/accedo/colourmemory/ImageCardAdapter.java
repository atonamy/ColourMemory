package tv.accedo.colourmemory;

import android.graphics.Point;
import android.graphics.Shader;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmResults;
import tyrantgit.explosionfield.ExplosionField;

/**
 * Created by archie on 2/3/16.
 */
public class ImageCardAdapter extends BaseAdapter {

    private static final float CARD_PROPORTION = 1.25f;
    private static final int USUAL_DELAY = 1000;
    private static final int SHAKE_DURATION = 700;
    private static final int INTRO_DURATION = 1500;
    private AppCompatActivity mContext;
    private List<CardState> mCards;
    private ExplosionField mExplosionField;
    private int clicked;
    private long lastTimeClicked;
    private Map.Entry<FlipImageView3D, CardState> currentSelection;
    private int gameCardCount;
    private GameEvents gameEvents;
    private boolean[] gameReset;
    private Realm realmDb;

    public interface GameEvents {
        public void gameOver();
        public void result(final boolean matched);
    }

    // Constructor
    public ImageCardAdapter(AppCompatActivity c) {
        mContext = c;
        mCards = new RealmList<>();
        mExplosionField = ExplosionField.attach2Window(c);
        gameEvents = null;
        currentSelection = null;
        setDefaults();
        gameReset = new boolean[]{false, false};
        // Init DB
        final RealmConfiguration realmConfig = new RealmConfiguration.Builder(mContext).build();
        realmDb = Realm.getInstance(realmConfig);
    }

    public void setDefaults() {
        clicked = 0;
        if(currentSelection != null && currentSelection.getKey().isFlipped())
            currentSelection.getKey().flip();
        currentSelection = null;
        gameCardCount = 8;
        lastTimeClicked = 0;
    }

    public void setGameEvents(GameEvents events) {
        gameEvents = events;
    }

    public void populateCards(boolean reset) {

        RealmResults<CardState> result = realmDb.where(CardState.class).findAll();
        result.sort("position");
        mCards = realmDb.copyFromRealm(result);
        gameReset[0] = true;
        gameReset[1] = true;
        setDefaults();


        if(mCards.size() == 0 || reset) {
            mCards.clear();
            List<Integer> content = new ArrayList<>();
            content.add(R.drawable.colour1);
            content.add(R.drawable.colour2);
            content.add(R.drawable.colour3);
            content.add(R.drawable.colour4);
            content.add(R.drawable.colour5);
            content.add(R.drawable.colour6);
            content.add(R.drawable.colour7);
            content.add(R.drawable.colour8);
            content.add(R.drawable.colour1);
            content.add(R.drawable.colour2);
            content.add(R.drawable.colour3);
            content.add(R.drawable.colour4);
            content.add(R.drawable.colour5);
            content.add(R.drawable.colour6);
            content.add(R.drawable.colour7);
            content.add(R.drawable.colour8);
            Collections.shuffle(content);

            for (int i = 0; i < content.size(); i++) {
                CardState card = new CardState();
                card.setPosition(i);
                card.setGone(false);
                card.setContent(content.get(i));
                card.setOpen(false);
                mCards.add(card);
            }
            saveState();
        } else {
            int gone_cards_count = 0;
            for (CardState card : mCards) {
                if (card.getGone())
                    gone_cards_count++;
            }
            gameCardCount -= (gone_cards_count/2);
        }


    }

    public boolean gameEnded() {
        return (gameCardCount == 0);
    }

    public void saveState() {
        realmDb.beginTransaction();
        realmDb.copyToRealmOrUpdate(mCards);
        realmDb.commitTransaction();
    }

    @Override
    public int getCount() {
        return mCards.size()+4;
    }

    @Override
    public CardState getItem(int position) {
        return (position > 3) ? mCards.get(position - 4) : null;
    }

    @Override
    public long getItemId(int position) {
        return (position > 3) ? mCards.get(position - 4).getContent() : 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        RoundedImageView card;

        if (convertView == null)
            card = initCard(position);
        else
            card = (RoundedImageView) convertView;

        if(position < 4 || (position > 3 && getItem(position).getGone()))
            card.setVisibility(View.INVISIBLE);
        else
            card.setVisibility(View.VISIBLE);

        if(gameReset[0])
        {
            card.setImageResource(R.drawable.card_bg);
            YoYo.with(Techniques.FlipInX)
                    .duration(INTRO_DURATION)
                    .playOn(card);

            if(isLastPosition(position))
                gameReset[0] = false;
        }

        return card;
    }

    protected boolean isLastPosition(int position) {
        return ((position-4) == (mCards.size()-1));
    }

    protected RoundedImageView initCard(int position) {
        Display display = mContext.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        final RoundedImageView card = new RoundedImageView(mContext);
        float w = ((float)size.x/4f) - ((float)size.x/100f)*1.3f;
        float h = w * CARD_PROPORTION;

        card.setTag(null);
        card.setLayoutParams(new GridView.LayoutParams(Math.round(w), (position < 4) ? Math.round(h/1.3f) : Math.round(h)));
        card.setScaleType(ImageView.ScaleType.FIT_CENTER);
        card.setPadding(5, 5, 5, 5);
        card.setCornerRadius((float) 15);
        card.setBorderWidth((float) 2);
        card.mutateBackground(true);
        card.setTileModeX(Shader.TileMode.REPEAT);
        card.setTileModeY(Shader.TileMode.REPEAT);

        ViewTreeObserver vtoOutdoor = card.getViewTreeObserver();
        if(position > 3)
            vtoOutdoor.addOnGlobalLayoutListener(new CardObserver(card, position));

        return card;
    }

    private class CardObserver implements ViewTreeObserver.OnGlobalLayoutListener {

        private RoundedImageView card;
        private int position;
        public CardObserver(RoundedImageView card, int position) {
            this.card = card;
            this.position = position;
        }

        @Override
        public void onGlobalLayout() {
            if(!gameReset[1])
                return;
            card.setPivotX(card.getMeasuredWidth() / 2);
            card.setPivotY(card.getMeasuredHeight() / 2);
            final FlipImageView3D flip_3d = new FlipImageView3D(card, R.drawable.card_bg, getItem(position).getContent());
            card.setOnClickListener(new CardClickListener(flip_3d, getItem(position)));
            if (getItem(position).getOpen()) {
                currentSelection = new AbstractMap.SimpleEntry<>(flip_3d, getItem(position));
                flip_3d.flip();
                clicked++;
            }

            if(isLastPosition(position))
                gameReset[1] = false;
        }
    }

    private class CardClickListener implements View.OnClickListener {

        private FlipImageView3D flip3D;
        private CardState selectedCard;

        public CardClickListener(FlipImageView3D flip_3d, CardState selected_card) {
            this.flip3D = flip_3d;
            this.selectedCard = selected_card;
        }

        @Override
        public void onClick(final View v) {
            if(clicked > 1 || flip3D.isAnimating() || SystemClock.elapsedRealtime() - lastTimeClicked < 25 || flip3D.isFlipped() ||
                    (currentSelection != null && currentSelection.getValue().getPosition() == selectedCard.getPosition()) ||
                    selectedCard.getGone())
                return;
            clicked++;
            flip3D.setOnFlipListener(null);
            lastTimeClicked = SystemClock.elapsedRealtime();
            flip3D.flip();
            if(gameCardCount == 0)
                return;
            CardState previous_card = (currentSelection != null) ? currentSelection.getValue() : null;
            if(currentSelection == null) {
                currentSelection = new AbstractMap.SimpleEntry<>(flip3D, selectedCard);
                selectedCard.setOpen(true);
            }
            else if(previous_card != null && previous_card.getContent() == selectedCard.getContent() &&
                    previous_card.getPosition() != selectedCard.getPosition()) {
                final Map.Entry<FlipImageView3D, CardState> selection = currentSelection;
                currentSelection = null;
                selection.getValue().setOpen(false);
                selectedCard.setGone(true);
                selection.getValue().setGone(true);
                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mExplosionField.explode(selection.getKey().getView());
                        mExplosionField.explode(v);
                        selection.getKey().flip();
                        flip3D.flip();
                        clicked = 0;
                        gameCardCount--;
                        if (gameEvents != null) {
                            gameEvents.result(true);
                            if (gameCardCount == 0)
                                gameEvents.gameOver();
                        }
                    }
                }, USUAL_DELAY);
            } else {
                final Map.Entry<FlipImageView3D, CardState> selection = currentSelection;
                currentSelection = null;
                selection.getValue().setOpen(false);
                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        YoYo.with(Techniques.Swing)
                                .duration(SHAKE_DURATION)
                                .playOn(selection.getKey().getView());
                        YoYo.with(Techniques.Swing)
                                .duration(SHAKE_DURATION)
                                .playOn(v);
                        (new Handler()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                selection.getKey().flip();
                                flip3D.flip();
                                clicked = 0;
                                if (gameEvents != null)
                                    gameEvents.result(false);
                            }
                        }, USUAL_DELAY);
                    }
                }, flip3D.getAnimationDuration());

            }

        }
    }
}
