package com.fallntic.jotaayumouride.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ALL")
public class PageAdapter extends FragmentPagerAdapter {

    private final List<Fragment> listFragments = new ArrayList<>();
    private final List<String> titleListFragments = new ArrayList<>();
    private final List<String> listFragmentTags = new ArrayList<>();

    public PageAdapter(FragmentManager manager) {
        super(manager);
    }

    @Override
    public Fragment getItem(int position) {
        return listFragments.get(position);
    }

    @Override
    public int getCount() {
        return listFragments.size();
    }

    public void addFragment(Fragment fragment, String title, int position) {
        listFragments.add(position, fragment);
        titleListFragments.add(position, title);
        listFragmentTags.add(position, title);
    }

    public void removeFragment(Fragment fragment, int position) {
        listFragments.remove(position);
        titleListFragments.remove(position);
        listFragmentTags.remove(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titleListFragments.get(position);
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    public CharSequence getFragmentTag(int position) {
        return listFragmentTags.get(position);
    }
}