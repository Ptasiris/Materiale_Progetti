package com.example.melanomaly;

import android.graphics.Bitmap;

public interface AvvisaMedico {
    default void onIconClick(int pos, Bitmap bitmap){};
}

