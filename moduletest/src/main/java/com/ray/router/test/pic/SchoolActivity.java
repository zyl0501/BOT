package com.ray.router.test.pic;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.ray.router.annotation.Action;
import com.ray.router.annotation.Autowired;
import com.ray.router.annotation.Interceptor;
import com.ray.router.launcher.Router;

/**
 * Created by qq448 on 2017/9/21.
 */
@Action(path = "/school", hasResult = true)
@Interceptor(clz = UserInterceptor.class)
public class SchoolActivity extends Activity {
  @Autowired(name = "school_id")
  int id;

  TextView schoolInfoTv;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_school);
    Router.I().inject(this);
//    id = getIntent().getIntExtra("school_id", -1);

    schoolInfoTv = (TextView) findViewById(R.id.school_info_text);
    schoolInfoTv.append("school " + id);
  }

  public void onStudentA(View view) {
    setResult(RESULT_OK, new Intent().putExtra("student", "Student A"));
    finish();
  }

  public void onStudentB(View view) {
    setResult(RESULT_OK, new Intent().putExtra("student", "Student B"));
    finish();
  }
}
