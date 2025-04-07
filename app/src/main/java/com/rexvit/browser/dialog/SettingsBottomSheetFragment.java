package com.rexvit.browser.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textview.MaterialTextView;
import com.rexvit.browser.Interface.UIController;
import com.rexvit.browser.R;
import com.rexvit.browser.constant.SettingsConstant;
import com.rexvit.browser.database.AdBlockDb;
import com.rexvit.browser.utils.AdBlock;
import com.rexvit.browser.utils.Preference;
import com.rexvit.browser.utils.Task;
import com.rexvit.browser.utils.Utils;

public class SettingsBottomSheetFragment extends BottomSheetDialogFragment {
    private static int searchEngineInt;
    public Activity c;
    public AlertDialog d;
    SwitchCompat mAdBlock;
    TextView mAdsCount;
    SwitchCompat mCloseTabs;
    SwitchCompat mDesktopSwith;
    SwitchCompat mLoadImages;
    TextView mSetSearchenginetext;

    LinearLayout contact;
    private UIController mUiController;
    public static final String TAG = "ActionBottomDialog";

    MaterialTextView btnCancel;
    LinearLayout mLrPolicy;
    LinearLayout searchEngineClick;

    public static SettingsBottomSheetFragment newInstance() {
        return new SettingsBottomSheetFragment();
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        setStyle(STYLE_NORMAL, R.style.askBottomSheet);


    }

    ImageView ivClose;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        setStyle(STYLE_NORMAL, R.style.askBottomSheet);
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.bottom_settings, (ViewGroup) null);
        c = getActivity();
        mLrPolicy = inflate.findViewById(R.id.lrPolicy);
        ivClose = inflate.findViewById(R.id.ivClose);
        searchEngineClick = inflate.findViewById(R.id.searchEngineClick);
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mLrPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Task.getPolicy(c, c.getPackageName());
            }
        });
        btnCancel = inflate.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dismiss();
            }
        });
        contact = inflate.findViewById(R.id.contact);
        mAdBlock = inflate.findViewById(R.id.adBlock);
        mAdsCount = inflate.findViewById(R.id.adsCount);
        mCloseTabs = inflate.findViewById(R.id.closetabsSwitch);
        mDesktopSwith = inflate.findViewById(R.id.desktopSwith);
        mLoadImages = inflate.findViewById(R.id.loadImages);
        mSetSearchenginetext = inflate.findViewById(R.id.setSearchenginetext);


        searchEngineClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                fabSetting();
            }
        });
        contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                contact();
            }
        });
        this.mUiController = (UIController) this.c;
        adBlock(this.mAdBlock, getActivity());
        closeTabs(this.mCloseTabs, getActivity());
        loadImages(this.mLoadImages, getActivity());
        desktop(this.mDesktopSwith, getActivity());
        searchEngineText();
        long count = AdBlockDb.count(AdBlockDb.class, null, null);
        String str = count > 1 ? " ads blocked" : " ad blocked";
        String format = Utils.format(count);
        TextView textView = this.mAdsCount;
        textView.setText(format + str);
        return inflate;
    }

    public void contact() {
        dismiss();
        Task.Feedback(getActivity());
    }

    private static void closeTabs(final SwitchCompat switchCompat, final Context context) {
        if (Preference.closeTabs(context)) {
            switchCompat.setChecked(true);
        } else {
            switchCompat.setChecked(false);
        }
        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                if (compoundButton.isChecked()) {
                    Preference.savePreferences(SettingsConstant.CLOSE_TABS, true, context);
                } else {
                    Preference.savePreferences(SettingsConstant.CLOSE_TABS, false, context);
                }
            }
        });
    }

    private void adBlock(final SwitchCompat switchCompat, Context context) {
        if (Preference.adBlock(context)) {
            switchCompat.setChecked(true);
        } else {
            switchCompat.setChecked(false);
        }
        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                if (switchCompat.isChecked()) {
                    try {
                        AdBlock.mBlockAds = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    SettingsBottomSheetFragment.this.mUiController.reloadPage();
                    Preference.savePreferences(SettingsConstant.ADBLOCK, true, SettingsBottomSheetFragment.this.getActivity());
                    return;
                }
                try {
                    AdBlock.mBlockAds = false;
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                SettingsBottomSheetFragment.this.mUiController.reloadPage();
                Preference.savePreferences(SettingsConstant.ADBLOCK, false, SettingsBottomSheetFragment.this.getActivity());
            }
        });
    }

    private void loadImages(final SwitchCompat switchCompat, Context context) {
        if (Preference.datSaveMode(context)) {
            switchCompat.setChecked(true);
        } else {
            switchCompat.setChecked(false);
        }
        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                if (switchCompat.isChecked()) {
                    SettingsBottomSheetFragment.this.mUiController.imageOnSet();
                    SettingsBottomSheetFragment.this.mUiController.reloadPage();
                    Preference.savePreferences(SettingsConstant.SWITCH_IMAGES, true, SettingsBottomSheetFragment.this.getActivity());
                    return;
                }
                SettingsBottomSheetFragment.this.mUiController.imageOffSet();
                SettingsBottomSheetFragment.this.mUiController.reloadPage();
                Preference.savePreferences(SettingsConstant.SWITCH_IMAGES, false, SettingsBottomSheetFragment.this.getActivity());
            }
        });
    }

    private void desktop(final SwitchCompat switchCompat, Context context) {
        if (Preference.desktopMode(context)) {
            switchCompat.setChecked(true);
        } else {
            switchCompat.setChecked(false);
        }
        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                if (switchCompat.isChecked()) {
                    SettingsBottomSheetFragment.this.mUiController.desktopSet();
                    SettingsBottomSheetFragment.this.mUiController.reloadPage();
                    Preference.savePreferences(SettingsConstant.DESKTOP, true, SettingsBottomSheetFragment.this.getActivity());
                    return;
                }
                SettingsBottomSheetFragment.this.mUiController.phoneSet();
                SettingsBottomSheetFragment.this.mUiController.reloadPage();
                Preference.savePreferences(SettingsConstant.DESKTOP, false, SettingsBottomSheetFragment.this.getActivity());
            }
        });
    }


    public void searchEngineText() {
        if (Preference.one(this.c)) {
            this.mSetSearchenginetext.setText("Google");
        } else if (Preference.two(this.c)) {
            this.mSetSearchenginetext.setText("Yahoo");
        } else if (Preference.three(this.c)) {
            this.mSetSearchenginetext.setText("Bing");
        } else if (Preference.four(this.c)) {
            this.mSetSearchenginetext.setText("DuckDuckGo");
        } else if (Preference.five(this.c)) {
            this.mSetSearchenginetext.setText("Ask");
        } else if (Preference.six(this.c)) {
            this.mSetSearchenginetext.setText("Baidu");
        } else if (Preference.eight(this.c)) {
            this.mSetSearchenginetext.setText("Lukayn");
        }


    }

    public void fabSetting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.set_search_engine);
        builder.setSingleChoiceItems(R.array.fab_choice, searchEngineInt, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int unused = SettingsBottomSheetFragment.searchEngineInt = i;
            }
        }).setNegativeButton(R.string.cancel, (DialogInterface.OnClickListener) null).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (SettingsBottomSheetFragment.searchEngineInt == 0) {
                    SettingsBottomSheetFragment.this.mUiController.GOOGLE_ENGINE();
                    SettingsBottomSheetFragment.this.mSetSearchenginetext.setText("Google");
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_ONE, true, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_TWO, false, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_THREE, false, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_FOUR, false, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_FIVE, false, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_SIX, false, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_SEVEN, false, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_EIGHT, false, SettingsBottomSheetFragment.this.getActivity());
                } else if (SettingsBottomSheetFragment.this.searchEngineInt == 1) {
                    SettingsBottomSheetFragment.this.mUiController.YAHOO_ENGINE();
                    SettingsBottomSheetFragment.this.mSetSearchenginetext.setText("Yahoo");
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_TWO, true, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_EIGHT, false, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_ONE, false, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_THREE, false, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_FOUR, false, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_FIVE, false, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_SIX, false, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_SEVEN, false, SettingsBottomSheetFragment.this.getActivity());
                } else if (SettingsBottomSheetFragment.searchEngineInt == 2) {
                    SettingsBottomSheetFragment.this.mUiController.BING_ENGINE();
                    SettingsBottomSheetFragment.this.mSetSearchenginetext.setText("Bing");
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_THREE, true, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_EIGHT, false, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_ONE, false, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_TWO, false, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_FOUR, false, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_FIVE, false, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_SIX, false, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_SEVEN, false, SettingsBottomSheetFragment.this.getActivity());
                } else if (SettingsBottomSheetFragment.searchEngineInt == 3) {
                    SettingsBottomSheetFragment.this.mUiController.DUCKDUCKGO_ENGINE();
                    SettingsBottomSheetFragment.this.mSetSearchenginetext.setText("DuckDuckGo");
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_FOUR, true, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_EIGHT, false, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_ONE, false, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_TWO, false, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_THREE, false, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_FIVE, false, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_SIX, false, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_SEVEN, false, SettingsBottomSheetFragment.this.getActivity());
                } else if (SettingsBottomSheetFragment.searchEngineInt == 4) {
                    SettingsBottomSheetFragment.this.mUiController.ASK_ENGINE();
                    SettingsBottomSheetFragment.this.mSetSearchenginetext.setText("Ask");
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_FIVE, true, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_EIGHT, false, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_ONE, false, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_TWO, false, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_FOUR, false, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_THREE, false, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_SIX, false, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_SEVEN, false, SettingsBottomSheetFragment.this.getActivity());
                } else if (SettingsBottomSheetFragment.searchEngineInt == 5) {
                    SettingsBottomSheetFragment.this.mUiController.BAIDU_ENGINE();
                    SettingsBottomSheetFragment.this.mSetSearchenginetext.setText("Baidu");
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_SIX, true, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_EIGHT, false, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_ONE, false, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_TWO, false, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_FOUR, false, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_FIVE, false, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_THREE, false, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_SEVEN, false, SettingsBottomSheetFragment.this.getActivity());
                } else if (SettingsBottomSheetFragment.searchEngineInt == 6) {
                    SettingsBottomSheetFragment.this.mUiController.YANDEX_ENGINE();
                    SettingsBottomSheetFragment.this.mSetSearchenginetext.setText("Yandex");
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_SEVEN, true, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_EIGHT, false, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_ONE, false, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_TWO, false, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_FOUR, false, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_FIVE, false, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_SIX, false, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_THREE, false, SettingsBottomSheetFragment.this.getActivity());
                } else if (SettingsBottomSheetFragment.searchEngineInt == 7) {
                    SettingsBottomSheetFragment.this.mUiController.LUKAYN_ENGINE();
                    SettingsBottomSheetFragment.this.mSetSearchenginetext.setText("Lukayn");
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_EIGHT, true, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_SEVEN, false, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_ONE, false, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_TWO, false, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_FOUR, false, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_FIVE, false, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_SIX, false, SettingsBottomSheetFragment.this.getActivity());
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_THREE, false, SettingsBottomSheetFragment.this.getActivity());
                }
            }
        });
        builder.create().show();
    }


}
