package com.example.seo.buddy;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;

/**
 * Created by Seo on 2017-02-08.
 */
public class RecycleUtils { // 메모리 재활용을 위한 class

    private RecycleUtils(){};

    public static void recursiveRecycle(View root) { // 메모리 재활용 메소드
        if (root == null)
            return;
        root.setBackground(null);
        if (root instanceof ViewGroup) {
            ViewGroup group = (ViewGroup)root;
            int count = group.getChildCount();
            for (int i = 0; i < count; i++) {
                recursiveRecycle(group.getChildAt(i));
            }

            if (!(root instanceof AdapterView)) {
                group.removeAllViews(); // 그룹들에 대해서 모든 뷰 삭제 -> 삭제함으로써 메모리 절약
            }

        }

        if (root instanceof ImageView) { // 뷰 그룹으로 받은 모든 이미지뷰에 대해서 null로 셋팅하여 메모리 줄인다
            ((ImageView)root).setImageDrawable(null);
        }

        root = null;

        return;
    }
}

