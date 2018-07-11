package com.ray.router;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by qq448 on 2017/9/21.
 */

public class Teacher implements Parcelable {
  private int id;
  private String name;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(this.id);
    dest.writeString(this.name);
  }

  public Teacher() {
  }

  protected Teacher(Parcel in) {
    this.id = in.readInt();
    this.name = in.readString();
  }

  public static final Parcelable.Creator<Teacher> CREATOR = new Parcelable.Creator<Teacher>() {
    @Override
    public Teacher createFromParcel(Parcel source) {
      return new Teacher(source);
    }

    @Override
    public Teacher[] newArray(int size) {
      return new Teacher[size];
    }
  };
}
