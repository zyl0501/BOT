package com.ray.router;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by qq448 on 2017/9/21.
 */

public class Student implements Parcelable {
  private int id;
  private String name;
  private int age;

  public Student(String name, int age) {
    this.name = name;
    this.age = age;
  }

  public Student() {
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

  protected Student(Parcel in) {
    this.id = in.readInt();
    this.name = in.readString();
    this.age = in.readInt();
  }

  public static final Creator<Student> CREATOR = new Creator<Student>() {
    @Override
    public Student createFromParcel(Parcel source) {
      return new Student(source);
    }

    @Override
    public Student[] newArray(int size) {
      return new Student[size];
    }
  };

  @Override
  public String toString() {
    return "Student{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", age=" + age +
        '}';
  }
}
