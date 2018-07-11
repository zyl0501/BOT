package com.ray.router.test.pic;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by qq448 on 2017/9/21.
 */

public class StudentModule implements Parcelable {
  private int id;
  private String name;
  private int age;

  public StudentModule(int id, String name, int age) {
    this.id = id;
    this.name = name;
    this.age = age;
  }

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

  public int getAge() {
    return age;
  }

  public void setAge(int age) {
    this.age = age;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(this.id);
    dest.writeString(this.name);
    dest.writeInt(this.age);
  }

  public StudentModule() {
  }

  protected StudentModule(Parcel in) {
    this.id = in.readInt();
    this.name = in.readString();
    this.age = in.readInt();
  }

  public static final Parcelable.Creator<StudentModule> CREATOR = new Parcelable.Creator<StudentModule>() {
    @Override
    public StudentModule createFromParcel(Parcel source) {
      return new StudentModule(source);
    }

    @Override
    public StudentModule[] newArray(int size) {
      return new StudentModule[size];
    }
  };
}
