package ru.melod1n.vk.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
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
import ru.melod1n.vk.common.TaskManager;
import ru.melod1n.vk.current.BaseActivity;
import ru.melod1n.vk.current.BaseFragment;
import ru.melod1n.vk.database.CacheStorage;
import ru.melod1n.vk.database.MemoryCache;
import ru.melod1n.vk.fragment.FragmentConversations;
import ru.melod1n.vk.fragment.FragmentLogin;
import ru.melod1n.vk.fragment.FragmentSettings;
import ru.melod1n.vk.service.LongPollService;
import ru.melod1n.vk.util.ViewUtil;
import ru.melod1n.vk.widget.CircleImageView;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.drawerLayout)
    DrawerLayout drawerLayout;

    @BindView(R.id.navigationView)
    NavigationView navigationView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private DrawerArrowDrawable toggleDrawable;

    private final FragmentConversations fragmentConversations = new FragmentConversations();
    private final FragmentSettings fragmentSettings = new FragmentSettings();
    private final FragmentLogin fragmentLogin = new FragmentLogin();

    private Fragment selectedFragment;
    private int selectedId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        prepareToolbar();
        prepareNavigationView();
        prepareDrawerToggle();
        checkExtraData();
        checkLogin(savedInstanceState);
    }

    @Override
    public void setTitle(int titleId) {
        setTitle(getString(titleId));
    }

    @Override
    public void setTitle(CharSequence title) {
        TextView textView = ViewUtil.getToolbarTitleTextView(toolbar);
        if (textView != null) {
            SpannableString string = new SpannableString(title);
            string.setSpan(new AbsoluteSizeSpan(24, true), 0, string.length(), 0);
            textView.setText(string);
        } else {
            super.setTitle(title);
        }
    }

    private void prepareDrawerToggle() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name);
        toggleDrawable = new DrawerArrowDrawable(this);
        toggleDrawable.setColor(AppGlobal.colorAccent);

        toggle.setDrawerArrowDrawable(toggleDrawable);

        drawerLayout.addDrawerListener(toggle);

        toggle.setDrawerSlideAnimationEnabled(false);
        toggle.syncState();
    }

    private void prepareToolbar() {
        setSupportActionBar(toolbar);
        Typeface font = ResourcesCompat.getFont(this, R.font.tt_commons_medium);

        if (font == null) return;

        ViewUtil.changeToolbarTitleFont(toolbar, font);
    }

    private void prepareNavigationView() {
        navigationView.setNavigationItemSelectedListener(this);
        prepareNavigationHeader(null);
    }

    private void prepareNavigationHeader(VKUser user) {
        View headerView = navigationView.getHeaderView(0);

        VKUser profile = user == null ? MemoryCache.getUser(UserConfig.getUserId()) : user;

        if (profile == null) return;

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

    private void checkLogin(Bundle savedInstanceState) {
        if (UserConfig.isLoggedIn()) {
            loadProfileInfo();
            startLongPoll();

            if (savedInstanceState == null) {
                selectedId = R.id.navigationConversations;

                navigationView.setCheckedItem(selectedId);

                openConversationsScreen();
            }
        } else {
            openLoginScreen();
        }
    }

    private void startLongPoll() {
        startService(new Intent(this, LongPollService.class));
    }

    private void openLoginScreen() {
        replaceFragment(fragmentLogin);
    }

    private void openConversationsScreen() {
        replaceFragment(fragmentConversations);
    }

    private void openSettingsScreen() {
        replaceFragment(fragmentSettings);
    }

    private void replaceFragment(Fragment fragment) {
        if (selectedFragment != null && fragment.getClass().getSimpleName().equals(selectedFragment.getClass().getSimpleName())) {
            return;
        }

        selectedFragment = fragment;

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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navigationConversations:
                openConversationsScreen();
                break;
            case R.id.navigationSettings:
                openSettingsScreen();
                break;
            default:
                return false;
        }

        selectedId = item.getItemId();

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public void checkNavigationAccess(BaseFragment fragment) {
        boolean accessed = !(fragment instanceof FragmentLogin);

        toolbar.setNavigationIcon(accessed ? toggleDrawable : null);
        drawerLayout.setDrawerLockMode((accessed ? DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED), navigationView);
    }
}
