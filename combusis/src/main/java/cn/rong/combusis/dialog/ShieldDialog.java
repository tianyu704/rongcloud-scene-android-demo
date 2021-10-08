package cn.rong.combusis.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.basis.widget.BottomDialog;
import com.kit.UIKit;
import com.kit.utils.KToast;
import com.kit.wapper.IResultBack;

import java.util.ArrayList;
import java.util.List;

import cn.rong.combusis.R;
import cn.rong.combusis.widget.FlowLayout;

/**
 * 屏蔽词
 */
public class ShieldDialog extends BottomDialog {
    private final static FlowLayout.LayoutParams TAG_LAYOUT_PARAMS =
            new FlowLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    static {
        TAG_LAYOUT_PARAMS.setMargins(20, 10, 20, 10);
    }

    private final String TAG_ADD = "@TAG_ADD";
    private List<String> stags = new ArrayList<>();
    private IResultBack<List<String>> resultBack;
    private int max_tag = 10;
    private ViewGroup flowLayout;
    private EditorDialog editorDialog;

    public ShieldDialog(Activity activity, int max) {
        super(activity);
        this.max_tag = max;
        setContentView(R.layout.dialog_shield, 50);
        stags.add(TAG_ADD);
        initView();
        setOnCancelListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (null != resultBack) {
                    stags.remove(0);
                    resultBack.onResult(stags);
                }
            }
        });
    }

    TextView title;
    String titlePre;

    public ShieldDialog setResultBack(IResultBack<List<String>> resultBack) {
        this.resultBack = resultBack;
        return this;
    }

    private void initView() {
        flowLayout = UIKit.getView(getContentView(), R.id.flow_tag);
        title = UIKit.getView(getContentView(), R.id.setting);
        titlePre = UIKit.getResources().getString(R.string.shield_setting);
        addView();
    }

    public ShieldDialog setTag(List<String> tags) {
        if (null != tags) {
            stags.clear();
            stags.add(TAG_ADD);
            stags.addAll(tags);
        }
        addView();
        return this;
    }

    private void addTag(String tag) {
        stags.add(tag);
        addView();
    }

    private void addView() {
        //往容器内添加TagView数据
        if (flowLayout != null) {
            flowLayout.removeAllViews();
        }
        int count = stags.size();
        //修改标题
        int tagSize = count - 1;
        String text = titlePre + (tagSize > 0 ? "(" + tagSize + "/" + max_tag + ")" : "");
        title.setText(text);
        for (int i = 0; i < count; i++) {
            final View tagView;
            String tag = stags.get(i);
            if (!TAG_ADD.equals(tag)) {// tag
                tagView = UIKit.inflate(R.layout.layout_tag);
                TextView tv = UIKit.getView(tagView, R.id.tv_tag);
                tv.setText(tag);
                UIKit.getView(tagView, R.id.iv_btn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        stags.remove(tag);
                        flowLayout.removeView(tagView);
                        //标题
                        int tagSize = stags.size() - 1;
                        String text = titlePre + (tagSize > 0 ? "(" + tagSize + "/" + max_tag + ")" : "");
                        title.setText(text);
                    }
                });
            } else {// add
                tagView = UIKit.inflate(R.layout.layout_tag_add);
                tagView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (stags.size() >= max_tag + 1) {
                            KToast.show("最多只能添加" + max_tag + "个屏蔽词");
                            return;
                        }
                        showAddTag();
                    }
                });
            }
            flowLayout.addView(tagView, TAG_LAYOUT_PARAMS);
        }
    }

    private void showAddTag() {
        if (null != editorDialog) {
            editorDialog.dismiss();
        }
        editorDialog = new EditorDialog(mActivity, new IResultBack<String>() {
            @Override
            public void onResult(String s) {
                editorDialog.dismiss();
                editorDialog = null;
                if (!TextUtils.isEmpty(s)) {
                    addTag(s);
                }
            }
        });
        editorDialog.show();
    }
}
