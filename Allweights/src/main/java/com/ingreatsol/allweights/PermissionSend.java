package com.ingreatsol.allweights;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;

public class PermissionSend implements Parcelable {
    public String[] permissions;

    public PermissionSend(String[] permissions) {
        this.permissions = permissions;
    }

    protected PermissionSend(@NonNull Parcel in) {
        permissions = in.createStringArray();
    }

    public static final Creator<PermissionSend> CREATOR = new Creator<PermissionSend>() {
        @NonNull
        @Contract("_ -> new")
        @Override
        public PermissionSend createFromParcel(Parcel in) {
            return new PermissionSend(in);
        }

        @NonNull
        @Contract(value = "_ -> new", pure = true)
        @Override
        public PermissionSend[] newArray(int size) {
            return new PermissionSend[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeStringArray(permissions);
    }
}
