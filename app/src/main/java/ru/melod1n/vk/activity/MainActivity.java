package ru.melod1n.vk.activity;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.melod1n.vk.R;
import ru.melod1n.vk.api.UserConfig;
import ru.melod1n.vk.api.VKApi;
import ru.melod1n.vk.api.model.VKUser;
import ru.melod1n.vk.common.AppGlobal;
import ru.melod1n.vk.concurrent.TaskManager;
import ru.melod1n.vk.database.CacheStorage;
import ru.melod1n.vk.database.MemoryCache;
import ru.melod1n.vk.fragment.FragmentConversations;
import ru.melod1n.vk.fragment.FragmentLogin;
import ru.melod1n.vk.widget.CircleImageView;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.drawerLayout)
    DrawerLayout drawerLayout;

    @BindView(R.id.navigationView)
    NavigationView navigationView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        prepareToolbar();
        prepareNavigationView();
        prepareDrawerToggle();
        checkExtraData();
        checkLogin();
    }

    private void prepareDrawerToggle() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name);
        toggle.getDrawerArrowDrawable().setColor(AppGlobal.colorAccent);
        drawerLayout.addDrawerListener(toggle);
        toggle.setDrawerSlideAnimationEnabled(false);
        toggle.syncState();
    }

    private void prepareToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.navigation_conversations);
    }

    private void prepareNavigationView() {
        navigationView.setCheckedItem(R.id.navigationConversations);
        prepareNavigationHeader(null);
    }

    private void prepareNavigationHeader(VKUser user) {
        View headerView = navigationView.getHeaderView(0);

        VKUser profile = user == null ? MemoryCache.getUser(UserConfig.getUserId()) : user;

        TextView profileName = headerView.findViewById(R.id.profileName);

        profileName.setText(profile.toString());

        TextView profileStatus = headerView.findViewById(R.id.profileStatus);

        String statusText = TextUtils.isEmpty(profile.getStatus()) ? "@id" + profile.getId() : profile.getStatus();
        profileStatus.setText(statusText);

        CircleImageView profileAvatar = headerView.findViewById(R.id.profileAvatar);

        if (!TextUtils.isEmpty(profile.getPhoto200())) {
            Picasso.get().load(profile.getPhoto200()).into(profileAvatar);
        } else {
            profileAvatar.setImageDrawable(new ColorDrawable(AppGlobal.colorAccent));
        }
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
            loadProfileInfo();
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

    private void loadProfileInfo() {
        TaskManager.execute(() -> VKApi.users().get().fields(VKUser.DEFAULT_FIELDS).execute(VKUser.class, new VKApi.OnResponseListener<VKUser>() {
            @Override
            public void onSuccess(ArrayList<VKUser> models) {
                VKUser profileUser = models.get(0);

                CacheStorage.insertUsers(profileUser.asList());
                prepareNavigationHeader(profileUser);
            }

            @Override
            public void onError(Exception e) {
                VKUser cachedUser = MemoryCache.getUser(UserConfig.getUserId());
                if (cachedUser != null) {
                    prepareNavigationHeader(cachedUser);
                } else {
                    Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }
        }));
    }

}
