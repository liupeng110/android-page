package cn.yhq.page.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import cn.yhq.page.core.IPageDataIntercept;
import cn.yhq.page.core.OnPageListener;
import cn.yhq.page.core.OnPullToRefreshProvider;
import cn.yhq.page.core.PageEngine;
import cn.yhq.page.core.PageManager;

/**
 * 是对PageEngine的UI层级的封装了，主要封装一些接口给Activity以及Fragment提供了，此外，PageContext里面提供了一个分页的配置类PageConfig，用于一些分页的基本配置，比如分页大小、是否在初始的时候自动加载数据等等。
 * <p>
 * Created by Yanghuiqiang on 2016/10/11.
 */

public final class PageContext<T, I> {
    private Context mContext;
    private PageEngine<T, I> mPageEngine;
    private PageConfig mPageConfig = new PageConfig();
    private List<IPageDataIntercept<I>> mPageDataIntercepts = new ArrayList<>();
    private List<OnPageListener> mOnPageListeners = new ArrayList<>();

    private View mPageView;
    private IPageViewProvider mPageViewProvider;

    public PageContext(Context context) {
        this.mContext = context;
    }

    public void initPageContext(IPageContextProvider<T, I> pageContextProvider) {
        pageContextProvider.onPageConfig(mPageConfig);
        pageContextProvider.addPageDataIntercepts(mPageDataIntercepts);
        this.mPageView = pageContextProvider.getPageView();
        this.mPageViewProvider = pageContextProvider.getPageViewProvider();
        mOnPageListeners.add(new DefaultPageListener(pageContextProvider));
        OnPullToRefreshProvider onPullToRefreshProvider = pageContextProvider.getOnPullToRefreshProvider();
        if (onPullToRefreshProvider != null) {
            onPullToRefreshProvider.setPullLoadMoreEnable(mPageConfig.pullLoadMoreEnable);
            onPullToRefreshProvider.setPullRefreshEnable(mPageConfig.pullRefreshEnable);
        }
        mPageEngine = new PageEngine.Builder<T, I>(mContext)
                .setPageSize(mPageConfig.pageSize)
                .setDataAppendMode(mPageConfig.dataAppendMode)
                .setPageAdapter(pageContextProvider.getPageAdapter())
                .setPageParser(pageContextProvider.getPageDataParser())
                .setPageRequester(pageContextProvider.getPageRequester())
                .setOnPullToRefreshProvider(pageContextProvider.getOnPullToRefreshProvider())
                .setPageDataIntercept(mPageDataIntercepts)
                .setOnPageListeners(mOnPageListeners)
                .build();
    }

    public final void start(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            boolean success = restorePageDataState(savedInstanceState);
            if (!success) {
                this.initPageData();
            }
        } else {
            if (mPageConfig.autoInitPageData) {
                this.initPageData();
            }
        }
    }

    public final OnPullToRefreshProvider getDefaultPullToRefreshProvider() {
        return PullToRefreshContextFactory.getPullToRefreshProvider(this.mPageView);
    }

    public final static void registerPullToRefreshProvider(Class<? extends View> viewClass, Class<? extends PullToRefreshContext> pullToRefreshContextClass) {
        PullToRefreshContextFactory.register(viewClass, pullToRefreshContextClass);
    }

    public final IPageViewProvider getDefaultPageViewProvider() {
        return new PageViewProvider(this.mPageView);
    }

    public final IPageViewManager getDefaultPageViewManager() {
        IPageViewManager pageViewManager = new PageViewManager(this.mPageViewProvider);
        pageViewManager.setOnReRequestListener(new OnReRequestListener() {
            @Override
            public void onReRequest() {
                initPageData();
            }
        });
        return pageViewManager;
    }

    public final boolean savePageDataState(Bundle savedInstanceState) {
        return mPageEngine.saveState(savedInstanceState);
    }

    public final boolean restorePageDataState(Bundle savedInstanceState) {
        return mPageEngine.restoreState(savedInstanceState);
    }

    public final PageManager<T, I> getPageManager() {
        return mPageEngine.getPageManager();
    }

    public final void clearPageData() {
        mPageEngine.clearPageData();
    }

    public final void initPageData() {
        if (mPageConfig.clearPageDataBeforeRequest) {
            clearPageData();
        }

        mPageEngine.initPageData();
    }

    public final void refreshPageData() {
        mPageEngine.refreshPageData();
    }

    public final void onDestroy() {
        mPageEngine.cancel();
    }

    public final PageConfig getPageConfig() {
        return this.mPageConfig;
    }

    public final void addOnPageListener(OnPageListener onPageListener) {
        this.mOnPageListeners.add(onPageListener);
    }

}
