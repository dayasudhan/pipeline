package com.kuruvatech.pipeline.Loader;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.kuruvatech.pipeline.Loader.render.LoadingDrawable;
import com.kuruvatech.pipeline.Loader.render.LoadingRenderer;
import com.kuruvatech.pipeline.Loader.render.LoadingRendererFactory;


public class LoadingView extends android.support.v7.widget.AppCompatImageView {
    private LoadingDrawable mLoadingDrawable;

    public LoadingView(Context context) {
        super(context);
    }

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        try {
            TypedArray ta = context.obtainStyledAttributes(attrs, khaanavali.customer.R.styleable.LoadingView);
            int loadingRendererId = ta.getInt(khaanavali.customer.R.styleable.LoadingView_loading_renderer, 0);
            LoadingRenderer loadingRenderer = LoadingRendererFactory.createLoadingRenderer(context, loadingRendererId);
            setLoadingRenderer(loadingRenderer);
            ta.recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setLoadingRenderer(LoadingRenderer loadingRenderer) {
        mLoadingDrawable = new LoadingDrawable(loadingRenderer);
        setImageDrawable(mLoadingDrawable);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAnimation();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimation();
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == View.VISIBLE) {
            startAnimation();
        } else {
            stopAnimation();
        }
    }

    private void startAnimation() {
        if (mLoadingDrawable != null) {
            mLoadingDrawable.start();
        }
    }

    private void stopAnimation() {
        if (mLoadingDrawable != null) {
            mLoadingDrawable.stop();
        }
    }
}
