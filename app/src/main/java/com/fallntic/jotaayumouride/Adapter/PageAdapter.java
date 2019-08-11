package com.fallntic.jotaayumouride.Adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class PageAdapter extends FragmentPagerAdapter {

    private final List<Fragment> listFragments = new ArrayList<>();
    private final List<String> titleListFragments = new ArrayList<>();

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
    }

    public void removeFragment(Fragment fragment, int position) {
        listFragments.remove(position);
        titleListFragments.remove(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titleListFragments.get(position);
    }
}