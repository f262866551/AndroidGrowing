package cn.ljuns.cyclerotation;

import android.content.Context;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ljuns on 2016/11/21.
 */

public class CycleRotationView extends FrameLayout {

    private Context mContext;
    private ViewPager mViewPager;
    private LinearLayout mPointGroup;

    private List<ImageView> mList; // 资源集合
    private Handler mHandler;

    private int pointSize = 20; // 小圆点的大小，默认为20dp
    private int pointMargin = 20; // 与前面一个小圆点的距离，默认为20dp

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    private OnItemClickListener mListener;

    public CycleRotationView(Context context) {
        super(context);
    }

    public CycleRotationView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.mContext = context;
        mHandler = new Handler();
        mList = new ArrayList();
        initView(mContext);
    }

    /**
     * 初始化布局
     * @param mContext
     */
    private void initView(Context mContext) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.cycle_rotation_layout, this, true);
        mViewPager = (ViewPager) view.findViewById(R.id.viewPager);
        mPointGroup = (LinearLayout) view.findViewById(R.id.pointGroup);
    }

    /**
     * 设置图片的URL
     * @param urls：图片的URL
     */
    public void setUrls(String[] urls) {
        // 数据集合为空时隐藏当前布局
        if (urls == null || urls.length == 0) {
            this.setVisibility(GONE);
            return;
        }
        for (int i = 0; i < urls.length; i++) {
            // 创建 ImageView，并设置图片
            ImageView img = new ImageView(mContext);
            Glide.with(mContext)
                    .load(urls[i])
//                    .placeholder(R.mipmap.ic_launcher)
//                    .error("http://p1.so.qhmsg.com/t019beddcaef2c8592b.jpg")
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .crossFade()
                    .into(img);
            mList.add(img);

            makePoints(i);
        }

        setUpWithAdapter(); // 设置 Adapter
        timerTask(); // 定时任务
    }

    /**
     * 设置图片资源
     * @param images：图片的集合
     */
    public void setImages(int[] images) {
        // 数据集合为空时隐藏当前布局
        if (images == null || images.length == 0) {
            this.setVisibility(GONE);
            return;
        }
        for (int i = 0; i < images.length; i++) {
            // 创建 ImageView，并设置图片
            ImageView img = new ImageView(mContext);
            img.setImageResource(images[i]);
            mList.add(img);

            makePoints(i); // 创建小圆点
        }

        setUpWithAdapter(); // 设置 Adapter
        timerTask(); // 定时任务
    }

    /**
     * 创建小圆点
     * @param i
     */
    private void makePoints(int i) {
        // 创建小圆点，实质也是 ImageView
        ImageView point = new ImageView(mContext);
        point.setImageResource(R.drawable.shape_point_selector);

        // 小圆点布局参数
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(pointSize, pointSize);

        // 第2个起才设置左边距
        if (i > 0) {
            params.leftMargin = pointMargin;
            point.setSelected(false); // 默认选中第1个
        } else {
            point.setSelected(true); // 默认选中第1个
        }

        point.setLayoutParams(params); // 给小圆点设置参数
        mPointGroup.addView(point); // 把小圆点添加到容器
    }

    /**
     * 与 Adapter 关联
     */
    private void setUpWithAdapter() {
        mViewPager.setAdapter(new CycleAdapter());

        // ViewPager 的监听事件
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            int lastPosition;
            @Override
            public void onPageSelected(int position) {
                position = position % mList.size();
                // 设置当前圆点选中
                mPointGroup.getChildAt(position).setSelected(true);
                // 设置前一个圆点不选中
                mPointGroup.getChildAt(lastPosition).setSelected(false);
                lastPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
    }

    /**
     * 定时任务
     */
    private void timerTask() {

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 当前选中的 item
                int currentItem = mViewPager.getCurrentItem();
                // 判断是否是最后一个 item
                if (currentItem == mViewPager.getAdapter().getCount() - 1) {
                    mViewPager.setCurrentItem(1);
                } else {
                    mViewPager.setCurrentItem(currentItem + 1);
                }

                // 不断给自己发消息
                mHandler.postDelayed(this, 3000);
            }
        }, 3000);
    }

    /**
     * 适配器
     */
    class CycleAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return Integer.MAX_VALUE;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            position = position % mList.size();
            final View child = mList.get(position);
            if (child.getParent() != null) {
                container.removeView(child);
            }

            // 点击事件
            if (mListener != null) {
                final int finalPosition = position;
                child.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mListener.onItemClick(child, finalPosition);
                    }
                });
            }
            container.addView(mList.get(position));
            return mList.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
//            position = position % mList.size();
//            container.removeView((View) object);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

    /**
     * 设置点击事件
     * @param listener
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    /**
     * 设置小圆点的大小
     * 默认为20dp，如果需要修改，则在 setImages() 方法前调用
     * @param size：小圆点大小
     */
    public void setPointSize(int size) {
        this.pointSize = dp2px(size);
    }

    /**
     * 设置小圆点的左边距
     * 默认为20dp，如果需要修改，则在 setImages() 方法前调用
     * @param margin：距离
     */
    public void setPointMargin(int margin) {
        this.pointMargin = dp2px(margin);
    }

    /**
     * 将 dp 转换成 px
     * @param dp
     * @return
     */
    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
