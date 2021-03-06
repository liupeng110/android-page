package cn.yhq.page.sample;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.request.RequestCall;

import cn.yhq.page.core.IPageAdapter;
import cn.yhq.page.core.IPageDataParser;
import cn.yhq.page.core.IPageRequester;
import cn.yhq.page.sample.entity.AlbumInfo;
import cn.yhq.page.sample.entity.Tracks;
import cn.yhq.page.ui.PageConfig;
import cn.yhq.page.ui.PageActivity;

/**
 * Created by Yanghuiqiang on 2016/10/12.
 */

public class OkHttpPageActivity extends PageActivity<AlbumInfo, Tracks> {
    private ListView mListView;
    private AlbumPageAdapter mPageAdapter;

    @Override
    protected int getContentViewLayoutId() {
        return R.layout.activity_network_page;
    }

    @Override
    public void onViewCreated(Bundle savedInstanceState) {
        mListView = (ListView) this.findViewById(R.id.list_view);
        mPageAdapter = new AlbumPageAdapter(this);
        mListView.setAdapter(mPageAdapter);
    }

    @Override
    public View getPageView() {
        return mListView;
    }

    @Override
    public void onPageConfig(PageConfig pageConfig) {
        super.onPageConfig(pageConfig);
        pageConfig.setPageSize(5);
    }

    @Override
    public IPageAdapter<Tracks> getPageAdapter() {
        return mPageAdapter;
    }

    @Override
    public IPageRequester<AlbumInfo, Tracks> getPageRequester() {
        return new OkHttpPageRequester<AlbumInfo, Tracks>(this) {

            @Override
            public RequestCall getRequestCall(int pageSize, int pageIndex, Tracks data) {
                return OkHttpUtils.get()
                        .url("http://v5.pc.duomi.com/search-ajaxsearch-searchall")
                        .addParams("kw", "夜曲")
                        .addParams("pz", String.valueOf(pageSize))
                        .addParams("pi", String.valueOf(pageIndex))
                        .build();
            }
        };
    }

    @Override
    public IPageDataParser<AlbumInfo, Tracks> getPageDataParser() {
        return new PageDataParser();
    }
}
