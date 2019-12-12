package ru.melod1n.vk.current;

import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {

    public abstract void checkNavigationAccess(BaseFragment fragment);

}
