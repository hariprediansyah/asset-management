package com.rama.myasset;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.PopUpToBuilder;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AssetAdapter extends RecyclerView.Adapter<AssetAdapter.ViewHolder> {
    Context context;
    List<Asset> arrayList = new ArrayList<>();
    OnItemClickListener onItemClickListener;

    public  AssetAdapter(Context context){
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.asset_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.assetName.setText(arrayList.get(holder.getAdapterPosition()).getAsset_name());
        holder.description.setText(arrayList.get(holder.getAdapterPosition()).getDescription());
        holder.btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onCLick(arrayList.get(holder.getAdapterPosition()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public void clear() {
        arrayList.clear();
        notifyDataSetChanged();
    }

    public Asset getItem(int i){
        return arrayList.get(i);
    }

    public void sort(boolean asc) {
        arrayList.sort(new Comparator<Asset>() {
            @Override
            public int compare(Asset o1, Asset o2) {
                if (asc) {
                    return o1.getTime_input().compareTo(o2.getTime_input());
                }
                return o2.getTime_input().compareTo(o1.getTime_input());
            }
        });
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView assetName, description;
        ImageView btnEdit;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            assetName = itemView.findViewById(R.id.txtAssetName);
            description = itemView.findViewById(R.id.txtDescription);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void add(Asset asset) {
        arrayList.add(asset);
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onCLick(Asset asset);
    }
}
