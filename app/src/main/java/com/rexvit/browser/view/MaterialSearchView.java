package com.rexvit.browser.view;

import android.content.Context;
import android.os.Build;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.rexvit.browser.R;


public class MaterialSearchView extends FrameLayout {
    private final String TAG;
    int cX;
    int cY;
    private AppCompatImageView clearSearch;
    OnClickListener clearSearchListner;
    OnClickListener closeListerner;
    private AppCompatImageView closeSearch;
    private Context context;
    private EditText editText;
    OnFocusChangeListener focusChangeListener;
    private CharSequence mCurrentQuery;
    private OnQueryTextListener mOnQueryTextListener;
    private ConstraintLayout materialSearchContainer;
    View parent;
    private Float parentHeight;
    private int parentWidth;
    int radius;

    
    public interface OnQueryTextListener {
        boolean onQueryTextChange(String str);

        boolean onQueryTextSubmit(String str);
    }

    public MaterialSearchView(Context context) {
        super(context);
        this.TAG = "MaterialSearchView";
        this.closeListerner = new OnClickListener() {
            @Override 
            public void onClick(View view) {
                MaterialSearchView.this.closeSearch();
                Log.d(MaterialSearchView.this.TAG, "Search is closed");
            }
        };
        this.clearSearchListner = new OnClickListener() {
            @Override 
            public void onClick(View view) {
                MaterialSearchView.this.editText.setText("");
                MaterialSearchView.this.clearSearch.setVisibility(View.GONE);
            }
        };
        this.focusChangeListener = new OnFocusChangeListener() {
            @Override 
            public void onFocusChange(View view, boolean z) {
                if (z) {
                    MaterialSearchView materialSearchView = MaterialSearchView.this;
                    materialSearchView.showKeyboard(materialSearchView.editText);
                }
            }
        };
        this.context = context;
        init();
    }

    public MaterialSearchView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.TAG = "MaterialSearchView";
        this.closeListerner = new OnClickListener() {
            @Override 
            public void onClick(View view) {
                MaterialSearchView.this.closeSearch();
                Log.d(MaterialSearchView.this.TAG, "Search is closed");
            }
        };
        this.clearSearchListner = new OnClickListener() {
            @Override 
            public void onClick(View view) {
                MaterialSearchView.this.editText.setText("");
                MaterialSearchView.this.clearSearch.setVisibility(View.GONE);
            }
        };
        this.focusChangeListener = new OnFocusChangeListener() {
            @Override 
            public void onFocusChange(View view, boolean z) {
                if (z) {
                    MaterialSearchView materialSearchView = MaterialSearchView.this;
                    materialSearchView.showKeyboard(materialSearchView.editText);
                }
            }
        };
        this.context = context;
        init();
    }

    public MaterialSearchView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.TAG = "MaterialSearchView";
        this.closeListerner = new OnClickListener() {
            @Override 
            public void onClick(View view) {
                MaterialSearchView.this.closeSearch();
                Log.d(MaterialSearchView.this.TAG, "Search is closed");
            }
        };
        this.clearSearchListner = new OnClickListener() {
            @Override 
            public void onClick(View view) {
                MaterialSearchView.this.editText.setText("");
                MaterialSearchView.this.clearSearch.setVisibility(View.GONE);
            }
        };
        this.focusChangeListener = new OnFocusChangeListener() {
            @Override 
            public void onFocusChange(View view, boolean z) {
                if (z) {
                    MaterialSearchView materialSearchView = MaterialSearchView.this;
                    materialSearchView.showKeyboard(materialSearchView.editText);
                }
            }
        };
        this.context = context;
        init();
    }

    public MaterialSearchView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.TAG = "MaterialSearchView";
        this.closeListerner = new OnClickListener() {
            @Override 
            public void onClick(View view) {
                MaterialSearchView.this.closeSearch();
                Log.d(MaterialSearchView.this.TAG, "Search is closed");
            }
        };
        this.clearSearchListner = new OnClickListener() {
            @Override 
            public void onClick(View view) {
                MaterialSearchView.this.editText.setText("");
                MaterialSearchView.this.clearSearch.setVisibility(View.GONE);
            }
        };
        this.focusChangeListener = new OnFocusChangeListener() {
            @Override 
            public void onFocusChange(View view, boolean z) {
                if (z) {
                    MaterialSearchView materialSearchView = MaterialSearchView.this;
                    materialSearchView.showKeyboard(materialSearchView.editText);
                }
            }
        };
        this.context = context;
        init();
    }

    private void init() {
        LayoutInflater.from(this.context).inflate(R.layout.material_search_view, (ViewGroup) this, true);
        ConstraintLayout constraintLayout = (ConstraintLayout) findViewById(R.id.material_search_container);
        this.materialSearchContainer = constraintLayout;
        constraintLayout.setVisibility(View.GONE);
        this.parent = (View) this.materialSearchContainer.getParent();
        EditText editText = (EditText) this.materialSearchContainer.findViewById(R.id.edit_text_search);
        this.editText = editText;
        editText.setOnFocusChangeListener(this.focusChangeListener);
        this.closeSearch = (AppCompatImageView) this.materialSearchContainer.findViewById(R.id.action_close_search);
        this.clearSearch = (AppCompatImageView) this.materialSearchContainer.findViewById(R.id.action_clear_search);
        this.closeSearch.setOnClickListener(this.closeListerner);
        this.clearSearch.setOnClickListener(this.clearSearchListner);
        this.editText.addTextChangedListener(new TextWatcher() { 
            @Override 
            public void afterTextChanged(Editable editable) {
            }

            @Override 
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override 
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                MaterialSearchView.this.onTextChanged(charSequence);
            }
        });
    }

    @Override 
    public void clearFocus() {
        hideKeyboard(this);
        super.clearFocus();
        this.editText.clearFocus();
    }

    
    public void onTextChanged(CharSequence charSequence) {
        Editable text = this.editText.getText();
        this.mCurrentQuery = text;
        if (!TextUtils.isEmpty(text)) {
            this.clearSearch.setVisibility(View.VISIBLE);
        } else {
            this.clearSearch.setVisibility(View.GONE);
        }
        OnQueryTextListener onQueryTextListener = this.mOnQueryTextListener;
        if (onQueryTextListener != null) {
            onQueryTextListener.onQueryTextChange(charSequence.toString());
        }
    }

    public void openSearch() {
        this.editText.setText("");
        this.editText.requestFocus();
        if (Build.VERSION.SDK_INT >= 21) {
            this.materialSearchContainer.setVisibility(View.VISIBLE);
        } else {
            this.materialSearchContainer.setVisibility(View.VISIBLE);
        }
    }

    public void closeSearch() {
        this.materialSearchContainer.setVisibility(View.GONE);
        this.editText.setText("");
        this.editText.clearFocus();
        hideKeyboard(this.editText);
    }

    public boolean isSearchOpen() {
        return this.materialSearchContainer.getVisibility() == View.VISIBLE;
    }

    private boolean isHardKeyboardAvailable() {
        return this.context.getResources().getConfiguration().keyboard != 1;
    }

    
    public void showKeyboard(View view) {
        if (Build.VERSION.SDK_INT <= 10 && view.hasFocus()) {
            view.clearFocus();
        }
        view.requestFocus();
        if (isHardKeyboardAvailable()) {
            return;
        }
        ((InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(view, 0);
    }

    private void hideKeyboard(View view) {
        ((InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void setOnQueryTextListener(OnQueryTextListener onQueryTextListener) {
        this.mOnQueryTextListener = onQueryTextListener;
    }
}
