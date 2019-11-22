package ru.melod1n.vk.current;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public abstract class BaseHolder extends RecyclerView.ViewHolder {

    public BaseHolder(@NonNull View v) {
        super(v);
    }

    protected abstract void bind(int position);
}
