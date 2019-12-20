package ru.melod1n.vk.current;

import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public abstract class BaseFragment extends Fragment {

    private String title;
    private int titleRes = -1;

    public BaseFragment(int titleRes) {
        this.titleRes = titleRes;
    }

    public BaseFragment(String title) {
        this.title = title;
    }

    public BaseFragment() {
        this.title = "";
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (TextUtils.isEmpty(title) && titleRes > 0) {
            title = getString(titleRes);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((BaseActivity) requireActivity()).checkNavigationAccess(this);

        if (titleRes > 0) {
            requireActivity().setTitle(titleRes);
        } else {
            requireActivity().setTitle(title);
        }
    }

    public CharSequence getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        requireActivity().setTitle(title);
    }

    public void setTitleRes(int titleRes) {
        this.titleRes = titleRes;
        requireActivity().setTitle(titleRes);
    }

    public int getTitleRes() {
        return titleRes;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (!hidden) {
            onResume();
        }
    }

    public void onReopen(@Nullable Bundle bundle) {

    }
}
