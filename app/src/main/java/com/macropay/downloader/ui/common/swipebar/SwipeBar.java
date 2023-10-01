package com.macropay.downloader.ui.common.swipebar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

//import com.ebanx.swipebtn.OnActiveListener;
//import com.ebanx.swipebtn.OnStateChangeListener;


import com.macropay.downloader.R;

import com.macropay.data.logs.ErrorMgr;
import com.macropay.data.logs.Log;
//otra opcion es:
//https://androidhiro.com/source/android/example/swipe-button/5385
//https://github.com/Babitababy/SlidingToggleButton
//https://www.geeksforgeeks.org/sliding-toggle-button-in-android/
public class SwipeBar  extends RelativeLayout {
    private static final int ENABLED = 0;
    private static final int DISABLED = 1;

    private ImageView swipeButtonInner;
    private float initialX;
    private boolean active;
    private TextView centerText;
    private ViewGroup background;

    private Drawable disabledDrawable;
    private Drawable enabledDrawable;

    private OnStateChangeListener onStateChangeListener;
    private OnActiveListener onActiveListener;

    private int collapsedWidth;
    private int collapsedHeight;

    private LinearLayout layer;
    private boolean trailEnabled = false;
    private boolean hasActivationState;


    // ...
    private final int shapeWidth = 100;
    private final int shapeHeight = 100;
    private final int textXOffset = 0;
    private final int textYOffset = 30;
    private Paint paintShape;
    int shapeColor = Color.BLUE;
    boolean displayShapeName = true;
String TAG = "SwipeBar";
    // ...
    public SwipeBar(Context context, AttributeSet attrs) {
        super(context, attrs);
       // Log.msg(TAG,"SwipeBar");
        init(context, attrs, -1, -1);

    }
    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        try{
            hasActivationState = true;

            background = new RelativeLayout(context);
            background.setVisibility(VISIBLE);
            RelativeLayout.LayoutParams layoutParamsView = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            layoutParamsView.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

            addView(background, layoutParamsView);
//TextView
            final TextView centerText = new TextView(context);
            this.centerText = centerText;
            centerText.setGravity(Gravity.CENTER);

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

            background.addView(centerText, layoutParams);

            final ImageView swipeButton = new ImageView(context);
            this.swipeButtonInner = swipeButton;

            if (attrs != null && defStyleAttr == -1 && defStyleRes == -1) {

                TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SwipeBar, defStyleAttr, defStyleRes);

                collapsedWidth = (int) typedArray.getDimension(R.styleable.SwipeBar_button_image_width, ViewGroup.LayoutParams.WRAP_CONTENT);
                collapsedHeight = (int) typedArray.getDimension(R.styleable.SwipeBar_button_image_height, ViewGroup.LayoutParams.WRAP_CONTENT);
                trailEnabled = typedArray.getBoolean(R.styleable.SwipeBar_button_trail_enabled, false);
                Drawable trailingDrawable = typedArray.getDrawable(R.styleable.SwipeBar_button_trail_drawable);
                Drawable backgroundDrawable = typedArray.getDrawable(R.styleable.SwipeBar_inner_text_background);

                if (backgroundDrawable != null) {
                    background.setBackground(backgroundDrawable);
                } else {
                    background.setBackground(ContextCompat.getDrawable(context, R.drawable.shape_rounded));
                }
/*                Log.msg(TAG,"init -3- trailEnabled: "+trailEnabled);
                if (trailEnabled) {
                    Log.msg(TAG,"-- trailEnabled --");
                    layer = new LinearLayout(context);

                    if (trailingDrawable != null) {
                        layer.setBackground(trailingDrawable);
                    } else {
                        layer.setBackground(typedArray.getDrawable(R.styleable.SwipeButton_button_background));
                    }

                    layer.setGravity(Gravity.START);
                    layer.setVisibility(View.GONE);
                    background.addView(layer, layoutParamsView);
                }*/

                centerText.setText(typedArray.getText(R.styleable.SwipeBar_inner_text));
                centerText.setTextColor(typedArray.getColor(R.styleable.SwipeBar_inner_text_color, Color.WHITE));

                float textSize = DimentionUtils.converPixelsToSp(typedArray.getDimension(R.styleable.SwipeBar_inner_text_size, 0), context);

                if (textSize == 0)
                    textSize= 12;

                centerText.setTextSize(textSize);

                disabledDrawable = typedArray.getDrawable(R.styleable.SwipeBar_button_image_disabled);
                enabledDrawable = typedArray.getDrawable(R.styleable.SwipeBar_button_image_enabled);
                float innerTextLeftPadding = typedArray.getDimension(R.styleable.SwipeBar_inner_text_left_padding, 0);
                float innerTextTopPadding = typedArray.getDimension(R.styleable.SwipeBar_inner_text_top_padding, 0);
                float innerTextRightPadding = typedArray.getDimension(R.styleable.SwipeBar_inner_text_right_padding, 0);
                float innerTextBottomPadding = typedArray.getDimension(R.styleable.SwipeBar_inner_text_bottom_padding, 0);

                int initialState = typedArray.getInt(R.styleable.SwipeBar_initial_stated, DISABLED);
              //  Log.msg(TAG,"init -7- initialState: "+initialState);
                if (initialState == ENABLED) {
                   // Log.msg(TAG,"ENABLED");
                    RelativeLayout.LayoutParams layoutParamsButton = new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);

                    layoutParamsButton.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
                    layoutParamsButton.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);

                    swipeButtonInner.setImageDrawable(enabledDrawable);

                    addView(swipeButtonInner, layoutParamsButton);


                    active = true;
                } else {
                    //  Log.msg(TAG,"DISABLE");
                    RelativeLayout.LayoutParams layoutParamsButton = new RelativeLayout.LayoutParams(collapsedWidth, collapsedHeight);

                    layoutParamsButton.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
                    layoutParamsButton.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);

                    swipeButtonInner.setImageDrawable(disabledDrawable);

                    addView(swipeButtonInner, layoutParamsButton);

                    active = false;
                }


                centerText.setPadding((int) innerTextLeftPadding,
                        (int) innerTextTopPadding,
                        (int) innerTextRightPadding,
                        (int) innerTextBottomPadding);

                Drawable buttonBackground = typedArray.getDrawable(R.styleable.SwipeBar_button_background);

                if (buttonBackground != null) {
                    swipeButtonInner.setBackground(buttonBackground);
                } else {

                    swipeButtonInner.setBackground(ContextCompat.getDrawable(context, R.drawable.shape_button));
                }
                //TODO
                //   Log.msg(TAG, "[Init] ----------------");
                //   Log.msg(TAG, "[1]swipeButtonInner.getWidth(): "+swipeButtonInner.getWidth() +" active: "+active);
                float buttonLeftPadding = typedArray.getDimension(R.styleable.SwipeBar_button_left_padding, 0);
                float buttonTopPadding = typedArray.getDimension(R.styleable.SwipeBar_button_top_padding, 0);
                float buttonRightPadding = typedArray.getDimension(R.styleable.SwipeBar_button_right_padding, 0);
                float buttonBottomPadding = typedArray.getDimension(R.styleable.SwipeBar_button_bottom_padding, 0);
                //    Log.msg(TAG,"buttonLeftPadding: "+buttonLeftPadding);
                //   Log.msg(TAG,"buttonTopPadding: "+buttonTopPadding);
                //   Log.msg(TAG,"buttonRightPadding: "+buttonRightPadding);
                //   Log.msg(TAG,"buttonBottomPadding: "+buttonBottomPadding);
                //   Log.msg(TAG,"getWidth(): "+getWidth());
                swipeButtonInner.setPadding((int) buttonLeftPadding,
                        (int) buttonTopPadding,
                        (int) buttonRightPadding,
                        (int) buttonBottomPadding);
                //  Log.msg(TAG, "[2]swipeButtonInner.getWidth(): "+swipeButtonInner.getWidth() +" active: "+active);
                hasActivationState = typedArray.getBoolean(R.styleable.SwipeBar_has_activate_state, true);
                //   Log.msg(TAG,"hasActivationState: "+hasActivationState);
                typedArray.recycle();
                background.setVisibility(VISIBLE);
                //   Log.msg(TAG, "[3]swipeButtonInner.getWidth(): "+swipeButtonInner.getWidth() +" active: "+active);
            }
        } catch (Exception ex) {
            ErrorMgr.INSTANCE.guardar(TAG, "init", ex.getMessage());
        }
        //    Log.msg(TAG,"init -12- ");
        setOnTouchListener(getButtonTouchListener());
    }


    //onTouch: event: sec_touchscreen
    private OnTouchListener getButtonTouchListener() {
        //  Log.msg(TAG,"getButtonTouchListener");
        return new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
              //  Log.msg(TAG,"onTouch: event: "+event.getDevice().getName() );
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        return !TouchUtils.isTouchOutsideInitialPosition(event, swipeButtonInner);
                    case MotionEvent.ACTION_MOVE:
                        try{
                            if (initialX == 0) {
                                initialX = swipeButtonInner.getX();
                            }

                           /* if (event.getX() > swipeButtonInner.getWidth() / 2 &&
                                    event.getX() + swipeButtonInner.getWidth() / 2 < getWidth()) {
                                swipeButtonInner.setX(event.getX() - swipeButtonInner.getWidth() / 2);
                                centerText.setAlpha(1 - 1.3f * (swipeButtonInner.getX() + swipeButtonInner.getWidth()) / getWidth());
                                setTrailingEffect();
                            }*/
//TODO :
                            //centerText.setText("getX() : "+event.getX() );
                            //+ " swipeButtonInner.getWidth(): "+swipeButtonInner.getWidth() +" getWidth(): "+getWidth()
                            //       Log.msg(TAG,"event.getX(): "+event.getX() );
                            int mediaButtonSize = swipeButtonInner.getWidth() / 2;
                            float curPoxX= event.getX();

                            //Si se movio mas alla del ancho del swipebutton.
                            if (event.getX() + mediaButtonSize > getWidth() &&
                                    swipeButtonInner.getX() + mediaButtonSize < getWidth())
                            {
                                //             Log.msg(TAG,"[1] Se salio del ancho del swipe " +(getWidth()  - swipeButtonInner.getWidth()));
                               // swipeButtonInner.setX(getWidth() - swipeButtonInner.getWidth());
                                curPoxX = getWidth() - swipeButtonInner.getWidth();
                            //    centerText.setText("excedio del tañono..." );
                               // ToastDPC.showToast(getContext(),"salio del 100%");
                                ActionUP(0.8f);
                            }

                          if (event.getX() < mediaButtonSize) {
                              //                Log.msg(TAG,"[2] menor al ancho del SwipeBar -  event.getX() "+event.getX()  );
                                //swipeButtonInner.setX(0);
                              curPoxX= 0;
                            }
                            swipeButtonInner.setX(curPoxX);
                        } catch (Exception ex) {
                            ErrorMgr.INSTANCE.guardar(TAG, "onTouch[ACTION_MOVE]", ex.getMessage());
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        //      Log.msg(TAG,"[MotionEvent.ACTION_UP] active. "+active);
                        ActionUP(0.9f);
                        return true;
                    case MotionEvent.ACTION_CANCEL:
                        //         Log.msg(TAG,"[MotionEvent.ACTION_CANCEL] active. "+active);
                        //    centerText.setText("ACTION_CANCEL");
                        ActionUP(0.6f);
                        return true;
                    default:
                        Log.INSTANCE.msg(TAG,"[MotionEvent.default]. "+event.getAction());
                }

                return false;
            }
        };
    }
    private void ActionUP(float porcAvance){
        try{
            if (active) {
                collapseButton();
            } else {
                //Si avanzo mas del 90%.lo da por bueno..
                float deslizo = swipeButtonInner.getX() + swipeButtonInner.getWidth();
                if ( deslizo > getWidth() * porcAvance) {
                    //    Log.msg(TAG,"[MotionEvent.ACTION_UP] hasActivationState: "+hasActivationState);
                    if (hasActivationState) {
                        expandButton();
                    } else if (onActiveListener != null) {
                        onActiveListener.onActive();
                        moveButtonBack();
                    }
                } else {
                    moveButtonBack();
                }
            }
        } catch (Exception ex) {
            ErrorMgr.INSTANCE.guardar(TAG, "ActionUP", ex.getMessage());
        }
    }
    public void setOnStateChangeListener(OnStateChangeListener onStateChangeListener) {
        this.onStateChangeListener = onStateChangeListener;
    }

    public void setOnActiveListener(OnActiveListener onActiveListener) {
        this.onActiveListener = onActiveListener;
    }
    private void expandButton() {
        try{
            //   Log.msg(TAG,"expandButton");
            final ValueAnimator positionAnimator = ValueAnimator.ofFloat(swipeButtonInner.getX(), 0);
            positionAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float x = (Float) positionAnimator.getAnimatedValue();
                    swipeButtonInner.setX(x);
                }
            });

            final ValueAnimator widthAnimator = ValueAnimator.ofInt(swipeButtonInner.getWidth(), getWidth());

            widthAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    ViewGroup.LayoutParams params = swipeButtonInner.getLayoutParams();
                    params.width = (Integer) widthAnimator.getAnimatedValue();
                    swipeButtonInner.setLayoutParams(params);
                }
            });


            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
//TODO
                    active = true;
                    swipeButtonInner.setImageDrawable(enabledDrawable);

                    if (onStateChangeListener != null) {
                        onStateChangeListener.onStateChange(active);
                    }

                    if (onActiveListener != null) {
                        onActiveListener.onActive();
                    }
                }
            });

            animatorSet.playTogether(positionAnimator, widthAnimator);
            animatorSet.start();
        } catch (Exception ex) {
            ErrorMgr.INSTANCE.guardar(TAG, "expandButton", ex.getMessage());
        }
    }

    private void moveButtonBack() {
        try{
            //  Log.msg(TAG,"moveButtonBack");
            final ValueAnimator positionAnimator = ValueAnimator.ofFloat(swipeButtonInner.getX(), 0);
            positionAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            positionAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float x = (Float) positionAnimator.getAnimatedValue();
                    swipeButtonInner.setX(x);
                    setTrailingEffect();
                }
            });

            positionAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    if (layer!=null) {
                        layer.setVisibility(View.GONE);
                    }
                }
            });

            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(
                    centerText, "alpha", 1);

            positionAnimator.setDuration(200);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(objectAnimator, positionAnimator);
            animatorSet.start();
        } catch (Exception ex) {
            ErrorMgr.INSTANCE.guardar(TAG, "moveButtonBack", ex.getMessage());
        }
    }

    private void collapseButton() {
        //    Log.msg(TAG,"collapseButton");
        int finalWidth;
        try{
            if (collapsedWidth == ViewGroup.LayoutParams.WRAP_CONTENT) {
                finalWidth = swipeButtonInner.getHeight();
            } else {
                finalWidth = collapsedWidth;
            }

            final ValueAnimator widthAnimator = ValueAnimator.ofInt(swipeButtonInner.getWidth(), finalWidth);

            widthAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    ViewGroup.LayoutParams params = swipeButtonInner.getLayoutParams();
                    params.width = (Integer) widthAnimator.getAnimatedValue();
                    swipeButtonInner.setLayoutParams(params);
                    setTrailingEffect();
                }
            });

            widthAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    active = false;
                    swipeButtonInner.setImageDrawable(disabledDrawable);
                    if (onStateChangeListener != null) {
                        onStateChangeListener.onStateChange(active);
                    }
                    if (layer!=null) {
                        layer.setVisibility(View.GONE);
                    }
                }
            });

            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(
                    centerText, "alpha", 1);

            AnimatorSet animatorSet = new AnimatorSet();

            animatorSet.playTogether(objectAnimator, widthAnimator);
            animatorSet.start();
        } catch (Exception ex) {
            ErrorMgr.INSTANCE.guardar(TAG, "collapseButton", ex.getMessage());
        }
    }

    private void setTrailingEffect() {
        try{
            if (trailEnabled) {
                //       Log.msg(TAG,"setTrailingEffect - trailEnabled: "+trailEnabled);
                layer.setVisibility(View.VISIBLE);
                layer.setLayoutParams(new RelativeLayout.LayoutParams(
                        (int) (swipeButtonInner.getX() + swipeButtonInner.getWidth() / 3), centerText.getHeight()));
            }
        } catch (Exception ex) {
            ErrorMgr.INSTANCE.guardar(TAG, "setTrailingEffect", ex.getMessage());
        }
    }
}
