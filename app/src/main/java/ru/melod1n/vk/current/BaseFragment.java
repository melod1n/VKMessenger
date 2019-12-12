package ru.melod1n.vk.current;

import androidx.fragment.app.Fragment;

public abstract class BaseFragment extends Fragment {

    @Override
    public void onResume() {
        super.onResume();
        ((BaseActivity) requireActivity()).checkNavigationAccess(this);
    }
}
