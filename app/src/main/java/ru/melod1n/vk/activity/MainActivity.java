package ru.melod1n.vk.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import ru.melod1n.vk.R;
import ru.melod1n.vk.api.UserConfig;
import ru.melod1n.vk.fragment.FragmentConversations;
import ru.melod1n.vk.fragment.FragmentLogin;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkExtraData();

        checkLogin();
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
