package io.wochat.app.components;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.transition.TransitionManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import io.wochat.app.R.attr;
import io.wochat.app.R.color;
import io.wochat.app.R.dimen;
import io.wochat.app.R.id;
import io.wochat.app.R.layout;
import io.wochat.app.R.styleable;

public class BadgedTabLayout extends TabLayout {
	protected ColorStateList badgeBackgroundColors;
	protected ColorStateList badgeTextColors;
	protected float badgeTextSize = 0.0F;
	protected float tabTextSize = 0.0F;

	public BadgedTabLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.badgeBackgroundColors = ContextCompat.getColorStateList(context, color.badge_color);
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, styleable.BadgedTabLayout, 0, 0);
		this.badgeTextColors = this.getContextColors();

		try {
			if (a.hasValue(styleable.BadgedTabLayout_badgeBackgroundColor)) {
				this.badgeBackgroundColors = a.getColorStateList(styleable.BadgedTabLayout_badgeBackgroundColor);
			}

			if (a.hasValue(styleable.BadgedTabLayout_badgeTextColor)) {
				this.badgeTextColors = a.getColorStateList(styleable.BadgedTabLayout_badgeTextColor);
			}

			if (a.hasValue(styleable.BadgedTabLayout_badgeTextSize)) {
				this.badgeTextSize = a.getDimension(styleable.BadgedTabLayout_badgeTextSize, 0.0F);
			}

			if (a.hasValue(styleable.BadgedTabLayout_tabTextSize)) {
				this.tabTextSize = a.getDimension(styleable.BadgedTabLayout_tabTextSize, 0.0F);
			}

			int selected;
			if (a.hasValue(styleable.BadgedTabLayout_badgeSelectedBackgroundColor)) {
				selected = a.getColor(styleable.BadgedTabLayout_badgeSelectedBackgroundColor, 0);
				this.badgeBackgroundColors = createColorStateList(this.badgeBackgroundColors.getDefaultColor(), selected);
			}

			if (a.hasValue(styleable.BadgedTabLayout_badgeSelectedTextColor)) {
				selected = a.getColor(styleable.BadgedTabLayout_badgeSelectedTextColor, 0);
				this.badgeTextColors = createColorStateList(this.badgeTextColors.getDefaultColor(), selected);
			}
		} finally {
			a.recycle();
		}

	}

	public ColorStateList getBadgeBackgroundColors() {
		return this.badgeBackgroundColors;
	}

	public float getBadgeTextSize() {
		return this.badgeTextSize;
	}

	public void setBadgeTextSize(float badgeTextSize) {
		this.badgeTextSize = badgeTextSize;
	}

	public void setBadgeBackgroundColors(ColorStateList badgeBackgroundColors) {
		this.badgeBackgroundColors = badgeBackgroundColors;
		this.updateTabViews();
	}

	public ColorStateList getBadgeTextColors() {
		return this.badgeTextColors;
	}

	public void setBadgeTextColors(ColorStateList badgeTextColors) {
		this.badgeTextColors = badgeTextColors;
		this.updateTabViews();
	}

	public void addTab(@NonNull Tab tab, int position, boolean setSelected) {
		super.addTab(tab, position, setSelected);
		this.onTabAdded(tab);
	}

	public void updateTabViews() {
		for(int i = 0; i < this.getTabCount(); ++i) {
			Tab tab = this.getTabAt(i);
			if (tab != null) {
				tab.setCustomView(this.makeCustomView(tab, layout.badged_tab));
			}
		}

	}

	private View makeCustomView(Tab tab, int resId) {
		LayoutInflater inflater = LayoutInflater.from(this.getContext());
		View view = inflater.inflate(resId, (ViewGroup)null);
		this.makeCustomTitle(tab, view);
		this.makeCustomIcon(tab, view);
		this.makeBadge(view);
		return view;
	}

	private void makeCustomIcon(Tab tab, View view) {
		if (tab.getIcon() == null) {
			Log.e("BadgedTabLayout", "Tab icon is null. Not setting icon.");
		} else {
			ImageView icon = (ImageView)view.findViewById(id.imageview_tab_icon);
			DrawableCompat.setTintList(tab.getIcon(), this.getTabTextColors());
			icon.setImageDrawable(tab.getIcon());
			icon.setVisibility(View.VISIBLE);
		}
	}

	public void setIcon(int position, @DrawableRes int resourse) {
		Tab tab = this.getTabAt(position);
		if (tab != null) {
			tab.setIcon(resourse);
			this.makeCustomIcon(tab, tab.getCustomView());
		}
	}

	private void makeBadge(View view) {
		TextView badge = (TextView)view.findViewById(id.textview_tab_badge);
		badge.setTextColor(this.badgeTextColors);
		if (this.badgeTextSize != 0.0F) {
			badge.setTextSize(0, this.badgeTextSize);
		}

		DrawableCompat.setTintList(badge.getBackground(), this.badgeBackgroundColors);
	}

	private void makeCustomTitle(Tab tab, View view) {
		TextView title = (TextView)view.findViewById(id.textview_tab_title);
		title.setTypeface(null, Typeface.BOLD);
		title.setTextColor(this.getTabTextColors());
		if (this.tabTextSize != 0.0F) {
			title.setTextSize(this.tabTextSize);
		}

		if (!TextUtils.isEmpty(tab.getText())) {
			title.setText(tab.getText());
		} else {
			title.setVisibility(View.GONE);
		}

	}

	public void setBadgeText(int index, @Nullable String text) {
		Tab tab = this.getTabAt(index);
		if (tab != null && tab.getCustomView() != null) {
			TextView badge = (TextView)tab.getCustomView().findViewById(id.textview_tab_badge);
			TextView tabText = (TextView)tab.getCustomView().findViewById(id.textview_tab_title);
			if (text == null) {
				badge.setVisibility(View.GONE);
				tabText.setMaxWidth(2147483647);
			} else {
				int maxWidth = this.getResources().getDimensionPixelSize(dimen.tab_text_max_width);
				badge.setText(text);
				tabText.setMaxWidth(maxWidth);
				badge.setVisibility(View.VISIBLE);
			}

			TransitionManager.beginDelayedTransition((ViewGroup)tab.getCustomView());
		} else {
			Log.e("BadgedTabLayout", "Tab is null. Not setting custom view");
		}
	}

	public void onTabAdded(Tab tab) {
		if (tab == null) {
			Log.e("BadgedTabLayout", "Tab is null. Not setting custom view");
		} else {
			tab.setCustomView(this.makeCustomView(tab, layout.badged_tab));
		}
	}

	private ColorStateList getContextColors() {
		TypedValue typedValue = new TypedValue();
		TypedArray a = this.getContext().obtainStyledAttributes(typedValue.data, new int[]{attr.colorPrimary, attr.colorPrimaryDark});
		int primaryColor = a.getColor(0, 0);
		int primaryDarkColor = a.getColor(1, 0);
		a.recycle();
		return createColorStateList(primaryDarkColor, primaryColor);
	}

	private static ColorStateList createColorStateList(int defaultColor, int selectedColor) {
		int[][] states = new int[2][];
		int[] colors = new int[2];
		//int i = 0;
		states[0] = SELECTED_STATE_SET;
		colors[0] = selectedColor;
		//int i = i + 1;
		states[1] = EMPTY_STATE_SET;
		colors[1] = defaultColor;
		//++i;
		return new ColorStateList(states, colors);
	}
}
