package ru.melod1n.vk.current;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public abstract class BaseAdapter<T, VH extends BaseHolder> extends RecyclerView.Adapter<VH> {

    private Context context;
    private LayoutInflater inflater;

    private ArrayList<T> values;
    private ArrayList<T> cleanValues;

    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;

    public BaseAdapter(Context context, ArrayList<T> values) {
        this.context = context;
        this.values = values;

        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(position);
        updateListeners(holder.itemView, position);
    }

    private void updateListeners(View v, int position) {
        if (onItemClickListener != null) {
            v.setOnClickListener(view -> onItemClickListener.onItemClick(position));
        }

        if (onItemLongClickListener != null) {
            v.setOnLongClickListener(v1 -> {
                onItemLongClickListener.onItemLongClick(position);
                return onItemClickListener != null;
            });
        }
    }

    @Override
    public int getItemCount() {
        if (values == null) return -1;
        return values.size();
    }

    public T getItem(int position) {
        if (values == null) return null;
        return values.get(position);
    }

    public void add(T item) {
        getValues().add(item);
    }

    public void add(int position, T item) {
        getValues().add(position, item);
    }

    public void addAll(ArrayList<T> values) {
        getValues().addAll(values);
    }

    public void addAll(int position, ArrayList<T> values) {
        getValues().addAll(position, values);
    }

    public void remove(int position) {
        getValues().remove(position);
    }

    public void remove(T item) {
        getValues().remove(item);
    }

    public void removeAll(ArrayList<T> values) {
        getValues().removeAll(values);
    }

    public void changeItems(ArrayList<T> items) {
        this.values = items;
        notifyItemRangeChanged(0, getItemCount(), -1);
    }

    public void clear() {
        this.values.clear();
    }

    public void filter(String query) {
        if (values == null) return;
        String lowerQuery = query.toLowerCase();

        if (cleanValues == null) {
            cleanValues = new ArrayList<>(values);
        }

        values.clear();

        if (query.isEmpty()) {
            values.addAll(cleanValues);
        } else {
            for (T value : cleanValues) {
                if (onQueryItem(value, lowerQuery)) {
                    values.add(value);
                }
            }
        }

        notifyDataSetChanged();
    }

    public boolean onQueryItem(T item, String lowerQuery) {
        return false;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    public Context getContext() {
        return context;
    }

    public LayoutInflater getInflater() {
        return inflater;
    }

    public ArrayList<T> getValues() {
        return values;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }

    public abstract void destroy();
}
