package ru.melod1n.vk.activity;

import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.melod1n.vk.R;
import ru.melod1n.vk.api.UserConfig;
import ru.melod1n.vk.common.AppGlobal;
import ru.melod1n.vk.fragment.FragmentConversations;
import ru.melod1n.vk.fragment.FragmentLogin;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.bottomBar)
    BottomNavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        checkExtraData();
        checkLogin();
        prepareNavigationView();
    }

    private void prepareNavigationView() {
        navigationView.setItemRippleColor(ColorStateList.valueOf(AppGlobal.colorPrimary));
        navigationView.setSelectedItemId(R.id.navigationConversations);
    }

    private void checkExtraData() {
        if (getIntent().hasExtra("token")) {
            String token = getIntent().getStringExtra("token");
            int userId = getIntent().getIntExtra("user_id", -1);

            UserConfig.setToken(token);
            UserConfig.setUserId(userId);
            UserConfig.save();
        }
    }

    private void checkLogin() {
        if (UserConfig.isLoggedIn()) {
            openConversationsScreen();
        } else {
            openLoginScreen();
        }
    }

    private void openLoginScreen() {
        replaceFragment(new FragmentLogin());
    }

    private void openConversationsScreen() {
        replaceFragment(new FragmentConversations());
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment, fragment.getClass().getSimpleName()).commit();
    }

}
