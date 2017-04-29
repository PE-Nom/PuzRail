package com.pentech.puzrail.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AdapterView;
import android.widget.ListView;
import android.view.View;

/**
 * Created by c0932 on 2017/03/16.
 */

public class MultiButtonListView extends ListView
        implements View.OnClickListener {
    /**
     * コンストラクタ
     * @param ctx
     */
    public MultiButtonListView(Context ctx){
        super(ctx);
    }

    /**
     * コンストラクタ
     * @param ctx
     * @param attrs
     */
    public MultiButtonListView(Context ctx , AttributeSet attrs){
        super(ctx , attrs);
    }

    /**
     * リスト内のボタンがクリックされたら呼ばれる
     */
    public void onClick(View view) {
        int pos = (Integer)view.getTag();
        this.performItemClick(view, pos, view.getId());//idって普通なに渡すの？
    }

}
