package com.ray.router;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by qq448 on 2017/9/21.
 */

public class School implements Parcelable {
  private int id;
  private String name;
  private String address;

  public School() {
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(this.id);
    dest.writeString(this.name);
    dest.writeString(this.address);
  }

  protected School(Parcel in) {
    this.id = in.readInt();
    this.name = in.readString();
    this.address = in.readString();
  }

  public static final Creator<School> CREATOR = new Creator<School>() {
    @Override
    public School createFromParcel(Parcel source) {
      return new School(source);
    }

    @Override
    public School[] newArray(int size) {
      return new School[size];
    }
  };
}
