package com.labour.lar.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.labour.lar.BaseAdapter;
import com.labour.lar.Constants;
import com.labour.lar.R;
import com.labour.lar.module.Project;
import com.labour.lar.widget.RoundImageView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProjectAdapter extends BaseAdapter<Project, ProjectAdapter.ItemHolder> {

    public ProjectAdapter(Context mContext) {
        super(mContext);
    }

    @Override
    protected View createView(int position, View convertView, ViewGroup parent) {
        View view = this.mInflater.inflate(R.layout.frag_project_item, null);
        ItemHolder holder = new ItemHolder(view);
        view.setTag(holder);
        return view;
    }

    @Override
    protected void fillView(int position, Project item, ItemHolder holder) {
        //Glide.with(mContext).load(Constants.IMAGE_HTTP_BASE + item.getAvatar()).into(holder.photo_iv);
        holder.photo_iv.setImageResource(R.mipmap.test);
    }

    class ItemHolder {
        public ItemHolder(View view){
            ButterKnife.bind(this, view);
        }

        @BindView(R.id.photo_iv)
        RoundImageView photo_iv;
        @BindView(R.id.name_tv)
        TextView name_tv;
        @BindView(R.id.state_tv)
        TextView state_tv;

        @BindView(R.id.company_tv)
        TextView company_tv;
        @BindView(R.id.type_tv)
        TextView type_tv;
        @BindView(R.id.mianji_tv)
        TextView mianji_tv;
    }
}
